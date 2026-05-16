package architecture;

import main.Player;

import java.awt.*;

public class Prison extends Tile
{

    private int duration = 2;

    public Prison(int positionIndex, TileType tileType, Point position, String name)
    {
        super(positionIndex, tileType, position, name);
    }

    @Override
    public void onPlayerArrive(Player player)
    {
        player.setStatus("isPrisoned");
        if(player.getPrisonRound() > 0)
            player.prisonRoundDecrease();
        else
            player.setPrisonRound(duration);
    }
}
