package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.util.MagicIncompatibleHullmods;
import java.util.HashSet;
import java.util.Set;

public class TADA_fullRacks extends BaseHullMod {
    
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("missleracks");   
        BLOCKED_HULLMODS.add("VEmissleracks");
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) { 
        for (String tmp : BLOCKED_HULLMODS) {
            if(stats.getVariant().getHullMods().contains(tmp)){
                MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), tmp, "TADA_fullRacks");
            }
        }
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
}
