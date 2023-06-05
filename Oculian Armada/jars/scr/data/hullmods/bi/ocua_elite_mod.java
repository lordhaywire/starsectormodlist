package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ocua_elite_mod extends BaseHullMod {
	
	//public static final float RANK_BONUS = 0.15f;
	//public static final float SUPPLY_USE_MULT = 25f;
	//public static final float HULL_BONUS = 15f;
	//public static final float ARMOR_BONUS = 15f;
	//public static final float FLUX_BONUS = 20f;
	public static final float DAMAGE_BONUS = 10f;
	public static final float OVERALL_BONUS = 1.25f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		//stats.getMinCrewMod().modifyMult(id, 0);
		//stats.getMaxCrewMod().modifyMult(id, 0);
		//stats.getMaxCombatReadiness().modifyFlat(id, RANK_BONUS);
		
		stats.getEnergyWeaponDamageMult().modifyMult(id, 1 + (DAMAGE_BONUS / 100));
		stats.getMissileWeaponDamageMult().modifyMult(id, 1 + (DAMAGE_BONUS / 100));
		stats.getBeamWeaponDamageMult().modifyMult(id, 1 + ((DAMAGE_BONUS / 100) / 3.14f));
                
		//stats.getArmorBonus().modifyPercent(id, HULL_BONUS);
		//stats.getHullBonus().modifyPercent(id, ARMOR_BONUS);
                
		//stats.getFluxCapacity().modifyPercent(id, FLUX_BONUS);
		//stats.getFluxDissipation().modifyPercent(id, (FLUX_BONUS / 2));
		stats.getAutofireAimAccuracy().modifyMult(id, OVERALL_BONUS);
		stats.getWeaponTurnRateBonus().modifyMult(id, OVERALL_BONUS);
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1 / OVERALL_BONUS);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1 / OVERALL_BONUS);
		
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
                if(index == 0) return "" + (int) (OVERALL_BONUS) + "%";
                if(index == 1) return "" + (int) (DAMAGE_BONUS) + "%";
                //else if(index == 3) return "" + (int) (FLUX_BONUS / 2) + "%";
                //else if(index == 4) return "" + (int) (SUPPLY_USE_MULT) + "%";
                //else if(index == 1) return "" + (int) (ARMOR_BONUS) + "%";
                
		//if(index == 0) return "" + (int) (RANK_BONUS * 100) + "%";
		//else if(index == 1) return "" + (int) (SUPPLY_USE_MULT) + "%";
        else {
            return null;
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
