package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class xlu_robust_construct extends BaseHullMod {

	public static float DEPLOYMENT_COST_MULT = 25f;
	
	public static float DMOD_EFFECT_MULT = 0.5f;
	public static float DMOD_AVOID_CHANCE = 50f;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).modifyMult(id, DMOD_EFFECT_MULT);
		stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, (1f - DMOD_AVOID_CHANCE * 0.01f));
		
		stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
		
		stats.getSuppliesToRecover().modifyMult(id, (1f - DEPLOYMENT_COST_MULT * 0.01f));
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round((1f - DMOD_EFFECT_MULT) * 100f) + "%";
		if (index == 1) return "" + (int) DMOD_AVOID_CHANCE + "%";
		if (index == 2) return "" + (int) DEPLOYMENT_COST_MULT + "%";
		return null;
	}
}








