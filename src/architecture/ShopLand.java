package architecture;

import javax.swing.*;
import java.awt.*;

public class ShopLand extends Land
{

    public ShopLand(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.maxLevel = 4;
        this.tax = new int[]{800,1600,3200,6400,7000};
        this.priceLevelUp = new int[]{2000,2000,3000,5000,6000};
        this.naiLong = new Image[]{
                new ImageIcon("/src/img/architecture/naiLong/shop12.png").getImage(),
                new ImageIcon("/src/img/architecture/naiLong/shop22.png").getImage(),
                new ImageIcon("/src/img/architecture/naiLong/shop32.png").getImage(),
                new ImageIcon("/src/img/architecture/naiLong/shop42.png").getImage(),
        };
        this.xiaoMei = new Image[]{
                new ImageIcon("/src/img/architecture/xiaoMei/shop1.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/shop2.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/shop3.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/shop4.png").getImage(),
                new ImageIcon("/src/img/architecture/xiaoMei/shop5.png").getImage(),
        };
    }
}
