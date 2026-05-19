package main;

import architecture.*;
import architecture.Event;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

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
    boolean isDiceRolling = false;
    int currentDiceFrame = 0;
    int diceValue;

    private int currentPlayerIndex = 0;
    Player[] players;
    static int round = 0;

    public ImageIcon[] naiLongLand =
    {
        null,null,new ImageIcon("src/img/architecture/naiLong/red22")
    };


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
            new Event(1, new Point(), "公园"),
            new ResidentLand(2, new Point(280, 375), "体育馆"),
            new ResidentLand(3, new Point(350, 325), "旅馆"),
            new ResearchLand(4, new Point(420, 275), "研究所"),
            new HotelLand(5, new Point(415, 165), "研究所"),
            new Empty(),
            new Chance(7,  new Point(), "抽卡点"),
            new Shop(8, new Point(), "商店"),

            new Hospital(9, new Point(), "医院"),
            new ResidentLand(10,  new Point(960, 185), "体育馆"),
            new ParkLand(11,  new Point(1026, 210), "小医院"),
            new Empty(),
            new ResidentLand(13, new Point(1160, 330), "居民楼3"),
            new Event(14, new Point(), "公园2"),
            new Prison(15, new Point(), "牢中"),

            new Chance(16, new Point(), "抽卡点2"),
            new GymLand(17, new Point(1028, 518), "暂定1"),
            new ResidentLand(18, new Point(958, 568), "暂定2"),
            new ResidentLand(19, new Point(958 - 70, 618), "暂定3"),
            new ResidentLand(20, new Point(958 - 140, 668), "暂定4"),
            new Hospital(21, new Point(), "医院2"),
            new Chance(22,  new Point(), "抽卡点3"),
            new Event(23,  new Point(), "公园"),
            new Empty(),

            new ResidentLand(25,  new Point(620, 715), "暂定5"),
            new ShopLand(26,  new Point(483, 600), "暂定6"),
            new Empty(),
            new ResidentLand(28,  new Point(419, 570), "暂定8"),
            new Empty()
    };
    //</editor-fold>


    /**
     * 功能描述：主地图的构造方法
     * @author cyt
     * @date 2026/5/14 14:29
     */
    public MainMap()
    {
        this.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                System.out.println(e.getX() + "," + e.getY());
            }
        });
        init();

        loadPlayerAndImg();



        startGameLoop();


    }


    /**
     * 功能描述：导入玩家
     * @author cyt
     * @date 2026/5/15 18:34
     */
    private void loadPlayerAndImg()
    {
        naiLong = new Player(0,"naiLong",points[0]);
        xiaoMei = new Player(0,"xiaoMei",points[0]);
        naiLong.setOtherPlayer(xiaoMei);
        xiaoMei.setOtherPlayer(naiLong);
        naiLong.setMapPoints(points);
        xiaoMei.setMapPoints(points);
        loadSprites(naiLong, xiaoMei);

        players[0] = naiLong;
        players[1] = xiaoMei;

        currentPlayer = players[currentPlayerIndex];

        actorLayer.revalidate();
        actorLayer.repaint();
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
     * 功能描述：加上背景层、贴图层、uiLayer层
     * @author cyt
     * @date 2026/5/14 14:20
     */
    private void init()
    {
        players = new Player[2];
        diceImg = new ImageIcon[6];
        for (int i = 0; i < diceImg.length; i++)
        {
            diceImg[i] = new ImageIcon("src/img/dice/0" + (i+1) + ".png");
        }

        this.setResizable(false);
        this.setSize(ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);
        this.setLayout(null);

        // 使用JLayeredPane实现分层
        layered = new JLayeredPane();
        layered.setBounds(0, 0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);
        this.setContentPane(layered);

        // bgLayer层
        bgLayer = new JLabel(new ImageIcon("src/img/map/MainMap.png"));
        bgLayer.setBounds(0,0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);

        // uiLayer层
        uiLayer = new JPanel(null){
            @Override
            public void paintComponent(Graphics g)
            {

                // 清空了上一次绘画东西
                super.paintComponent(g);
                g.drawImage(new ImageIcon("src/img/player/naiLong3.png").getImage(), 10, 60, 340, 110, null);
                g.drawImage(new ImageIcon("src/img/player/xiaoMei3.png").getImage(), 10, 200, 340, 110, null);
                String naiLongMoney = Integer.toString(naiLong.getMoney());
                String xiaoMeiMoney = Integer.toString(xiaoMei.getMoney());
                String naiLongProperty = Integer.toString(naiLong.getProperty());
                String xiaoMeiProperty = Integer.toString(xiaoMei.getProperty());

                Font font = new Font("微软雅黑",Font.BOLD,14);
                g.setFont(font);

                g.drawString(naiLongMoney,200,102);
                g.drawString(xiaoMeiMoney,200,242);
                g.drawString(naiLongProperty,240,145);
                g.drawString(xiaoMeiProperty,240,288);

            }
        };
        uiLayer.setOpaque(false);
        uiLayer.setBounds(0,0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);

        // tileLayer层
        tileLayer = new JPanel(null){
            public void paintComponent(Graphics g)
            {
                // 清空了上一次绘画东西
                super.paintComponent(g);
                renderBuildings(g);
            }
        };
        tileLayer.setOpaque(false);
        tileLayer.setBounds(0,0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);

        // actorLayer层
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

        layered.setLayer(bgLayer, 0);
        layered.setLayer(tileLayer, 50);
        layered.setLayer(actorLayer, 80);
        layered.setLayer(uiLayer, 100);
        // 添加顺序随意
        layered.add(bgLayer);
        layered.add(tileLayer);
        layered.add(actorLayer);
        layered.add(uiLayer);

        tileLayer.revalidate();
        tileLayer.repaint();

        addUiDice();

        initTeleportButtons();

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }


    /**
     * 功能描述：添加ui界面的骰子并启动动画
     * @author cyt
     * @date 2026/5/16 19:41
     */
    private void addUiDice()
    {
        ImageIcon icon1 = new ImageIcon("src/img/dice/vv.png");
        ImageIcon icon2 = new ImageIcon("src/img/dice/cc.png");

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

                Timer walkTimer = new Timer(200, evt2 -> {
                    boolean done = currentPlayer.advanceOneStep();
                    tileLayer.repaint();
                    actorLayer.repaint();

                    if (done) {
                        ((Timer) evt2.getSource()).stop();
                        refreshLayers();

                        buyLand();

                        nextPlayer();
                    }
                });
                walkTimer.start();
            }
        });
    }


    /**
     * 功能描述：一次性更新三个层级
     * @author cyt
     * @date 2026/5/19 12:26
     */
    private void refreshLayers()
    {
        tileLayer.repaint();
        actorLayer.repaint();
        uiLayer.repaint();
    }


    /**
     * 功能描述：玩家买地逻辑
     * @author cyt
     * @date 2026/5/19 12:26
     */
    private void buyLand()
    {
        if (tiles[currentPlayer.getPositionIndex()].getTileType() == TileType.PROPERTY)
        {
            Land land = (Land) tiles[currentPlayer.getPositionIndex()];
                land.onPlayerArrive(currentPlayer);
                refreshLayers();
        }
    }


    /**
     * 功能描述：开始游戏循环
     * @author cyt
     * @date 2026/5/16 19:43
     */
    private void startGameLoop()
    {
        updateGameUI();
    }


    /**
     * 功能描述：处理当前回合逻辑
     * @author cyt
     * @date 2026/5/16 19:43
     */
    private void handlePlayerTurn()
    {
        System.out.println("当前玩家是" + currentPlayer.getName());
        // 下一步逻辑
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
        updateGameUI();
        handlePlayerTurn();
    }


    /**
     * 功能描述：更新ui
     * @author cyt
     * @date 2026/5/16 19:43
     */
    private void updateGameUI(){

    }


    /**
     * 功能描述：增加游戏回合
     * @author cyt
     * @date 2026/5/16 19:44
     */
    public void roundIncrease()
    {
        round++;
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
                 int x = (int) land.getPosition().getX();
                 int y = (int) land.getPosition().getY();
            Image[] images;
            if( land.getOwner().equals(naiLong)) {images = land.getNaiLongImg();}
            else {images = land.getXiaoMeiImg();}

            int s = land.getPositionIndex();
            if (!( s==5 || s==4 || s==11 || s==26 || s==17 )){

                g.drawImage(
                        images[land.getHouseLevel()],
                        x + land.getOffSetX(),y + land.getOffSetY(),
                        null
                );
            } else if ( s==4 || s==17 ){
                g.drawImage(
                  images[land.getHouseLevel()],
                  x + land.getOffSetX(),y + land.getOffSetY(),
                  126,94,
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
     * 功能描述：测试启动
     * @author cyt
     * @date 2026/5/19 12:25
     */
    static void main()
    {
        new MainMap();
    }

    /**
     * 功能描述：创建传送按钮，让naiLong传送到指定位置
     * @param targetIndex 目标位置的索引
     * @author cyt
     * @date 2026/5/19
     */
    private void createTeleportButton(int targetIndex)
    {
        JButton teleportBtn = new JButton("传送到位置" + targetIndex);
        teleportBtn.setBounds(10, 350, 150, 40);
        teleportBtn.setFont(new Font("微软雅黑", Font.BOLD, 12));

        teleportBtn.addActionListener(e -> {
            if (naiLong != null && !naiLong.isWalking())
            {
                System.out.println("奈龙传送到位置: " + targetIndex);

                int oldIndex = naiLong.getPositionIndex();
                naiLong.setPositionIndex(targetIndex);
                naiLong.setPosition(new Point(points[targetIndex]));

                refreshLayers();

                System.out.println("从位置 " + oldIndex + " 传送到 " + targetIndex);

                Tile targetTile = tiles[targetIndex];
                if (targetTile != null)
                {
                    System.out.println("到达格子: " + targetTile.getName());

                    if (targetTile instanceof Land land)
                    {
                        land.onPlayerArrive(naiLong);
                        refreshLayers();
                    }
                }
            }
        });

        uiLayer.add(teleportBtn);
        uiLayer.revalidate();
        uiLayer.repaint();
    }

    /**
     * 功能描述：初始化传送按钮（可以调用多次创建多个按钮）
     * @author cyt
     * @date 2026/5/19
     */
    private void initTeleportButtons()
    {
        createTeleportButton(2);
    }
}
