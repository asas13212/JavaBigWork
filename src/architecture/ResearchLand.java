package architecture;

import javax.swing.*;
import java.awt.*;

public class ResearchLand extends Land
{

    public ResearchLand(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.maxLevel = 4;
        this.priceLevelUp = new int[]{2000,2000,3000,5000,8000};
        this.tax = new int[]{800,1600,3200,6400,12000};
        this.offSetX = 0;
        this.offSetY = 23;
        this.naiLong = new Image[]{
                new ImageIcon("src/img/architecture/naiLong/yanjiu12.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/yanjiu22.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/yanjiu32.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/yanjiu42.png").getImage(),
                new ImageIcon("src/img/architecture/naiLong/yanjiu52.png").getImage(),
        };
        this.xiaoMei = new Image[]{
                new ImageIcon("src/img/architecture/xiaoMei/yanjiu1.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/yanjiu2.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/yanjiu3.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/yanjiu4.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/yanjiu5.png").getImage(),
        };
    }
}
