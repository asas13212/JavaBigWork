package props;

import main.AIDecision;
import main.Player;

import javax.swing.*;

/**
 * 功能描述：偷取道具，偷取对方百分之十的金钱
 * @author cyt &amp; Claude
 * @date 2026/6/1 0:00
 */
public class Theft extends Prop
{

    /**
     * 功能描述：构造偷取道具，设置名称、描述和价格
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    public Theft()
    {
        this.setDescription("偷取对方百分之十的金钱");
        this.setPrice(1000);
        this.setName("偷取");
    }

    /**
     * 功能描述：使用偷取道具，偷取对方百分之十的金钱；AI自动跳过弹窗
     * @param target 被偷取的玩家
     * @return 是否使用成功
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    @Override
    public boolean isUsed(Player target)
    {
        if (target == null) return false;

        // 约定：MainMap 调用 use("偷取", 对方) 时，target 是"被偷的人"
        Player thief = target.getOtherPlayer();
        if (thief == null) return false;

        int victimMoney = target.getMoney();
        if (victimMoney <= 0)
        {
            if (thief.isAI() || thief.isOnline())
                AIDecision.showAIMessage(thief.getName() + " 想偷但对方没钱");
            else
                JOptionPane.showMessageDialog(null, "对方没有钱可以偷取！");
            return false;
        }

        // 偷 10%（向上取整），至少 1 元，但不超过对方现有金钱
        int amount = (int) Math.ceil(victimMoney * 0.1);
        amount = Math.max(1, amount);
        amount = Math.min(amount, victimMoney);

        target.moneyDecrease(amount);
        thief.moneyIncrease(amount);

        if (thief.isAI() || thief.isOnline())
            AIDecision.showAIMessage(thief.getName() + " 偷取了 " + target.getName() + " $" + amount);
        else
            JOptionPane.showMessageDialog(null,
                    thief.getName() + "偷取了" + target.getName() + "的" + amount + "金钱！");
        return true;
    }
}
