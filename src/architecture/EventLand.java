package architecture;

import main.Player;

import java.awt.*;

public class EventLand extends Tile
{

    public EventLand(int positionIndex, TileType tileType, Point position, String name)
    {
        super(positionIndex, tileType, position, name);
    }

    @Override
    public void onPlayerArrive(Player player)
    {

    }
}
