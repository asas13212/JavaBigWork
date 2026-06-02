# 大富翁联机对战 - 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 Monopoly Java Swing 游戏添加 WebSocket 联机对战功能（C/S 架构，服务端权威，房间号机制，2 人对战）

**Architecture:** 客户端引入 GameController 抽象层（LocalController/RemoteController）让三种模式共用 MainMap；服务端分四层（Transport → Session → Room → Engine），MonopolyEngine 是权威游戏状态机。客户端通过 NetworkClient → WebSocket → 阿里云 MonopolyServer 通信，JSON 协议。

**Tech Stack:** JDK 25, Java Swing, org.java-websocket 1.5.7, JSON (手动编解码)

**Design Doc:** `docs/superpowers/specs/2026-06-02-online-multiplayer-design.md`

---

## 文件结构总览

### 新增目录

```
src/
├── shared/                     # 客户端+服务端共享
│   └── MessageType.java        # 消息类型枚举 + JSON 编解码
├── server/                     # 服务端
│   ├── MonopolyServer.java     # 入口
│   ├── transport/
│   │   └── WebSocketEndpoint.java
│   ├── session/
│   │   ├── SessionManager.java
│   │   └── PlayerSession.java
│   ├── room/
│   │   ├── Room.java
│   │   ├── RoomManager.java
│   │   └── MonopolyRoom.java
│   ├── engine/
│   │   ├── GameEngine.java
│   │   ├── MonopolyEngine.java
│   │   └── GameState.java
│   └── util/
│       └── MessageCodec.java
└── main/                       # 客户端（新增文件）
    ├── GameController.java     # 接口
    ├── LocalController.java    # 本地双人
    ├── RemoteController.java   # 联机模式
    ├── NetworkClient.java      # WebSocket 客户端
    ├── OnlinePanel.java        # 联机面板 UI
    └── GameMode.java           # 枚举
```

### 修改文件

- `src/main/MainMap.java` — 引入 GameMode 枚举 + GameController 字段
- `src/main/GameMenu.java` — 新增"联机对战"入口
- `src/main/LeaderAnim.java` — 注册 OnlinePanel 到 CardLayout
- `src/main/ModeSelectPanel.java` — 无修改（保留双人/人机）
- `src/main/Player.java` — 新增 `sessionId` 字段
- `.gitignore` — 添加 lib/ 排除

### 新增依赖

- `lib/java-websocket-1.5.7.jar`

---

## Phase 1: 环境准备

### Task 1: 下载 WebSocket 库

**Files:**
- Create: `lib/java-websocket-1.5.7.jar` (download)

- [ ] **Step 1: 下载 java-websocket JAR**

```bash
mkdir -p lib
curl -L -o lib/java-websocket-1.5.7.jar \
  "https://repo1.maven.org/maven2/org/java-websocket/Java-WebSocket/1.5.7/Java-WebSocket-1.5.7.jar"
```

- [ ] **Step 2: 更新 .gitignore 排除 lib**

Read `E:\Projects\Monopoly\.gitignore`，在末尾追加：

```
### Dependencies ###
lib/
```

- [ ] **Step 3: 验证 JAR 存在**

```bash
ls -la lib/java-websocket-1.5.7.jar
```

Expected: 文件存在，约 150KB

- [ ] **Step 4: 验证编译 classpath 可用**

```bash
javac -encoding UTF-8 -cp "lib/java-websocket-1.5.7.jar" -d out --source-path src src/Main.java 2>&1 | head -5
```

Expected: 无错误（只有可能已有的 warning）

---

## Phase 2: 共享消息类型

### Task 2: 创建 MessageType 枚举与基础消息类

**Files:**
- Create: `src/shared/MessageType.java`

- [ ] **Step 1: 写入 MessageType.java**

```java
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
    HOTEL_CHOICE,      // 住店确认 {"choice":true}
    SHOP_BUY,          // 商店购买 {"itemName":"地雷"}
    CASINO_BET,        // 赌场下注 {"amount":500}
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
```

- [ ] **Step 2: 编译验证**

```bash
javac -encoding UTF-8 -d out src/shared/MessageType.java
```

Expected: 编译成功，无错误

- [ ] **Step 3: Commit**

```bash
git add src/shared/MessageType.java
git commit -m "feat: add shared MessageType enum for client-server protocol"
```

### Task 3: 创建 Message 消息封装类

**Files:**
- Create: `src/shared/Message.java`

- [ ] **Step 1: 写入 Message.java**

```java
package shared;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：WebSocket 消息封装，包含类型和可变数据字段
 * 使用方法：new Message(MessageType.CREATE_ROOM).put("roomId", 4821)
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class Message
{
    private MessageType type;
    private final Map<String, Object> data;

    public Message(MessageType type)
    {
        this.type = type;
        this.data = new HashMap<>();
    }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public Message put(String key, Object value)
    {
        data.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) { return (T) data.get(key); }

    public <T> T get(String key, T defaultValue)
    {
        T val = get(key);
        return val != null ? val : defaultValue;
    }

    public boolean has(String key) { return data.containsKey(key); }

    public Map<String, Object> getData() { return data; }
}
```

- [ ] **Step 2: 编译验证**

```bash
javac -encoding UTF-8 -d out src/shared/Message.java src/shared/MessageType.java
```

Expected: 编译成功

- [ ] **Step 3: Commit**

```bash
git add src/shared/Message.java
git commit -m "feat: add shared Message class for JSON-serializable messages"
```

### Task 4: 创建 MessageCodec JSON 编解码器

**Files:**
- Create: `src/server/util/MessageCodec.java`

- [ ] **Step 1: 创建目录并写入 MessageCodec.java**

```bash
mkdir -p src/server/util
```

```java
package server.util;

import shared.Message;
import shared.MessageType;

/**
 * 功能描述：消息 JSON 编解码器 —— 手动序列化/反序列化，零外部依赖
 * 消息格式：{"type":"ROLL_DICE","data":{"roomId":4821,...}}
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class MessageCodec
{
    /**
     * 编码 Message → JSON 字符串
     */
    public static String encode(Message msg)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"").append(msg.getType().name()).append("\"");
        if (!msg.getData().isEmpty())
        {
            sb.append(",\"data\":{");
            boolean first = true;
            for (var entry : msg.getData().entrySet())
            {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escape(entry.getKey())).append("\":");
                appendValue(sb, entry.getValue());
            }
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    private static void appendValue(StringBuilder sb, Object val)
    {
        if (val == null) { sb.append("null"); return; }
        if (val instanceof String) { sb.append("\"").append(escape((String) val)).append("\""); return; }
        if (val instanceof Number || val instanceof Boolean) { sb.append(val); return; }
        sb.append("\"").append(escape(val.toString())).append("\"");
    }

    /**
     * 解码 JSON 字符串 → Message
     */
    public static Message decode(String json)
    {
        String typeStr = extractValue(json, "type");
        if (typeStr == null) return new Message(MessageType.ERROR);

        MessageType type;
        try { type = MessageType.valueOf(typeStr); }
        catch (IllegalArgumentException e) { return new Message(MessageType.ERROR); }

        Message msg = new Message(type);

        // 简单 data 字段提取
        String dataBlock = extractBlock(json, "data");
        if (dataBlock != null)
        {
            // 提取每个 key-value
            parseDataBlock(dataBlock, msg);
        }
        return msg;
    }

    private static String extractValue(String json, String key)
    {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx += search.length();
        while (idx < json.length() && (json.charAt(idx) == ' ' || json.charAt(idx) == '\t')) idx++;

        if (json.charAt(idx) == '"')
        {
            int end = json.indexOf('"', idx + 1);
            return json.substring(idx + 1, end);
        }
        else if (json.charAt(idx) == '{' || json.charAt(idx) == '[')
        {
            return null; // 嵌套对象由 extractBlock 处理
        }
        else
        {
            // 数字或布尔
            int end = idx;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']')
                end++;
            return json.substring(idx, end).trim();
        }
    }

    private static String extractBlock(String json, String key)
    {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx += search.length();
        while (idx < json.length() && json.charAt(idx) != '{') idx++;
        int depth = 1;
        int start = idx;
        idx++;
        while (idx < json.length() && depth > 0)
        {
            if (json.charAt(idx) == '{') depth++;
            else if (json.charAt(idx) == '}') depth--;
            idx++;
        }
        return json.substring(start, idx);
    }

    private static void parseDataBlock(String block, Message msg)
    {
        // 去掉首尾 { }
        block = block.trim();
        if (block.startsWith("{")) block = block.substring(1);
        if (block.endsWith("}")) block = block.substring(0, block.length() - 1);

        int i = 0;
        while (i < block.length())
        {
            // 跳过空白和逗号
            while (i < block.length() && (block.charAt(i) == ' ' || block.charAt(i) == ',' || block.charAt(i) == '\n')) i++;
            if (i >= block.length()) break;

            // 读 key
            if (block.charAt(i) != '"') break;
            int keyEnd = block.indexOf('"', i + 1);
            String key = block.substring(i + 1, keyEnd);
            i = keyEnd + 1;

            // 跳过 :
            while (i < block.length() && (block.charAt(i) == ' ' || block.charAt(i) == ':')) i++;

            // 读 value
            if (i >= block.length()) break;
            if (block.charAt(i) == '"')
            {
                int valEnd = block.indexOf('"', i + 1);
                msg.put(key, block.substring(i + 1, valEnd));
                i = valEnd + 1;
            }
            else if (block.charAt(i) == '{')
            {
                // 嵌套对象 — 跳过（本协议中不出现）
                int depth = 1;
                i++;
                while (i < block.length() && depth > 0)
                {
                    if (block.charAt(i) == '{') depth++;
                    else if (block.charAt(i) == '}') depth--;
                    i++;
                }
            }
            else
            {
                // 数字或布尔
                int start = i;
                while (i < block.length() && block.charAt(i) != ',' && block.charAt(i) != '}' && block.charAt(i) != ' ')
                    i++;
                String val = block.substring(start, i).trim();
                try { msg.put(key, Integer.parseInt(val)); }
                catch (NumberFormatException e1)
                {
                    try { msg.put(key, Double.parseDouble(val)); }
                    catch (NumberFormatException e2)
                    {
                        msg.put(key, "true".equals(val) || "false".equals(val) ? Boolean.parseBoolean(val) : val);
                    }
                }
            }
        }
    }

    private static String escape(String s)
    {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
javac -encoding UTF-8 -d out src/server/util/MessageCodec.java src/shared/Message.java src/shared/MessageType.java
```

