package main;

import javax.swing.*;

public class Win
{

    public Win(Player player)
    {
        JOptionPane.showMessageDialog(null,player.getName() + "破产了","游戏结束",JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
}
