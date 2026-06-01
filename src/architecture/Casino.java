package architecture;

import main.Player;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

public class Casino extends Tile
{
    private static final Random RNG = new Random();
    private static final int MAX_PLAYS = 3;
    private static final int HOLE_COUNT = 15;
    private static final int MIN_BET = 100;

    // {倍率, 权重, 亮灯数}
    private static final int[][] CONFIG = {
            {2, 35, 6},
            {4, 30, 3},
            {6, 20, 2},
            {8, 10, 1},
            {10, 5, 1}
    };

    public Casino(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.CASINO;
    }

    @Override
    public void onPlayerArrive(Player player)
    {
        JOptionPane.showMessageDialog(null,
                "欢迎来到皇家赌场！弹珠游戏，最多玩 " + MAX_PLAYS + " 把\n"
                        + "15个洞口，弹珠落入亮灯口即中奖！");

        for (int play = 0; play < MAX_PLAYS; play++)
        {
            String input = JOptionPane.showInputDialog(null,
                    "第 " + (play + 1) + "/" + MAX_PLAYS + " 把\n"
                            + "最低赌注 " + MIN_BET + "，你有 $" + player.getMoney(),
                    "赌场", JOptionPane.QUESTION_MESSAGE);

            if (input == null) break;

            int bet;
            try {
                bet = Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "请输入有效数字！");
                play--;
                continue;
            }

            if (bet < MIN_BET)
            {
                JOptionPane.showMessageDialog(null, "最低赌注 " + MIN_BET + "！");
                play--;
                continue;
            }
            if (bet > player.getMoney())
            {
                JOptionPane.showMessageDialog(null, "钱不够！你只有 $" + player.getMoney());
                play--;
                continue;
            }

            player.moneyDecrease(bet);

            int multiplier = selectMultiplier();
            int litCount = getLitCount(multiplier);
            int[] litHoles = selectLitHoles(litCount);
            int targetHole = RNG.nextInt(HOLE_COUNT);
            boolean win = contains(litHoles, targetHole);

            showPachinko(multiplier, litHoles, targetHole);

            if (win)
            {
                int reward = bet * multiplier;
                player.moneyIncrease(reward);
                JOptionPane.showMessageDialog(null,
                        "中了 " + multiplier + " 倍！\n弹珠落入亮灯洞口，获得 $" + reward + "！");
            }
            else
            {
                JOptionPane.showMessageDialog(null,
                        "没中！弹珠落入未亮灯洞口……");
            }

            if (play < MAX_PLAYS - 1)
            {
                int cont = JOptionPane.showConfirmDialog(null,
                        "已玩 " + (play + 1) + "/" + MAX_PLAYS + " 把，继续吗？",
                        "赌场", JOptionPane.YES_NO_OPTION);
                if (cont != JOptionPane.YES_OPTION) break;
            }
        }
    }

    // ==================== 弹珠动画（原 PachinkoPanel） ====================

    private static void showPachinko(int multiplier, int[] litHoles, int targetHole)
    {
        JOptionPane.showOptionDialog(null, new PachinkoPanel(multiplier, litHoles, targetHole),
                "弹珠机 - " + multiplier + "x 倍率",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[]{}, null);
    }

    private static class PachinkoPanel extends JPanel
    {
        private static final int COLS = 5;
        private static final int ROWS = 3;
        private static final int HOLE_R = 20;
        private static final int PANEL_W = 500;
        private static final int PANEL_H = 520;
        private static final int BALL_R = 8;
        private static final int TOTAL_FRAMES = 110;

        private final int multiplier;
        private final int[] litHoles;
        private final int targetHole;
        private final Point2D.Double[] knots;

        private double ballX, ballY;
        private int frame;
        private javax.swing.Timer timer;

        PachinkoPanel(int multiplier, int[] litHoles, int targetHole)
        {
            this.multiplier = multiplier;
            this.litHoles = litHoles;
            this.targetHole = targetHole;
            setPreferredSize(new Dimension(PANEL_W, PANEL_H));

            Point2D.Double target = holeCenter(targetHole);
            Random rng = new Random();

            knots = new Point2D.Double[]{
                    new Point2D.Double(PANEL_W / 2.0, 10),
                    new Point2D.Double(PANEL_W / 2.0 + rng.nextDouble(-50, 50), 120),
                    new Point2D.Double(PANEL_W / 2.0 + rng.nextDouble(-80, 80), 240),
                    new Point2D.Double(PANEL_W / 2.0 + rng.nextDouble(-60, 60), 340),
                    new Point2D.Double(target.x + rng.nextDouble(-30, 30), target.y - 50),
                    target
            };

            ballX = knots[0].x;
            ballY = knots[0].y;

            timer = new javax.swing.Timer(18, e -> {
                frame++;
                if (frame <= TOTAL_FRAMES)
                {
                    double t = (double) frame / TOTAL_FRAMES;
                    ballPos(t);
                    repaint();
                }
                else
                {
                    timer.stop();
                    SwingUtilities.windowForComponent(this).dispose();
                }
            });

            javax.swing.Timer starter = new javax.swing.Timer(300, e -> timer.start());
            starter.setRepeats(false);
            starter.start();
        }

        private void ballPos(double t)
        {
            int seg = Math.min((int) (t * (knots.length - 1)), knots.length - 2);
            double st = t * (knots.length - 1) - seg;
            double smooth = st * st * (3 - 2 * st);

            ballX = knots[seg].x + (knots[seg + 1].x - knots[seg].x) * smooth;
            ballY = knots[seg].y + (knots[seg + 1].y - knots[seg].y) * smooth;

            if (t > 0.1 && t < 0.8)
                ballX += Math.sin(t * 40) * 4;
        }

        private static Point2D.Double holeCenter(int index)
        {
            int col = index % COLS;
            int row = index / COLS;
            double x = (col + 1.0) * PANEL_W / (COLS + 1.0);
            double y = PANEL_H - 55 - row * 105;
            return new Point2D.Double(x, y);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(15, 50, 35));
            g2.fillRect(0, 0, PANEL_W, PANEL_H);

            // 装饰钉
            g2.setColor(new Color(160, 160, 160));
            for (int r = 0; r < 10; r++)
            {
                int n = (r % 2 == 0) ? 7 : 6;
                double ox = (r % 2 == 0) ? 30 : 65;
                double py = 30 + r * 38;
                for (int i = 0; i < n; i++)
                {
                    double px = ox + i * (PANEL_W - 2 * ox) / (n - 1);
                    g2.fillOval((int) px - 3, (int) py - 3, 6, 6);
                }
            }

            // 洞口
            for (int i = 0; i < HOLE_COUNT; i++)
            {
                Point2D.Double c = holeCenter(i);
                boolean lit = Casino.contains(litHoles, i);
                boolean isTarget = (i == targetHole && frame > TOTAL_FRAMES - 15);

                int x = (int) (c.x - HOLE_R);
                int y = (int) (c.y - HOLE_R);
                int d = HOLE_R * 2;

                if (lit)
                {
                    g2.setColor(new Color(255, 80, 30, 40));
                    g2.fillOval(x - 6, y - 6, d + 12, d + 12);
                    g2.setColor(new Color(255, 120, 40, 100));
                    g2.fillOval(x - 3, y - 3, d + 6, d + 6);
                    g2.setColor(new Color(255, 180, 60));
                    g2.fillOval(x, y, d, d);
                    g2.setColor(new Color(255, 230, 100));
                    g2.drawOval(x, y, d, d);
                }
                else
                {
                    g2.setColor(new Color(30, 30, 30));
                    g2.fillOval(x, y, d, d);
                    g2.setColor(new Color(60, 60, 60));
                    g2.drawOval(x, y, d, d);
                }

                if (isTarget)
                {
                    boolean flash = (frame / 3) % 2 == 0;
                    if (flash)
                    {
                        g2.setColor(new Color(255, 255, 0, lit ? 180 : 100));
                        g2.fillOval(x - 8, y - 8, d + 16, d + 16);
                    }
                }
            }

            // 弹珠
            if (frame > 0 && frame <= TOTAL_FRAMES)
            {
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillOval((int) ballX - BALL_R + 2, (int) ballY - BALL_R + 2, BALL_R * 2, BALL_R * 2);
                GradientPaint gp = new GradientPaint(
                        (int) ballX - BALL_R, (int) ballY - BALL_R, new Color(240, 240, 240),
                        (int) ballX + BALL_R, (int) ballY + BALL_R, new Color(160, 160, 160));
                g2.setPaint(gp);
                g2.fillOval((int) ballX - BALL_R, (int) ballY - BALL_R, BALL_R * 2, BALL_R * 2);
                g2.setColor(new Color(255, 255, 255, 150));
                g2.fillOval((int) ballX - BALL_R / 2, (int) ballY - BALL_R, BALL_R / 2, BALL_R / 2);
            }

            // 标题
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(multiplier + "x 倍率 · 亮 " + litHoles.length + " 灯", 10, PANEL_H - 10);
        }
    }

    // ==================== 工具方法 ====================

    private int selectMultiplier()
    {
        int total = 0;
        for (int[] c : CONFIG) total += c[1];
        int roll = RNG.nextInt(total);
        int accum = 0;
        for (int[] c : CONFIG)
        {
            accum += c[1];
            if (roll < accum) return c[0];
        }
        return CONFIG[0][0];
    }

    private int getLitCount(int multiplier)
    {
        for (int[] c : CONFIG)
            if (c[0] == multiplier) return c[2];
        return 1;
    }

    private int[] selectLitHoles(int count)
    {
        java.util.List<Integer> all = new ArrayList<>();
        for (int i = 0; i < HOLE_COUNT; i++) all.add(i);
        Collections.shuffle(all, RNG);
        int[] result = new int[count];
        for (int i = 0; i < count; i++) result[i] = all.get(i);
        return result;
    }

    static boolean contains(int[] arr, int val)
    {
        for (int a : arr) if (a == val) return true;
        return false;
    }
}
