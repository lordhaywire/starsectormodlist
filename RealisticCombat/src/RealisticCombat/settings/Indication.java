package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Indication {

    private static final String SETTINGS_FILE_PATH = "data/config/Indicators.json";

    private static boolean DEFAULT;

    private static int TOGGLE_KEY;

    private static float ZOOM_CUTOFF;

    private static final List<RealisticCombat.renderers.Indication> INDICATIONS = new ArrayList<>();


    /**
     * @return {@code float} zoom level closer than which every HUD extension
     *         feature should be hidden
     */
    public static float getZoomCutoff() { return ZOOM_CUTOFF; }

    /**
     * Returns the LWJGL keyboard constant for the HUD extensions toggle key.
     * <p>
     * @return The LWJGL {@link Keyboard} constant of the key used to toggle the
     *         radar on or off. See
     * <a href="http://legacy.lwjgl.org/javadoc/constant-values.html#org.lwjgl.input.Keyboard.KEY_0">
     * the LWJGL documentation page</a> for details.
     * <p>
     */
    public static int getToggleKey() { return TOGGLE_KEY; }

    /**
     * @return {@code boolean} whether every HUD extension should use the
     *         default RealisticCombat.settings in the code, or custom RealisticCombat.settings in the .json
     *         file, of that extension.
     */
    public static boolean isDefault() { return DEFAULT; }

    /**
     * @return {@link List} of {@link RealisticCombat.renderers.Indication}s enabled
     */
    public static List<RealisticCombat.renderers.Indication> getIndications() { return INDICATIONS; }


    public static void load() throws JSONException, IOException {
        final JSONObject json = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);

        final JSONObject general = json.getJSONObject("general");
        DEFAULT = general.getBoolean("default");
        ZOOM_CUTOFF = (float) general.getDouble("zoomCutoff");
        TOGGLE_KEY = general.getInt("toggleKey");

        RealisticCombat.settings.Diffraction.load(json);
        RealisticCombat.settings.Lead.load(json);
        RealisticCombat.settings.Momentum.load(json);
        RealisticCombat.settings.Projectile.load(json);
        RealisticCombat.settings.Status.load(json);

        try {
            if (RealisticCombat.settings.Diffraction.isEnabled())
                INDICATIONS.add(RealisticCombat.renderers.Diffraction.class.newInstance());
            if (RealisticCombat.settings.Lead.isEnabled())
                INDICATIONS.add(RealisticCombat.renderers.Lead.class.newInstance());
            if (RealisticCombat.settings.Momentum.isEnabled())
                INDICATIONS.add(RealisticCombat.renderers.Momentum.class.newInstance());
            if (RealisticCombat.settings.Projectile.isEnabled())
                INDICATIONS.add(RealisticCombat.renderers.Projectile.class.newInstance());
            if (RealisticCombat.settings.Status.isEnabled())
                INDICATIONS.add(RealisticCombat.renderers.Status.class.newInstance());
        } catch (Throwable ignored) {}
    }
}
