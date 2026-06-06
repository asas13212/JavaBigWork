package props;

import main.AIDecision;
import main.ConstantNum;
import main.Player;

/**
 * 功能描述：包子道具，食用恢复生命值
 * @author cyt &amp; Claude
 * @date 2026/6/1 0:00
 */
public class BaoZi extends Prop
{
    private int recover = 20;

    /**
     * 功能描述：恢复量在创造时候给出
     * @author cyt
     * @date 2026/5/15 12:22
     */
    public BaoZi()
    {
        this.recover = 20;
        this.setName("包子");
        this.setDescription("香喷喷的包子，食用恢复20生命");
        this.setPrice(1000);
    }

    /**
     * 功能描述：使用包子恢复生命值，生命值已满时AI自动跳过
     * @param target 目标玩家
     * @return 是否使用成功
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    @Override
    public boolean isUsed(Player target)
    {
        if (target.getHp() >= ConstantNum.PLAYER_HP)
        {
            if (target.isAI() || target.isOnline())
                AIDecision.showAIMessage(target.getName() + " 生命值已满，跳过包子");
            else
                javax.swing.JOptionPane.showMessageDialog(null, "生命值已满，无需使用！");
            return false;
        }
        int actualHeal = Math.min(recover, ConstantNum.PLAYER_HP - target.getHp());
        target.hpIncrease(actualHeal);
        return true;
    }
}
