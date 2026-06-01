package architecture;

import main.ConstantNum;
import main.Player;

import javax.swing.*;
import java.awt.*;

public class Hospital extends Tile
{

    public Hospital(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.HOSPITAL;
    }

    @Override
    public void onPlayerArrive(Player player)
    {
        int choice = JOptionPane.showOptionDialog(
                null,
                "欢迎光临医院！每次只能治疗一次",
                "医院",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{"500块 - 恢复10HP", "2000块 - 恢复50HP", "离开"},
                "离开"
        );

        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) return;

        int cost = choice == 0 ? 500 : 2000;  // choice == 0 就是500，否则是2000
        int heal = choice == 0 ? 10 : 50;

        if (player.getMoney() < cost)
        {
            JOptionPane.showMessageDialog(null, "钱不够！需要" + cost + "块 你只有 $" + player.getMoney());
            return;
        }

        if (player.getHp() >= ConstantNum.PLAYER_HP)
        {
            JOptionPane.showMessageDialog(null, "生命值已满，无需治疗！");
            return;
        }

        int oldHp = player.getHp();
        player.moneyDecrease(cost);
        int newHp = Math.min(oldHp + heal, ConstantNum.PLAYER_HP);
        player.setHp(newHp);

        JOptionPane.showMessageDialog(null, "治疗成功！恢复了 " + (newHp - oldHp) + " 点生命！");
    }

}
