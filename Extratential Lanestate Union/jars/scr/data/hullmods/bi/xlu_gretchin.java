package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class xlu_gretchin extends BaseHullMod {

        public static final float BALLISTIC_MOD = 3f;
        //public static final float BALLISTIC_MOD_CRUISER = 4f;
        public static final float FRI_BAL_FLUX = 30f;
        public static final float DES_BAL_FLUX = 30f;
        public static final float CRU_BAL_FLUX = 30f;
        public static final float CAP_BAL_FLUX = 30f;
	public static final float SMALL_RANGE_THRESHOLD = 700f;
	public static final float MEDIUM_RANGE_THRESHOLD = 800f;
	public static final float LARGE_RANGE_THRESHOLD = 900f;
	public static final float RANGE_MULT = 0.25f;
	
        
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            if (hullSize == HullSize.FRIGATE) {
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 -(FRI_BAL_FLUX / 100)));
                stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -BALLISTIC_MOD);
                //if (!(stats.getWeaponRangeMultPastThreshold().getFlatMod() > SMALL_RANGE_THRESHOLD)) {
                if (!stats.getVariant().getHullMods().contains("safetyoverrides")) {
                    stats.getWeaponRangeThreshold().modifyFlat(id, SMALL_RANGE_THRESHOLD);
                }
	    } else if (hullSize == HullSize.DESTROYER) {
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 -(DES_BAL_FLUX / 100)));
                stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -BALLISTIC_MOD);
                //if (!(stats.getWeaponRangeMultPastThreshold().getFlatMod() > SMALL_RANGE_THRESHOLD)) {
                if (!stats.getVariant().getHullMods().contains("safetyoverrides")) {
                    stats.getWeaponRangeThreshold().modifyFlat(id, SMALL_RANGE_THRESHOLD);
                }
	    } else if (hullSize == HullSize.CRUISER) {
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 -(CRU_BAL_FLUX / 100)));
                stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -BALLISTIC_MOD);
                //if (!(stats.getWeaponRangeMultPastThreshold().getFlatMod() > MEDIUM_RANGE_THRESHOLD)) {
                if (!stats.getVariant().getHullMods().contains("safetyoverrides")) {
                    stats.getWeaponRangeThreshold().modifyFlat(id, MEDIUM_RANGE_THRESHOLD);
                }
	    } else if (hullSize == HullSize.CAPITAL_SHIP) {
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 -(CAP_BAL_FLUX / 100)));
                stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -BALLISTIC_MOD);
                //if (!(stats.getWeaponRangeMultPastThreshold().getFlatMod() > LARGE_RANGE_THRESHOLD)) {
                if (!stats.getVariant().getHullMods().contains("safetyoverrides")) {
                    stats.getWeaponRangeThreshold().modifyFlat(id, LARGE_RANGE_THRESHOLD);
                }
	    }
                
            if (!stats.getVariant().getHullMods().contains("safetyoverrides")) {
                stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT);
            }
		
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
	if(index == 0) {
            if(hullSize == HullSize.FRIGATE || hullSize == HullSize.DESTROYER || hullSize == HullSize.CRUISER) return "Medium Ballistics by " + (int) BALLISTIC_MOD;
            //else if(hullSize == HullSize.CRUISER) return "Medium Ballistics by " + (int) BALLISTIC_MOD_CRUISER;
            else if(hullSize == HullSize.CAPITAL_SHIP) return "Large Ballistics by " + (int) BALLISTIC_MOD;
            else return null;
        }
	if(index == 1) {
            if(hullSize == HullSize.FRIGATE) return "" + (int) FRI_BAL_FLUX + "%";
            else if(hullSize == HullSize.DESTROYER) return "" + (int) DES_BAL_FLUX + "%";
            else if(hullSize == HullSize.CRUISER) return "" + (int) CRU_BAL_FLUX + "%";
            else if(hullSize == HullSize.CAPITAL_SHIP) return "" + (int) CAP_BAL_FLUX + "%";
            else return null;
        }
	if(index == 2) return "" + (int) (100 - (RANGE_MULT * 100)) + "%";
	if(index == 3) {
            if(hullSize == HullSize.FRIGATE || hullSize == HullSize.DESTROYER) return Misc.getRoundedValue(SMALL_RANGE_THRESHOLD);
            else if(hullSize == HullSize.CRUISER) return Misc.getRoundedValue(MEDIUM_RANGE_THRESHOLD);
            else if(hullSize == HullSize.CAPITAL_SHIP) return Misc.getRoundedValue(LARGE_RANGE_THRESHOLD);
            else return null;
        }
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
    	if (ship.getHullSpec().getHullId().startsWith("xlu_")) return true;
//        else if (ship.getVariant().hasHullMod(HullMods.SAFETYOVERRIDES)) return false;
		
	return true;
	}
}
