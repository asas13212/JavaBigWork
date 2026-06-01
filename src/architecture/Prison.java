package architecture;

import main.Player;

import javax.swing.*;
import java.awt.*;

/**
 * 功能描述：监狱格子，玩家被关押2回合，AI自动处理并显示消息
 * @author cyt & Claude
 * @date 2026/6/1
 */
public class Prison extends Tile
{

    private static final int DURATION = 2;

    /**
     * 功能描述：构造监狱格子
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
    public Prison(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.PRISON;
    }

    /**
     * 功能描述：玩家到达监狱，被关押指定回合数，AI自动处理并显示消息
     * @param player 到达的玩家
     * @author cyt & Claude
     */
    @Override
    public void onPlayerArrive(Player player)
    {
        if (player.getBarrierStopTurns() > 0)
            return;

        player.setBarrierStopTurns(DURATION);
        player.setStatus("isPrisoned");
        if (player.isAI())
            main.AIDecision.showAIMessage(player.getName() + " 被关进了监狱！停止 " + DURATION + " 回合");
        else
            JOptionPane.showMessageDialog(null,
                    player.getName() + " 被关进了监狱！停止 " + DURATION + " 回合",
                    "牢底坐穿",
                    JOptionPane.INFORMATION_MESSAGE);
    }
}
