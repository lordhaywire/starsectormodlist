package RealisticCombat.calculation;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.loading.ProjectileSpecAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import static RealisticCombat.settings.ThreeDimensionalTargeting.*;

public final class Lead {

    private static float getHalfThickness(final ShipAPI ship) {
        try {
            return ship.getListeners(RealisticCombat.listeners.ThreeDimensionalTargeting.class).get(
                    0).getHalfThickness();
        } catch (final Throwable ignored) {
            return ship.getCollisionRadius() * getHalfThicknessFactor();
        }
    }

    private static float getEvasionTime(final ShipAPI ship) {
        final float distance = getEvasionDistance(ship),
                    acceleration = ship.getAcceleration()
                                   * getEvasionAccelerationFactor(ship.getHullSize());
        return (float) Math.sqrt(2 * distance / acceleration);
    }

    private static float getEvasionTime(final MissileAPI missile) {
        final float distance = getEvasionDistance(missile),
                    turnAcceleration = missile.getTurnAcceleration(),
                    turningTime = 2 * (float) Math.sqrt(Math.PI / turnAcceleration);
        return turningTime + (float) Math.sqrt(2 * distance / missile.getAcceleration());
    }

    /**
     * @param a {@code float} coefficient of x^2
     * @param b {@code float} coefficient of x
     * @param c {@code float} constant
     * <p>
     * @return {@link Vector2f} solutions of a quadratic equation
     *          ax^2 + bx + c = 0
     */
    private static float[] quad(final float a, final float b, final float c) {
        if (Float.compare(Math.abs(a), 0) == 0) {
            return (Float.compare(Math.abs(b), 0) == 0)
                    ? (Float.compare(Math.abs(c), 0) == 0) ? new float[] {0, 0} : null
                    : new float[] {-c / b, -c / b};
        }
        float d = b * b - 4 * a * c;
        if (d < 0) return null;
        d = (float) Math.sqrt(d);
        final float e = 2 * a;
        return new float[]{(-b - d) / e, (-b + d) / e};
    }

    private static class WeaponGroupDerivedStats {

        private final float averageWeaponSpeed;
        private final Vector2f center;

        private static boolean isActiveNonBeamWeapon(final WeaponAPI weapon) {
            return !(weapon.isDisabled()
                    || weapon.isBeam()
                    || weapon.isBurstBeam()
                    || weapon.getProjectileSpeed() < 50f);
        }

        private static boolean firesMissiles(final WeaponAPI weapon) {
            return weapon.getSpec().getProjectileSpec() instanceof MissileSpecAPI;
        }

        private static float getMissileSpeed(final WeaponAPI weapon) {
            return ((MissileSpecAPI) weapon.getSpec().getProjectileSpec()
            ).getHullSpec().getEngineSpec().getMaxSpeed();
        }

        /**
         * {@code WeaponAPI.getProjectileSpeed()} returns the speed
         * in weapon_data.csv rather than after RealisticCombat has
         * modified the weapon and projectile specs.
         *
         * @param weapon {@link WeaponAPI}
         *
         * @return {@code float} true speed of the projectile of
         *         a {@link WeaponAPI}
         */
        private static float getProjectileSpeed(final WeaponAPI weapon) {
            return ((ProjectileSpecAPI) weapon.getSpec().getProjectileSpec()).getMoveSpeed(
                    weapon.getShip().getMutableStats(), weapon);
        }

        private static float getWeaponSpeed(final WeaponAPI weapon) {
            return firesMissiles(weapon) ? getMissileSpeed(weapon) : getProjectileSpeed(weapon);
        }

        private WeaponGroupDerivedStats(final WeaponGroupAPI weaponGroup) {
            float totalWeaponSpeed = 0.01f, activeNonBeamWeapons = 0;
            Vector2f center = new Vector2f(0, 0);
            for (final WeaponAPI weapon : weaponGroup.getWeaponsCopy())
                if (isActiveNonBeamWeapon(weapon)) {
                    totalWeaponSpeed += getWeaponSpeed(weapon);
                    Vector2f.add(center, weapon.getLocation(), center);
                    activeNonBeamWeapons += 1f;
                }
            averageWeaponSpeed = totalWeaponSpeed / activeNonBeamWeapons;
            center.x /= activeNonBeamWeapons; center.y /= activeNonBeamWeapons;
            this.center = center;
        }

