package props;

import main.Player;

import javax.swing.*;

public class Dice extends Prop
{

    public Dice()
    {
        this.setDescription("控制下一次骰子投掷点数");
        this.setName("万能骰子");
        this.setPrice(1500);
    }

    @Override
    public boolean isUsed(Player target)
    {
        JComboBox<String> jComboBox = new JComboBox<>(
            new String[]{"一点", "两点", "三点", "四点","五点","六点"}
        );
        return true;
    }
}
