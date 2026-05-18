package architecture;

import main.Player;

import java.awt.*;

public class Start extends Tile
{

    public Start(Point position)
    {
        this.position = position;
        this.tileType = TileType.START;
    }

    @Override
    public void onPlayerArrive(Player player)
    {

    }
}
