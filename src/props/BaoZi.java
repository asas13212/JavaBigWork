package props;

import main.Player;

public class BaoZi extends Props
{
    private int recover = 20;

    /**
     * 功能描述：恢复量在创造时候给出
     * @author cyt
     * @date 2026/5/15 12:22
     */
    public BaoZi(int recover)
    {
        this.recover = recover;
    }

    @Override
    public void isUsed(Player target)
    {
        target.hpIncrease(recover);
    }
}
