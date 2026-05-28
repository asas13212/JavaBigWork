package architecture;

import javax.swing.*;
import java.awt.*;

public class GymLand extends Land
{

    public GymLand(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.maxLevel = 4;
        this.priceLevelUp = new int[]{2000,2000,2000,3000,5000,8000};
        this.tax = new int[]{400,800,1600,3200,6400,12000};
        this.offSetX = 0;
        this.offSetY = 20;
        this.naiLong = new Image[]{
                new ImageIcon("src/img/architecture/naiLong/tiyub.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/tiyu2.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/体育馆3蓝.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/体育馆4蓝.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/体育馆5蓝.png").getImage(),
        };
        this.xiaoMei = new Image[]{
                new ImageIcon("src/img/architecture/xiaoMei/tiyu1.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/tiyu.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/体育馆3红.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/体育馆4红.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/体育馆5红.png").getImage(),
        };
    }

    @Override
    public void renderBuilding(Graphics g) {
        if (getOwner() == null) return;
        int x = (int) getPosition().getX() + offSetX;
        int y = (int) getPosition().getY() + offSetY;
        Image[] images = "naiLong".equals(getOwner().getName()) ? naiLong : xiaoMei;
        g.drawImage(images[getHouseLevel()], x + 5, y + 10, 126, 94, null);
    }

    @Override
    public void renderUnsold(Graphics g) {
        Point p = getPosition();
        if (p == null) return;
        g.drawImage(new ImageIcon("src/img/architecture/小地未售2.png").getImage(), (int)p.getX(), (int)p.getY(), null);
    }
}
