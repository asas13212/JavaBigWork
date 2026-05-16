package architecture;

import main.Player;

import java.awt.*;

public class Hospital extends Tile
{

    public Hospital(int positionIndex, TileType tileType, Point position, String name)
    {
        super(positionIndex, tileType, position, name);
    }

    @Override
    public void onPlayerArrive(Player player)
    {
        player.hpIncrease(50);
        player.moneyDecrease(2000);
    }

}
