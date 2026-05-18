package architecture;

import main.Player;

import java.awt.*;

public class Shop extends Tile
{

    public Shop(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.SHOP;
    }

    @Override
    public void onPlayerArrive(Player player)
    {

    }
}
