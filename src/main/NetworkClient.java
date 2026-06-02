package main;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import server.util.MessageCodec;
import shared.Message;

import java.net.URI;
import java.util.function.Consumer;

/**
 * 功能描述：WebSocket 客户端封装 —— 连接服务端、收发消息、断线回调
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class NetworkClient
{
    private WebSocketClient client;
    private final String serverUri;
    private Consumer<Message> onMessage;
    private Runnable onDisconnect;
    private boolean connected = false;

    public NetworkClient(String serverUri)
    {
        this.serverUri = serverUri;
    }

    public void setOnMessage(Consumer<Message> handler) { this.onMessage = handler; }
    public void setOnDisconnect(Runnable handler) { this.onDisconnect = handler; }
    public boolean isConnected() { return connected; }

    public void connect()
    {
        try
        {
            client = new WebSocketClient(new URI(serverUri))
            {
                @Override
                public void onOpen(ServerHandshake handshake)
                {
                    connected = true;
                    System.out.println("[NetworkClient] 已连接到 " + serverUri);
                }

                @Override
                public void onMessage(String json)
                {
                    Message msg = MessageCodec.decode(json);
                    System.out.println("[NetworkClient] 收到: " + msg.getType());
                    if (NetworkClient.this.onMessage != null)
                        NetworkClient.this.onMessage.accept(msg);
                }

                @Override
                public void onClose(int code, String reason, boolean remote)
                {
                    connected = false;
                    System.out.println("[NetworkClient] 连接关闭: " + reason);
                    if (NetworkClient.this.onDisconnect != null)
                        NetworkClient.this.onDisconnect.run();
                }

                @Override
                public void onError(Exception ex)
                {
                    System.err.println("[NetworkClient] 错误: " + ex.getMessage());
                }
            };
            client.connect();
        }
        catch (Exception e)
        {
            System.err.println("[NetworkClient] 连接失败: " + e.getMessage());
        }
    }

    public void send(Message msg)
    {
        if (client != null && client.isOpen())
        {
            String json = MessageCodec.encode(msg);
            System.out.println("[NetworkClient] 发送: " + msg.getType());
            client.send(json);
        }
    }

    public void disconnect()
    {
        if (client != null)
        {
            client.close();
            connected = false;
        }
    }
}
