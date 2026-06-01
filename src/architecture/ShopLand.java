package architecture;

import main.Player;
import props.*;

import javax.swing.*;
import java.awt.*;

public class ShopLand extends Land
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

    public ShopLand(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.maxLevel = 4;
        this.priceLevelUp = new int[]{2000,2000,3000,5000,6000};
        this.tax = new int[]{800,1600,3200,6400,7000};
        this.naiLong = new Image[]{
                new ImageIcon("src/img/architecture/naiLong/shop12.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/shop22.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/shop32.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/shop42.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/shop52.png").getImage(),
        };
        this.xiaoMei = new Image[]{
                new ImageIcon("src/img/architecture/xiaoMei/shop1.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/shop2.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/shop3.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/shop4.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/shop5.png").getImage(),
        };
    }

    @Override
    public void onPlayerArrive(Player player)
    {
        if (getOwner() == null)
        {
            super.onPlayerArrive(player);
            return;
        }

        boolean isOwner = (getOwner() == player);

        JPanel grid = new JPanel(new GridLayout(4, 2, 10, 10));
        final int[] selected = {-1};

        for (int i = 0; i < ITEMS.length; i++)
        {
            Prop p = ITEMS[i];
            int price = isOwner ? p.getPrice() * 4 / 5 : p.getPrice();
            JButton btn = new JButton("<html><center>" + p.getName()
                    + "<br>$" + price
                    + "<br>" + p.getDescription() + "</center></html>");
            btn.setPreferredSize(new Dimension(180, 80));
            final int idx = i;
            btn.addActionListener(e -> {
                selected[0] = idx;
                SwingUtilities.windowForComponent(btn).dispose();
            });
            grid.add(btn);
        }

        String title = isOwner ? getName() + "（本店 · 八折）" : getName();
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("欢迎光临！每次只能购买一次", JLabel.CENTER), BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);

        JOptionPane.showOptionDialog(null, panel, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[]{"离开"}, "离开");

        int choice = selected[0];
        if (choice < 0) return;

        Prop chosen = ITEMS[choice];
        int price = isOwner ? chosen.getPrice() * 4 / 5 : chosen.getPrice();

        if (player.getMoney() < price)
        {
            JOptionPane.showMessageDialog(null,
                    "钱不够！需要 $" + price + "，你只有 $" + player.getMoney());
            return;
        }

        player.moneyDecrease(price);
        player.addProp(chosen);

        if (!isOwner)
        {
            int commission = chosen.getPrice() / 5;
            getOwner().moneyIncrease(commission);
            JOptionPane.showMessageDialog(null,
                    "成功购买 " + chosen.getName() + "！\n$" + commission + " 税金已交给 " + getOwner().getName());
        }
        else
        {
            JOptionPane.showMessageDialog(null,
                    "成功购买 " + chosen.getName() + "！（八折优惠）");
        }
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
