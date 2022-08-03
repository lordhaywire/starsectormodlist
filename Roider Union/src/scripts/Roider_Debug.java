package scripts;

import com.fs.starfarer.api.Global;

/**
 * Author: SafariJohn
 */
public class Roider_Debug {
    public static final boolean TECH_EXPEDITIONS;

    static {
        TECH_EXPEDITIONS = getDebugBoolean("roider_techExpDebug");
    }

    private static boolean getDebugBoolean(String id) {
        try {
            return Global.getSettings().getBoolean(id);
        } catch (Exception ex) {}

        return false;
    }
}
