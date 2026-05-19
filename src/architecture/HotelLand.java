package architecture;

import javax.swing.*;
import java.awt.*;

public class HotelLand extends Land
{

    public HotelLand(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.maxLevel = 4;
        this.priceLevelUp = new int[]{3000,4500,5000,6800,8000};
        this.tax = new int[]{400,1000,3000,3000,5000};
        this.naiLong = new Image[]{
          new ImageIcon("/src/img/architecture/naiLong/旅馆1（蓝色）.png").getImage(),
          new ImageIcon("/src/img/architecture/naiLong/旅馆2（蓝色）.png").getImage(),
          new ImageIcon("/src/img/architecture/naiLong/旅馆3蓝色.png").getImage(),
          new ImageIcon("/src/img/architecture/naiLong/旅馆4（蓝色）.png").getImage(),
          new ImageIcon("/src/img/architecture/naiLong/旅馆5（蓝）左.png").getImage(),
        };
        this.xiaoMei = new Image[]{
                new ImageIcon("/src/img/architecture/xiaoMei/旅馆1（红色）.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/旅馆2（红色）右.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/旅馆3（红色）.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/旅馆4(红色)右.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/旅馆5（红）右.png").getImage(),
        };
    }
}
