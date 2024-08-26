package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class Toggles {

    private static final String SETTINGS_FILE_PATH = "data/config/Toggles.json";

    private static boolean
            DAMAGE_REDUCES_CR,
            DISPLAY_WEAPON_ARC,
            ENEMY_FLEET_RETREATS,
            MODIFY_FIGHTER_SPECS,
            MODIFY_SHIP_SPECS,
            MODIFY_MAP,
            MODIFY_WEAPON_SPECS,
            REPLACE_DAMAGE_MODEL,
            THREE_DIMENSIONAL_TARGETING;


    /**
     * @return {code boolean} whether the vanilla damage model should be replaced
     */
    public static boolean isDamageModelToBeReplaced() {
        return REPLACE_DAMAGE_MODEL;
    }

    /**
     * @return {@code boolean} whether damage should reduce combat readiness
     */
    public static boolean isDamageToReduceCR() { return DAMAGE_REDUCES_CR; }

    /**
     * @return {@code boolean} whether to modify ShipHullSpecs
     */
    public static boolean isEveryShipSpecToBeModified() { return MODIFY_SHIP_SPECS; }

    /**
     * @return {@code boolean} whether to modify FighterWingSpecs
     */
    public static boolean isEveryFighterSpecToBeModified() { return MODIFY_FIGHTER_SPECS; }

    /**
     * @return {@code boolean} whether to modify WeaponSpecs
     */
    public static boolean isEveryWeaponSpecToBeModified() { return MODIFY_WEAPON_SPECS; }

    /**
     * @return whether the commander of the enemy fleet, with thresholds
     *         depending on personality, orders full retreat should the total
     *         deployment points of its remaining combat ships, including
     *         reserves, be too small a fraction of the same total of the
     *         player fleet or too great a fraction of the enemy total have
     *         been lost
     */
    public static boolean isEnemyFleetToRetreat() { return ENEMY_FLEET_RETREATS; }

    /**
     * @return {@code boolean} whether the combat map should be modified
     */
    public static boolean isMapToBeModified() {
        return MODIFY_MAP;
    }

    /**
     * @return {@code boolean} whether every ship should limit the range of
     *         every non-missile projectile weapon on it to one at which
     *         the target of that weapon could not strafe, whether horizontally
     *         or vertically, off the path of the projectile before it would
     *         strike
     */
    public static boolean isTargetingThreeDimensional() { return THREE_DIMENSIONAL_TARGETING; }

    /**
     * @return {@code boolean} whether every non-point-defense weapon arc of
     *         the player and target, if selected, should be displayed, arcs of
     *         selected groups or weapons targeting the player highlighted
     */
    public static boolean isWeaponArcTobeDisplayed() { return DISPLAY_WEAPON_ARC; }


    public static void load() throws JSONException, IOException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);

        MODIFY_MAP = settings.getBoolean("modifyMap");
        THREE_DIMENSIONAL_TARGETING = settings.getBoolean("threeDimensionalTargeting");
        REPLACE_DAMAGE_MODEL = settings.getBoolean("replaceDamageModel");
        DAMAGE_REDUCES_CR = settings.getBoolean("damageReducesCR");
        ENEMY_FLEET_RETREATS = settings.getBoolean("enemyFleetRetreats");
        MODIFY_SHIP_SPECS = settings.getBoolean("modifyShipSpecs");
        MODIFY_FIGHTER_SPECS= settings.getBoolean("modifyFighterSpecs");
        MODIFY_WEAPON_SPECS = settings.getBoolean("modifyWeaponSpecs");
        DISPLAY_WEAPON_ARC = settings.getBoolean("displayWeaponArc");
    }
}
