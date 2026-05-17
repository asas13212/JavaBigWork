package architecture;

import main.Player;
import props.Mine;

import java.awt.*;

public abstract class Tile
{
    private int positionIndex;

    private TileType tileType;

    public Point position;

    private String name;


    private boolean hasMine = false;

    private boolean hasBarrier = false;

    public Tile(int positionIndex, TileType tileType, Point position, String name)
    {
        this.positionIndex = positionIndex;
        this.tileType = tileType;
        this.position = position;
        this.name = name;
    }

    public Tile()
    {
    }

    public abstract void onPlayerArrive(Player player);

    void plantMine(Mine mine)
    {
        if (this.hasMine)
            System.out.println("这个区域已经埋雷了");
        else {
            this.hasMine = true;
        }
    }

    void removeMine()
    {
         if (!this.hasMine)
             System.out.println("这里已经没有地雷了");
         else {
             this.hasMine = false;
         }
    }

    //<editor-fold desc="getter and setter">
    public int getPositionIndex()
    {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex)
    {
        this.positionIndex = positionIndex;
    }

    public TileType getTileType()
    {
        return tileType;
    }

    public void setTileType(TileType tileType)
    {
        this.tileType = tileType;
    }

    public Point getPosition()
    {
        return position;
    }

    public void setPosition(Point position)
    {
        this.position = position;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    public boolean isHasMine()
    {
        return hasMine;
    }

    public void setHasMine(boolean hasMine)
    {
        this.hasMine = hasMine;
    }

    public boolean isHasBarrier()
    {
        return hasBarrier;
    }

    public void setHasBarrier(boolean hasBarrier)
    {
        this.hasBarrier = hasBarrier;
    }


    //</editor-fold>
}
