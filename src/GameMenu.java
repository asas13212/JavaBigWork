
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * 功能描述：游戏主菜单
 * @author cyt
 * @date 2026/5/13 20:50
 */
public class GameMenu extends JPanel
{

    Image[] img;
    JLabel[] jLabels;
    JLabel[] jLabelsClick;
    String[] buttonPath = {
            "src/img/gameMenu/bk1.png",
            "src/img/gameMenu/bk2.png"
    };
    CardLayout cardLayout;
    Container container;

    
    /**
     * 功能描述：游戏菜单构造方法
     * @author cyt
     * @date 2026/5/13 20:33
     */
    public GameMenu()
    {

        System.out.println("已切换到游戏菜单");
        this.setLayout(null);
        this.setVisible(true);
        this.loadImage();
        this.setSize(Constant.WINDOWS_WIDTH,Constant.WINDOWS_HEIGHT);
        this.uiDisplay();
        this.buttonAddListener();

        System.out.println("加载完毕");
    }
    
    /**
     * 功能描述：给卡片布局赋值
     * @author cyt
     * @date 2026/5/13 20:32
     */
    public void setCardLayout(CardLayout cardLayout)
    {
        this.cardLayout = cardLayout;
    }

    /**
     * 功能描述：给容器赋值
     * @author cyt
     * @date 2026/5/13 20:33
     */
    public void setContainer(Container container)
    {
        this.container = container;
    }

    /**
     * 功能描述：Menu界面的图片
     * @author cyt
     * @date 2026/5/12 19:27
     */
    private void loadImage()
    {
        img = new Image[5];

        // 导入开始游戏 的图片到img中
        for (int i = 0; i < 5; i++) {
            ImageIcon icon = new ImageIcon("src/img/gameMenu/" + (i + 1) + ".png");
            img[i] = icon.getImage();
        }

        // 导入img图片到按钮中
        jLabels = new JLabel[img.length];
        for (int i = 0; i < img.length; i++)
        {
            // jLabel中new ImageIcon
            assert img[i] != null;
            jLabels[i] = new JLabel(new ImageIcon(img[i]));
        }

        jLabelsClick = new JLabel[5];

        for (int i = 0; i < 4; i++)
        {
            jLabelsClick[i] = new JLabel(new ImageIcon(buttonPath[0]));
            if (jLabelsClick[i] == null)
                System.out.println("第" + i + "张导入失败");
        }

        // 导入红色图标
        jLabelsClick[4] = new JLabel(new ImageIcon(buttonPath[1]));

    }

    /**
     * 功能描述：加载游戏主菜单
     * @author cyt
     * @date 2026/5/12 21:59
     */
    private void uiDisplay()
    {

        for (int i = 0; i < 4; i++)
        {
            jLabelsClick[i].setBounds(50, 115 + i * 65, 60, 60);
            this.add(jLabelsClick[i]);
        }

        jLabelsClick[0].setName("开始游戏");
        jLabelsClick[1].setName("游戏说明");
        jLabelsClick[2].setName("制作团队");
        jLabelsClick[3].setName("退出游戏");

        for (int i = 0; i < img.length - 1; i++)
        {
            jLabels[i+1].setBounds(150,115 + i * 65,110,60);
            this.add(jLabels[i+1]);
//            System.out.println(i + 2 + "张加载成功");
        }

        jLabels[0].setBounds(0, 0, Constant.WINDOWS_WIDTH, Constant.WINDOWS_HEIGHT);
        this.add(jLabels[0]);
    }

    /**
     * 功能描述：给按钮添加监听
     * @author cyt
     * @date 2026/5/13 18:54
     */
    private void buttonAddListener()
    {

        for (int i = 0; i < jLabelsClick.length; i++)
        {
            jLabelsClick[i].addMouseListener(new MyButtonListener());
        }   
    }

    /**
     * 功能描述：内部类，负责鼠标监听逻辑
     * @author cyt
     * @date 2026/5/13 14:58
     */
    class MyButtonListener implements MouseListener
    {

        @Override
        public void mouseClicked(MouseEvent e)
        {
            JLabel jLabel = (JLabel) e.getSource();
            switch (jLabel.getName())
            {
                case "开始游戏":
                    System.out.println("开始游戏按钮被点击了");
                    cardLayout.show(container,jLabel.getName());
                    break;
                case "游戏说明":
                    System.out.println("游戏说明按钮被点击了");
                    cardLayout.show(container,jLabel.getName());
                    break;
                case "制作团队":
                    System.out.println("制作团队按钮被点击了");
                    cardLayout.show(container,jLabel.getName());
                    break;
                case "退出游戏":
                    System.exit(0);
                default:
                    break;
            }
        }

        @Override
        public void mousePressed(MouseEvent e)
        {

        }

        @Override
        public void mouseReleased(MouseEvent e)
        {

        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            // 进入时要记得图标
            JLabel jLabel = (JLabel) e.getSource();
            jLabel.setIcon(new ImageIcon(buttonPath[1]));
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            // 退出要记得改变图标
            JLabel jLabel = (JLabel) e.getSource();
            jLabel.setIcon(new ImageIcon(buttonPath[0]));
        }
    }


}


