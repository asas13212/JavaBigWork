package props;

import main.Player;

import javax.swing.*;

/**
 * 功能描述：万能骰子道具，选择下一次投掷的点数
 * @author cyt &amp; Claude
 * @date 2026/6/1 0:00
 */
public class Dice extends Prop
{

    /**
     * 功能描述：构造万能骰子道具，设置名称、描述和价格
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    public Dice()
    {
        this.setDescription("选择下一次投掷的点数");
        this.setName("万能骰子");
        this.setPrice(1500);
    }

    /**
     * 功能描述：使用万能骰子，选择下一次投掷的点数；AI由MainMap预设点数，直接返回
     * @param target 目标玩家
     * @return 是否使用成功
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    @Override
    public boolean isUsed(Player target)
    {
        // AI: 已在 MainMap.startAITurn() 中预设点数，直接返回 true
        if (target.isAI()) return target.hasNextDiceValue();

        String[] choices = {"1点", "2点", "3点", "4点", "5点", "6点"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "选择下一次投掷的点数：",
                "万能骰子",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                choices,
                choices[0]
        );

        if (choice < 0) return false;

        target.setNextDiceValue(choice + 1);
        JOptionPane.showMessageDialog(null, "已锁定下次投掷为 " + (choice + 1) + " 点！");
        return true;
    }
}
