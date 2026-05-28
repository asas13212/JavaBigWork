package props;

import main.Player;

public class IdCard extends Prop
{

    public IdCard()
    {
        this.setDescription("变身超人，下一次骰子变为20面骰子");
        this.setName("身份证");
        this.setPrice(2000);
    }

    @Override
    public boolean isUsed(Player target)
    {
        target.setNextDiceSides(20);
        return true;
    }
}
