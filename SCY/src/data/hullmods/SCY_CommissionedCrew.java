package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;
import static data.scripts.util.SCY_txt.txt;

public class SCY_CommissionedCrew extends BaseHullMod { 
    
    private final float SENSOR_IMPROVEMENT=15;
    private final String CREW="CHM_commission";

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSensorProfile().modifyPercent(id, -SENSOR_IMPROVEMENT);
        stats.getSensorStrength().modifyPercent(id, SENSOR_IMPROVEMENT);
    }
    
    //auto remove crew hullmod
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
        if (ship.getVariant().getHullMods().contains(CREW)) {                
            ship.getVariant().removeMod(CREW);      
        }        
    }
	
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return SENSOR_IMPROVEMENT+txt("%");
        if (index == 1) return "-"+SENSOR_IMPROVEMENT+txt("%");
        return null;
    }
}
