# AI 开发对话记录

> 记录时间：2026/06/01（两轮迭代）
> 主题：实现 AI 对战模式 + 素材图片模式选择 UI
> 第一轮：实现 AI 决策类、自动回合、模式选择基础功能
> 第二轮：用 23.png / 24.png / choice.png 替换 JOptionPane，实现图形化模式选择

---

## 一、用户需求

**原始问题：**
> "帮我完成AI类，使得游戏可以选择双人玩和与AI玩两种选项"

**需求拆解：**
1. 在游戏菜单中添加模式选择（双人对战 / 人机对战）
2. 实现 AI 玩家自动决策逻辑（掷骰、买地、升级、使用道具等）
3. 让现有游戏流程兼容 AI 自动操作（跳过弹窗确认）

---

## 二、代码架构分析

### 现有架构梳理

| 文件 | 职责 |
|---|---|
| `Main.java` | 程序入口，启动开场动画 |
| `LeaderAnim.java` | 开场动画 → 切换到 GameMenu |
| `GameMenu.java` | 主菜单（开始游戏/游戏说明/制作团队/退出） |
| `MainMap.java` | 核心游戏地图，管理玩家、回合、骰子、行走动画 |
| `Player.java` | 玩家类：属性、道具、移动、购买等 |
| `DiceController.java` | 骰子 UI 控制及动画 |
| `Land.java` | 地产基类：购买、升级、收税逻辑 |
| `HotelLand.java` | 旅馆地块 |
| `ShopLand.java` | 超市地块（购买道具） |
| `AIDecision.java` | （空文件）待实现的 AI 决策类 |

### 关键流程

```
玩家点击骰子 → 掷骰 → 行走动画（逐格） → 到达格子 → 
触发格子事件（买地/升级/交税/道具等） → 切换下一玩家
```

### 痛点
- 所有交互都通过 `JOptionPane` 弹窗，AI 无法自动处理
- 骰子依赖鼠标点击 `JLabel`
- 没有 AI 玩家概念

---

## 三、实现方案

### 修改清单

| 文件 | 修改内容 |
|---|---|
| `Player.java` | 新增 `isAI` 字段、getter/setter；`use()` 方法跳过 AI 确认弹窗 |
| `AIDecision.java` | **核心新增**：完整的 AI 决策逻辑（买地、升级、道具、商店、旅馆） |
| `DiceController.java` | 新增 `triggerRoll()` 公开方法，抽取 `executeRoll()` 核心逻辑 |
| `Land.java` | `onPlayerArrive()` 中检测 AI 玩家，使用 AIDecision 自动决定买地/升级 |
| `HotelLand.java` | `onPlayerArrive()` 中检测 AI 玩家，自动决定是否住店 |
| `ShopLand.java` | `onPlayerArrive()` 中检测 AI 玩家，自动选择购买道具 |
| `MainMap.java` | 新增 `aiMode` 构造参数；`startAITurn()` 自动回合流程；`nextPlayer()` 触发 AI 回合 |
| `GameMenu.java` | "开始游戏"按钮改为切换到 ModeSelectPanel |
| `ModeSelectPanel.java` | **第二轮新增**：图形化模式选择面板（choice.png 背景 + 23.png/24.png 按钮） |
| `LeaderAnim.java` | 注册 ModeSelectPanel 到 CardLayout |

### 架构设计

```
GameMenu（选择模式）
    │
    ├─ 双人对战 → MainMap(aiMode=false)
    │                 ├─ players[0] = 人类 "naiLong"
    │                 └─ players[1] = 人类 "xiaoMei"
    │
    └─ 人机对战 → MainMap(aiMode=true)
                      ├─ players[0] = 人类 "naiLong"
                      └─ players[1] = AI "AI·xiaoMei"
```

### AI 回合流程

```
nextPlayer() 切换到 AI
    │
    ▼
startAITurn() [延迟 800ms]
    │
    ├─ AIDecision.decidePropBeforeRoll() → 决定是否使用道具
    ├─ AIDecision.decidePropTarget()     → 决定道具目标
    ├─ Player.use()                       → 执行道具（跳过确认弹窗）
    │
    ▼ [延迟 500ms]
    │
diceController.triggerRoll() → 自动掷骰
    │
    ▼
行走动画 → 到达格子
    │
    ├─ Land.onPlayerArrive()       → AIDecision.shouldBuyLand / shouldUpgradeLand
    ├─ HotelLand.onPlayerArrive()  → AIDecision.shouldUseHotel
    └─ ShopLand.onPlayerArrive()   → AIDecision.chooseShopItem
    │
    ▼
nextPlayer() 切换回人类玩家
```

