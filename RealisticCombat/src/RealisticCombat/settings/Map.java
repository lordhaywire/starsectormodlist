package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class Map {

    private static final String SETTINGS_FILE_PATH = "data/config/Map.json";

    private static float
            MAP_SIZE_FACTOR,
            STANDOFF_FACTOR_WITH_OBJECTIVES,
            STANDOFF_FACTOR_WITHOUT_OBJECTIVES;


    /**
     * @return {@code float} factor by which to multiply the length and width
     *         of the combat map
     */
    public static float getMapSizeFactor() { return MAP_SIZE_FACTOR; }

    /**
     * @return {@code float} factor by which to multiply the standoff between
     *         fleets on a combat map having objectives
     */
    public static float getStandoffFactorWithObjectives() {
        return STANDOFF_FACTOR_WITH_OBJECTIVES;
    }

    /**
     * @return {@code float} factor by which to multiply the standoff between
     *         fleets on a combat map lacking objectives
     */
    public static float getStandoffFactorWithoutObjectives() {
        return STANDOFF_FACTOR_WITHOUT_OBJECTIVES;
    }


    public static void load() throws JSONException, IOException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);

        MAP_SIZE_FACTOR = (float) settings.getDouble("mapSizeFactor");
        STANDOFF_FACTOR_WITH_OBJECTIVES = (float) settings.getDouble(
                "standOffFactorWithObjectives");
        STANDOFF_FACTOR_WITHOUT_OBJECTIVES = (float) settings.getDouble(
                "standOffFactorWithoutObjectives");
    }
}
