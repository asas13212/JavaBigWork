package main;

import architecture.Land;
import props.Props;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * 功能描述：玩家类
 * @author cyt
 * @date 2026/5/14 22:04
 */
public class Player
{
    private Random random;

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

    private int prisonRound;

    private Point[] mapPoints;

    // 拥有的地产
    private ArrayList<Land> landOwned;

    // 剩余步数
    private int stepsRemaining;

    private int walkFrame;

    private Timer walkTimer;

    private Player other;

    private boolean isInToxic = false;

    private boolean isMoving;
    /**
     * 功能描述：玩家的初始化方法
     * @author cyt
     * @date 2026/5/14 15:04
     */
    public Player(int positionIndex, String name,Point srcPosition)
    {
        this.random = new Random();
        landOwned = new ArrayList<>();
        this.positionIndex = positionIndex;
        this.name = name;
        this.setMoney(ConstantNum.PLAYER_MONEY);
        this.setHp(ConstantNum.PLAYER_HP);
        moveTowards = 1;
        this.position = srcPosition;

    }

    /**
     * 功能描述：渲染静止状态方法
     * AI 改成可以支持逐帧行走
     * @author cyt
     * @date 2026/5/15 9:03
     */
    protected void renderStaticSprite(Graphics g,Point position)
    {
        // AI告诉我的巨大错误--可能有空指针异常
        if (g == null || position == null || moveSprites == null || moveSprites.length == 0)
            return;

        Image sheet = moveSprites[moveTowards - 1];
        int maxFrames = sheet.getWidth(null) / ConstantNum.FRAME_WIDTH;

        int sx = (walkFrame % maxFrames) * ConstantNum.FRAME_WIDTH;  // 切第几帧
        int sy = 0;

        int x = (int) position.getX();
        int y = (int) position.getY();

        g.drawImage(sheet,
                x, y, x + ConstantNum.FRAME_WIDTH, y + ConstantNum.FRAME_HEIGHT,   // 目标
                sx, sy, sx + ConstantNum.FRAME_WIDTH, sy + ConstantNum.FRAME_HEIGHT, // 源
                null);
    }


    /**
     * 功能描述：道具的使用逻辑
     * @author cyt
     * @date 2026/5/14 22:13
     */
    private void use(Props props, Player target)
    {
        props.isUsed(target);
    }


    /**
     * 功能描述：增加生命
     * @author cyt
     * @date 2026/5/15 9:05
     */
    public void hpDecrease(int num)
    {
        hp -= num;
    }

    /**
     * 功能描述：减少生命
     * @author cyt
     * @date 2026/5/15 9:05
     */
    public void hpIncrease(int num)
    {
        hp += num;
    }

    public void moneyIncrease(int num)
    {
        money += num;
    }

    public void moneyDecrease(int num)
    {
        money -= num;
        if (money < 0)
        {
            new Win(this);
        }
    }


    /**
     * 功能描述：导入贴图
     * @author cyt
     * @date 2026/5/15 18:35
     */
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

    /**
     * 功能描述：监狱计时器
     * @author cyt
     * @date 2026/5/15 18:35
     */
    public void prisonRoundDecrease(){
        this.prisonRound--;
    }

    /**
     * 功能描述：roll骰子
     * @author cyt
     * @date 2026/5/16 19:46
     */
    public int rollDice()
    {
        return random.nextInt(0,6) + 1;
    }

    /**
     * 功能描述：AI 提供的走一格方法
     * @author cyt
     * @date 2026/5/16 19:17
     */
    public boolean advanceOneStep()
    {
        if (stepsRemaining <= 0)
        {
            isMoving = false;
            return true;
        }

        walkFrame++;
        positionIndex = (positionIndex + 1) % mapPoints.length;
        if(positionIndex == 0)
            // 起点加钱
            this.money += ConstantNum.START_MONEY;

        // 改贴图方向
        if ( positionIndex >= 0 && positionIndex <= 8){
            moveTowards = 1;
        }else if (positionIndex >= 9 && positionIndex <= 15) {
            moveTowards = 2;
        }else if (positionIndex >= 16 && positionIndex <= 23){
            moveTowards = 3;
        }else {
            moveTowards = 4;
        }

        this.position = new Point(mapPoints[positionIndex]);
        stepsRemaining--;

        if (stepsRemaining <= 0){
            isMoving = false;
            return true;
        }

        return false;
    }

    /**
     * 功能描述：看看是不是在走
     * @author cyt
     * @date 2026/5/16 19:46
     */
    public boolean isWalking() { return isMoving; }

    /**
     * 功能描述：AI 开始走时候赋值
     * @author cyt
     * @date 2026/5/16 19:47
     */
    public void startWalk(int steps)
    {
        this.stepsRemaining = steps;
        this.walkFrame = 0;
        this.isMoving = true;
    }

    /**
     * 功能描述：重置行走帧
     * @author cyt
     * @date 2026/5/16 19:47
     */
    public void resetWalkFrame() { this.walkFrame = 0; }

    public String getOtherPlayerName()
    {
        return other.getName();
    }

    public void setOtherPlayer(Player other)
    {
        this.other = other;
    }

    public int getProperty()
    {
        return this.landOwned.toArray().length;
    }

    //<editor-fold desc="一些getter与getter方法">


    public void setMapPoints(Point[] mapPoints)
    {
        this.mapPoints = mapPoints;
    }

    public void addTilesOwned(Land land)
    {
        this.landOwned.add(land);
    }

    public Land getLandOwned(int num)
    {
        return landOwned.get(num);
    }

    public int getPrisonRound()
    {
        return prisonRound;
    }

    public void setPrisonRound(int prisonRound)
    {
        this.prisonRound = prisonRound;
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

    public Point getPosition()
    {
        return position;
    }

    public void setPosition(Point position)
    {
        this.position = position;
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

    public void setDice(int dice)
    {
        this.dice = dice;
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

    public int getMoveTowards()
    {
        return moveTowards;
    }

    public void setMoveTowards(int moveTowards)
    {
        this.moveTowards = moveTowards;
    }

    public Image[] getMoveSprites()
    {
        return moveSprites;
    }

    public void setMoveSprites(Image[] moveSprites)
    {
        this.moveSprites = moveSprites;
    }

    public Image getStaticSprite()
    {
        return staticSprite;
    }

    public void setStaticSprite(Image staticSprite)
    {
        this.staticSprite = staticSprite;
    }

    public void setInToxic(boolean inToxic)
    {
        isInToxic = inToxic;
    }

    public boolean isInToxic()
    {
        return isInToxic;
    }

    //</editor-fold>

}
