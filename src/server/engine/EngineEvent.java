package server.engine;

import shared.Message;

/**
 * 功能描述：引擎事件 - 表示一次游戏逻辑操作产生的输出，由 Room 层负责投递
 * targetPlayerIndex == -1 时表示广播给所有玩家
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class EngineEvent
{
    public int targetPlayerIndex;   // -1 = broadcast all
    public Message message;

    public EngineEvent(int targetPlayerIndex, Message message)
    {
        this.targetPlayerIndex = targetPlayerIndex;
        this.message = message;
    }

    /** 创建广播事件（发给所有玩家） */
    public static EngineEvent broadcast(Message msg)
    {
        return new EngineEvent(-1, msg);
    }

    /** 创建定向事件（发给指定玩家） */
    public static EngineEvent toPlayer(int idx, Message msg)
    {
        return new EngineEvent(idx, msg);
    }
}
