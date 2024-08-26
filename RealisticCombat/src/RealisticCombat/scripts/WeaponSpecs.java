package RealisticCombat.scripts;

import RealisticCombat.calculation.Beam;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.EngineSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI;
import com.fs.starfarer.api.loading.ProjectileSpecAPI;
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import static RealisticCombat.settings.Categorization.WeaponCategory;
import static RealisticCombat.settings.WeaponSpecs.*;

public final class WeaponSpecs {

    private static final float BEAM_SPEED = (float) (4 * 3 * Math.pow(10, 8)),
                               REFIRE_DELAY_PER_BURST_SHOT = 2;

    public static float getMuzzleVelocity(final float range, final float speed) {
        return Math.max(getMuzzleVelocityMinimum(), Math.min(getMuzzleVelocityMaximum(),
                getMuzzleVelocityFactor() * (Math.max(range, speed))));
    }

    private static int getMaxAmmo(final WeaponCategory weaponCategory,
                                  final WeaponSpecAPI weaponSpec)
    {
        final int ceiling = getMaxAmmoCeiling(weaponCategory, weaponSpec.getSize());
        if (!(weaponSpec.getMaxAmmo() < Integer.MAX_VALUE)) return ceiling;
        final int excessAmmo = Math.max(weaponSpec.getMaxAmmo() - ceiling, 0);
        return excessAmmo == 0 ? weaponSpec.getMaxAmmo() : ceiling + (int) Math.sqrt(excessAmmo);
    }

    private static boolean isPointDefenseBeam(final BeamWeaponSpecAPI beamWeaponSpec) {
        return beamWeaponSpec.getAIHints().contains(AIHints.PD)
                || beamWeaponSpec.getAIHints().contains(AIHints.PD_ONLY)
                || beamWeaponSpec.getAIHints().contains(AIHints.PD_ALSO);
    }

    private static float getEfficientRange(final float initialIntensity,
                                           final float fluxPerSecond,
                                           final float originalRange,
                                           final float minEfficiency)
    {
        float range = originalRange, step = originalRange / 10;
        for (int i = 0; i < 100; i++) {
            float efficiency = Beam.getDiffractedIntensity(initialIntensity, range) / fluxPerSecond;
            if (Math.abs(efficiency - minEfficiency) < 0.001f) break;
            range += step;
            if (efficiency < minEfficiency && step > 0 || efficiency > minEfficiency && step < 0)
                step *= -0.1f;
        } return range;
    }

    private static float getInitialIntensity(final float effectPerSecond, final float weaponRange) {
        float initialIntensity = effectPerSecond, step = effectPerSecond / 10;
        for (int i = 0; i < 100; i++) {
            float diffractedIntensity = Beam.getDiffractedIntensity(initialIntensity, weaponRange);
            if (Math.abs(diffractedIntensity - effectPerSecond) < 0.001f) break;
            initialIntensity += step;
            if (diffractedIntensity < effectPerSecond && step < 0
                || diffractedIntensity > effectPerSecond && step > 0) step *= -0.1;
        } return initialIntensity;
    }

    public static void modifyProjectileSpec(final ProjectileSpecAPI projectileSpec,
                                            final float muzzleVelocity) {
        if (projectileSpec.getDamage().getType() == DamageType.HIGH_EXPLOSIVE
            && projectileSpec.getDamage().getBaseDamage() < 1000)
            projectileSpec.getDamage().setDamage(
                    RealisticCombat.settings.WeaponSpecs.getProjectileDamageBonus()
                            + projectileSpec.getDamage().getBaseDamage());
        projectileSpec.setMoveSpeed(muzzleVelocity);
        projectileSpec.setMaxRange(muzzleVelocity);
    }

    /**
     * Modify to be more realistic a {@link WeaponSpecAPI}, the
     * {@link WeaponAPI} to be generated from which should fire a
     * non-{@link MissileAPI} {@link DamagingProjectileAPI}, and the
     * {@link ProjectileSpecAPI} thereof.
     * <p>
     * Dramatically increase range and speed.
     */
    public static void modifyProjectileWeaponSpec(final WeaponSpecAPI weaponSpec,
                                                  final float muzzleVelocity) {
        weaponSpec.setProjectileSpeed(muzzleVelocity);
        weaponSpec.setMaxRange(muzzleVelocity);
        weaponSpec.setMinSpread(Math.min(weaponSpec.getMinSpread(), getMinimumSpread()));
        weaponSpec.setMaxSpread(Math.min(weaponSpec.getMaxSpread(), getMaximumSpread()));
    }

