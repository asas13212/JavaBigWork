package main;

import architecture.Land;
import debug.Log;
import props.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.IntPredicate;

/**
 * 功能描述：玩家类
 * @author cyt
 * @date 2026/5/14 22:04
 */
public class Player
{
    private Random random;

    private  Integer nextDiceSides = null;

    private  Integer nextDiceValue = null;

    private int hp;

    private int money;
    // 按索引来确定位置
    private int positionIndex = 0;

    private Point position;

    private String name;

    private String status;

    private int moveTowards;

    private Image[] moveSprites;

    private Image staticSprite;

    private int barrierStopTurns = 0;

    private Point[] mapPoints;

    // 拥有的地产
    private ArrayList<Land> landOwned;

    // 剩余步数
    private int stepsRemaining;

    private int walkFrame;

    private Player other;

    private boolean isInToxic = false;

    private boolean isMoving;

    private IntPredicate onBarrierPlace;

    // AI: 地雷放置回调，由 MainMap 注入
    private IntPredicate onMinePlace;

    // 道具库存：用道具"名字"作为 key，避免 new 出来的 Prop 对象不一致导致数量回弹
    private HashMap<String, Integer> propsCount;

    /**
     * 功能描述：玩家的初始化方法
     * @author cyt
     * @date 2026/5/14 15:04
     */
    public Player(int positionIndex, String name,Point srcPosition)
    {
        this.random = new Random();
        landOwned = new ArrayList<>();
        propsCount = new HashMap<>();
        this.positionIndex = positionIndex;
        this.name = name;
        this.position = srcPosition;
        this.setMoney(ConstantNum.PLAYER_MONEY);
        this.setHp(ConstantNum.PLAYER_HP);
        moveTowards = 1;

        this.addProp(new BaoZi());
        this.addProp(new ExamWeek());
        this.addProp(new Mine());
        this.addProp(new Barrier());
        this.addProp(new HouseLevelUp());
        this.addProp(new Theft());
        this.addProp(new Dice());
        this.addProp(new IdCard());
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
        int w = sheet.getWidth(null);
        if (w <= 0) return;
        int maxFrames = w / ConstantNum.FRAME_WIDTH;
        if (maxFrames == 0) return;


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
    public void use(String propName, Player target)
    {
        Prop lookup = switch (propName) {
            case "包子"    -> new BaoZi();
            case "考试周"  -> new ExamWeek();
            // AI: 地雷需要注入放置回调，放在当前玩家脚下
            case "地雷" -> getMine();
            case "路障" -> getBarrier();
            case "偷取"    -> new Theft();
            case "万能骰子"-> new Dice();
            case "升级卡"  -> new HouseLevelUp();
            case "身份证"  -> new IdCard();
            default   -> null;
        };
        if (lookup == null) return;

        Integer count = propsCount.get(propName);
        if (count == null || count <= 0) return;

        int result = JOptionPane.showOptionDialog(
                null,
                lookup.getName() + ":" + lookup.getDescription() ,
                "是否使用",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"确定", "取消"}, // 自定义按钮
                "确定"
        );
        if ( result != 0 )
            return;

        if (!lookup.isUsed(target)) return;

        // AI: 路障和地雷在 onPlace 回调中自行扣除库存，此处跳过
        if (lookup instanceof Barrier || lookup instanceof Mine) return;

        if (count == 1) {
            propsCount.remove(propName);
        } else {
            propsCount.put(propName, count - 1);
        }
    }

    private Barrier getBarrier()
    {
        Barrier b = new Barrier();
        b.setOnPlace(index -> {
            boolean success = onBarrierPlace != null && onBarrierPlace.test(index);
            if (success) {
                Integer c = propsCount.get("路障");
                // 减少地雷数量的逻辑
                if (c != null) {
                    if (c == 1) propsCount.remove("路障");
                    else propsCount.put("路障", c - 1);
                }
            }
            return success;
        });
        return b;
    }

    private Mine getMine()
    {
        Mine m = new Mine();
        m.setOnPlace(index -> {
            boolean success = onMinePlace != null && onMinePlace.test(index);
            if (success) {
                Integer c = propsCount.get("地雷");
                if (c != null) {
                    if (c == 1) propsCount.remove("地雷");
                    else propsCount.put("地雷", c - 1);
                }
            }
            return success;
        });
        return m;
    }


