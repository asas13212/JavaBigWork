package server.engine;

import shared.Message;
import java.util.List;

/**
 * 功能描述：游戏引擎接口 - 纯函数式设计，输入消息输出事件，不依赖网络或 UI
 * 未来可替换为其他游戏类型（飞行棋、UNO...），实现无状态游戏逻辑
 * @author cyt & Claude
 * @date 2026/6/2
 */
public interface GameEngine
{
    /**
     * 处理玩家动作，返回本次操作产生的所有事件
     * @param state        当前游戏状态（会被原地修改）
     * @param playerIndex  发起动作的玩家索引（0 或 1）
     * @param action       玩家动作消息
     * @return 本次操作产生的事件列表（可能为空）
     */
    List<EngineEvent> processAction(GameState state, int playerIndex, Message action);

    /**
     * 创建初始游戏状态
     * @param player0Name      玩家0名称
     * @param player0SessionId 玩家0会话ID
     * @param player1Name      玩家1名称
     * @param player1SessionId 玩家1会话ID
     * @return 初始化的游戏状态
     */
    GameState createInitialState(String player0Name, String player0SessionId,
                                  String player1Name, String player1SessionId);
}
