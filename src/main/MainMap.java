package main;

import architecture.*;
import debug.DebugTools;
import debug.Log;
import props.Barrier;
import props.Mine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntPredicate;

public class MainMap extends JFrame
{
    // 布局相关
    JLayeredPane layered;
    JLabel bgLayer;
    JPanel uiLayer,tileLayer,actorLayer;
    DiceController diceController;
    Timer walkTimer;
    JLabel[] propJLabels;

    // 游戏相关
    private int currentPlayerIndex = 0;
    Player[] players;
    static int round = 0;

    // 瓦片与背景导入
    BoardConfig boardConfig = new BoardConfig();

    // 根据使用对象分类
    private enum PropCategory { SELF, SELECTABLE, OTHER }

    // 快速建造模板类
    private record PropDef(String name, String iconPath, int boundsX, int boundsY, int boundsW, int boundsH, int countX/*数量的位置 */, int countY, PropCategory category) {}

    private static final PropDef[] PROP_DEFS =
    //<editor-fold desc="道具卡片">
    {
        new PropDef("包子",   "props/baozi.png",     907, 950, 59, 75,  930, 1032, PropCategory.SELF),
        new PropDef("考试周", "props/exam_week.png",  967, 950, 59, 75,  995, 1032, PropCategory.SELECTABLE),
        new PropDef("地雷",   "props/shoulei.png",   1033, 950, 59, 75, 1060, 1032, PropCategory.SELF),
        new PropDef("路障",   "props/gaijian.png",   1093, 950, 59, 75, 1125, 1032, PropCategory.OTHER),
        new PropDef("偷取",   "props/fail_exam.png", 1153, 950, 58, 75, 1190, 1032, PropCategory.OTHER),
        new PropDef("万能骰子","props/copy.png",     1218, 950, 59, 75, 1255, 1032, PropCategory.SELECTABLE),
        new PropDef("升级卡", "props/gongren.png",   1277, 950, 59, 75, 1310, 1032, PropCategory.SELF),
        new PropDef("身份证", "props/pass.png",      1335, 950, 59, 75, 1375, 1032, PropCategory.SELF),
    };
    //</editor-fold>

