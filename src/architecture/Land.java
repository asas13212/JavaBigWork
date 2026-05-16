package architecture;

import main.*;
import java.awt.*;

public class Land extends Tile
{
    private Player owner;

    private int houseLevel;

    private int oneLevelPrice;

    private int twoLevelPrice;

    private int price;


    public Land(int positionIndex, TileType tileType, Point position, String name)
    {
        super(positionIndex, tileType, position, name);
        this.houseLevel = 0;
    }

    @Override
    public void onPlayerArrive(Player player)
    {
        if (this.owner == null)
        {
            System.out.println("可以购买该地产");
            player.BuyLand(this);
            player.moneyDecrease(price);
        }else if ( owner != player){
            System.out.println("不是自己地产，应该交租了");
        }else {
            System.out.println("到了自己地产");
        }
    }


    void landLevelUp(Player owner)
    {
        if(houseLevel == ConstantNum.MAX_LEVEL )
            return;

        if (houseLevel == 0)
        {
            owner.moneyDecrease(oneLevelPrice);
            houseLevel++;
        }else if (houseLevel == 1)
        {
            owner.moneyDecrease(twoLevelPrice);
            houseLevel++;
        }

    }
}
