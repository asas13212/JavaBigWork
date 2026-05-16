package main;

import architecture.TileType;
import org.testng.annotations.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

public class MainMap extends JFrame
{
    JLayeredPane layered;
    JLabel bgLayer;
    JPanel uiLayer,tileLayer;
    JPanel actorLayer;
    Player naiLong;
    Player xiaoMei;
    Player currentPlayer;
    ImageIcon[] diceImg;
    Timer diceTimer;
    boolean isDiceRolling = false;
    int currentDiceFrame = 0;
    int diceValue;

    private boolean gameRunning = true;
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
    TileType[] tileTypes
    //<editor-fold desc="格子类型">
            = {
      TileType.START,TileType.EVENT,TileType.PROPERTY, TileType.PROPERTY,TileType.PROPERTY,TileType.PROPERTY,TileType.GACHA,TileType.SHOP,
            TileType.HOSPITAL,TileType.PROPERTY,TileType.PROPERTY,TileType.PROPERTY,TileType.PROPERTY,TileType.PROPERTY,TileType.EVENT,TileType.PRISON,
            TileType.GACHA,TileType.PROPERTY,TileType.PROPERTY,TileType.PROPERTY,TileType.PROPERTY,TileType.HOSPITAL,TileType.GACHA,TileType.EVENT,
            TileType.EVENT,TileType.PROPERTY,TileType.PROPERTY,TileType.PROPERTY,TileType.PROPERTY,TileType.EMPTY

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
        uiLayer = new JPanel(null);
        uiLayer.setOpaque(false);
        uiLayer.setBounds(0,0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);

        // tileLayer层
        tileLayer = new JPanel(null);
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
                // AI 教我做动画
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

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void setPoints(Point[] points)
    {
        this.points = points;
    }

    /**
     * 功能描述：添加ui界面的骰子并完成动画
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

                        //
                        currentDiceFrame = 0;

                        System.out.println(currentPlayer.getName() + "投掷出了" + diceValue
                        + "点数");


                        diceTimer = new Timer(80,evt ->
                        {
                            currentDiceFrame++;
                            actorLayer.repaint();

                            if(currentDiceFrame >= 8)
                            {
                                diceTimer.stop();
                                isDiceRolling = false;

                                jLabel1.setIcon(icon1);

                                // 骰子动画结束，启动行走动画
                                currentPlayer.startWalk(diceValue);

                                Timer walkTimer = new Timer(200, evt2 -> {
                                    boolean done = currentPlayer.advanceOneStep();
                                    actorLayer.repaint();

                                    if (done) {
                                        ((Timer) evt2.getSource()).stop();
                                        currentPlayer.resetWalkFrame();
                                        actorLayer.repaint();
                                        nextPlayer();
                                    }
                                });
                                walkTimer.start();
                            }
                        });
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

}
