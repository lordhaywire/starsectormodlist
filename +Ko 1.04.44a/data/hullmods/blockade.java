package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class blockade extends BaseHullMod {

	public static final float HE_DAMAGE_REDUCTION = 0.20f;
	public static float ZERO_FLUX_LEVEL = 10f;
	public static final float BLOCKADE_BOOST = 25f;
	private static final int BURN_BOOST = 1;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHighExplosiveDamageTakenMult().modifyMult(id, 1f - HE_DAMAGE_REDUCTION);
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, ZERO_FLUX_LEVEL * 0.01f);

		boolean sMod = isSMod(stats);
		if (sMod) {
		    stats.getZeroFluxSpeedBoost().modifyFlat(id, BLOCKADE_BOOST);
		    stats.getMaxBurnLevel().modifyFlat(id, BURN_BOOST);
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) (ZERO_FLUX_LEVEL) + "%";
		if (index == 1) return "" + (int) Math.round(HE_DAMAGE_REDUCTION * 100f) + "%";
		return null;
	}

	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) (BLOCKADE_BOOST);
		if (index == 1) return "+" + (int) (BURN_BOOST);
		return null;
	}


}








