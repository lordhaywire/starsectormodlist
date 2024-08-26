package RealisticCombat.scripts;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;

public final class ShipSpecs {

    private static float getNewMaxSpeed(final ShipHullSpecAPI shipHullSpec) {
        return shipHullSpec.getEngineSpec().getMaxSpeed()
                + RealisticCombat.settings.ShipSpecs.getMaxSpeedBonus(shipHullSpec.getHullSize());
    }

    private static float getNewAcceleration(final ShipHullSpecAPI shipHullSpec) {
        return shipHullSpec.getEngineSpec().getAcceleration()
                * RealisticCombat.settings.ShipSpecs.getAccelerationFactor(shipHullSpec.getHullSize());
    }

    private static float getNewDeceleration(final ShipHullSpecAPI shipHullSpec) {
        return shipHullSpec.getEngineSpec().getDeceleration()
                * RealisticCombat.settings.ShipSpecs.getDecelerationFactor(shipHullSpec.getHullSize());
    }

    private static float getNewTurnAcceleration(final ShipHullSpecAPI shipHullSpec) {
        return shipHullSpec.getEngineSpec().getTurnAcceleration()
                * RealisticCombat.settings.ShipSpecs.getTurnAccelerationFactor(shipHullSpec.getHullSize());
    }

    private static float getNewMaxTurnRate(final ShipHullSpecAPI shipHullSpec) {
        return shipHullSpec.getEngineSpec().getMaxTurnRate()
                * RealisticCombat.settings.ShipSpecs.getMaxTurnRateFactor(shipHullSpec.getHullSize());
    }

    /**
     * Modify the speed and maneuverability of a {@link ShipAPI} to be more
     * realistic.
     *
     * Increase top speed, decrease acceleration and deceleration,
     * @param shipHullSpec {@link ShipHullSpecAPI}
     */
    public static void modify(final ShipHullSpecAPI shipHullSpec) {
        final ShipHullSpecAPI.EngineSpecAPI engineSpec = shipHullSpec.getEngineSpec();
        if (engineSpec == null) return;
        engineSpec.setMaxSpeed(getNewMaxSpeed(shipHullSpec));
        engineSpec.setAcceleration(getNewAcceleration(shipHullSpec));
        engineSpec.setDeceleration(getNewDeceleration(shipHullSpec));
        engineSpec.setTurnAcceleration(getNewTurnAcceleration(shipHullSpec));
        engineSpec.setMaxTurnRate(getNewMaxTurnRate(shipHullSpec));
    }
}
