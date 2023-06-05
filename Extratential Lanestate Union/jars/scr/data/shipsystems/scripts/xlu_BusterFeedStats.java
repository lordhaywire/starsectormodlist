package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class xlu_BusterFeedStats extends BaseShipSystemScript {

	public static final float DAMAGE_BONUS = 1f;
	public static final float ROF_PENALTY = 0.33f;
	public static final float FLUX_PENALTY = 50f;
	
        @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f - ROF_PENALTY * effectLevel;
		float mult2 = 1f + DAMAGE_BONUS * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f + (FLUX_PENALTY * 0.01f));
		stats.getBallisticWeaponDamageMult().modifyMult(id, mult2);
		
	}
        @Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getBallisticWeaponDamageMult().unmodify(id);
	}
	
        @Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f - ROF_PENALTY * effectLevel;
		float mult2 = 1f + DAMAGE_BONUS * effectLevel;
		float bonPer = (int) ((mult - 1f) * 100f);
		float bonPer2 = (int) ((mult2 - 1f) * 100f);
		if (index == 0) {
			return new StatusData("ballistic RoF +" + (int) bonPer + "%", false);
		}
		if (index == 1) {
			return new StatusData("ballistic flux +" + (int) FLUX_PENALTY + "%", false);
		}
		if (index == 2) {
			return new StatusData("ballistic damage +" + (int) bonPer2 + "%", false);
		}
		return null;
	}
}
