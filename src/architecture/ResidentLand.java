package architecture;

import javax.swing.*;
import java.awt.*;

public class ResidentLand extends Land
{

    public ResidentLand(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.maxLevel = 5; // 最高五级，最低0级
        this.tax = new int[]{400,600,800,1600,3200,4800};
        this.priceLevelUp = new int[]{1500,1500,2000,2000,3000,5000};
        this.naiLong = new Image[]{
            new ImageIcon("/src/img/architecture/naiLong/red11.png").getImage(),
            new ImageIcon("/src/img/architecture/naiLong/red22.png").getImage(),
            new ImageIcon("/src/img/architecture/naiLong/red33.png").getImage(),
            new ImageIcon("/src/img/architecture/naiLong/red44.png").getImage(),
            new ImageIcon("/src/img/architecture/naiLong/red55.png").getImage(),
            new ImageIcon("/src/img/architecture/naiLong/red66.png").getImage(),
        };
        this.xiaoMei = new Image[]{
                new ImageIcon("/src/img/architecture/xiaoMei/red1.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/red2.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/red3.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/red4.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/red5.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/red6.png").getImage(),
        };
    }
}
