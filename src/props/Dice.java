package props;

import main.Player;

public class Dice extends Prop
{

    public Dice()
    {
        this.setDescription("控制下一次骰子投掷点数");
        this.setName("万能骰子");
        this.setPrice(1500);
    }

    @Override
    public void isUsed(Player target)
    {

    }
}
