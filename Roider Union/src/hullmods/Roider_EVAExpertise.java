package hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

/**
 * Author: SafariJohn
 */
public class Roider_EVAExpertise extends BaseHullMod {
	public static final float REPAIR_BONUS = 20f;
	public static final float CASUALTY_REDUCTION = 20f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		stats.getCrewLossMult().modifyMult(id, 1f - CASUALTY_REDUCTION * 0.01f);
    }

	public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 0) return "" + (int) REPAIR_BONUS + "%";
		if (index == 1) return "" + (int) CASUALTY_REDUCTION + "%";
		return null;
	}

}
