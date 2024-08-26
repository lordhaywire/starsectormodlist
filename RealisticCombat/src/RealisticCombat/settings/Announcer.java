package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public final class Announcer {
    private static final String SETTINGS_FILE_PATH = "data/config/Announcer.json";

    /**
     * Organizes the types of events for which announcements can be picked.
     */
    public enum EVENT_TYPE {
        DESTROYED_FRIENDLY,
        DESTROYED_ENEMY,
        DISABLED_FRIENDLY,
        DISABLED_ENEMY,
        RETREATED_FRIENDLY,
        RETREATED_ENEMY,
        DEPLOYED_FRIENDLY,
        DEPLOYED_ENEMY
    }

    private static HashMap<EVENT_TYPE, ArrayList<Float>> DURATIONS;

    private static HashMap<EVENT_TYPE, ArrayList<String>> LINE_IDS;

    public static HashMap<EVENT_TYPE, ArrayList<Float>> getDurations() { return DURATIONS; }

    public static HashMap<EVENT_TYPE, ArrayList<String>> getLineIds() { return LINE_IDS; }

    public static void load() throws JSONException, IOException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);
        for (final EVENT_TYPE eventType : EVENT_TYPE.values()) {
            final JSONArray eventTypeJSONArray = settings.getJSONArray(eventType.toString());
            final ArrayList<Float> durations = new ArrayList<>();
            final ArrayList<String> lineIds = new ArrayList<>();
            for (int i = 0; i < eventTypeJSONArray.length(); i++) {
                lineIds.add(eventTypeJSONArray.getJSONArray(i).getString(0));
                durations.add((float) eventTypeJSONArray.getJSONArray(i).getDouble(1));
            }
            LINE_IDS.put(eventType, lineIds);
            DURATIONS.put(eventType, durations);
        }
    }
}
