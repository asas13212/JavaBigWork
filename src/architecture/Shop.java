package architecture;

import main.Player;
import props.*;

import javax.swing.*;
import java.awt.*;

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

    public Shop(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.SHOP;
    }

    @Override
    public void onPlayerArrive(Player player)
    {
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
