# 大富翁联机对战 - 设计文档

> 创建日期：2026/06/02
> 状态：设计完成，待实现
> 所属工程：第二个大工程

---

## 一、概述

将现有大富翁 Java Swing 桌面游戏改造为联机对战形式，采用客户端-服务器（C/S）架构。玩家可通过创建/加入房间（4 位数字房间号）进行远程 2 人对战。服务端部署在阿里云 ECS，同时预留 Web 接口为后续浏览器版本做准备。

---

## 二、架构总览

```
┌─────────────────────┐         WebSocket          ┌─────────────────────┐
│   客户端 A (Swing)   │ ◄═══════════════════════► │                     │
│   naiLong (人类)     │   JSON 消息                 │   阿里云服务器       │
└─────────────────────┘                             │                     │
                                                    │  MonopolyServer.jar │
┌─────────────────────┐         WebSocket          │  ├─ RoomManager     │
│   客户端 B (Swing)   │ ◄═══════════════════════► │  ├─ SessionManager  │
│   xiaoMei (人类)     │   JSON 消息                 │  ├─ GameEngine     │
└─────────────────────┘                             │  └─ HTTP API (预留) │
                                                    └─────────────────────┘
```

**核心原则**：服务端权威 — 所有游戏逻辑（掷骰、买地、扣钱、胜负）由服务端判定，客户端只负责渲染和发送操作意图。

---

## 三、技术选型

| 维度 | 决策 | 原因 |
|------|------|------|
| 网络架构 | C/S 服务器权威 | 防作弊、状态一致、天然支持 Web 扩展 |
| 通信协议 | WebSocket + JSON | 实时双向、浏览器原生支持、Java 生态成熟 |
| 服务端语言 | 纯 Java (JDK 25) | 与客户端统一技术栈，单 JAR 部署 |
| WebSocket 库 | org.java-websocket | 纯 Java、无需容器、main() 直接启动 |
| 房间机制 | 创建-加入（4位数字房间号） | 简单可靠，类似王者荣耀开房间 |
| 玩家数量 | 固定 2 人对战 | 与现有双人模式兼容，改动最小 |
| 联机 AI | 不做 | 联机仅人人对战，AI 保留在本地人机模式 |

---

## 四、服务端分层架构

```
┌─────────────────────────────────────────────────────┐
│                   Transport Layer                    │
│  WebSocketEndpoint (今天) │ HTTP API (预留Web版)      │
│  统一收发 JSON，不关心业务逻辑                          │
├─────────────────────────────────────────────────────┤
│                   Session Layer                      │
│  SessionManager: 管理所有连接                          │
│  Session = 一个连接，与 Player 松耦合                   │
│  支持同一 Player 多 Session（Web 多开 / 重连）          │
├─────────────────────────────────────────────────────┤
│                    Room Layer                        │
│  RoomManager: 创建/销毁/查找房间                       │
│  Room 抽象：定义生命周期，不绑定具体游戏                  │
│  ┌──────────┐                                        │
│  │  Room    │  ← 抽象接口                             │
│  │  - id    │                                         │
│  │  - players[]                                      │
│  │  - start() / end()                                │
│  └────┬─────┘                                        │
│       │ implements                                   │
│  ┌────┴───────────┐                                  │
│  │ MonopolyRoom   │  ← 大富翁具体逻辑                  │
│  │ MonopolyEngine │  ← 权威游戏状态机                  │
│  └────────────────┘                                  │
├─────────────────────────────────────────────────────┤
│                   Game Engine Layer                  │
│  GameEngine<Action, Event> 接口:                      │
│    processAction(state, player, action) → List<Event>│
│  MonopolyEngine 实现大富翁规则                          │
│  纯函数设计，可单元测试（不依赖网络）                      │
│  未来可替换为其他游戏（飞行棋、UNO...）                   │
├─────────────────────────────────────────────────────┤
│                   Storage Layer (预留)                │
│  GameRepository 接口                                  │
│  InMemoryRepository (今天)                            │
│  JdbcRepository (未来连数据库)                         │
└─────────────────────────────────────────────────────┘
```

### 关键接口

```java
// 房间抽象 - 不绑定游戏类型
interface Room {
    String getId();
    List<PlayerSession> getPlayers();
    void onPlayerJoin(PlayerSession session);
    void onPlayerLeave(PlayerSession session);
    void onMessage(PlayerSession session, Message msg);
    RoomState getState();
}

// 游戏引擎 - 纯函数式，输入消息输出事件
interface GameEngine<Action, Event> {
    List<Event> processAction(GameState state, Player player, Action action);
    GameState getInitialState(List<Player> players);
}
```

