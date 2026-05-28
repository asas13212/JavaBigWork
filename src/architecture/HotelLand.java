package architecture;

import javax.swing.*;
import java.awt.*;

public class HotelLand extends Land
{

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
        g.drawImage(new ImageIcon("src/img/architecture/大地未售2.png").getImage(), (int)p.getX(), (int)p.getY(), null);
    }
}
