package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
//import com.fs.starfarer.api.impl.campaign.ids.Factions;
//import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class CHM_ocua extends BaseHullMod {

	public static final float ENERGY_FLUX_FRIGATE = 10f;
	public static final float ENERGY_FLUX_DESTROYER = 7.5f;
	public static final float ENERGY_FLUX_CRUISER = 5f;
	public static final float ENERGY_FLUX_CAPITAL = 5f;
	public static final float SHIELD_UPKEEP = 10f;
        //public static final float BALLISTIC_BONUS = 5f;
        //public static final float BALLISTIC_FLUX = 10f;
        //public static final float ACCURACY_DEBUFF = 15f;
        
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

            if (hullSize == HullSize.FRIGATE) {
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (ENERGY_FLUX_FRIGATE / 100)));
                stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (ENERGY_FLUX_FRIGATE / 100)));
            } else if (hullSize == HullSize.DESTROYER) {
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (ENERGY_FLUX_DESTROYER / 100)));
                stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (ENERGY_FLUX_DESTROYER / 100)));
            } else if (hullSize == HullSize.CRUISER) {
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (ENERGY_FLUX_CRUISER / 100)));
                stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (ENERGY_FLUX_CRUISER / 100)));
            } else if (hullSize == HullSize.CAPITAL_SHIP) {
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (ENERGY_FLUX_CAPITAL / 100)));
                stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (ENERGY_FLUX_CAPITAL / 100)));
            }
            stats.getShieldUpkeepMult().modifyMult(id, (1 - (SHIELD_UPKEEP / 100)));
            //stats.getBallisticRoFMult().modifyPercent(id, BALLISTIC_BONUS);
            //stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 -(BALLISTIC_FLUX / 100)));
            //stats.getAutofireAimAccuracy().modifyPercent(id, ACCURACY_DEBUFF);
    }
    
    @Override //All you need is this to be honest. The framework will do everything on its own.
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
                if (ship.getVariant().hasHullMod("CHM_commission")) {
                    ship.getVariant().removeMod("CHM_commission");
                }
    }
    
	public String getDescriptionParam(int index, HullSize hullSize) {
                if (index == 0) return "10%/7.5%/5%/5%";
                if (index == 1) return "" + (int) SHIELD_UPKEEP + "%";
		
		return null;
	}

}
