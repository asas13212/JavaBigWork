package main;

/**
 * 功能描述：游戏控制器接口 —— 三种模式统一入口
 * @author cyt & Claude
 * @date 2026/6/2
 */
public interface GameController
{
    void onDiceClicked();
    void onPropClicked(String propName, String targetName);
    void onLandChoice(boolean yes);
    void onUpgradeChoice(boolean yes);
    GameMode getMode();
    boolean isMyTurn();
    int getLocalPlayerIndex();
}
