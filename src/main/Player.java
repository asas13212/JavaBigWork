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
 * 功能描述：玩家类，管理玩家属性（金钱/血量/位置）、地产、道具和行走逻辑
 * @author cyt & Claude
 * @date 2026/6/1 21:00
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

    // 联机: 游戏控制器引用
    private GameController gameController;
    public GameController getGameController() { return gameController; }
    public void setGameController(GameController gc) { this.gameController = gc; }

    // AI: 是否为 AI 玩家
    private boolean isAI = false;

    // AI: 路障目标 tile 索引（AI 预设，人类为 null）
    private Integer barrierAiTargetIndex = null;

    private IntPredicate onBarrierPlace;

    // AI: 地雷放置回调，由 MainMap 注入
    private IntPredicate onMinePlace;

    // 联机: session 标识
    private String sessionId;
    private boolean isOnline = false;

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

//        this.addProp(new BaoZi());
//        this.addProp(new ExamWeek());
//        this.addProp(new Mine());
//        this.addProp(new Barrier());
//        this.addProp(new HouseLevelUp());
//        this.addProp(new Theft());
//        this.addProp(new Dice());
//        this.addProp(new IdCard());
    }


    /**
     * 功能描述：渲染静止状态方法
     * AI 改成可以支持逐帧行走
     * @author cyt & Claude
     * @date 2026/6/1 21:00
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
     * 功能描述：道具的使用逻辑，AI 玩家跳过确认对话框
     * @param propName 道具名称
     * @param target 目标玩家
     * @author cyt & Claude
     * @date 2026/6/1 21:00
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

        // AI / 联机重放：跳过确认对话框
        if (!isAI && !isOnline) {
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
        }

        if (!lookup.isUsed(target)) return;

        // AI: 路障和地雷在 onPlace 回调中自行扣除库存，此处跳过
        if (lookup instanceof Barrier || lookup instanceof Mine) return;

        if (count == 1) {
            propsCount.remove(propName);
        } else {
            propsCount.put(propName, count - 1);
        }
    }

    /**
     * 功能描述：创建路障道具实例，注入放置回调与 AI 预设索引
     * @return 配置好的路障实例
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    private Barrier getBarrier()
    {
        Barrier b = new Barrier();
        // AI: 预设了路障索引则直接注入 Barrier，跳过弹窗
        if (barrierAiTargetIndex != null)
        {
            b.setAiTargetIndex(barrierAiTargetIndex);
            barrierAiTargetIndex = null; // 用完清掉
        }
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

    /**
     * 功能描述：创建地雷道具实例，注入放置回调
     * @return 配置好的地雷实例
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
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
     * 功能描述：AI 提供的走一格方法，逐格推进并处理方向切换
     * @return true 表示行走完成
     * @author cyt & Claude
     * @date 2026/6/1 21:00
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
     * 功能描述：AI 开始走时候赋值，设置剩余步数并标记行走状态
     * @param steps 行走步数
     * @author cyt & Claude
     * @date 2026/6/1 21:00
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

    /**
     * 功能描述：获取对手玩家的名称
     * @return 对手玩家名称
     * @author cyt
     * @date 2026/6/1 21:00
     */
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

    /**
     * 功能描述：返回玩家信息的字符串表示
     * @return 玩家名称字符串
     * @author cyt
     * @date 2026/6/1 21:00
     */
    @Override
    public String toString()
    {
        return "Player{" +
                "name='" + name + '\'' +
                '}';
    }

    //<editor-fold desc="一些getter与getter方法">


    /**
     * 功能描述：设置地图坐标点数组
     * @param mapPoints 坐标点数组
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setMapPoints(Point[] mapPoints)
    {
        this.mapPoints = mapPoints;
    }

    /**
     * 功能描述：添加地产到持有列表
     * @param land 要添加的地产
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void addTilesOwned(Land land)
    {
        this.landOwned.add(land);
    }

    /**
     * 功能描述：按索引获取持有的地产
     * @param num 地产索引
     * @return 地产对象
     * @author cyt
     * @date 2026/6/1 21:00
     */
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

    /**
     * 功能描述：获取路障停止回合数
     * @return 剩余停止回合数
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public int getBarrierStopTurns()
    {
        return barrierStopTurns;
    }

    /**
     * 功能描述：设置路障停止回合数
     * @param barrierStopTurns 停止回合数
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setBarrierStopTurns(int barrierStopTurns)
    {
        this.barrierStopTurns = barrierStopTurns;
    }

    /**
     * 功能描述：获取当前生命值
     * @return 生命值
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public int getHp()
    {
        return hp;
    }

    /**
     * 功能描述：设置生命值
     * @param hp 生命值
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setHp(int hp)
    {
        this.hp = hp;
    }

    /**
     * 功能描述：获取当前金钱
     * @return 金钱数量
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public int getMoney()
    {
        return money;
    }

    /**
     * 功能描述：设置金钱数量
     * @param money 金钱数量
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setMoney(int money)
    {
        this.money = money;
    }

    /**
     * 功能描述：获取当前位置索引
     * @return 位置索引
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public int getPositionIndex()
    {
        return positionIndex;
    }

    /**
     * 功能描述：设置位置索引
     * @param positionIndex 位置索引
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setPositionIndex(int positionIndex)
    {
        this.positionIndex = positionIndex;
    }

    /**
     * 功能描述：获取当前坐标
     * @return 坐标点
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public Point getPosition()
    {
        return position;
    }

    /**
     * 功能描述：设置坐标位置
     * @param position 坐标点
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setPosition(Point position)
    {
        this.position = position;
    }

    /**
     * 功能描述：获取玩家名称
     * @return 玩家名称
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public String getName()
    {
        return name;
    }

    /**
     * 功能描述：设置玩家名称
     * @param name 玩家名称
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * 功能描述：获取玩家状态
     * @return 状态字符串
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 功能描述：设置玩家状态
     * @param status 状态字符串
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * 功能描述：获取行走方向
     * @return 方向值（1~4）
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public int getMoveTowards()
    {
        return moveTowards;
    }

    /**
     * 功能描述：设置行走方向
     * @param moveTowards 方向值
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setMoveTowards(int moveTowards)
    {
        this.moveTowards = moveTowards;
    }

    /**
     * 功能描述：获取行走精灵图数组
     * @return 精灵图数组
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public Image[] getMoveSprites()
    {
        return moveSprites;
    }

    /**
     * 功能描述：设置行走精灵图数组
     * @param moveSprites 精灵图数组
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setMoveSprites(Image[] moveSprites)
    {
        this.moveSprites = moveSprites;
    }

    /**
     * 功能描述：获取站立精灵图
     * @return 站立精灵图
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public Image getStaticSprite()
    {
        return staticSprite;
    }

    /**
     * 功能描述：设置站立精灵图
     * @param staticSprite 站立精灵图
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setStaticSprite(Image staticSprite)
    {
        this.staticSprite = staticSprite;
    }

    /**
     * 功能描述：设置中毒状态
     * @param inToxic 是否中毒
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setInToxic(boolean inToxic)
    {
        isInToxic = inToxic;
    }

    /**
     * 功能描述：检查是否处于中毒状态
     * @return true 表示中毒
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public boolean isInToxic()
    {
        return isInToxic;
    }

    /**
     * 功能描述：获取持有道具种类数量
     * @return 道具种类数
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public int getPropsNum()
    {
        return propsCount.size();
    }

    /**
     * 功能描述：获取道具库存 Map
     * @return 道具名到数量的映射
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public HashMap<String, Integer> getProps()
    {
        return propsCount;
    }

    /**
     * 功能描述：添加一个道具到库存
     * @param prop 道具实例
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void addProp(Prop prop)
    {
        if (prop == null || prop.getName() == null) return;
        String name = prop.getName();
        propsCount.put(name, propsCount.getOrDefault(name, 0) + 1);
    }

    /**
     * 功能描述：设置路障放置回调
     * @param onBarrierPlace 路障放置回调谓词
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setOnBarrierPlace(IntPredicate onBarrierPlace)
    {
        this.onBarrierPlace = onBarrierPlace;
    }

    /**
     * 功能描述：设置下一次掷骰的面数
     * @param sides 骰子面数
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setNextDiceSides(int sides)
    {
        this.nextDiceSides = sides;
    }

    /**
     * 功能描述：检查是否有下一次骰子面数覆盖
     * @return true 表示有特殊骰子
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public boolean hasNextDiceOverride()
    {
        return nextDiceSides != null;
    }

    /**
     * 功能描述：消耗特殊骰子面数，若没有则返回默认值
     * @param defaultSize 默认面数
     * @return 实际使用的骰子面数
     * @author cyt
     * @date 2026/6/1 21:00
     */
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

    /**
     * 功能描述：检查是否有下一次骰子面数设置
     * @return true 表示有特殊面数
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public boolean hasNextDiceSides()
    {
        return nextDiceSides != null;
    }

    /**
     * 功能描述：检查是否有下一次骰子预设值
     * @return true 表示有预设值
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public boolean hasNextDiceValue()
    {
        return nextDiceValue != null;
    }

    /**
     * 功能描述：设置下一次掷骰的预设值（万能骰子）
     * @param value 预设骰子值
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void setNextDiceValue(int value)
    {
        this.nextDiceValue = value;
    }

    /**
     * 功能描述：设置地雷放置回调
     * @param onMinePlace 地雷放置回调谓词
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public void setOnMinePlace(IntPredicate onMinePlace)
    {
        this.onMinePlace = onMinePlace;
    }

    /**
     * 功能描述：检查是否为 AI 玩家
     * @return true 表示 AI 控制
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public boolean isAI()
    {
        return isAI;
    }

    /**
     * 功能描述：设置是否为 AI 玩家
     * @param isAI true 表示 AI 控制
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public void setAI(boolean isAI)
    {
        this.isAI = isAI;
    }

    /**
     * 功能描述：设置 AI 路障目标瓦片索引
     * @param index 目标瓦片索引
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public void setBarrierAiTargetIndex(int index)
    {
        this.barrierAiTargetIndex = index;
    }

    /**
     * 功能描述：获取联机会话标识
     * @return sessionId
     * @author cyt & Claude
     * @date 2026/6/2
     */
    public String getSessionId() { return sessionId; }

    /**
     * 功能描述：设置联机会话标识
     * @param id 会话ID
     * @author cyt & Claude
     * @date 2026/6/2
     */
    public void setSessionId(String id) { this.sessionId = id; }

    /**
     * 功能描述：检查是否为联机玩家
     * @return true 表示联机模式
     * @author cyt & Claude
     * @date 2026/6/2
     */
    public boolean isOnline() { return isOnline; }

    /**
     * 功能描述：设置联机玩家标志
     * @param online 是否联机
     * @author cyt & Claude
     * @date 2026/6/2
     */
    public void setOnline(boolean online) { isOnline = online; }

    //</editor-fold>

}
