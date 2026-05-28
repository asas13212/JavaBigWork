package props;

import architecture.Land;
import main.Player;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HouseLevelUp extends Prop
{

    private final Random random = new Random();

    public HouseLevelUp()
    {
        this.setDescription("随机一个未升级地产升级");
        this.setPrice(2000);
        this.setName("升级卡");
    }

    @Override
    public boolean isUsed(Player target)
    {
        if (target == null) return false;

        // 约定：升级卡只对"使用者自己"生效，因此 target 就是自己
        int landCount = target.getLandOwnedCount();
        if (landCount <= 0)
        {
            JOptionPane.showMessageDialog(null, "你还没有任何地产，无法使用升级卡！");
            return false;
        }

        // 先找到所有的 Land，再从里面寻找可以升级的房产
        List<Land> candidates = new ArrayList<>();
        for (int i = 0; i < landCount; i++)
        {
            Land land = target.getLandOwned(i);
            if (land == null) continue;
            if (land.getOwner() != target) continue;
            if (land.getHouseLevel() != 0) continue;
            if (!land.canLevelUp()) continue;
            candidates.add(land);
        }

        if (candidates.isEmpty())
        {
            JOptionPane.showMessageDialog(null, "没有可升级的地产（需要有未升级的已购地产）！");
            return false;
        }

        Land chosen = candidates.get(random.nextInt(candidates.size()));
        int before = chosen.getHouseLevel();
        chosen.houseLevelUp();
        int after = chosen.getHouseLevel();

        JOptionPane.showMessageDialog(null,
                "升级卡生效：" + chosen.getName() + " 等级 " + before + " -> " + after);
        return true;
    }
}
