package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class BattleCreationPlugin {

    private static final String SETTINGS_FILE_PATH = "data/config/BattleCreationPlugin.json";

    private static float MAP_WIDTH_BASE, MAP_HEIGHT_BASE;


    public static float getMapWidthBase() { return MAP_WIDTH_BASE; }

    public static float getMapHeightBase() { return MAP_HEIGHT_BASE; }

    public static void load() throws JSONException, IOException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);

        MAP_WIDTH_BASE = (float) settings.getDouble("mapWidthBase");
        MAP_HEIGHT_BASE = (float) settings.getDouble("mapHeightBase");
    }
}
