package props;

import main.Player;

import javax.swing.*;
import java.util.function.IntPredicate;

public class Barrier extends Prop
{
    public static final int ROUND_LIFE = 5;

    private IntPredicate onPlace;

    @Override
    public void isUsed(Player target)
    {
        JFrame jFrame = new JFrame("选择安置位置");

        jFrame.setSize(250, 150);
        jFrame.setLocationRelativeTo(null);
        jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel();

        JLabel label = new JLabel("位置索引：");
        panel.add(label);

        String[] items = new String[30];
        for (int i = 0; i < 30; i++) {
            items[i] = String.valueOf(i);
        }

        final JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setSelectedIndex(0);

        JButton btnConfirm = new JButton("确定");
        panel.add(btnConfirm);

        btnConfirm.addActionListener(e -> {
            String selectStr = (String) comboBox.getSelectedItem();
            int selectIndex = Integer.parseInt(selectStr);

            System.out.println("你选中的索引是：" + selectIndex);

            if (onPlace != null) {
                if (onPlace.test(selectIndex)) {
                    JOptionPane.showMessageDialog(jFrame, "已选择索引：" + selectIndex);
                    jFrame.dispose();
                }
            } else {
                jFrame.dispose();
            }
        });

        panel.add(comboBox);
        jFrame.setContentPane(panel);
        jFrame.setVisible(true);
    }

    public Barrier()
    {
        this.setName("路障");
        this.setPrice(1000);
        this.setDescription("路障，可在任意格子放一个路障，所有玩家碰到路障会停止一回合，持续" + ROUND_LIFE + "回合");
    }

    public void setOnPlace(IntPredicate onPlace) {
        this.onPlace = onPlace;
    }
}
