package props;

import main.IsTriggerable;
import main.Player;

import java.util.function.IntPredicate;

public class Mine extends Prop implements IsTriggerable
{
    private int damage = 40;

    // AI: 地雷放置回调，由 MainMap 注入，用于将地雷种到棋盘格子上
    private IntPredicate onPlace;

    public Mine()
    {
        this.setName("地雷");
        this.setPrice(2000);
        this.setDescription("在自己脚下埋下地雷，玩家碰到扣" + damage + "生命");
    }

    // AI: 放置地雷到当前玩家所在格子
    @Override
    public void isUsed(Player target)
    {
        int index = target.getPositionIndex();
        if (onPlace != null)
        {
            onPlace.test(index);
        }
    }

    // AI: 玩家踩中地雷时触发，扣除生命
    @Override
    public void isTriggered(Player target)
    {
        target.hpDecrease(damage);
    }

    // AI: setter 供 Player.use() 注入回调
    public void setOnPlace(IntPredicate onPlace)
    {
        this.onPlace = onPlace;
    }
}
