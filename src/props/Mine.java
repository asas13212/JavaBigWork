package props;

import main.IsTriggerable;
import main.Player;

public class Mine extends Props implements IsTriggerable
{
    // 使用目标
    private Player target;

    // 造成的伤害
    private int damage = 50;

    // 放置的位置
    private int positionIndex;

    public Mine(int damage)
    {
        this.damage = damage;
    }


    @Override
    public void isUsed(Player target)
    {

    }

    /**
     * 功能描述：触发时候的方法
     * @author cyt
     * @date 2026/5/15 9:15
     */
    @Override
    public void isTriggered(Player target)
    {
        target.hpDecrease(damage);
    }
}
