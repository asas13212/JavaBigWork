# 联机操作同步 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 联机模式下买地、升级、道具三个操作走通网络：操作者本地执行 → 服务端转发对手 → 对手重放

**Architecture:** 保持客户端权威，服务端只转发。操作者先本地执行再发消息，服务端 relayToOther 不变。双方各自执行相同逻辑保持状态一致。

**Tech Stack:** Java Swing + WebSocket (org.java-websocket) + 手写 JSON 编解码

---

### Task 1: Land.java — 买地联机分支补全本地执行

**Files:**
- Modify: `src/architecture/Land.java:72-80`
- Modify: `src/architecture/Land.java:121-129`

**说明:** 联机模式下，`onPlayerArrive` 的买地和升级分支目前只弹窗+发消息就 return，本地没有执行扣钱/设owner/升级。需要在发消息的同时执行本地逻辑。价格从消息传给对手用 `getCurrentPrice()` 获取，对手在 `handleGameUpdate` 里用同样价格。

- [ ] **Step 1: 修改买地联机分支 (line 72-80)**

将原来的：
```java
if (player.isOnline())
{
    int r = JOptionPane.showOptionDialog(null,
        "购买当前房产:" + this.getName() + "? 价格是" + this.getCurrentPrice(),
        "确认", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new String[]{"确定", "取消"}, "确定");
    player.getGameController().onLandChoice(r == 0);
    return;
}
```

改为：
```java
if (player.isOnline())
{
    int r = JOptionPane.showOptionDialog(null,
        "购买当前房产:" + this.getName() + "? 价格是" + this.getCurrentPrice(),
        "确认", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new String[]{"确定", "取消"}, "确定");
    if (r == 0)
    {
        player.getGameController().onLandChoice(true);
        if (player.getMoney() >= priceLevelUp[houseLevel])
        {
            player.moneyDecrease(priceLevelUp[houseLevel]);
            this.setOwner(player);
            player.addTilesOwned(this);
        }
        else
        {
            JOptionPane.showMessageDialog(null, "你没资格啊没资格");
        }
    }
    return;
}
```

- [ ] **Step 2: 修改升级联机分支 (line 121-129)**

将原来的：
```java
if (player.isOnline())
{
    int r = JOptionPane.showOptionDialog(null,
        "升级当前房产:" + this.getName() + "? 价格是" + this.priceLevelUp[houseLevel+1],
        "确认", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new String[]{"确定", "取消"}, "确定");
    player.getGameController().onUpgradeChoice(r == 0);
    return;
}
```

改为：
```java
if (player.isOnline())
{
    int r = JOptionPane.showOptionDialog(null,
        "升级当前房产:" + this.getName() + "? 价格是" + this.priceLevelUp[houseLevel+1],
        "确认", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new String[]{"确定", "取消"}, "确定");
    if (r == 0)
    {
        player.getGameController().onUpgradeChoice(true);
        if (player.getMoney() >= priceLevelUp[houseLevel+1])
        {
            player.moneyDecrease(priceLevelUp[houseLevel+1]);
            this.houseLevel++;
        }
        else
        {
            JOptionPane.showMessageDialog(null, "你没资格啊没资格");
        }
    }
    return;
}
```

- [ ] **Step 3: 编译验证**

Run: `bash compile.bat`
Expected: 编译通过

- [ ] **Step 4: 提交**

```bash
git add src/architecture/Land.java
git commit -m "fix: 联机买地和升级分支执行本地逻辑，发消息同时扣钱/设owner/升级"
```

---

### Task 2: RemoteController — 消息增加 tileIndex 和 playerName 字段

**Files:**
- Modify: `src/main/RemoteController.java:151-155`

**说明:** `onLandChoice` 和 `onUpgradeChoice` 发出的消息需要包含 `tileIndex`（当前玩家所在格子）和 `playerName`，对手才能知道在哪个地产执行操作。只发 `choice=true` 时携带（选取消不发消息也没事，但为了一致性也发）。

- [ ] **Step 1: 修改 onLandChoice**

将原来的：
```java
@Override public void onLandChoice(boolean yes)
{ client.send(new Message(MessageType.BUY_LAND).put("choice", yes)); }
```

