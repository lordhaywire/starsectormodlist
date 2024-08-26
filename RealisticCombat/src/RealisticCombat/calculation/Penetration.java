package RealisticCombat.calculation;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;

public final class Penetration {

    /**
     * @return {@code float} millimeters of rolled homogeneous armor
     *         penetrable
     */
    public static float getPenetration(final BeamAPI beam) {
        return Beam.getDiffractedIntensity(beam);
    }

    /**
     * @return {@code float} millimeters of rolled homogeneous armor
     *         penetrable
     */
    public static float getPenetration(final DamagingProjectileAPI projectile) {
        return projectile.getDamage().getBaseDamage();
    }
}