---

## 四、AI 决策策略

### 1. 买地决策 `shouldBuyLand()`
- **必须条件**：购买后剩余资金 ≥ 初始资金的 30%
- **高价值优先**：最高税率 ≥ 3000 的地块必买
- **普通地块**：80% 概率购买（引入随机性避免过于机械）

### 2. 升级决策 `shouldUpgradeLand()`
- **必须条件**：升级后剩余资金 ≥ 初始资金的 20%
- **高级别更谨慎**：3 级以上时降为 60% 概率
- **普通升级**：85% 概率

### 3. 道具使用 `decidePropBeforeRoll()`
按优先级依次判断：
1. **包子**：HP ≤ 30 时使用
2. **地雷**：对手在后方 10 格内时，埋在脚下
3. **路障**：放在对手前方 4 格位置拦截
4. **升级卡**：有地产且有足够资金时使用
5. **偷取**：对手资金超过自己 2000+ 时使用
6. **考试周**：对手 HP ≤ 50 时使用
7. **万能骰子**：前面 6 格内有自己的高级地产时使用

### 4. 商店购物 `chooseShopItem()`
优先级：地雷 > 路障 > 升级卡 > 偷取 > 考试周 > 万能骰子 > 包子 > 身份证

### 5. 旅馆决策 `shouldUseHotel()`
- 自己开的旅馆（免费）：必用
- 别人的旅馆：资金 ≥ 2000 时使用

---

## 五、程序迭代修改日志

### 第 1 轮：Player.java 添加 AI 标识
- 新增 `private boolean isAI = false` 字段
- 新增 `isAI()` getter 和 `setAI(boolean)` setter
- 修改 `use()` 方法：当 `isAI` 为 true 时跳过 `JOptionPane` 确认弹窗

### 第 2 轮：AIDecision.java 核心实现
- 实现 `shouldBuyLand()` — 土地购买决策
- 实现 `shouldUpgradeLand()` — 土地升级决策
- 实现 `decidePropBeforeRoll()` — 掷骰前道具决策
- 实现 `decidePropTarget()` — 道具目标选择
- 实现 `chooseShopItem()` — 商店购物选择
- 实现 `shouldUseHotel()` — 旅馆消费决策
- 实现 `chooseDiceValue()` — 万能骰子点数选择
- 实现 `chooseLandToUpgrade()` — 升级卡目标选择
- **编译错误修复**：`land.getTax()` → `land.tax`（tax 是 public 字段，无 getter）

### 第 3 轮：DiceController.java 重构
- 抽取 `executeRoll()` 方法（从 mouseClicked 中独立出来）
- 新增 `triggerRoll()` 公开方法供 AI 自动调用

### 第 4 轮：Land/HotelLand/ShopLand AI 适配
- **Land.java**：`onPlayerArrive()` 中增加 `player.isAI()` 分支，调用 AIDecision 自动决策
- **HotelLand.java**：`onPlayerArrive()` 中增加 AI 分支，自动决定是否住店
- **ShopLand.java**：`onPlayerArrive()` 中增加 AI 分支，自动选择购买道具

### 第 5 轮：MainMap.java AI 模式集成
- 新增 `aiMode` 字段和构造参数
- `loadPlayerAndImg()` 中设置 AI 玩家标识
- 新增 `startAITurn()` 方法：AI 自动回合（道具决策 → 延迟掷骰）
- `nextPlayer()` 末尾增加 AI 回合触发逻辑
- 使用双重 Timer 实现 AI 行动的视觉延迟（800ms 思考 + 500ms 掷骰）

### 第 6 轮：GameMenu.java 模式选择
- "开始游戏" 按钮改为弹出 `JOptionPane` 模式选择
- 选项：「双人对战」→ `MainMap(false)`，「人机对战」→ `MainMap(true)`

### 编译验证
- 所有修改文件编译通过（javac -encoding UTF-8）
- 全量编译通过（无错误、无警告）

