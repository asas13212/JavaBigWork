package server.engine;

import shared.Message;
import shared.MessageType;
import java.util.*;

/**
 * 精简引擎：只生成骰子+计算路径+控制回合。游戏逻辑由客户端 MainMap 运行。
 */
public class MonopolyEngine implements GameEngine
{
    private static final int TILE_COUNT = 30;
    private static final int START_BONUS = 2000;
    private final Random random = new Random();

    @Override
    public List<EngineEvent> processAction(GameState state, int playerIndex, Message action)
    {
        if (playerIndex < 0 || playerIndex >= state.players.size()) return List.of();
        return switch (action.getType())
        {
            case ROLL_DICE -> handleRollDice(state, playerIndex);
            case TURN_END  -> handleTurnEnd(state);
            case LEAVE_ROOM -> handleLeave(state, playerIndex);
            default        -> relayToOther(state, playerIndex, action);
        };
    }

    @Override
    public GameState createInitialState(String name0, String sid0, String name1, String sid1)
    {
        GameState s = new GameState(TILE_COUNT);
        GameState.PlayerSnapshot p0 = new GameState.PlayerSnapshot();
        p0.name = name0; p0.sessionId = sid0; p0.money = 10000; p0.hp = 100;
        GameState.PlayerSnapshot p1 = new GameState.PlayerSnapshot();
        p1.name = name1; p1.sessionId = sid1; p1.money = 10000; p1.hp = 100;
        s.players.add(p0); s.players.add(p1);
        s.currentPlayerIndex = 0; s.phase = "rolling";
        return s;
    }

    // === 掷骰 ===
    private List<EngineEvent> handleRollDice(GameState state, int playerIndex)
    {
        GameState.PlayerSnapshot p = state.players.get(playerIndex);
        int dice = random.nextInt(6) + 1;

        // 计算路径
        List<Integer> steps = new ArrayList<>();
        int pos = p.positionIndex;
        for (int i = 0; i < dice; i++)
        {
            pos = (pos + 1) % TILE_COUNT;
            steps.add(pos);
            if (pos == 0) p.money += START_BONUS;
        }
        p.positionIndex = pos;
        state.round++;

        List<EngineEvent> events = new ArrayList<>();
        events.add(EngineEvent.broadcast(
            new Message(MessageType.DICE_RESULT)
                .put("value", dice).put("player", p.name).put("round", state.round)));
        events.add(EngineEvent.broadcast(
            new Message(MessageType.WALK_ANIM)
                .put("steps", steps).put("player", p.name)
                .put("finalTile", steps.isEmpty() ? p.positionIndex : steps.get(steps.size()-1))));
        return events;
    }

    // === 回合结束 ===
    private List<EngineEvent> handleTurnEnd(GameState state)
    {
        state.currentPlayerIndex = (state.currentPlayerIndex + 1) % 2;
        for (int i = 0; i < 2; i++)
        {
            GameState.PlayerSnapshot next = state.getCurrentPlayer();
            if (next.barrierStopTurns > 0)
            {
                next.barrierStopTurns--;
                state.currentPlayerIndex = (state.currentPlayerIndex + 1) % 2;
            }
            else break;
        }
        return List.of(EngineEvent.broadcast(
            new Message(MessageType.TURN_NOTIFY)
                .put("player", state.getCurrentPlayer().name).put("round", state.round)));
    }

    // === 转发给对方 ===
    private List<EngineEvent> relayToOther(GameState state, int playerIndex, Message action)
    {
        int other = (playerIndex + 1) % 2;
        return List.of(EngineEvent.toPlayer(other,
            new Message(MessageType.GAME_UPDATE)
                .put("event", action.getType().name())
                .put("data", action.getData())));
    }

    private List<EngineEvent> handleLeave(GameState state, int playerIndex)
    {
        int other = (playerIndex + 1) % 2;
        return List.of(EngineEvent.broadcast(
            new Message(MessageType.GAME_OVER).put("winner", state.players.get(other).name)));
    }
}