    /**
     * 功能描述：主地图的构造方法
     * @author cyt
     * @date 2026/5/14 14:29
     */
    public MainMap()
    {
        // 导入调试包
        DebugTools.install(this);

        // 导入与创造道具图片
        loadPropsAndImg();

        // 导入玩家与其图片
        loadPlayerAndImg();

        // 给特殊地块注入回调
        setupSpecialTiles();

        // 渲染四个层级----背景层，瓦片层，玩家层，UI层
        renderFourLayers();

        // 设置当前的窗口的各类设置
        this.setResizable(false);
        this.setSize(ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);
        this.setLayout(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    /**
     * 功能描：导入道具
     * @author cyt
     * @date 2026/5/27 14:12
     */
    private void loadPropsAndImg()
    {
        propJLabels = new JLabel[PROP_DEFS.length];
        for (int i = 0; i < PROP_DEFS.length; i++)
        {
            PropDef def = PROP_DEFS[i];
            propJLabels[i] = new JLabel();
            propJLabels[i].setIcon(new ImageIcon("src/img/" + def.iconPath));
            // record 自动简化了方法
            propJLabels[i].setName(def.name);
        }
    }

    /**
     * 功能描述：导入玩家
     * @author cyt
     * @date 2026/5/15 18:34
     */
    private void loadPlayerAndImg()
    {
        players = new Player[2];
        players[0] = new Player(0,"naiLong",boardConfig.getPoints()[0]);
        players[1] = new Player(0,"xiaoMei",boardConfig.getPoints()[0]);
        players[0].setOtherPlayer(players[1]);
        players[1].setOtherPlayer(players[0]);

        players[0].setOnBarrierPlace(createBarrierCallback());
        players[1].setOnBarrierPlace(createBarrierCallback());

        // AI: 地雷放置回调，地雷只能放在当前玩家所在格子
        players[0].setOnMinePlace(createMineCallback());
        players[1].setOnMinePlace(createMineCallback());

        players[0].setMapPoints(boardConfig.getPoints());
        players[1].setMapPoints(boardConfig.getPoints());

        players[0].setMoveSprites(new String[]{
                "src/img/player/男1.png",
                "src/img/player/男2.png",
                "src/img/player/男3.png",
                "src/img/player/男4.png"
        });

        players[1].setMoveSprites(new String[]{
                "src/img/player/女1.png",
                "src/img/player/女2.png",
                "src/img/player/女3.png",
                "src/img/player/女4.png",
        });
    }


    /**
     * 功能描述：渲染三个主要层级
     * @author cyt
     * @date 2026/5/20 14:07
     */
    private void renderFourLayers()
    {
        // 使用JLayeredPane实现分层
        layered = new JLayeredPane();
        layered.setBounds(0, 0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);
        this.setContentPane(layered);

        // bgLayer层
        bgLayer = new JLabel(new ImageIcon("src/img/map/MainMap.png"));
        bgLayer.setBounds(0,0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);

        // uiLayer层 ---- 显示玩家信息
        uiLayer = new UILayerPanel(this);
        addPropsListener();

        diceController = new DiceController(this, () -> {
            getCurrentPlayer().startWalk(diceController.getDiceValue());
            startWalkAnimation(this::afterMoveResolveTile);
        });
        diceController.attachTo(uiLayer);
        updatePropLabelsForCurrentPlayer();

        // tileLayer层
        tileLayer = new TileLayerPanel(this);

        // actorLayer层 --- 负责绘画玩家与骰子
        actorLayer = new ActorLayerPanel(this);

        // 设置渲染层级 --- ui最上面，
        layered.setLayer(bgLayer, 0);
        layered.setLayer(tileLayer, 50);
        layered.setLayer(actorLayer, 80);
        layered.setLayer(uiLayer, 100);

        // 添加顺序随意
        layered.add(bgLayer);
        layered.add(tileLayer);
        layered.add(actorLayer);
        layered.add(uiLayer);

        // 必备的检验方法：当你的TileLayer组件的大小、位置、内部瓦片的排列方式发生变化时必须调用
        tileLayer.revalidate();
        tileLayer.repaint();
    }

    /**
     * 功能描述：获取当前的玩家
     * @author cyt
     * @date 2026/5/29 17:35
     */
    public Player getCurrentPlayer() {
        return players[currentPlayerIndex];
    }

    /**
     * 功能描述：渲染道具类 UI
     * @author cyt
     * @date 2026/5/27 14:51
     */
    void renderPropUI(Graphics g)
    {
        for (PropDef def : PROP_DEFS)
        {
            Integer count = getCurrentPlayer().getProps().get(def.name);
            if (count != null && count > 0)
            {
                g.drawString(String.valueOf(count), def.countX, def.countY);
            }
        }
    }

    /**
     * 功能描述：给各个道具加上监听
     * @author cyt
     * @date 2026/5/27 14:39
     */
    private void addPropsListener()
    {
        for (int i = 0; i < propJLabels.length; i++)
        {
            // 循环里的匿名类，不能用变化的 i，必须用一个固定不变的 finalI！
            int finalI = i;
            PropDef def = PROP_DEFS[i];
            propJLabels[i].addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    switch (def.category) {
                        case SELF -> getCurrentPlayer().use(def.name, getCurrentPlayer());
                        case SELECTABLE -> {
                            int result = JOptionPane.showOptionDialog(
                                    null,
                                    "选择玩家，对其使用" + def.name,
                                    "选择玩家",
                                    JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    new String[]{"自己", "对方", "取消"},
                                    "确定"
                            );
                            if (result == 1) {
                                getCurrentPlayer().use(def.name, players[(currentPlayerIndex + 1) % players.length]);
                            } else if (result == 0) {
                                getCurrentPlayer().use(def.name, getCurrentPlayer());
                            }
                        }
                        case OTHER -> getCurrentPlayer().use(def.name, players[(currentPlayerIndex + 1) % players.length]);
                    }
                    refreshLayers();
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    super.mouseEntered(e);
                    propJLabels[finalI].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    super.mouseExited(e);
                    propJLabels[finalI].setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            });
        }

        for (JLabel jLabel : propJLabels)
        {
            uiLayer.add(jLabel);
        }
    }

