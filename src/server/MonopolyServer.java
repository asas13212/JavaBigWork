package server;

import server.room.RoomManager;
import server.session.SessionManager;
import server.transport.WebSocketEndpoint;

public class MonopolyServer
{
    public static void main(String[] args)
    {
        int port = 8080;
        if (args.length > 0)
        {
            try { port = Integer.parseInt(args[0]); }
            catch (NumberFormatException e)
            {
                System.err.println("端口格式错误，使用默认 8080");
            }
        }

        SessionManager sessionManager = new SessionManager();
        RoomManager roomManager = new RoomManager();

        WebSocketEndpoint server = new WebSocketEndpoint(port, sessionManager, roomManager);

        // Cleanup daemon thread
        Thread cleanupThread = new Thread(() -> {
            while (true)
            {
                try { Thread.sleep(300_000); } catch (InterruptedException e) { break; }
                System.out.println("[清理] 当前活跃房间数: " + roomManager.getRoomCount());
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();

        server.start();
        System.out.println("大富翁联机服务器启动成功！");
    }
}
