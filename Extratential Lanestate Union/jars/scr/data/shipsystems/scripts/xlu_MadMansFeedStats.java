package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class xlu_MadMansFeedStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 0.5f;
	public static final float WEAPON_FLUX_REDUCTION = 0.67f;
	
        @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + ROF_BONUS * effectLevel;
		float beam_mult = 1f + (ROF_BONUS * 0.5f) * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getEnergyRoFMult().modifyMult(id, mult);
		stats.getBeamWeaponDamageMult().modifyMult(id, beam_mult);
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, WEAPON_FLUX_REDUCTION);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, WEAPON_FLUX_REDUCTION);
	}
        @Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getEnergyRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
	}
	
        @Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		float bonusPercent = (float) (mult - 1f) * 100f;
		float fluxPercent = 100 - (WEAPON_FLUX_REDUCTION * 100);
		if (index == 0) {
			return new StatusData("-" + (int) fluxPercent + "% non-missile flux requirements" , false);
		} else if (index == 1) {
			return new StatusData("non-missile rate of fire +" + (int) bonusPercent + "%", false);
		}
		return null;
	}
}
