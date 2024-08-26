package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class FreeCamera {

    private static final String FILE_PATH = "data/config/FreeCamera.json";

    private static int TOGGLE, ZOOM_IN, ZOOM_OUT;

    private static float SCREEN_HALF_HEIGHT_THRESHOLD_FRACTION,
                         SCREEN_HALF_HEIGHT_SCALE_FRACTION,
                         BASE_SPEED,
                         ZOOM_FACTOR;

    /**
     * @return {@code int} activate or deactivate the free camera
     */
    public static int getToggle() { return TOGGLE; }

    /**
     * @return {@code int} zoom in
     */
    public static int getZoomIn() { return ZOOM_IN; }

    /**
     * @return {@code int} zoom out
     */
    public static int getZoomOut() { return ZOOM_OUT; }

    /**
     * @return {@code float} fraction of half the height of the screen moving
     *         the mouse a distance from the center greater than which will
     *         start moving the viewport
     */
    public static float getScreenHalfHeightThresholdFraction() {
        return SCREEN_HALF_HEIGHT_THRESHOLD_FRACTION;
    }

    /**
     * @return {@code float} fraction of half the height of screen, beyond the
     *          threshold fraction, moving the mouse over which will hasten
     *          viewport movement
     */
    public static float getScreenHalfHeightScaleFraction() {
        return SCREEN_HALF_HEIGHT_SCALE_FRACTION;
    }

    /**
     * @return {@code float} base number of pixels per second to move the camera
     */
    public static float getBaseSpeed() { return BASE_SPEED; }

    /**
     * @return {@code float} factor whereby to scale the viewport as the zoom
     *         in or zoom out button is pressed
     */
    public static float getZoomFactor() { return ZOOM_FACTOR; }

    public static void load() throws JSONException, IOException {
        final JSONObject json = Global.getSettings().loadJSON(FILE_PATH);

        TOGGLE = json.getInt("toggle");
        ZOOM_IN = json.getInt("zoomIn");
        ZOOM_OUT = json.getInt("zoomOut");
        SCREEN_HALF_HEIGHT_THRESHOLD_FRACTION = (float) json.getDouble(
                "screenHalfHeightThresholdFraction");
        SCREEN_HALF_HEIGHT_SCALE_FRACTION = (float) json.getDouble(
                "screenHalfHeightScaleFraction") ;
        BASE_SPEED = (float) json.getDouble("baseSpeed");
        ZOOM_FACTOR = (float) json.getDouble("zoomFactor");
    }
}
