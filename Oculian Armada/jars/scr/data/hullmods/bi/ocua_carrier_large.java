package data.hullmods.bi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import java.util.ArrayList;
import java.util.List;

public class ocua_carrier_large extends BaseHullMod {
    
	public static final float CARRIER_REFIT = 0.67f;
	public static final float CARRIER_REPLACEMENT_LOSS = 0.75f;
        
        public static final float DAMAGE_BONUS = 1.1f;
        public static final float WING_RANGE = 1000f;
        public static final float WING_TIME = 86400f;

        @Override
        public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
                stats.getFighterWingRange().modifyFlat(id, WING_RANGE);
                
                stats.getFighterRefitTimeMult().modifyMult(id, CARRIER_REFIT);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, CARRIER_REPLACEMENT_LOSS);
        }

    //The code has to run every frame to work properly. Also, i have the distinct feeling it will not work on a ship with Reserve Deployments.
        @Override
        public void advanceInCombat(ShipAPI ship, float amount) {
            if (ship != null) {
                List<String> builtIns = ship.getHullSpec().getBuiltInWings();

                for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
                    if (bay.getWing() == null) continue;

                    //bay.makeCurrentIntervalFast();
                    FighterWingSpecAPI spec = bay.getWing().getSpec();

                    //Only affects drones: delete this if you want it to affect fighters with crew, too
                    if (spec.getVariant().getHullSpec().getMinCrew() != 0) continue;
                    
                    //Affects exclusively to Oculian fighters with the according prefix.
                    if (!spec.getVariant().getHullSpec().getHullId().startsWith("ocua_")) continue;

                    //The actual code which adds fighters: slightly differs from the vanilla Reserve Deployments, due to it not deploying extra fighters faster than normal
                    int addForWing = getAdditionalFor(spec);
                    int maxTotal = spec.getNumFighters() + addForWing;
                    int actualAdd = maxTotal - bay.getWing().getWingMembers().size();
                    if (actualAdd > 0) {
                        bay.setExtraDeployments(actualAdd);
                        bay.setExtraDeploymentLimit(maxTotal);
                        bay.setExtraDuration(WING_TIME);
                    }
                }
            }
            
                for (ShipAPI fighter : getFighters(ship)) {
                    if (fighter.isHulk()) continue;
                            MutableShipStatsAPI fStats = fighter.getMutableStats();
				
                            fStats.getBallisticWeaponDamageMult().modifyMult(null, DAMAGE_BONUS);
                            fStats.getEnergyWeaponDamageMult().modifyMult(null, DAMAGE_BONUS);
                            fStats.getMissileWeaponDamageMult().modifyMult(null, DAMAGE_BONUS);
				
                    
		}
    }

	private List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<ShipAPI>();
		
//		this didn't catch fighters returning for refit		
//		for (FighterLaunchBayAPI bay : carrier.getLaunchBaysCopy()) {
//			if (bay.getWing() == null) continue;
//			result.addAll(bay.getWing().getWingMembers());
//		}
		
		for (ShipAPI ship : Global.getCombatEngine().getShips()) {
			if (!ship.isFighter()) continue;
			if (ship.getWing() == null) continue;
			if (ship.getWing().getSourceShip() == carrier) {
				result.add(ship);
			}
		}
		
		return result;
	}
	
	public static int getAdditionalFor(FighterWingSpecAPI spec) {
		int size = spec.getNumFighters();
		if (size <= 1) return 1;
		if (size <= 2) return 2;
		if (size <= 3) return 3;
		return 4;
	}
	
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
            //if(index == 0) return "" + (int) FIGHTER_NUMBER_ADD + "%";
            if (index == 0) return "4";
            else if (index == 1) return "" + WING_RANGE + " units";
            else if (index == 2) return "" + CARRIER_REFIT + " %";
            else if (index == 3) return "" + CARRIER_REPLACEMENT_LOSS + " %";
            else if (index == 4) return "" + (int) ((100 * DAMAGE_BONUS) - 100) + "%";
            else {
                return null;
            }
            
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