### 第 7 轮：ModeSelectPanel.java 图形化模式选择（第二轮迭代）
- 用 `choice.png`（500×460）作全屏背景
- `23.png`（224×307）作双人对战按钮，位于左侧
- `24.png`（224×307）作人机对战按钮，位于右侧
- 三等分间距布局，垂直居中
- 左上角返回按钮（与 GameCard 风格统一）
- `LeaderAnim.initGameCard()` 中注册面板到 CardLayout（名称："模式选择"）
- `GameMenu.java` "开始游戏" 改为 `cardLayout.show(container, "模式选择")`
- 只有双人和人机两种选项，不提供 AI vs AI

### 编译验证（第二轮）
- 全量编译通过

---

## 六、Prompt 记录

### Prompt 1（用户）
```
现在我和你的聊天要记录在AIDev中，里面要包含我提出的问题和你的聊天记录，
同时要写出你的解答过程，用md写，包含各阶段Prompt记录、程序迭代修改日志。
现在帮我完成AI类，使得游戏可以选择双人玩和与AI玩两种选项
```

### Prompt 2（Claude 内部规划）
> 分析了现有代码架构，确定了 7 个修改任务：
> 1. Player.java 添加 isAI 字段
> 2. AIDecision.java 实现 AI 决策
> 3. DiceController.java 自动掷骰
> 4. Land/HotelLand/ShopLand AI 适配
> 5. MainMap.java AI 模式
> 6. GameMenu.java 模式选择
> 7. AIDev.md 文档记录

### Prompt 3（Claude 编译调试）
> 修复 `land.getTax()` → `land.tax` 编译错误
> javac 全量编译通过

---

---

## 八、第二轮迭代：用素材图片替换 JOptionPane 模式选择

### Prompt 记录

**用户 Prompt：**
> img\gameMenu有三张png图片,23,24,choice，放在点击开始游戏之后，
> 点击23后是正常的本地双人游戏，24则是与AI对战，不要有两个AI对战的选项，
> 这个对话也要记录在AIDev

### 需求分析
1. 用项目已有的三张图片替换之前的 `JOptionPane` 弹窗
2. `choice.png`（500×460）— 模式选择背景
3. `23.png`（224×307）— 双人对战按钮
4. `24.png`（224×307）— 人机对战按钮
5. 不提供"AI vs AI"选项（只有双人、人机两种）

### 实现方案

| 文件 | 修改 |
|---|---|
| `ModeSelectPanel.java` | **新增**：模式选择面板，背景 choice.png + 两个按钮 23/24 |
| `LeaderAnim.java` | `initGameCard()` 中注册 ModeSelectPanel 到 CardLayout |
| `GameMenu.java` | "开始游戏" 改为 `cardLayout.show("模式选择")` 而非 JOptionPane |

### UI 布局计算
```
窗口: 500 × 460
23.png + 24.png = 224 + 224 = 448
三等分间距: (500 - 448) / 3 ≈ 17
btn23: x=17,  y=(460-307)/2=77, w=224, h=307
btn24: x=258, y=77,                 w=224, h=307
```

### 用户交互流程
```
主菜单 → 点击"开始游戏"
       → CardLayout 切换到 ModeSelectPanel
         ├─ 点击 23.png（双人对战）→ new MainMap(false) → 开始游戏
         ├─ 点击 24.png（人机对战）→ new MainMap(true)  → 开始游戏
         └─ 点返回按钮 → 回到主菜单
```

### 编译结果
- 全量编译通过，零错误

---

---

## 十、第三轮迭代：AI 策略深度优化 + 路障弹窗修复

### Prompt 记录

**用户原始 Prompt（逐字）：**
> using superpowers, 继续迭代优化,2. **路障策略优化**：放在自己的高级地产前方让对手踩中交税
>   3. **地雷策略优化**：预判对手路径，在关键位置埋雷
>   4. **身份证使用**：在即将经过高风险区域时使用,同时在使用路障时候，会出现选择索引的选项，不要这个选项，记录到AIDev中，还有要把我的原始问题放上去，不要简写

### 需求拆解

