package RealisticCombat.scripts;

import com.fs.starfarer.api.combat.ShipAPI;

public final class CombatReadiness {

    private static float getLimitFactor(final float hullLevel) {
        final float a = 1 / (1 - 1 / RealisticCombat.settings.DamageModel.getMinimumCombatReadyHullLevel()),
                    b = (1 - a) * (1 - hullLevel);
        return 1 - b * b;
    }

    /**
     * Reduces the combat readiness of this {@link ShipAPI} based on hull
     * level because a damaged ship is less ready to fight.
     */
    public static void limit(final ShipAPI ship) {
        final float limit = ship.getCRAtDeployment() * getLimitFactor(ship.getHullLevel());
        if (ship.getCurrentCR() > limit) ship.setCurrentCR(limit);
    }
}