---

## 五、通信协议

### 消息信封

```json
{
  "ver": 1,
  "type": "game_action",
  "action": "roll_dice",
  "seq": 42,
  "ts": 1717334400000,
  "payload": {}
}
```

### 消息类型清单

**客户端 → 服务端**：

| action | payload | 说明 |
|--------|---------|------|
| `create_room` | `{}` | 创建房间 |
| `join_room` | `{"roomId": 4821}` | 加入房间 |
| `ready` | `{}` | 准备开始 |
| `roll_dice` | `{}` | 掷骰 |
| `use_prop` | `{"propName":"路障","targetIdx":12}` | 使用道具 |
| `buy_land` | `{"choice":true}` | 买地确认 |
| `upgrade` | `{"choice":true}` | 升级确认 |
| `hotel_choice` | `{"choice":true}` | 住店确认 |
| `shop_buy` | `{"itemName":"地雷"}` | 商店购买 |
| `casino_bet` | `{"amount":500}` | 赌场下注 |
| `leave_room` | `{}` | 离开房间 |

**服务端 → 客户端**：

| type | payload | 说明 |
|------|---------|------|
| `room_created` | `{"roomId":4821}` | 房间已创建 |
| `player_joined` | `{"playerName":"xiaoMei"}` | 对手加入 |
| `game_start` | `{"firstPlayer":"naiLong"}` | 游戏开始 |
| `game_state` | `{"state":{...}}` | 全量状态同步 |
| `game_update` | `{"update":{...}}` | 增量状态更新 |
| `turn_notify` | `{"player":"naiLong"}` | 通知轮到谁 |
| `dice_result` | `{"value":6}` | 骰子结果 |
| `walk_anim` | `{"steps":[0,1,2,...]}` | 行走路径 |
| `ask_choice` | `{"question":"buy_land","data":{...}}` | 要求玩家做选择 |
| `player_left` | `{}` | 对手离开 |
| `game_over` | `{"winner":"naiLong"}` | 游戏结束 |

### 消息枚举定义（共享代码）

```java
// MessageType.java - 客户端服务端共享
public enum MessageType {
    // 客户端→服务端
    CREATE_ROOM, JOIN_ROOM, READY, ROLL_DICE,
    USE_PROP, BUY_LAND, UPGRADE, HOTEL_CHOICE,
    SHOP_BUY, CASINO_BET, LEAVE_ROOM,

    // 服务端→客户端
    ROOM_CREATED, PLAYER_JOINED, GAME_START,
    GAME_STATE, GAME_UPDATE, TURN_NOTIFY,
    DICE_RESULT, WALK_ANIM, ASK_CHOICE,
    PLAYER_LEFT, GAME_OVER, ERROR
}
```

---

## 六、客户端改造方案

### GameController 抽象层

```java
// 统一接口 - 三种模式共用 MainMap
interface GameController {
    void onDiceClicked();
    void onPropClicked(String propName);
    void onLandChoice(boolean yes);
    void onShopBuy(String item);
    void onCasinoBet(int amount);
    void onHotelChoice(boolean yes);
}

class LocalController implements GameController { /* 现有逻辑迁移 */ }
class AIController implements GameController    { /* 现有逻辑迁移 */ }
class RemoteController implements GameController {
    private NetworkClient client;
    // 所有方法：发送 JSON 到服务端，禁用 UI，等待响应
}
```

### MainMap 改造

```
现有：MainMap(boolean aiMode)
新增：MainMap(GameMode mode)

GameMode 枚举：LOCAL_PVP | LOCAL_PVE | ONLINE
```

**最小侵入原则**：
- 所有"本地决策"分支改为从 GameController 获取
- 现有 JOptionPane 弹窗保持不变
- 新增"等待对手..."状态 UI
- 骰子点击权限复用现有 AI 拦截逻辑

### 联机面板 UI

```
┌─────────────────────────────┐
│         ← 返回              │
│                             │
│   ┌─────────────────┐       │
│   │   创建房间        │       │
│   │   与好友对战      │       │
│   └─────────────────┘       │
│                             │
│   房间号: [____]             │
│   ┌─────────────────┐       │
│   │   加入房间        │       │
│   └─────────────────┘       │
│                             │
│   状态: 等待对手加入...      │
└─────────────────────────────┘
```

---

## 七、房间生命周期

