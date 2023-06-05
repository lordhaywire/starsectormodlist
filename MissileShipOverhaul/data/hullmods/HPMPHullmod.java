package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class HPMPHullmod extends BaseHullMod {
	
	public static float MISSILE_SPEED_BONUS = 50f;
	public static final float MISSILE_RANGE_MULT = 200f;
	public static float MISSILE_ACCEL_BONUS = 100f;
	public static float MISSILE_TURN_RATE_PENALTY = 30f;
	public static float MISSILE_TURN_ACCEL_PENALTY = 30f;
	public static float MISSILE_HEALTH_PENALTY = 50f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getMissileMaxSpeedBonus().modifyPercent(id, MISSILE_SPEED_BONUS);
		stats.getMissileWeaponRangeBonus().modifyFlat(id, MISSILE_RANGE_MULT);
		stats.getMissileAccelerationBonus().modifyPercent(id, MISSILE_ACCEL_BONUS);
		stats.getMissileMaxTurnRateBonus().modifyPercent(id, -MISSILE_TURN_RATE_PENALTY);
		stats.getMissileTurnAccelerationBonus().modifyPercent(id, MISSILE_TURN_ACCEL_PENALTY);
		stats.getMissileHealthBonus().modifyPercent(id, -MISSILE_HEALTH_PENALTY);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) MISSILE_SPEED_BONUS + "%";
		if (index == 1) return "" + (int) MISSILE_RANGE_MULT;
		if (index == 2) return "" + (int) MISSILE_TURN_RATE_PENALTY + "%";
		if (index == 3) return "" + (int) MISSILE_HEALTH_PENALTY + "%";
		return null;
	}
	
	
	
}