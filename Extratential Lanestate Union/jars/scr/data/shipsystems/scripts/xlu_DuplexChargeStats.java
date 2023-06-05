package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class xlu_DuplexChargeStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 0.66f;
	public static final float FLUX_REDUCTION = 33f;
	public static final float DAMAGE_BONUS_PERCENT = 33f;
	public static final float EXTRA_DAMAGE_TAKEN_PERCENT = 100f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + ROF_BONUS * effectLevel;
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
		stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getEnergyWeaponDamageMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		float bonusBallistic = (int) ((mult - 1f) * 100f);
		float bonusEnergy = DAMAGE_BONUS_PERCENT * effectLevel;
		float damageTakenPercent = EXTRA_DAMAGE_TAKEN_PERCENT * effectLevel;
		if (index == 0) {
			return new StatusData("ballistic rate of fire +" + (int) bonusBallistic + "%", false);
		}
		if (index == 1) {
			return new StatusData("ballistic flux use -" + (int) FLUX_REDUCTION + "%", false);
		}
		if (index == 2) {
			return new StatusData("+" + (int) bonusEnergy + "% energy weapon damage" , false);
		}
		return null;
	}
}
