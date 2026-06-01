package architecture;

import debug.Log;
import main.AIDecision;
import main.Player;
import props.Mine;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * 功能描述：事件格子，包含14种随机事件，千分之一概率触发金钱翻倍大奖，支持AI自动处理
 * @author cyt & Claude
 * @date 2026/6/1
 */
public class Event extends Tile
{

    Timer walkTimer;

    /**
     * 功能描述：构造事件格子
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
    public Event(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.EVENT;

    }

    /**
     * 功能描述：玩家到达事件格，随机触发14种事件之一，AI自动处理
     * @param player 到达的玩家
     * @author cyt & Claude
     */
    @Override
    public void onPlayerArrive(Player player)
    {
        boolean isAI = player.isAI();

        Random rareRand = new Random();
        if (rareRand.nextInt(0, 1000) == 99)
        {
            String msg = "千分之一概率中大奖，你真幸运!\n金钱翻倍";
            if (isAI) AIDecision.showAIMessage(player.getName() + " " + msg);
            else JOptionPane.showMessageDialog(null, msg, "奇迹!!!", JOptionPane.INFORMATION_MESSAGE);
            player.moneyIncrease(player.getMoney());
            return;
        }

        Random rand = new Random();
        int num = rand.nextInt(0, 14) + 1;
        switch (num)
        {
            case 1 ->
            {
                Log.info(player.getName() + " 触发事件：车祸 —— 金钱减25%");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 被车创飞了，钱被抢走一半，还了一半");
                else JOptionPane.showMessageDialog(null, "你被车创飞了，包里的钱撒了一地，村口大爷大妈抢走了一半\n但是看在你太惨 ,还了一半给你", "噩耗突传", JOptionPane.INFORMATION_MESSAGE);
                player.moneyDecrease((int) (player.getMoney() * 0.25));
            }
            case 2 ->
            {
                Log.info(player.getName() + " 触发事件：肚子痛 —— 停止一回合");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 肚子太痛了，停止一回合");
                else JOptionPane.showMessageDialog(null, "肚子太痛了，需要进厕所\n停止一回合", "噩耗突传", JOptionPane.INFORMATION_MESSAGE);
                player.setBarrierStopTurns(1);
            }
            case 3 ->
            {
                int value = player.rollDice();
                Log.info(player.getName() + " 触发事件：顺风车 —— 向前 " + value + " 步");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 搭乘顺风车，向前走 " + value + " 步");
                else JOptionPane.showMessageDialog(null, "搭乘顺风车赶路，向前走 " + value + " 步", "一般般吧", JOptionPane.INFORMATION_MESSAGE);
                player.startWalk(value);
            }
            case 4 ->
            {
                Log.info(player.getName() + " 触发事件：中彩票 —— +$3000");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 买彩票中了3000块！");
                else JOptionPane.showMessageDialog(null, "买彩票中了3000块!!!\n金钱增加3000", "喜从天降", JOptionPane.INFORMATION_MESSAGE);
                player.moneyIncrease(3000);
            }
            case 5 ->
            {
                Log.info(player.getName() + " 触发事件：股票亏损 —— -$2000");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 买股票亏了2000");
                else JOptionPane.showMessageDialog(null, "买股票把裤衩子都亏没了\n金钱减2000", "噩耗突传", JOptionPane.INFORMATION_MESSAGE);
                player.moneyDecrease(2000);
            }
            case 6 ->
            {
                Log.info(player.getName() + " 触发事件：扶老奶奶 —— +$1500");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 扶老奶奶过马路，获得1500！");
                else JOptionPane.showMessageDialog(null, "扶老奶奶过马路，奶奶给了你1500!\n金钱增加1500", "你是个好人!", JOptionPane.INFORMATION_MESSAGE);
                player.moneyIncrease(1500);
            }
            case 7 ->
            {
                Log.info(player.getName() + " 触发事件：被讹 —— -$500");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 扶老奶奶被讹了500");
                else JOptionPane.showMessageDialog(null, "扶倒地老奶奶起来，奶奶拉着你的肩膀\n并且向你发起了转账\n金钱减500", "好人没好报啊(哭)", JOptionPane.INFORMATION_MESSAGE);
                player.moneyDecrease(500);
            }
            case 8 ->
            {
                Log.info(player.getName() + " 触发事件：重返高中 —— HP-20，停止一回合");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 重返高中，HP-20，停止一回合");
                else JOptionPane.showMessageDialog(null, "回到高中了，你心血来潮，准备回去体验高中生活\n生命减20\n停止一回合", "三好学生", JOptionPane.INFORMATION_MESSAGE);
                player.hpDecrease(20);
                player.setBarrierStopTurns(1);
            }
            case 9 ->
            {
                Log.info(player.getName() + " 触发事件：520红包 —— +$1314");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 收到520红包，+1314！");
                else JOptionPane.showMessageDialog(null, "5.20到了，微信某个陌生人给你转了1314，你大为震撼\n金钱增加1314", "你是个好人", JOptionPane.INFORMATION_MESSAGE);
                player.moneyIncrease(1314);
            }
            case 10 ->
            {
                Log.warn(player.getName() + " 触发事件：食物中毒 —— 每回合 HP-3");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 食物中毒了，每回合掉3HP");
                else JOptionPane.showMessageDialog(null, "你吃了过期的罐头，食物中毒了\n每回合掉3生命", "噩耗突传", JOptionPane.INFORMATION_MESSAGE);
                player.setInToxic(true);
            }
            case 11 ->
            {
                Log.info(player.getName() + " 触发事件：捡到地雷 —— 地雷卡+1");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 捡到地雷卡+1");
                else JOptionPane.showMessageDialog(null, "路上见到了一个地雷\n地雷卡加一", "卡牌大师", JOptionPane.INFORMATION_MESSAGE);
                player.addProp(new Mine());
            }
            case 12 ->
            {
                Log.info(player.getName() + " 触发事件：大亨投资 —— 随机地产升级");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 房地产大亨投资，随机地产升级");
                else JOptionPane.showMessageDialog(null, "有一个房地产大亨投资了你的地皮\n随机地产等级加一", "房地产大亨", JOptionPane.INFORMATION_MESSAGE);

                // 收集所有未满级的地产
                java.util.List<Land> candidates = new java.util.ArrayList<>();
                for (int i = 0; i < player.getLandOwnedCount(); i++)
                {
                    Land l = player.getLandOwned(i);
                    if (l != null && l.getHouseLevel() < l.getMaxLevel())
                        candidates.add(l);
                }

                if (candidates.isEmpty())
                {
                    if (isAI) AIDecision.showAIMessage(player.getName() + " 没有可升级的地产");
                    else JOptionPane.showMessageDialog(null, "没有可升级的地产，白搭了", "可惜", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                Land land = candidates.get(rand.nextInt(candidates.size()));
                int before = land.getHouseLevel();
                land.houseLevelUp();
                Log.info(player.getName() + " 大亨投资：" + land.getName() + " 等级 " + before + " -> " + land.getHouseLevel());
                if (isAI) AIDecision.showAIMessage(player.getName() + " " + land.getName() + " 升级！" + before + " -> " + land.getHouseLevel());
                else JOptionPane.showMessageDialog(null, land.getName() + " 等级 " + before + " -> " + land.getHouseLevel(), "升级成功!", JOptionPane.INFORMATION_MESSAGE);
            }
            case 13 ->
            {
                Log.info(player.getName() + " 触发事件：学动物叫 —— +$800");
                if (isAI) AIDecision.showAIMessage(player.getName() + " 学动物叫，获得800！");
                else JOptionPane.showMessageDialog(null, "路上有人在大声吆喝:\"学任一一种动物叫，可以有红包拿!!\"\n你尝试了一下，竟然不是骗子\n金钱增加800", "喜从天降", JOptionPane.INFORMATION_MESSAGE);
                player.moneyIncrease(800);
            }
            case 14 ->
            {
                Log.info(player.getName() + " 触发事件：岔路选择");
                if (isAI)
                {
                    // AI 随机选一条路
                    boolean goForest = rand.nextBoolean();
                    if (goForest)
                    {
                        AIDecision.showAIMessage(player.getName() + " 选了树林，获得1000");
                        player.moneyIncrease(1000);
                    }
                    else
                    {
                        AIDecision.showAIMessage(player.getName() + " 选了草地，中毒了！");
                        player.setInToxic(true);
                    }
                }
                else
                {
                    int result = JOptionPane.showOptionDialog(
                            null,
                            "前面出现两条路：右边是树林，左边是草地，你走哪边",
                            "抉择抉择",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[]{"树林", "草地"},
                            "确定"
                    );
                    if (result == 0)
                    {
                        JOptionPane.showMessageDialog(null, "一阵风吹过，获得金钱1000");
                        player.moneyIncrease(1000);
                    } else
                    {
                        JOptionPane.showMessageDialog(null, "草地有条蛇，你被咬了!\n已中毒");
                        player.setInToxic(true);
                    }
                }
            }
        }
    }

}
