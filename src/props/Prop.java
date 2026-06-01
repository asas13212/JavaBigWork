package props;

import main.Player;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * 功能描述：道具抽象基类，所有道具均继承此类
 * @author cyt
 * @date 2026/6/1 0:00
 */
public abstract class Prop
{
    private String name;

    private String description;

    private int price;

    private String filePath;


    /**
     * 功能描述：所有道具都会被使用
     * @author cyt
     * @date 2026/5/14 22:12
     */
    public abstract boolean isUsed(Player target);

    //<editor-fold desc="getter and setter 方法">
    /**
     * 功能描述：获取道具名称
     * @return 道具名称
     * @author cyt
     * @date 2026/6/1 0:00
     */
    public String getName()
    {
        return name;
    }

    /**
     * 功能描述：获取道具描述
     * @return 道具描述
     * @author cyt
     * @date 2026/6/1 0:00
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * 功能描述：获取道具价格
     * @return 道具价格
     * @author cyt
     * @date 2026/6/1 0:00
     */
    public int getPrice()
    {
        return price;
    }

    /**
     * 功能描述：设置道具名称
     * @param string 道具名称
     * @author cyt
     * @date 2026/6/1 0:00
     */
    public void setName(String string){
        this.name = string;
    }

    /**
     * 功能描述：设置道具描述
     * @param description 道具描述
     * @author cyt
     * @date 2026/6/1 0:00
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * 功能描述：设置道具价格
     * @param price 道具价格
     * @author cyt
     * @date 2026/6/1 0:00
     */
    public void setPrice(int price)
    {
        this.price = price;
    }
    //</editor-fold>

    /**
     * 功能描述：AI重写方法，便于匹配
     * @author cyt
     * @date 2026/5/26 13:24
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prop prop)) return false;
        return java.util.Objects.equals(name, prop.name);
    }

    /**
     * 功能描述：基于名称计算哈希值，配合 equals 用于集合操作
     * @return 哈希值
     * @author cyt
     * @date 2026/6/1 0:00
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(name);
    }
}
