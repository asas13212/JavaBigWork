package architecture;

import javax.swing.*;
import java.awt.*;

/**
 * 功能描述：住宅地产，可升至5级，展示对应等级建筑图像
 * @author cyt
 * @date 2026/6/1
 */
public class ResidentLand extends Land
{
    /**
     * 功能描述：构造住宅地产，初始化等级上限、升级费用、过路费及建筑图像
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
    public ResidentLand(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.maxLevel = 5; // 最高五级，最低0级
        this.offSetX = 1;
        this.offSetY = 20;
        this.priceLevelUp = new int[]{1500,1500,2000,2000,3000,5000};
        this.tax = new int[]{400,600,800,1600,3200,4800};
        this.naiLong = new Image[]{
            new ImageIcon("src/img/architecture/naiLong/red11.png").getImage(),
            new ImageIcon("src/img/architecture/naiLong/red22.png").getImage(),
            new ImageIcon("src/img/architecture/naiLong/red33.png").getImage(),
            new ImageIcon("src/img/architecture/naiLong/red44.png").getImage(),
            new ImageIcon("src/img/architecture/naiLong/red55.png").getImage(),
            new ImageIcon("src/img/architecture/naiLong/red66.png").getImage(),
        };
        this.xiaoMei = new Image[]{
                new ImageIcon("src/img/architecture/xiaoMei/red1.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/red2.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/red3.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/red4.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/red5.png").getImage(),
                new ImageIcon("src/img/architecture/xiaoMei/red6.png").getImage(),
        };
    }

    /**
     * 功能描述：渲染未售住宅的底图，根据位置选择不同底图
     * @param g 图形上下文
     */
    @Override
    public void renderUnsold(Graphics g) {
        Point p = getPosition();
        if (p == null) return;
        int idx = getPositionIndex();
        String name = (idx >= 0 && idx <= 9) || (idx >= 16 && idx <= 21)
            ? "小地未售2.png" : "小地未售.png";
        g.drawImage(new ImageIcon("src/img/architecture/" + name).getImage(), (int)p.getX(), (int)p.getY(), null);
    }
}
