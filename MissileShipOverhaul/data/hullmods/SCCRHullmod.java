package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.combat.BaseHullMod;

public class SCCRHullmod extends BaseHullMod {
	
	public static final float HULL_PENALTY = 60f;
	public static float RECOVERY_CHANCE_PERCENT = 50f;
	public static float ARMOR_REDUCTION_PERCENT = 90f;
	public static float CARGO_BONUS = 0f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		CARGO_BONUS = stats.getVariant().getHullSpec().getArmorRating() * ARMOR_REDUCTION_PERCENT / 100f;
		stats.getCargoMod().modifyFlat(id, CARGO_BONUS);
		stats.getArmorBonus().modifyMult(id, 1f - ARMOR_REDUCTION_PERCENT/100f);
		stats.getHullBonus().modifyPercent(id, -HULL_PENALTY);
		stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyMult(id, 1f - RECOVERY_CHANCE_PERCENT / 100f);
	}
	
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ARMOR_REDUCTION_PERCENT + "%";
		if (index == 1) return "" + (int) HULL_PENALTY + "%";
		if (index == 2) return "" + (int) CARGO_BONUS;
		if (index == 3) return "" + (int) RECOVERY_CHANCE_PERCENT + "%";
		return null;
	}
	
	
}