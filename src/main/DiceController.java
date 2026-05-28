package main;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

    public DiceController(MainMap map, Runnable onAnimationDone)
    {
        this.map = map;
        this.onAnimationDone = onAnimationDone;
        this.iconNormal = new ImageIcon("src/img/dice/vv.png");
        this.iconClicked = new ImageIcon("src/img/dice/cc.png");

        loadDiceImages();
        createDiceButton();
    }

    private void loadDiceImages()
    {
        diceImg = new ImageIcon[6];
        for (int i = 0; i < diceImg.length; i++)
        {
            diceImg[i] = new ImageIcon("src/img/dice/0" + (i + 1) + ".png");
        }
    }

    private void createDiceButton()
    {
        diceButton = new JLabel(iconNormal);
        diceButton.setBounds(ConstantNum.DICE_POSITION_X, ConstantNum.DICE_POSITION_Y, 150, 129);

        diceButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (isDiceRolling || map.getCurrentPlayer().isWalking())
                    return;

                isDiceRolling = true;
                diceButton.setIcon(iconClicked);
                boolean isSpecialDice = map.getCurrentPlayer().hasNextDiceSides();
                diceValue = map.getCurrentPlayer().rollDice();

                System.out.println(map.getCurrentPlayer().getName() + "投掷出了" + diceValue + "点数");
                if (map.getCurrentPlayer().isInToxic())
                    map.getCurrentPlayer().hpDecrease(3);
                map.roundIncrease();

                if (isSpecialDice)
                {
                    isDiceRolling = false;
                    diceButton.setIcon(iconNormal);
                    JOptionPane.showMessageDialog(null,map.getCurrentPlayer().getName() + "20面大骰子：" + diceValue
                    + "点!");
                    onAnimationDone.run();
                }else {
                    currentDiceFrame = 0;
                    startDiceAnimation();
                }
            }
        });
    }

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

    public void attachTo(JPanel uiLayer)
    {
        uiLayer.add(diceButton);
    }

    public int getDiceValue()
    {
        return diceValue;
    }
}
