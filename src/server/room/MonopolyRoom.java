package server.room;

import server.engine.*;
import server.session.PlayerSession;
import shared.Message;
import shared.MessageType;
import server.util.MessageCodec;
import java.util.List;

public class MonopolyRoom implements Room
{
    private final String id;
    private PlayerSession host;
    private PlayerSession guest;
    private GameState state;
    private final MonopolyEngine engine;

    public MonopolyRoom(String id)
    {
        this.id = id;
        this.engine = new MonopolyEngine();
    }

    @Override public String getId() { return id; }
    @Override public PlayerSession getHost() { return host; }
    @Override public PlayerSession getGuest() { return guest; }
    @Override public boolean isFull() { return host != null && guest != null; }

    @Override
    public void onPlayerJoin(PlayerSession session)
    {
        if (host == null)
        {
            host = session;
            session.isHost = true;
            session.roomId = id;
            session.playerName = "naiLong";
            sendTo(session, new Message(MessageType.ROOM_CREATED).put("roomId", id));
        }
        else if (guest == null)
        {
            guest = session;
            session.roomId = id;
            session.playerName = "xiaoMei";
            sendTo(host, new Message(MessageType.PLAYER_JOINED).put("playerName", guest.playerName));
            sendTo(guest, new Message(MessageType.ROOM_CREATED).put("roomId", id));
        }
    }

    @Override
    public void onPlayerLeave(PlayerSession session)
    {
        if (state != null && !"finished".equals(state.phase))
        {
            int idx = getPlayerIndex(session);
            engine.processAction(state, idx, new Message(MessageType.LEAVE_ROOM));
        }
    }

    @Override
    public void onMessage(PlayerSession session, Message msg)
    {
        MessageType type = msg.getType();

        if (type == MessageType.READY)
        {
            if (isFull() && state == null)
            {
                state = engine.createInitialState(
                    host.playerName, host.getId(),
                    guest.playerName, guest.getId());
                // 告诉每个客户端各自的角色
                sendTo(host, new Message(MessageType.GAME_START)
                    .put("playerIndex", 0)
                    .put("playerName", "naiLong")
                    .put("firstPlayer", state.getCurrentPlayer().name));
                sendTo(guest, new Message(MessageType.GAME_START)
                    .put("playerIndex", 1)
                    .put("playerName", "xiaoMei")
                    .put("firstPlayer", state.getCurrentPlayer().name));
                broadcast(new Message(MessageType.TURN_NOTIFY)
                    .put("player", state.getCurrentPlayer().name));
            }
            return;
        }

        if (type == MessageType.LEAVE_ROOM)
        {
            onPlayerLeave(session);
            return;
        }

        if (state == null) return;
        int playerIdx = getPlayerIndex(session);
        List<EngineEvent> events = engine.processAction(state, playerIdx, msg);

        for (EngineEvent event : events)
        {
            if (event.targetPlayerIndex < 0)
                broadcast(event.message);
            else
                sendToPlayer(event.targetPlayerIndex, event.message);
        }
    }

    @Override public GameState getState() { return state; }

    @Override
    public int getPlayerIndex(PlayerSession session)
    {
        if (session == host) return 0;
        if (session == guest) return 1;
        return -1;
    }

    private void sendTo(PlayerSession session, Message msg)
    {
        if (session != null && session.socket.isOpen())
            session.socket.send(MessageCodec.encode(msg));
    }

    private void sendToPlayer(int idx, Message msg)
    {
        if (idx == 0) sendTo(host, msg);
        else if (idx == 1) sendTo(guest, msg);
    }

    private void broadcast(Message msg)
    {
        sendTo(host, msg);
        sendTo(guest, msg);
    }
}
