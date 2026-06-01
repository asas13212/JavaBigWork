package main;

import architecture.Land;
import architecture.Tile;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * 功能描述：AI 决策类 —— 包含 AI 玩家的所有决策逻辑
 * 决策策略：综合评估金钱、血量、位置和对手状态，做出合理的游戏决策
 * @author cyt & Claude
 * @date 2026/6/1
 */
public class AIDecision
{
    private static final Random RNG = new Random();

    //<editor-fold desc="土地购买与升级决策">

    /**
     * 功能描述：AI 决定是否购买空地 策略：价格合理 + 预留足够资金（至少保留 30% 初始资金）
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public static boolean shouldBuyLand(Player ai, Land land)
    {
        if (land.getOwner() != null) return false;

        int price = land.getCurrentPrice();
        int minReserve = ConstantNum.PLAYER_MONEY / 3; // 保留 ~3333

        // 基础判断：买得起且有安全余额
        if (ai.getMoney() < price + minReserve) return false;

        // 高价值地块（税率高的）更值得买
        int maxTax = land.tax[land.tax.length - 1];
        if (maxTax >= 3000) return true;   // 高价值必买

        // 普通地块：80% 概率购买（增加一些随机性）
        return RNG.nextInt(100) < 80;
    }

    /**
     * 功能描述：AI 决定是否升级已有地产
     * 策略：升级后仍保留 20% 初始资金
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public static boolean shouldUpgradeLand(Player ai, Land land)
    {
        if (land.getOwner() != ai) return false;
        if (!land.canLevelUp()) return false;

        int price = land.getCurrentPrice();
        int minReserve = ConstantNum.PLAYER_MONEY / 5; // 保留 ~2000

        if (ai.getMoney() < price + minReserve) return false;

        // 升级到高级别时更谨慎
        int level = land.getHouseLevel();
        if (level >= 3 && ai.getMoney() < price + ConstantNum.PLAYER_MONEY / 2)
            return RNG.nextInt(100) < 60;

        return RNG.nextInt(100) < 85;
    }

    //</editor-fold>

    //<editor-fold desc="道具使用决策">

    /**
     * 功能描述：AI 在掷骰前决定使用哪个道具（返回道具名，null 表示不使用）
     * 策略优先级：包子(低血) > 身份证(高风险区) > 地雷(预判路径) > 路障(高级地产前) > 升级卡 > 偷取 > 考试周 > 万能骰子
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public static String decidePropBeforeRoll(Player ai, Player[] allPlayers, Tile[] tiles)
    {
        var props = ai.getProps();
        Player opponent = ai.getOtherPlayer();

        // 1. 血量低时优先吃包子
        if (ai.getHp() <= 30 && props.containsKey("包子"))
            return "包子";

        // 2. 身份证：前方 6 格有高风险区域（对手高级地产集群 / Casino）
        if (props.containsKey("身份证") && shouldUseIdCard(ai, tiles))
            return "身份证";

        // 3. 地雷：预判对手路径，在关键位置埋雷
        if (props.containsKey("地雷"))
        {
            Tile currentTile = tiles[ai.getPositionIndex()];
            if (!currentTile.hasMine() && shouldPlaceMine(ai, opponent, tiles))
                return "地雷";
        }

        // 4. 路障：放在自己高级地产前方拦截对手收税
        if (props.containsKey("路障"))
        {
            int barrierIdx = decideBarrierIndex(ai, tiles);
            if (barrierIdx >= 0)
                return "路障";
        }

        // 5. 升级卡：升级自己最高级但未满级的地产
        if (props.containsKey("升级卡"))
        {
            if (ai.getProperty() > 0 && ai.getMoney() > 1500)
                return "升级卡";
        }

        // 6. 偷取：对手钱多时偷
        if (props.containsKey("偷取") && opponent.getMoney() > ai.getMoney() + 2000)
            return "偷取";

        // 7. 考试周：对手血量低时使用
        if (props.containsKey("考试周") && opponent.getHp() <= 50)
            return "考试周";

        // 8. 万能骰子：需要精确步数时使用（前面 6 步内有自己的高级地产可收税）
        if (props.containsKey("万能骰子"))
        {
            for (int step = 1; step <= 6; step++)
            {
                int idx = (ai.getPositionIndex() + step) % tiles.length;
                if (tiles[idx] instanceof Land land
                        && land.getOwner() == ai
                        && land.getHouseLevel() >= 3)
                {
                    return "万能骰子";
                }
            }
        }

        return null;
    }

    /**
     * 功能描述：路障策略优化：找到自己最高级地产，在其前方 1~3 格放路障拦截对手
     * 返回最佳 tile 索引，-1 表示没有合适位置
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public static int decideBarrierIndex(Player ai, Tile[] tiles)
    {
        int bestIdx = -1;
        int bestScore = 0;

        // 遍历 AI 拥有的地产
        for (int i = 0; i < ai.getLandOwnedCount(); i++)
        {
            Land land = ai.getLandOwned(i);
            if (land == null || land.getHouseLevel() < 2) continue;

            int landIdx = land.getPositionIndex();
            int taxRate = land.tax[land.getHouseLevel()];

            // 在地产前方 1~3 格（逆时针，即索引减小方向）找候选位
            for (int offset = 1; offset <= 3; offset++)
            {
                int candidate = (landIdx - offset + tiles.length) % tiles.length;
                Tile t = tiles[candidate];

                // 过滤：不能已有路障/地雷，不能是特殊格子
                if (t.hasBarrier() || t.hasMine()) continue;
                if (t instanceof architecture.Prison || t instanceof architecture.Hospital) continue;
                if (t instanceof architecture.Start) continue;

                // 评分：税率 × 10 + 越靠近地产分越高
                int score = taxRate * 10 + (4 - offset) * 100;
                if (score > bestScore)
                {
                    bestScore = score;
                    bestIdx = candidate;
                }
            }
        }

        return bestIdx;
    }

    /**
     * 功能描述：地雷策略优化：预判对手路径，在关键位置埋雷
     * 埋雷位置 = AI 当前脚下，决策的是"何时埋"：
     *   1. 对手紧随其后 ≤6 格 → 必埋
     *   2. 当前格是高频位置（Chance/Event/Casino/商店入口/自己高级地产前）→ 埋
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    private static boolean shouldPlaceMine(Player ai, Player opponent, Tile[] tiles)
    {
        int aiIdx = ai.getPositionIndex();
        int oppIdx = opponent.getPositionIndex();

        // 对手在后方 6 格内（马上要踩到）
        int dist = (aiIdx - oppIdx + tiles.length) % tiles.length;
        if (dist > 0 && dist <= 6)
            return true;

        // 当前格是高频位置
        Tile current = tiles[aiIdx];
        if (current instanceof architecture.Chance
                || current instanceof architecture.Event
                || current instanceof architecture.Casino
                || current instanceof architecture.Shop)
            return true;

        // 当前格在自己高级地产前方 1~3 格
        for (int i = 0; i < ai.getLandOwnedCount(); i++)
        {
            Land land = ai.getLandOwned(i);
            if (land == null || land.getHouseLevel() < 2) continue;
            int landIdx = land.getPositionIndex();
            for (int offset = 1; offset <= 3; offset++)
            {
                int frontIdx = (landIdx - offset + tiles.length) % tiles.length;
                if (frontIdx == aiIdx) return true;
            }
        }

        return false;
    }

    /**
     * 功能描述：身份证策略：扫描前方 6 格，检测高风险区域（对手高级地产集群 / Casino）
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    private static boolean shouldUseIdCard(Player ai, Tile[] tiles)
    {
        Player opponent = ai.getOtherPlayer();
        int dangerCount = 0;
        int totalTax = 0;

        for (int step = 1; step <= 6; step++)
        {
            int idx = (ai.getPositionIndex() + step) % tiles.length;
            Tile t = tiles[idx];

            // 对手高级地产（level >= 3）
            if (t instanceof Land land
                    && land.getOwner() == opponent
                    && land.getHouseLevel() >= 3)
            {
                dangerCount++;
                totalTax += land.tax[land.getHouseLevel()];
            }

            // 赌场也是高风险
            if (t instanceof architecture.Casino)
            {
                dangerCount++;
            }
        }

        // 触发条件：2+ 个危险格，或预计税费 > 2000
        return dangerCount >= 2 || totalTax > 2000;
    }

    /**
     * 功能描述：AI 决定道具的使用目标
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public static Player decidePropTarget(Player ai, String propName, Player[] allPlayers)
    {
        return switch (propName)
        {
            case "地雷", "路障", "升级卡", "包子", "身份证" -> ai;      // 对自己使用
            case "偷取", "考试周" -> ai.getOtherPlayer();               // 对对手使用
            case "万能骰子" -> RNG.nextBoolean() ? ai : ai.getOtherPlayer();
            default -> ai;
        };
    }

    //</editor-fold>

    //<editor-fold desc="商店与旅馆决策">

    /**
     * 功能描述：AI 决定在商店购买哪个道具（返回索引，-1 表示不买）
     * 优先级：地雷 > 路障 > 升级卡 > 偷取 > 考试周 > 万能骰子 > 包子 > 身份证
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public static int chooseShopItem(Player ai, boolean isOwner)
    {
        // 道具优先级顺序（名字匹配 ShopLand 中的 ITEMS 顺序）
        // BaoZi=0, ExamWeek=1, Mine=2, Barrier=3, Theft=4, Dice=5, HouseLevelUp=6, IdCard=7
        int[] priority = {2, 3, 6, 4, 1, 5, 0, 7}; // Mine > Barrier > Upgrade > Theft > ExamWeek > Dice > BaoZi > IdCard

        for (int idx : priority)
        {
            String propName = switch (idx)
            {
                case 0 -> "包子";
                case 1 -> "考试周";
                case 2 -> "地雷";
                case 3 -> "路障";
                case 4 -> "偷取";
                case 5 -> "万能骰子";
                case 6 -> "升级卡";
                case 7 -> "身份证";
                default -> null;
            };

            int price;
            if (isOwner) {
                // 八折
                price = getPropPrice(propName) * 4 / 5;
            } else {
                price = getPropPrice(propName);
            }

            if (ai.getMoney() >= price + 1000)
                return idx;
        }

        return -1; // 买不起任何东西
    }

    /**
     * 功能描述：获取道具原价
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    private static int getPropPrice(String name)
    {
        return switch (name)
        {
            case "包子" -> 500;
            case "考试周" -> 800;
            case "地雷" -> 600;
            case "路障" -> 600;
            case "偷取" -> 1000;
            case "万能骰子" -> 800;
            case "升级卡" -> 1200;
            case "身份证" -> 1500;
            default -> 999;
        };
    }

    /**
     * 功能描述：AI 决定是否在旅馆消费
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public static boolean shouldUseHotel(Player ai, boolean isOwner)
    {
        if (isOwner) return true; // 免费，必用
        return ai.getMoney() >= 2000; // 留足余额才消费
    }

    //</editor-fold>

    //<editor-fold desc="万能骰子数值选择">

    /**
     * 功能描述：AI 选择万能骰子的点数（1~20，通常选 6 最稳，或选能走到目标格子的步数）
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public static int chooseDiceValue(Player ai, Tile[] tiles)
    {
        // 优先走到自己的高级地产
        for (int step = 1; step <= 6; step++)
        {
            int idx = (ai.getPositionIndex() + step) % tiles.length;
            if (tiles[idx] instanceof Land land
                    && land.getOwner() == ai
                    && land.getHouseLevel() >= 3)
            {
                return step;
            }
        }

        // 避免踩到对手的高级地产
        for (int step = 1; step <= 6; step++)
        {
            int idx = (ai.getPositionIndex() + step) % tiles.length;
            if (tiles[idx] instanceof Land land
                    && land.getOwner() == ai.getOtherPlayer()
                    && land.getHouseLevel() >= 3)
            {
                // 尝试跳过它
                return Math.min(step + 1, 6);
            }
        }

        // 默认6步
        return 6;
    }

    //</editor-fold>

    //<editor-fold desc="升级卡目标选择">

    /**
     * 功能描述：AI 选择要升级的地产（返回地产索引，-1 表示不升级）
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public static int chooseLandToUpgrade(Player ai)
    {
        int bestIdx = -1;
        int bestTaxGain = 0;

        for (int i = 0; i < ai.getLandOwnedCount(); i++)
        {
            Land land = ai.getLandOwned(i);
            if (land == null || !land.canLevelUp()) continue;

            int currentLevel = land.getHouseLevel();
            int nextLevel = currentLevel + 1;
            if (nextLevel >= land.tax.length) continue;

            // 税率增幅越大越优先
            int taxGain = land.tax[nextLevel] - land.tax[currentLevel];
            if (taxGain > bestTaxGain)
            {
                bestTaxGain = taxGain;
                bestIdx = i;
            }
        }

        return bestIdx;
    }

    //</editor-fold>

    //<editor-fold desc="AI 弹窗辅助">

    /**
     * 功能描述：AI 自动消失弹窗：显示 1.5 秒后自动关闭，不阻塞后续流程
     * 人类玩家可以看到 AI 在做什么，但不需要手动点击关闭
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public static void showAIMessage(String msg)
    {
        showAIMessage(null, "AI 操作", msg);
    }

    /**
     * 功能描述：AI 自动消失弹窗（带标题），显示 1.5 秒后自动关闭
     * @param parent 父组件
     * @param title 弹窗标题
     * @param msg 消息内容
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public static void showAIMessage(Component parent, String title, String msg)
    {
        JOptionPane pane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(parent, title);
        // 1.5 秒后自动关闭
        Timer timer = new Timer(1500, e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();
        dialog.setVisible(true);
    }

    //</editor-fold>
}
