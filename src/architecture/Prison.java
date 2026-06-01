package architecture;

import main.Player;

import javax.swing.*;
import java.awt.*;

public class Prison extends Tile
{

    private static final int DURATION = 2;

    public Prison(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.PRISON;
    }

    @Override
    public void onPlayerArrive(Player player)
    {
        if (player.getBarrierStopTurns() > 0)
            return;

        player.setBarrierStopTurns(DURATION);
        player.setStatus("isPrisoned");
        JOptionPane.showMessageDialog(null,
                player.getName() + " 被关进了监狱！停止 " + DURATION + " 回合",
                "牢底坐穿",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
