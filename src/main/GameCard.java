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
            @Override
            public void mouseClicked(MouseEvent e)
            {
                cardLayout.show(container,"游戏菜单");
                container.revalidate();
                container.repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                jLabel.setIcon(new ImageIcon(buttonPath[1]));
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                // 退出要记得改变图标
                jLabel.setIcon(new ImageIcon(buttonPath[0]));
            }
        });
    }
}
