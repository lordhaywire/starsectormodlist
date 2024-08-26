package RealisticCombat.scripts;

import RealisticCombat.calculation.Collision;
import RealisticCombat.calculation.Vector;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import static RealisticCombat.calculation.Damage.getDamageType;
import static com.fs.starfarer.api.combat.DamageType.KINETIC;

public final class Ricochet {

    private static final float PROBABILITY = 0.9f, DISTANCE_BEHIND = 15;

    private static Vector2f getArmorVector(final BoundsAPI.SegmentAPI segment) {
        return Misc.getUnitVector(segment.getP1(), segment.getP2());
    }

    private static Vector2f getLocationBehind(final Vector2f location, final Vector2f direction) {
        return Vector.difference(location, Vector.scalarProduct(DISTANCE_BEHIND, direction));
    }

    private static Vector2f getRicochetVector(final DamagingProjectileAPI projectile,
                                              final ShipAPI ship) {
        return Vector.reflection(Misc.normalise(projectile.getVelocity()),
                getArmorVector(Collision.getClosestSegment(projectile.getLocation(), ship)));
    }

    private static float getRicochetFacing(final Vector2f ricochetVector) {
        final float ricochetAngle = Misc.getAngleInDegrees(ricochetVector);
        return ricochetAngle > 0 ? ricochetAngle : 360 + ricochetAngle;
    }

    /**
     * Return whether this {@link DamagingProjectileAPI} could ricochet.
     * <p></p>
     * @param projectile {@link DamagingProjectileAPI} hitting the armor of a
     *                   {@link ShipAPI}
     * <p></p>
     * @return {@link Boolean} whether the {@link DamagingProjectileAPI} will
     *         bounce off the armor of a {@link ShipAPI}
     */
    private static boolean isAble(final DamagingProjectileAPI projectile) {
        return getDamageType(projectile) == KINETIC
                && projectile.getProjectileSpec() != null
                && projectile.getProjectileSpec().getOnHitEffect() == null;
    }

    /**
     * Return whether this {@link DamagingProjectileAPI} ricochets off this
     * {@link ShipAPI} this time.
     * <p></p>
     * @param projectile {@link DamagingProjectileAPI} hitting the armor of a
     *                   {@link ShipAPI}
     * <p></p>
     * @return {@link Boolean} whether the {@link DamagingProjectileAPI} will
     *         bounce off the armor of a {@link ShipAPI}
     */
    public static boolean isCaused(final DamagingProjectileAPI projectile, final ShipAPI ship) {
        return isAble(projectile)
                && Collision.getClosestSegment(projectile.getLocation(), ship) != null
                && Collision.isPointWithinBounds(getLocationBehind(projectile.getLocation(),
                                                 Misc.normalise(projectile.getVelocity())), ship)
                && PROBABILITY * 90 * Math.random() > Collision.getObliqueAngle(projectile, ship);
    }

    /**
     * Bounce a {@link DamagingProjectileAPI} off the armor of a
     * {@link ShipAPI} at the same angle it hit.
     * <p></p>
     * @param projectile {@link DamagingProjectileAPI} hitting the armor of a
     *                   {@link ShipAPI}
     * @param ship {@link ShipAPI} being hit
     */
    public static void occur(final DamagingProjectileAPI projectile, final ShipAPI ship) {
        String id = null;
        try {
            final ShipAPI source = projectile.getSource();
            final WeaponAPI weapon = projectile.getWeapon();
            id = projectile.getWeapon().getId();
            final Vector2f location = projectile.getLocation();
            final Vector2f vector = getRicochetVector(projectile, ship);
            final float facing = getRicochetFacing(vector);
            final Vector2f velocity = ship.getVelocity();
            Global.getCombatEngine().spawnProjectile(
                /*projectile.getSource(),
                projectile.getWeapon(),
                projectile.getWeapon().getId(),
                projectile.getLocation(),
                getRicochetFacing(getRicochetVector(projectile, ship)),
                ship.getVelocity()*/
                    source, weapon, id, location, facing, velocity
            );
        } catch (Throwable t) {
            System.out.println(id + " " + projectile.getProjectileSpecId());
        }
    }
}
