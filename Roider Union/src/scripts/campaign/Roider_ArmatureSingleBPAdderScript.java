package scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Equipment;

/**
 * Author: SafariJohn
 */
public class Roider_ArmatureSingleBPAdderScript implements EveryFrameScript {
    public static final String KNOWS_ARMATURE = "$roider_knowsArmature";

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_ArmatureSingleBPAdderScript.class, "tracker", "t");
    }

    IntervalUtil tracker;

    public Roider_ArmatureSingleBPAdderScript() {
        tracker = new IntervalUtil(0.33f, 0.5f);
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        tracker.advance(Misc.getDays(amount));
        if (!tracker.intervalElapsed()) return;

        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (faction.getMemoryWithoutUpdate().contains(KNOWS_ARMATURE)) continue;

            boolean knowsWing = faction.knowsFighter(Roider_Equipment.ARMATURE_WING);
            boolean knowsSingle = faction.knowsFighter(Roider_Equipment.ARMATURE_SINGLE);

            if (knowsSingle && knowsWing) {
                faction.getMemoryWithoutUpdate().set(KNOWS_ARMATURE, true);
                continue;
            }

            if (!knowsSingle && !knowsWing) continue;

            if (!knowsWing && knowsSingle) faction.addKnownFighter(Roider_Equipment.ARMATURE_WING, true);
            if (!knowsSingle && knowsWing) faction.addKnownFighter(Roider_Equipment.ARMATURE_SINGLE, true);
        }
    }
}
