package props;

import debug.Log;
import main.Player;

import javax.swing.*;
import java.util.function.IntPredicate;

/**
 * 功能描述：路障道具，可在任意格子放置路障，碰到路障的玩家会停止一回合
 * @author cyt &amp; Claude
 * @date 2026/6/1 0:00
 */
public class Barrier extends Prop
{
    public static final int ROUND_LIFE = 5;

    private IntPredicate onPlace;

    // AI: AI 预设的目标 tile 索引，非 null 时跳过弹窗直接放置
    private Integer aiTargetIndex = null;

    /**
     * 功能描述：设置AI预设的目标格子索引，非null时跳过弹窗直接放置路障
     * @param index 目标格子索引
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    public void setAiTargetIndex(int index)
    {
        this.aiTargetIndex = index;
    }

    /**
     * 功能描述：使用路障道具，AI预设了索引则跳过弹窗直接放置，否则弹出选择窗口
     * @param target 使用路障的玩家
     * @return 是否使用成功
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    @Override
    public boolean isUsed(Player target)
    {
        // AI: AI 预设了索引则跳过弹窗，直接放置路障
        if (aiTargetIndex != null)
        {
            int index = aiTargetIndex;
            aiTargetIndex = null; // 用完清掉
            Log.info("路障放置，选中索引：" + index);
            if (onPlace != null)
            {
                onPlace.test(index);
            }
            return true;
        }

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

            Log.info("路障放置，选中索引：" + selectIndex);

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
        return true;
    }

    /**
     * 功能描述：构造路障道具，设置名称、描述和价格
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    public Barrier()
    {
        this.setName("路障");
        this.setPrice(1000);
        this.setDescription("路障，可在任意格子放一个路障，所有玩家碰到路障会停止一回合，持续" + ROUND_LIFE + "回合");
    }

    /**
     * 功能描述：设置路障放置回调，供 MainMap 注入
     * @param onPlace 放置回调函数
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    public void setOnPlace(IntPredicate onPlace) {
        this.onPlace = onPlace;
    }
}