    /**
     * 功能描述：渲染玩家 UI
     * @author cyt
     * @date 2026/5/27 14:19
     */
    void renderPlayerUI(Graphics g)
    {
        var p1 = BoardConfig.P1_LAYOUT;
        var p2 = BoardConfig.P2_LAYOUT;
        var card = BoardConfig.PROP_CARD_BG;

        g.drawImage(new ImageIcon("src/img/player/naiLong4.png").getImage(),
            p1.imgX(), p1.imgY(), p1.imgW(), p1.imgH(), null);
        g.drawImage(new ImageIcon("src/img/player/xiaoMei4.png").getImage(),
            p2.imgX(), p2.imgY(), p2.imgW(), p2.imgH(), null);
        g.drawImage(new ImageIcon("src/img/props/kapianlan.png").getImage(),
            card.x, card.y, card.width, card.height, null);
        if (currentPlayerIndex == 0)
        {
            g.drawImage(new ImageIcon("src/img/player/red.png").getImage(),
            p1.imgX() + 270,p1.imgY() + 40,50,37,null);
        }else if (currentPlayerIndex == 1)
        {
            g.drawImage(new ImageIcon("src/img/player/blue.png").getImage(),
            p2.imgX() + 270,p2.imgY() + 40,50,37,null);
        }

        Font font = new Font("微软雅黑",Font.BOLD,14);
        g.setFont(font);

        g.drawString(Integer.toString(players[0].getMoney()), p1.moneyX(), p1.moneyY());
        g.drawString(Integer.toString(players[1].getMoney()), p2.moneyX(), p2.moneyY());
        g.drawString(Integer.toString(players[0].getProperty()), p1.propertyX(), p1.propertyY());
        g.drawString(Integer.toString(players[1].getProperty()), p2.propertyX(), p2.propertyY());
        g.drawString(Integer.toString(players[0].getHp()), p1.hpX(), p1.hpY());
        g.drawString(Integer.toString(players[1].getHp()), p2.hpX(), p2.hpY());
    }


    /**
     * 功能描述：AI 启动当前玩家的走路动画（Timer 驱动 advanceOneStep）
     * 注意：不要在 Tile/Event 里自己创建 walkTimer；统一由 MainMap 控制。
     * @author cyt
     * @date 2026/5/20
     */
    private void startWalkAnimation(Runnable onFinished)
    {
        if (walkTimer != null && walkTimer.isRunning())
        {
            walkTimer.stop();
        }

        walkTimer = new Timer(200, evt -> {
            // 一步一步走
            boolean done = getCurrentPlayer().advanceOneStep();
            refreshLayers();

            // AI: 每步检测地雷，爆炸不阻止行走
            checkMine();

            // 触碰到路障立刻停止行走
            if (!done && hitBarrier())
            {
                done = true;
            }

            if (done)
            {
                ((Timer) evt.getSource()).stop();
                getCurrentPlayer().resetWalkFrame();
                refreshLayers();

                if (onFinished != null)
                {
                    onFinished.run();
                }
            }
        });

        walkTimer.start();
    }

    /**
     * 功能描述：触碰到路障后的逻辑
     * @author cyt
     * @date 2026/5/28 14:34
     */
    private boolean hitBarrier()
    {
        Tile tile = boardConfig.getTiles()[getCurrentPlayer().getPositionIndex()];
        if (tile != null && tile.hasBarrier())
        {
            tile.removeBarrier();
            JOptionPane.showMessageDialog(null, getCurrentPlayer().getName() + "碰到了路障，停止前进！");
            getCurrentPlayer().cancelWalk();
            return true;
        }
        return false;
    }

    /**
     * 功能描述：AI: 检查当前玩家所在格子是否有地雷，有则引爆（扣血但不停止行走）
     * @author cyt
     * @date 2026/5/28 13:37
     */
    private void checkMine()
    {
        Tile tile = boardConfig.getTiles()[getCurrentPlayer().getPositionIndex()];
        if (tile != null && tile.hasMine())
        {
            tile.removeMine();
            getCurrentPlayer().hpDecrease(40);
            JOptionPane.showMessageDialog(null, getCurrentPlayer().getName() + "踩到了地雷！失去40点生命！");
        }
    }

    /**
     * 功能描述：一次移动结束后，触发当前格子的到达效果。
     * 如果格子效果里调用了 player.startWalk(n)，这里会继续走路动画；否则结束回合。
     * @author cyt
     * @date 2026/5/20
     */
    private void afterMoveResolveTile()
    {
        onArrive();

        // Event 等格子可能会调用 startWalk 再走几步（比如顺风车事件）
        if (getCurrentPlayer().isWalking())
        {
            // 牛逼，这我学个毛
            startWalkAnimation(this::afterMoveResolveTile);
            return;
        }

        // 没有额外移动了，回合结束
        nextPlayer();
    }