改为：
```java
@Override public void onLandChoice(boolean yes)
{
    if (!yes) return;
    Player cp = mainMap.getCurrentPlayer();
    int tileIndex = cp.getPositionIndex();
    client.send(new Message(MessageType.BUY_LAND)
        .put("choice", true)
        .put("tileIndex", tileIndex)
        .put("playerName", cp.getName()));
}
```

- [ ] **Step 2: 修改 onUpgradeChoice**

将原来的：
```java
@Override public void onUpgradeChoice(boolean yes)
{ client.send(new Message(MessageType.UPGRADE).put("choice", yes)); }
```

改为：
```java
@Override public void onUpgradeChoice(boolean yes)
{
    if (!yes) return;
    Player cp = mainMap.getCurrentPlayer();
    int tileIndex = cp.getPositionIndex();
    client.send(new Message(MessageType.UPGRADE)
        .put("choice", true)
        .put("tileIndex", tileIndex)
        .put("playerName", cp.getName()));
}
```

- [ ] **Step 3: 编译验证**

Run: `bash compile.bat`
Expected: 编译通过

- [ ] **Step 4: 提交**

```bash
git add src/main/RemoteController.java
git commit -m "feat: 联机买地/升级消息增加 tileIndex 和 playerName 字段"
```

---

### Task 3: RemoteController — handleGameUpdate 实现 BUY_LAND 和 UPGRADE

**Files:**
- Modify: `src/main/RemoteController.java:120-135`

**说明:** 对手客户端收到 `GAME_UPDATE` 后，根据消息携带的 `tileIndex` 和 `playerName` 重放买地/升级操作。`GAME_UPDATE` 的 `data` 字段是原始消息的 data Map（由服务端 `relayToOther` 封装）。

- [ ] **Step 1: 实现 handleGameUpdate 的 BUY_LAND 和 UPGRADE case**

将原来的：
```java
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
```

改为：
```java
private void handleGameUpdate(Message msg)
{
    String event = msg.get("event", "");
    if (event == null) return;
    Player p0 = mainMap.getPlayer(0), p1 = mainMap.getPlayer(1);

    @SuppressWarnings("unchecked")
    java.util.Map<String, Object> data = msg.get("data");
    if (data == null) { mainMap.refreshLayers(); return; }

    String playerName = (String) data.get("playerName");
    int tileIndex = data.get("tileIndex") instanceof Number n ? n.intValue() : -1;
    if (playerName == null || tileIndex < 0) { mainMap.refreshLayers(); return; }

    Player actor = playerName.equals("naiLong") ? p0 : p1;
    Tile tile = mainMap.boardConfig.getTiles()[tileIndex];
    if (!(tile instanceof Land land)) { mainMap.refreshLayers(); return; }

    switch (event)
    {
        case "BUY_LAND":
            if (land.getOwner() == null)
            {
                int price = land.getCurrentPrice();
                actor.moneyDecrease(price);
                land.setOwner(actor);
                actor.addTilesOwned(land);
                Log.info(playerName + " 购买了 " + land.getName() + "，花费 $" + price);
            }
            break;

        case "UPGRADE":
            if (land.getOwner() == actor && land.canLevelUp())
            {
                int upgradePrice = land.getUpgradePrice();
                actor.moneyDecrease(upgradePrice);
                land.houseLevelUp();
                Log.info(playerName + " 升级了 " + land.getName() + "，花费 $" + upgradePrice);
            }
            break;

        case "USE_PROP":
            // Task 5 实现
            break;
    }
    mainMap.refreshLayers();
}
```

注意：需要给 `Land.java` 新增 `getUpgradePrice()` 方法（在 Task 3.5 中添加）。

- [ ] **Step 2: 在 Land.java 增加 getUpgradePrice() 方法**

在 `Land.java` 中添加（`getCurrentPrice()` 方法旁边）：

```java
/**
 * 功能描述：获取升级到下一级的价格（最高级时返回 Integer.MAX_VALUE）
 */
public int getUpgradePrice()
{
    if (houseLevel >= maxLevel) return Integer.MAX_VALUE;
    return priceLevelUp[houseLevel + 1];
}
```