        private float getAverageSpeed() { return averageWeaponSpeed; }

        private Vector2f getCenter() { return this.center; }
    }

    /**
     * @return {@code boolean} whether bracketing a ship with four shots, three
     *         in a circle around it and on in the middle, guaranteeing a hit,
     *         is possible for this {@link WeaponAPI} within this {@code float}
     *         leadingTime within which the target can evade
     */
    public static boolean isBracketingPossible(final WeaponAPI weapon, final float leadingTime) {
        return weapon.getSpec().getDerivedStats().getRoF() * leadingTime >= 4;
    }

    /**
     * @return {@code float} factor whereby to multiply the base range of this
     *         {@link WeaponAPI} when bracketing this {@link CombatEntityAPI}
     *
     */
    public static float getBracketingRangeFactor(final WeaponAPI weapon,
                                                 final CombatEntityAPI entity)
    {
        return 1.15f
                * Misc.getDistance(weapon.getLocation(), entity.getLocation())
                / weapon.getSpec().getMaxRange();
    }

    public static float getEvasionDistance(final CombatEntityAPI entity) {
        return (entity instanceof ShipAPI)
                ? getHalfThickness((ShipAPI) entity)
                : entity.getCollisionRadius()
                  * ((entity instanceof MissileAPI) ? getEvasionDistanceFactorMissile() : 0.5f);
    }

    public static float getEvasionTime(final CombatEntityAPI entity) {
        return (entity instanceof ShipAPI)
                ? getEvasionTime((ShipAPI) entity)
                : (entity instanceof MissileAPI) ? getEvasionTime((MissileAPI) entity) : 0;
    }

    public static Vector2f getRelativeVelocity(final CombatEntityAPI a, final CombatEntityAPI b) {
        return Vector.difference(a.getVelocity(), b.getVelocity());
    }

    /**
     * @param projectileLoc {@link Vector2f} average {@link WeaponAPI} location
     *                      of a {@link WeaponGroupAPI}
     * @param speed {@code float} average {@link DamagingProjectileAPI} speed
     *              of the {@link WeaponAPI}s of a {@link WeaponGroupAPI}
     * @param targetLoc {@link Vector2f} target location
     * @param targetVel {@link Vector2f} target velocity
     * <p></p>
     * @return {@link Vector2f} at which a {@link DamagingProjectileAPI}
     *         fired from the average {@link Vector2f} location of the
     *         {@link WeaponAPI}s of the selected {@link WeaponGroupAPI}, with
     *         the {@code float} average speed of the
     *         {@link DamagingProjectileAPI} of the same, would intercept a
     *         target of initial {@link Vector2f} location and anticipated
     *         constant {@link Vector2f} velocity
     */
    public static Vector2f intercept(final Vector2f projectileLoc,
                                      final float speed,
                                      final Vector2f targetLoc,
                                      final Vector2f targetVel)
    {
        final Vector2f difference = Vector2f.sub(targetLoc, projectileLoc, new Vector2f());
        final float a = targetVel.x * targetVel.x + targetVel.y * targetVel.y - speed * speed,
                b = 2 * (targetVel.x * difference.x + targetVel.y * difference.y),
                c = difference.x * difference.x + difference.y * difference.y;
        final float[] solutions = quad(a, b, c);

        if (solutions == null) return null;
        float bestFit = Math.min(solutions[0], solutions[1]);
        if (bestFit < 0) bestFit = Math.max(solutions[0], solutions[1]);
        return (bestFit < 0) ? null : new Vector2f(targetLoc.x + targetVel.x * bestFit,
                targetLoc.y + targetVel.y * bestFit);
    }

    public static Vector2f getLead(final WeaponGroupAPI weaponGroup, final CombatEntityAPI target) {
        final WeaponGroupDerivedStats stats = new WeaponGroupDerivedStats(weaponGroup);
        final Vector2f relativeVelocity = getRelativeVelocity(target, weaponGroup.getShip());
        return intercept(stats.getCenter(), stats.getAverageSpeed(), target.getLocation(),
                         relativeVelocity);
    }
}
