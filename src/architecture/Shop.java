package architecture;

import main.AIDecision;
import main.Player;
import props.*;

import javax.swing.*;
import java.awt.*;

/**
 * 功能描述：商店格子，玩家可在此原价购买道具，AI自动选购
 * @author cyt & Claude
 * @date 2026/6/1
 */
public class Shop extends Tile
{

    private static final Prop[] ITEMS = {
            new BaoZi(),
            new ExamWeek(),
            new Mine(),
            new Barrier(),
            new Theft(),
            new Dice(),
            new HouseLevelUp(),
            new IdCard()
    };

    /**
     * 功能描述：构造商店格子
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
    public Shop(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.SHOP;
    }

    /**
     * 功能描述：玩家到达商店，AI自动选购或弹出人类玩家购物界面
     * @param player 到达的玩家
     * @author cyt & Claude
     */
    @Override
    public void onPlayerArrive(Player player)
    {
        // AI: 自主购物
        if (player.isAI())
        {
            // 复用 AIDecision 的商店选择逻辑（isOwner=false 即原价）
            int choice = AIDecision.chooseShopItem(player, false);
            if (choice < 0) return;

            Prop chosen = ITEMS[choice];
            if (player.getMoney() < chosen.getPrice()) return;

            player.moneyDecrease(chosen.getPrice());
            player.addProp(chosen);
            AIDecision.showAIMessage(player.getName() + " 在商店购买了 " + chosen.getName() + "（$" + chosen.getPrice() + "）");
            return;
        }

        JPanel grid = new JPanel(new GridLayout(4, 2, 10, 10));

        final int[] selected = {-1};

        for (int i = 0; i < ITEMS.length; i++)
        {
            Prop p = ITEMS[i];
            JButton btn = new JButton("<html><center>" + p.getName()
                    + "<br>$" + p.getPrice()
                    + "<br>" + p.getDescription() + "</center></html>");
            btn.setPreferredSize(new Dimension(180, 80));
            final int idx = i;
            btn.addActionListener(e -> {
                selected[0] = idx;
                SwingUtilities.windowForComponent(btn).dispose();
            });
            grid.add(btn);
        }

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("欢迎光临！每次光临只能购买一次", JLabel.CENTER), BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);

        JOptionPane.showOptionDialog(
                null,
                panel,
                "商店",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{"离开"},
                "离开"
        );

        int choice = selected[0];
        if (choice < 0)
            return;

        Prop chosen = ITEMS[choice];
        if (player.getMoney() < chosen.getPrice())
        {
            JOptionPane.showMessageDialog(null,
                    "钱不够！需要 $" + chosen.getPrice() + "，你只有 $" + player.getMoney());
            return;
        }

        player.moneyDecrease(chosen.getPrice());
        player.addProp(chosen);
        JOptionPane.showMessageDialog(null, "成功购买 " + chosen.getName() + "！");
    }
}