- [ ] **Step 3: 在 RemoteController.java 顶部添加 import**

确认文件头部已有或添加：
```java
import architecture.Land;
import architecture.Tile;
```

- [ ] **Step 4: 编译验证**

Run: `bash compile.bat`
Expected: 编译通过

- [ ] **Step 5: 提交**

```bash
git add src/main/RemoteController.java src/architecture/Land.java
git commit -m "feat: 联机对手客户端接收并执行买地/升级操作"
```

---

### Task 4: MainMap — 联机模式道具走网络

**Files:**
- Modify: `src/main/MainMap.java:345-387` (addPropsListener 中的 mouseClicked)

**说明:** 当前 `addPropsListener` 中的 `mouseClicked` 直接调用 `player.use()`，联机模式下需要先通过 `GameController.onPropClicked()` 发消息，再本地执行。为传递目标信息，给 `GameController.onPropClicked` 增加 `targetName` 参数。

- [ ] **Step 1: 修改 GameController 接口 onPropClicked 签名**

修改 `src/main/GameController.java:12`：
```java
void onPropClicked(String propName);  // 旧的
```
改为：
```java
void onPropClicked(String propName, String targetName);  // 新的
```

- [ ] **Step 2: 修改 LocalController.onPropClicked 签名**

修改 `src/main/LocalController.java:18`：
```java
@Override public void onPropClicked(String propName) { /* handled by MainMap.addPropsListener */ }
```
改为：
```java
@Override public void onPropClicked(String propName, String targetName) { /* handled by MainMap.addPropsListener */ }
```

- [ ] **Step 3: 修改 RemoteController.onPropClicked 签名并充实**

修改 `src/main/RemoteController.java:148-149`：
```java
@Override public void onPropClicked(String propName)
{ if (!myTurn) return; client.send(new Message(MessageType.USE_PROP).put("propName", propName)); }
```
改为：
```java
@Override public void onPropClicked(String propName, String targetName)
{
    if (!myTurn) return;
    client.send(new Message(MessageType.USE_PROP)
        .put("propName", propName)
        .put("playerName", myPlayerName)
        .put("targetName", targetName));
}
```

- [ ] **Step 4: 修改 MainMap.addPropsListener 的 mouseClicked**

将 mouseClicked 方法中的切换逻辑改为联机模式下走网络。原来的 switch 逻辑保持不变用于本地模式，在 switch 之前加入联机路由。

在 `mouseClicked` 方法中，把：
```java
// AI 回合或非本人回合时禁止点击道具
if (getCurrentPlayer().isAI() || (gameController != null && !gameController.isMyTurn())) return;

switch (def.category) {
    ...
}
refreshLayers();
```

改为：
```java
// AI 回合或非本人回合时禁止点击道具
if (getCurrentPlayer().isAI() || (gameController != null && !gameController.isMyTurn())) return;

// 联机模式：先发消息再本地执行
if (gameController != null && gameController.getMode() == GameMode.ONLINE)
{
    Player currentPlayer = getCurrentPlayer();
    switch (def.category)
    {
        case SELF -> {
            gameController.onPropClicked(def.name, currentPlayer.getName());
            currentPlayer.use(def.name, currentPlayer);
        }
        case SELECTABLE -> {
            int result = JOptionPane.showOptionDialog(
                    null,
                    "选择玩家，对其使用" + def.name,
                    "选择玩家",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"自己", "对方", "取消"},
                    "确定"
            );
            if (result == 1) {
                Player other = players[(currentPlayerIndex + 1) % players.length];
                gameController.onPropClicked(def.name, other.getName());
                currentPlayer.use(def.name, other);
            } else if (result == 0) {
                gameController.onPropClicked(def.name, currentPlayer.getName());
                currentPlayer.use(def.name, currentPlayer);
            }
        }
        case OTHER -> {
            Player other = players[(currentPlayerIndex + 1) % players.length];
            gameController.onPropClicked(def.name, other.getName());
            currentPlayer.use(def.name, other);
        }
    }
    refreshLayers();
    return;
}

// 本地模式：原有逻辑
switch (def.category) {
    ...
}
refreshLayers();
```