| # | 需求 | 说明 |
|---|------|------|
| 1 | 路障弹窗修复 | AI 使用路障时 JFrame 弹窗要求选索引，AI 应自动选 |
| 2 | 路障策略优化 | 放在自己高级地产前方 1~3 格拦截对手，迫使交税 |
| 3 | 地雷策略优化 | 预判对手路径：紧随 ≤6 格必埋；高频格（Chance/Casino/Event）必埋；自己高级地产前方必埋 |
| 4 | 身份证策略 | 前方 6 格有 2+ 对手高级地产 或 预计税费 >2000 时使用 20 面骰跳过 |

### 实现方案

#### 核心问题：Barrier.isUsed() 弹窗

原代码每次调用都会弹出 JFrame + JComboBox 让用户选择索引（0-29）。人类玩家需要这个 UI，但 AI 应自动决定。

**解决思路**：在 Barrier 新增 `aiTargetIndex` 字段，Player 通过 `getBarrier()` 预设该字段，`isUsed()` 检测到预设值后跳过弹窗直接放置。

#### 调用链

```
MainMap.startAITurn()
  ├─ AIDecision.decidePropBeforeRoll() → 返回 "路障"
  ├─ AIDecision.decideBarrierIndex()   → 返回最佳 tile 索引
  ├─ ai.setBarrierAiTargetIndex(idx)
  └─ ai.use("路障", ai)
       └─ Player.getBarrier()
            ├─ barrierAiTargetIndex != null → b.setAiTargetIndex(idx)
            └─ Barrier.isUsed()
                 ├─ aiTargetIndex != null → 跳过 JFrame → onPlace.test(idx) ✓
                 └─ aiTargetIndex == null → 原有 JFrame 弹窗（人类玩家）
```

#### 修改清单

| 文件 | 修改 |
|------|------|
| `Barrier.java` | 新增 `aiTargetIndex` 字段 + `setAiTargetIndex()`；`isUsed()` 开头检测预设值跳过弹窗 |
| `Player.java` | 新增 `barrierAiTargetIndex` 字段 + setter；`getBarrier()` 读取并传递 |
| `AIDecision.java` | 新增 `decideBarrierIndex()`（3 个策略方法）；`shouldPlaceMine()`；`shouldUseIdCard()`；`decidePropBeforeRoll()` 重排优先级 |
| `MainMap.java` | `startAITurn()` 中为路障计算索引并预设 |

### AI 策略详细设计

#### 路障策略 `decideBarrierIndex()`

```
遍历 AI 拥有的 level ≥ 2 地产：
  对每个地产，取前方 1~3 格（逆时针，即索引 -1~-3）
  
候选位过滤：
  ✗ 已有路障 | 地雷
  ✗ Prison | Hospital | Start

评分公式：地产税率 × 10 + (4 - offset) × 100
选最高分返回
```

#### 地雷策略 `shouldPlaceMine()`

```
触发条件（任一满足即埋）：
  1. 对手在后方 ≤6 格内（马上踩到）
  2. 当前格是高频位置（Chance / Event / Casino / Shop）
  3. 当前格在自己高级地产前方 1~3 格
```

注意：地雷始终埋在 AI 当前脚下（与人类规则一致），优化的是"何时触发埋雷"的决策逻辑。

#### 身份证策略 `shouldUseIdCard()`

```
扫描 AI 前方 6 格：
  统计对手拥有的 level ≥ 3 地产数量
  累加预计税费
  检测 Casino 存在

触发（任一满足）：
  ✓ 危险格 ≥ 2 个
  ✓ 预计税费 > 2000
```

#### 道具优先级（调整后）

```
包子(HP≤30) > 身份证(高风险区) > 地雷(预判路径) > 路障(高级地产前)
  > 升级卡 > 偷取 > 考试周 > 万能骰子
```

身份证从"不使用"提升到第二优先级。

### 迭代日志（第三轮）

#### 第 8 轮：Barrier.java AI 预设索引
- 新增 `private Integer aiTargetIndex = null`
- 新增 `setAiTargetIndex(int index)` setter
- `isUsed()` 开头检测：若 `aiTargetIndex != null`，跳过 JFrame，直接调 `onPlace.test(index)`

#### 第 9 轮：Player.java 路障索引传递
- 新增 `private Integer barrierAiTargetIndex = null`
- 新增 `setBarrierAiTargetIndex(int index)` setter
- `getBarrier()` 中：若 `barrierAiTargetIndex != null`，调 `b.setAiTargetIndex()` 注入

