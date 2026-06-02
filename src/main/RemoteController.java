package main;

import shared.Message;
import shared.MessageType;

/**
 * 功能描述：联机对战控制器 —— 所有操作转为 JSON 消息发送到服务端，接收服务端事件驱动 MainMap
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class RemoteController implements GameController
{
    private final MainMap mainMap;
    private final NetworkClient client;
    private boolean myTurn = false;
    private String myPlayerName = "naiLong";
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

    /**
     * 游戏开始后接管 NetworkClient 的消息处理
     */
    public void setupGameHandler()
    {
        client.setOnMessage(msg -> {
            switch (msg.getType())
            {
                case TURN_NOTIFY:
                    String turnPlayer = msg.get("player", "");
                    boolean isMine = myPlayerName.equals(turnPlayer);
                    setMyTurn(isMine);
                    break;

                case DICE_RESULT:
                    // 服务端通知骰子结果，后续走路动画由 walk_anim 驱动
                    break;

                case WALK_ANIM:
                    // TODO: 驱动 MainMap 行走动画
                    break;

                case GAME_UPDATE:
                    // TODO: 同步棋盘状态
                    break;

                case ASK_CHOICE:
                    // TODO: 弹出选择对话框
                    break;

                case GAME_OVER:
                    String winner = msg.get("winner", "");
                    javax.swing.JOptionPane.showMessageDialog(mainMap, winner + " 获胜！");
                    break;
            }
        });
    }

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
