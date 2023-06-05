package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class xlu_crew_base extends BaseHullMod {

    protected static final float PARA_PAD = 10f;
    protected static final float SECTION_PAD = 10f;
    protected static final float INTERNAL_PAD = 4f;
    protected static final float INTERNAL_PARA_PAD = 4f;
    
    protected static final float LOAD_OF_BULL = 3f;

        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
            return null;
        }
        
        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
            return true;
        }
    
        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        }
        
	@Override
	public boolean affectsOPCosts() {
		return true;
	}

        @Override
        public String getUnapplicableReason(ShipAPI ship) {
            /*if (ship == null || !ship.getVariant().getHullMods().contains("xlu_armorclad")) {
               return "Must have the XLU Armorclad Works";
            }*/
            if ((ship.getVariant().getHullMods().contains("automated") && ship.getHullSpec().getMinCrew() == 0f) ||
                    (ship.getVariant().getHullMods().contains("ocua_drone_mod") && ship.getHullSpec().getMinCrew() <= 1f)) {
               return "Cannot provide living space on an automated ship that does not have capacity";
            }
            if (ship.getVariant().getHullMods().contains("xlu_crew_hardyboys") ||
                ship.getVariant().getHullMods().contains("xlu_crew_warshots") ||
                ship.getVariant().getHullMods().contains("xlu_crew_waymakers")) {
               return "Lanestate Auxilliary already settled";
            }
        
            return null;
        }
}
