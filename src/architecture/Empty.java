package architecture;

import main.Player;

import java.awt.*;

public class Empty extends Tile
{

    public Empty(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.EMPTY;
    }

    public Empty(){
        this.tileType = TileType.EMPTY;
    }

    @Override
    public void onPlayerArrive(Player player)
    {

    }
}
