package RealisticCombat.listeners;

import RealisticCombat.calculation.Lead;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;
import RealisticCombat.scripts.Categorization;

import java.util.Collection;

import static RealisticCombat.calculation.Lead.getBracketingRangeFactor;
import static RealisticCombat.calculation.Lead.isBracketingPossible;
import static RealisticCombat.settings.ThreeDimensionalTargeting.getHalfThicknessFactor;

/**
 * Limits the range of ballistic and non-beam energy weapons to one within
 * which their targets could not jink their projectiles in time.
 */
public final class ThreeDimensionalTargeting implements WeaponBaseRangeModifier {

    private final float halfThickness;

    public ThreeDimensionalTargeting(final ShipAPI ship) { halfThickness = getHalfThickness(ship); }

    private static boolean isLeadingUnncessary(final WeaponAPI weapon) {
        return weapon.isBeam() || weapon.getSpec().getProjectileSpec() instanceof MissileSpecAPI;
    }

    private static float getHalfThickness(final ShipAPI ship) {
        try {
            final JSONObject json = Global.getSettings().getMergedJSON(
                    "data/hulls/" + ship.getHullSpec().getHullId() + ".ship");
            return (float) Math.min(json.getDouble("width"), json.getDouble("height"))
                    * getHalfThicknessFactor();
        } catch (Throwable ignored) {
            return ship.getCollisionRadius() * getHalfThicknessFactor();
        }
    }

    /**
     * @return {@code boolean} whether this {@link WeaponGroupAPI} aboard
     *         this {@link ShipAPI} is to be aimed by the player
     */
    private static boolean isManuallyAimed(final WeaponGroupAPI weaponGroup) {
        return !Categorization.isAutopilotOn()
                && weaponGroup.getShip() == Global.getCombatEngine().getPlayerShip()
                && weaponGroup == weaponGroup.getShip().getSelectedGroupAPI();
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
     * @return {@link ShipAPI} or {@link MissileAPI} targeted by the
     *         {@link AutofireAIPlugin} of this {@link WeaponAPI} in
     *         or {@code null} should the {@link AutofireAIPlugin}
     *         have targeted neither
     */
    private static CombatEntityAPI getAutofireTarget(final AutofireAIPlugin autofireAI)
    {
        if (autofireAI.getTargetShip() != null) return autofireAI.getTargetShip();
        else if (autofireAI.getTargetMissile() != null) return autofireAI.getTargetMissile();
        return null;
    }

    /**
     * @return {@link ShipAPI} or {@link MissileAPI} targeted by the
     *         {@link AutofireAIPlugin} of this {@link WeaponAPI} in
     *         in this {@link WeaponGroupAPI} aboard this {@link ShipAPI},
     *         {@link CombatEntityAPI} targeted by this {@link ShipAPI}
     *         should the {@link AutofireAIPlugin} have selected neither,
     *         or {@code null} should neither have targeted anything
     */
    private static CombatEntityAPI getTarget(final WeaponAPI weapon,
                                             final WeaponGroupAPI weaponGroup,
                                             final ShipAPI ship)
    {
        if (weaponGroup == null) return ship.getShipTarget();
        else if (weaponGroup.isAutofiring() && weaponGroup.getAutofirePlugin(weapon) != null)
            return getAutofireTarget(weaponGroup.getAutofirePlugin(weapon));
        else if (weaponGroup == ship.getSelectedGroupAPI()) return ship.getShipTarget();
        return null;
    }

    /**
     * @return {@code float} half thickness of this ship
     */
    public float getHalfThickness() { return halfThickness; }

    /**
     * Remove the projectile range and speed bonuses of this {@link ShipAPI}
     */
    public static void unmodifyMuzzleVelocities(final ShipAPI ship) {
        final MutableShipStatsAPI stats = ship.getMutableStats();
        stats.getBallisticWeaponRangeBonus().unmodify();
        stats.getEnergyWeaponRangeBonus().unmodify();
        stats.getBallisticProjectileSpeedMult().unmodify();
        stats.getEnergyProjectileSpeedMult().unmodify();
    }

    /**
     * Multiply the range of the {@link DamagingProjectileAPI}s of the
     * {@link WeaponAPI}s aboard a strike craft {@link ShipAPI} by the
     * strafing time of the target and range and projectile speed by the
     * range and muzzle velocity by the fighter muzzle velocity factor.
     */

    public static void modifyStrikeCraftProjectileRangeAndSpeed(final ShipAPI ship) {
        final float
                strafingTime = getLeadingFactor(ship)
                                * ((ship.getShipTarget() == null)
                                    ? 1 : RealisticCombat.calculation.Lead.getEvasionTime(ship.getShipTarget())),
                factor = RealisticCombat.settings.ThreeDimensionalTargeting.getMuzzleVelocityFactorForStrikeCraft();
        final String id = ship.getFleetMemberId();
        final MutableShipStatsAPI stats = ship.getMutableStats();
        stats.getBallisticWeaponRangeBonus().modifyMult(id, strafingTime * factor);
        stats.getEnergyWeaponRangeBonus().modifyMult(id, strafingTime * factor);
        stats.getBallisticProjectileSpeedMult().modifyMult(id, factor);
        stats.getEnergyProjectileSpeedMult().modifyMult(id, factor);
        stats.getBeamWeaponRangeBonus().unmodify();
    }

    @Override
    public float getWeaponBaseRangePercentMod(final ShipAPI shipAPI, final WeaponAPI weaponAPI) {
        return 0;
    }

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
    public float getWeaponBaseRangeMultMod(final ShipAPI ship, final WeaponAPI weapon) {
        if (isLeadingUnncessary(weapon)) return 1;

        final WeaponGroupAPI weaponGroup = ship.getWeaponGroupFor(weapon);
        if (weaponGroup == null) return 1;
        final boolean manuallyAimed = isManuallyAimed(weaponGroup);
        final CombatEntityAPI target = manuallyAimed ? ship.getShipTarget()
                                                     : getTarget(weapon, weaponGroup, ship);

        if (target == null) return 1;
        final Vector2f relativeVelocity = Lead.getRelativeVelocity(target, ship);
        final Vector2f lead = Lead.intercept(weapon.getLocation(), weapon.getProjectileSpeed(),
                target.getLocation(), relativeVelocity);

        if (lead == null) return 1;
        final float leadingTime = getLeadingFactor(ship) * Lead.getEvasionTime(target);
        return isBracketingPossible(weapon, leadingTime) ? getBracketingRangeFactor(weapon, target)
                                                         : leadingTime;
    }

    @Override
    public float getWeaponBaseRangeFlatMod(final ShipAPI shipAPI, final WeaponAPI weaponAPI) {
        return 0;
    }
}
