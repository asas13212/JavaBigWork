package main;

import javax.swing.*;
import java.awt.*;

/**
 * 功能描述：UI 渲染层，负责绘制玩家信息面板和道具栏
 * @author cyt
 * @date 2026/6/1 21:00
 */
public class UILayerPanel extends JPanel
{
    private final MainMap map;

    /**
     * 功能描述：构造方法，初始化透明面板并绑定主地图引用
     * @param map 主地图实例
     * @author cyt
     * @date 2026/6/1 21:00
     */
    UILayerPanel(MainMap map)
    {
        super(null);
        this.map = map;
        setOpaque(false);
        setBounds(0, 0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);
    }

    /**
     * 功能描述：绘制玩家信息（金钱/血量/地产）和当前玩家的道具栏
     * @param g 图形上下文
     * @author cyt
     * @date 2026/6/1 21:00
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        map.renderPlayerUI(g);
        map.renderPropUI(g);
    }
}
