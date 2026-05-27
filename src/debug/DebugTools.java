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
                System.out.printf("[%d, %d]\n", e.getX(), e.getY());
            }
        });
    }

    /**
     * 功能描述：传送面板 直接传送
     * @author cyt
     * @date 2026/5/27 15:12
     */
    private static void teleportPanel(MainMap map) {
        JFrame panel = new JFrame("Debug Teleport");
        panel.setLayout(new java.awt.FlowLayout());
        panel.add(new JLabel("目标索引:"));
        // 文字域
        JTextField field = new JTextField(3);
        panel.add(field);
        JButton btn = new JButton("传送");
        panel.add(btn);

        btn.addActionListener(e -> {
            try {
                int idx = Integer.parseInt(field.getText().trim());
                map.teleportTo(idx);                  // MainMap 暴露一个 public方法
            } catch (NumberFormatException ignored) {}
        });
        panel.setAlwaysOnTop(true);
        panel.setLocation(1400, 100);
        panel.pack();
        panel.setVisible(true);
    }
}