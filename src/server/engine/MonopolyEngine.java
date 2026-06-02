package server.engine;

import shared.Message;
import shared.MessageType;

import java.util.*;

/**
 * 功能描述：大富翁权威游戏规则引擎 - 纯服务端逻辑，零 UI 依赖
 * 处理掷骰、买地、升级、道具使用等核心游戏操作
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class MonopolyEngine implements GameEngine
{
    // ==================== 游戏常量 ====================

    private static final int TILE_COUNT = 30;
    private static final int[] SPECIAL_TILES = {0, 6, 12, 18, 24}; // 非地产格
    private static final int START_MONEY_BONUS = 2000;
    private static final int INITIAL_MONEY = 10000;
    private static final int INITIAL_HP = 100;
    private static final int MAX_LEVEL = 3;
    private static final int BARRIER_ROUNDS = 3;
    private static final int MINE_DAMAGE = 40;
    private static final int HEAL_AMOUNT = 40;
    private static final int MAX_HP = 100;

    // ==================== 工具字段 ====================

    private final Random random = new Random();

    // ==================== GameEngine 实现 ====================

    @Override
    public List<EngineEvent> processAction(GameState state, int playerIndex, Message action)
    {
        if (playerIndex < 0 || playerIndex >= state.players.size())
            return List.of();

        return switch (action.getType())
        {
            case ROLL_DICE  -> handleRollDice(state, playerIndex);
            case BUY_LAND   -> handleBuyLand(state, playerIndex, action);
            case UPGRADE    -> handleUpgrade(state, playerIndex, action);
            case USE_PROP   -> handleUseProp(state, playerIndex, action);
            case LEAVE_ROOM -> handleLeaveRoom(state, playerIndex);
            default         -> List.of();
        };
    }

    @Override
    public GameState createInitialState(String player0Name, String player0SessionId,
                                         String player1Name, String player1SessionId)
    {
        GameState state = new GameState(TILE_COUNT);

        // 玩家 0
        GameState.PlayerSnapshot p0 = new GameState.PlayerSnapshot();
        p0.name = player0Name;
        p0.sessionId = player0SessionId;
        p0.money = INITIAL_MONEY;
        p0.hp = INITIAL_HP;
        p0.positionIndex = 0;
        p0.isOnline = true;
        state.players.add(p0);

        // 玩家 1
        GameState.PlayerSnapshot p1 = new GameState.PlayerSnapshot();
        p1.name = player1Name;
        p1.sessionId = player1SessionId;
        p1.money = INITIAL_MONEY;
        p1.hp = INITIAL_HP;
        p1.positionIndex = 0;
        p1.isOnline = true;
        state.players.add(p1);

        // 随机先手
        state.currentPlayerIndex = random.nextBoolean() ? 0 : 1;
        state.phase = "rolling";

        return state;
    }

    // ==================== ROLL_DICE ====================

    private List<EngineEvent> handleRollDice(GameState state, int playerIndex)
    {
        // 校验
        if (state.currentPlayerIndex != playerIndex)
            return List.of(errorEvent("不是你的回合"));
        if (!"rolling".equals(state.phase))
            return List.of(errorEvent("当前阶段不可掷骰"));
        if ("finished".equals(state.phase))
            return List.of(errorEvent("游戏已结束"));

        GameState.PlayerSnapshot player = state.players.get(playerIndex);
        List<EngineEvent> events = new ArrayList<>();

        // 掷骰 1-6
        int dice = random.nextInt(6) + 1;
        events.add(EngineEvent.broadcast(
            new Message(MessageType.DICE_RESULT).put("value", dice)
        ));

        // 逐格行走，记录步数
        List<Integer> walkSteps = new ArrayList<>();

        for (int i = 0; i < dice; i++)
        {
            int nextPos = (player.positionIndex + i + 1) % TILE_COUNT;

            // 经过起点加钱
            if (nextPos == 0)
            {
                player.money += START_MONEY_BONUS;
            }

            // 检查地雷
            if (state.hasMine[nextPos])
            {
                state.hasMine[nextPos] = false;
                player.hp -= MINE_DAMAGE;
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "mine_explode")
                        .put("tile", nextPos)
                        .put("player", player.name)
                        .put("hp", player.hp)
                ));
            }

            // 检查路障
            if (state.hasBarrier[nextPos])
            {
                state.hasBarrier[nextPos] = false;
                state.barrierRounds[nextPos] = 0;
                player.barrierStopTurns = 1;
                walkSteps.add(nextPos);
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "barrier_hit")
                        .put("tile", nextPos)
                        .put("player", player.name)
                ));
                break;
            }

            walkSteps.add(nextPos);

            // 检查死亡（踩地雷后 HP <= 0）
            if (player.hp <= 0)
            {
                break;
            }
        }

        // 更新玩家位置
        if (!walkSteps.isEmpty())
        {
            player.positionIndex = walkSteps.get(walkSteps.size() - 1);
        }

        // 发送行走路径
        events.add(EngineEvent.broadcast(
            new Message(MessageType.WALK_ANIM).put("steps", walkSteps)
        ));

        // 检查游戏结束（HP <= 0 或 money < 0）
        if (checkGameOver(state, events))
            return events;

        // 解析到达格子的效果（路障停止后仍需触发 tile 效果，与本地游戏一致）
        resolveTileArrival(state, playerIndex, events);

        return events;
    }

    // ==================== BUY_LAND ====================

    private List<EngineEvent> handleBuyLand(GameState state, int playerIndex, Message action)
    {
        if (state.currentPlayerIndex != playerIndex)
            return List.of(errorEvent("不是你的回合"));
        if (!"choosing".equals(state.phase))
            return List.of(errorEvent("当前无需选择"));

        GameState.PlayerSnapshot player = state.players.get(playerIndex);
        int tileIdx = player.positionIndex;
        boolean choice = action.get("choice", false);
        List<EngineEvent> events = new ArrayList<>();

        if (choice && isPropertyTile(tileIdx) && state.landOwners[tileIdx] == -1)
        {
            int price = calculatePrice(tileIdx);
            if (player.money >= price)
            {
                player.money -= price;
                state.landOwners[tileIdx] = playerIndex;
                state.landLevels[tileIdx] = 1; // 初始等级为 1
                player.ownedLandIndices.add(tileIdx);
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "land_bought")
                        .put("tile", tileIdx)
                        .put("player", player.name)
                        .put("price", price)
                ));
            }
        }

        // 结束选择阶段，切换玩家
        state.phase = "rolling";
        if (!checkGameOver(state, events))
        {
            switchToNextPlayer(state, events);
        }
        return events;
    }

    // ==================== UPGRADE ====================

    private List<EngineEvent> handleUpgrade(GameState state, int playerIndex, Message action)
    {
        if (state.currentPlayerIndex != playerIndex)
            return List.of(errorEvent("不是你的回合"));
        if (!"choosing".equals(state.phase))
            return List.of(errorEvent("当前无需选择"));

        GameState.PlayerSnapshot player = state.players.get(playerIndex);
        int tileIdx = player.positionIndex;
        boolean choice = action.get("choice", false);
        List<EngineEvent> events = new ArrayList<>();

        if (choice && isPropertyTile(tileIdx)
                && state.landOwners[tileIdx] == playerIndex
                && state.landLevels[tileIdx] < MAX_LEVEL)
        {
            int upgradeCost = calculatePrice(tileIdx);
            if (player.money >= upgradeCost)
            {
                player.money -= upgradeCost;
                state.landLevels[tileIdx]++;
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "land_upgraded")
                        .put("tile", tileIdx)
                        .put("player", player.name)
                        .put("level", state.landLevels[tileIdx])
                        .put("cost", upgradeCost)
                ));
            }
        }

        // 结束选择阶段，切换玩家
        state.phase = "rolling";
        if (!checkGameOver(state, events))
        {
            switchToNextPlayer(state, events);
        }
        return events;
    }

    // ==================== USE_PROP ====================

    private List<EngineEvent> handleUseProp(GameState state, int playerIndex, Message action)
    {
        if (state.currentPlayerIndex != playerIndex)
            return List.of(errorEvent("不是你的回合"));
        if (!"rolling".equals(state.phase))
            return List.of(errorEvent("当前阶段不可使用道具"));

        GameState.PlayerSnapshot player = state.players.get(playerIndex);
        String propName = action.get("propName", "");
        List<EngineEvent> events = new ArrayList<>();

        // 检查道具库存
        Integer count = player.props.get(propName);
        if (count == null || count <= 0)
            return List.of(errorEvent("没有该道具: " + propName));

        switch (propName)
        {
            case "地雷" -> {
                // 放在当前玩家所在格子，不能重复放
                int tileIdx = player.positionIndex;
                if (state.hasMine[tileIdx])
                    return List.of(errorEvent("该位置已有地雷"));
                state.hasMine[tileIdx] = true;
                consumeProp(player, propName);
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "mine_placed")
                        .put("tile", tileIdx)
                        .put("player", player.name)
                ));
            }
            case "路障" -> {
                int targetIdx = action.get("targetIdx", -1);
                if (targetIdx < 0 || targetIdx >= TILE_COUNT)
                    return List.of(errorEvent("无效目标位置: " + targetIdx));
                if (state.hasBarrier[targetIdx])
                    return List.of(errorEvent("该位置已有路障"));
                state.hasBarrier[targetIdx] = true;
                state.barrierRounds[targetIdx] = BARRIER_ROUNDS;
                consumeProp(player, propName);
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "barrier_placed")
                        .put("tile", targetIdx)
                        .put("player", player.name)
                ));
            }
            case "包子" -> {
                if (player.hp >= MAX_HP)
                    return List.of(errorEvent("生命值已满"));
                player.hp = Math.min(player.hp + HEAL_AMOUNT, MAX_HP);
                consumeProp(player, propName);
                events.add(EngineEvent.toPlayer(playerIndex,
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "healed")
                        .put("hp", player.hp)
                        .put("amount", HEAL_AMOUNT)
                ));
                events.add(EngineEvent.toPlayer(otherIndex(playerIndex),
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "opponent_healed")
                        .put("player", player.name)
                        .put("hp", player.hp)
                ));
            }
            default -> {
                return List.of(errorEvent("未知道具: " + propName));
            }
        }

        return events;
    }

    // ==================== LEAVE_ROOM ====================

    private List<EngineEvent> handleLeaveRoom(GameState state, int playerIndex)
    {
        List<EngineEvent> events = new ArrayList<>();

        int otherIdx = otherIndex(playerIndex);
        GameState.PlayerSnapshot other = state.players.get(otherIdx);
        GameState.PlayerSnapshot leaver = state.players.get(playerIndex);

        leaver.isOnline = false;
        state.phase = "finished";
        state.winner = other.name;

        events.add(EngineEvent.broadcast(
            new Message(MessageType.PLAYER_LEFT)
                .put("leaver", leaver.name)
        ));
        events.add(EngineEvent.broadcast(
            new Message(MessageType.GAME_OVER)
                .put("winner", other.name)
                .put("reason", "player_left")
        ));

        return events;
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 解析到达格子的效果：判断是否为地产，触发购买/交税/升级逻辑
     */
    private void resolveTileArrival(GameState state, int playerIndex, List<EngineEvent> events)
    {
        GameState.PlayerSnapshot player = state.players.get(playerIndex);
        int tileIdx = player.positionIndex;

        // 特殊格子（非地产）不触发产权逻辑
        if (isSpecialTile(tileIdx))
        {
            switchToNextPlayer(state, events);
            return;
        }

        int ownerIdx = state.landOwners[tileIdx];

        if (ownerIdx == -1)
        {
            // 无人拥有 -> 询问购买
            int price = calculatePrice(tileIdx);
            state.phase = "choosing";
            events.add(EngineEvent.toPlayer(playerIndex,
                new Message(MessageType.ASK_CHOICE)
                    .put("question", "buy_land")
                    .put("tile", tileIdx)
                    .put("price", price)
            ));
        }
        else if (ownerIdx != playerIndex)
        {
            // 他人拥有 -> 交税
            int level = state.landLevels[tileIdx];
            int tax = calculateTax(tileIdx, level);
            player.money -= tax;
            state.players.get(ownerIdx).money += tax;
            events.add(EngineEvent.broadcast(
                new Message(MessageType.GAME_UPDATE)
                    .put("event", "tax_paid")
                    .put("tile", tileIdx)
                    .put("payer", player.name)
                    .put("receiver", state.players.get(ownerIdx).name)
                    .put("amount", tax)
            ));

            if (!checkGameOver(state, events))
            {
                switchToNextPlayer(state, events);
            }
        }
        else
        {
            // 自己拥有 -> 询问升级（如可升级）
            if (state.landLevels[tileIdx] < MAX_LEVEL)
            {
                int upgradeCost = calculatePrice(tileIdx);
                state.phase = "choosing";
                events.add(EngineEvent.toPlayer(playerIndex,
                    new Message(MessageType.ASK_CHOICE)
                        .put("question", "upgrade")
                        .put("tile", tileIdx)
                        .put("cost", upgradeCost)
                        .put("currentLevel", state.landLevels[tileIdx])
                ));
            }
            else
            {
                switchToNextPlayer(state, events);
            }
        }
    }

    /**
     * 切换到下一个可行动的玩家。
     * 跳过 barrierStopTurns > 0 的玩家，递增回合数并清理过期路障。
     */
    private void switchToNextPlayer(GameState state, List<EngineEvent> events)
    {
        int attempts = 0;
        int nextIdx = state.currentPlayerIndex;

        do {
            nextIdx = (nextIdx + 1) % state.players.size();

            // 当轮到玩家 0 时递增回合
            if (nextIdx == 0)
            {
                state.round++;
                decreaseAllBarrierRounds(state);
            }

            GameState.PlayerSnapshot nextPlayer = state.players.get(nextIdx);

            // 跳过路障停止的玩家
            if (nextPlayer.barrierStopTurns > 0)
            {
                nextPlayer.barrierStopTurns--;
                // 如果是因为坐牢的停止，且路障结束，清除状态
                if ("isPrisoned".equals(nextPlayer.status) && nextPlayer.barrierStopTurns == 0)
                {
                    nextPlayer.status = null;
                }
                continue;
            }

            break;
        } while (++attempts < state.players.size());

        state.currentPlayerIndex = nextIdx;
        state.phase = "rolling";

        events.add(EngineEvent.broadcast(
            new Message(MessageType.TURN_NOTIFY)
                .put("player", state.players.get(nextIdx).name)
                .put("playerIndex", nextIdx)
                .put("round", state.round)
        ));
    }

    /**
     * 所有路障减少 1 回合生命周期，归零的移除
     */
    private void decreaseAllBarrierRounds(GameState state)
    {
        for (int i = 0; i < state.tileCount; i++)
        {
            if (state.hasBarrier[i])
            {
                state.barrierRounds[i]--;
                if (state.barrierRounds[i] <= 0)
                {
                    state.hasBarrier[i] = false;
                    state.barrierRounds[i] = 0;
                }
            }
        }
    }

    /**
     * 检查游戏是否结束（HP <= 0 或 money < 0）。
     * @return true 表示游戏已结束
     */
    private boolean checkGameOver(GameState state, List<EngineEvent> events)
    {
        for (int i = 0; i < state.players.size(); i++)
        {
            GameState.PlayerSnapshot p = state.players.get(i);
            if (p.hp <= 0 || p.money < 0)
            {
                state.phase = "finished";
                int winnerIdx = otherIndex(i);
                state.winner = state.players.get(winnerIdx).name;
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_OVER)
                        .put("winner", state.winner)
                        .put("loser", p.name)
                        .put("reason", p.hp <= 0 ? "hp_zero" : "bankrupt")
                ));
                return true;
            }
        }
        return false;
    }

    // ==================== 工具方法 ====================

    /** 是否为特殊（非地产）格 */
    private boolean isSpecialTile(int idx)
    {
        for (int s : SPECIAL_TILES)
        {
            if (s == idx) return true;
        }
        return false;
    }

    /** 是否为地产格（非特殊格） */
    private boolean isPropertyTile(int idx)
    {
        return !isSpecialTile(idx);
    }

    /** 购买价格 = 500 + (index % 10) * 100 */
    private int calculatePrice(int tileIdx)
    {
        return 500 + (tileIdx % 10) * 100;
    }

    /** 过路费 = (200 + (index % 10) * 80) * level */
    private int calculateTax(int tileIdx, int level)
    {
        return (200 + (tileIdx % 10) * 80) * level;
    }

    /** 获取对手玩家的索引 */
    private int otherIndex(int playerIndex)
    {
        return (playerIndex + 1) % 2;
    }

    /** 消耗一个道具 */
    private void consumeProp(GameState.PlayerSnapshot player, String propName)
    {
        Integer count = player.props.get(propName);
        if (count != null)
        {
            if (count <= 1)
                player.props.remove(propName);
            else
                player.props.put(propName, count - 1);
        }
    }

    /** 创建一个 ERROR 事件的辅助方法 */
    private EngineEvent errorEvent(String msg)
    {
        return EngineEvent.broadcast(
            new Message(MessageType.ERROR).put("msg", msg)
        );
    }
}
