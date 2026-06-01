package architecture;

import debug.Log;
import main.Player;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * 功能描述：旅馆地产，可升至4级，玩家支付费用后随机前进1~6步，AI自动决定
 * @author cyt & Claude
 * @date 2026/6/1
 */
public class HotelLand extends Land
{
    private static final Random RNG = new Random();
    private static final int WALK_COST = 800;

    /**
     * 功能描述：构造旅馆地产，初始化等级上限、升级费用、过路费及建筑图像
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
    public HotelLand(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.maxLevel = 4;
        this.offSetX = 0;
        this.offSetY = -6;
        this.priceLevelUp = new int[]{3000,4500,5000,6800,8000};
        this.tax = new int[]{400,1000,3000,3000,5000};
        this.naiLong = new Image[]{
          new ImageIcon("src/img/architecture/naiLong/旅馆1（蓝色）.png").getImage(),
          new ImageIcon("src/img/architecture/naiLong/旅馆2（蓝色）.png").getImage(),
          new ImageIcon("src/img/architecture/naiLong/旅馆3蓝色.png").getImage(),
          new ImageIcon("src/img/architecture/naiLong/旅馆4（蓝色）.png").getImage(),
          new ImageIcon("src/img/architecture/naiLong/旅馆5（蓝）左.png").getImage(),
        };
        this.xiaoMei = new Image[]{
                new ImageIcon("src/img/architecture/xiaoMei/旅馆1（红色）.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/旅馆2（红色）右.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/旅馆3（红色）.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/旅馆4(红色)右.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/旅馆5（红）右.png").getImage(),
        };
    }

    /**
     * 功能描述：玩家到达旅馆，AI自动决定或弹出人类玩家交互界面
     * @param player 到达的玩家
     * @author cyt & Claude
     */
    @Override
    public void onPlayerArrive(Player player)
    {
        if (getOwner() == null)
        {
            super.onPlayerArrive(player);
            return;
        }

        if (player.getBarrierStopTurns() > 0)
            return;

        boolean isOwner = (getOwner() == player);

        // 非业主先交过路费
        if (!isOwner)
        {
            int taxAmount = tax[getHouseLevel()];
            Log.info(player.getName() + " 交税 $" + taxAmount + " 给 " + getOwner().getName() + "（旅馆 Lv" + getHouseLevel() + "）");
            if (player.isAI())
            {
                player.moneyDecrease(taxAmount);
                getOwner().moneyIncrease(taxAmount);
                main.AIDecision.showAIMessage(player.getName() + " 到了 " + getOwner().getName()
                        + " 的旅馆，交税 $" + taxAmount);
            }
            else
            {
                JOptionPane.showMessageDialog(null,
                        "到了" + getOwner().getName() + "的旅馆，交税 $" + taxAmount,
                        getName(), JOptionPane.INFORMATION_MESSAGE);
                player.moneyDecrease(taxAmount);
                getOwner().moneyIncrease(taxAmount);
            }
        }

        // AI: AI 玩家自动决定是否住店
        if (player.isAI())
        {
            if (!main.AIDecision.shouldUseHotel(player, isOwner))
                return;

            if (!isOwner)
            {
                if (player.getMoney() < WALK_COST) return;
                player.moneyDecrease(WALK_COST);
                getOwner().moneyIncrease(WALK_COST);
                main.AIDecision.showAIMessage(player.getName() + " 支付了 $" + WALK_COST + " 给 " + getOwner().getName());
            }

            int steps = RNG.nextInt(6) + 1;
            main.AIDecision.showAIMessage(player.getName() + " 骰子点数：" + steps + "，前进 " + steps + " 步！");
            player.startWalk(steps);
            return;
        }

        String msg = isOwner ? "欢迎老板！免费随机前进 1~6 步" : "欢迎光临旅馆！花 $" + WALK_COST + " 随机前进 1~6 步";

        int choice = JOptionPane.showOptionDialog(null, msg, getName(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[]{"出发！", "离开"}, "离开");

        if (choice != 0) return;

        if (!isOwner)
        {
            if (player.getMoney() < WALK_COST)
            {
                JOptionPane.showMessageDialog(null, "钱不够！需要 $" + WALK_COST + "，你只有 $" + player.getMoney());
                return;
            }
            player.moneyDecrease(WALK_COST);
            getOwner().moneyIncrease(WALK_COST);
            JOptionPane.showMessageDialog(null, "支付了 $" + WALK_COST + " 给 " + getOwner().getName());
        }

        int steps = RNG.nextInt(6) + 1;
        JOptionPane.showMessageDialog(null, "骰子点数：" + steps + "，前进 " + steps + " 步！");
        player.startWalk(steps);
    }

    /**
     * 功能描述：渲染已售旅馆的建筑图像
     * @param g 图形上下文
     */
    @Override
    public void renderBuilding(Graphics g) {
        if (getOwner() == null) return;
        int x = (int) getPosition().getX() + offSetX;
        int y = (int) getPosition().getY() + offSetY;
        Image[] images = "naiLong".equals(getOwner().getName()) ? naiLong : xiaoMei;
        Image img = images[getHouseLevel()];
        g.drawImage(img, x - img.getWidth(null) + 270, y - img.getHeight(null) + 190, null);
    }


    /**
     * 功能描述：渲染未售旅馆的底图
     * @param g 图形上下文
     */
    @Override
    public void renderUnsold(Graphics g) {
        Point p = getPosition();
        if (p == null) return;
        g.drawImage(new ImageIcon("src/img/architecture/大地未售2.png").getImage(), (int)p.getX(), (int)p.getY(), null);
    }
}
