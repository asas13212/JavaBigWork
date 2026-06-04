package shared;

/**
 * 功能描述：客户端与服务端共享的消息类型枚举，定义所有 WebSocket 通信的消息标识
 * @author cyt & Claude
 * @date 2026/6/2
 */
public enum MessageType
{
    // === 客户端 → 服务端 ===
    CREATE_ROOM,       // 创建房间
    JOIN_ROOM,         // 加入房间 {"roomId": 4821}
    READY,             // 准备开始
    ROLL_DICE,         // 掷骰
    USE_PROP,          // 使用道具 {"propName":"路障","targetIdx":12}
    BUY_LAND,          // 买地确认 {"choice":true}
    UPGRADE,           // 升级确认 {"choice":true}
    TURN_END,          // 回合结束，请求切换玩家
    LEAVE_ROOM,        // 离开房间

    // === 服务端 → 客户端 ===
    ROOM_CREATED,      // 房间已创建 {"roomId":4821}
    PLAYER_JOINED,     // 对手加入 {"playerName":"xiaoMei"}
    GAME_START,        // 游戏开始 {"firstPlayer":"naiLong"}
    GAME_STATE,        // 全量状态同步 {"state":{...}}
    GAME_UPDATE,       // 增量状态更新 {"update":{...}}
    TURN_NOTIFY,       // 通知轮到谁 {"player":"naiLong"}
    DICE_RESULT,       // 骰子结果 {"value":6}
    WALK_ANIM,         // 行走路径 {"steps":[0,1,2,...]}
    ASK_CHOICE,        // 要求玩家做选择 {"question":"buy_land","data":{...}}
    PLAYER_LEFT,       // 对手离开
    GAME_OVER,         // 游戏结束 {"winner":"naiLong"}
    ERROR              // 错误 {"msg":"房间不存在"}
}
