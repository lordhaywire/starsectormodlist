package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import data.scripts.plugins.xlu_BlockedHullmodDisplayScript;
import java.util.HashSet;
import java.util.Set;
public class xlu_closed_shell extends BaseHullMod {

    public static final float SHIELD_PENALTY = 0.5f;
    public static final float SHIELD_MOVEMENT_PENALTY = 0.67f;
        
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("adaptiveshields");
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            stats.getShieldTurnRateMult().modifyMult(id, (1 - SHIELD_MOVEMENT_PENALTY));
            stats.getShieldUnfoldRateMult().modifyMult(id, (1 - SHIELD_MOVEMENT_PENALTY));
            stats.getShieldArcBonus().modifyMult(id, (1 - SHIELD_PENALTY));
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);

        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                xlu_BlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
         
//        if (ship.getVariant().getHullMods().contains("adaptiveshields")) {
//            ship.getVariant().removeMod("adaptiveshields");
//        }
        
    }
	
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
	if (index == 0) return "" + (int) (SHIELD_PENALTY * 100) + "%";
	if (index == 1) return "" + (int) (100 - (SHIELD_MOVEMENT_PENALTY * 100)) + "%";
        if (index == 2) return "unable to convert into Omnidirectional";
	return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("xlu_");
    }
}
