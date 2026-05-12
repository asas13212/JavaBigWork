
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GameMenu extends JPanel
{

    Image[] img;
    JLabel[] jLabels;
    JLabel[] jButtons;

    public GameMenu()
    {
        System.out.println("已切换到游戏菜单");
        this.setLayout(null);
        this.setVisible(true);
        this.loadImage();
        this.setSize(Constant.WINDOWS_WIDTH,Constant.WINDOWS_HEIGHT);
        this.UIDisplay();


        System.out.println("加载完毕");
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

        // 导入选择图标
        String[] btuPath = {
                "src/img/gameMenu/bk1.png",
//                "src/img/gameMenu/b2.png"
        };

        jButtons = new JLabel[5];
//        jButtons[4] = new JLabel(new ImageIcon(btuPath[1]));

        for (int i = 0; i < 4; i++)
        {
            jButtons[i] = new JLabel(new ImageIcon(btuPath[0]));
            if (jButtons[i] == null)
                System.out.println("第" + i + "张导入失败");
        }
    }

    /**
     * 功能描述：加载游戏主菜单
     * @author cyt
     * @date 2026/5/12 21:59
     */
    private void UIDisplay()
    {

        for (int i = 0; i < 4; i++)
        {
            jButtons[i].setBounds(50, 115 + i * 65, 60, 60);
            this.add(jButtons[i]);
        }

        for (int i = 0; i < img.length - 1; i++)
        {
            jLabels[i+1].setBounds(150,115 + i * 65,110,60);
            this.add(jLabels[i+1]);
//            System.out.println(i + 2 + "张加载成功");
        }

        jLabels[0].setBounds(0, 0, Constant.WINDOWS_WIDTH, Constant.WINDOWS_HEIGHT);
        this.add(jLabels[0]);
    }

    class MyBtuttonListener implements MouseListener
    {

        @Override
        public void mouseClicked(MouseEvent e)
        {

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

        }

        @Override
        public void mouseExited(MouseEvent e)
        {

        }
    }
}
