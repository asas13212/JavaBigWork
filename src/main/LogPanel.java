package main;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 功能描述：游戏日志面板，实时显示带颜色标记的日志信息（INFO/DEBUG/WARN/ERROR）
 * @author cyt & Claude
 * @date 2026/6/1 21:00
 */
public class LogPanel extends JPanel
{
    private JTextPane area = new JTextPane();
    private JScrollPane scrollPane;

    private DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    // 各种信息的颜色
    private static final Color C_INFO  = new Color(0xF5F5F5); // 绿
    private static final Color C_DEBUG = new Color(0x33CCFF); // 蓝
    private static final Color C_WARN  = new Color(0xFFAA00); // 橙
    private static final Color C_ERROR = new Color(0xFF3333); // 红

    /**
     * 功能描述：构造方法，初始化黑色背景的日志面板和只读文本区域
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public LogPanel()
    {
        this.setLayout(new BorderLayout());
        this.setOpaque(true);
        this.setBackground(Color.BLACK);

        area.setEditable(false);
        area.setBackground(Color.BLACK);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        this.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 功能描述：清空日志面板所有内容
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public void clear()
    {
        area.setText("");
    }

    /**
     * 功能描述：Log 类调用的入口，根据日志级别以不同颜色追加带时间戳的日志
     * @param level 日志级别（DEBUG/WARN/ERROR/INFO）
     * @param msg 日志消息内容
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public void append(String level, String msg)
    {
        String time = LocalTime.now().format(timeFmt);
        Color color = switch (level) {
            case "DEBUG" -> C_DEBUG;
            case "WARN"  -> C_WARN;
            case "ERROR" -> C_ERROR;
            default      -> C_INFO;
        };

        StyledDocument doc = area.getStyledDocument();
        Style style = area.addStyle(null, null);
        StyleConstants.setForeground(style, color);

        try {
            doc.insertString(doc.getLength(), "[" + time + "] " + msg + "\n", style);
        } catch (BadLocationException ignored) {}

        // 自动滚到底部
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
}