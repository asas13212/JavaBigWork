package props;

import main.Player;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public int getPrice()
    {
        return price;
    }

    public void setName(String string){
        this.name = string;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name);
    }
}