Expected: 编译成功

- [ ] **Step 3: Commit**

```bash
git add src/server/util/MessageCodec.java
git commit -m "feat: add MessageCodec for manual JSON encode/decode"
```

---

## Phase 3: 服务端引擎层（纯逻辑，可单测）

### Task 5: 创建 GameState 游戏状态数据类

**Files:**
- Create: `src/server/engine/GameState.java`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p src/server/engine src/server/room src/server/session src/server/transport
```

- [ ] **Step 2: 写入 GameState.java**

```java
package server.engine;

import java.util.*;

/**
 * 功能描述：服务端权威游戏状态快照 —— 包含棋盘、玩家、回合等所有可变数据
 * 这是一个纯数据类，不包含任何 UI 逻辑，供 MonopolyEngine 读写
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class GameState
{
    // === 玩家状态快照 ===
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
        public Map<String, Integer> props;      // 道具名 → 数量
        public List<Integer> ownedLandIndices;  // 拥有的地产 tile 索引

        public PlayerSnapshot() { props = new HashMap<>(); ownedLandIndices = new ArrayList<>(); }
    }

    // === 棋盘状态 ===
    public int[] landOwners;        // tile 索引 → 玩家索引（-1 = 无主）
    public int[] landLevels;        // tile 索引 → 等级
    public boolean[] hasBarrier;    // tile 索引 → 是否有路障
    public int[] barrierRounds;     // tile 索引 → 路障剩余回合
    public boolean[] hasMine;       // tile 索引 → 是否有地雷

    // === 游戏流程 ===
    public int currentPlayerIndex;  // 当前回合的玩家索引 (0 或 1)
    public int round;
    public String phase;            // "waiting" | "rolling" | "walking" | "choosing" | "finished"
    public String winner;           // null | player name

    public List<PlayerSnapshot> players;
    public int tileCount;           // 棋盘总格数

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

    public PlayerSnapshot getCurrentPlayer()
    {
        return players.get(currentPlayerIndex);
    }

    public PlayerSnapshot getPlayerByName(String name)
    {
        return players.stream().filter(p -> p.name.equals(name)).findFirst().orElse(null);
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
javac -encoding UTF-8 -d out src/server/engine/GameState.java
```

Expected: 编译成功

- [ ] **Step 4: Commit**

```bash
git add src/server/engine/GameState.java
git commit -m "feat: add server GameState data class"
```

### Task 6: 创建 GameEngine 接口

**Files:**
- Create: `src/server/engine/GameEngine.java`

- [ ] **Step 1: 写入 GameEngine.java**

```java
package server.engine;

import shared.Message;
import java.util.List;

/**
 * 功能描述：游戏引擎接口 —— 纯函数式，输入游戏动作消息 → 输出响应事件列表
 * 不依赖网络、UI 或任何外部状态，可独立单元测试
 * @author cyt & Claude
 * @date 2026/6/2
 */
public interface GameEngine
{
    /**
     * 处理玩家动作，返回需要广播的事件列表
     * @param state  当前游戏状态（会被原地修改）
     * @param playerIndex 发起动作的玩家索引 (0 或 1)
     * @param action 客户端发来的动作消息
     * @return 响应事件列表（每个事件有目标玩家索引，-1 表示广播给房间内所有人）
     */
    List<EngineEvent> processAction(GameState state, int playerIndex, Message action);

    /**
     * 创建初始游戏状态
     */
    GameState createInitialState(String player0Name, String player0SessionId,
                                  String player1Name, String player1SessionId);
}
```

- [ ] **Step 2: 创建 EngineEvent 类**

同文件追加，或创建独立文件 `src/server/engine/EngineEvent.java`：

```java
package server.engine;

import shared.Message;

