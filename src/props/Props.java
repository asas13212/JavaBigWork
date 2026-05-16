package props;

import main.Player;

public abstract class Props
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
    public abstract void isUsed(Player target);

    //<editor-fold desc=" setter 方法">
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

    public String getFilePath()
    {
        return filePath;
    }
    //</editor-fold>
}
