package architecture;

import main.AIDecision;
import main.Player;
import debug.Log;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * 功能描述：赌场格子，弹珠博彩玩法，支持自动AI下注
 * @author cyt & Claude
 * @date 2026/6/1
 */
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

    /** 构建倍率概率表字符串 */
    private static String getOddsTable()
    {
        int totalWeight = 0;
        for (int[] c : CONFIG) totalWeight += c[1];

        StringBuilder sb = new StringBuilder();
        sb.append("倍率  出现概率  亮灯  中奖率\n");
        for (int[] c : CONFIG)
        {
            double appear = c[1] * 100.0 / totalWeight;
            double hit = c[2] * 100.0 / HOLE_COUNT;
            sb.append(String.format(" %dx   %2.0f%%     %2d   %4.1f%%\n",
                    c[0], appear, c[2], hit));
        }
        return sb.toString();
    }

    /**
     * 功能描述：构造赌场格子
     * @param positionIndex 格子索引
     * @param position 坐标
     * @param name 名称
     */
    public Casino(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.CASINO;
    }

    /**
     * 功能描述：玩家到达赌场，AI自动下注或弹出人类玩家交互界面
     * @param player 到达的玩家
     * @author cyt & Claude
     */
    @Override
    public void onPlayerArrive(Player player)
    {
        // AI: 自主赌场逻辑
        if (player.isAI())
        {
            playCasinoAI(player);
            return;
        }

        JOptionPane.showMessageDialog(null,
                "欢迎来到皇家赌场！弹珠游戏，最多玩 " + MAX_PLAYS + " 把\n"
                        + "15个洞口，弹珠落入亮灯口即中奖！\n\n"
                        + "【倍率概率表】\n" + getOddsTable());

        for (int play = 0; play < MAX_PLAYS; play++)
        {
            // 每次下注前先展示倍率表，让玩家决定是否下注
            int seeOdds = JOptionPane.showConfirmDialog(null,
                    "第 " + (play + 1) + "/" + MAX_PLAYS + " 把\n"
                            + "你有 $" + player.getMoney() + "，最低赌注 $" + MIN_BET + "\n\n"
                            + "【倍率概率表】\n" + getOddsTable(),
                    "赌场 · 查看倍率",
                    JOptionPane.OK_CANCEL_OPTION);
            if (seeOdds != JOptionPane.OK_OPTION) break;

            String input = JOptionPane.showInputDialog(null,
                    "请输入下注金额（最低 $" + MIN_BET + "）\n当前资金 $" + player.getMoney(),
                    "赌场 · 下注",
                    JOptionPane.QUESTION_MESSAGE);

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

    /**
     * 功能描述：AI 自主赌场逻辑，自动下注（资金 10%，保底 $100），保留弹珠动画，结果自动消失弹窗
     * @param ai AI 玩家
     * @author cyt & Claude
     */
    private void playCasinoAI(Player ai)
    {
        Log.info(ai.getName() + "（AI）进入皇家赌场，资金 $" + ai.getMoney());
        AIDecision.showAIMessage(ai.getName() + " 进入赌场！\n资金 $" + ai.getMoney()
                + "，自动下注中...\n\n" + getOddsTable());

        for (int play = 0; play < MAX_PLAYS; play++)
        {
            // 自动下注：资金 10%，保底 $100
            int bet = Math.max(MIN_BET, ai.getMoney() / 10);
            if (bet > ai.getMoney())
            {
                Log.info(ai.getName() + "（AI）资金不足，离开赌场");
                AIDecision.showAIMessage(ai.getName() + " 钱不够了，离开赌场");
                break;
            }

            Log.info(ai.getName() + "（AI）第 " + (play + 1) + "/" + MAX_PLAYS + " 把，下注 $" + bet);
            AIDecision.showAIMessage(ai.getName() + " 第 " + (play + 1) + "/" + MAX_PLAYS + " 把\n"
                    + "下注 $" + bet + "（资金 " + ai.getMoney() + "）\n\n" + getOddsTable());
            ai.moneyDecrease(bet);

            int multiplier = selectMultiplier();
            int litCount = getLitCount(multiplier);
            int[] litHoles = selectLitHoles(litCount);
            int targetHole = RNG.nextInt(HOLE_COUNT);
            boolean win = contains(litHoles, targetHole);

            // 保留弹珠动画！AI 玩家也能看到弹珠落下
            showPachinko(multiplier, litHoles, targetHole);

            if (win)
            {
                int reward = bet * multiplier;
                ai.moneyIncrease(reward);
                Log.info(ai.getName() + "（AI）中了 " + multiplier + " 倍！获得 $" + reward);
                AIDecision.showAIMessage(ai.getName() + " 第 " + (play + 1) + " 把\n"
                        + "中了 " + multiplier + " 倍！获得 $" + reward
                        + "\n当前资金 $" + ai.getMoney());
            }
            else
            {
                Log.info(ai.getName() + "（AI）第 " + (play + 1) + " 把没中");
                AIDecision.showAIMessage(ai.getName() + " 第 " + (play + 1) + " 把\n"
                        + "没中... 当前资金 $" + ai.getMoney());
            }
        }
    }

    // ==================== 弹珠动画（原 PachinkoPanel） ====================

    /**
     * 功能描述：显示弹珠动画面板
     * @param multiplier 倍率
     * @param litHoles 亮灯洞口索引
     * @param targetHole 弹珠目标洞口索引
     */
    private static void showPachinko(int multiplier, int[] litHoles, int targetHole)
    {
        JOptionPane.showOptionDialog(null, new PachinkoPanel(multiplier, litHoles, targetHole),
                "弹珠机 - " + multiplier + "x 倍率",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[]{}, null);
    }

    /**
     * 功能描述：弹珠动画内部面板，使用贝塞尔曲线模拟弹珠下落
     */
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

        /**
         * 功能描述：构造弹珠面板，初始化贝塞尔曲线路径并启动动画
         * @param multiplier 倍率
         * @param litHoles 亮灯洞口索引
         * @param targetHole 弹珠目标洞口索引
         */
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

        /**
         * 功能描述：根据进度 t 计算弹珠当前位置，使用贝塞尔平滑插值
         * @param t 动画进度 0~1
         */
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

        /**
         * 功能描述：根据洞口索引计算其在面板上的中心坐标
         * @param index 洞口索引
         * @return 洞口中心坐标
         */
        private static Point2D.Double holeCenter(int index)
        {
            int col = index % COLS;
            int row = index / COLS;
            double x = (col + 1.0) * PANEL_W / (COLS + 1.0);
            double y = PANEL_H - 55 - row * 105;
            return new Point2D.Double(x, y);
        }

        /**
         * 功能描述：绘制弹珠机面板，包括装饰钉、洞口、弹珠等
         * @param g 图形上下文
         */
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

    /**
     * 功能描述：根据权重随机选择一个倍率
     * @return 倍率值
     */
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

    /**
     * 功能描述：根据倍率获取亮灯洞口数量
     * @param multiplier 倍率
     * @return 亮灯洞口数
     */
    private int getLitCount(int multiplier)
    {
        for (int[] c : CONFIG)
            if (c[0] == multiplier) return c[2];
        return 1;
    }

    /**
     * 功能描述：随机选择指定数量的亮灯洞口
     * @param count 亮灯洞口数量
     * @return 亮灯洞口索引数组
     */
    private int[] selectLitHoles(int count)
    {
        java.util.List<Integer> all = new ArrayList<>();
        for (int i = 0; i < HOLE_COUNT; i++) all.add(i);
        Collections.shuffle(all, RNG);
        int[] result = new int[count];
        for (int i = 0; i < count; i++) result[i] = all.get(i);
        return result;
    }

    /**
     * 功能描述：判断数组中是否包含指定值
     * @param arr 数组
     * @param val 目标值
     * @return 是否包含
     */
    static boolean contains(int[] arr, int val)
    {
        for (int a : arr) if (a == val) return true;
        return false;
    }
}
