package main;

import architecture.*;
import architecture.Event;
import debug.DebugTools;
import props.Barrier;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class MainMap extends JFrame
{
    JLayeredPane layered;
    JLabel bgLayer;
    JPanel uiLayer,tileLayer,actorLayer;
    Player naiLong;
    Player xiaoMei;
    Player currentPlayer;
    ImageIcon[] diceImg;
    Timer diceTimer;
    Timer walkTimer;
    ImageIcon[] props;
    JLabel[] propJLabels;
    boolean isDiceRolling = false;
    int currentDiceFrame = 0;
    int diceValue;

    private int currentPlayerIndex = 0;
    Player[] players;
    static int round = 0;

    public Point[] points
    //<editor-fold desc="直接copy过来">
            = {
            new Point(235,515), new Point(305,465), new Point(375,415) ,new Point(445,360), new Point(510,310),
            new Point(580,265), new Point(650,215), new Point(720,165) ,new Point(790,135), new Point(860,185),
            new Point(925,230), new Point(990,275), new Point(1050,320),new Point(1120,370),new Point(1190,415),
            new Point(1260,465),new Point(1190,515),new Point(1120,565),new Point(1050,615),new Point(990,665),
            new Point(925,715), new Point(860,765), new Point(790,815), new Point(720,865), new Point(650,815),
            new Point(580,765), new Point(510,715), new Point(445,665), new Point(375,615), new Point(305,565)
    };
    //</editor-fold>

    Tile[] tiles = {
    //<editor-fold desc="初始化">
            new Start(new Point()),
            new Event(1, new Point(), "小吃街"),
            new ResidentLand(2, new Point(280, 375), "居民楼1"),
            new ResidentLand(3, new Point(350, 325), "居民楼2"),
            new GymLand(4, new Point(420, 275), "体育馆1"),
            new HotelLand(5, new Point(415, 165), "旅馆"),
            new Empty(6, new Point(), "空地"),
            new Chance(7,  new Point(), "抽卡点"),
            new Shop(8, new Point(), "商店"),

            new Hospital(9, new Point(), "医院"),
            new ResidentLand(10,  new Point(960, 185), "居民楼3"),
            new ParkLand(11,  new Point(1026, 210), "公园"),
            new Empty(12, new Point(), "空地"),
            new ResidentLand(13, new Point(1160, 330), "居民楼4"),
            new Event(14, new Point(), "市民公园"),
            new Prison(15, new Point(), "牢中"),

            new Chance(16, new Point(), "抽卡点2"),
            new GymLand(17, new Point(1028, 518), "体育馆2"),
            new ResidentLand(18, new Point(958, 568), "居民楼5"),
            new ResidentLand(19, new Point(958 - 70, 618), "居民楼6"),
            new ResidentLand(20, new Point(958 - 140, 668), "居民楼7"),
            new Hospital(21, new Point(), "医院2"),
            new Chance(22,  new Point(), "抽卡点3"),
            new Event(23,  new Point(), "上饶中学"),
            new Empty(24, new Point(), "空地"),

            new ResidentLand(25,  new Point(620, 715), "居民楼8"),
            new ShopLand(26,  new Point(483, 600), "超市"),
            new Empty(27, new Point(), "空地"),
            new ResidentLand(28,  new Point(419, 570), "居民楼9"),
            new Empty(29, new Point(), "空地")
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
        loadPlayerImg();

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
        props = new ImageIcon[8];
        props[0] = new ImageIcon("src/img/props/baozi.png");
        props[1] = new ImageIcon("src/img/props/exam_week.png");
        props[2] = new ImageIcon("src/img/props/shoulei.png");
        props[3] = new ImageIcon("src/img/props/gaijian.png");
        props[4] = new ImageIcon("src/img/props/fail_exam.png");
        props[5] = new ImageIcon("src/img/props/copy.png");
        props[6] = new ImageIcon("src/img/props/gongren.png");
        props[7] = new ImageIcon("src/img/props/pass.png");

        propJLabels = new JLabel[8];

        for (int i = 0; i < propJLabels.length; i++)
        {
            propJLabels[i] = new JLabel();
            propJLabels[i].setIcon(props[i]);
        }

        propJLabels[0].setName("包子");
        propJLabels[1].setName("考试周");
        propJLabels[2].setName("地雷");
        propJLabels[3].setName("路障");
        propJLabels[4].setName("偷取");
        propJLabels[5].setName("万能骰子");
        propJLabels[6].setName("升级卡");
        propJLabels[7].setName("身份证");
    }

    /**
     * 功能描述：导入玩家
     * @author cyt
     * @date 2026/5/15 18:34
     */
    private void loadPlayerImg()
    {
        // 导入两个玩家
        players = new Player[2];
        naiLong = new Player(0,"naiLong",points[0]);
        xiaoMei = new Player(0,"xiaoMei",points[0]);
        naiLong.setOtherPlayer(xiaoMei);
        xiaoMei.setOtherPlayer(naiLong);

        // 回调注册
        naiLong.setOnBarrierPlace(index -> {
            if (tiles[index].hasBarrier()) {
                JOptionPane.showMessageDialog(null, "该位置已有路障，无法重复放置！");
                return false;
            }
            tiles[index].plantBarrier(Barrier.ROUND_LIFE);
            refreshLayers();
            return true;
        });
        xiaoMei.setOnBarrierPlace(index -> {
            if (tiles[index].hasBarrier()) {
                JOptionPane.showMessageDialog(null, "该位置已有路障，无法重复放置！");
                return false;
            }
            tiles[index].plantBarrier(Barrier.ROUND_LIFE);
            refreshLayers();
            return true;
        });

        //TODO 玩家为传入位置，感觉这里可以回调解耦
        naiLong.setMapPoints(points);
        xiaoMei.setMapPoints(points);

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

        players[0] = naiLong;
        players[1] = xiaoMei;

        // 初始玩家是naiLong
        currentPlayer = players[currentPlayerIndex];

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
        uiLayer = new JPanel(null){
            @Override
            public void paintComponent(Graphics g)
            {
                // 清空了上一次绘画东西
                super.paintComponent(g);

                // 渲染玩家的 UI
                renderPlayerUI(g);

                // 渲染道具类 UI
                renderPropUI(g);

            }
        };
        uiLayer.setOpaque(false);
        uiLayer.setBounds(0,0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);

        // 给道具加上监听
        addPropsListener();

        // 给骰子加上监听
        addDiceListener();

        // 初始渲染一下道具栏
        updatePropLabelsForCurrentPlayer();

        // tileLayer层
        tileLayer = new JPanel(null){
            public void paintComponent(Graphics g)
            {
                // 清空了上一次绘画东西
                super.paintComponent(g);
                renderBuildings(g);
                renderBarriers(g);
            }
        };
        tileLayer.setOpaque(false);
        tileLayer.setBounds(0,0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);

        // actorLayer层 --- 负责绘画玩家与骰子
        actorLayer = new JPanel(null) {
            // 这里在创建刷新都会自动调用
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (naiLong != null) {
                    naiLong.renderStaticSprite(g, naiLong.getPosition());
                }
                if (xiaoMei != null) {
                    xiaoMei.renderStaticSprite(g, xiaoMei.getPosition());
                }
                if (diceImg != null && diceImg[0] != null && !isDiceRolling) {
                    if( round == 0 )
                    {
                        g.drawImage(diceImg[0].getImage(),
                                1100, 650,
                                1200, 850,
                                900, 0,
                                1000, 200,
                                null);
                    }else {
                        g.drawImage(diceImg[diceValue - 1].getImage(),
                                1100, 650,
                                1200, 850,
                                900, 0,
                                1000, 200,
                                null);
                    }
                }
                // AI 教我做投掷骰子动画
                if (isDiceRolling && diceImg != null)
                {
                    int frameWidth = 100;
                    int srcX = currentDiceFrame * frameWidth;
                    int srcY = 0;

                    g.drawImage(
                            diceImg[diceValue -1].getImage(),
                            1100,650,1100+100,650 + 200,
                            srcX,srcY,srcX + frameWidth,200,null
                    );
                }

            }
        };
        actorLayer.setOpaque(false);
        actorLayer.setBounds(0,0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);

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
     * 功能描述：渲染道具类 UI
     * @author cyt
     * @date 2026/5/27 14:51
     */
    private void renderPropUI(Graphics g)
    {
        for (Map.Entry<String, Integer> entry : currentPlayer.getProps().entrySet()) {

            String name = entry.getKey();
            int count = entry.getValue();
            if (count <= 0) continue;

            switch (name){
                case "包子"   -> g.drawString(String.valueOf(count),930,1032);
                case "考试周"  -> g.drawString(String.valueOf(count),995,1032);
                case "地雷"   -> g.drawString(String.valueOf(count),1060,1032);
                case "路障"   -> g.drawString(String.valueOf(count),1125,1032);
                case "偷取"   -> g.drawString(String.valueOf(count),1190,1032);
                case "万能骰子"-> g.drawString(String.valueOf(count),1255,1032);
                case "升级卡" -> g.drawString(String.valueOf(count),1310,1032);
                case "身份证" -> g.drawString(String.valueOf(count),1375,1032);
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
            int finalI = i;
            propJLabels[i].addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    super.mouseClicked(e);
                    JLabel jLabel =  (JLabel)e.getSource();
                    if (jLabel.getName().equals("包子") || jLabel.getName().equals("身份证") || jLabel.getName().equals("升级卡"))
                    {
                        currentPlayer.use(jLabel.getName(),currentPlayer);
                    }else if (jLabel.getName().equals("万能骰子") || jLabel.getName().equals("考试周") )
                    {
                        int result = JOptionPane.showOptionDialog(
                                null,
                                "选择玩家，对其使用考试周或万能骰子",
                                "选择玩家",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new String[]{"自己", "对方","取消"}, // 自定义按钮
                                "确定"
                        );
                        if ( result == 1 ){
                            currentPlayer.use(jLabel.getName(),(players[(currentPlayerIndex + 1)%players.length]));
                        }else if ( result == 0)
                        {
                            currentPlayer.use(jLabel.getName(),currentPlayer);
                        }
                    }else
                    {
                        currentPlayer.use(jLabel.getName(),(players[(currentPlayerIndex + 1)%players.length]));
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

        // 添加道具栏到UI
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
    private void renderPlayerUI(Graphics g)
    {
        g.drawImage(new ImageIcon("src/img/player/naiLong4.png").getImage(), 10, 60, 340, 110, null);
        g.drawImage(new ImageIcon("src/img/player/xiaoMei4.png").getImage(), 10, 200, 340, 110, null);
        g.drawImage(new ImageIcon("src/img/props/kapianlan.png").getImage(),900,950,500,87,null);
        String naiLongMoney = Integer.toString(naiLong.getMoney());
        String xiaoMeiMoney = Integer.toString(xiaoMei.getMoney());
        String naiLongProperty = Integer.toString(naiLong.getProperty());
        String xiaoMeiProperty = Integer.toString(xiaoMei.getProperty());
        String naiLongHp = Integer.toString(naiLong.getHp());
        String xiaoMeiHp = Integer.toString(xiaoMei.getHp());

        Font font = new Font("微软雅黑",Font.BOLD,14);
        g.setFont(font);

        g.drawString(naiLongMoney,200,102);
        g.drawString(xiaoMeiMoney,200,242);
        g.drawString(naiLongProperty,240,145);
        g.drawString(xiaoMeiProperty,240,288);
        g.drawString(naiLongHp,210,123);
        g.drawString(xiaoMeiHp,210,263);
    }


    /**
     * 功能描述：添加ui界面的骰子并启动动画 --- 主逻辑开始
     * @author cyt
     * @date 2026/5/16 19:41
     */
    private void addDiceListener()
    {
        // 导入骰子图片
        diceImg = new ImageIcon[6];
        for (int i = 0; i < diceImg.length; i++)
        {
            diceImg[i] = new ImageIcon("src/img/dice/0" + (i+1) + ".png");
        }
        
        // 点击骰子的图片
        ImageIcon icon1 = new ImageIcon("src/img/dice/vv.png");
        ImageIcon icon2 = new ImageIcon("src/img/dice/cc.png");

        // 触发逻辑的图片按钮
        JLabel jLabel1 = new JLabel(icon1);

        jLabel1.setBounds(ConstantNum.DICE_POSITION_X,ConstantNum.DICE_POSITION_Y, 150,129);

        jLabel1.addMouseListener(
                new MouseAdapter()
                {
                    /**
                     * 功能描述：AI 帮我完善了游戏循环逻辑，做了两个动画渲染
                     * @author cyt
                     * @date 2026/5/16 19:24
                     */
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {

                        // 已经点过就退出
                        if (isDiceRolling || currentPlayer.isWalking())
                            return;

                        // 被路障挡住，跳过本回合
                        if (currentPlayer.getBarrierStopTurns() > 0)
                        {
                            JOptionPane.showMessageDialog(null, currentPlayer.getName() + "被路障挡住了，停止一回合！");
                            currentPlayer.setBarrierStopTurns(0);
                            nextPlayer();
                            return;
                        }

                        // 正在点
                        isDiceRolling = true;

                        // 点完变图标
                        jLabel1.setIcon(icon2);

                        // 得出结果
                        diceValue = currentPlayer.rollDice();

                        // 现在的骰子帧数
                        currentDiceFrame = 0;

                        System.out.println(currentPlayer.getName() + "投掷出了" + diceValue
                        + "点数");


                        renderDiceAnim(jLabel1, icon1);
                        diceTimer.start();

                        if (currentPlayer.isInToxic())
                            currentPlayer.hpDecrease(3);
                        roundIncrease();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e)
                    {
                        super.mouseEntered(e);

                    }

                    @Override
                    public void mouseExited(MouseEvent e)
                    {
                        super.mouseExited(e);
                    }

                }
        );

        uiLayer.add(jLabel1);

    }


    /**
     * 功能描述：开始骰子动画
     * @author cyt
     * @date 2026/5/18 13:53
     */
    private void renderDiceAnim(JLabel jLabel1, ImageIcon icon1)
    {
        diceTimer = new Timer(80,evt ->
        {
            currentDiceFrame++;
            refreshLayers();

            if(currentDiceFrame >= 8)
            {
                diceTimer.stop();
                isDiceRolling = false;

                jLabel1.setIcon(icon1);

                // 骰子动画结束，启动行走动画
                currentPlayer.startWalk(diceValue);

                // 开始本次“走路动画”，结束后处理格子效果（可能会继续触发走路）
                startWalkAnimation(this::afterMoveResolveTile);

            }
        });
    }


    /**
     * 功能描述：AI 启动当前玩家的走路动画（Timer 驱动 advanceOneStep）
     * 注意：不要在 Tile/Event 里自己创建 walkTimer；统一由 MainMap 控制。
     * @author cyt
     * @date 2026/5/20
     */
    private void startWalkAnimation(Runnable onFinished)
    {
        // 防止重复开多个 walkTimer
        if (walkTimer != null && walkTimer.isRunning())
        {
            walkTimer.stop();
        }

        walkTimer = new Timer(200, evt -> {
            boolean done = currentPlayer.advanceOneStep();
            refreshLayers();

            if (done)
            {
                ((Timer) evt.getSource()).stop();
                // 重置走路帧
                currentPlayer.resetWalkFrame();
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
     * 功能描述：一次移动结束后，触发当前格子的到达效果。
     * 如果格子效果里调用了 player.startWalk(n)，这里会继续走路动画；否则结束回合。
     * @author cyt
     * @date 2026/5/20
     */
    private void afterMoveResolveTile()
    {
        onArrive();

        // Event 等格子可能会调用 startWalk 再走几步（比如顺风车事件）
        if (currentPlayer.isWalking())
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
    private void refreshLayers()
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
        Tile tile = tiles[currentPlayer.getPositionIndex()];
        System.out.println(currentPlayer.getName() + "进入" + tiles[currentPlayer.getPositionIndex()].getName() + "格子");

        if (tile.hasBarrier())
        {
            JOptionPane.showMessageDialog(null, currentPlayer.getName() + "碰到了路障，停止一回合！");
            currentPlayer.setBarrierStopTurns(1);
            tile.removeBarrier();
        }

        tile.onPlayerArrive(currentPlayer);
        refreshLayers();
    }

    /**
     * 功能描述：下一个玩家
     * @author cyt
     * @date 2026/5/16 19:42
     */
    public void nextPlayer()
    {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
        currentPlayer = players[currentPlayerIndex];
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
        decreaseAllBarrierRounds();
    }

    /**
     * 功能描述：所有的路障减少回合数
     * @author cyt
     * @date 2026/5/27 13:49
     */
    private void decreaseAllBarrierRounds()
    {
        for (Tile tile : tiles)
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
    private void renderBuildings(Graphics g) {
        //<editor-fold desc="AI 修改之前">
//        for (int i = tiles.length - 1; i >= 0; i--) {
//            Tile tile = tiles[i];
//
//            if (tile instanceof Land) {
//                Land land = (Land) tile;
//                // 只有当地产有所有者且有建筑时才渲染
//                if (land.getOwner() != null ) {
//                    renderBuildingForLand(g, land);
//                } else {
//                    renderBuidingForNull(g, land);
//                }
//            }
//        }
        //</editor-fold>

        for (int i = tiles.length - 1; i >= 0; i--) {
            Tile tile = tiles[i];
            if (tile instanceof Land land) {
                int index = land.getPositionIndex();
                if ((index >= 0 && index <= 9) || (index >= 22 && index <= 29)) {
                    renderLand(g, land);
                }
            }
        }

        // 第二步：再渲染正向区域（右+左，后画，盖在前面）
        for (int i = 0; i < tiles.length; i++) {
            Tile tile = tiles[i];
            if (tile instanceof Land land) {
                int index = land.getPositionIndex();
                // 右半段 10~15 + 左半段 22~29，正向渲染
                if ((index >= 10 && index <= 15) || (index >= 16 && index <= 21)) {
                    renderLand(g, land);
                }
            }
        }
    }


    /**
     * 功能描述：AI 提取的简化方法
     * @author cyt
     * @date 2026/5/17 22:15
     */
    private void renderLand(Graphics g, Land land) {
        if (land.getOwner() != null) {
                 int x = (int) land.getPosition().getX() + land.getOffSetX();
                 int y = (int) land.getPosition().getY() + land.getOffSetY();
            Image[] images;
            if( land.getOwner().equals(naiLong)) {images = land.getNaiLongImg();}
            else {images = land.getXiaoMeiImg();}

            int s = land.getPositionIndex();
            if (!( s==5 || s==4 || s==11 || s==26 || s==17 )){
                int imgX = images[land.getHouseLevel()].getWidth(null);
                int imgY = images[land.getHouseLevel()].getHeight(null);
                g.drawImage(
                        images[land.getHouseLevel()],
                        x + 130 - imgX,y - imgY + 100 ,
                        null
                );
            } else if ( s==4 || s==17 ){
                g.drawImage(
                  images[land.getHouseLevel()],
                  x + 5,y +10 ,
                  126,94,
                   null
                );
            }else if ( s==5 )
            {
                int imgX = images[land.getHouseLevel()].getWidth(null);
                int imgY = images[land.getHouseLevel()].getHeight(null);
                g.drawImage(
                        images[land.getHouseLevel()],
                        x - imgX + 270,y - imgY + 190,
                        null
                );
            } else
            {
                int imgX = images[land.getHouseLevel()].getWidth(null);
                int imgY = images[land.getHouseLevel()].getHeight(null);
                g.drawImage(
                        images[land.getHouseLevel()],
                        x - imgX + 270,y - imgY + 190,
                        null
                );
            }

        } else {
            Point point = land.getPosition();
            if (point == null) return;

            int x = (int) point.getX();
            int y = (int) point.getY();

            Image unsoldImage = new ImageIcon("src/img/architecture/小地未售2.png").getImage();
            Image unsoldImage2 = new ImageIcon("src/img/architecture/大地未售2.png").getImage();
            Image unsoldImage4 = new ImageIcon("src/img/architecture/大地未售.png").getImage();
            Image unsoldImage3 = new ImageIcon("src/img/architecture/小地未售.png").getImage();

            int index = land.getPositionIndex();

            if (index == 5) {
                g.drawImage(unsoldImage2, x, y, null);
            } else if (index == 11 || index == 26) {
                g.drawImage(unsoldImage4, x, y, null);
            } else if ((index >= 2 && index <= 5) || (index >= 17 && index <= 20)) {
                g.drawImage(unsoldImage, x, y, null);
            } else if ((index >= 10 && index <= 13) || (index >= 25 && index <= 28)) {
                g.drawImage(unsoldImage3, x, y, null);
            }

        }
    }



    /**
     * 功能描述：AI给我的渲染图片
     * @author cyt
     * @date 2026/5/26 17:37
     */
    private void updatePropLabelsForCurrentPlayer() {
        // 1. 全隐藏
        for (JLabel label : propJLabels) {
            label.setVisible(false);
            label.setBounds(0, 0, 0, 0);
        }

        // 2. 根据 currentPlayer 的 props 再显示
        for (Map.Entry<String, Integer> entry : currentPlayer.getProps().entrySet()) {
            String name = entry.getKey();
            int count = entry.getValue();
            if (count <= 0) continue;

            switch (name) {
                case "包子" -> { propJLabels[0].setBounds(907,950,59,75); propJLabels[0].setVisible(true); }
                case "考试周" -> { propJLabels[1].setBounds(967,950,59,75); propJLabels[1].setVisible(true); }
                case "地雷" -> { propJLabels[2].setBounds(1033,950,59,75); propJLabels[2].setVisible(true); }
                case "路障" -> { propJLabels[3].setBounds(1093,950,59,75); propJLabels[3].setVisible(true); }
                case "偷取" -> { propJLabels[4].setBounds(1153,950,58,75); propJLabels[4].setVisible(true); }
                case "万能骰子" -> { propJLabels[5].setBounds(1218,950,59,75); propJLabels[5].setVisible(true); }
                case "升级卡" -> { propJLabels[6].setBounds(1277,950,59,75); propJLabels[6].setVisible(true); }
                case "身份证" -> { propJLabels[7].setBounds(1335,950,59,75); propJLabels[7].setVisible(true); }
            }
        }
    }


    /**
     * 功能描述：AI 传送按钮
     * @author cyt
     * @date 2026/5/27 15:08
     */
    public void teleportTo(int targetIndex)
    {
        if (naiLong == null || naiLong.isWalking() || targetIndex < 0 || targetIndex >=
                tiles.length) return;
        naiLong.setPositionIndex(targetIndex);
        naiLong.setPosition(new Point(points[targetIndex]));
        refreshLayers();
        Tile t = tiles[targetIndex];
        if (t != null) t.onPlayerArrive(naiLong);
        refreshLayers();
    }

    /**
     * 功能描述：渲染路障
     * @author cyt
     * @date 2026/5/26 20:59
     */
    private void renderBarriers(Graphics g)
    {
        Image barrierImg = new ImageIcon("src/img/props/拦路牌.png").getImage(); // 你资源里有这个

        for (Tile tile : tiles) {
            if (tile == null) continue;
            if (!tile.hasBarrier()) continue;
            Point point = points[tile.getPositionIndex()];
            g.drawImage(barrierImg,(int) point.getX(),(int) point.getY(),88,94,null);
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
