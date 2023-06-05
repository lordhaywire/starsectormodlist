package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ocua_external_carriage extends BaseHullMod {

	public static final float SUPPLY_USE_MULT = 0.67f;
	public static final float SUPPLY_RECOVERY_MULT = 2f;
	public static final float CREW_DEDUCTION = 2f;
	public static final float PROFILE_ADD = 60f;
	public static final float SENSOR_DEDUCTION = 30f;
	public static final float FIRING_BONUS = 1.15f;
   
   @Override
   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
                stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT);
                stats.getSuppliesToRecover().modifyMult(id, SUPPLY_RECOVERY_MULT);
		stats.getSensorProfile().modifyFlat(id, PROFILE_ADD);
		stats.getSensorStrength().modifyFlat(id, -SENSOR_DEDUCTION);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, (2 - FIRING_BONUS));
		stats.getMissileWeaponFluxCostMod().modifyMult(id, (2 - FIRING_BONUS));
		stats.getEnergyRoFMult().modifyMult(id, FIRING_BONUS);
		stats.getMissileRoFMult().modifyMult(id, FIRING_BONUS);
		stats.getBeamWeaponDamageMult().modifyMult(id, FIRING_BONUS);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if (index == 0) return "" + (int) (100 - (100 * (SUPPLY_USE_MULT))) + "%";
	if (index == 1) return "" + (int) ((100 * (SUPPLY_RECOVERY_MULT)) - 100) + "%";
	if (index == 2) return "" + (int) PROFILE_ADD;
	if (index == 3) return "" + (int) SENSOR_DEDUCTION;
        if (index == 4) return "" + (int) ((100 * (FIRING_BONUS)) - 100) + "%";
	return null;
    }
	
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
