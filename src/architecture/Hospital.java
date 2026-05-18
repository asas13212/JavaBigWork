package architecture;

import main.Player;

import java.awt.*;

public class Hospital extends Tile
{

    public Hospital(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
    }

    @Override
    public void onPlayerArrive(Player player)
    {
        player.hpIncrease(50);
        player.moneyDecrease(2000);
    }

}
