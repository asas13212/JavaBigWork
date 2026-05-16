package architecture;

import main.Player;

import java.awt.*;

public class Shop extends Tile
{

    public Shop(int positionIndex, TileType tileType, Point position, String name)
    {
        super(positionIndex, tileType, position, name);
    }

    @Override
    public void onPlayerArrive(Player player)
    {

    }
}
