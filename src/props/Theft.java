package props;

import main.Player;

public class Theft extends Prop
{

    public Theft()
    {
        this.setDescription("偷取对方百分之十的金钱");
        this.setPrice(1000);
        this.setName("偷取");
    }

    @Override
    public void isUsed(Player target)
    {

    }
}
