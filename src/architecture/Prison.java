package architecture;

import main.Player;

import java.awt.*;

public class Prison extends Tile
{

    private int duration = 2;

    public Prison(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.PRISON;
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