/**
 * 功能描述：引擎处理后的事件 —— 包含目标玩家索引和响应消息
 * targetPlayerIndex = -1 表示广播给房间内所有玩家
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

    public static EngineEvent broadcast(Message msg) { return new EngineEvent(-1, msg); }
    public static EngineEvent toPlayer(int idx, Message msg) { return new EngineEvent(idx, msg); }
}
```

- [ ] **Step 3: 编译验证**

```bash
javac -encoding UTF-8 -d out \
  src/server/engine/GameEngine.java \
  src/server/engine/EngineEvent.java \
  src/server/engine/GameState.java \
  src/shared/Message.java \
  src/shared/MessageType.java
```

Expected: 编译成功

- [ ] **Step 4: Commit**

```bash
git add src/server/engine/GameEngine.java src/server/engine/EngineEvent.java
git commit -m "feat: add GameEngine interface and EngineEvent class"
```

### Task 7: 创建 MonopolyEngine 核心规则引擎

**Files:**
- Create: `src/server/engine/MonopolyEngine.java`

这是最核心的文件，将 MainMap 的游戏逻辑迁移到服务端。需要实现完整的掷骰、行走、地块触发、道具使用、买地升级等逻辑。

- [ ] **Step 1: 写入 MonopolyEngine.java（第一部分：框架 + 掷骰）**

```java
package server.engine;

import shared.Message;
import shared.MessageType;

import java.util.*;

/**
 * 功能描述：大富翁规则引擎 —— 服务端权威实现，处理所有游戏逻辑
 * 迁移自 MainMap.java 的核心逻辑，去除所有 UI 依赖
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class MonopolyEngine implements GameEngine
{
    // === 棋盘常量（与 BoardConfig 保持一致） ===
    private static final int TILE_COUNT = 30;
    private static final int[] TILE_TAX = new int[TILE_COUNT];     // 每格基础税率
    private static final int[] TILE_PRICE = new int[TILE_COUNT];   // 每格购买价格
    private static final int[] TILE_MAX_LEVEL = new int[TILE_COUNT];
    private static final boolean[] TILE_IS_LAND = new boolean[TILE_COUNT];  // 是否地产格

    static
    {
        // 初始化棋盘数据（与服务端 BoardConfig 保持一致）
        for (int i = 0; i < TILE_COUNT; i++)
        {
            TILE_MAX_LEVEL[i] = 3;
            TILE_IS_LAND[i] = true;
            TILE_PRICE[i] = 500 + (i % 10) * 100;
            TILE_TAX[i] = 200 + (i % 10) * 80;
        }
        // 特殊格（非地产）
        int[] specialTiles = {0, 6, 12, 18, 24}; // Start, Chance, Event, Casino, Park 等
        for (int idx : specialTiles) TILE_IS_LAND[idx] = false;
    }

    private static final int START_MONEY = 2000;
    private static final int INITIAL_MONEY = 10000;
    private static final int INITIAL_HP = 100;
    private static final Random RNG = new Random();

    // 步骤大小常量
    private static final int DICE_RESULT = 0;
    private static final int WALK_STAGE = 1;
    private static final int TILE_RESOLVE = 2;

    @Override
    public GameState createInitialState(String player0Name, String player0SessionId,
                                         String player1Name, String player1SessionId)
    {
        GameState state = new GameState(TILE_COUNT);

        GameState.PlayerSnapshot p0 = new GameState.PlayerSnapshot();
        p0.name = player0Name;
        p0.sessionId = player0SessionId;
        p0.money = INITIAL_MONEY;
        p0.hp = INITIAL_HP;
        p0.positionIndex = 0;
        p0.isOnline = true;

        GameState.PlayerSnapshot p1 = new GameState.PlayerSnapshot();
        p1.name = player1Name;
        p1.sessionId = player1SessionId;
        p1.money = INITIAL_MONEY;
        p1.hp = INITIAL_HP;
        p1.positionIndex = 0;
        p1.isOnline = true;

        state.players.add(p0);
        state.players.add(p1);
        state.currentPlayerIndex = 0;
        state.phase = "rolling";
        return state;
    }

    @Override
    public List<EngineEvent> processAction(GameState state, int playerIndex, Message action)
    {
        List<EngineEvent> events = new ArrayList<>();
        String actionName = action.getType().name();

        switch (action.getType())
        {
            case ROLL_DICE:
                return processRollDice(state, playerIndex);
            case BUY_LAND:
                return processBuyLand(state, playerIndex, action.get("choice", false));
            case UPGRADE:
                return processUpgrade(state, playerIndex, action.get("choice", false));
            case USE_PROP:
                return processUseProp(state, playerIndex, action);
            case LEAVE_ROOM:
                return processLeave(state, playerIndex);
            default:
                events.add(EngineEvent.toPlayer(playerIndex,
                    new Message(MessageType.ERROR).put("msg", "未知动作: " + actionName)));
        }
        return events;
    }

    // ===== 掷骰逻辑 =====

    private List<EngineEvent> processRollDice(GameState state, int playerIndex)
    {
        List<EngineEvent> events = new ArrayList<>();
        GameState.PlayerSnapshot player = state.getCurrentPlayer();

        if (state.players.indexOf(player) != playerIndex) {
            events.add(EngineEvent.toPlayer(playerIndex,
                new Message(MessageType.ERROR).put("msg", "不是你的回合")));
            return events;
        }

        int diceValue = RNG.nextInt(6) + 1;
        state.round++;

        // 计算行走路径
        List<Integer> steps = new ArrayList<>();
        int pos = player.positionIndex;
        for (int i = 0; i < diceValue; i++)
        {
            pos = (pos + 1) % TILE_COUNT;
            steps.add(pos);
            if (pos == 0)
                player.money += START_MONEY;
        }
        player.positionIndex = pos;
        state.phase = "choosing";

        // 广播骰子结果
        events.add(EngineEvent.broadcast(
            new Message(MessageType.DICE_RESULT)
                .put("value", diceValue)
                .put("player", player.name)));

        // 广播行走动画
        events.add(EngineEvent.broadcast(
            new Message(MessageType.WALK_ANIM)
                .put("player", player.name)
                .put("steps", steps)));

        // 到达格子逻辑
        events.addAll(resolveTile(state, player, playerIndex));

        // 检查胜负
        events.addAll(checkGameOver(state));

        // 回合切换
        if (state.phase.equals("choosing") && events.stream().noneMatch(e -> e.message.getType() == MessageType.GAME_OVER))
        {
            state.currentPlayerIndex = (state.currentPlayerIndex + 1) % 2;
            // 跳过被路障停止的玩家
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
            state.phase = "rolling";
            events.add(EngineEvent.broadcast(
                new Message(MessageType.TURN_NOTIFY)
                    .put("player", state.getCurrentPlayer().name)));
        }

        return events;
    }

    // ===== 地块到达逻辑 =====

    private List<EngineEvent> resolveTile(GameState state, GameState.PlayerSnapshot player, int playerIndex)
    {
        List<EngineEvent> events = new ArrayList<>();
        int tileIdx = player.positionIndex;

        // 检查地雷
        if (tileIdx >= 0 && tileIdx < TILE_COUNT && state.hasMine[tileIdx])
        {
            state.hasMine[tileIdx] = false;
            player.hp -= 40;
            events.add(EngineEvent.broadcast(
                new Message(MessageType.GAME_UPDATE)
                    .put("event", "mine_explode")
                    .put("player", player.name)
                    .put("tileIdx", tileIdx)
                    .put("hpChange", -40)));
        }

        // 检查路障
        if (tileIdx >= 0 && tileIdx < TILE_COUNT && state.hasBarrier[tileIdx])
        {
            state.hasBarrier[tileIdx] = false;
            player.barrierStopTurns = 1;
            events.add(EngineEvent.broadcast(
                new Message(MessageType.GAME_UPDATE)
                    .put("event", "barrier_hit")
                    .put("player", player.name)
                    .put("tileIdx", tileIdx)));
            return events;
        }

        // 地产格逻辑
        if (tileIdx >= 0 && tileIdx < TILE_COUNT && TILE_IS_LAND[tileIdx])
        {
            int ownerIdx = state.landOwners[tileIdx];

            if (ownerIdx < 0)
            {
                // 无主地产 → 询问买地
                events.add(EngineEvent.toPlayer(playerIndex,
                    new Message(MessageType.ASK_CHOICE)
                        .put("question", "buy_land")
                        .put("tileIdx", tileIdx)
                        .put("price", TILE_PRICE[tileIdx])));
            }
            else if (ownerIdx != playerIndex)
            {
                // 别人的地产 → 交税
                int tax = TILE_TAX[tileIdx] * (state.landLevels[tileIdx] + 1);
                player.money -= tax;
                state.players.get(ownerIdx).money += tax;
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "pay_tax")
                        .put("player", player.name)
                        .put("owner", state.players.get(ownerIdx).name)
                        .put("tileIdx", tileIdx)
                        .put("amount", tax)));
            }
            else if (state.landLevels[tileIdx] < TILE_MAX_LEVEL[tileIdx])
            {
                // 自己的地产 → 询问升级
                int upgradePrice = TILE_PRICE[tileIdx] * (state.landLevels[tileIdx] + 1);
                events.add(EngineEvent.toPlayer(playerIndex,
                    new Message(MessageType.ASK_CHOICE)
                        .put("question", "upgrade")
                        .put("tileIdx", tileIdx)
                        .put("price", upgradePrice)
                        .put("currentLevel", state.landLevels[tileIdx])));
            }
        }

        return events;
    }

    // ===== 买地逻辑 =====

    private List<EngineEvent> processBuyLand(GameState state, int playerIndex, boolean choice)
    {
        List<EngineEvent> events = new ArrayList<>();
        GameState.PlayerSnapshot player = state.players.get(playerIndex);
        int tileIdx = player.positionIndex;

        if (choice && tileIdx >= 0 && tileIdx < TILE_COUNT && state.landOwners[tileIdx] < 0)
        {
            int price = TILE_PRICE[tileIdx];
            if (player.money >= price)
            {
                player.money -= price;
                state.landOwners[tileIdx] = playerIndex;
                state.landLevels[tileIdx] = 1;
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "land_bought")
                        .put("player", player.name)
                        .put("tileIdx", tileIdx)
                        .put("price", price)));
            }
        }
        return events;
    }

    // ===== 升级逻辑 =====

    private List<EngineEvent> processUpgrade(GameState state, int playerIndex, boolean choice)
    {
        List<EngineEvent> events = new ArrayList<>();
        GameState.PlayerSnapshot player = state.players.get(playerIndex);
        int tileIdx = player.positionIndex;

        if (choice && tileIdx >= 0 && tileIdx < TILE_COUNT
            && state.landOwners[tileIdx] == playerIndex
            && state.landLevels[tileIdx] < TILE_MAX_LEVEL[tileIdx])
        {
            int price = TILE_PRICE[tileIdx] * (state.landLevels[tileIdx] + 1);
            if (player.money >= price)
            {
                player.money -= price;
                state.landLevels[tileIdx]++;
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "land_upgraded")
                        .put("player", player.name)
                        .put("tileIdx", tileIdx)
                        .put("newLevel", state.landLevels[tileIdx])
                        .put("price", price)));
            }
        }
        return events;
    }

    // ===== 道具使用（精简版） =====

    private List<EngineEvent> processUseProp(GameState state, int playerIndex, Message action)
    {
        List<EngineEvent> events = new ArrayList<>();
        GameState.PlayerSnapshot player = state.players.get(playerIndex);
        String propName = action.get("propName", "");

        Map<String, Integer> props = player.props;
        Integer count = props.get(propName);
        if (count == null || count <= 0) return events;

        switch (propName)
        {
            case "地雷":
                int mineIdx = player.positionIndex;
                if (mineIdx >= 0 && mineIdx < TILE_COUNT && !state.hasMine[mineIdx])
                {
                    state.hasMine[mineIdx] = true;
                    decProp(props, propName);
                    events.add(EngineEvent.broadcast(
                        new Message(MessageType.GAME_UPDATE)
                            .put("event", "mine_placed")
                            .put("tileIdx", mineIdx)
                            .put("player", player.name)));
                }
                break;

            case "路障":
                int barrierIdx = action.get("targetIdx", -1);
                if (barrierIdx >= 0 && barrierIdx < TILE_COUNT && !state.hasBarrier[barrierIdx])
                {
                    state.hasBarrier[barrierIdx] = true;
                    state.barrierRounds[barrierIdx] = 3;
                    decProp(props, propName);
                    events.add(EngineEvent.broadcast(
                        new Message(MessageType.GAME_UPDATE)
                            .put("event", "barrier_placed")
                            .put("tileIdx", barrierIdx)
                            .put("player", player.name)));
                }
                break;

            case "包子":
                player.hp = Math.min(100, player.hp + 40);
                decProp(props, propName);
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_UPDATE)
                        .put("event", "prop_used")
                        .put("player", player.name)
                        .put("prop", "包子")
                        .put("hpChange", 40)));
                break;
        }
        return events;
    }

    // ===== 离开 =====

    private List<EngineEvent> processLeave(GameState state, int playerIndex)
    {
        List<EngineEvent> events = new ArrayList<>();
        state.players.get(playerIndex).isOnline = false;
        int otherIdx = (playerIndex + 1) % 2;
        state.winner = state.players.get(otherIdx).name;
        state.phase = "finished";
        events.add(EngineEvent.broadcast(
            new Message(MessageType.PLAYER_LEFT)
                .put("player", state.players.get(playerIndex).name)));
        events.add(EngineEvent.broadcast(
            new Message(MessageType.GAME_OVER)
                .put("winner", state.winner)));
        return events;
    }

    // ===== 胜负判定 =====

    private List<EngineEvent> checkGameOver(GameState state)
    {
        List<EngineEvent> events = new ArrayList<>();
        for (GameState.PlayerSnapshot p : state.players)
        {
            if (p.money < 0 || p.hp <= 0)
            {
                String winner = state.players.stream()
                    .filter(pp -> pp != p).findFirst().get().name;
                state.winner = winner;
                state.phase = "finished";
                events.add(EngineEvent.broadcast(
                    new Message(MessageType.GAME_OVER).put("winner", winner)));
                break;
            }
        }
        return events;
    }

    // ===== 工具方法 =====

    private void decProp(Map<String, Integer> props, String name)
    {
        Integer c = props.get(name);
        if (c != null)
        {
            if (c == 1) props.remove(name);
            else props.put(name, c - 1);
        }
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
javac -encoding UTF-8 -d out \
  src/server/engine/MonopolyEngine.java \
  src/server/engine/GameEngine.java \
  src/server/engine/EngineEvent.java \
  src/server/engine/GameState.java \
  src/shared/Message.java \
  src/shared/MessageType.java
```

Expected: 编译成功

- [ ] **Step 3: Commit**

```bash
git add src/server/engine/MonopolyEngine.java
git commit -m "feat: add MonopolyEngine - authoritative server-side game logic"
```

---

## Phase 4: 服务端网络层

### Task 8: 创建 PlayerSession 和 SessionManager

**Files:**
- Create: `src/server/session/PlayerSession.java`
- Create: `src/server/session/SessionManager.java`

- [ ] **Step 1: 写入 PlayerSession.java**

```java
package server.session;

import org.java_websocket.WebSocket;

/**
 * 功能描述：封装一个 WebSocket 连接，包含玩家标识和房间归属
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class PlayerSession
{
    public final WebSocket socket;
    public String playerName;
    public String roomId;          // null = 未加入房间
    public boolean isHost;         // 是否为房间创建者

    public PlayerSession(WebSocket socket)
    {
        this.socket = socket;
    }

    public String getId()
    {
        return socket.getRemoteSocketAddress().toString();
    }
}
```

- [ ] **Step 2: 写入 SessionManager.java**

```java
package server.session;

import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述：管理所有 WebSocket 连接的生命周期
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class SessionManager
{
    private final Map<WebSocket, PlayerSession> sessions = new ConcurrentHashMap<>();

    public PlayerSession register(WebSocket socket)
    {
        PlayerSession session = new PlayerSession(socket);
        sessions.put(socket, session);
        return session;
    }

    public void unregister(WebSocket socket)
    {
        sessions.remove(socket);
    }

    public PlayerSession get(WebSocket socket)
    {
        return sessions.get(socket);
    }

    public int getCount()
    {
        return sessions.size();
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
javac -encoding UTF-8 -cp "lib/java-websocket-1.5.7.jar" -d out \
  src/server/session/PlayerSession.java \
  src/server/session/SessionManager.java
```

Expected: 编译成功

- [ ] **Step 4: Commit**

```bash
git add src/server/session/PlayerSession.java src/server/session/SessionManager.java
git commit -m "feat: add server session management layer"
```

### Task 9: 创建 Room 接口、RoomManager、MonopolyRoom

**Files:**
- Create: `src/server/room/Room.java`
- Create: `src/server/room/RoomManager.java`
- Create: `src/server/room/MonopolyRoom.java`

- [ ] **Step 1: 写入 Room.java**

```java
package server.room;

import server.engine.GameState;
import server.session.PlayerSession;
import shared.Message;

/**
 * 功能描述：房间接口 —— 定义房间生命周期，不绑定具体游戏类型
 * @author cyt & Claude
 * @date 2026/6/2
 */
