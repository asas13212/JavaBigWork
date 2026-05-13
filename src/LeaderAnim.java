import javax.swing.*;
import java.awt.*;

/**
 * 功能描述：引导界面
 * @author cyt
 * @date 2026/5/13 20:50
 */
public class LeaderAnim extends JFrame
{

    Image[] img;
    int index = 0;
    GameCard[] gameCard;
    CardLayout cardLayout;
    JPanel jPanel;

    /**
     * 功能描述：LeaderAnim构造方法
     * @author cyt
     * @date 2026/5/13 18:45
     */
    public LeaderAnim()
    {
        jPanel = new JPanel();
        cardLayout = new CardLayout();
        jPanel.setLayout(cardLayout);
        this.add(jPanel);
        this.setSize(Constant.WINDOWS_WIDTH,Constant.WINDOWS_HEIGHT);
        this.setTitle("第一个窗口");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.loadImage();
        this.setVisible(true);

        initGameCard();

        // 开始初始动画
        startAnim();
        // 切换到菜单
        switchToGameMenu();


    }

    /**
     * 功能描述：制作游戏卡片，切换界面使用
     * @author cyt
     * @date 2026/5/13 20:28
     */
    private void initGameCard()
    {
        gameCard = new GameCard[4];
        gameCard[0] = new GameCard("开始游戏",cardLayout,jPanel);
        gameCard[1] = new GameCard("游戏说明",cardLayout,jPanel);
        gameCard[2] = new GameCard("制作团队",cardLayout,jPanel);

        gameCard[1].addImage("src/img/gameMenu/shuoming.png");
        gameCard[2].addImage("src/img/gameMenu/team.png");
        gameCard[1].addExitButton("src/img/gameMenu/bk1.png");
        gameCard[2].addExitButton("src/img/gameMenu/bk1.png");

        jPanel.add(gameCard[0],"开始游戏");
        jPanel.add(gameCard[1],"游戏说明");
        jPanel.add(gameCard[2],"制作团队");
    }

    /**
     * 加载图片
     * <p>自动读取所有的png加载图片</p>
     */
    private void loadImage()
    {
        String[] filePath =
            {
                // 这里必须使用绝对路径
                "src/img/leaderPic/1.png",
                "src/img/leaderPic/2.png",
            };

        img = new Image[filePath.length];

        for (int i = 0; i < filePath.length; i++)
        {
            // ToolKit 工具包真是好用
            img[i] = Toolkit.getDefaultToolkit().getImage(filePath[i]);
        }
    }

    /**
     * 功能描述：重写 JFrame 的方法
     * @author cyt
     * @date 2026/5/11 19:50
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        if ( index >= 0 && index < img.length) {
            g.drawImage(img[index], 0, 0, 500, 460, this);
        }
    }

    /**
     * 功能描述：正确启动初始图片
     * @author cyt
     * @date 2026/5/11 22:20
     */
    public void startAnim()
    {
        // TODO 异步加载可能会首帧消失，Thread.sleep会阻塞
        while ( index < img.length )
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            index++;
            if( index == img.length )
                break;

            this.repaint();
        }
        System.out.println("成功脱出");
    }
/**
 * 功能描述：实现游戏菜单的进入
 * @author cyt
 * @date 2026/5/13 19:05
 */
    private void switchToGameMenu() {
        GameMenu gameMenu = new GameMenu();
        gameMenu.setCardLayout(cardLayout);
        gameMenu.setContainer(jPanel);
        jPanel.add(gameMenu, "游戏菜单");
        cardLayout.show(jPanel, "游戏菜单");
        jPanel.revalidate();
        jPanel.repaint();
    }
}