    /**
     * 功能描述：一次性更新三个层级
     * @author cyt
     * @date 2026/5/19 12:26
     */
    public void refreshLayers()
    {
        updatePropLabelsForCurrentPlayer();
        tileLayer.repaint();
        actorLayer.repaint();
        uiLayer.repaint();
    }

    /**
     * 功能描述：玩家买地逻辑
     * @author cyt
     * @date 2026/5/19 12:26
     */
    private void onArrive()
    {
        Tile tile = boardConfig.getTiles()[getCurrentPlayer().getPositionIndex()];
        Log.info(getCurrentPlayer().getName() + " 进入 [" + tile.getName() + "] 格子（索引 " + getCurrentPlayer().getPositionIndex() + "）");

        tile.onPlayerArrive(getCurrentPlayer());
        refreshLayers();
    }

    /**
     * 功能描述：下一个玩家
     * @author cyt
     * @date 2026/5/16 19:42
     */
    public void nextPlayer()
    {
        // 切换逻辑真是精妙
        for (int i = 0; i < players.length; i++)
        {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
            Player p = getCurrentPlayer();

            int skip = p.getBarrierStopTurns();
            if (skip > 0)
            {
                p.setBarrierStopTurns(skip - 1);
                if ("isPrisoned".equals(p.getStatus()))
                {
                    if (skip == 1)
                    {
                        p.setStatus(null);
                        JOptionPane.showMessageDialog(null, p.getName() + " 刑满释放！");
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null,
                                p.getName() + " 还在坐牢，剩余 " + (skip - 1) + " 回合");
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(null, p.getName() + " 硬控了一回合！");
                }
                continue;
            }
            break;
        }
        refreshLayers();
    }

    /**
     * 功能描述：增加游戏回合
     * @author cyt
     * @date 2026/5/16 19:44
     */
    public void roundIncrease()
    {
        round++;
        Log.info("====== 第 " + round + " 回合 ======");
        decreaseAllBarrierRounds();
    }

    /**
     * 功能描述：所有的路障减少回合数
     * @author cyt
     * @date 2026/5/27 13:49
     */
    private void decreaseAllBarrierRounds()
    {
        for (Tile tile : boardConfig.getTiles())
        {
            if (tile != null && tile.hasBarrier())
            {
                tile.decreaseBarrierRound();
            }
        }
    }

    /**
     * 功能描述：渲染所有房屋建筑
     * @author cyt
     * @date 2026/5/17
     */
    void renderBuildings(Graphics g) {
        for (int i = boardConfig.getTiles().length - 1; i >= 0; i--) {
            Tile tile = boardConfig.getTiles()[i];
            if (tile instanceof Land land) {
                int index = land.getPositionIndex();
                if ((index >= 0 && index <= 9) || (index >= 22 && index <= 29)) {
                    renderLand(g, land);
                }
            }
        }

        // 第二步：再渲染正向区域（右+左，后画，盖在前面）
        for (int i = 0; i < boardConfig.getTiles().length; i++) {
            Tile tile = boardConfig.getTiles()[i];
            if (tile instanceof Land land) {
                int index = land.getPositionIndex();
                // 右半段 10~15 + 下段 16~21，正向渲染
                if ((index >= 10 && index <= 15) || (index >= 16 && index <= 21)) {
                    renderLand(g, land);
                }
            }
        }
    }

    /**
     * 功能描述：渲染地产
     * @author cyt
     * @date 2026/5/28 10:31
     */
    private void renderLand(Graphics g, Land land) {
        if (land.getOwner() != null) {
            land.renderBuilding(g);
        } else {
            land.renderUnsold(g);
        }
    }

    /**
     * 功能描述：AI给我的渲染图片
     * @author cyt
     * @date 2026/5/26 17:37
     */
    private void updatePropLabelsForCurrentPlayer() {
        for (JLabel label : propJLabels) {
            label.setVisible(false);
            label.setBounds(0, 0, 0, 0);
        }

        for (int i = 0; i < PROP_DEFS.length; i++) {
            PropDef def = PROP_DEFS[i];
            Integer count = getCurrentPlayer().getProps().get(def.name);
            if (count != null && count > 0) {
                propJLabels[i].setBounds(def.boundsX, def.boundsY, def.boundsW, def.boundsH);
                propJLabels[i].setVisible(true);
            }
        }
    }


