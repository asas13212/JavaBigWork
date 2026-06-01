package architecture;

import debug.Log;
import main.Player;
import props.*;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Chance extends Tile
{

    private static final Random RNG = new Random();

    private static final Prop[] POOL = {
            new BaoZi(),
            new ExamWeek(),
            new Mine(),
            new Barrier(),
            new Theft(),
            new Dice(),
            new HouseLevelUp(),
            new IdCard()
    };

    public Chance(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.GACHA;
    }

    @Override
    public void onPlayerArrive(Player player)
    {
        Prop prize = POOL[RNG.nextInt(POOL.length)];
        player.addProp(prize);
        Log.info(player.getName() + " 抽到了 [" + prize.getName() + "]");
        JOptionPane.showMessageDialog(null,
                player.getName() + " 抽到了 【" + prize.getName() + "】！\n" + prize.getDescription());
    }


}
