package architecture;

import main.Player;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

public class ParkLand extends Land
{
    private static final int TELEPORT_COST = 1500;
    private static final int MAX_INDEX = 29;

    private IntConsumer teleportHandler;

    public ParkLand(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.maxLevel = 0;
        this.priceLevelUp = new int[]{8000};
        this.tax = new int[]{1000};
        this.naiLong = new Image[]{new ImageIcon("src/img/architecture/naiLong/公园（蓝色）.png").getImage()};
        this.xiaoMei = new Image[]{new ImageIcon("src/img/architecture/xiaoMei/公园（红色）.png").getImage()};
    }

    public void setTeleportHandler(IntConsumer handler) { this.teleportHandler = handler; }

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

    @Override
    public void renderBuilding(Graphics g) {
        if (getOwner() == null) return;
        int x = (int) getPosition().getX() + offSetX;
        int y = (int) getPosition().getY() + offSetY;
        Image[] images = "naiLong".equals(getOwner().getName()) ? naiLong : xiaoMei;
        Image img = images[getHouseLevel()];
        g.drawImage(img, x - img.getWidth(null) + 270, y - img.getHeight(null) + 190, null);
    }

    @Override
    public void renderUnsold(Graphics g) {
        Point p = getPosition();
        if (p == null) return;
        g.drawImage(new ImageIcon("src/img/architecture/大地未售.png").getImage(), (int)p.getX(), (int)p.getY(), null);
    }
}
