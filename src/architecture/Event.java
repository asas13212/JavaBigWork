package architecture;

import main.Player;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Event extends Tile
{

    Timer walkTimer;
    public Event(int positionIndex, Point position, String name)
    {
        super(positionIndex, position, name);
        this.tileType = TileType.EVENT;

    }

    @Override
    public void onPlayerArrive(Player player)
    {
        Random rareRand = new Random();
        if (rareRand.nextInt(0, 1000) == 99) {
            JOptionPane.showMessageDialog(null,"千分之一概率中大奖，你真幸运!\n金钱翻倍","奇迹!!!",JOptionPane.INFORMATION_MESSAGE);
            player.moneyIncrease(player.getMoney());
        }

        Random rand = new Random();
        int num = rand.nextInt(0,14) + 1;
//        int num = 3;
        switch (num)
        {
            case 1 -> {
                System.out.println("事件1");
                JOptionPane.showMessageDialog(null,"你被车创飞了，包里的钱撒了一地，村口大爷大妈抢走了一半\n但是看在你太惨 ,还了一半给你","噩耗突传",JOptionPane.INFORMATION_MESSAGE);
                player.moneyDecrease((int)(player.getMoney() * 0.25));
            }
            case 2 -> {
                System.out.println("事件2");
                JOptionPane.showMessageDialog(null,"肚子太痛了，需要进厕所\n停止一回合","噩耗突传",JOptionPane.INFORMATION_MESSAGE);
                // 停止一回合的逻辑
            }
            case 3 -> {
                int value = player.rollDice();
                System.out.println("事件3");
                JOptionPane.showMessageDialog(null,"搭乘顺风车赶路，向前走 " + value +" 步","一般般吧",JOptionPane.INFORMATION_MESSAGE);
                player.startWalk(value);
            }
            case 4 -> {
                System.out.println("事件4");
                JOptionPane.showMessageDialog(null,"买彩票中了3000块!!!\n金钱增加3000","喜从天降",JOptionPane.INFORMATION_MESSAGE);
                player.moneyIncrease(3000);
            }
            case 5 -> {
                System.out.println("事件5");
                JOptionPane.showMessageDialog(null,"买股票把裤衩子都亏没了\n金钱减2000","噩耗突传",JOptionPane.INFORMATION_MESSAGE);
                player.moneyDecrease(2000);
            }
            case 6 -> {
                System.out.println("事件6");
                JOptionPane.showMessageDialog(null,"扶老奶奶过马路，奶奶给了你1500!\n金钱增加1500","你是个好人!",JOptionPane.INFORMATION_MESSAGE);
                player.moneyIncrease(1500);
            }
            case 7 -> {
                System.out.println("事件7");
                JOptionPane.showMessageDialog(null,"扶倒地老奶奶起来，奶奶拉着你的肩膀\n并且向你发起了转账\n金钱减500","好人没好报啊(哭)",JOptionPane.INFORMATION_MESSAGE);
                player.moneyDecrease(500);
            }
            case 8 -> {
                System.out.println("事件8");
                JOptionPane.showMessageDialog(null,"回到高中了，你心血来潮，准备回去体验高中生活\n生命减20\n停止一回合","三好学生",JOptionPane.INFORMATION_MESSAGE);
                // 停止一回合

                player.hpDecrease(20);
            }
            case 9 -> {
                System.out.println("事件9");
                JOptionPane.showMessageDialog(null,"5.20到了，微信某个陌生人给你转了1314，你大为震撼\n金钱增加1314","你是个好人",JOptionPane.INFORMATION_MESSAGE);
                player.moneyIncrease(1314);
            }
            case 10 -> {
                System.out.println("事件10");
                JOptionPane.showMessageDialog(null,"你吃了过期的罐头，食物中毒了\n每回合掉3生命","噩耗突传",JOptionPane.INFORMATION_MESSAGE);
                player.setInToxic(true);
            }
            case 11 ->{
                System.out.println("事件11");
                JOptionPane.showMessageDialog(null,"路上见到了一个地雷\n地雷卡加一","卡牌大师",JOptionPane.INFORMATION_MESSAGE);
                // 添加地雷
            }
            case 12 -> {
                System.out.println("事件12");
                JOptionPane.showMessageDialog(null,"有一个房地产大亨投资了你的地皮\n随机地产等级加一","房地产大亨",JOptionPane.INFORMATION_MESSAGE);
                int n = player.getProperty();
                if (n == 0)
                {
                    JOptionPane.showMessageDialog(null,"没房产啊没房产","可惜",JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int random = rand.nextInt(0,n);
                Land land = player.getLandOwned(random);

                if ( land.getHouseLevel() < land.maxLevel)
                {
                    land.houseLevelUp();
                }else {
                    JOptionPane.showMessageDialog(null,"可惜，随机到的地皮已经满级了","糟糕!",JOptionPane.INFORMATION_MESSAGE);
                }
            }
            case 13 -> {
                System.out.println("事件13");
                JOptionPane.showMessageDialog(null,"路上有人在大声吆喝:\"学任一一种动物叫，可以有红包拿!!\"\n你尝试了一下，竟然不是骗子\n金钱增加800","喜从天降",JOptionPane.INFORMATION_MESSAGE);
                player.moneyIncrease(800);
            }
            case 14 -> {
                System.out.println("事件14");
                int result = JOptionPane.showOptionDialog(
                        null,
                        "前面出现两条路：右边是树林，左边是草地，你走哪边",
                        "抉择抉择",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"确定", "取消"}, // 自定义按钮
                        "确定"
                );
                if ( result == 0 )
                {
                    JOptionPane.showMessageDialog(null,"一阵风吹过，获得金钱1000");
                }else
                {
                    JOptionPane.showMessageDialog(null,"草地有条蛇，你被咬了!\n已中毒");
                    player.setInToxic(true);
                }
            }
        }
    }


}
