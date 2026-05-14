import javax.swing.*;
import java.awt.*;
import java.awt.font.ImageGraphicAttribute;

/**
 * 功能描述：玩家类
 * @author cyt
 * @date 2026/5/14 22:04
 */
public class Player
{
    private int hp;

    private int money;
    // 按索引来确定位置
    private int positionIndex = 0;

    private Point position;

    private String name;

    private int dice;

    private int round;

    private String status;

    private int moveTowards;

    private Image[] moveSprites;

    private Image staticSprite;

    private int frameIndex;

    /**
     * 功能描述：玩家的初始化方法
     * @author cyt
     * @date 2026/5/14 15:04
     */
    public Player(int positionIndex, String name,Point srcPosition)
    {
        this.positionIndex = positionIndex;
        this.name = name;
        this.setMoney(10000);
        this.setHp(100);
        moveTowards = 1;
        this.position = srcPosition;

    }

    protected void renderStaticSprite(Graphics g,Point position)
    {
        // AI告诉我的巨大错误--可能有空指针异常
        if (g == null || position == null || moveSprites == null || moveSprites.length == 0) {
            return;
        }
        int x , y;
        x = (int) position.getX();
        y = (int) position.getY();
        g.drawImage(moveSprites[moveTowards+1],
                x, y,                 // 目标左上
                x + 63, y + 100,      // 目标右下
                0, 0,                 // 原图左上
                63, 100,              // 原图右下
                null);
    }

    /**
     * 功能描述：传入精灵表,让构造方法里面清新一点
     * @author cyt
     * @date 2026/5/14 16:05
     */
    private static void loadSprites(Player naiLong, Player xiaoMei)
    {
        naiLong.setMoveSprites(new String[]{
                "src/img/player/男1.png",
                "src/img/player/男2.png",
                "src/img/player/男3.png",
                "src/img/player/男4.png"
        });

        xiaoMei.setMoveSprites(new String[]{
                "src/img/player/女1.png",
                "src/img/player/女2.png",
                "src/img/player/女3.png",
                "src/img/player/女4.png",
        });
    }

    /**
     * 功能描述：道具的使用逻辑
     * @author cyt
     * @date 2026/5/14 22:13
     */
    private void use(Props props,Player target)
    {
        props.isUsed(target);
    }


    //<editor-fold desc="一些getter与getter方法">


    public Point getPosition()
    {
        return position;
    }

    public void setPosition(Point position)
    {
        this.position = position;
    }

    public void setMoveSprites(String[] filePath)
    {
        int index = filePath.length;
        moveSprites = new Image[index];

        for (int i = 0; i < index; i++)
        {
            moveSprites[i] = (new ImageIcon(filePath[i]).getImage());
        }

        staticSprite = moveSprites[0];

    }

    public Image getStaticSprite()
    {
        return staticSprite;
    }

    public Image[] getMoveSprites()
    {
        return moveSprites;
    }

    public int getHp()
    {
        return hp;
    }

    public void setHp(int hp)
    {
        this.hp = hp;
    }

    public int getMoney()
    {
        return money;
    }

    public void setMoney(int money)
    {
        this.money = money;
    }

    public int getPositionIndex()
    {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex)
    {
        this.positionIndex = positionIndex;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getDice()
    {
        return dice;
    }

    public int getRound()
    {
        return round;
    }

    public void setRound(int round)
    {
        this.round = round;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    //</editor-fold>

}
