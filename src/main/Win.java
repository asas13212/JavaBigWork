package main;

import debug.Log;
import javax.swing.*;

public class Win
{

    public Win(Player loser)
    {
        Player winner = loser.getOtherPlayer();
        Log.info("====== 游戏结束 ======");
        Log.info(winner.getName() + " 获胜！剩余 $" + winner.getMoney() + "，HP=" + winner.getHp());
        Log.info(loser.getName() + " 破产！剩余 $" + loser.getMoney() + "，HP=" + loser.getHp());
        JOptionPane.showMessageDialog(null,loser.getName() + "破产了","游戏结束",JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
}
