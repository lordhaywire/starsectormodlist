package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ocua_z_strikecraft_hullmod extends BaseHullMod {

        public static final float ENERGY_FLUX = 80f;
	public static final float HARD_VENT_PERCENT = 10f;
        
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 -(ENERGY_FLUX / 100)));
            stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 -(ENERGY_FLUX / 100)));
            
            stats.getHardFluxDissipationFraction().modifyFlat(id, HARD_VENT_PERCENT * 0.01f);
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
	if(index == 0) return "" + (int) ENERGY_FLUX + "%";
        return null;
    }
}
