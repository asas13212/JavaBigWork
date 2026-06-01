package props;

import main.ConstantNum;
import main.Player;

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

    @Override
    public boolean isUsed(Player target)
    {
        if (target.getHp() >= ConstantNum.PLAYER_HP)
        {
            javax.swing.JOptionPane.showMessageDialog(null, "生命值已满，无需使用！");
            return false;
        }
        int actualHeal = Math.min(recover, ConstantNum.PLAYER_HP - target.getHp());
        target.hpIncrease(actualHeal);
        return true;
    }
}
