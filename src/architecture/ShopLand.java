package architecture;

import main.Player;
import props.*;

import javax.swing.*;
import java.awt.*;

/**
 * 功能描述：商店地产，可升至4级，玩家可在商店中购买道具，AI自动选购
 * @author cyt & Claude
 * @date 2026/6/1
 */
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

    /**
     * 功能描述：构造商店地产，初始化等级上限、升级费用、过路费及建筑图像
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
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

    /**
     * 功能描述：玩家到达商店地产，AI自动选购或弹出人类玩家购物界面
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

        boolean isOwner = (getOwner() == player);

        // AI: AI 玩家自动选择
        if (player.isAI())
        {
            int choice = main.AIDecision.chooseShopItem(player, isOwner);
            if (choice < 0) return;

            Prop chosen = ITEMS[choice];
            int price = isOwner ? chosen.getPrice() * 4 / 5 : chosen.getPrice();

            if (player.getMoney() < price) return;

            player.moneyDecrease(price);
            player.addProp(chosen);

            if (!isOwner)
            {
                int commission = chosen.getPrice() / 5;
                getOwner().moneyIncrease(commission);
                main.AIDecision.showAIMessage(player.getName() + " 购买了 " + chosen.getName()
                        + "！\n$" + commission + " 税金已交给 " + getOwner().getName());
            }
            else
            {
                main.AIDecision.showAIMessage(player.getName() + " 购买了 " + chosen.getName() + "！（八折优惠）");
            }
            return;
        }

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

    /**
     * 功能描述：渲染已售商店的建筑图像
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
     * 功能描述：渲染未售商店的底图
     * @param g 图形上下文
     */
    @Override
    public void renderUnsold(Graphics g) {
        Point p = getPosition();
        if (p == null) return;
        g.drawImage(new ImageIcon("src/img/architecture/大地未售.png").getImage(), (int)p.getX(), (int)p.getY(), null);
    }
}
