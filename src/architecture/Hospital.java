package architecture;

import main.AIDecision;
import main.ConstantNum;
import main.Player;

import javax.swing.*;
import java.awt.*;

/**
 * 功能描述：医院格子，玩家可花费金钱恢复HP，AI自动决定治疗策略
 * @author cyt & Claude
 * @date 2026/6/1
 */
public class Hospital extends Tile
{

    /**
     * 功能描述：构造医院格子
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
    public Hospital(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.HOSPITAL;
    }

    /**
     * 功能描述：玩家到达医院，AI自动决定治疗策略或弹出人类玩家交互界面
     * @param player 到达的玩家
     * @author cyt & Claude
     */
    @Override
    public void onPlayerArrive(Player player)
    {
        // AI: 自主决定治疗
        if (player.isAI())
        {
            // 满血不治
            if (player.getHp() >= ConstantNum.PLAYER_HP) return;
            // HP ≤ 50 用 2000 块的，否则用 500 块的
            boolean useExpensive = player.getHp() <= 50 && player.getMoney() >= 2000;
            int cost = useExpensive ? 2000 : 500;
            int heal = useExpensive ? 50 : 10;
            if (player.getMoney() < cost) return;

            int oldHp = player.getHp();
            player.moneyDecrease(cost);
            int newHp = Math.min(oldHp + heal, ConstantNum.PLAYER_HP);
            player.setHp(newHp);
            AIDecision.showAIMessage(player.getName() + " 在医院治疗，恢复 " + (newHp - oldHp) + " HP（花费 $" + cost + "）");
            return;
        }

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

        int cost = choice == 0 ? 500 : 2000;
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
