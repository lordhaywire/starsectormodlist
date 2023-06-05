package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ocua_gretly_explosion extends BaseHullMod {

	public static final float DAMAGE_MULT = 4.0f;
	public static final float RADIUS_MULT = 1.75f;
	//public static final float EXPLOSIVE_SIZE = 2.0f;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, DAMAGE_MULT);
		stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, RADIUS_MULT);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
            if(index == 0) return "" + (int) (DAMAGE_MULT * 100) + "%";
            if(index == 1) return "" + (int) ((RADIUS_MULT * 100) - 100) + "%";
            if(index == 2) return "Gretly";
            if(index == 3) return "Use this modification at your own risk.";
            
            else {
                return null;
            }
	}

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
            return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_gretly");
        }
    

        @Override
        public String getUnapplicableReason(ShipAPI ship) {
            if (ship == null || !ship.getHullSpec().getHullId().equals("ocua_gretly")) {
                return "Gretly class drone frigate required";
            }
        
            return null;
        }

}
