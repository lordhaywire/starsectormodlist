package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import org.json.JSONException;
import org.json.JSONObject;
import RealisticCombat.util.JSONUtils;

import java.io.IOException;
import java.util.HashMap;

import static com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

public final class Categorization {

    private static final String SETTINGS_FILE_PATH = "data/config/Categorization.json";

    private static HashMap<WeaponCategory, HashMap<DamageType, Float>> DAMAGE_THRESHOLDS;

    /**
     * Categories of weapons based on what they fire.
     *
     * <li>{@code PROJECTILE}</li> un-guided, un-powered projectiles
     *
     * <li>{@code LAUNCHER_MISSILE}</li> {@code MISSILE} {@link WeaponType}
     * and higher damage
     *
     * <li>{@code LAUNCHER_TORPEDO}</li> {@code MISSILE} {@link WeaponType}
     * and lower damage
     *
     * <li>{@code BEAM}</li> {@code ENERGY} {@link WeaponType} and fires a beam
     */
    public enum WeaponCategory {
        PROJECTILE,
        LAUNCHER_MISSILE,
        LAUNCHER_TORPEDO,
        BEAM
    }

    /**
     * Categories of missile based on their burst or continuous fire,
     * targeting ships or point defense, and ship or missile firing
     * platform.
     *
     * <li>ANTI_SHIP_BURST</li> fires in a burst, at a ship, from a ship
     * <li>ANTI_SHIP_CONTINUOUS</li> fires continuously, at a ship, from a ship
     * <li>POINT_DEFENSE_BURST</li> fires in a burst, in point defense, from a ship
     * <li>POINT_DEFENSE_CONTINUOUS</li> fires continuously, in point defense, from a ship
     * <li>DIRECTED_ENERGY_MUNITION_BURST</li> fires in a burst from a missile
     * <li>DIRECTED_ENERGY_MUNITION_CONTINUOUS</li> fires continuously from a missile
     */
    public enum BeamCategory {
        ANTI_SHIP_BURST,
        ANTI_SHIP_CONTINUOUS,
        POINT_DEFENSE_BURST,
        POINT_DEFENSE_CONTINUOUS,
        DIRECTED_ENERGY_MUNITION_BURST,
        DIRECTED_ENERGY_MUNITION_CONTINUOUS
    }

    /**
     * @return {@code float} minimum damage-per-shot of a {@link WeaponSpecAPI}
     *         to be considered a cannon
     */
    public static float getDamageThreshold(final WeaponCategory weaponCategory,
                                           final DamageType damageType) {
        return DAMAGE_THRESHOLDS.get(weaponCategory).get(damageType);
    }

    public static void load() throws JSONException, IOException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);
        DAMAGE_THRESHOLDS = JSONUtils.getWeaponCategoryByDamageTypeFloats(settings.getJSONObject(
                "damageThresholds"));
    }
}
