package props;

import main.Player;

public class HouseLevelUp extends Prop
{

    public HouseLevelUp()
    {
        this.setDescription("随机一个未升级地产升级");
        this.setPrice(2000);
        this.setName("升级卡");
    }

    @Override
    public void isUsed(Player target)
    {

    }
}
