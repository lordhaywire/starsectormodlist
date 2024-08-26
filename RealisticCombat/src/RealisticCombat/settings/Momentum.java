package RealisticCombat.settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class Momentum {

    private static boolean ENABLED;

    private static float OFFSET_FACTOR, LENGTH_FACTOR, WIDTH_FACTOR;


    /**
     * @return {@code boolean} should momentum indicators be displayed
     */
    public static boolean isEnabled() { return ENABLED; }

    /**
     * @return {@code float} factor whereby to multiply the shield radius
     *         of a ship to obtain the distance from its shield center to
     *         the innermost point of its momentum indicator
     */
    public static float getOffsetFactor() { return OFFSET_FACTOR; }

    /**
     * @return {@code float} factor whereby to multiply all momentum
     *         indicator lengths
     */
    public static float getLengthFactor() { return LENGTH_FACTOR; }

    /**
     * @return {@code float} factor whereby to multiply  all momentum
     *         indicator widths
     */
    public static float getWidthFactor() { return WIDTH_FACTOR; }


    public static void load(final JSONObject json) throws JSONException, IOException {
        final JSONObject settings = json.getJSONObject(Momentum.class.getSimpleName());
        final boolean Default = Indication.isDefault();

        ENABLED = settings.getBoolean("enabled");
        OFFSET_FACTOR = Default ? 1.5f : (float) settings.getDouble("offsetFactor");
        LENGTH_FACTOR = Default ? 10 : (float) settings.getDouble("lengthFactor");
        WIDTH_FACTOR = Default ? 10 : (float) settings.getDouble("widthFactor");
    }
}
