package props;

import main.Player;

import javax.swing.*;

public class Dice extends Prop
{

    public Dice()
    {
        this.setDescription("选择下一次投掷的点数");
        this.setName("万能骰子");
        this.setPrice(1500);
    }

    @Override
    public boolean isUsed(Player target)
    {
        String[] choices = {"1点", "2点", "3点", "4点", "5点", "6点"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "选择下一次投掷的点数：",
                "万能骰子",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                choices,
                choices[0]
        );

        if (choice < 0) return false;

        target.setNextDiceValue(choice + 1);
        JOptionPane.showMessageDialog(null, "已锁定下次投掷为 " + (choice + 1) + " 点！");
        return true;
    }
}
