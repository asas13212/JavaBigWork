package props;

import main.Player;

public class ExamWeek extends Prop
{

    public ExamWeek()
    {
        this.setName("考试周");
        this.setDescription("选择任意一个玩家暂停一回合");
        this.setPrice(1000);
    }

    @Override
    public boolean isUsed(Player target)
    {
        if (target == null) return false;
        target.setBarrierStopTurns(target.getBarrierStopTurns() + 1);
        return true;
    }
}