#### 第 10 轮：AIDecision.java 三大策略
- 新增 `decideBarrierIndex()` — 在自己最高级地产前方 1~3 格选最佳位置
- 新增 `shouldPlaceMine()` — 预判对手路径（紧随/高频格/高级地产前）
- 新增 `shouldUseIdCard()` — 前方 6 格风险扫描
- `decidePropBeforeRoll()` 重构：新优先级 + 调用三个新方法

#### 第 11 轮：MainMap.java 集成
- `startAITurn()` 新增路障处理分支：`AIDecision.decideBarrierIndex()` → `ai.setBarrierAiTargetIndex()`

### 编译验证（第三轮）
- 全量编译通过，零错误

---

---

## 十二、第四轮迭代：AI 弹窗自动关闭 + 赌场/超市/旅馆自主操作

### Prompt 记录

**用户原始 Prompt：**
> use superpower,现在的AI有几个问题，进入超市，赌场，医院无法自主操作，立刻完善，同时弹窗弹出AI无法自主关闭，我需要可以自主关闭它，记得记录在AIDev

**后续讨论：**
> 加入赌场的自主操作，但是我希望可以看到弹珠落下操作，可以实现吗
> → 可以，弹珠动画自带自动关闭，跳过其他阻塞弹窗即可
>
> 可以，但是我希望可以看到信息怎么办
> → 方案A：屏幕日志面板（后续做）/ 方案B：自动消失提示
>
> 算了，先做B吧，A等到我之后连接数据库之后再做

### 需求拆解

| # | 需求 | 说明 |
|---|------|------|
| 1 | 赌场 AI 自主操作 | 自动下注、自动玩 3 把、保留弹珠动画、跳过所有阻塞弹窗 |
| 2 | 超市 AI 弹窗自动关闭 | 购买结果弹窗 1.5 秒自动消失 |
| 3 | 旅馆(医院) AI 弹窗自动关闭 | 支付/骰子结果弹窗 1.5 秒自动消失 |
| 4 | 全局 AI 弹窗自动关闭 | 买地/升级/交税/路障/地雷/坐牢等所有 AI 弹窗 1.5 秒自动消失 |

### 核心方案：`showAIMessage()` 自动消失弹窗

```java
// AIDecision.java
public static void showAIMessage(String msg) {
    JOptionPane pane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE);
    JDialog dialog = pane.createDialog(null, "AI 操作");
    Timer timer = new Timer(1500, e -> dialog.dispose());
    timer.setRepeats(false);
    timer.start();
    dialog.setVisible(true);  // 阻塞直到 timer 关闭
}
```

人类玩家可以看到 AI 在做什么，但 1.5 秒后自动关闭，不需要手动点击。

**注意**：人类玩家的所有弹窗保持不变（`JOptionPane.showMessageDialog` / `showOptionDialog` / `showInputDialog`）。

### AI 赌场策略 `playCasinoAI()`

```
进入赌场 → Log 记录 + 自动消失弹窗提示
  ↓
第 1/2/3 把循环：
  ├─ 自动下注：max(100, 资金/10)
  ├─ 资金不足 → 退出
  ├─ 🎰 弹珠动画（保留！PachinkoPanel 自动播放 110 帧后 dispose）
  ├─ 中奖/未中 → Log 记录 + 自动消失弹窗
  └─ 自动下一把（不询问）
```

### 修改清单

| 文件 | 修改 |
|------|------|
| **AIDecision.java** | 新增 `showAIMessage(String)` 和 `showAIMessage(Component, String, String)` 两个自动消失弹窗方法 |
| **Casino.java** | `onPlayerArrive()` 新增 AI 分支 → 调用 `playCasinoAI()`：自动下注、保留弹珠动画、结果自动消失弹窗 |
| **ShopLand.java** | AI 购买成功弹窗 2 处：`JOptionPane.showMessageDialog` → `AIDecision.showAIMessage` |
| **HotelLand.java** | AI 支付/骰子弹窗 2 处：`JOptionPane.showMessageDialog` → `AIDecision.showAIMessage` |
| **Land.java** | AI 买地弹窗(1处)、AI 升级弹窗(1处)、AI 交税弹窗(1处) → `AIDecision.showAIMessage` |
| **MainMap.java** | `hitBarrier()` 踩路障、`checkMine()` 踩地雷、`nextPlayer()` 坐牢(3处) → AI 时自动消失弹窗；同时新增 `LogPanel` 屏幕日志面板 |
| **LogPanel.java** | 用户新增：屏幕日志面板，实时显示 AI 决策过程 |