public interface Room
{
    String getId();
    PlayerSession getHost();
    PlayerSession getGuest();
    boolean isFull();
    void onPlayerJoin(PlayerSession session);
    void onPlayerLeave(PlayerSession session);
    void onMessage(PlayerSession session, Message msg);
    GameState getState();
    int getPlayerIndex(PlayerSession session);
}
```

- [ ] **Step 2: 写入 RoomManager.java**

```java
package server.room;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述：房间池管理器 —— 创建/查找/销毁房间
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class RoomManager
{
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Random rng = new Random();

    /** 创建房间，返回 4 位房间号 */
    public String createRoom()
    {
        String roomId;
        do {
            roomId = String.format("%04d", rng.nextInt(10000));
        } while (rooms.containsKey(roomId));

        Room room = new MonopolyRoom(roomId);
        rooms.put(roomId, room);
        return roomId;
    }

    public Room getRoom(String roomId)
    {
        return rooms.get(roomId);
    }

    public void removeRoom(String roomId)
    {
        rooms.remove(roomId);
    }

    public int getRoomCount()
    {
        return rooms.size();
    }
}
```

- [ ] **Step 3: 写入 MonopolyRoom.java**

```java
package server.room;

import server.engine.*;
import server.session.PlayerSession;
import shared.Message;
import shared.MessageType;
import server.util.MessageCodec;

import java.util.List;

