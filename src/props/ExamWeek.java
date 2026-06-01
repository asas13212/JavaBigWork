package props;

import main.Player;

/**
 * 功能描述：考试周道具，选择任意一个玩家暂停一回合
 * @author cyt
 * @date 2026/6/1 0:00
 */
public class ExamWeek extends Prop
{

    /**
     * 功能描述：构造考试周道具，设置名称、描述和价格
     * @author cyt
     * @date 2026/6/1 0:00
     */
    public ExamWeek()
    {
        this.setName("考试周");
        this.setDescription("选择任意一个玩家暂停一回合");
        this.setPrice(1000);
    }

    /**
     * 功能描述：对目标玩家使用考试周，使其暂停一回合
     * @param target 目标玩家
     * @return 是否使用成功
     * @author cyt
     * @date 2026/6/1 0:00
     */
    @Override
    public boolean isUsed(Player target)
    {
        if (target == null) return false;
        target.setBarrierStopTurns(target.getBarrierStopTurns() + 1);
        return true;
    }
}
