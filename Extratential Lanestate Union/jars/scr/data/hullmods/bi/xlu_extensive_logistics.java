package data.hullmods.bi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import data.scripts.plugins.xlu_BlockedHullmodDisplayScript;
import java.util.HashSet;
import java.util.Set;

public class xlu_extensive_logistics extends BaseHullMod {
    public static final int MAX_LOG = 1;
    public static final float RANGE_BONUS = 60f;
    //public static final float PD_MINUS = 10f;
        
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("targetingunit");
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        int logM = 0;
	for (String huge : stats.getVariant().getHullMods()) {
            HullModSpecAPI spec = Global.getSettings().getHullModSpec(huge);
            if (spec.hasUITag("Logistics")) logM++;
	}
        stats.getDynamic().getMod(Stats.MAX_LOGISTICS_HULLMODS_MOD).modifyFlat(id, MAX_LOG);
        
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        //stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, (RANGE_BONUS));
        //stats.getBeamPDWeaponRangeBonus().modifyPercent(id, (RANGE_BONUS));
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
	if (index == 0) return "" + MAX_LOG;
	if (index == 1) return "" + (int) RANGE_BONUS + "%";
	//if (index == 2) return "" + (int) (RANGE_BONUS - PD_MINUS) + "%";
        if (index == 2) return "range extension hullmods";
	return null;
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                xlu_BlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
    }
}
