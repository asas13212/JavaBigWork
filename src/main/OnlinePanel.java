package main;

import debug.Log;
import shared.Message;
import shared.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.Socket;

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
    private JButton btnCreate, btnJoin;
    private boolean isHost = false;   // 标记自己是不是房主

    // 图片路径
    private static final String IMG_BG = "src/img/gameMenu/choice.png";
    private static final String IMG_BACK = "src/img/gameMenu/bk1.png";
    private static final String IMG_BACK_HOVER = "src/img/gameMenu/bk2.png";

    // 配色
    private static final Color COLOR_BLUE = new Color(50, 120, 220);
    private static final Color COLOR_GREEN = new Color(40, 180, 80);
    private static final Color COLOR_DARK = new Color(30, 30, 50);
    private static final Color COLOR_WHITE = new Color(255, 255, 255);
    private static final Color COLOR_YELLOW = new Color(255, 255, 180);

    public OnlinePanel(CardLayout cardLayout, Container container)
    {
        this.cardLayout = cardLayout;
        this.container = container;
        this.setLayout(null);
        this.setSize(ConstantNum.WINDOWS_WIDTH, ConstantNum.WINDOWS_HEIGHT);

        // 背景
        JLabel bg = new JLabel(new ImageIcon(IMG_BG));
        bg.setBounds(0, 0, ConstantNum.WINDOWS_WIDTH, ConstantNum.WINDOWS_HEIGHT);
        this.add(bg);

        // === 标题 ===
        JLabel titleLabel = new JLabel("联 机 对 战", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(COLOR_WHITE);
        titleLabel.setBounds(0, 25, 500, 40);
        this.add(titleLabel);

        // === 上半部分：创建房间 ===
        JLabel createLabel = new JLabel("我没有房间，创建一个新房间", SwingConstants.CENTER);
        createLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        createLabel.setForeground(new Color(200, 200, 220));
        createLabel.setBounds(0, 75, 500, 25);
        this.add(createLabel);

        btnCreate = new JButton("创 建 房 间");
        styleButton(btnCreate, COLOR_BLUE);
        btnCreate.setBounds(120, 110, 260, 55);
        btnCreate.setFont(new Font("微软雅黑", Font.BOLD, 22));
        btnCreate.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (btnCreate.isEnabled())
                {
                    Log.info("联机：创建房间");
                    statusLabel.setText("正在连接服务器...");
                    isHost = true;
                    setButtonsEnabled(false);
                    createRoom();
                }
            }
        });
        this.add(btnCreate);

        // === 分隔线 ===
        JLabel divider = new JLabel("━━━━━━ 或 ━━━━━━", SwingConstants.CENTER);
        divider.setFont(new Font("微软雅黑", Font.BOLD, 14));
        divider.setForeground(new Color(150, 150, 180));
        divider.setBounds(0, 175, 500, 25);
        this.add(divider);

        // === 下半部分：加入房间 ===
        JLabel joinLabel = new JLabel("朋友已创建房间，输入房间号加入", SwingConstants.CENTER);
        joinLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        joinLabel.setForeground(new Color(200, 200, 220));
        joinLabel.setBounds(0, 210, 500, 25);
        this.add(joinLabel);

        // 房间号输入框
        roomIdField = new JTextField();
        roomIdField.setFont(new Font("微软雅黑", Font.BOLD, 28));
        roomIdField.setHorizontalAlignment(SwingConstants.CENTER);
        roomIdField.setBounds(140, 245, 220, 50);
        roomIdField.setBackground(COLOR_DARK);
        roomIdField.setForeground(COLOR_WHITE);
        roomIdField.setCaretColor(COLOR_WHITE);
        roomIdField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BLUE, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        // 输入提示
        roomIdField.setToolTipText("请输入 4 位数字房间号");
        this.add(roomIdField);

        // 加入按钮
        btnJoin = new JButton("加 入 房 间");
        styleButton(btnJoin, COLOR_GREEN);
        btnJoin.setBounds(120, 310, 260, 50);
        btnJoin.setFont(new Font("微软雅黑", Font.BOLD, 20));
        btnJoin.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (!btnJoin.isEnabled()) return;
                String code = roomIdField.getText().trim();
                if (code.isEmpty())
                {
                    statusLabel.setText("⚠ 请先输入房间号！");
                    return;
                }
                if (!code.matches("\\d{4}"))
                {
                    statusLabel.setText("⚠ 房间号为 4 位数字，请检查！");
                    return;
                }
                Log.info("联机：加入房间 " + code);
                statusLabel.setText("正在加入房间 " + code + "...");
                setButtonsEnabled(false);
                joinRoom(code);
            }
        });
        this.add(btnJoin);

        // === 状态显示 ===
        statusLabel = new JLabel("输入 4 位房间号，然后点击「加入房间」", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        statusLabel.setForeground(COLOR_YELLOW);
        statusLabel.setBounds(30, 375, 440, 30);
        this.add(statusLabel);

        // === 返回按钮 ===
        addBackButton();

        // 确保组件在最上层
        this.setComponentZOrder(btnCreate, 0);
        this.setComponentZOrder(btnJoin, 0);
        this.setComponentZOrder(roomIdField, 0);
        this.setComponentZOrder(statusLabel, 0);

        Log.info("联机对战面板加载完毕");
    }

    /**
     * 统一样式的按钮
     */
    private void styleButton(JButton btn, Color bgColor)
    {
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        // 圆角边框
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.brighter(), 2),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
    }

    /**
     * 启用/禁用按钮
     */
    private void setButtonsEnabled(boolean enabled)
    {
        btnCreate.setEnabled(enabled);
        btnJoin.setEnabled(enabled);
    }

    // ==================== 网络逻辑 ====================

    private void createRoom()
    {
        // 先启动本地服务端（如果还没启动）
        startLocalServer();
        client = new NetworkClient("ws://localhost:8080");
        setupMessageHandler();
        connectAndSend(new Message(MessageType.CREATE_ROOM));
    }

    /**
     * 自动启动本地服务端（后台进程），如果端口已被占用则跳过
     */
    private void startLocalServer()
    {
        // 快速检测 8080 端口是否已被占用
        if (isPortOpen("localhost", 8080))
        {
            Log.info("服务端已在运行（端口 8080 被占用），跳过启动");
            return;
        }

        Log.info("自动启动本地服务端...");
        statusLabel.setText("正在启动服务端...");
        new Thread(() -> {
            try
            {
                String classpath = "out;lib/java-websocket-1.5.7.jar;lib/slf4j-api-2.0.9.jar;lib/slf4j-nop-2.0.9.jar";
                new ProcessBuilder("java", "-cp", classpath, "server.MonopolyServer", "8080")
                    .redirectErrorStream(true)
                    .start();

                // 轮询等待就绪
                for (int i = 0; i < 30; i++)
                {
                    Thread.sleep(300);
                    if (isPortOpen("localhost", 8080))
                    {
                        SwingUtilities.invokeLater(() ->
                            statusLabel.setText("服务端已就绪，正在创建房间..."));
                        return;
                    }
                }
                SwingUtilities.invokeLater(() ->
                    statusLabel.setText("⚠ 服务端启动超时，请手动运行 run-server.bat"));
            }
            catch (Exception e)
            {
                Log.error("启动服务端失败: " + e.getMessage());
                SwingUtilities.invokeLater(() ->
                    statusLabel.setText("⚠ 启动服务端失败: " + e.getMessage()));
            }
        }).start();
    }

    private boolean isPortOpen(String host, int port)
    {
        try (Socket s = new Socket(host, port))
        {
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private void joinRoom(String code)
    {
        client = new NetworkClient("ws://localhost:8080");
        setupMessageHandler();
        connectAndSend(new Message(MessageType.JOIN_ROOM).put("roomId", code));
    }

    private void connectAndSend(Message initialMsg)
    {
        new Thread(() -> {
            client.connect();
            try
            {
                for (int i = 0; i < 50 && !client.isConnected(); i++)
                    Thread.sleep(100);
            }
            catch (InterruptedException ignored) { }

            SwingUtilities.invokeLater(() -> {
                if (client.isConnected())
                {
                    client.send(initialMsg);
                }
                else
                {
                    statusLabel.setText("✗ 连接服务器超时，请检查网络");
                    setButtonsEnabled(true);
                }
            });
        }).start();
    }

    private void setupMessageHandler()
    {
        client.setOnMessage(msg -> {
            SwingUtilities.invokeLater(() -> {
                switch (msg.getType())
                {
                    case ROOM_CREATED:
                        roomId = msg.get("roomId", "");
                        roomIdField.setText(roomId);
                        if (isHost)
                        {
                            statusLabel.setText("✓ 房间创建成功！房间号：" + roomId + "，等待对手加入...");
                        }
                        else
                        {
                            statusLabel.setText("✓ 已加入房间 " + roomId + "，等待主机开始...");
                        }
                        break;
                    case PLAYER_JOINED:
                        String name = msg.get("playerName", "对手");
                        statusLabel.setText("✓ " + name + " 已加入！正在开始游戏...");
                        // 房主看到有人加入，自动发 READY 开始游戏
                        if (isHost && client != null && client.isConnected())
                        {
                            new Thread(() -> {
                                try { Thread.sleep(800); } catch (InterruptedException ignored) { }
                                SwingUtilities.invokeLater(() ->
                                    client.send(new Message(MessageType.READY)));
                            }).start();
                        }
                        break;
                    case GAME_START:
                        int myIdx = msg.get("playerIndex", 0);
                        String myName = msg.get("playerName", "naiLong");
                        String firstPlayer = msg.get("firstPlayer", "");
                        statusLabel.setText("✓ 游戏开始！你是 " + myName);
                        startOnlineGame(myIdx, myName, firstPlayer);
                        break;
                    case ERROR:
                        statusLabel.setText("✗ 错误：" + msg.get("msg", "未知错误"));
                        setButtonsEnabled(true);
                        break;
                }
            });
        });

        client.setOnDisconnect(() -> {
            SwingUtilities.invokeLater(() -> {
                if (statusLabel != null)
                    statusLabel.setText("✗ 连接断开，请重试");
                setButtonsEnabled(true);
            });
        });
    }

    private void startOnlineGame(int playerIndex, String playerName, String firstPlayer)
    {
        String role = (playerIndex == 0) ? "【房主】" : "【客户端】";
        MainMap mainMap = new MainMap(GameMode.ONLINE, playerIndex);
        mainMap.setTitle("大富翁 - " + role + " - " + playerName);
        RemoteController rc = new RemoteController(mainMap, client);
        rc.setLocalPlayerIndex(playerIndex);
        rc.setMyPlayerName(playerName);
        // 首个回合：firstPlayer==自己则 myTurn=true（防御 TURN_NOTIFY 时序丢失）
        if (playerName.equals(firstPlayer)) rc.setMyTurn(true);
        mainMap.setGameController(rc);
        rc.setupGameHandler();
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    // ==================== 返回按钮 ====================

    private void addBackButton()
    {
        JLabel backBtn = new JLabel(new ImageIcon(IMG_BACK));
        backBtn.setBounds(0, 0, 60, 60);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (client != null)
                    client.disconnect();
                cardLayout.show(container, "游戏菜单");
                container.revalidate();
                container.repaint();
            }
            @Override
            public void mouseEntered(MouseEvent e)
            {
                backBtn.setIcon(new ImageIcon(IMG_BACK_HOVER));
            }
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