/**
 * 功能描述：大富翁房间实现 —— 绑定 MonopolyEngine 处理游戏逻辑
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class MonopolyRoom implements Room
{
    private final String id;
    private PlayerSession host;
    private PlayerSession guest;
    private GameState state;
    private final MonopolyEngine engine;

    public MonopolyRoom(String id)
    {
        this.id = id;
        this.engine = new MonopolyEngine();
    }

    @Override public String getId() { return id; }
    @Override public PlayerSession getHost() { return host; }
    @Override public PlayerSession getGuest() { return guest; }
    @Override public boolean isFull() { return host != null && guest != null; }

    @Override
    public void onPlayerJoin(PlayerSession session)
    {
        if (host == null)
        {
            host = session;
            session.isHost = true;
            session.roomId = id;
            session.playerName = "naiLong";
            sendTo(session, new Message(MessageType.ROOM_CREATED).put("roomId", id));
        }
        else if (guest == null)
        {
            guest = session;
            session.roomId = id;
            session.playerName = "xiaoMei";
            sendTo(host, new Message(MessageType.PLAYER_JOINED).put("playerName", guest.playerName));
            sendTo(guest, new Message(MessageType.ROOM_CREATED).put("roomId", id));
        }
    }

    @Override
    public void onPlayerLeave(PlayerSession session)
    {
        if (state != null && state.phase != "finished")
        {
            int idx = getPlayerIndex(session);
            engine.processAction(state, idx, new Message(MessageType.LEAVE_ROOM));
        }
    }

    @Override
    public void onMessage(PlayerSession session, Message msg)
    {
        MessageType type = msg.getType();

        // 房间管理消息
        if (type == MessageType.READY)
        {
            if (isFull() && state == null)
            {
                state = engine.createInitialState(
                    host.playerName, host.getId(),
                    guest.playerName, guest.getId());
                broadcast(new Message(MessageType.GAME_START)
                    .put("firstPlayer", state.getCurrentPlayer().name));
                broadcast(new Message(MessageType.TURN_NOTIFY)
                    .put("player", state.getCurrentPlayer().name));
            }
            return;
        }

        if (type == MessageType.LEAVE_ROOM)
        {
            onPlayerLeave(session);
            return;
        }

        // 游戏动作消息 → 转发引擎
        if (state == null) return;
        int playerIdx = getPlayerIndex(session);
        List<EngineEvent> events = engine.processAction(state, playerIdx, msg);

        for (EngineEvent event : events)
        {
            if (event.targetPlayerIndex < 0)
                broadcast(event.message);
            else
                sendToPlayer(event.targetPlayerIndex, event.message);
        }
    }

    @Override public GameState getState() { return state; }

    @Override
    public int getPlayerIndex(PlayerSession session)
    {
        if (session == host) return 0;
        if (session == guest) return 1;
        return -1;
    }

    private void sendTo(PlayerSession session, Message msg)
    {
        if (session != null && session.socket.isOpen())
            session.socket.send(MessageCodec.encode(msg));
    }

    private void sendToPlayer(int idx, Message msg)
    {
        if (idx == 0) sendTo(host, msg);
        else if (idx == 1) sendTo(guest, msg);
    }

    private void broadcast(Message msg)
    {
        sendTo(host, msg);
        sendTo(guest, msg);
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
javac -encoding UTF-8 -cp "lib/java-websocket-1.5.7.jar" -d out \
  src/server/room/Room.java \
  src/server/room/RoomManager.java \
  src/server/room/MonopolyRoom.java \
  src/server/engine/MonopolyEngine.java \
  src/server/engine/GameEngine.java \
  src/server/engine/EngineEvent.java \
  src/server/engine/GameState.java \
  src/server/session/PlayerSession.java \
  src/server/util/MessageCodec.java \
  src/shared/Message.java \
  src/shared/MessageType.java
```

Expected: 编译成功

- [ ] **Step 5: Commit**

```bash
git add src/server/room/Room.java src/server/room/RoomManager.java src/server/room/MonopolyRoom.java
git commit -m "feat: add Room abstraction, RoomManager, and MonopolyRoom"
```

### Task 10: 创建 WebSocketEndpoint 和 MonopolyServer 入口

**Files:**
- Create: `src/server/transport/WebSocketEndpoint.java`
- Create: `src/server/MonopolyServer.java`

- [ ] **Step 1: 写入 WebSocketEndpoint.java**

```java
package server.transport;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import server.room.RoomManager;
import server.session.PlayerSession;
import server.session.SessionManager;
import server.util.MessageCodec;
import shared.Message;
import shared.MessageType;
import server.room.Room;

import java.net.InetSocketAddress;

/**
 * 功能描述：WebSocket 端点 —— 接收客户端连接，路由消息到对应房间
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class WebSocketEndpoint extends WebSocketServer
{
    private final SessionManager sessionManager;
    private final RoomManager roomManager;

    public WebSocketEndpoint(int port, SessionManager sessionManager, RoomManager roomManager)
    {
        super(new InetSocketAddress(port));
        this.sessionManager = sessionManager;
        this.roomManager = roomManager;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        PlayerSession session = sessionManager.register(conn);
        System.out.println("[连接] 新客户端: " + session.getId());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        PlayerSession session = sessionManager.get(conn);
        if (session != null && session.roomId != null)
        {
            Room room = roomManager.getRoom(session.roomId);
            if (room != null)
                room.onPlayerLeave(session);
        }
        sessionManager.unregister(conn);
        System.out.println("[断开] 客户端: " + (session != null ? session.getId() : conn));
    }

    @Override
    public void onMessage(WebSocket conn, String json)
    {
        PlayerSession session = sessionManager.get(conn);
        if (session == null) return;

        Message msg = MessageCodec.decode(json);
        System.out.println("[消息] " + session.getId() + " → " + msg.getType());

        MessageType type = msg.getType();

        // 房间管理消息（无需已加入房间）
        if (type == MessageType.CREATE_ROOM)
        {
            String roomId = roomManager.createRoom();
            Room room = roomManager.getRoom(roomId);
            room.onPlayerJoin(session);
            return;
        }

        if (type == MessageType.JOIN_ROOM)
        {
            String roomId = msg.get("roomId", "");
            Room room = roomManager.getRoom(roomId);
            if (room == null)
            {
                session.socket.send(MessageCodec.encode(
                    new Message(MessageType.ERROR).put("msg", "房间 " + roomId + " 不存在")));
                return;
            }
            if (room.isFull())
            {
                session.socket.send(MessageCodec.encode(
                    new Message(MessageType.ERROR).put("msg", "房间已满")));
                return;
            }
            room.onPlayerJoin(session);
            return;
        }

        // 游戏消息 → 转发到房间
        if (session.roomId != null)
        {
            Room room = roomManager.getRoom(session.roomId);
            if (room != null)
                room.onMessage(session, msg);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        System.err.println("[错误] " + (conn != null ? conn.getRemoteSocketAddress() : "null") + ": " + ex.getMessage());
    }

    @Override
    public void onStart()
    {
        System.out.println("[服务] MonopolyServer 已启动，监听端口 " + getPort());
    }
}
```

- [ ] **Step 2: 写入 MonopolyServer.java**

```java
package server;

import server.room.RoomManager;
import server.session.SessionManager;
import server.transport.WebSocketEndpoint;

/**
 * 功能描述：大富翁联机服务端入口 —— 启动 WebSocket 服务器
 * 用法：java -cp "monopoly-server.jar;lib/java-websocket-1.5.7.jar" server.MonopolyServer [port]
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class MonopolyServer
{
    public static void main(String[] args)
    {
        int port = 8080;
        if (args.length > 0)
        {
            try { port = Integer.parseInt(args[0]); }
            catch (NumberFormatException e)
            {
                System.err.println("端口格式错误，使用默认 8080");
            }
        }

        SessionManager sessionManager = new SessionManager();
        RoomManager roomManager = new RoomManager();

        WebSocketEndpoint server = new WebSocketEndpoint(port, sessionManager, roomManager);

        // 定时清理空闲房间
        Thread cleanupThread = new Thread(() -> {
            while (true)
            {
                try { Thread.sleep(300_000); } catch (InterruptedException e) { break; }
                // 清理逻辑可在后续迭代中添加
                System.out.println("[清理] 当前活跃房间数: " + roomManager.getRoomCount());
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();

        server.start();
        System.out.println("大富翁联机服务器启动成功！");
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
javac -encoding UTF-8 -cp "lib/java-websocket-1.5.7.jar" -d out \
  src/server/MonopolyServer.java \
  src/server/transport/WebSocketEndpoint.java \
  src/server/room/Room.java \
  src/server/room/RoomManager.java \
  src/server/room/MonopolyRoom.java \
  src/server/engine/MonopolyEngine.java \
  src/server/engine/GameEngine.java \
  src/server/engine/EngineEvent.java \
  src/server/engine/GameState.java \
  src/server/session/PlayerSession.java \
  src/server/session/SessionManager.java \
  src/server/util/MessageCodec.java \
  src/shared/Message.java \
  src/shared/MessageType.java
```

Expected: 编译成功

- [ ] **Step 4: Commit**

```bash
git add src/server/transport/WebSocketEndpoint.java src/server/MonopolyServer.java
git commit -m "feat: add WebSocket endpoint and MonopolyServer entry point"
```

---

## Phase 5: 客户端网络层

### Task 11: 创建 NetworkClient

**Files:**
- Create: `src/main/NetworkClient.java`

- [ ] **Step 1: 写入 NetworkClient.java**

```java
package main;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import server.util.MessageCodec;
import shared.Message;
import shared.MessageType;

import java.net.URI;
import java.util.function.Consumer;

/**
 * 功能描述：WebSocket 客户端封装 —— 连接服务端、收发消息、断线重连
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class NetworkClient
{
    private WebSocketClient client;
    private final String serverUri;
    private Consumer<Message> onMessage;
    private Runnable onDisconnect;
    private boolean connected = false;

    public NetworkClient(String serverUri)
    {
        this.serverUri = serverUri;
    }

    public void setOnMessage(Consumer<Message> handler) { this.onMessage = handler; }
    public void setOnDisconnect(Runnable handler) { this.onDisconnect = handler; }
    public boolean isConnected() { return connected; }

    public void connect()
    {
        try
        {
            client = new WebSocketClient(new URI(serverUri))
            {
                @Override
                public void onOpen(ServerHandshake handshake)
                {
                    connected = true;
                    System.out.println("[NetworkClient] 已连接到 " + serverUri);
                }

                @Override
                public void onMessage(String json)
                {
                    Message msg = MessageCodec.decode(json);
                    System.out.println("[NetworkClient] 收到: " + msg.getType());
                    if (NetworkClient.this.onMessage != null)
                        NetworkClient.this.onMessage.accept(msg);
                }

                @Override
                public void onClose(int code, String reason, boolean remote)
                {
                    connected = false;
                    System.out.println("[NetworkClient] 连接关闭: " + reason);
                    if (NetworkClient.this.onDisconnect != null)
                        NetworkClient.this.onDisconnect.run();
                }

                @Override
                public void onError(Exception ex)
                {
                    System.err.println("[NetworkClient] 错误: " + ex.getMessage());
                }
            };
            client.connect();
        }
        catch (Exception e)
        {
            System.err.println("[NetworkClient] 连接失败: " + e.getMessage());
        }
    }

    public void send(Message msg)
    {
        if (client != null && client.isOpen())
        {
            String json = MessageCodec.encode(msg);
            System.out.println("[NetworkClient] 发送: " + msg.getType());
            client.send(json);
        }
    }

    public void disconnect()
    {
        if (client != null)
        {
            client.close();
            connected = false;
        }
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
javac -encoding UTF-8 -cp "lib/java-websocket-1.5.7.jar" -d out \
  src/main/NetworkClient.java \
  src/server/util/MessageCodec.java \
  src/shared/Message.java \
  src/shared/MessageType.java
```

Expected: 编译成功

- [ ] **Step 3: Commit**

```bash
git add src/main/NetworkClient.java
git commit -m "feat: add NetworkClient WebSocket wrapper"
```

---

## Phase 6: 客户端 GameController 抽象

### Task 12: 创建 GameMode 枚举和 GameController 接口

**Files:**
- Create: `src/main/GameMode.java`
- Create: `src/main/GameController.java`

- [ ] **Step 1: 写入 GameMode.java**

```java
package main;

/**
 * 功能描述：游戏模式枚举
 * @author cyt & Claude
 * @date 2026/6/2
 */
