package main;

import shared.Message;
import shared.MessageType;

/**
 * 功能描述：联机对战控制器 —— 所有操作转为 JSON 消息发送到服务端
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class RemoteController implements GameController
{
    private final MainMap mainMap;
    private final NetworkClient client;
    private boolean myTurn = false;
    private String myPlayerName;
    private int localPlayerIndex = 0;

    public RemoteController(MainMap mainMap, NetworkClient client)
    {
        this.mainMap = mainMap;
        this.client = client;
    }

    public void setMyPlayerName(String name) { this.myPlayerName = name; }
    public String getMyPlayerName() { return myPlayerName; }
    public void setMyTurn(boolean turn) { this.myTurn = turn; }
    public void setLocalPlayerIndex(int idx) { this.localPlayerIndex = idx; }

    @Override
    public void onDiceClicked()
    {
        if (!myTurn) return;
        client.send(new Message(MessageType.ROLL_DICE));
    }

    @Override
    public void onPropClicked(String propName)
    {
        if (!myTurn) return;
        client.send(new Message(MessageType.USE_PROP).put("propName", propName));
    }

    @Override
    public void onLandChoice(boolean yes)
    {
        client.send(new Message(MessageType.BUY_LAND).put("choice", yes));
    }

    @Override
    public void onUpgradeChoice(boolean yes)
    {
        client.send(new Message(MessageType.UPGRADE).put("choice", yes));
    }

    @Override public GameMode getMode() { return GameMode.ONLINE; }
    @Override public boolean isMyTurn() { return myTurn; }
    @Override public int getLocalPlayerIndex() { return localPlayerIndex; }
    public NetworkClient getClient() { return client; }
}
