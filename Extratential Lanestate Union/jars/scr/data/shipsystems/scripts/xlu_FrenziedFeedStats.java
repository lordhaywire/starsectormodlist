package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class xlu_FrenziedFeedStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 1.5f;
	public static final float FLUX_REDUCTION = 67f;
	public static final float TURN_REDUCTION = 50f;
        
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + ROF_BONUS * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
	//	stats.getMaxTurnRate().modifyPercent(id, -TURN_REDUCTION);
	//	stats.getTurnAcceleration().modifyPercent(id, -TURN_REDUCTION);
	}
        
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
	//	stats.getMaxTurnRate().unmodify(id);
	//	stats.getTurnAcceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 0) {
			return new StatusData("ballistic rate of fire +" + (int) bonusPercent + "%", false);
		}
		if (index == 1) {
			return new StatusData("ballistic flux use -" + (int) FLUX_REDUCTION + "%", false);
		}
		/*if (index == 2) {
			return new StatusData("turn speed -" + (int) TURN_REDUCTION + "%", false);
		}*/
		return null;
	}
}