public enum GameMode
{
    LOCAL_PVP,   // 本地双人对战
    LOCAL_PVE,   // 本地人机对战
    ONLINE       // 联机对战
}
```

- [ ] **Step 2: 写入 GameController.java**

```java
package main;

/**
 * 功能描述：游戏控制器接口 —— 定义玩家操作的统一入口，三种模式（本地双人/人机/联机）各自实现
 * @author cyt & Claude
 * @date 2026/6/2
 */
public interface GameController
{
    /** 玩家点击骰子 */
    void onDiceClicked();

    /** 玩家点击道具 */
    void onPropClicked(String propName);

    /** 买地确认/取消 */
    void onLandChoice(boolean yes);

    /** 升级确认/取消 */
    void onUpgradeChoice(boolean yes);

    /** 当前控制器对应的模式 */
    GameMode getMode();

    /** 是否轮到本地玩家操作（联机模式下，对手回合时返回 false） */
    boolean isMyTurn();

    /** 获取本地玩家索引 */
    int getLocalPlayerIndex();
}
```

- [ ] **Step 3: 编译验证**

```bash
javac -encoding UTF-8 -d out src/main/GameMode.java src/main/GameController.java
```

Expected: 编译成功

- [ ] **Step 4: Commit**

```bash
git add src/main/GameMode.java src/main/GameController.java
git commit -m "feat: add GameMode enum and GameController interface"
```

### Task 13: 创建 LocalController（迁移现有双人逻辑）

**Files:**
- Create: `src/main/LocalController.java`

- [ ] **Step 1: 写入 LocalController.java**

```java
package main;

