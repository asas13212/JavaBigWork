package architecture;

import main.Player;

import java.awt.*;

/**
 * 功能描述：起点格子，玩家经过或到达时可领取金钱
 * @author cyt
 * @date 2026/6/1
 */
public class Start extends Tile
{

    /**
     * 功能描述：构造起点格子
     * @param position 坐标
     */
    public Start(Point position)
    {
        this.position = position;
        this.tileType = TileType.START;
    }

    /**
     * 功能描述：玩家到达起点时的逻辑（由外部控制器处理）
     * @param player 到达的玩家
     */
    @Override
    public void onPlayerArrive(Player player)
    {
    }
}
