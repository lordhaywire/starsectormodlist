package RealisticCombat.settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class Lead {

    private static boolean ENABLED;

    private static float THICKNESS_FACTOR;

    private static int SEGMENTS;


    /**
     * @return {@code boolean} should the lead indicator be displayed
     */
    public static boolean isEnabled() { return ENABLED; }

    /**
     * @return {@code int} the number of segments comprising the lead indicator
     */
    public static int getSegments() { return SEGMENTS; }

    /**
     * @return {@code float} factor the the radius of the target ship that the
     *         thickness of the lead should be
     */
    public static float getThicknessFactor() { return THICKNESS_FACTOR; }


    public static void load(final JSONObject json) throws JSONException, IOException {
        final JSONObject settings = json.getJSONObject(Lead.class.getSimpleName());
        final boolean Default = Indication.isDefault();

        ENABLED = settings.getBoolean("enabled");
        SEGMENTS = Default ? 256 : settings.getInt("segments");
        final float areaFactor = Default ? 0.02f : (float) settings.getDouble("thickness"),
                    a = (float) Math.sqrt(1 - areaFactor);
        THICKNESS_FACTOR = (1 - a) / (1 + a);
    }
}