/**
 * 功能描述：本地双人对战控制器 —— 所有操作直接由 MainMap 本地执行，不做网络通信
 * isMyTurn() 始终返回 true（本地模式两个玩家都在同一台机器上操作）
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class LocalController implements GameController
{
    private final MainMap mainMap;

    public LocalController(MainMap mainMap)
    {
        this.mainMap = mainMap;
    }

    @Override
    public void onDiceClicked()
    {
        // 本地模式：直接由 DiceController 处理，此方法不会被调用
        // 保留用于接口完整性
    }

    @Override
    public void onPropClicked(String propName)
    {
        // 本地模式：直接由 MainMap.addPropsListener 处理
    }

    @Override
    public void onLandChoice(boolean yes)
    {
        // 本地模式：直接由 Land.onPlayerArrive 内 JOptionPane 处理
    }

    @Override
    public void onUpgradeChoice(boolean yes)
    {
        // 本地模式：直接由 Land.onPlayerArrive 内 JOptionPane 处理
    }

    @Override
    public GameMode getMode() { return GameMode.LOCAL_PVP; }

    @Override
    public boolean isMyTurn() { return true; }

    @Override
    public int getLocalPlayerIndex() { return mainMap.getCurrentPlayerIndex(); }
}
```

- [ ] **Step 2: 编译验证**

```bash
javac -encoding UTF-8 -d out \
  src/main/LocalController.java \
  src/main/GameController.java \
  src/main/GameMode.java \
  src/main/MainMap.java 2>&1 | tail -5
```

Expected: 编译成功（可能有一些 MainMap 自身的警告，忽略）

- [ ] **Step 3: Commit**

```bash
git add src/main/LocalController.java
git commit -m "feat: add LocalController for local PvP mode"
```

### Task 14: 创建 RemoteController（联机模式）

**Files:**
- Create: `src/main/RemoteController.java`

- [ ] **Step 1: 写入 RemoteController.java**

```java
package main;

import shared.Message;
import shared.MessageType;

/**
 * 功能描述：联机对战控制器 —— 所有玩家操作转为 JSON 消息发送到服务端，
 * 服务端返回的事件驱动 MainMap 更新 UI
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

    @Override
    public GameMode getMode() { return GameMode.ONLINE; }

    @Override
    public boolean isMyTurn() { return myTurn; }

    @Override
    public int getLocalPlayerIndex() { return localPlayerIndex; }

    public NetworkClient getClient() { return client; }
}
```

- [ ] **Step 2: 编译验证**

```bash
javac -encoding UTF-8 -cp "lib/java-websocket-1.5.7.jar" -d out \
  src/main/RemoteController.java \
  src/main/GameController.java \
  src/main/GameMode.java \
  src/main/NetworkClient.java \
  src/server/util/MessageCodec.java \
  src/shared/Message.java \
  src/shared/MessageType.java
```

Expected: 编译成功

- [ ] **Step 3: Commit**

```bash
git add src/main/RemoteController.java
git commit -m "feat: add RemoteController for online mode"
```

---

## Phase 7: 客户端 UI 集成

### Task 15: 创建 OnlinePanel 联机面板

**Files:**
- Create: `src/main/OnlinePanel.java`

模仿 `ModeSelectPanel.java` 的 UI 模式，创建联机面板。

- [ ] **Step 1: 写入 OnlinePanel.java**

```java
package main;

import debug.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 功能描述：联机对战面板 —— 创建房间 / 加入房间
 * 使用 choice.png 作背景，23.png 作"创建房间"，24.png 作"加入房间"（复用素材）
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class OnlinePanel extends JPanel
{
    private CardLayout cardLayout;
    private Container container;
    private NetworkClient client;
    private JLabel statusLabel;
    private JTextField roomIdField;
    private RemoteController remoteController;

    private static final String IMG_BG = "src/img/gameMenu/choice.png";
    private static final String IMG_CREATE = "src/img/gameMenu/23.png";
    private static final String IMG_JOIN = "src/img/gameMenu/24.png";
    private static final String IMG_BACK = "src/img/gameMenu/bk1.png";
    private static final String IMG_BACK_HOVER = "src/img/gameMenu/bk2.png";

    private static final int BTN_W = 200;
    private static final int BTN_H = 260;

    public OnlinePanel(CardLayout cardLayout, Container container)
    {
        this.cardLayout = cardLayout;
        this.container = container;
        this.setLayout(null);
        this.setSize(ConstantNum.WINDOWS_WIDTH, ConstantNum.WINDOWS_HEIGHT);

        // 背景
        JLabel bg = new JLabel(new ImageIcon(IMG_BG));
        bg.setBounds(0, 0, ConstantNum.WINDOWS_WIDTH, ConstantNum.WINDOWS_HEIGHT);
        this.add(bg);

        // 布局：上半部分两个按钮（创建/加入），下半部分状态文字 + 房间号输入
        int totalBtnW = BTN_W * 2;
        int gap = (ConstantNum.WINDOWS_WIDTH - totalBtnW) / 3;
        int btnY = 60;

        // "创建房间" 按钮
        JLabel btnCreate = new JLabel(new ImageIcon(IMG_CREATE));
        btnCreate.setBounds(gap, btnY, BTN_W, BTN_H);
        btnCreate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCreate.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Log.info("联机：创建房间");
                statusLabel.setText("正在连接服务器...");
                connectAndCreateRoom();
            }
        });
        this.add(btnCreate);

        // "加入房间" 按钮
        JLabel btnJoin = new JLabel(new ImageIcon(IMG_JOIN));
        btnJoin.setBounds(gap * 2 + BTN_W, btnY, BTN_W, BTN_H);
        btnJoin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnJoin.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                String roomId = roomIdField.getText().trim();
                if (roomId.isEmpty())
                {
                    statusLabel.setText("请输入房间号！");
                    return;
                }
                Log.info("联机：加入房间 " + roomId);
                statusLabel.setText("正在加入房间 " + roomId + "...");
                connectAndJoinRoom(roomId);
            }
        });
        this.add(btnJoin);

        // 房间号输入框
        roomIdField = new JTextField();
        roomIdField.setFont(new Font("微软雅黑", Font.BOLD, 20));
        roomIdField.setHorizontalAlignment(JTextField.CENTER);
        roomIdField.setBounds(150, 330, 200, 40);
        this.add(roomIdField);

        // 状态文字
        statusLabel = new JLabel("输入房间号后点击下方按钮加入，或创建新房间", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBounds(50, 380, 400, 30);
        this.add(statusLabel);

        // 返回按钮
        addBackButton();
    }

    /**
     * 连接服务器并创建房间
     */
    private void connectAndCreateRoom()
    {
        connectToServer();
        if (client != null && client.isConnected())
        {
            client.send(new shared.Message(shared.MessageType.CREATE_ROOM));
        }
    }

    /**
     * 连接服务器并加入房间
     */
    private void connectAndJoinRoom(String roomId)
    {
        connectToServer();
        if (client != null && client.isConnected())
        {
            client.send(new shared.Message(shared.MessageType.JOIN_ROOM).put("roomId", roomId));
        }
    }

    /**
     * 建立 WebSocket 连接
     */
    private void connectToServer()
    {
        if (client != null && client.isConnected()) return;

        // TODO: 替换为实际阿里云服务器地址
        String serverUri = "ws://localhost:8080";
        client = new NetworkClient(serverUri);

        client.setOnMessage(msg -> {
            SwingUtilities.invokeLater(() -> handleMessage(msg));
        });

        client.setOnDisconnect(() -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("连接断开，请重试");
            });
        });

        client.connect();

        // 等待连接建立（简单轮询）
        try
        {
            for (int i = 0; i < 50 && !client.isConnected(); i++)
                Thread.sleep(100);
        }
        catch (InterruptedException ignored) {}
    }

    /**
     * 处理服务端消息
     */
    private void handleMessage(shared.Message msg)
    {
        switch (msg.getType())
        {
            case ROOM_CREATED:
                String roomId = msg.get("roomId", "");
                statusLabel.setText("房间创建成功！房间号: " + roomId + "，等待对手加入...");
                Log.info("房间创建成功: " + roomId);
                break;

            case PLAYER_JOINED:
                String playerName = msg.get("playerName", "");
                statusLabel.setText(playerName + " 已加入房间！");
                Log.info(playerName + " 加入了房间");
                break;

            case GAME_START:
                statusLabel.setText("游戏开始！");
                Log.info("联机游戏开始");
                startOnlineGame();
                break;

            case ERROR:
                statusLabel.setText("错误: " + msg.get("msg", "未知错误"));
                break;

            default:
                break;
        }
    }

    /**
     * 服务端通知游戏开始 → 创建 MainMap 进入联机模式
     */
    private void startOnlineGame()
    {
        remoteController = new RemoteController(null, client);

        // 创建联机 MainMap（先构造再设置 controller）
        MainMap mainMap = new MainMap(GameMode.ONLINE);
        remoteController = new RemoteController(mainMap, client);

        // 后续 MainMap 会在构造中设置 controller 引用...
        // 此处先关闭当前窗口，打开游戏窗口
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    private void addBackButton()
    {
        JLabel backBtn = new JLabel(new ImageIcon(IMG_BACK));
        backBtn.setBounds(0, 0, 60, 60);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (client != null) client.disconnect();
                cardLayout.show(container, "游戏菜单");
                container.revalidate();
                container.repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                backBtn.setIcon(new ImageIcon(IMG_BACK_HOVER));
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                backBtn.setIcon(new ImageIcon(IMG_BACK));
            }
        });
        this.add(backBtn);
        this.setComponentZOrder(backBtn, 0);
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
javac -encoding UTF-8 -cp "lib/java-websocket-1.5.7.jar" -d out \
  src/main/OnlinePanel.java \
  src/main/RemoteController.java \
  src/main/NetworkClient.java \
  src/main/GameController.java \
  src/main/GameMode.java \
  src/main/ConstantNum.java \
  src/debug/Log.java \
  src/shared/Message.java \
  src/shared/MessageType.java \
  src/server/util/MessageCodec.java
```

Expected: 编译成功

- [ ] **Step 3: Commit**

```bash
git add src/main/OnlinePanel.java
git commit -m "feat: add OnlinePanel for room creation and joining"
```

### Task 16: 修改 MainMap 支持 GameMode 枚举

**Files:**
- Modify: `src/main/MainMap.java`

- [ ] **Step 1: 修改 MainMap 构造器**

将 `MainMap(boolean aiMode)` 改为 `MainMap(GameMode mode)`。

编辑现有代码：
- 字段 `private final boolean aiMode` → `private final GameMode gameMode`
- 构造器改为 `public MainMap(GameMode gameMode)`
- 构造器中 `this.aiMode = aiMode` → `this.gameMode = gameMode`
- `loadPlayerAndImg()` 中 `if (aiMode)` → `if (gameMode == GameMode.LOCAL_PVE)`
- `nextPlayer()` 中 `if (getCurrentPlayer().isAI())` 的判断前新增联机模式处理：
  - 联机模式下，切换回合后发送 `TURN_NOTIFY` 给自己的 handler

- [ ] **Step 2: 新增 GameController 字段**

在 MainMap 中新增：
```java
// 在字段声明区域新增
private GameController gameController;

// 新增 getter
public GameController getGameController() { return gameController; }
public void setGameController(GameController c) { this.gameController = c; }
```

- [ ] **Step 3: 在构造器中根据 mode 初始化 controller**

```java
public MainMap(GameMode gameMode)
{
    this.gameMode = gameMode;
    // ... 现有初始化代码 ...

    // 根据模式创建 controller
    switch (gameMode)
    {
        case LOCAL_PVP:
            this.gameController = new LocalController(this);
            break;
        case LOCAL_PVE:
            this.gameController = new LocalController(this); // AI 逻辑不变
            break;
        case ONLINE:
            // RemoteController 由 OnlinePanel 在游戏开始后注入
            this.gameController = null; // 后续通过 setGameController 设置
            break;
    }
    // ...
}
```

- [ ] **Step 4: 添加 getCurrentPlayerIndex 的 public getter**

```java
public int getCurrentPlayerIndex() { return currentPlayerIndex; }
```

- [ ] **Step 5: 编译验证**

```bash
javac -encoding UTF-8 -cp "lib/java-websocket-1.5.7.jar" -d out \
  src/main/MainMap.java \
  src/main/LocalController.java \
  src/main/RemoteController.java \
  src/main/GameController.java \
  src/main/GameMode.java \
  src/main/Player.java \
  src/main/DiceController.java \
  src/main/BoardConfig.java \
  src/main/ConstantNum.java \
  $(find src/architecture -name "*.java") \
  $(find src/props -name "*.java") \
  $(find src/debug -name "*.java") \
  $(find src/shared -name "*.java") \
  $(find src/server/util -name "*.java")
