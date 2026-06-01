package architecture;

import debug.Log;
import main.Player;
import props.*;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * 功能描述：抽卡格子，玩家随机抽取一张道具卡，AI自动领取
 * @author cyt & Claude
 * @date 2026/6/1
 */
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

    /**
     * 功能描述：构造抽卡格子
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
    public Chance(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.GACHA;
    }

    /**
     * 功能描述：玩家到达抽卡点，随机获取一张道具，AI自动领取并显示消息
     * @param player 到达的玩家
     * @author cyt & Claude
     */
    @Override
    public void onPlayerArrive(Player player)
    {
        Prop prize = POOL[RNG.nextInt(POOL.length)];
        player.addProp(prize);
        Log.info(player.getName() + " 抽到了 [" + prize.getName() + "]");
        if (player.isAI())
            main.AIDecision.showAIMessage(player.getName() + " 抽到了 【" + prize.getName() + "】！\n" + prize.getDescription());
        else
            JOptionPane.showMessageDialog(null,
                    player.getName() + " 抽到了 【" + prize.getName() + "】！\n" + prize.getDescription());
    }


}
