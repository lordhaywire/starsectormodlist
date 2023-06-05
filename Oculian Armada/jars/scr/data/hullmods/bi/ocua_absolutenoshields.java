package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.plugins.OCUA_BlockedHullmodDisplayScript;
import java.util.HashSet;
import java.util.Set;

public class ocua_absolutenoshields extends BaseHullMod {

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("frontshield");
        BLOCKED_HULLMODS.add("adaptiveshields");
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
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "unable to equip Shields";
        if (index == 1) return "Omni-Shield";
	return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
