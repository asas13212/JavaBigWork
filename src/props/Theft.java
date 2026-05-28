package props;

import main.Player;

import javax.swing.*;

public class Theft extends Prop
{

    public Theft()
    {
        this.setDescription("偷取对方百分之十的金钱");
        this.setPrice(1000);
        this.setName("偷取");
    }

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
            JOptionPane.showMessageDialog(null, "对方没有钱可以偷取！");
            return false;
        }

        // 偷 10%（向上取整），至少 1 元，但不超过对方现有金钱
        int amount = (int) Math.ceil(victimMoney * 0.1);
        amount = Math.max(1, amount);
        amount = Math.min(amount, victimMoney);

        target.moneyDecrease(amount);
        thief.moneyIncrease(amount);

        JOptionPane.showMessageDialog(null,
                thief.getName() + "偷取了" + target.getName() + "的" + amount + "金钱！");
        return true;
    }
}
