package main;

import javax.swing.*;
import java.awt.*;

/**
 * 功能描述：玩家与骰子渲染层，负责绘制玩家角色和骰子动画
 * @author cyt
 * @date 2026/6/1 21:00
 */
public class ActorLayerPanel extends JPanel
{
    private final MainMap map;

    /**
     * 功能描述：构造方法，初始化透明面板并绑定主地图引用
     * @param map 主地图实例
     * @author cyt
     * @date 2026/6/1 21:00
     */
    ActorLayerPanel(MainMap map)
    {
        super(null);
        this.map = map;
        setOpaque(false);
        setBounds(0, 0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);
    }

    /**
     * 功能描述：绘制玩家角色、格子索引和骰子动画
     * @param g 图形上下文
     * @author cyt
     * @date 2026/6/1 21:00
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        map.renderTileIndexes(g);
        if (map.players != null) {
            if (map.players[0] != null) {
                map.players[0].renderStaticSprite(g, map.players[0].getPosition());
            }
            if (map.players[1] != null) {
                map.players[1].renderStaticSprite(g, map.players[1].getPosition());
            }
        }
        DiceController dc = map.diceController;
        if (dc != null && dc.diceImg != null && dc.diceImg[0] != null && !dc.isDiceRolling) {
            int idx = Math.max(0, Math.min(dc.diceValue - 1, dc.diceImg.length - 1));
            g.drawImage(dc.diceImg[idx].getImage(),
                    1100, 650, 1200, 850, 900, 0, 1000, 200, null);
        }
        if (dc != null && dc.isDiceRolling && dc.diceImg != null) {
            int frameWidth = 100;
            int srcX = dc.currentDiceFrame * frameWidth;
            int idx = Math.min(dc.diceValue - 1, dc.diceImg.length - 1);
            g.drawImage(
                    dc.diceImg[idx].getImage(),
                    1100, 650, 1100 + 100, 650 + 200,
                    srcX, 0, srcX + frameWidth, 200, null);
        }
    }
}
