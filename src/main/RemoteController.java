package main;

import shared.Message;
import shared.MessageType;
import javax.swing.*;
import java.util.List;

/**
 * 联机对战控制器：本地玩家跑完整本地逻辑，远程玩家看动画+同步状态
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

    /** 游戏开始后接管消息 */
    public void setupGameHandler()
    {
        client.setOnMessage(msg -> SwingUtilities.invokeLater(() -> {
            switch (msg.getType())
            {
                case TURN_NOTIFY: handleTurnNotify(msg); break;
                case DICE_RESULT: handleDiceResult(msg); break;
                case WALK_ANIM:  handleWalkAnim(msg);  break;
                case GAME_UPDATE: handleGameUpdate(msg); break;
                case GAME_OVER:   handleGameOver(msg);  break;
            }
        }));
    }

    // ===== TURN =====
    private void handleTurnNotify(Message msg)
    {
        String who = msg.get("player", "");
        setMyTurn(myPlayerName.equals(who));
        int pIdx = who.equals("naiLong") ? 0 : 1;
        mainMap.setCurrentPlayerIndex(pIdx);
        mainMap.refreshLayers();
    }

    // ===== DICE =====
    private void handleDiceResult(Message msg)
    {
        String player = msg.get("player", "");
        int value = msg.get("value", 1);
        int pIdx = player.equals("naiLong") ? 0 : 1;
        mainMap.setCurrentPlayerIndex(pIdx);

        if (pIdx == localPlayerIndex)
            mainMap.getDiceController().triggerRollWithValue(value);  // 动画+走路
        else
            mainMap.getDiceController().showResultOnly(value);        // 只显示点数
    }

    // ===== WALK ===== (远程玩家)
    @SuppressWarnings("unchecked")
    private void handleWalkAnim(Message msg)
    {
        String player = msg.get("player", "");
        int pIdx = player.equals("naiLong") ? 0 : 1;
        if (pIdx == localPlayerIndex) return; // 本地走路由骰子回调自动触发

        List<Integer> steps = (List<Integer>) msg.get("steps");
        if (steps == null || steps.isEmpty()) return;
        mainMap.startRemoteWalk(pIdx, steps);
    }

    // ===== STATE SYNC =====
    private void handleGameUpdate(Message msg)
    {
        String event = msg.get("event", "");
        if (event == null) return;
        Player p0 = mainMap.getPlayer(0), p1 = mainMap.getPlayer(1);
        switch (event)
        {
            case "money": case "hp": case "props": case "land": case "position":
                // 全量同步由具体字段驱动
                break;
            case "turn_end":
                // 对方回合结束，状态已在 DICE_RESULT 时更新
                break;
        }
        mainMap.refreshLayers();
    }

    private void handleGameOver(Message msg)
    {
        JOptionPane.showMessageDialog(mainMap, msg.get("winner", "") + " 获胜！");
    }

    // ===== 操作 → 服务端 =====
    @Override public void onDiceClicked()
    { if (!myTurn) return; client.send(new Message(MessageType.ROLL_DICE)); }

    @Override public void onPropClicked(String propName)
    { if (!myTurn) return; client.send(new Message(MessageType.USE_PROP).put("propName", propName)); }

    @Override public void onLandChoice(boolean yes)
    { client.send(new Message(MessageType.BUY_LAND).put("choice", yes)); }

    @Override public void onUpgradeChoice(boolean yes)
    { client.send(new Message(MessageType.UPGRADE).put("choice", yes)); }

    /** 本地回合走完（骰子→走路→格子逻辑全跑完），通知服务端切回合 */
    public void turnEnded()
    {
        client.send(new Message(MessageType.TURN_END));
    }

    @Override public GameMode getMode() { return GameMode.ONLINE; }
    @Override public boolean isMyTurn() { return myTurn; }
    @Override public int getLocalPlayerIndex() { return localPlayerIndex; }
}
