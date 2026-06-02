package main;

/**
 * 功能描述：本地双人对战控制器 —— 所有操作直接在 MainMap 本地执行
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class LocalController implements GameController
{
    private final MainMap mainMap;

    public LocalController(MainMap mainMap)
    {
        this.mainMap = mainMap;
    }

    @Override public void onDiceClicked() { /* handled by DiceController directly */ }
    @Override public void onPropClicked(String propName) { /* handled by MainMap.addPropsListener */ }
    @Override public void onLandChoice(boolean yes) { /* handled by Land.onPlayerArrive JOptionPane */ }
    @Override public void onUpgradeChoice(boolean yes) { /* handled by Land.onPlayerArrive JOptionPane */ }
    @Override public GameMode getMode() { return GameMode.LOCAL_PVP; }
    @Override public boolean isMyTurn() { return true; }
    @Override public int getLocalPlayerIndex() { return mainMap.getCurrentPlayerIndex(); }
}
