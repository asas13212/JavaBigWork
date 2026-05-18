package architecture;

import main.*;

import javax.swing.*;
import java.awt.*;

public abstract class Land extends Tile
{
    private Player owner;

    private int houseLevel = 0;

    // 过路费
    public int[] tax;

    // 升级费用
    protected int[] priceLevelUp;

    protected int maxLevel;

    protected Image[] naiLong;
    protected Image[] xiaoMei;


    public Land(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.houseLevel = 0;
        this.tileType = TileType.PROPERTY;

    }

    @Override
    public void onPlayerArrive(Player player)
    {
        if (this.owner == null)
        {
            int result = JOptionPane.showOptionDialog(
                    null,
                    "购买当前房产:" + this.getName()  +"? 价格是" + this.getCurrentPrice() ,
                    "确认",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"确定", "取消"}, // 自定义按钮
                    "确定"
            );
            if ( result == 0 )
            {
                if (player.getMoney() >= priceLevelUp[houseLevel])
                {
                    player.moneyDecrease(priceLevelUp[houseLevel]);
                    this.setOwner(player);
                }else
                {
                    JOptionPane.showMessageDialog(null,"你没资格啊没资格");
                }
            }

        }else if ( owner != player){
            JOptionPane.showMessageDialog(null, "交付" + tax[houseLevel], "到了" + player.getOtherPlayerName() + "的领地", JOptionPane.INFORMATION_MESSAGE);
            player.moneyDecrease(tax[houseLevel]);
        }else {
            // 升级逻辑
        }
    }


    protected void landLevelUp(Player owner)
    {
        if(houseLevel == ConstantNum.MAX_LEVEL )
            return;

        if (houseLevel == 0)
        {
            owner.moneyDecrease(priceLevelUp[houseLevel]);
            houseLevel++;
        }else if (houseLevel == 1)
        {
            owner.moneyDecrease(priceLevelUp[houseLevel]);
            houseLevel++;
        }

    }

    public Player getOwner()
    {
        return owner;
    }

    public int getHouseLevel()
    {
        return houseLevel;
    }

    public void setOwner(Player owner)
    {
        this.owner = owner;
    }

    /**
     * 功能描述：这里是需要升级的钱
     * @author cyt
     * @date 2026/5/18 21:08
     */
    public int getCurrentPrice()
    {
        return priceLevelUp[houseLevel];
    }

    public void houseLevelUp()
    {
        this.houseLevel++;
    }
}
