package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;

import static RealisticCombat.util.JSONUtils.toColor;

public final class Colors {

    /**
     * Path to the RealisticCombat.settings file
     */
    private static final String SETTINGS_FILE_PATH = "data/config/Colors.json";

    /**
     * Alpha of all elements
     */
    private static float ALPHA;

    /**
     * Colors
     */
    private static java.awt.Color
            PLAYER, ALLY, FRIENDLY, ENEMY, TARGET, HULK, NEUTRAL,

            FLUX,

            KINETIC, HIGH_EXPLOSIVE, FRAGMENTATION, ENERGY, EMP;


    /**
     * @return {@code float} alpha depending on zoom
     */
    public static float getAlpha(final float viewMult) {
        return ALPHA * (viewMult < 4 ? 0.5f : 1);
    }

    /**
     * @return {@link java.awt.Color} corresponding to player
     */
    public static java.awt.Color getPlayer() { return PLAYER; }

    /**
     * @return {@link java.awt.Color} corresponding to ally
     */
    public static java.awt.Color getAlly() { return ALLY; }

    /**
     * @return {@link java.awt.Color} corresponding to friendly
     */
    public static java.awt.Color getFriendly() { return FRIENDLY; }

    /**
     * @return {@link java.awt.Color} corresponding to enemy
     */
    public static java.awt.Color getEnemy() { return ENEMY; }

    /**
     * @return {@link java.awt.Color} corresponding to target
     */
    public static java.awt.Color getTarget() { return TARGET; }

    /**
     * @return {@link java.awt.Color} corresponding to neutral
     */
    public static java.awt.Color getNeutral() { return NEUTRAL; }

    /**
     * @return {@link java.awt.Color} corresponding to flux
     */
    public static java.awt.Color getFlux() { return FLUX; }

    /**
     * @return {@link java.awt.Color} corresponding to a {@link ShipAPI}
     */
    public static java.awt.Color getColor(final ShipAPI ship) {
        if (ship.isHulk()) return HULK;
        final ShipAPI player = Global.getCombatEngine().getPlayerShip();
        if (ship == player) return PLAYER;
        if (ship.isAlly()) return ALLY;
        if (ship.getOwner() == player.getOwner()) return FRIENDLY;
        if (ship == player.getShipTarget()) return TARGET;
        if (ship.getOwner() + player.getOwner() == 1) return ENEMY;
        return NEUTRAL;
    }

    /**
     * @return {@link java.awt.Color} corresponding to a {@link ShipAPI}
     */
    public static java.awt.Color getColor(final DamagingProjectileAPI projectile) {
        if (projectile.getDamage().getFluxComponent() > projectile.getDamageAmount())
            return EMP;
        switch (projectile.getDamageType()) {
            case KINETIC: return KINETIC;
            case HIGH_EXPLOSIVE: return HIGH_EXPLOSIVE;
            case ENERGY: return ENERGY;
            case FRAGMENTATION: return FRAGMENTATION;
            default: return FLUX;
        }
    }

    /**
     * Load the RealisticCombat.settings from the file.
     */
    public static void load() throws JSONException, IOException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);
        final boolean Default = settings.getBoolean("default");

        ALPHA = Default ? 0.5f : (float) settings.getDouble("alpha");

        PLAYER = Default ? new Color(50, 100, 255)
                         : toColor(settings.getJSONArray("player"));
        ALLY = Default ? Global.getSettings().getColor("textFriendColor")
                       : toColor(settings.getJSONArray("friendly"));
        FRIENDLY = Default ? Global.getSettings().getColor("textFriendColor")
                           : toColor(settings.getJSONArray("friendly"));
        ENEMY = Default ? Global.getSettings().getColor("textEnemyColor")
                        : toColor(settings.getJSONArray("enemy"));
        TARGET = Default ? new Color(255, 0, 0)
                         : toColor(settings.getJSONArray("target"));
        HULK = Default ? Global.getSettings().getColor("textGrayColor")
                       : toColor(settings.getJSONArray("hulk"));
        NEUTRAL = Default ? Global.getSettings().getColor("textNeutralColor")
                          : toColor(settings.getJSONArray("neutral"));
        FLUX = Default ? new Color(200, 200, 200)
                       : toColor(settings.getJSONArray("flux"));

        KINETIC = Default ? new Color(255, 255, 255)
                          : toColor(settings.getJSONArray("kinetic"));
        HIGH_EXPLOSIVE = Default ? new Color(255,0,0)
                                 : toColor(settings.getJSONArray("highExplosive"));
        FRAGMENTATION = Default ? new Color(255,255,0)
                                : toColor(settings.getJSONArray("fragmentation"));
        ENERGY = Default ? new Color(0,255,0)
                : toColor(settings.getJSONArray("energy"));
        EMP = Default ? new Color(0, 255, 255)
                : toColor(settings.getJSONArray("energy"));
    }
}