```

Expected: 编译成功

- [ ] **Step 6: Commit**

```bash
git add src/main/MainMap.java
git commit -m "feat: refactor MainMap to use GameMode enum and GameController"
```

### Task 17: 修改 GameMenu 和 LeaderAnim 添加联机入口

**Files:**
- Modify: `src/main/GameMenu.java`
- Modify: `src/main/LeaderAnim.java`
- Modify: `src/main/ModeSelectPanel.java`（微调）

- [ ] **Step 1: 修改 GameMenu.java**

在 `mouseClicked` 的 switch 中，将"开始游戏"改为跳转到模式选择：

现有代码已正确跳转到"模式选择"。现在需要在 GameMenu 中添加联机入口。

GameMenu 的 `uiDisplay()` 中，`jLabelsClick` 当前只有 4 个按钮（开始游戏/游戏说明/制作团队/退出游戏）。新增第 5 个按钮"联机对战"。

具体修改：在 `uiDisplay()` 中现有的 4 个按钮后追加：
```java
// 在第 4 个按钮后追加
jLabelsClick[4].setName("退出游戏");

// 新增：联机对战按钮（需要扩展数组容量）
```

由于 GameMenu 结构和素材受限，改为在已有的"开始游戏"逻辑中不修改，而在 `ModeSelectPanel` 中新增第三个按钮"联机对战"。

- [ ] **Step 2: 修改 ModeSelectPanel.java 添加第三个按钮**

在 ModeSelectPanel 构造器中，于现有的"双人对战"（23.png）和"人机对战"（24.png）之外，新增"联机对战"按钮。

布局计算：三个按钮并排，三等分变为四等分（总宽度 = 3 × BTN_W，间距 = (500 - 3*224)/4 ≈ -43，放不下）。

改为：上排两个按钮（双人/人机），下排一个按钮（联机），联机使用文字按钮。

```java
// 联机对战按钮（位于下方居中，纯文字大按钮，蓝色字体）
JButton btnOnline = new JButton("联机对战");
btnOnline.setFont(new Font("微软雅黑", Font.BOLD, 22));
btnOnline.setForeground(new Color(50, 120, 220));
btnOnline.setBounds(150, 340, 200, 50);
btnOnline.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
btnOnline.setContentAreaFilled(false);
btnOnline.setBorderPainted(false);
btnOnline.addActionListener(e -> {
    Log.info("选择模式：联机对战");
    cardLayout.show(container, "联机对战");
    container.revalidate();
    container.repaint();
});
this.add(btnOnline);
```

- [ ] **Step 3: 修改 LeaderAnim.java 注册 OnlinePanel**

在 `initGameCard()` 方法中，现有 `gameCard = new GameCard[4]`，追加 OnlinePanel 注册：

```java
// 在 initGameCard() 末尾追加
OnlinePanel onlinePanel = new OnlinePanel(cardLayout, jPanel);
jPanel.add(onlinePanel, "联机对战");
```

- [ ] **Step 4: 编译验证**

```bash
javac -encoding UTF-8 -cp "lib/java-websocket-1.5.7.jar" -d out \
  src/main/LeaderAnim.java \
  src/main/GameMenu.java \
  src/main/ModeSelectPanel.java \
  src/main/OnlinePanel.java \
  src/main/GameCard.java \
  src/main/MainMap.java \
  src/main/ConstantNum.java \
  $(find src/debug -name "*.java") \
  $(find src/shared -name "*.java")
```

Expected: 编译成功

- [ ] **Step 5: Commit**

```bash
git add src/main/GameMenu.java src/main/LeaderAnim.java src/main/ModeSelectPanel.java
git commit -m "feat: add online mode entry in menu and mode selection"
```

### Task 18: 修改 Player.java 新增联机字段

**Files:**
- Modify: `src/main/Player.java`

- [ ] **Step 1: 新增字段**

在 Player.java 字段区末尾追加：
```java
// 联机: session 标识
private String sessionId;
private boolean isOnline = false;

public String getSessionId() { return sessionId; }
public void setSessionId(String id) { this.sessionId = id; }
public boolean isOnline() { return isOnline; }
public void setOnline(boolean online) { isOnline = online; }
```

- [ ] **Step 2: 编译验证**

```bash
javac -encoding UTF-8 -d out src/main/Player.java
```

Expected: 编译成功

- [ ] **Step 3: Commit**

```bash
git add src/main/Player.java
git commit -m "feat: add online fields to Player class"
```

---

## Phase 8: 全量编译 & 打包脚本

### Task 19: 服务端编译打包脚本

**Files:**
- Create: `build-server.sh`

- [ ] **Step 1: 写入 build-server.sh**

```bash
cat > build-server.sh << 'SCRIPT'
#!/bin/bash
# 大富翁联机服务端编译打包脚本
# 用法: bash build-server.sh

set -e

echo "=== 编译服务端 ==="

# 清理旧输出
rm -rf out/server
mkdir -p out/server

# 编译
javac -encoding UTF-8 \
  -cp "lib/java-websocket-1.5.7.jar" \
  -d out/server \
  $(find src/shared -name "*.java") \
  $(find src/server -name "*.java")

echo "编译成功！"

# 创建 MANIFEST
echo "Main-Class: server.MonopolyServer" > out/server/MANIFEST.MF

# 打包（包含依赖）
mkdir -p out/server/jar
cd out/server
cp ../../lib/java-websocket-1.5.7.jar jar/
cd jar
jar xf java-websocket-1.5.7.jar
rm java-websocket-1.5.7.jar
cd ..
jar cfm monopoly-server.jar MANIFEST.MF -C jar/ . -C . $(find . -name "*.class" -not -path "./jar/*")

cd ../..

echo "=== 打包完成: out/server/monopoly-server.jar ==="
echo ""
echo "部署命令:"
echo "  scp out/server/monopoly-server.jar root@<阿里云IP>:/opt/monopoly-server/"
echo "  ssh root@<阿里云IP> 'java -jar /opt/monopoly-server/monopoly-server.jar &'"
SCRIPT

chmod +x build-server.sh
```

- [ ] **Step 2: 测试编译脚本**

```bash
bash build-server.sh
```

Expected: 编译成功，生成 `out/server/monopoly-server.jar`

- [ ] **Step 3: Commit**

```bash
git add build-server.sh
git commit -m "feat: add server build script"
```

### Task 20: 客户端全量编译验证

**Files:**
- Modify: `build-release.sh`（如果存在）或手动验证

- [ ] **Step 1: 全量客户端编译**

```bash
javac -encoding UTF-8 \
  -cp "lib/java-websocket-1.5.7.jar" \
  -d out \
  $(find src -name "*.java" -not -path "src/server/*")
```

Expected: 编译成功，零错误

- [ ] **Step 2: 运行客户端测试启动**

```bash
java -cp "out;lib/java-websocket-1.5.7.jar" Main
```

Expected: 窗口正常启动，菜单正常显示

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: full compilation verification after online mode integration"
```

---

## Phase 9: 集成测试

### Task 21: 本地联机测试

- [ ] **Step 1: 启动服务端**

```bash
java -cp "out/server;lib/java-websocket-1.5.7.jar" server.MonopolyServer 8080
```

Expected: 输出 `[服务] MonopolyServer 已启动，监听端口 8080`

- [ ] **Step 2: 启动客户端 A**

打开另一个终端：
```bash
java -cp "out;lib/java-websocket-1.5.7.jar" Main
```

操作：主菜单 → 开始游戏 → 联机对战 → 创建房间
Expected: 状态显示"房间创建成功！房间号: XXXX"

- [ ] **Step 3: 启动客户端 B**

打开第三个终端：
```bash
java -cp "out;lib/java-websocket-1.5.7.jar" Main
```

操作：主菜单 → 开始游戏 → 联机对战 → 输入房间号 → 加入房间
Expected: 状态显示"游戏开始！"，双方窗口进入 MainMap

- [ ] **Step 4: 验证回合切换**

- 客户端 A 窗口应显示"轮到你"或骰子可点击
- 客户端 B 窗口骰子应为不可点击状态
- A 掷骰后，服务端广播行走动画，B 窗口同步显示

---

## 已知限制 & 后续迭代

1. **MonopolyEngine 精简版**：当前仅实现了掷骰、买地、升级、地雷、路障、包子。需后续补充：商店、赌场、旅馆、公园传送、偷取、考试周、身份证、万能骰子等完整逻辑。

2. **RemoteController ↔ MainMap 联机联动**：MainMap 需要在联机模式下禁用本地骰子点击（已有 AI 拦截逻辑可复用），并将 `diceResult`/`walkAnim`/`askChoice` 等服务端事件映射到 UI 动画。

3. **重连机制**：当前 30 秒断线窗口未实现，需在 MonopolyRoom 中增加定时器和状态机。

4. **HTTP API 预留**：Transport 层目前仅有 WebSocket，后续需新增 HTTP 端点（房间列表、历史记录等）。

5. **阿里云部署**：需在阿里云 ECS 上安装 JDK 25，上传 JAR，配置 systemd 服务和防火墙规则。