    /**
     * 功能描述：增加生命
     * @author cyt
     * @date 2026/5/15 9:05
     */
    public void hpDecrease(int num)
    {
        Log.warn(this.getName() + " 生命减少 " + num + "，当前 HP=" + (hp - num));
        hp -= num;
        if (hp < 0)
        {
            Log.error(this.getName() + " HP 归零，游戏结束！");
            new Win(this);
        }
    }

    /**
     * 功能描述：减少生命
     * @author cyt
     * @date 2026/5/15 9:05
     */
    public void hpIncrease(int num)
    {
        Log.info(this.getName() + " 生命增加 " + num + "，当前 HP=" + (hp + num));
        hp += num;
    }

    /**
     * 功能描述：加钱
     * @author cyt
     * @date 2026/5/26 21:51
     */
    public void moneyIncrease(int num)
    {
        Log.info(this.getName() + " 金钱增加 " + num + "，当前 $" + (money + num));
        money += num;
    }


    /**
     * 功能描述：减钱
     * @author cyt
     * @date 2026/5/26 21:51
     */
    public void moneyDecrease(int num)
    {
        Log.warn(this.getName() + " 金钱减少 " + num + "，当前 $" + (money - num));
        money -= num;
        if (money < 0)
        {
            Log.error(this.getName() + " 金钱归零，游戏结束！");
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
     * 功能描述：roll骰子
     * @author cyt
     * @date 2026/5/16 19:46
     */
    public int rollDice()
    {
        if (nextDiceValue != null)
        {
            int value = nextDiceValue;
            nextDiceValue = null;
            return value;
        }
        int sides = consumeDiceSidesOrDefault(6);
        return random.nextInt(0,sides) + 1;
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

    /**
     * 功能描述：停止行走
     * @author cyt
     * @date 2026/5/30 10:29
     */
    public void cancelWalk()
    {
        this.stepsRemaining = 0;
        this.isMoving = false;
    }

    public String getOtherPlayerName()
    {
        return other.getName();
    }

    /**
     * 功能描述：获取别的玩家
     * @author cyt
     * @date 2026/5/30 10:28
     */
    public Player getOtherPlayer()
    {
        return other;
    }

    /**
     * 功能描述：设置别动玩家
     * @author cyt
     * @date 2026/5/30 10:28
     */
    public void setOtherPlayer(Player other)
    {
        this.other = other;
    }

    /**
     * 功能描述：找房产数量
     * @author cyt
     * @date 2026/5/30 10:29
     */
    public int getProperty()
    {
        return this.landOwned.toArray().length;
    }

    @Override
    public String toString()
    {
        return "Player{" +
                "name='" + name + '\'' +
                '}';
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

    /**
     * 返回当前玩家持有地产数量（等价于 getProperty()，但更直观）。
     */
    public int getLandOwnedCount()
    {
        return landOwned.size();
    }

    public int getBarrierStopTurns()
    {
        return barrierStopTurns;
    }

    public void setBarrierStopTurns(int barrierStopTurns)
    {
        this.barrierStopTurns = barrierStopTurns;
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

    public int getPropsNum()
    {
        return propsCount.size();
    }

    public HashMap<String, Integer> getProps()
    {
        return propsCount;
    }

    public void addProp(Prop prop)
    {
        if (prop == null || prop.getName() == null) return;
        String name = prop.getName();
        propsCount.put(name, propsCount.getOrDefault(name, 0) + 1);
    }

    public void setOnBarrierPlace(IntPredicate onBarrierPlace)
    {
        this.onBarrierPlace = onBarrierPlace;
    }

    public void setNextDiceSides(int sides)
    {
        this.nextDiceSides = sides;
    }

    public boolean hasNextDiceOverride()
    {
        return nextDiceSides != null;
    }
    public int consumeDiceSidesOrDefault(int defaultSize)
    {
        // 可以把它消耗掉，当作一次性的用品
        if (nextDiceSides != null)
        {
            int sides = nextDiceSides;
            nextDiceSides = null;
            return sides;
        }
        return defaultSize;
    }

    public boolean hasNextDiceSides()
    {
        return nextDiceSides != null;
    }

    public boolean hasNextDiceValue()
    {
        return nextDiceValue != null;
    }

    public void setNextDiceValue(int value)
    {
        this.nextDiceValue = value;
    }
    // AI: 地雷放置回调 setter
    public void setOnMinePlace(IntPredicate onMinePlace)
    {
        this.onMinePlace = onMinePlace;
    }

    //</editor-fold>

}
