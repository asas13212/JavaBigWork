package architecture;

import javax.swing.*;
import java.awt.*;

public class ParkLand extends Land
{

    public ParkLand(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.maxLevel = 0;
        this.priceLevelUp = new int[]{8000};
        this.tax = new int[]{1000};
        this.naiLong = new Image[]{new ImageIcon("src/img/architecture/naiLong/公园（蓝色）.png").getImage()};
        this.xiaoMei = new Image[]{new ImageIcon("src/img/architecture/xiaoMei/公园（红色）.png").getImage()};
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
