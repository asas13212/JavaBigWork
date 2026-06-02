package main;

import debug.Log;
import shared.Message;
import shared.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 功能描述：联机对战面板 —— 创建/加入房间，等待对手并启动联机游戏
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class OnlinePanel extends JPanel
{
    private CardLayout cardLayout;
    private Container container;

    // 联机相关
    private NetworkClient client;
    private String roomId;
    private JTextField roomIdField;
    private JLabel statusLabel;

    // 按钮图片路径
    private static final String IMG_23 = "src/img/gameMenu/23.png";
    private static final String IMG_24 = "src/img/gameMenu/24.png";
    private static final String IMG_CHOICE = "src/img/gameMenu/choice.png";
    private static final String IMG_BACK = "src/img/gameMenu/bk1.png";
    private static final String IMG_BACK_HOVER = "src/img/gameMenu/bk2.png";

    /** 23.png / 24.png 实际像素尺寸 */
    private static final int BTN_W = 224;
    private static final int BTN_H = 307;

    /**
     * 功能描述：构造方法，加载联机对战面板
     * @param cardLayout 卡片布局管理器
     * @param container 父容器
     * @author cyt & Claude
     * @date 2026/6/2
     */
    public OnlinePanel(CardLayout cardLayout, Container container)
    {
        this.cardLayout = cardLayout;
        this.container = container;
        this.setLayout(null);
        this.setSize(ConstantNum.WINDOWS_WIDTH, ConstantNum.WINDOWS_HEIGHT);

        // 背景图
        JLabel bg = new JLabel(new ImageIcon(IMG_CHOICE));
        bg.setBounds(0, 0, ConstantNum.WINDOWS_WIDTH, ConstantNum.WINDOWS_HEIGHT);
        this.add(bg);

        // 两个模式按钮左右居中放置（靠近顶部）
        int totalBtnW = BTN_W * 2;
        int gap = (ConstantNum.WINDOWS_WIDTH - totalBtnW) / 3;  // 三等分间距
        int btnY = 30;

        // 23.png — 创建房间
        JLabel btnCreate = createButton(IMG_23, gap - 10, btnY, BTN_W, BTN_H);
        btnCreate.addMouseListener(new MouseAdapter()
        {
            /**
             * 功能描述：点击创建房间，连接服务器并发送 CREATE_ROOM 消息
             * @param e 鼠标事件
             * @author cyt & Claude
             * @date 2026/6/2
             */
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Log.info("点击：创建房间");
                createRoom();
            }
        });
        this.add(btnCreate);

        // 24.png — 加入房间
        JLabel btnJoin = createButton(IMG_24, gap * 2 + BTN_W, btnY, BTN_W, BTN_H);
        btnJoin.addMouseListener(new MouseAdapter()
        {
            /**
             * 功能描述：点击加入房间，读取房间号并发送 JOIN_ROOM 消息
             * @param e 鼠标事件
             * @author cyt & Claude
             * @date 2026/6/2
             */
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Log.info("点击：加入房间");
                joinRoom();
            }
        });
        this.add(btnJoin);

        // 房间号输入框
        roomIdField = new JTextField();
        roomIdField.setBounds(150, btnY + BTN_H + 20, 200, 35);
        roomIdField.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        roomIdField.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(roomIdField);

        // 状态标签
        statusLabel = new JLabel("输入房间号后点击「加入房间」", SwingConstants.CENTER);
        statusLabel.setBounds(50, btnY + BTN_H + 60, 400, 30);
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        statusLabel.setForeground(new Color(255, 255, 200));
        this.add(statusLabel);

        // 返回按钮（左上角）
        addBackButton();

        // 确保按钮在最上层
        this.setComponentZOrder(btnCreate, 0);
        this.setComponentZOrder(btnJoin, 0);

        Log.info("联机对战面板加载完毕");
    }

    /**
     * 功能描述：创建一个可点击的图片按钮
     * @param imgPath 图片路径
     * @param x 横坐标
     * @param y 纵坐标
     * @param w 宽度
     * @param h 高度
     * @return 配置好的 JLabel 按钮
     * @author cyt & Claude
     * @date 2026/6/2
     */
    private JLabel createButton(String imgPath, int x, int y, int w, int h)
    {
        JLabel label = new JLabel(new ImageIcon(imgPath));
        label.setBounds(x, y, w, h);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return label;
    }

    /**
     * 功能描述：创建房间 —— 连接服务器并发送 CREATE_ROOM
     * @author cyt & Claude
     * @date 2026/6/2
     */
    private void createRoom()
    {
        statusLabel.setText("正在连接服务器...");
        client = new NetworkClient("ws://localhost:8080");
        setupMessageHandler();
        connectAndSend(new Message(MessageType.CREATE_ROOM));
    }

    /**
     * 功能描述：加入房间 —— 读取房间号，连接服务器并发送 JOIN_ROOM
     * @author cyt & Claude
     * @date 2026/6/2
     */
    private void joinRoom()
    {
        String code = roomIdField.getText().trim();
        if (code.isEmpty())
        {
            statusLabel.setText("请输入房间号");
            return;
        }
        statusLabel.setText("正在连接服务器...");
        client = new NetworkClient("ws://localhost:8080");
        setupMessageHandler();
        final String joinCode = code;
        connectAndSend(new Message(MessageType.JOIN_ROOM).put("roomId", joinCode));
    }

    /**
     * 功能描述：在后台线程中连接服务器，连接成功后发送初始消息
     * @param initialMsg 连接成功后发送的消息
     * @author cyt & Claude
     * @date 2026/6/2
     */
    private void connectAndSend(Message initialMsg)
    {
        new Thread(() -> {
            client.connect();
            // 轮询等待连接建立
            try
            {
                for (int i = 0; i < 50 && !client.isConnected(); i++)
                {
                    Thread.sleep(100);
                }
            }
            catch (InterruptedException ignored) { }

            if (client.isConnected())
            {
                SwingUtilities.invokeLater(() -> client.send(initialMsg));
            }
            else
            {
                SwingUtilities.invokeLater(() -> statusLabel.setText("连接服务器超时"));
            }
        }).start();
    }

    /**
     * 功能描述：设置 NetworkClient 的消息处理器
     * @author cyt & Claude
     * @date 2026/6/2
     */
    private void setupMessageHandler()
    {
        client.setOnMessage(msg -> {
            SwingUtilities.invokeLater(() -> {
                switch (msg.getType())
                {
                    case ROOM_CREATED:
                        roomId = msg.get("roomId", "");
                        statusLabel.setText("房间号: " + roomId + "，等待对手...");
                        roomIdField.setText(roomId);
                        break;
                    case PLAYER_JOINED:
                        statusLabel.setText(msg.get("playerName", "") + " 已加入！点击准备开始...");
                        break;
                    case GAME_START:
                        startOnlineGame();
                        break;
                    case ERROR:
                        statusLabel.setText(msg.get("msg", "错误"));
                        break;
                }
            });
        });
    }

    /**
     * 功能描述：服务端通知游戏开始，创建联机游戏主窗口
     * @author cyt & Claude
     * @date 2026/6/2
     */
    private void startOnlineGame()
    {
        MainMap mainMap = new MainMap(GameMode.ONLINE);
        RemoteController rc = new RemoteController(mainMap, client);
        mainMap.setGameController(rc);
        // RemoteController 将处理所有网络通信
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    /**
     * 功能描述：左上角返回按钮（与 GameCard 的退出按钮风格一致）
     * @author cyt & Claude
     * @date 2026/6/2
     */
    private void addBackButton()
    {
        JLabel backBtn = new JLabel(new ImageIcon(IMG_BACK));
        backBtn.setBounds(0, 0, 60, 60);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new MouseAdapter()
        {
            /**
             * 功能描述：点击返回按钮，断开连接并切换回游戏菜单
             * @param e 鼠标事件
             * @author cyt & Claude
             * @date 2026/6/2
             */
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (client != null && client.isConnected())
                {
                    client.disconnect();
                }
                cardLayout.show(container, "游戏菜单");
                container.revalidate();
                container.repaint();
            }

            /**
             * 功能描述：鼠标进入时切换为高亮返回图标
             * @param e 鼠标事件
             * @author cyt & Claude
             * @date 2026/6/2
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
             * @date 2026/6/2
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