- [ ] **Step 5: 确认 MainMap 已有 import**

MainMap.java 需要 import GameMode（已有）和 GameController（已有）。

- [ ] **Step 6: 编译验证**

Run: `bash compile.bat`
Expected: 编译通过

- [ ] **Step 7: 提交**

```bash
git add src/main/GameController.java src/main/LocalController.java src/main/RemoteController.java src/main/MainMap.java
git commit -m "feat: 联机模式道具使用走网络——发消息后本地执行"
```

---

### Task 5: RemoteController — handleGameUpdate 实现 USE_PROP

**Files:**
- Modify: `src/main/RemoteController.java:131-133` (handleGameUpdate 中的 USE_PROP case)

**说明:** 对手客户端收到 `USE_PROP` 后，根据消息中的 `propName`、`playerName`、`targetName` 重放道具效果。

- [ ] **Step 1: 实现 USE_PROP case**

在 `handleGameUpdate` 的 switch 中，将：
```java
case "USE_PROP":
    // Task 5 实现
    break;
```

改为：
```java
case "USE_PROP":
{
    String propName = (String) data.get("propName");
    String targetName = (String) data.get("targetName");
    if (propName != null && targetName != null)
    {
        Player target = targetName.equals("naiLong") ? p0 : p1;
        actor.use(propName, target);
        Log.info(playerName + " 对 " + targetName + " 使用了 " + propName);
    }
    break;
}
```

- [ ] **Step 2: 编译验证**

Run: `bash compile.bat`
Expected: 编译通过

- [ ] **Step 3: 提交**

```bash
git add src/main/RemoteController.java
git commit -m "feat: 联机对手客户端接收并执行道具使用操作"
```

---

### Task 6: 联机功能验证

**说明:** 启动服务端和两个客户端，验证买地、升级、道具三个操作在双方客户端上状态一致。

- [ ] **Step 1: 启动服务端**

```bash
bash run-server.bat
```
或
```bash
java -cp "out;lib/java-websocket-1.5.7.jar;lib/slf4j-api-2.0.9.jar;lib/slf4j-nop-2.0.9.jar" server.MonopolyServer 8080
```

- [ ] **Step 2: 启动客户端 A（房主）**

点击"联机对战" → "创建房间" → 记下房间号

- [ ] **Step 3: 启动客户端 B（客机）**

点击"联机对战" → 输入房间号 → "加入房间"

- [ ] **Step 4: 验证买地同步**

在房主回合走到无主地产 → 点击"确定"购买 → 检查：
- [ ] 房主客户端：地产显示已购买，金钱扣除
- [ ] 客机客户端：同一地产显示已购买，房主金钱同步扣除

- [ ] **Step 5: 验证升级同步**

在房主回合走到自己地产 → 点击"确定"升级 → 检查：
- [ ] 房主客户端：地产等级提升，金钱扣除
- [ ] 客机客户端：同一地产等级同步提升

- [ ] **Step 6: 验证道具同步**

使用道具（如"包子"对自己、"偷取"对对手）→ 检查：
- [ ] 双方客户端道具库存和效果一致
- [ ] 路障放置后双方都看到路障图标
- [ ] 地雷放置后双方都看到地雷图标

---

### 可能遇到的问题

1. **路障放置**: `Barrier.isUsed()` 内部会弹出 JFrame 选择位置。对手收到 `USE_PROP` 后重放时可能也会弹窗（对手客户端也调用 `use()`）。需要检查 `Barrier` 和 `Mine` 道具的 `isUsed` 实现，确认联机模式下是否会导致重复弹窗。

2. **万能骰子**: `Dice.isUsed()` 内部设定 `player.setNextDiceValue()`。对手重放时会设定对手自己的骰子值（因为 `actor` 是操作者而非接收者）。需要确认 `playerName` 和 `targetName` 的语义一致。

3. **升级卡**: `HouseLevelUp.isUsed()` 需要选择一个地产来升级。对手重放时需要知道选了哪个地产。
