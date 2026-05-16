package architecture;

import main.Player;

import java.awt.*;

public class Chance extends Tile
{

    public Chance(int positionIndex, TileType tileType, Point position, String name)
    {
        super(positionIndex, tileType, position, name);
    }

    @Override
    public void onPlayerArrive(Player player)
    {

    }


}
