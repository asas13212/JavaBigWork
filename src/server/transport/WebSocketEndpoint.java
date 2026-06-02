package server.transport;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import server.room.RoomManager;
import server.room.Room;
import server.session.PlayerSession;
import server.session.SessionManager;
import server.util.MessageCodec;
import shared.Message;
import shared.MessageType;

import java.net.InetSocketAddress;

public class WebSocketEndpoint extends WebSocketServer
{
    private final SessionManager sessionManager;
    private final RoomManager roomManager;

    public WebSocketEndpoint(int port, SessionManager sessionManager, RoomManager roomManager)
    {
        super(new InetSocketAddress(port));
        this.sessionManager = sessionManager;
        this.roomManager = roomManager;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        PlayerSession session = sessionManager.register(conn);
        System.out.println("[连接] 新客户端: " + session.getId());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        PlayerSession session = sessionManager.get(conn);
        if (session != null && session.roomId != null)
        {
            Room room = roomManager.getRoom(session.roomId);
            if (room != null)
                room.onPlayerLeave(session);
        }
        sessionManager.unregister(conn);
        System.out.println("[断开] 客户端: " + (session != null ? session.getId() : conn));
    }

    @Override
    public void onMessage(WebSocket conn, String json)
    {
        PlayerSession session = sessionManager.get(conn);
        if (session == null) return;

        Message msg = MessageCodec.decode(json);
        System.out.println("[消息] " + session.getId() + " → " + msg.getType());

        MessageType type = msg.getType();

        if (type == MessageType.CREATE_ROOM)
        {
            String roomId = roomManager.createRoom();
            Room room = roomManager.getRoom(roomId);
            room.onPlayerJoin(session);
            return;
        }

        if (type == MessageType.JOIN_ROOM)
        {
            String roomId = msg.get("roomId", "");
            Room room = roomManager.getRoom(roomId);
            if (room == null)
            {
                session.socket.send(MessageCodec.encode(
                    new Message(MessageType.ERROR).put("msg", "房间 " + roomId + " 不存在")));
                return;
            }
            if (room.isFull())
            {
                session.socket.send(MessageCodec.encode(
                    new Message(MessageType.ERROR).put("msg", "房间已满")));
                return;
            }
            room.onPlayerJoin(session);
            return;
        }

        if (session.roomId != null)
        {
            Room room = roomManager.getRoom(session.roomId);
            if (room != null)
                room.onMessage(session, msg);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        System.err.println("[错误] " + (conn != null ? conn.getRemoteSocketAddress() : "null") + ": " + ex.getMessage());
    }

    @Override
    public void onStart()
    {
        System.out.println("[服务] MonopolyServer 已启动，监听端口 " + getPort());
    }
}
