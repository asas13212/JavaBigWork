package architecture;

import javax.swing.*;
import java.awt.*;

public class ShopLand extends Land
{

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
