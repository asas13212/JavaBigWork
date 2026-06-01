package architecture;

/**
 * 功能描述：格子类型枚举，定义地图上各类格子的名称与描述
 * @author cyt
 * @date 2026/6/1
 */
public enum TileType
{
    START("起点", "出发即可领钱"),
    PROPERTY("地产", "可以购买并建造建筑"),
    SHOP("商店", "购买道具的地方"),
    EVENT("公园", "休息一回合或触发事件"),
    PRISON("监狱", "暂停游戏两回合"),
    HOSPITAL("医院", "恢复生命值"),
    GACHA("抽卡点", "随机抽取一张卡片"),
    CASINO("赌场", "弹珠博彩"),
    EMPTY("空地","无效果");

    private final String name;
    private final String description;


    /**
     * 功能描述：枚举构造方法
     * @param name 类型名称
     * @param description 类型描述
     */
    TileType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 功能描述：获取类型名称
     * @return 类型名称
     */
    public String getName() {
        return name;
    }

    /**
     * 功能描述：获取类型描述
     * @return 类型描述
     */
    public String getDescription() {
        return description;
    }
}
