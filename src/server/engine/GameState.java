package server.engine;

import java.util.*;

/**
 * 功能描述：权威服务端游戏状态 - 纯数据类，表示服务端游戏完整状态
 * 所有游戏逻辑由 MonopolyEngine 操作此对象，不包含 UI 依赖
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class GameState
{
    // ==================== 玩家快照内部类 ====================

    public static class PlayerSnapshot
    {
        public String name;
        public String sessionId;
        public int money;
        public int hp;
        public int positionIndex;
        public int barrierStopTurns;
        public String status;           // null | "isPrisoned"
        public boolean isOnline;
        public Map<String, Integer> props;      // propName -> count
        public List<Integer> ownedLandIndices;  // tile indices owned

        public PlayerSnapshot()
        {
            props = new HashMap<>();
            ownedLandIndices = new ArrayList<>();
        }
    }

    // ==================== 棋盘状态 ====================

    public int[] landOwners;        // tileIdx -> playerIndex (-1 = unowned)
    public int[] landLevels;        // tileIdx -> level
    public boolean[] hasBarrier;
    public int[] barrierRounds;
    public boolean[] hasMine;

    // ==================== 游戏流程 ====================

    public int currentPlayerIndex;  // 0 or 1
    public int round;
    public String phase;            // "waiting" | "rolling" | "choosing" | "finished"
    public String winner;           // null or winner name

    public List<PlayerSnapshot> players;
    public int tileCount;

    // ==================== 构造方法 ====================

    public GameState(int tileCount)
    {
        this.tileCount = tileCount;
        this.players = new ArrayList<>();
        this.landOwners = new int[tileCount];
        this.landLevels = new int[tileCount];
        this.hasBarrier = new boolean[tileCount];
        this.barrierRounds = new int[tileCount];
        this.hasMine = new boolean[tileCount];
        Arrays.fill(landOwners, -1);
        this.phase = "waiting";
        this.round = 0;
    }

    // ==================== 便捷方法 ====================

    public PlayerSnapshot getCurrentPlayer()
    {
        if (players.isEmpty() || currentPlayerIndex < 0 || currentPlayerIndex >= players.size())
            return null;
        return players.get(currentPlayerIndex);
    }

    public PlayerSnapshot getPlayerByName(String name)
    {
        return players.stream().filter(p -> p.name.equals(name)).findFirst().orElse(null);
    }
}
