package debug;

import main.MainMap;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 功能描述：调试类，为开发者使用
 * @author cyt
 * @date 2026/5/27 15:09
 */
public class DebugTools {

    // 开关变量
    public static final boolean ENABLED = true;

    /**
     * 功能描述：安装方法
     * @author cyt
     * @date 2026/5/27 15:10
     */
    public static void install(MainMap map) {
        if (!ENABLED) return;
        coordPicker(map);
        teleportPanel(map);
    }

    /**
     * 功能描述：点击即可打印坐标
     * @author cyt
     * @date 2026/5/27 15:10
     */
    private static void coordPicker(MainMap map) {
        map.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Log.debug("点击坐标: [" + e.getX() + ", " + e.getY() + "]");
            }
        });
    }

    /**
     * 功能描述：传送与加道具面板 直接传送
     * @author cyt
     * @date 2026/5/27 15:12
     */
    private static void teleportPanel(MainMap map) {
        JFrame panel = new JFrame("Debug");
        panel.setLayout(new java.awt.FlowLayout());
        panel.add(new JLabel("目标索引:"));
        JTextField field = new JTextField(3);
        panel.add(field);
        JButton btn = new JButton("传送");

        btn.addActionListener(e -> {
            try {
                int idx = Integer.parseInt(field.getText().trim());
                map.teleportTo(idx);
            } catch (NumberFormatException ignored) {}
        });
        panel.add(btn);

        JButton addPropsBtn = new JButton("道具全加一");
        addPropsBtn.addActionListener(e -> {
            String[] names = {"包子", "考试周", "地雷", "路障", "升级卡", "偷取", "万能骰子", "身份证"};
            main.Player target = map.getCurrentPlayer();
            for (String name : names) {
                target.addProp(namedProp(name));
            }
            map.refreshLayers();
        });
        panel.add(addPropsBtn);
        panel.setAlwaysOnTop(true);
        panel.setLocation(1400, 100);
        panel.pack();
        panel.setVisible(true);
    }

    /**
     * 功能描述：创造加道具，因为addProp只看名字
     * @author cyt
     * @date 2026/5/30 9:36
     */
    private static props.Prop namedProp(String name) {
        props.Prop p = new props.Prop() {
            @Override
            public boolean isUsed(main.Player target) { return false; }
        };
        p.setName(name);
        return p;
    }
}