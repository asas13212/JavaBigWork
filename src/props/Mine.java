package props;

import main.Player;

import java.util.function.IntPredicate;

/**
 * 功能描述：地雷道具，在自己脚下埋下地雷，玩家碰到扣生命
 * @author cyt &amp; Claude
 * @date 2026/6/1 0:00
 */
public class Mine extends Prop
{
    private int damage = 40;

    // AI: 地雷放置回调，由 MainMap 注入，用于将地雷种到棋盘格子上
    private IntPredicate onPlace;

    /**
     * 功能描述：构造地雷道具，设置名称、描述和价格
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    public Mine()
    {
        this.setName("地雷");
        this.setPrice(2000);
        this.setDescription("在自己脚下埋下地雷，玩家碰到扣" + damage + "生命");
    }

    /**
     * 功能描述：放置地雷到当前玩家所在格子
     * @param target 使用地雷的玩家
     * @return 是否使用成功
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    @Override
    public boolean isUsed(Player target)
    {
        int index = target.getPositionIndex();
        if (onPlace != null)
        {
            onPlace.test(index);
        }
        return true;
    }

    /**
     * 功能描述：设置地雷放置回调，供 MainMap 注入，用于将地雷种到棋盘格子上
     * @param onPlace 放置回调函数
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    public void setOnPlace(IntPredicate onPlace)
    {
        this.onPlace = onPlace;
    }
}