### 迭代日志

#### 第 12 轮：AIDecision.java 自动消失弹窗
- 新增 `showAIMessage(String msg)` — 默认标题 "AI 操作"，1.5 秒自动关闭
- 新增 `showAIMessage(Component parent, String title, String msg)` — 可指定父组件和标题

#### 第 13 轮：Casino.java AI 赌场
- `onPlayerArrive()` 新增 `if (player.isAI())` 分支
- 新增 `playCasinoAI(Player ai)` 私有方法：自动下注 3 把、保留弹珠动画、结果自动消失弹窗
- 新增 `import main.AIDecision`、`import debug.Log`

#### 第 14 轮：ShopLand/HotelLand/Land AI 弹窗替换
- **ShopLand.java**：AI 购买结果 2 处弹窗改为 `AIDecision.showAIMessage`
- **HotelLand.java**：AI 支付/骰子 2 处弹窗改为 `AIDecision.showAIMessage`
- **Land.java**：AI 买地/升级/交税 3 处弹窗改为 `AIDecision.showAIMessage`；交税弹窗新增 AI 判断

#### 第 15 轮：MainMap.java 全局 AI 弹窗
- `hitBarrier()`：新增 `if (player.isAI())` → 自动消失弹窗
- `checkMine()`：新增 `if (player.isAI())` → 自动消失弹窗
- `nextPlayer()`：坐牢 3 处弹窗新增 AI 判断
- 新增 `LogPanel logPanel` 字段（用户自行添加屏幕日志面板）

### 编译验证（第四轮）
- 全量编译通过，零错误

---

## 十三、第五轮迭代：升级卡弹窗修复 + AI 回合禁用道具点击

### Prompt 记录

**用户原始 Prompt：**
> 升级卡窗口无法正常关闭,同时当到了AI回合时候，我仍旧可以点击道具，立刻修复这个问题

### 需求拆解

| # | 问题 | 根因 | 修复 |
|---|------|------|------|
| 1 | 升级卡弹窗无法关闭 | `HouseLevelUp.isUsed()` 中 3 处 `JOptionPane.showMessageDialog` 对 AI 同样阻塞 | AI 时改为 `AIDecision.showAIMessage` 自动消失 |
| 2 | AI 回合可点击道具 | `addPropsListener` 的 `mouseClicked` 没有校验当前玩家是否为 AI | 开头加 `if (getCurrentPlayer().isAI()) return;` |

### 修改清单

| 文件 | 修改 |
|------|------|
| **HouseLevelUp.java** | `isUsed()` 中 3 处弹窗（无地产/无可升级/升级成功）加 `target.isAI()` 分支 → `AIDecision.showAIMessage` |
| **MainMap.java** | `addPropsListener()` 的 `mouseClicked` 开头加 AI 拦截：`if (getCurrentPlayer().isAI()) return;` |

### 迭代日志

#### 第 16 轮：HouseLevelUp.java AI 弹窗修复
- 新增 `import main.AIDecision`
- 无地产弹窗：AI → `showAIMessage`，人类 → 保留原弹窗
- 无可升级弹窗：同上
- 升级成功弹窗：同上

#### 第 17 轮：MainMap.java AI 回合道具拦截
- `mouseClicked` 开头新增 AI 回合守卫，人类无法在 AI 回合点击道具

### 编译验证（第五轮）
- 全量编译通过，零错误

---

## 十一、后续可能的优化方向

1. **AI 难度分级**：简单/普通/困难，调整决策概率阈值
2. **AI vs AI 观战模式**：两个 AI 自动对战
3. **决策日志面板**：在 UI 上实时显示 AI 的决策过程（LogPanel 已添加）
4. **地雷位置扩展**：允许 AI 像路障一样在任意位置埋雷（需改 Mine 道具机制）
5. **路障连招**：路障 + 地雷组合，路障截停后正好踩地雷
6. **数据库连接**：持久化游戏记录、排行榜
