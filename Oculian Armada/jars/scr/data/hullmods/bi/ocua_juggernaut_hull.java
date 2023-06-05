package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import data.scripts.everyframe.OCUA_BlockedHullmodDisplayScript;
import java.util.HashSet;
import java.util.Set;

public class ocua_juggernaut_hull extends BaseHullMod {
	
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("targetingunit");
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
    }
    
	public static final float SENSOR_ADD = 60f;
	public static final float PROFILE_ADD = 30f;
	public static final float SIGHT_BONUS = 20f;
	public static final float TURRET_BONUS = 50f;
	public static final float RADIUS_MULT = 1.2f;
	public static final float DAMAGE_MULT = 1.5f;
        
	public static final float RANGE_BONUS = 70f; //70f; //10f;
	public static final float PD_MINUS = 10f;
        
	//public static final float ROF_BONUS = 25f;
	//public static final float EFFICIENCY_BONUS = 25f;

	//public static final float RANGE_FRIGATE = 50f;
	//public static final float RANGE_DESTROYER = 40f;
	//public static final float RANGE_CRUISER = 20f;
        
        @Override
        public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
                stats.getSensorStrength().modifyFlat(id, SENSOR_ADD);
                stats.getSensorProfile().modifyFlat(id, PROFILE_ADD);
                stats.getSightRadiusMod().modifyMult(id, (1 + ((100 + SIGHT_BONUS) / 100)));
            
                //stats.getEnergyRoFMult().modifyMult(id, (1 + (ROF_BONUS / 100)));
                //stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 + (EFFICIENCY_BONUS / 100)));
                //stats.getBeamWeaponDamageMult().modifyPercent(id, ROF_BONUS);
            
                //if (hullSize == HullSize.FRIGATE) {
                //    stats.getEffectiveArmorBonus().modifyPercent(id, RANGE_FRIGATE);
                //} else if (hullSize == HullSize.DESTROYER) {
                //    stats.getEffectiveArmorBonus().modifyPercent(id, RANGE_DESTROYER);
                //} else if (hullSize == HullSize.CRUISER) {
                //    stats.getEffectiveArmorBonus().modifyPercent(id, RANGE_CRUISER);
                //}
                stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
                stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
                stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, (RANGE_BONUS - PD_MINUS));
                stats.getBeamPDWeaponRangeBonus().modifyPercent(id, (RANGE_BONUS - PD_MINUS));
            
                stats.getWeaponHealthBonus().modifyMult(id, (1 + (TURRET_BONUS / 100)));
                
                stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, DAMAGE_MULT);
                stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, RADIUS_MULT);
        }
	
        @Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            for (String tmp : BLOCKED_HULLMODS) {
                if (ship.getVariant().getHullMods().contains(tmp)) {
                    ship.getVariant().removeMod(tmp);
                    OCUA_BlockedHullmodDisplayScript.showBlocked(ship);
                }
            }
	}
	
        @Override
        public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) PROFILE_ADD;
		if (index == 1) return "" + (int) SENSOR_ADD;
		if (index == 2) return "" + (int) SIGHT_BONUS + "%";
		if (index == 3) return "" + (int) TURRET_BONUS + "%";
		if (index == 4) return "" + (int) RANGE_BONUS + "%";
		if (index == 5) return "" + (int) (RANGE_BONUS - PD_MINUS) + "%";
                if (index == 6) return "range extension hullmods";
                
                return null;
        }

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
            return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
        }
}
