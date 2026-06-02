package main;

import debug.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 功能描述：游戏模式选择面板 —— 双人对战 / 人机对战
 * 点击"开始游戏"后显示，使用 choice.png 作背景，23.png/24.png 作按钮
 * @author cyt & Claude
 * @date 2026/6/1
 */
public class ModeSelectPanel extends JPanel
{
    private CardLayout cardLayout;
    private Container container;

    // 按钮图片路径
    private static final String IMG_23 = "src/img/gameMenu/23.png";
    private static final String IMG_24 = "src/img/gameMenu/24.png";
    private static final String IMG_CHOICE = "src/img/gameMenu/choice.png";
    private static final String IMG_BACK = "src/img/gameMenu/bk1.png";
    private static final String IMG_BACK_HOVER = "src/img/gameMenu/bk2.png";

    /** 23.png 实际像素尺寸 */
    private static final int BTN_W = 224;
    private static final int BTN_H = 307;

    /**
     * 功能描述：构造方法，加载双人对战/人机对战的模式选择界面
     * @param cardLayout 卡片布局管理器
     * @param container 父容器
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public ModeSelectPanel(CardLayout cardLayout, Container container)
    {
        this.cardLayout = cardLayout;
        this.container = container;
        this.setLayout(null);
        this.setSize(ConstantNum.WINDOWS_WIDTH, ConstantNum.WINDOWS_HEIGHT);

        // 背景图
        JLabel bg = new JLabel(new ImageIcon(IMG_CHOICE));
        bg.setBounds(0, 0, ConstantNum.WINDOWS_WIDTH, ConstantNum.WINDOWS_HEIGHT);
        this.add(bg);

        // 两个模式按钮左右居中放置
        int totalBtnW = BTN_W * 2;
        int gap = (ConstantNum.WINDOWS_WIDTH - totalBtnW) / 3;  // 三等分间距
        int btnY = (ConstantNum.WINDOWS_HEIGHT - BTN_H) / 2;

        // 23.png — 双人对战
        JLabel btn23 = createButton(IMG_23, gap -10, btnY, BTN_W, BTN_H, false);
        btn23.addMouseListener(new MouseAdapter()
        {
            /**
             * 功能描述：选择双人对战模式，关闭当前窗口并启动新游戏
             * @param e 鼠标事件
             * @author cyt & Claude
             * @date 2026/6/1 21:00
             */
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Log.info("选择模式：双人对战");
                new MainMap(GameMode.LOCAL_PVP);
                SwingUtilities.getWindowAncestor(ModeSelectPanel.this).dispose();
            }
        });
        this.add(btn23);

        // 24.png — 人机对战
        JLabel btn24 = createButton(IMG_24, gap * 2 + BTN_W, btnY, BTN_W, BTN_H, true);
        btn24.addMouseListener(new MouseAdapter()
        {
            /**
             * 功能描述：选择人机对战模式，关闭当前窗口并启动 AI 对战游戏
             * @param e 鼠标事件
             * @author cyt & Claude
             * @date 2026/6/1 21:00
             */
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Log.info("选择模式：人机对战");
                new MainMap(GameMode.LOCAL_PVE);
                SwingUtilities.getWindowAncestor(ModeSelectPanel.this).dispose();
            }
        });
        this.add(btn24);

        // 返回按钮（左上角）
        addBackButton();

        // 联机对战按钮（位于底部居中，带半透明背景）
        JButton btnOnline = new JButton("🌐 联 机 对 战");
        btnOnline.setFont(new Font("微软雅黑", Font.BOLD, 20));
        btnOnline.setForeground(Color.WHITE);
        btnOnline.setBackground(new Color(50, 120, 220, 200));
        btnOnline.setBounds(120, 380, 260, 50);
        btnOnline.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOnline.setContentAreaFilled(true);
        btnOnline.setBorderPainted(true);
        btnOnline.setBorder(BorderFactory.createLineBorder(new Color(80, 160, 255), 2));
        btnOnline.setFocusPainted(false);
        btnOnline.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Log.info("选择模式：联机对战");
                cardLayout.show(container, "联机对战");
                container.revalidate();
                container.repaint();
            }
        });
        this.add(btnOnline);

        // 确保按钮在最上层
        this.setComponentZOrder(btn23, 0);
        this.setComponentZOrder(btn24, 0);
        this.setComponentZOrder(btnOnline, 0);

        Log.info("模式选择面板加载完毕");
    }

    /**
     * 功能描述：创建一个可点击的图片按钮
     * @param imgPath 图片路径
     * @param x 横坐标
     * @param y 纵坐标
     * @param w 宽度
     * @param h 高度
     * @param isAI 是否为 AI 模式按钮
     * @return 配置好的 JLabel 按钮
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    private JLabel createButton(String imgPath, int x, int y, int w, int h, boolean isAI)
    {
        JLabel label = new JLabel(new ImageIcon(imgPath));
        label.setBounds(x, y, w, h);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // 记录是哪个按钮
        label.setName(isAI ? "AI" : "PVP");
        return label;
    }

    /**
     * 功能描述：左上角返回按钮（与 GameCard 的退出按钮风格一致）
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    private void addBackButton()
    {
        JLabel backBtn = new JLabel(new ImageIcon(IMG_BACK));
        backBtn.setBounds(0, 0, 60, 60);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new MouseAdapter()
        {
            /**
             * 功能描述：点击返回按钮，切换回游戏菜单
             * @param e 鼠标事件
             * @author cyt & Claude
             * @date 2026/6/1 21:00
             */
            @Override
            public void mouseClicked(MouseEvent e)
            {
                cardLayout.show(container, "游戏菜单");
                container.revalidate();
                container.repaint();
            }

            /**
             * 功能描述：鼠标进入时切换为高亮返回图标
             * @param e 鼠标事件
             * @author cyt & Claude
             * @date 2026/6/1 21:00
             */
            @Override
            public void mouseEntered(MouseEvent e)
            {
                backBtn.setIcon(new ImageIcon(IMG_BACK_HOVER));
            }

            /**
             * 功能描述：鼠标退出时恢复默认返回图标
             * @param e 鼠标事件
             * @author cyt & Claude
             * @date 2026/6/1 21:00
             */
            @Override
            public void mouseExited(MouseEvent e)
            {
                backBtn.setIcon(new ImageIcon(IMG_BACK));
            }
        });
        this.add(backBtn);
        this.setComponentZOrder(backBtn, 0);
    }
}
