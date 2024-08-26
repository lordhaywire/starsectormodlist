package RealisticCombat.settings;

import RealisticCombat.util.JSONUtils;
import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public final class FleetRetreat {

    private static final String SETTINGS_FILE_PATH = "data/config/FleetRetreat.json";

    private static HashMap<String, Float> LOSS_THRESHOLDS, OUTNUMBER_THRESHOLDS;

    public static float getLossThreshold(final String personalityId) {
        return LOSS_THRESHOLDS.get(personalityId);
    }

    public static float getOutnumberThreshold(final String personalityId) {
        return OUTNUMBER_THRESHOLDS.get(personalityId);
    }

    public static void load() throws JSONException, IOException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);

        LOSS_THRESHOLDS = JSONUtils.getStringToFloatHashmap(settings.getJSONObject(
                "lossThresholds"));
        OUTNUMBER_THRESHOLDS = JSONUtils.getStringToFloatHashmap(settings.getJSONObject(
                "outnumberThresholds"));
    }
}
