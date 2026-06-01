package architecture;

import debug.Log;
import main.Player;
import props.Mine;

import java.awt.*;

/**
 * 功能描述：地图格子抽象基类，定义位置、地雷、路障等通用属性与行为
 * @author cyt
 * @date 2026/6/1
 */
public abstract class Tile
{
    protected int positionIndex;

    protected TileType tileType;

    protected Point position;

    protected String name;

    protected boolean hasMine = false;

    protected boolean hasBarrier = false;

    protected int barrierRound = 0;

    /**
     * 功能描述：有参构造
     * @author cyt
     * @date 2026/5/26 20:01
     */
    public Tile(int positionIndex, Point position, String name)
    {
        this.positionIndex = positionIndex;
        this.position = position;
        this.name = name;
    }

    /**
     * 功能描述：无参构造
     * @author cyt
     * @date 2026/5/26 20:01
     */
    public Tile()
    {
    }

    /**
     * 功能描述：玩家到了地块，执行的逻辑
     * @author cyt
     * @date 2026/5/26 20:01
     */
    public abstract void onPlayerArrive(Player player);

    /**
     * 功能描述：埋地雷
     * @author cyt
     * @date 2026/5/26 20:02
     */
    public void plantMine(Mine mine)
    {
        if (this.hasMine)
            Log.warn("[" + this.getName() + "] 区域已经埋雷，无法重复放置");
        else {
            Log.info("[" + this.getName() + "] 埋雷成功");
            this.hasMine = true;
        }
    }

    /**
     * 功能描述：移除地雷
     * @author cyt
     * @date 2026/5/26 20:02
     */
    public void removeMine()
    {
         if (!this.hasMine)
             Log.debug("[" + this.getName() + "] 地雷已被移除，无需重复操作");
         else {
             Log.warn("[" + this.getName() + "] 地雷引爆！");
             this.hasMine = false;
         }
    }

    /**
     * 功能描述：放置路障
     * @author cyt
     * @date 2026/5/26 20:05
     */
    public void plantBarrier(int rounds)
    {
        if (this.hasBarrier) {
            Log.warn("[" + this.getName() + "] 该位置已有路障，无法重复放置");
            return;
        }
        this.hasBarrier = true;
        this.barrierRound = rounds;
    }

    /**
     * 功能描述：移除路障
     * @author cyt
     * @date 2026/5/26 20:04
     */
    public void removeBarrier()
    {
        if(!hasBarrier)
        {
            Log.debug("[" + this.getName() + "] 路障不存在，无需移除");
        }else
        {
            this.hasBarrier = false;
            this.barrierRound = 0;
        }
    }

    /**
     * 功能描述：每回合递减路障生命周期，归零自动消失
     * @author cyt
     * @date 2026/5/26
     */
    public void decreaseBarrierRound()
    {
        if (!hasBarrier) return;
        barrierRound--;
        if (barrierRound <= 0) {
            removeBarrier();
            Log.info("[" + this.getName() + "] 路障过期消失");
        }
    }

    /**
     * 功能描述：获取路障剩余回合数
     * @author cyt
     * @date 2026/6/1
     */
    public int getBarrierRound()
    {
        return barrierRound;
    }

    /**
     * 功能描述：判断是否有路障
     * @author cyt
     * @date 2026/5/26 20:04
     */
    public boolean hasBarrier() { return hasBarrier; }

    /**
     * 功能描述：判断是否有地雷
     * @author cyt
     * @date 2026/5/26 20:06
     */
    /**
     * 功能描述：判断是否有地雷
     * @author cyt
     * @date 2026/5/26 20:06
     */
    public boolean hasMine() {return hasMine; }
    //<editor-fold desc="getter and setter">
    /**
     * 功能描述：获取格子索引
     * @return 格子索引
     */
    public int getPositionIndex()
    {
        return positionIndex;
    }

    /**
     * 功能描述：设置格子索引
     * @param positionIndex 格子索引
     */
    public void setPositionIndex(int positionIndex)
    {
        this.positionIndex = positionIndex;
    }

    /**
     * 功能描述：获取格子类型
     * @return 格子类型
     */
    public TileType getTileType()
    {
        return tileType;
    }

    /**
     * 功能描述：设置格子类型
     * @param tileType 格子类型
     */
    public void setTileType(TileType tileType)
    {
        this.tileType = tileType;
    }

    /**
     * 功能描述：获取格子坐标
     * @return 格子坐标
     */
    public Point getPosition()
    {
        return position;
    }

    /**
     * 功能描述：设置格子坐标
     * @param position 格子坐标
     */
    public void setPosition(Point position)
    {
        this.position = position;
    }

    /**
     * 功能描述：获取格子名称
     * @return 格子名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 功能描述：设置格子名称
     * @param name 格子名称
     */
    public void setName(String name)
    {
        this.name = name;
    }
    //</editor-fold>
}
