package hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import ids.Roider_Ids.Roider_Hullmods;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: SafariJohn
 */
public class Roider_TrackerSwap extends BaseHullMod {

    public static final Map<String, Integer> WINGS_PER_SHIP = new HashMap<>();
    static {
        WINGS_PER_SHIP.put("roider_sheriff", 1);
        WINGS_PER_SHIP.put("roider_jane", 1);
        WINGS_PER_SHIP.put("roider_cowboy", 1);
        WINGS_PER_SHIP.put("roider_ranch", 2);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        // Handled in Roider_MIDAS instead.
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(Roider_Hullmods.GLITZ_SWITCH)) {
            return "Drones are already switched";
        }


        return "Can only be applied to ships with built-in Breaker drones";
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(Roider_Hullmods.GLITZ_SWITCH)) return false;

        return WINGS_PER_SHIP.containsKey(ship.getHullSpec().getBaseHullId());
    }

}
