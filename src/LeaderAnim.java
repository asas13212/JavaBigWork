import javax.swing.*;
import java.awt.*;

public class LeaderAnim extends JFrame
{

    private Image[] img;
    int index = 0;

    public LeaderAnim() throws InterruptedException
    {
        this.setSize(500,460);
        this.setTitle("第一个窗口");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        loadImage();
        this.setVisible(true);

        // 开始初始动画
        startAnim();

        new GameIndex();

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
                "src/img/LeaderPic/1.png",
                "src/img/LeaderPic/2.png",
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
        super.paint(g);   // 这一句就是清空上一帧画面，防止花屏
        if ( index >= 0 && index < img.length) {
            g.drawImage(img[index], 0, 0, 500, 460, this);
        }
    }

    /**
     * 功能描述：正确启动初始图片
     * @author cyt
     * @date 2026/5/11 22:20
     */
    public void startAnim() throws InterruptedException
    {
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

            repaint();
        }
        System.out.println("成功脱出");
    }

}
