package RealisticCombat.calculation;

import com.fs.starfarer.api.combat.BeamAPI;

public final class Beam {

    private static float getInitialIntensity(final BeamAPI beam) {
        return beam.getWeapon().getDamage().getBaseDamage();
    }

    public static float getDiffractedIntensity(final float initialIntensity,
                                               final float distance)
    {
        if (initialIntensity == 0) return 0;
        if (distance == 0) return initialIntensity;
        return initialIntensity / (1 + distance * distance / (initialIntensity * initialIntensity));
    }

    /**
     * @return {@code float} diffracted {@link BeamAPI} intensity, neglecting
     *         height difference between weapon and target and beam movement
     *         across the target, smoothly limited to initial intensity
     */
    public static float getDiffractedIntensity(final BeamAPI beam) {
        return getDiffractedIntensity(getInitialIntensity(beam), beam.getLength());
    }
}
