package server.session;

import org.java_websocket.WebSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager
{
    private final Map<WebSocket, PlayerSession> sessions = new ConcurrentHashMap<>();

    public PlayerSession register(WebSocket socket)
    {
        PlayerSession session = new PlayerSession(socket);
        sessions.put(socket, session);
        return session;
    }

    public void unregister(WebSocket socket)
    {
        sessions.remove(socket);
    }

    public PlayerSession get(WebSocket socket)
    {
        return sessions.get(socket);
    }

    public int getCount() { return sessions.size(); }
}
