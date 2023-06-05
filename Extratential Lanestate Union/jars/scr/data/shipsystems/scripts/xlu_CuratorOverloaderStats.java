package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class xlu_CuratorOverloaderStats extends BaseShipSystemScript {

	public static final float DAMAGE_BONUS = 0.2f;
	public static final float PROJECTILE_SPEED = 0.5f;
	public static final float RANGE_BONUS = 50f;
        
	//public static final float FLUX_PENALTY = 1.1f;
	public static final float ROF_PENALTY = 0.8f;
	public static final float TURN_PENALTY = 0.33f;
	public static final float BREAK_SPEED = 0.1f;
	public static final float BREAK_DECELERATION = 3f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		float dam_mult = 1f + DAMAGE_BONUS * effectLevel;
		float proj_mult = 1f + PROJECTILE_SPEED * effectLevel;
		float range_perc = RANGE_BONUS * effectLevel;
		stats.getBallisticWeaponDamageMult().modifyMult(id, dam_mult);
		stats.getProjectileSpeedMult().modifyMult(id, proj_mult);
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, range_perc);
                
		float break_speed = 1 - ((1 - BREAK_SPEED) * effectLevel);
		//stats.getBallisticWeaponFluxCostMod().modifyPercent(id, FLUX_PENALTY);
		stats.getBallisticRoFMult().modifyMult(id, ROF_PENALTY);
		stats.getMaxTurnRate().modifyPercent(id, TURN_PENALTY);
		stats.getMaxSpeed().modifyMult(id, break_speed);
                
		if (effectLevel < 1) {
                    stats.getBallisticRoFMult().modifyMult(id, 0);
                }
	}
        
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponDamageMult().unmodify(id);
		stats.getProjectileSpeedMult().unmodify(id);
		stats.getBallisticWeaponRangeBonus().unmodify(id);
                
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getMaxSpeed().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float dam_mult = 1f + DAMAGE_BONUS * effectLevel;
		float range_mult = RANGE_BONUS * effectLevel;
		float dam_Percent = (int) ((dam_mult - 1f) * 100f);
		float range_Percent = (int) range_mult;
		if (index == 0) {
			return new StatusData("ballistic damage +" + (int) dam_Percent + "%", false);
		}
		if (index == 1) {
			return new StatusData("ballistic range +" + (int) range_Percent + "%", false);
		}
		if ((index == 2) && (effectLevel < 1)) {
			return new StatusData("Systems transitioning, Weapons Disabled", false);
		}
		if ((index == 2) && (effectLevel == 1)) {
			return new StatusData("'Ere 'ey come!", false);
		}
		return null;
	}
}
