package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class xlu_industrial_tracker extends BaseHullMod {

	public static final float COST_REDUCTION  = 10f;
	public static final float GUIDANCE_BONUS  = 1.25f;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
                stats.getMissileGuidance().modifyMult(id, GUIDANCE_BONUS);
                stats.getMissileMaxTurnRateBonus().modifyMult(id, GUIDANCE_BONUS);
                stats.getMissileTurnAccelerationBonus().modifyMult(id, GUIDANCE_BONUS);
		stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) COST_REDUCTION + "";
                if (index == 1) return "" + (int) ((GUIDANCE_BONUS * 100) - 100) + "%";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}
