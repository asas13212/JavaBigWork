package main;

import debug.Log;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 功能描述：骰子控制器，管理骰子按钮、骰子动画和掷骰逻辑
 * @author cyt & Claude
 * @date 2026/6/1 21:00
 */
public class DiceController
{
    private final MainMap map;
    private final Runnable onAnimationDone;

    ImageIcon[] diceImg;
    boolean isDiceRolling;
    int currentDiceFrame;
    int diceValue;

    private Timer diceTimer;
    private JLabel diceButton;
    private final ImageIcon iconNormal;
    private final ImageIcon iconClicked;

    /**
     * 功能描述：构造方法，加载骰子图片并创建骰子按钮
     * @param map 主地图实例
     * @param onAnimationDone 动画完成后的回调
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public DiceController(MainMap map, Runnable onAnimationDone)
    {
        this.map = map;
        this.onAnimationDone = onAnimationDone;
        this.iconNormal = new ImageIcon("src/img/dice/vv.png");
        this.iconClicked = new ImageIcon("src/img/dice/cc.png");

        loadDiceImages();
        createDiceButton();
    }

    /**
     * 功能描述：加载六面骰子图片资源
     * @author cyt
     * @date 2026/6/1 21:00
     */
    private void loadDiceImages()
    {
        diceImg = new ImageIcon[6];
        for (int i = 0; i < diceImg.length; i++)
        {
            diceImg[i] = new ImageIcon("src/img/dice/0" + (i + 1) + ".png");
        }
    }

    /**
     * 功能描述：创建骰子按钮并绑定鼠标点击监听
     * @author cyt
     * @date 2026/6/1 21:00
     */
    private void createDiceButton()
    {
        diceButton = new JLabel(iconNormal);
        diceButton.setBounds(ConstantNum.DICE_POSITION_X, ConstantNum.DICE_POSITION_Y, 150, 129);

        diceButton.addMouseListener(new MouseAdapter()
        {
            /**
             * 功能描述：点击骰子按钮触发掷骰，骰子滚动中或玩家行走中忽略
             * @param e 鼠标事件
             * @author cyt
             * @date 2026/6/1 21:00
             */
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (isDiceRolling || map.getCurrentPlayer().isWalking())
                    return;

                // 联机模式：通过 GameController 发送到服务端
                GameController gc = map.getGameController();
                if (gc != null && gc.getMode() == GameMode.ONLINE)
                {
                    gc.onDiceClicked();
                    return;
                }

                executeRoll(false, -1);
            }
        });
    }

    /** AI 自动掷骰 */
    public void triggerRoll()
    {
        if (isDiceRolling || map.getCurrentPlayer().isWalking()) return;
        executeRoll(false, -1);
    }

    /** 联机：服务端点数，触发完整动画+走路 */
    public void triggerRollWithValue(int serverValue)
    {
        if (isDiceRolling || map.getCurrentPlayer().isWalking()) return;
        executeRoll(true, serverValue);
    }

    /** 联机：仅显示对方骰子结果，不触发走路 */
    public void showResultOnly(int serverValue)
    {
        diceValue = serverValue;
        diceButton.setIcon(iconNormal);
        map.refreshLayers();
    }

    private void executeRoll(boolean useServerValue, int serverValue)
    {
        isDiceRolling = true;
        diceButton.setIcon(iconClicked);

        if (useServerValue)
        {
            diceValue = serverValue;
        }
        else
        {
            boolean isSpecialDice = map.getCurrentPlayer().hasNextDiceSides();
            boolean isFixedDice = map.getCurrentPlayer().hasNextDiceValue();
            diceValue = map.getCurrentPlayer().rollDice();
        }

        Log.info(map.getCurrentPlayer().getName() + " 投掷出了 " + diceValue + " 点");
        if (map.getCurrentPlayer().isInToxic())
            map.getCurrentPlayer().hpDecrease(3);

        if (useServerValue)
        {
            // 联机：播放动画后触发回调（走路），回合由服务端控制
            currentDiceFrame = 0;
            startDiceAnimation();
            return;
        }

        map.roundIncrease();

        boolean isSpecialDice = map.getCurrentPlayer().hasNextDiceSides();
        boolean isFixedDice = map.getCurrentPlayer().hasNextDiceValue();

        if (isFixedDice)
        {
            isDiceRolling = false;
            diceButton.setIcon(iconNormal);
            String msg1 = map.getCurrentPlayer().getName() + " 万能骰子：" + diceValue + "点!";
            if (map.getCurrentPlayer().isAI())
                AIDecision.showAIMessage(msg1);
            else
                JOptionPane.showMessageDialog(null, msg1);
            onAnimationDone.run();
        }
        else if (isSpecialDice)
        {
            isDiceRolling = false;
            diceButton.setIcon(iconNormal);
            String msg2 = map.getCurrentPlayer().getName() + " 20面大骰子：" + diceValue + "点!";
            if (map.getCurrentPlayer().isAI())
                AIDecision.showAIMessage(msg2);
            else
                JOptionPane.showMessageDialog(null, msg2);
            onAnimationDone.run();
        }else {
            currentDiceFrame = 0;
            startDiceAnimation();
        }
    }

    /**
     * 功能描述：启动骰子滚动动画（8帧循环），结束后触发回调
     * @author cyt
     * @date 2026/6/1 21:00
     */
    private void startDiceAnimation()
    {

        diceTimer = new Timer(80, evt ->
        {
            currentDiceFrame++;
            map.refreshLayers();

            if (currentDiceFrame >= 8)
            {
                diceTimer.stop();
                isDiceRolling = false;
                diceButton.setIcon(iconNormal);
                onAnimationDone.run();
            }
        });
        diceTimer.start();
    }

    /**
     * 功能描述：将骰子按钮附加到指定的 UI 层面板
     * @param uiLayer 目标 UI 层面板
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public void attachTo(JPanel uiLayer)
    {
        uiLayer.add(diceButton);
    }

    /**
     * 功能描述：获取当前掷出的骰子点数
     * @return 骰子点数（1~6 或万能骰子的 1~20）
     * @author cyt
     * @date 2026/6/1 21:00
     */
    public int getDiceValue()
    {
        return diceValue;
    }
}
