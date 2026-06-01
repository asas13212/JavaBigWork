package architecture;

import main.Player;

import java.awt.*;

/**
 * 功能描述：空地格子，无特殊效果
 * @author cyt
 * @date 2026/6/1
 */
public class Empty extends Tile
{

    /**
     * 功能描述：有参构造空地格子
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
    public Empty(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.EMPTY;
    }

    /**
     * 功能描述：无参构造空地格子
     */
    public Empty(){
        this.tileType = TileType.EMPTY;
    }

    /**
     * 功能描述：玩家到达空地时无特殊效果
     * @param player 到达的玩家
     */
    @Override
    public void onPlayerArrive(Player player)
    {

    }
}
