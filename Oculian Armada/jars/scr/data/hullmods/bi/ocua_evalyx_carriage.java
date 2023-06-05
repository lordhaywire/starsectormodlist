package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ocua_evalyx_carriage extends BaseHullMod {

	public static final float SUPPLY_USE_MULT = 0.5f;
	public static final float FIRING_BONUS = 1.15f;
   
   @Override
   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
                stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, (2 - FIRING_BONUS));
		stats.getMissileWeaponFluxCostMod().modifyMult(id, (2 - FIRING_BONUS));
		stats.getEnergyRoFMult().modifyMult(id, FIRING_BONUS);
		stats.getMissileRoFMult().modifyMult(id, FIRING_BONUS);
		stats.getBeamWeaponDamageMult().modifyMult(id, FIRING_BONUS);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if (index == 0) return "" + (int) (100 - (100 * (SUPPLY_USE_MULT))) + "%";
        if (index == 1) return "" + (int) ((100 * (FIRING_BONUS)) - 100) + "%";
	return null;
    }
	
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
