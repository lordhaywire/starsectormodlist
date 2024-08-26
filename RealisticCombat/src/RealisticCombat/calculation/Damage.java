package RealisticCombat.calculation;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

import static RealisticCombat.calculation.Penetration.getPenetration;
import static RealisticCombat.settings.DamageModel.getDamageFactor;
import static RealisticCombat.settings.DamageModel.getShieldDamageFactor;

public final class Damage {

    private static final float BREAK_EVEN_FACTOR = 500;

    private static float getEfficiency(final ShieldAPI shield) {
        return shield.getFluxPerPointOfDamage();
    }

    /**
     * @return {@code float} EMP damage inflicted by this
     *         {@link DamagingProjectileAPI}
     */
    public static float getEmp(final DamagingProjectileAPI projectile) {
        return projectile.getEmpAmount();
    }

    /**
     * @return {@code float} EMP damage inflicted by this
     *         {@link BeamAPI}
     */
    public static float getEmp(final BeamAPI beam) {
        return beam.getDamage().getFluxComponent()
                * Global.getCombatEngine().getElapsedInLastFrame();
    }

    /**
     * @return {@code float} shield damage inflicted by this {@link BeamAPI}
     */
    public static float getShield(final ShipAPI ship, final BeamAPI beam) {
        final float penetration = getPenetration(beam),
                    efficiency = getEfficiency(ship.getShield()),
                    log = (float) (Math.log10(getPenetration(beam))
                                    / Math.log10(BREAK_EVEN_FACTOR * efficiency * efficiency));
        return Math.max(log, 0)
                * penetration
                * getShieldDamageFactor(Damage.getDamageType(beam));
    }

    /**
     * @return {@code float} shield damage inflicted by this {@link DamagingProjectileAPI}
     */
    public static float getShield(final ShipAPI ship, final DamagingProjectileAPI projectile) {
        final float penetration = getPenetration(projectile),
                    efficiency = getEfficiency(ship.getShield()),
                    log = (float) (Math.log10(penetration)
                            / Math.log10(BREAK_EVEN_FACTOR * efficiency * efficiency));
        return Math.max(log, 0)
                * penetration
                * getShieldDamageFactor(Damage.getDamageType(projectile));
    }

    public static float getPotential(final BeamAPI beam) {
        return Global.getCombatEngine().getElapsedInLastFrame()
                * getDamageFactor(getDamageType(beam))
                * beam.getWeapon().getDamage().getBaseDamage();
    }

    public static float getPotential(final DamagingProjectileAPI projectile) {
        return getDamageFactor(getDamageType(projectile)) * getPenetration(projectile);
    }

    /**
     * @return {@link DamageType} of this {@link DamagingProjectileAPI},
     *         {@link MissileAPI}, or {@link BeamAPI}
     */
    public static DamageType getDamageType(final Object object) {
        return (object instanceof BeamAPI) ? ((BeamAPI) object).getDamage().getType()
                : ((DamagingProjectileAPI) object).getDamageType();
    }
}
