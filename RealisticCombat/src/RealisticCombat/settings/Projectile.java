package RealisticCombat.settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class Projectile {

    private static boolean ENABLED;

    private static int ENERGY_SEGMENTS;

    private static float
            BALLISTIC_LENGTH_STRONG,
            BALLISTIC_WIDTH_STRONG,
            MISSILE_LENGTH_STRONG,
            MISSILE_WIDTH_STRONG,
            ENERGY_RADIUS_STRONG,

            BALLISTIC_LENGTH_WEAK,
            BALLISTIC_WIDTH_WEAK,
            MISSILE_LENGTH_WEAK,
            MISSILE_WIDTH_WEAK,
            ENERGY_RADIUS_WEAK;


    /**
     * @return {@code boolean} should projectile indicators be displayed
     */
    public static boolean isEnabled() { return ENABLED; }

    /**
     * @return {@code float} base length of all ballistic projectile indicators
     */
    public static float getBallisticLength(final boolean strong) {
        return strong ? BALLISTIC_LENGTH_STRONG : BALLISTIC_LENGTH_WEAK;
    }

    /**
     * @return {@code float} base width of all ballistic projectile indicators
     */
    public static float getBallisticWidth(final boolean strong) {
        return  strong ? BALLISTIC_WIDTH_STRONG : BALLISTIC_WIDTH_WEAK;
    }

    /**
     * @return {@code float} base length of all missile indicators
     */
    public static float getMissileLength(final boolean strong) {
        return strong ? MISSILE_LENGTH_STRONG : MISSILE_LENGTH_WEAK;
    }

    /**
     * @return {@code float} base width of all missile indicators
     */
    public static float getMissileWidth(final boolean strong) {
        return strong ? MISSILE_WIDTH_STRONG : MISSILE_WIDTH_WEAK;
    }

    /**
     * @return {@code float} base radius of the energy projectile indicator
     */
    public static float getEnergyRadius(final boolean strong) {
        return strong ? ENERGY_RADIUS_STRONG : ENERGY_RADIUS_WEAK;
    }

    /**
     * @return {@code int} segments comprising the energy projectile indicator
     */
    public static int getEnergySegments() { return ENERGY_SEGMENTS; }


    public static void load(final JSONObject json) throws JSONException, IOException {
        final JSONObject settings = json.getJSONObject(Projectile.class.getSimpleName());
        final boolean Default = Indication.isDefault();

        ENABLED = settings.getBoolean("enabled");

        ENERGY_SEGMENTS = Default ? 16 : settings.getInt("energySegments");

        BALLISTIC_LENGTH_STRONG = Default ? 12
                                          : (float) settings.getDouble("ballisticLengthStrong");
        BALLISTIC_WIDTH_STRONG = Default ? 7.5f : (float) settings.getDouble("ballisticWidthStrong");
        MISSILE_LENGTH_STRONG = Default ? 15 : (float) settings.getDouble("missileLengthStrong");
        MISSILE_WIDTH_STRONG = Default ? 7.5f : (float) settings.getDouble("missileWidthStrong");
        ENERGY_RADIUS_STRONG = Default ? 7.5f : (float) settings.getDouble("energyRadiusStrong");

        BALLISTIC_LENGTH_WEAK = Default ? 8 : (float) settings.getDouble("ballisticLengthWeak");
        BALLISTIC_WIDTH_WEAK = Default ? 4 : (float) settings.getDouble("ballisticWidthWeak");
        MISSILE_LENGTH_WEAK = Default ? 10 : (float) settings.getDouble("missileLengthWeak");
        MISSILE_WIDTH_WEAK = Default ? 5 : (float) settings.getDouble("missileWidthWeak");
        ENERGY_RADIUS_WEAK = Default ? 5 : (float) settings.getDouble("radiusEnergyWeak");
    }
}
