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
        this.naiLong = new Image[]{new ImageIcon("/src/img/architecture/naiLong/公园（蓝色）.png").getImage()};
        this.xiaoMei = new Image[]{new ImageIcon("/src/img/architecture/xiaoMei/公园（红色）.png").getImage()};
    }
}
