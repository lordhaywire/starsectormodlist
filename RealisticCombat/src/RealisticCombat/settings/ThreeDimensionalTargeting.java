package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;


public final class ThreeDimensionalTargeting {

    private static final String SETTINGS_FILE_PATH = "data/config/ThreeDimensionalTargeting.json";

    private static float HALF_THICKNESS_FACTOR,
                         EVASION_DISTANCE_FACTOR_MISSILE,
                         RANGING_FACTOR,
                         LEADING_FACTOR,
                         MUZZLE_VELOCITY_FACTOR_FOR_STRIKE_CRAFT;

    private static HashMap<ShipAPI.HullSize, Float>  STRAFING_ACCELERATION_FACTORS;


    /**
     * @return {@code float} factor of {@code float} collision radius
     *         a {@link ShipAPI} or {@link MissileAPI} must cross to
     *         evade an incoming shot.
     */
    public static float getHalfThicknessFactor() { return HALF_THICKNESS_FACTOR; }

    /**
     * @return {@code float} factor of {@code float} collision radius
     *         a {@link ShipAPI} or {@link MissileAPI} must cross to
     *         evade an incoming shot.
     */
    public static float getEvasionDistanceFactorMissile() {
        return EVASION_DISTANCE_FACTOR_MISSILE;
    }

    /**
     * @return {@code float} factor whereby a non-launcher projectile
     *         {@link WeaponAPI} overestimates the distance to its target
     */
    public static float getRangingFactor() { return RANGING_FACTOR; }

    /**
     * @return {@code float} factor whereby a non-launcher projectile
     *         {@link WeaponAPI} underestimates the time wherein its target
     *         could strafe off the path of the projectiles of the
     *         {@link WeaponAPI}
     */
    public static float getLeadingFactor() { return LEADING_FACTOR; }

    /**
     * @return {@code float} factor of acceleration whereby a {@link ShipAPI}
     *         could accelerate up, down, or sideways
     */
    public static float getEvasionAccelerationFactor(final ShipAPI.HullSize hullSize) {
        return STRAFING_ACCELERATION_FACTORS.get(hullSize);
    }

    /**
     * @return {@code float} whereby to multiply the muzzle velocities of
     *         the {@link DamagingProjectileAPI}s of the {@link WeaponAPI}s of
     *         a strike craft {@link ShipAPI}
     */
    public static float getMuzzleVelocityFactorForStrikeCraft() {
        return MUZZLE_VELOCITY_FACTOR_FOR_STRIKE_CRAFT;
    }


    private static HashMap<HullSize, Float> getHullSizeToFloatHashmap(final JSONObject hullSizes) {
        final HashMap<HullSize, Float> hashmap = new HashMap<HullSize, Float>() {};
        for (HullSize hullSize : HullSize.values()) {
            try { if (hullSizes.has(hullSize.toString()))
                      hashmap.put(hullSize, (float) hullSizes.getDouble(hullSize.toString())); }
            catch (Throwable t) { t.printStackTrace(); }
        } return hashmap;
    }


    public static void load() throws JSONException, IOException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);

        MUZZLE_VELOCITY_FACTOR_FOR_STRIKE_CRAFT = (float) settings.getDouble(
                "muzzleVelocityFactorForStrikeCraft");
        HALF_THICKNESS_FACTOR = (float) settings.getDouble("halfThicknessFactor");
        EVASION_DISTANCE_FACTOR_MISSILE = (float) settings.getDouble(
                "evasionDistanceFactorMissile");
        RANGING_FACTOR = (float) settings.getDouble("rangingFactor");
        LEADING_FACTOR = (float) settings.getDouble("leadingFactor");
        STRAFING_ACCELERATION_FACTORS = getHullSizeToFloatHashmap(settings.getJSONObject(
                "strafingAccelerationFactors"));
    }
}
