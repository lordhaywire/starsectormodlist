package RealisticCombat.settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class Diffraction {

    private static boolean ENABLED;

    private static float OFFSET_X, OFFSET_Y, THICKNESS;


    /**
     * @return {@code boolean}
     */
    public static boolean isEnabled() { return ENABLED; }

    /**
     * @return {@code float} x-axis distance from indicator center to edges
     */
    public static float getOffsetX() { return OFFSET_X; }

    /**
     * @return {@code float} y-axis distance from center of indicator to cursor
     */
    public static float getOffsetY() { return OFFSET_Y; }

    /**
     * @return {@code float} indicator line base width
     */
    public static float getThickness() { return THICKNESS; }


    public static void load(final JSONObject json) throws JSONException, IOException {
        final JSONObject settings = json.getJSONObject(Diffraction.class.getSimpleName());
        final boolean Default = Indication.isDefault();

        ENABLED = settings.getBoolean("enabled");
        OFFSET_X = Default ? 100 : (float) settings.getDouble("offsetX");
        OFFSET_Y = Default ? 100 : (float) settings.getDouble("offsetY");
        THICKNESS = Default ? 10 : (float) settings.getDouble("thickness");
    }
}
