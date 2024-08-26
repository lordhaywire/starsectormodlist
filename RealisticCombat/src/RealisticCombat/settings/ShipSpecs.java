package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.json.JSONException;
import org.json.JSONObject;
import RealisticCombat.util.JSONUtils;

import java.io.IOException;
import java.util.HashMap;

public final class ShipSpecs {

    private static final String SETTINGS_FILE_PATH = "data/config/ShipSpecs.json";

    private static float FIGHTER_WING_RANGE_FACTOR;

    private static HashMap<HullSize, Float>
            MAX_SPEED_BONUSES,
            ACCELERATION_FACTORS,
            DECELERATION_FACTORS,
            MAX_TURN_RATE_FACTORS,
            TURN_ACCELERATION_FACTORS;


    /**
     * @return what {@code float} factor of the top speed of a {@link ShipAPI}
     *         should its acceleration be
     */
    public static float getFighterWingRangeFactor() {
        return FIGHTER_WING_RANGE_FACTOR;
    }

    /**
     * @return {@code float} how much the top speed of a {@link ShipAPI} should
     *         increase
     */
    public static float getMaxSpeedBonus(final HullSize hullSize) {
        return MAX_SPEED_BONUSES.get(hullSize);
    }

    /**
     * @param hullSize {@link HullSize}
     *
     * @return what {@code float} factor of the top speed of a {@link ShipAPI}
     *         should its acceleration be
     */
    public static float getAccelerationFactor(final HullSize hullSize) {
        return ACCELERATION_FACTORS.get(hullSize);
    }

    /**
     * @return {@code float} factor by which to multiply the deceleration
     *         of a {@link ShipAPI}
     */
    public static float getDecelerationFactor(final HullSize hullSize) {
        return DECELERATION_FACTORS.get(hullSize);
    }

    /**
     * @return {@code float} factor by which to multiply the turn
     *         acceleration of a {@link ShipAPI}
     */
    public static float getTurnAccelerationFactor(final HullSize hullSize) {
        return TURN_ACCELERATION_FACTORS.get(hullSize);
    }

    /**
     * @return {@code float} factor by which to multiply the max turn rate of a
     *         {@link ShipAPI}
     */
    public static float getMaxTurnRateFactor(final HullSize hullSize) {
        return MAX_TURN_RATE_FACTORS.get(hullSize);
    }


    public static void load() throws JSONException, IOException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);

        FIGHTER_WING_RANGE_FACTOR = (float) settings.getDouble("fighterWingRangeFactor");

        MAX_SPEED_BONUSES = JSONUtils.getHullSizeToFloatHashmap(
                settings.getJSONObject("maxSpeedBonuses"));
        ACCELERATION_FACTORS = JSONUtils.getHullSizeToFloatHashmap(
                settings.getJSONObject("accelerationFactors"));
        DECELERATION_FACTORS = JSONUtils.getHullSizeToFloatHashmap(
                settings.getJSONObject("decelerationFactors"));
        TURN_ACCELERATION_FACTORS = JSONUtils.getHullSizeToFloatHashmap(
                settings.getJSONObject("turnAccelerationFactors"));
        MAX_TURN_RATE_FACTORS = JSONUtils.getHullSizeToFloatHashmap(
                settings.getJSONObject("maxTurnRateFactors"));
    }
}