    /**
     * 功能描述：给公园等特殊地块注入回调（传送等）
     * @author cyt
     * @date 2026/5/29
     */
    private void setupSpecialTiles()
    {
        for (Tile tile : boardConfig.getTiles())
        {
            if (tile instanceof ParkLand park)
            {
                park.setTeleportHandler(this::teleportTo);
            }
        }
    }

    /**
     * 功能描述：AI 传送按钮
     * @author cyt
     * @date 2026/5/27 15:08
     */
    private IntPredicate createBarrierCallback() {
        return index -> {
            if (boardConfig.getTiles()[index].hasBarrier()) {
                JOptionPane.showMessageDialog(null, "该位置已有路障，无法重复放置！");
                return false;
            }
            boardConfig.getTiles()[index].plantBarrier(Barrier.ROUND_LIFE);
            refreshLayers();
            return true;
        };
    }

    /**
     * 功能描述：AI: 地雷放置回调 —— 只能放在当前玩家脚下，不能重复放置，无生命周期
     * @author cyt
     * @date 2026/5/28 13:38
     */
    private IntPredicate createMineCallback() {
        return index -> {
            if (boardConfig.getTiles()[index].hasMine()) {
                JOptionPane.showMessageDialog(null, "该位置已有地雷，无法重复放置！");
                return false;
            }
            boardConfig.getTiles()[index].plantMine(new Mine());
            refreshLayers();
            return true;
        };
    }

    /**
     * 功能描述：传送功能
     * @author cyt
     * @date 2026/5/28 10:30
     */
    public void teleportTo(int targetIndex)
    {
        if (getCurrentPlayer() == null || getCurrentPlayer().isWalking() || targetIndex < 0 || targetIndex >=
                boardConfig.getTiles().length) return;
        getCurrentPlayer().setPositionIndex(targetIndex);
        getCurrentPlayer().setPosition(new Point(boardConfig.getPoints()[targetIndex]));
        refreshLayers();

        Tile t = boardConfig.getTiles()[targetIndex];
        if (t != null)
            t.onPlayerArrive(getCurrentPlayer());
        refreshLayers();
    }

    /**
     * 功能描述：渲染格子索引号
     * @author cyt
     * @date 2026/5/31
     */
    void renderTileIndexes(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.setFont(new Font("微软雅黑", Font.BOLD, 12));
        for (int i = 0; i < boardConfig.getPoints().length; i++)
        {
            Point p = boardConfig.getPoints()[i];
            // AI 索引文字画在格子左上角偏右下的位置，你自己调
            g.drawString(String.valueOf(i), p.x + 15, p.y + 30);
        }
    }

    /**
     * 功能描述：渲染路障
     * @author cyt
     * @date 2026/5/26 20:59
     */
    void renderBarriers(Graphics g)
    {
        Image barrierImg = new ImageIcon("src/img/props/拦路牌.png").getImage(); // 你资源里有这个

        for (Tile tile : boardConfig.getTiles()) {
            if (tile == null) continue;
            if (!tile.hasBarrier()) continue;
            Point point = boardConfig.getPoints()[tile.getPositionIndex()];
            g.drawImage(barrierImg,(int) point.getX(),(int) point.getY(),88,94,null);
        }
    }

    /**
     * 功能描述：AI: 渲染地雷图标到有地雷的格子上
     * @author cyt
     * @date 2026/5/28 13:39
     */
    void renderMines(Graphics g)
    {
        Image mineImg = new ImageIcon("src/img/props/mine.png").getImage();

        for (Tile tile : boardConfig.getTiles()) {
            if (tile == null) continue;
            if (!tile.hasMine()) continue;
            Point point = boardConfig.getPoints()[tile.getPositionIndex()];
            g.drawImage(mineImg, (int) point.getX() - 10, (int) point.getY(),75,100, null);
        }
    }

    /**
     * 功能描述：测试启动
     * @author cyt
     * @date 2026/5/19 12:25
     */
    static void main()
    {
        new MainMap();
    }
}
