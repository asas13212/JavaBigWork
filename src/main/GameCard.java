package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 功能描述：卡片样式的模板类
 * @author cyt
 * @date 2026/5/13 20:51
 */
public class GameCard extends JPanel
{
    protected String name;
    String[] buttonPath = {
            "src/img/gameMenu/bk1.png",
            "src/img/gameMenu/bk2.png"
    };
    CardLayout cardLayout;
    Container container;

    /**
     * 功能描述：构造方法，初始化卡片名称和布局容器
     * @param name 卡片名称
     * @param cardLayout 卡片布局管理器
     * @param container 父容器
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public GameCard(String name,CardLayout cardLayout,Container container)
    {
        this.name = name;
        this.setLayout(null);
        this.cardLayout = cardLayout;
        this.container = container;
    }

    /**
     * 功能描述：添加背景图片
     * @author cyt
     * @date 2026/5/13 20:52
     */
    protected void addImage(String filePath)
    {
        JLabel jLabel = new JLabel(new ImageIcon(filePath));
        jLabel.setBounds(0,0,500,460);
        this.add(jLabel);
    }

    /**
     * 功能描述：name的get方法
     * @author cyt
     * @date 2026/5/13 20:48
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * 功能描述：name的set方法
     * @author cyt
     * @date 2026/5/13 20:49
     */
    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * 功能描述：加载退出图片
     * @author cyt
     * @date 2026/5/13 20:52
     */
    public void addExitButton(String filePath)
    {
        // 告诉idea，这个变量引用不会变
        final JLabel jLabel = new JLabel(new ImageIcon(filePath));
        jLabel.setBounds(0,0,60,60);
        this.add(jLabel);
        this.setComponentZOrder(jLabel, 0); // 强制到最上层
        this.repaint();

        jLabel.addMouseListener(new MouseAdapter()
        {
            /**
             * 功能描述：点击退出按钮，返回游戏菜单
             * @param e 鼠标事件
             * @author cyt
             * @date 2026/6/1 21:00
             */
            @Override
            public void mouseClicked(MouseEvent e)
            {
                cardLayout.show(container,"游戏菜单");
                container.revalidate();
                container.repaint();
            }

            /**
             * 功能描述：鼠标进入时切换为高亮按钮图标
             * @param e 鼠标事件
             * @author cyt
             * @date 2026/6/1 21:00
             */
            @Override
            public void mouseEntered(MouseEvent e)
            {
                jLabel.setIcon(new ImageIcon(buttonPath[1]));
            }

            /**
             * 功能描述：鼠标退出时恢复默认按钮图标
             * @param e 鼠标事件
             * @author cyt
             * @date 2026/6/1 21:00
             */
            @Override
            public void mouseExited(MouseEvent e)
            {
                // 退出要记得改变图标
                jLabel.setIcon(new ImageIcon(buttonPath[0]));
            }
        });
    }
}
