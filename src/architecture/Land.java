package architecture;

import debug.Log;
import main.*;

import javax.swing.*;
import java.awt.*;

/**
 * 功能描述：地产抽象基类，处理购买、升级、收税等核心逻辑，支持AI自动决策
 * @author cyt & Claude
 * @date 2026/6/1
 */
public abstract class Land extends Tile
{
    private Player owner;

    private int houseLevel = 0;

    // 过路费
    public int[] tax;

    // 升级费用
    protected int[] priceLevelUp;

    protected int maxLevel;

    protected Image[] naiLong;
    protected Image[] xiaoMei;

    protected int offSetX;
    protected int offSetY;



    /**
     * 功能描述：构造地产格子，初始等级为0，类型设为PROPERTY
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
    public Land(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.houseLevel = 0;
        this.tileType = TileType.PROPERTY;

    }

    /**
     * 功能描述：玩家到达地产，处理购买、交税、升级等逻辑，AI自动决策
     * @param player 到达的玩家
     * @author cyt & Claude
     */
    @Override
    public void onPlayerArrive(Player player)
    {
        if (this.owner == null)
        {
            // AI
            if (player.isAI())
            {
                if (AIDecision.shouldBuyLand(player, this) && player.getMoney() >= priceLevelUp[houseLevel])
                {
                    player.moneyDecrease(priceLevelUp[houseLevel]);
                    this.setOwner(player); player.addTilesOwned(this);
                    AIDecision.showAIMessage(player.getName() + " 购买了 " + this.getName() + "，花费 $" + priceLevelUp[houseLevel]);
                }
                return;
            }
            // 联机：弹窗选，发服务端 + 本地执行
            if (player.isOnline())
            {
                int r = JOptionPane.showOptionDialog(null,
                    "购买当前房产:" + this.getName() + "? 价格是" + this.getCurrentPrice(),
                    "确认", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, new String[]{"确定", "取消"}, "确定");
                if (r == 0)
                {
                    player.getGameController().onLandChoice(true);
                    if (player.getMoney() >= priceLevelUp[houseLevel])
                    {
                        player.moneyDecrease(priceLevelUp[houseLevel]);
                        this.setOwner(player);
                        player.addTilesOwned(this);
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "你没资格啊没资格");
                    }
                }
                return;
            }
            // 本地
            int result = JOptionPane.showOptionDialog(null,
                    "购买当前房产:" + this.getName() + "? 价格是" + this.getCurrentPrice(),
                    "确认", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, new String[]{"确定", "取消"}, "确定");
            if (result == 0 && player.getMoney() >= priceLevelUp[houseLevel])
            {
                player.moneyDecrease(priceLevelUp[houseLevel]);
                this.setOwner(player); player.addTilesOwned(this);
            }
            else if (result == 0)
                JOptionPane.showMessageDialog(null, "你没资格啊没资格");

        }
        else if (owner != player)
        {
            int taxAmount = tax[houseLevel];
            Log.info(player.getName() + " 交税 $" + taxAmount + " 给 " + owner.getName());
            if (player.isAI())
                AIDecision.showAIMessage(player.getName() + " 到了 " + owner.getName() + " 的领地，交税 $" + taxAmount);
            else
                JOptionPane.showMessageDialog(null, "交付" + taxAmount, "到了" + player.getOtherPlayerName() + "的领地", JOptionPane.INFORMATION_MESSAGE);
            player.moneyDecrease(taxAmount);
            owner.moneyIncrease(taxAmount);
        }
        else
        {
            if (houseLevel >= maxLevel) return;
            // AI
            if (player.isAI())
            {
                if (AIDecision.shouldUpgradeLand(player, this) && player.getMoney() >= priceLevelUp[houseLevel+1])
                {
                    player.moneyDecrease(priceLevelUp[houseLevel+1]);
                    this.houseLevel++;
                    AIDecision.showAIMessage(player.getName() + " 升级了 " + this.getName() + " 到 " + houseLevel + " 级，花费 $" + priceLevelUp[houseLevel]);
                }
                return;
            }
            // 联机：弹窗选，发服务端 + 本地执行
            if (player.isOnline())
            {
                int r = JOptionPane.showOptionDialog(null,
                    "升级当前房产:" + this.getName() + "? 价格是" + this.priceLevelUp[houseLevel+1],
                    "确认", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, new String[]{"确定", "取消"}, "确定");
                if (r == 0)
                {
                    player.getGameController().onUpgradeChoice(true);
                    if (player.getMoney() >= priceLevelUp[houseLevel+1])
                    {
                        player.moneyDecrease(priceLevelUp[houseLevel+1]);
                        this.houseLevel++;
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "你没资格啊没资格");
                    }
                }
                return;
            }
            // 本地
            int result = JOptionPane.showOptionDialog(null,
                    "升级当前房产:" + this.getName() + "? 价格是" + this.priceLevelUp[houseLevel+1],
                    "确认", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, new String[]{"确定", "取消"}, "确定");
            if (result == 0 && player.getMoney() >= priceLevelUp[houseLevel+1])
            {
                player.moneyDecrease(priceLevelUp[houseLevel+1]);
                this.houseLevel++;
            }
            else if (result == 0)
                JOptionPane.showMessageDialog(null, "你没资格啊没资格");
        }
    }

    /**
     * 功能描述：获取地产所有者
     * @return 地产所有者
     */
    public Player getOwner()
    {
        return owner;
    }

    /**
     * 功能描述：获取当前房屋等级
     * @return 房屋等级
     */
    public int getHouseLevel()
    {
        return houseLevel;
    }

    /**
     * 获取该地产的最高等级（用于升级卡等逻辑判断）。
     */
    public int getMaxLevel()
    {
        return maxLevel;
    }

    /**
     * 功能描述：设置地产所有者
     * @param owner 所有者玩家
     */
    public void setOwner(Player owner)
    {
        this.owner = owner;
    }

    /**
     * 功能描述：这里是需要升级的钱
     * @author cyt
     * @date 2026/5/18 21:08
     */
    public int getCurrentPrice()
    {
        return priceLevelUp[houseLevel];
    }

    /**
     * 功能描述：获取升级到下一级的价格（最高级时返回最大值）
     * @return 升级价格
     * @author cyt & Claude
     * @date 2026/6/6
     */
    public int getUpgradePrice()
    {
        if (houseLevel >= maxLevel) return Integer.MAX_VALUE;
        return priceLevelUp[houseLevel + 1];
    }

    /**
     * 功能描述：房屋升级，防止越界
     */
    public void houseLevelUp()
    {
        // 防止越界（否则渲染 images[houseLevel] 会数组越界）
        if (houseLevel < maxLevel)
        {
            this.houseLevel++;
        }
    }

    /**
     * 功能描述：判断房屋是否还能升级
     * @return 是否可升级
     */
    public boolean canLevelUp()
    {
        return houseLevel < maxLevel;
    }

    /**
     * 功能描述：获取耐龙阵营的建筑图像数组
     * @return 建筑图像数组
     */
    public Image[] getNaiLongImg(){
        return naiLong;
    }

    /**
     * 功能描述：获取小美阵营的建筑图像数组
     * @return 建筑图像数组
     */
    public Image[] getXiaoMeiImg(){
        return xiaoMei;
    }

    /**
     * 功能描述：获取建筑渲染X偏移量
     * @return X偏移量
     */
    public int getOffSetX()
    {
        return offSetX;
    }

    /**
     * 功能描述：获取建筑渲染Y偏移量
     * @return Y偏移量
     */
    public int getOffSetY()
    {
        return offSetY;
    }

    /**
     * 功能描述：渲染已售地产的建筑图像
     * @param g 图形上下文
     */
    public void renderBuilding(Graphics g) {
        if (owner == null) return;
        int x = (int) getPosition().getX() + offSetX;
        int y = (int) getPosition().getY() + offSetY;
        Image[] images = "naiLong".equals(owner.getName()) ? naiLong : xiaoMei;
        Image img = images[houseLevel];
        g.drawImage(img, x + 130 - img.getWidth(null), y - img.getHeight(null) + 100, null);
    }

    /**
     * 功能描述：渲染未售地产的底图（子类覆写）
     * @param g 图形上下文
     */
    public void renderUnsold(Graphics g) {}
}
