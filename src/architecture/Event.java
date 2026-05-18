package architecture;

import main.Player;

import java.awt.*;

public class Event extends Tile
{

    public Event(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.EVENT;
    }

    @Override
    public void onPlayerArrive(Player player)
    {

    }
}
