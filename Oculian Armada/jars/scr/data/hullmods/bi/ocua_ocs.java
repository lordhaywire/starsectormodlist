package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ocua_ocs extends BaseHullMod {
	
	public static final float SYSTEMS_BONUS = 1.2f;
	public static final float RATE_BONUS = 1.25f;
	public static final float EFFICIENCY_BONUS = 1.1f;
	public static final float HULL_BONUS = 20f;
	public static final float ARMOR_BONUS = 20f;
	public static final float FLUX_BONUS = 20f;
	public static final float COST_REDUCTION  = 1;
	public static final float COST_REDUCTION_LIGHT  = 2;
	public static final float COST_REDUCTION_HEAVY  = 3;
	public static final float SUPPLY_USE_MULT = 50f;
	public static final float SUPPLY_USE_FLAT = 2f;
        

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getSuppliesToRecover().modifyPercent(id, SUPPLY_USE_MULT);
                stats.getSuppliesPerMonth().modifyPercent(id, SUPPLY_USE_MULT);
		
                if (hullSize == HullSize.FIGHTER) {
                    stats.getSuppliesToRecover().modifyFlat(id, (0));
                } else if (hullSize == HullSize.FRIGATE) {
                    stats.getSuppliesToRecover().modifyFlat(id, (float) (SUPPLY_USE_FLAT / 2));
                    stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_LIGHT);
                    stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_LIGHT);
	        } else if (hullSize == HullSize.DESTROYER) {
                    stats.getSuppliesToRecover().modifyFlat(id, (float) (SUPPLY_USE_FLAT * 1.5));
                    stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_LIGHT);
                    stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_LIGHT);
	        } else if (hullSize == HullSize.CRUISER) {
                    stats.getSuppliesToRecover().modifyFlat(id, (float) (SUPPLY_USE_FLAT * 2.5));
                    stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_HEAVY);
                    stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_HEAVY);
	        } else if (hullSize == HullSize.CAPITAL_SHIP) {
                    stats.getSuppliesToRecover().modifyFlat(id, (float) (SUPPLY_USE_FLAT * 4));
                    stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_HEAVY);
                    stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_HEAVY);
	        }
                
		stats.getBeamWeaponDamageMult().modifyMult(id, RATE_BONUS);
		stats.getEnergyRoFMult().modifyMult(id, RATE_BONUS);
		stats.getMissileRoFMult().modifyMult(id, RATE_BONUS);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, (2 - EFFICIENCY_BONUS));
		stats.getMissileWeaponFluxCostMod().modifyMult(id, (2 - EFFICIENCY_BONUS));
		stats.getEnergyAmmoBonus().modifyMult(id, RATE_BONUS);
		stats.getMissileAmmoBonus().modifyMult(id, RATE_BONUS);
		stats.getDynamic().getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION);
		
		stats.getArmorBonus().modifyPercent(id, ARMOR_BONUS);
		stats.getHullBonus().modifyPercent(id, HULL_BONUS);
		
		stats.getFluxCapacity().modifyPercent(id, FLUX_BONUS);
		stats.getFluxDissipation().modifyPercent(id, (FLUX_BONUS / 2));
                stats.getVentRateMult().modifyPercent(id, (FLUX_BONUS / 2));
		
		stats.getEngineHealthBonus().modifyMult(id, SYSTEMS_BONUS);
		stats.getWeaponHealthBonus().modifyMult(id, SYSTEMS_BONUS);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
	if(index == 0) return "" + (int) ((RATE_BONUS * 100) - 100) + "%";
	if(index == 1) return "" + (int) ((EFFICIENCY_BONUS * 100) - 100) + "%";
	if(index == 2) return "" + (int) ((SYSTEMS_BONUS * 100) - 100) + "%";
	if(index == 3) return "" + (int) COST_REDUCTION;
	if(index == 4) return "" + (int) (COST_REDUCTION_LIGHT) + "/" + (int) (COST_REDUCTION_LIGHT) + "/" + (int) (COST_REDUCTION_HEAVY) + "/" + (int) (COST_REDUCTION_HEAVY);
	if(index == 5) return "" + (int) (SUPPLY_USE_FLAT / 2) + "/" + (int) (SUPPLY_USE_FLAT * 1.5) + "/" + (int) (SUPPLY_USE_FLAT * 2.5) + "/" + (int) (SUPPLY_USE_FLAT * 4) + " + " + (int) (SUPPLY_USE_MULT) + "%";
        else {
            return null;
        }
    }
    
    @Override
    public boolean affectsOPCosts() {
    	return true;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