    /**
     * Modify to be more realistic a {@link WeaponSpecAPI}, the
     * {@link WeaponAPI} to be generated from which should fire a
     * {@link BeamAPI}.
     * <p>
     * Travel instantly with much higher damage, which the inverse square
     * diffraction RealisticCombat.calculation elsewhere in the mod presumes
     * and mitigates.
     */
    public static void modifyBeamWeaponSpec(final BeamWeaponSpecAPI beamWeaponSpec,
                                            final boolean directedEnergyMunition)
    {
        beamWeaponSpec.setBeamSpeed(BEAM_SPEED);
        final boolean pointDefense = isPointDefenseBeam(beamWeaponSpec);
        final float
                beamIntensityFactor = getBeamIntensityFactor(beamWeaponSpec.isBurstBeam(),
                                                             pointDefense,
                                                             directedEnergyMunition),
                damagePerSecond = beamWeaponSpec.getDerivedStats().getDps() * beamIntensityFactor,
                empPerSecond = beamWeaponSpec.getDerivedStats().getEmpPerSecond() * beamIntensityFactor,
                greaterEffectPerSecond = Math.max(damagePerSecond, empPerSecond),
                originalRange = beamWeaponSpec.getMaxRange(),
                initialIntensity,
                minimumFluxEfficiency;

        if (pointDefense) {
            initialIntensity = Math.min(getPointDefenseBeamIntensityLimit(
                    beamWeaponSpec.isBurstBeam()), getInitialIntensity(greaterEffectPerSecond,
                        originalRange));
            minimumFluxEfficiency = getPointDefenseBeamMinimumFluxEfficiency();
            if (beamWeaponSpec.isBurstBeam())
                beamWeaponSpec.setMaxAmmo(
                        (RealisticCombat.settings.WeaponSpecs.getPointDefenseBurstBeamAmmoFactor()
                        * beamWeaponSpec.getMaxAmmo()));
        } else {
            initialIntensity = getInitialIntensity(greaterEffectPerSecond, originalRange);
            minimumFluxEfficiency = getAntiShipBeamMinimumFluxEfficiency();
        }

        final float effectFactor = initialIntensity / greaterEffectPerSecond;

        beamWeaponSpec.setDamagePerSecond(damagePerSecond * effectFactor);

        beamWeaponSpec.setMaxRange((beamWeaponSpec.getDerivedStats().getFluxPerSecond() <= 0
                                || damagePerSecond == 0)
                                    ? Float.POSITIVE_INFINITY
                                    : getEfficientRange(
                                            initialIntensity,
                                            beamWeaponSpec.getDerivedStats().getFluxPerSecond(),
                                            originalRange,
                                            minimumFluxEfficiency
                                    )
        );
    }

    public static void modifyEngineSpec(final WeaponCategory weaponCategory,
                                        final EngineSpecAPI engineSpec) {
        engineSpec.setMaxSpeed(getMaxSpeedFactor(weaponCategory)
                * engineSpec.getMaxSpeed());
        engineSpec.setAcceleration(getAccelerationFactor(weaponCategory)
                * engineSpec.getAcceleration());
        engineSpec.setDeceleration(getDecelerationFactor(weaponCategory)
                * engineSpec.getDeceleration());
        engineSpec.setTurnAcceleration(getTurnAccelerationFactor(weaponCategory)
                * engineSpec.getTurnAcceleration());
        engineSpec.setMaxTurnRate(getMaxTurnRateFactor(weaponCategory)
                * engineSpec.getMaxTurnRate());
    }

    /**
     * Modify to be more realistic a {@link WeaponSpecAPI}, the
     * {@link WeaponAPI} to be generated from which should fire a
     * {@link MissileAPI}.
     * <p>
     * Dramatically increase range, speed, and maneuverability.
     */
    public static void modifyLauncherSpec(final WeaponSpecAPI weaponSpec,
                                          final WeaponCategory weaponCategory)
    {
        weaponSpec.setMaxRange(getLauncherRangeBonus(weaponCategory, weaponSpec.getSize())
                + weaponSpec.getMaxRange());
        weaponSpec.setMaxAmmo(getMaxAmmo(weaponCategory, weaponSpec));
        if (weaponSpec.getBurstSize() > 1) {
            ProjectileWeaponSpecAPI projectileWeaponSpec = (ProjectileWeaponSpecAPI) weaponSpec;
            projectileWeaponSpec.setBurstSize(weaponSpec.getTurretFireOffsets().size());
            projectileWeaponSpec.setRefireDelay(REFIRE_DELAY_PER_BURST_SHOT
                    * weaponSpec.getBurstSize());
        }
    }
}










