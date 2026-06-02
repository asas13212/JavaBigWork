package server.room;

import server.engine.GameState;
import server.session.PlayerSession;
import shared.Message;

public interface Room
{
    String getId();
    PlayerSession getHost();
    PlayerSession getGuest();
    boolean isFull();
    void onPlayerJoin(PlayerSession session);
    void onPlayerLeave(PlayerSession session);
    void onMessage(PlayerSession session, Message msg);
    GameState getState();
    int getPlayerIndex(PlayerSession session);
}
