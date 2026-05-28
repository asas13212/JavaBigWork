package main;

import javax.swing.*;
import java.awt.*;

public class ActorLayerPanel extends JPanel
{
    private final MainMap map;

    ActorLayerPanel(MainMap map)
    {
        super(null);
        this.map = map;
        setOpaque(false);
        setBounds(0, 0, ConstantNum.MAP_WIDTH, ConstantNum.MAP_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
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
            if (MainMap.round == 0) {
                g.drawImage(dc.diceImg[0].getImage(),
                        1100, 650, 1200, 850,
                        900, 0, 1000, 200, null);
            } else {
                int idx = Math.min(dc.diceValue - 1, dc.diceImg.length - 1);
                g.drawImage(dc.diceImg[idx].getImage(),
                        1100, 650, 1200, 850,
                        900, 0, 1000, 200, null);
            }
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
