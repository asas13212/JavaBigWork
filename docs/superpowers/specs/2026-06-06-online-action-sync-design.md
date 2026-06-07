# 联机操作同步 - 设计文档

> 创建日期：2026/06/06
> 状态：待实现
> 依赖：2026-06-02-online-multiplayer-design.md（联机基础架构）

---

## 一、问题

联机模式下，买地、升级、道具使用三个操作虽然发了网络消息，但存在两个问题：

1. **操作者自己没执行**：`Land.java` 联机分支弹窗后只发消息就 return，本地没有扣钱/设owner
2. **对手收不到**：服务端只 `relayToOther`（单播给对手），且 `RemoteController.handleGameUpdate()` 的 switch 分支是空的

## 二、方案：本地执行 + 广播 + 对方重放

核心思路：操作者本地先执行 → 发服务端 → 服务端广播给双方 → 操作者忽略（已执行），对手重放相同操作。

### 2.1 买地

**Land.java 联机分支**：
```
弹窗选确定 → 扣钱、设owner（本地执行）→ 发 BUY_LAND（含 tileIndex）→ 服务端广播
```

**MonopolyEngine**：`relayToOther` 改为 broadcast

**RemoteController.handleGameUpdate**：收到 `BUY_LAND`，根据 `tileIndex` 找到对应 Land，执行扣钱+设owner

消息格式：
```json
{"event":"BUY_LAND", "playerName":"naiLong", "tileIndex":5}
```

### 2.2 升级

同上模式。

消息格式：
```json
{"event":"UPGRADE", "playerName":"naiLong", "tileIndex":5}
```

### 2.3 道具

**MainMap.addPropsListener**：联机模式下不直接调 `player.use()`，改为：
- 先确定目标（SELF/OTHER 直接确定，SELECTABLE 弹窗选）
- 发 `USE_PROP` → 服务端广播
- 本地执行道具效果

**RemoteController.handleGameUpdate**：收到 `USE_PROP`，对手客户端执行相同道具。

消息格式：
```json
{"event":"USE_PROP", "playerName":"naiLong", "propName":"路障", "targetName":"xiaoMei"}
```

## 三、骰子（无需改动）

当前骰子流程已正确：
```
点击 → DiceController 检测联机 → gc.onDiceClicked() 发 ROLL_DICE
     → 服务端返回 DICE_RESULT → triggerRollWithValue() 动画+走路
     → WALK_ANIM 被本地玩家忽略（pIdx==localPlayerIndex 时 return）
```

## 四、改动清单

| 文件 | 改动内容 |
|------|----------|
| `src/architecture/Land.java` | 联机分支：发消息后也执行本地逻辑 |
| `src/server/engine/MonopolyEngine.java` | `relayToOther` 改成 broadcast；消息里加 `playerName` |
| `src/main/MainMap.java` | `addPropsListener`：联机模式走 `GameController.onPropClicked()` |
| `src/main/RemoteController.java` | `handleGameUpdate` 实现三个 case；`onPropClicked` 加本地执行和消息字段 |
| `src/main/Player.java` | 可能需要一个 `isOnline()` 检查方法（已有） |

## 五、自检

- [x] 覆盖买地、升级、道具三个操作
- [x] 消息格式包含足够上下文让对方重放
- [x] 骰子流程确认无误，不改动
- [x] 改动量小，每个文件几行到十几行
- [x] 不涉及新文件
