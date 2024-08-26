package RealisticCombat.settings;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class Status {

    private static boolean ENABLED;

    private static float EDGE_THICKNESS, INNERMOST_DIAMOND_RADIUS_FACTOR;

    public enum QuantityName { HULL, FLUX }


    /**
     * @return {@code boolean} should indicator diamonds be displayed
     */
    public static boolean isEnabled() { return ENABLED; }

    /**
     * @return {@code float} base thickness of every edge of every
     *         indicator diamond
     */
    public static float getEdgeThickness() { return EDGE_THICKNESS; }

    /**
     * @return {@code float} factor of the base radius, from center to corner,
     *         of the innermost indicator diamond equalling that of the next-
     *         innermost indicator diamond, with the radius of the third-
     *         innermost diamond equalling that of the innermost diamond
     *         times twice this factor, and so on.
     *
     */
    public static float getInnermostDiamondRadiusFactor() {
        return INNERMOST_DIAMOND_RADIUS_FACTOR;
    }

    /**
     * @return {@link java.awt.Color} of the flux diamond of any {@link ShipAPI}
     */
    private static java.awt.Color getFluxColor(final ShipAPI ship) {
        return (ship.getFluxTracker().isOverloaded())
                ? ship.getOverloadColor()
                : (ship.getFluxTracker().isVenting())
                    ? ship.getVentCoreColor()
                    : Colors.getFlux();
    }

    /**
     * @return {@link java.awt.Color} of any diamond of any {@link ShipAPI}
     */
    public static java.awt.Color getColor(final ShipAPI ship, final QuantityName quantityName) {
        switch (quantityName) {
            case HULL: return Colors.getColor(ship);
            case FLUX: return getFluxColor(ship);
            default: return Misc.getGrayColor();
        }
    }


    public static void load(final JSONObject json) throws JSONException, IOException {
        final JSONObject settings = json.getJSONObject(Diffraction.class.getSimpleName());
        final boolean Default = Indication.isDefault();

        ENABLED = settings.getBoolean("enabled");
        EDGE_THICKNESS = Default ? 6.7f : (float) settings.getDouble("edgeThickness");
        INNERMOST_DIAMOND_RADIUS_FACTOR = Default
                ? 1.15f : (float) settings.getDouble("innermostDiamondRadiusFactor");
    }
}