```
玩家A 点击"创建房间"
    │
    ▼
服务端生成 4 位房间号 → 显示给 A："房间号 4821，等待对手..."
    │
    ▼
玩家B 输入房间号 4821 加入
    │
    ▼
双方就位，A 点击"开始游戏"
    │
    ▼
╔══════════════════════════════╗
║     游戏运行中               ║
║  ┌────────┐  ┌────────┐     ║
║  │ A 回合  │→│ B 回合  │ →  ║
║  │ 操作... │  │ 等待... │     ║
║  └────────┘  └────────┘     ║
╚══════════════════════════════╝
    │
    ▼
有人获胜 → game_over 广播
    │
    ▼
房间保留，可重新开始或退出
```

**异常处理**：
- 断线重连：30 秒窗口，超时判定在线方获胜
- 房间空闲 5 分钟自动销毁
- 房间号不重复（同一时间）

---

## 八、文件清单

### 新增文件（服务端）

| 文件 | 包 | 说明 |
|------|-----|------|
| `MonopolyServer.java` | `server` | 服务端入口，启动 WebSocket |
| `WebSocketEndpoint.java` | `server.transport` | WebSocket 端点 |
| `SessionManager.java` | `server.session` | 连接管理 |
| `PlayerSession.java` | `server.session` | 连接封装 |
| `Room.java` | `server.room` | 房间抽象接口 |
| `RoomManager.java` | `server.room` | 房间池管理 |
| `MonopolyRoom.java` | `server.room` | 大富翁房间实现 |
| `GameEngine.java` | `server.engine` | 引擎接口 |
| `MonopolyEngine.java` | `server.engine` | 大富翁规则引擎 |
| `GameState.java` | `server.engine` | 权威游戏状态 |
| `GameRepository.java` | `server.storage` | 存储接口(预留) |
| `InMemoryRepository.java` | `server.storage` | 内存实现 |
| `MessageCodec.java` | `server.util` | JSON 编解码 |
| `MessageType.java` | `shared` | 消息枚举(共享) |

### 新增文件（客户端）

| 文件 | 包 | 说明 |
|------|-----|------|
| `GameController.java` | `main` | 游戏控制器接口 |
| `LocalController.java` | `main` | 本地模式实现 |
| `RemoteController.java` | `main` | 联机模式实现 |
| `NetworkClient.java` | `main` | WebSocket 客户端封装 |
| `OnlinePanel.java` | `main` | 联机面板 UI |
| `GameMode.java` | `main` | 游戏模式枚举 |

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `MainMap.java` | 引入 GameController，GameMode 枚举，原有逻辑拆分到 LocalController |
| `GameMenu.java` | 新增"联机对战"按钮 |
| `LeaderAnim.java` | 注册 OnlinePanel 到 CardLayout |
| `Player.java` | 新增联机相关字段（sessionId 等） |

### 依赖新增

- `lib/java-websocket-1.5.7.jar`（客户端 + 服务端共用）

---

## 九、部署方案

```
阿里云 ECS
├── JDK 25 (Temurin)
├── /opt/monopoly-server/
│   ├── monopoly-server.jar
│   └── server.properties
├── systemd: monopoly-server.service
│   └── ExecStart=java -jar monopoly-server.jar --port=8080
├── 防火墙开放 8080 端口
└── 日志: /var/log/monopoly-server.log
```

**server.properties**：
```properties
server.port=8080
room.max=50
room.idle.timeout=300
room.reconnect.timeout=30
```

**一键部署**：
```bash
scp monopoly-server.jar root@<IP>:/opt/monopoly-server/
ssh root@<IP> "systemctl restart monopoly-server"
```

---

## 十、后续扩展预留

| 方向 | 预留设计 |
|------|----------|
| Web 浏览器版 | HTTP API + WebSocket 同协议，Transport 层加 HTTP 端点即可 |
| 数据库持久化 | GameRepository 接口，换 InMemory → JDBC 实现 |
| 更多游戏类型 | Room 接口 + GameEngine 接口，新游戏只需实现两个接口 |
| 2-4 人扩展 | Room 中 players 已是 List，引擎改为多玩家即可 |
| AI 陪玩联机 | MonopolyEngine 可注入 AI 策略，替换真人 PlayerSession |

---

## 十一、自检清单

- [x] 无 TBD / TODO 占位符
- [x] 架构分层与功能描述一致
- [x] 通信协议覆盖所有游戏操作
- [x] 接口定义完整（Room, GameEngine, GameController）
- [x] 部署方案可执行
- [x] 文件清单完整
- [x] 扩展预留点明确
