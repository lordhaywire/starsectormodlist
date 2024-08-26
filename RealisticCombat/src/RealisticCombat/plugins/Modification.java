package RealisticCombat.plugins;

import RealisticCombat.listeners.*;
import RealisticCombat.scripts.Categorization;
import RealisticCombat.scripts.CombatReadiness;
import RealisticCombat.scripts.DamageReportManagerV1;
import RealisticCombat.settings.Toggles;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;

import java.util.List;

/**
 * Change ship stats before combat and in the fleet, refit, and shop menus.
 * <p>Increase top speed and mass and reduce maneuverability.</p>
 */
public final class Modification extends BaseEveryFrameCombatPlugin {

    private static boolean isProhibited() {
        return Global.getCurrentState() != GameState.COMBAT
                || Global.getCombatEngine() == null
                || Global.getCombatEngine().isPaused();
    }

    private static void adjustAI(final ShipAPI ship) {
        if (!(ship.getShipAI() == null || ship.getShipAI().getConfig() == null)
            && ship.getShipAI().getConfig().turnToFaceWithUndamagedArmor)
            ship.getShipAI().getConfig().turnToFaceWithUndamagedArmor = false;
    }

    private static void maximizeAutofireAccuracy(final ShipAPI ship) {
        if (ship.getMutableStats().getAutofireAimAccuracy().getBaseValue() != 1)
            ship.getMutableStats().getAutofireAimAccuracy().setBaseValue(1);
    }

    private static void enableThreeDimensionalTargeting(final ShipAPI ship) {
        maximizeAutofireAccuracy(ship);
        ThreeDimensionalTargeting.unmodifyMuzzleVelocities(ship);
        if (Categorization.isStrikeCraft(ship))
            ThreeDimensionalTargeting.modifyStrikeCraftProjectileRangeAndSpeed(ship);
        else {
            if (!ship.hasListenerOfClass(ThreeDimensionalTargeting.class))
                ship.addListener(new ThreeDimensionalTargeting(ship));
            if (!ship.hasListenerOfClass(GunLocking.class)
                && ship == Global.getCombatEngine().getPlayerShip())
                ship.addListener(new GunLocking(ship));
        }
    }

    private static void replaceDamageModel(final ShipAPI ship) {
        adjustAI(ship);
        if (!ship.hasListenerOfClass(DamageModel.class)) ship.addListener(new DamageModel());
    }

    private static void limitCombatReadinessAndEnableRetreat(final ShipAPI ship) {
        CombatReadiness.limit(ship);
        if (!(ship.hasListenerOfClass(Retreat.class)
              || Categorization.isStrikeCraft(ship)
              || Categorization.isStation(ship))) ship.addListener(new Retreat(ship));
    }

    private static void displayWeaponArcs(final ShipAPI ship) {
        final ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
        if (!ship.isAlive()) return;
        if (playerShip != null && ship == playerShip.getShipTarget()) {
            if (!ship.hasListenerOfClass(WeaponFacings.class))
                ship.addListener(new WeaponFacings((ship)));
        } else if (ship.hasListenerOfClass(WeaponFacings.class))
            ship.removeListenerOfClass(WeaponFacings.class);
    }

    private static void modify(final ShipAPI ship) {
        if (Toggles.isTargetingThreeDimensional()) enableThreeDimensionalTargeting(ship);
        if (Toggles.isDamageModelToBeReplaced()) replaceDamageModel(ship);
        if (Toggles.isDamageToReduceCR()) limitCombatReadinessAndEnableRetreat(ship);
        if (Toggles.isWeaponArcTobeDisplayed()) displayWeaponArcs(ship);
    }

    @Override
    public void advance(final float amount, final List<InputEventAPI> list) {
        if (isProhibited()) return;
        DamageReportManagerV1.getDamageReportManager().clearDamageReports();
        for (final ShipAPI ship : Global.getCombatEngine().getShips())
            if (Categorization.isFlying(ship)) modify(ship);
    }
}
