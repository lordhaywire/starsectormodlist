package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ocua_EnergyFeedStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 1f;
    public static final float WEAPON_FLUX_REDUCTION = 0.67f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + ROF_BONUS * effectLevel;
		stats.getEnergyRoFMult().modifyMult(id, mult);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, WEAPON_FLUX_REDUCTION);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyRoFMult().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		float bonusPercent = (int) (mult - 1f) * 100f;
		float fluxPercent = 100 - (WEAPON_FLUX_REDUCTION * 100);
		if (index == 0) {
			return new StatusData("-" + (int) fluxPercent + "% energy flux requirements" , false);
		} else if (index == 1) {
			return new StatusData("energy rate of fire +" + (int) bonusPercent + "%", false);
		}
		return null;
	}
}
