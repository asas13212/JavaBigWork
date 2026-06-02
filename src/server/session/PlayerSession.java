package server.session;

import org.java_websocket.WebSocket;

public class PlayerSession
{
    public final WebSocket socket;
    public String playerName;
    public String roomId;          // null = not in a room
    public boolean isHost;         // true if created the room

    public PlayerSession(WebSocket socket)
    {
        this.socket = socket;
    }

    public String getId()
    {
        return socket.getRemoteSocketAddress().toString();
    }
}
