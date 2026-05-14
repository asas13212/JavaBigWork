import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MainMap extends JFrame
{
    JLayeredPane layered;
    JLabel bgLayer;
    JPanel uiLayer,tileLayer;
    JPanel actorLayer;
    Player naiLong;
    Player xiaoMei;
    Point[] points = {
            //<editor-fold desc="直接copy过来">
            new Point(235,515), new Point(305,465), new Point(375,415) ,new Point(445,360), new Point(510,310),
            new Point(580,265), new Point(650,215), new Point(720,165) ,new Point(790,135), new Point(860,185),
            new Point(925,230), new Point(990,275), new Point(1050,320),new Point(1120,370),new Point(1190,415),
            new Point(1260,465),new Point(1190,515),new Point(1120,565),new Point(1050,615),new Point(990,665),
            new Point(925,715), new Point(860,765), new Point(790,815), new Point(720,865), new Point(650,815),
            new Point(580,765), new Point(510,715), new Point(445,665), new Point(375,615), new Point(305,565)
    };
    //</editor-fold>

    /**
     * 功能描述：主地图的构造方法
     * @author cyt
     * @date 2026/5/14 14:29
     */
    public MainMap()
    {
        init();

        naiLong = new Player(0,"naiLong",points[0]);
        xiaoMei = new Player(0,"xiaoMei",points[0]);
        loadSprites(naiLong, xiaoMei);

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
        this.setResizable(false);
        this.setSize(Constant.MAP_WIDTH,Constant.MAP_HEIGHT);
        this.setLayout(null);

        // 使用JLayeredPane实现分层
        layered = new JLayeredPane();
        layered.setBounds(0, 0, Constant.MAP_WIDTH, Constant.MAP_HEIGHT);
        this.setContentPane(layered);

        // bgLayer层
        bgLayer = new JLabel(new ImageIcon("src/img/map/MainMap.png"));
        bgLayer.setBounds(0,0,Constant.MAP_WIDTH,Constant.MAP_HEIGHT);

        // uiLayer层
        uiLayer = new JPanel(null);
        uiLayer.setOpaque(false);
        uiLayer.setBounds(0,0,Constant.MAP_WIDTH,Constant.MAP_HEIGHT);

        // tileLayer层
        tileLayer = new JPanel(null);
        tileLayer.setOpaque(false);
        tileLayer.setBounds(0,0,Constant.MAP_WIDTH,Constant.MAP_HEIGHT);

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
            }
        };

        actorLayer.setOpaque(false);
        actorLayer.setBounds(0,0,Constant.MAP_WIDTH,Constant.MAP_HEIGHT);

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

        this.setVisible(true);
    }
}
