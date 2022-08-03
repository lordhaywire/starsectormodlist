package scripts.campaign.cleanup;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Hullmods;
import java.util.HashSet;
import java.util.Set;
import scripts.campaign.rulecmd.Roider_MadRockpiper;

/**
 * Fixes bug where build-in menu de-permafies built in MIDAS
 * Author: SafariJohn
 */
public class Roider_MadMIDASHealer implements EveryFrameScript {

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_MadMIDASHealer.class, "interval", "i");
    }

    private IntervalUtil interval = new IntervalUtil(1f, 1f);

    private static transient Set<String> madMidasShips = new HashSet<>();

    public static boolean isMadMidas(String memberId) {
        return madMidasShips.contains(memberId);
    }

    public Roider_MadMIDASHealer() {
        madMidasShips.clear();
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
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.REFIT) return;

        interval.advance(amount);
        if (!interval.intervalElapsed()) return;

        for (FleetMemberAPI m : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
            ShipVariantAPI variant = m.getVariant();

            // Heal lost MIDAS
            if (madMidasShips.contains(m.getId()) && !Roider_MadRockpiper.hasBuiltInMIDAS(m)) {
                if (variant.getSMods().contains(Roider_Hullmods.MIDAS)) variant.removePermaMod(Roider_Hullmods.MIDAS);
                variant.addPermaMod(Roider_Hullmods.MIDAS, false);
            }
        }

        madMidasShips.clear();
        for (FleetMemberAPI m : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
            // Rebuild ship list
            if (Roider_MadRockpiper.hasBuiltInMIDAS(m)) {
                madMidasShips.add(m.getId());
            }
        }

    }

}
