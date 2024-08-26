package RealisticCombat.listeners;

import RealisticCombat.calculation.Lead;
import RealisticCombat.scripts.Categorization;
import RealisticCombat.settings.Colors;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.util.Collection;

public final class GunLocking implements AdvanceableListener {

    private static final int FRAMES_TO_BLOCK_FIRE = 20;

    private static final float TIME_BETWEEN_REMINDERS = 10, REMINDER_SIZE = 16;

    private static boolean anySelectedWeaponOnTarget = false;

    private static float timeSincePlayerReminded = 0;

    private final ShipAPI ship;

    public GunLocking(final ShipAPI ship) { this.ship = ship; }

    private static boolean isLed(final WeaponAPI weapon) {
        return !(weapon.isBeam() || weapon.getSpec().getProjectileSpec() instanceof MissileSpecAPI);
    }

    /**
     * @return {@code boolean} whether a rangefinder is installed on this
     *         {@link ShipAPI}
     */
    private static boolean isRangefinderInstalled(final ShipAPI ship) {
        return ship.getVariant().getHullMods().contains("ballistic_rangefinder");
    }

    /**
     * @return {@code boolean} whether a targeting system is installed on this
     *         {@link ShipAPI}
     */
    private static boolean isTargetingSystemInstalled(final ShipAPI ship) {
        final Collection<String> hullmods = ship.getVariant().getHullMods();
        return  hullmods.contains("targetingunit")
                || hullmods.contains("dedicated_targeting_core")
                || hullmods.contains("advancedcore")
                || hullmods.contains("supercomputer");
    }

    /**
     * @return {@code float} margin whereby this {@link ShipAPI}
     *         overestimates the distance from each {@link WeaponAPI}
     *         aboard to the target of that {@link WeaponAPI}
     */
    private static float getRangingFactor(final ShipAPI ship) {
        return isRangefinderInstalled(ship)
                ? 1 : RealisticCombat.settings.ThreeDimensionalTargeting.getRangingFactor();
    }

    /**
     * @return {@code float} margin whereby a {@link ShipAPI} overestimates
     *         the distance the target of every non-missile projectile
     *         {@link WeaponAPI} aboard could strafe before the projectile
     *         of that {@link WeaponAPI} would reach it
     */
    private static float getLeadingFactor(final ShipAPI ship) {
        return isTargetingSystemInstalled(ship)
                ? 1 : RealisticCombat.settings.ThreeDimensionalTargeting.getLeadingFactor();
    }

    /**
     * Hold the fire of this {@link WeaponAPI}
     */
    private static void holdFire(final WeaponAPI weapon) {
        weapon.setRemainingCooldownTo(Math.max(weapon.getCooldownRemaining(),
                FRAMES_TO_BLOCK_FIRE * Global.getCombatEngine().getElapsedInLastFrame()));
    }

    private static void remindPlayerToSelectTarget() {
        final ViewportAPI viewport = Global.getCombatEngine().getViewport();
        Global.getCombatEngine().addFloatingText(
                new Vector2f(viewport.convertScreenXToWorldX(Mouse.getX()),
                        viewport.convertScreenYToWorldY(Mouse.getY())),
                "R to select target.",
                viewport.convertScreenHeightToWorldHeight(REMINDER_SIZE),
                Colors.getFriendly(),
                null,
                1,
                0
        );
    }

    private void updateLock (final WeaponAPI weapon, final CombatEntityAPI target) {
        if (target == null) {
            anySelectedWeaponOnTarget = false;
            holdFire(weapon);
            timeSincePlayerReminded += Global.getCombatEngine().getElapsedInLastFrame();
            if (Mouse.isButtonDown(0) && timeSincePlayerReminded > TIME_BETWEEN_REMINDERS) {
                remindPlayerToSelectTarget();
                timeSincePlayerReminded = 0;
            }
            return;
        }

        final Vector2f relativeVelocity = Lead.getRelativeVelocity(target, ship);
        final Vector2f lead = Lead.intercept(weapon.getLocation(), weapon.getProjectileSpeed(),
                target.getLocation(), relativeVelocity);

        if (lead == null) {
            anySelectedWeaponOnTarget = false;
            holdFire(weapon);
            return;
        }

        final float leadingFactor = getLeadingFactor(ship),
                    evasionTime = Lead.getEvasionTime(target),
                    leadingTime = leadingFactor * evasionTime,
                    rangingFactor = getRangingFactor(ship),
                    range = rangingFactor * Misc.getDistance(lead, weapon.getLocation()),
                    muzzleVelocity = weapon.getSpec().getMaxRange(),
                    effectiveRange = muzzleVelocity * leadingTime,
                    direction = Misc.getAngleInDegrees(weapon.getLocation(), lead);

        final boolean bracketingPossible = Lead.isBracketingPossible(weapon, evasionTime),
                      inRange = effectiveRange > range;

        final float evasionDistance = bracketingPossible ? target.getCollisionRadius() / 2
                                                         : Lead.getEvasionDistance(target),
                    ratio = evasionDistance / range,
                    aimError = Misc.getAngleDiff(weapon.getCurrAngle(), direction),
                    aimErrorLimit = Misc.DEG_PER_RAD * (float) Math.atan(ratio);

        final boolean onTarget = aimError < aimErrorLimit;
        if (inRange && onTarget)
            anySelectedWeaponOnTarget = true;
        else {
            anySelectedWeaponOnTarget = false;
            holdFire(weapon);
        }
    }

    public static boolean isAnySelectedWeaponOnTarget() { return anySelectedWeaponOnTarget; }

    /**
     * Return the time wherein the non-missile projectile of this
     * {@link WeaponAPI} would reach the target, if any, of this
     * {@link ShipAPI}.
     * <p>
     * If this {@link ShipAPI} has a target, return the time wherein that
     * target could  up-, down-, left-, or rightward strafe from the path of
     * the projectile of this {@link WeaponAPI} before that projectile would
     * reach the target.
     * <p>
     * If this {@link ShipAPI} lacks a target or has targeted itself, or if
     * the target of this this {@link ShipAPI} has been disabled or destroyed,
     * return one second.
     *
     * @return {@code float} time in seconds
     */
    @Override
    public void advance(final float amount) {
        final ShipAPI ship = Global.getCombatEngine().getPlayerShip();
        if (ship == null) return;
        final WeaponGroupAPI selectedWeaponGroup = ship.getSelectedGroupAPI();
        if (selectedWeaponGroup == null || Categorization.isAutopilotOn()) return;
        for (final WeaponAPI weapon : selectedWeaponGroup.getWeaponsCopy())
            if (isLed(weapon)) updateLock(weapon, ship.getShipTarget());
    }
}
