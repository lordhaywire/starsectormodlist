package hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

/**
 * Author: SafariJohn
 */
public class Roider_FirestormMod extends BaseHullMod {

	public static final float COST_MOD = 4;

	public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -COST_MOD);
		stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyFlat(id, -COST_MOD);
	}

	public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 0) return "" + (int) COST_MOD + "";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}
