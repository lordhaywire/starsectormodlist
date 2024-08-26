package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.ViewportAPI;

import org.json.JSONException;
import org.json.JSONObject;
import RealisticCombat.renderers.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Contains the radar's configuration data obtained from the files within
 * {@code data/config/radar/}.
 *
 * @author LazyWizard
 * @since 2.0
 */
public final class Radar {
    //Settings file path
    private static final String SETTINGS_FILE_PATH = "data/config/Radar.json";
    //Enable or disable
    private static boolean ENABLED;
    // List of loaded rendering RealisticCombat.plugins
    private static final List<Class<? extends CombatRenderer>> COMBAT_RENDERER_CLASSES =
            new ArrayList<>();
    // Performance RealisticCombat.settings
    private static final boolean USE_VBOS = GLContext.getCapabilities().OpenGL15;
    private static final float TIME_BETWEEN_UPDATE_FRAMES = 0.05f;
    private static final int VERTICES_PER_CIRCLE = 144;
    // Display RealisticCombat.settings
    private static final float RADAR_RENDER_RADIUS = Display.getHeight() * 0.125f,
                               COMBAT_SIGHT_RADIUS = 20000;
    // Zoom controls
    private static float ZOOM_ANIMATION_DURATION;
    private static int NUM_ZOOM_LEVELS;
    // Radar color RealisticCombat.settings
    private static float CONTACT_ALPHA;
    private static final java.awt.Color FRIENDLY_COLOR = Misc.getPositiveHighlightColor(),
                               ENEMY_COLOR = Misc.getNegativeHighlightColor(),
                               NEUTRAL_COLOR = Misc.getGrayColor(),
                               ALLY_COLOR = new java.awt.Color(0,150,0,255);
    // Radar button LWJGL constants
    private static int ZOOM_IN_KEY, ZOOM_OUT_KEY;


    public static void reloadRenderers() {
        COMBAT_RENDERER_CLASSES.clear();
        COMBAT_RENDERER_CLASSES.add(ShipRenderer.class);
        COMBAT_RENDERER_CLASSES.add(MissileRenderer.class);
    }

    /**
     * @return {@code boolean} should the radar be displayed
     */
    public static boolean isEnabled() { return ENABLED; }

    public static float getMaxCombatSightRange() { return COMBAT_SIGHT_RADIUS; }

    /**
     * Returns the list of RealisticCombat.renderers used in combat.
     * <p>
     * @return the {@link List} of {@link CombatRenderer}s used in combat.
     * <p>
     * @since 2.0
     */
    public static List<Class<? extends CombatRenderer>> getCombatRendererClasses() {
        return Collections.unmodifiableList(COMBAT_RENDERER_CLASSES);
    }

    /**
     * Returns whether the radar will use vertex buffer objects (VBOs) when
     * rendering.
     * <p>
     * @return {@code true} if VBOs are enabled and the user's card supports
     *         them, {@code false} otherwise.
     * <p>
     * @since 2.0
     */
    public static boolean usesVertexBufferObjects()
    {
        return USE_VBOS;
    }

    /**
     * Returns how many vertices the radar should use when creating circles.
     * <p>
     * @return How many vertices any drawn circles should use.
     * <p>
     * @since 2.1
     */
    public static int getVerticesPerCircle()
    {
        return VERTICES_PER_CIRCLE;
    }

    /**
     * Returns the radius of the rendered radar circle, in pixels.
     * <p>
     * @return The radius of the radar circle, in pixels.
     * <p>
     * @since 2.2
     */
    public static float getRadarRenderRadius() { return RADAR_RENDER_RADIUS; }

    /**
     * Returns how long there is between each radar update frame.
     * <p>
     * @return How long between each radar update frame, in seconds.
     * <p>
     * @since 2.0
     */
    public static float getTimeBetweenUpdateFrames() { return TIME_BETWEEN_UPDATE_FRAMES; }

    /**
     * Returns how long it takes for the radar to animate switching zoom levels.
     * <p>
     * @return How long it takes the radar to switch zoom levels, in seconds.
     * <p>
     * @since 2.0
     */
    public static float getZoomAnimationDuration() { return ZOOM_ANIMATION_DURATION; }

    /**
     * Returns how many zoom levels the radar supports.
     * <p>
     * @return How many different zoom levels the radar is configured to
     *         support.
     * <p>
     * @since 2.0
     */
    public static int getNumZoomLevels() { return NUM_ZOOM_LEVELS; }

    /**
     * Returns the alpha modifier for all radar contacts.
     * <p>
     * @return The alpha modifier that should be applied to all radar contacts
     *         (but not the user interface, see
     *         {@link Colors#getAlpha(ViewportAPI)}).
     * <p>
     * @since 2.0
     */
    public static float getRadarContactAlpha() { return CONTACT_ALPHA; }

    /**
     * Returns the LWJGL keyboard constant for the radar zoom in key.
     * <p>
     * @return The LWJGL {@link Keyboard} constant of the key used to zoom the
     *         radar in. See
     * <a href="http://legacy.lwjgl.org/javadoc/constant-values.html#org.lwjgl.input.Keyboard.KEY_0">
     * the LWJGL documentation page</a> for details.
     * <p>
     * @since 2.0
     */
    public static int getZoomInKey() { return ZOOM_IN_KEY; }

    /**
     * Returns the LWJGL keyboard constant for the radar zoom out key.
     * <p>
     * @return The LWJGL {@link Keyboard} constant of the key used to zoom the
     *         radar out. See
     * <a href="http://legacy.lwjgl.org/javadoc/constant-values.html#org.lwjgl.input.Keyboard.KEY_0">
     * the LWJGL documentation page</a> for details.
     * <p>
     * @since 2.0
     */
    public static int getZoomOutKey() { return ZOOM_OUT_KEY; }

    public static void load() throws JSONException, IOException {
        final JSONObject Settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);
        final boolean Default = Indication.isDefault();

        ENABLED = Settings.getBoolean("enabled");
        NUM_ZOOM_LEVELS = Default ? 4 : Settings.getInt("numberOfZoomLevels");
        ZOOM_IN_KEY = Default ? 13 : Settings.getInt("zoomInKey");
        ZOOM_OUT_KEY = Default ? 12 : Settings.getInt("zoomOutKey");
        ZOOM_ANIMATION_DURATION = Default ? 2f
                                          : (float) Settings.getDouble("zoomAnimationDuration");
        CONTACT_ALPHA = Default ? 0.85f : (float) Settings.getDouble("contactAlpha");

    }

    private Radar() {}

    private static class RendererWrapper<T> implements Comparable<RendererWrapper> {
        private final Class<T> renderClass;
        private final int renderOrder;

        RendererWrapper(Class<T> renderClass, int renderOrder) {
            this.renderClass = renderClass;
            this.renderOrder = renderOrder;
        }

        Class<T> getRendererClass()
        {
            return renderClass;
        }

        int getRenderOrder()
        {
            return renderOrder;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            if (!(other instanceof RendererWrapper)) return false;

            RendererWrapper tmp = (RendererWrapper) other;
            return renderClass.equals(tmp.renderClass) && renderOrder == tmp.renderOrder;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 61 * hash + Objects.hashCode(this.renderClass);
            hash = 61 * hash + this.renderOrder;
            return hash;
        }

        @Override
        public int compareTo(RendererWrapper other) {
            return Integer.compare(this.renderOrder, other.renderOrder);
        }
    }
}
