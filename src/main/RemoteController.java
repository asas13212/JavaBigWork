package main;

import shared.Message;
import shared.MessageType;
import debug.Log;
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
    { this.mainMap = mainMap; this.client = client; }

    public void setMyPlayerName(String name) { this.myPlayerName = name; }
    public String getMyPlayerName() { return myPlayerName; }
    public void setMyTurn(boolean turn) { this.myTurn = turn; }
    public void setLocalPlayerIndex(int idx) { this.localPlayerIndex = idx; }

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
    private int lastRound = 0;

    private void handleTurnNotify(Message msg)
    {
        String who = msg.get("player", "");
        int round = msg.get("round", 0);
        setMyTurn(myPlayerName.equals(who));
        int pIdx = who.equals("naiLong") ? 0 : 1;
        mainMap.setCurrentPlayerIndex(pIdx);

        if (round != lastRound)
        {
            lastRound = round;
            Log.info("====== 第 " + round + " 回合 ======");
        }
        Log.info("轮到 " + who + (myTurn ? "（我）" : "（对方）") + " 行动");
        mainMap.refreshLayers();
    }

    // ===== DICE =====
    private void handleDiceResult(Message msg)
    {
        String player = msg.get("player", "");
        int value = msg.get("value", 1);
        int round = msg.get("round", 0);
        int pIdx = player.equals("naiLong") ? 0 : 1;
        mainMap.setCurrentPlayerIndex(pIdx);

        if (pIdx == localPlayerIndex)
        {
            mainMap.getDiceController().triggerRollWithValue(value);
        }
        else
        {
            Log.info(player + " 投掷出了 " + value + " 点");
            mainMap.getDiceController().showResultOnly(value);
        }
    }

    // ===== WALK (远程玩家) =====
    @SuppressWarnings("unchecked")
    private void handleWalkAnim(Message msg)
    {
        String player = msg.get("player", "");
        int pIdx = player.equals("naiLong") ? 0 : 1;
        if (pIdx == localPlayerIndex) return;

        List<Integer> steps = (List<Integer>) msg.get("steps");
        if (steps == null || steps.isEmpty()) return;

        int finalTile = msg.get("finalTile", steps.get(steps.size() - 1));
        Log.info(player + " 走了 " + steps.size() + " 步，到达格子 #" + finalTile);
        mainMap.startRemoteWalk(pIdx, steps);
    }

    // ===== 状态同步 =====
    private void handleGameUpdate(Message msg)
    {
        String event = msg.get("event", "");
        if (event == null) return;
        Player p0 = mainMap.getPlayer(0), p1 = mainMap.getPlayer(1);

        switch (event)
        {
            case "BUY_LAND":
            case "UPGRADE":
            case "USE_PROP":
                // relayToOther 转发的消息，后续完善
                break;
        }
        mainMap.refreshLayers();
    }

    private void handleGameOver(Message msg)
    {
        String winner = msg.get("winner", "");
        Log.info("游戏结束！" + winner + " 获胜！");
        JOptionPane.showMessageDialog(mainMap, winner + " 获胜！游戏结束");
    }

    // ===== 操作 → 服务端 =====
    @Override public void onDiceClicked()
    { if (!myTurn) return; myTurn = false; client.send(new Message(MessageType.ROLL_DICE)); }

    @Override public void onPropClicked(String propName)
    { if (!myTurn) return; client.send(new Message(MessageType.USE_PROP).put("propName", propName)); }

    @Override public void onLandChoice(boolean yes)
    { client.send(new Message(MessageType.BUY_LAND).put("choice", yes)); }

    @Override public void onUpgradeChoice(boolean yes)
    { client.send(new Message(MessageType.UPGRADE).put("choice", yes)); }

    public void turnEnded()
    { client.send(new Message(MessageType.TURN_END)); }

    @Override public GameMode getMode() { return GameMode.ONLINE; }
    @Override public boolean isMyTurn() { return myTurn; }
    @Override public int getLocalPlayerIndex() { return localPlayerIndex; }
}
