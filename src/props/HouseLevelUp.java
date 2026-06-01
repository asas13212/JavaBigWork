package props;

import architecture.Land;
import main.AIDecision;
import main.Player;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 功能描述：升级卡道具，随机升级一个未升级的已购地产
 * @author cyt &amp; Claude
 * @date 2026/6/1 0:00
 */
public class HouseLevelUp extends Prop
{

    private final Random random = new Random();

    /**
     * 功能描述：构造升级卡道具，设置名称、描述和价格
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    public HouseLevelUp()
    {
        this.setDescription("随机一个未升级地产升级");
        this.setPrice(2000);
        this.setName("升级卡");
    }

    /**
     * 功能描述：使用升级卡，随机升级一个未升级的已购地产；AI自动跳过弹窗
     * @param target 使用升级卡的玩家
     * @return 是否使用成功
     * @author cyt &amp; Claude
     * @date 2026/6/1 0:00
     */
    @Override
    public boolean isUsed(Player target)
    {
        if (target == null) return false;

        // 约定：升级卡只对"使用者自己"生效，因此 target 就是自己
        int landCount = target.getLandOwnedCount();
        if (landCount <= 0)
        {
            if (target.isAI())
                AIDecision.showAIMessage(target.getName() + " 还没有地产，无法使用升级卡");
            else
                JOptionPane.showMessageDialog(null, "你还没有任何地产，无法使用升级卡！");
            return false;
        }

        // 所有未满级的地产都可以升级
        List<Land> candidates = new ArrayList<>();
        for (int i = 0; i < landCount; i++)
        {
            Land land = target.getLandOwned(i);
            if (land == null) continue;
            if (land.getOwner() != target) continue;
            if (!land.canLevelUp()) continue;
            candidates.add(land);
        }

        if (candidates.isEmpty())
        {
            if (target.isAI())
                AIDecision.showAIMessage(target.getName() + " 没有可升级的地产（所有地产均已满级）");
            else
                JOptionPane.showMessageDialog(null, "没有可升级的地产（所有地产均已满级）！");
            return false;
        }

        Land chosen = candidates.get(random.nextInt(candidates.size()));
        int before = chosen.getHouseLevel();
        chosen.houseLevelUp();
        int after = chosen.getHouseLevel();

        if (target.isAI())
            AIDecision.showAIMessage(target.getName() + " 升级卡生效：" + chosen.getName() + " 等级 " + before + " -> " + after);
        else
            JOptionPane.showMessageDialog(null, "升级卡生效：" + chosen.getName() + " 等级 " + before + " -> " + after);
        return true;
    }
}
