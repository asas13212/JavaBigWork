package props;

import main.Player;

/**
 * 功能描述：身份证道具，变身超人，下一次骰子变为20面骰子
 * @author cyt
 * @date 2026/6/1 0:00
 */
public class IdCard extends Prop
{

    /**
     * 功能描述：构造身份证道具，设置名称、描述和价格
     * @author cyt
     * @date 2026/6/1 0:00
     */
    public IdCard()
    {
        this.setDescription("变身超人，下一次骰子变为20面骰子");
        this.setName("身份证");
        this.setPrice(2000);
    }

    /**
     * 功能描述：对目标玩家使用身份证，下一次骰子变为20面骰子
     * @param target 目标玩家
     * @return 是否使用成功
     * @author cyt
     * @date 2026/6/1 0:00
     */
    @Override
    public boolean isUsed(Player target)
    {
        target.setNextDiceSides(20);
        return true;
    }
}
