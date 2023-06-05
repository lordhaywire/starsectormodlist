package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;




@SuppressWarnings("unchecked")
public class OTPHullmod extends BaseHullMod {
	
	public static final float FIRE_RATE_BONUS = 10f;
	public static float FLUX_DISSIPATION_PERCENT = 0.9f;
	public static float VISION_RANGE_MULTIPLIER = 1f;
	public static float AUTOFIRE_AIM_BONUS = 0.3f;
	public static float OVERLOAD_PERCENT = 25f;
	
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_PERCENT);
		stats.getSightRadiusMod().modifyMult(id, 1f + VISION_RANGE_MULTIPLIER);
		stats.getBallisticRoFMult().modifyMult(id, 1f + FIRE_RATE_BONUS * 0.01f);
		stats.getEnergyRoFMult().modifyMult(id, 1f + FIRE_RATE_BONUS * 0.01f);
		stats.getAutofireAimAccuracy().modifyFlat(id, AUTOFIRE_AIM_BONUS);
		stats.getOverloadTimeMod().modifyMult(id, 1f + OVERLOAD_PERCENT / 100f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FIRE_RATE_BONUS + "%";
		if (index == 1) return "" + (int)Math.round(AUTOFIRE_AIM_BONUS * 100f) + "%";
		if (index == 2) return "" + (int)Math.round(VISION_RANGE_MULTIPLIER * 100f) + "%";
		if (index == 3) return "" + (int)Math.round((1f - FLUX_DISSIPATION_PERCENT) * 100f) + "%";
		if (index == 4) return "" + (int) OVERLOAD_PERCENT + "%";
		return null;
	}
	
	
	
	
	
	
	
	
}