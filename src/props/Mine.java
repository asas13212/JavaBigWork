package props;

import main.IsTriggerable;
import main.Player;

public class Mine extends Prop implements IsTriggerable
{
    // 使用目标
    private Player target;

    // 造成的伤害
    private int damage = 40;

    // 放置的位置
    private int positionIndex;

    public Mine()
    {
        this.setName("地雷");
        this.setPrice(2000);
        this.setDescription("在自己脚下埋下地雷，玩家碰到扣" + damage + "生命");
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
