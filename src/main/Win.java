package main;

import debug.Log;
import javax.swing.*;
import java.awt.*;

/**
 * 功能描述：游戏结束处理类，展示胜利画面并退出程序
 * @author cyt & Claude
 * @date 2026/6/1 21:00
 */
public class Win
{

    /**
     * 功能描述：构造方法，记录胜负信息、展示胜利画面后退出游戏
     * @param loser 失败玩家
     * @author cyt & Claude
     * @date 2026/6/1 21:00
     */
    public Win(Player loser)
    {
        Player winner = loser.getOtherPlayer();
        Log.info("====== 游戏结束 ======");
        Log.info(winner.getName() + " 获胜！剩余 $" + winner.getMoney() + "，HP=" + winner.getHp());
        Log.info(loser.getName() + " 破产！剩余 $" + loser.getMoney() + "，HP=" + loser.getHp());

        // 构建胜利画面面板
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(20, 20, 40));

        // 标题：胜利玩家
        JLabel title = new JLabel("🏆 " + winner.getName() + " 获胜！🏆", JLabel.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setForeground(new Color(255, 215, 0));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        panel.add(title, BorderLayout.NORTH);

        // 胜利图片
        ImageIcon winIcon = new ImageIcon("src/img/player/win.png");
        JLabel imgLabel = new JLabel(winIcon, JLabel.CENTER);
        panel.add(imgLabel, BorderLayout.CENTER);

        // 底部信息
        JLabel info = new JLabel("<html><center>"
                + winner.getName() + "：$" + winner.getMoney() + " | HP " + winner.getHp() + "<br>"
                + loser.getName() + "：破产了..."
                + "</center></html>", JLabel.CENTER);
        info.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        info.setForeground(Color.LIGHT_GRAY);
        info.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        panel.add(info, BorderLayout.SOUTH);

        JOptionPane.showOptionDialog(null, panel,
                "游戏结束 · " + winner.getName() + " 胜利！",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[]{"退出游戏"}, "退出游戏");

        System.exit(0);
    }
}
