package main;

import javax.swing.*;
import java.awt.*;

public class UILayerPanel extends JPanel
{
    private final MainMap map;

    UILayerPanel(MainMap map)
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
        map.renderPlayerUI(g);
        map.renderPropUI(g);
    }
}
