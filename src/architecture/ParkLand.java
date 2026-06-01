package architecture;

import main.AIDecision;
import main.Player;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

/**
 * 功能描述：公园地产，可传送至任意格子，AI自动选择伏击位置
 * @author cyt & Claude
 * @date 2026/6/1
 */
public class ParkLand extends Land
{
    private static final int TELEPORT_COST = 1500;
    private static final int MAX_INDEX = 29;

    private IntConsumer teleportHandler;

    /**
     * 功能描述：构造公园地产，初始化等级上限、价格、过路费及建筑图像
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
    public ParkLand(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.maxLevel = 0;
        this.priceLevelUp = new int[]{8000};
        this.tax = new int[]{1000};
        this.naiLong = new Image[]{new ImageIcon("src/img/architecture/naiLong/公园（蓝色）.png").getImage()};
        this.xiaoMei = new Image[]{new ImageIcon("src/img/architecture/xiaoMei/公园（红色）.png").getImage()};
    }

    /**
     * 功能描述：设置传送处理器回调
     * @param handler 传送处理器
     */
    public void setTeleportHandler(IntConsumer handler) { this.teleportHandler = handler; }

    /**
     * 功能描述：玩家到达公园，AI自动传送至伏击位置或弹出人类玩家传送界面
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

        // AI: 自主决定传送
        if (player.isAI())
        {
            if (teleportHandler == null) return;

            if (!isOwner)
            {
                if (player.getMoney() < TELEPORT_COST + 1000) return; // 留够钱
                player.moneyDecrease(TELEPORT_COST);
                getOwner().moneyIncrease(TELEPORT_COST);
                AIDecision.showAIMessage(player.getName() + " 支付 $" + TELEPORT_COST + " 使用公园传送");
            }

            // AI 传送到对手后方 3 格（伏击位置）
            int target = (player.getOtherPlayer().getPositionIndex() - 3 + MAX_INDEX + 1) % (MAX_INDEX + 1);
            teleportHandler.accept(target);
            AIDecision.showAIMessage(player.getName() + " 传送到 " + target + " 号格子");
            return;
        }

        if (!isOwner)
        {
            int choice = JOptionPane.showOptionDialog(null,
                    "公园传送门！花 $" + TELEPORT_COST + " 传送到任意格子？",
                    getName(),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, new String[]{"传送！", "离开"}, "离开");
            if (choice != 0) return;

            if (player.getMoney() < TELEPORT_COST)
            {
                JOptionPane.showMessageDialog(null, "钱不够！需要 $" + TELEPORT_COST + "，你只有 $" + player.getMoney());
                return;
            }
            player.moneyDecrease(TELEPORT_COST);
            getOwner().moneyIncrease(TELEPORT_COST);
            JOptionPane.showMessageDialog(null, "支付了 $" + TELEPORT_COST + " 给 " + getOwner().getName());
        }

        if (teleportHandler == null) return;

        String input = JOptionPane.showInputDialog(null,
                "输入目标格子索引 (0~" + MAX_INDEX + ")",
                getName() + " · 传送",
                JOptionPane.QUESTION_MESSAGE);

        if (input == null) return;

        int target;
        try {
            target = Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "请输入有效数字！");
            return;
        }

        if (target < 0 || target > MAX_INDEX)
        {
            JOptionPane.showMessageDialog(null, "索引超出范围 (0~" + MAX_INDEX + ")！");
            return;
        }

        teleportHandler.accept(target);
    }

    /**
     * 功能描述：渲染已售公园的建筑图像
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
     * 功能描述：渲染未售公园的底图
     * @param g 图形上下文
     */
    @Override
    public void renderUnsold(Graphics g) {
        Point p = getPosition();
        if (p == null) return;
        g.drawImage(new ImageIcon("src/img/architecture/大地未售.png").getImage(), (int)p.getX(), (int)p.getY(), null);
    }
}
