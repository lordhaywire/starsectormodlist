package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import org.json.JSONException;
import org.json.JSONObject;
import RealisticCombat.settings.Categorization.WeaponCategory;
import RealisticCombat.settings.Categorization.BeamCategory;
import RealisticCombat.util.JSONUtils;

import java.io.IOException;
import java.util.HashMap;

public final class WeaponSpecs {

    private static final String SETTINGS_FILE_PATH = "data/config/WeaponSpecs.json";

    private static int POINT_DEFENSE_BURST_BEAM_AMMO_FACTOR;

    private static float DAMAGE_BONUS_HIGH_EXPLOSIVE_PROJECTILE,
                         MUZZLE_VELOCITY_FACTOR,
                         MUZZLE_VELOCITY_MINIMUM,
                         MUZZLE_VELOCITY_MAXIMUM,
                         MINIMUM_SPREAD,
                         MAXIMUM_SPREAD,
                         INTENSITY_LIMIT_POINT_DEFENSE_BURST_BEAM,
                         INTENSITY_LIMIT_POINT_DEFENSE_CONTINUOUS_BEAM,
                         ANTI_SHIP_BEAM_MINIMUM_FLUX_EFFICIENCY,
                         POINT_DEFENSE_BEAM_MINIMUM_FLUX_EFFICIENCY;

    private static HashMap<WeaponCategory, Float>
            MAX_SPEED_FACTORS,
            ACCELERATION_FACTORS,
            DECELERATION_FACTORS,
            TURN_ACCELERATION_FACTORS,
            MAX_TURN_RATE_FACTORS;

    private static HashMap<Categorization.BeamCategory, Float> INTENSITY_FACTORS;

    private static HashMap<WeaponCategory, HashMap<WeaponSize, Float>> LAUNCHER_RANGE_BONUSES;

    private static HashMap<WeaponCategory, HashMap<WeaponSize, Integer>> AMMUNITION_CAPACITIES;

    /**
     * @return {@code float} increasing damage of non-missile high-explosive
     *         projectile weapons of less than 1,000 damage
     */
    public static float getProjectileDamageBonus() {
        return DAMAGE_BONUS_HIGH_EXPLOSIVE_PROJECTILE;
    }

    /**
     * @return {@code float} factor whereby to multiply muzzle velocity
     */
    public static float getMuzzleVelocityFactor() { return MUZZLE_VELOCITY_FACTOR; }

    /**
     * @return {@code float} maximum muzzle velocity of a projectile
     */
    public static float getMuzzleVelocityMaximum() { return MUZZLE_VELOCITY_MAXIMUM; }

    /**
     * @return {@code float} maximum muzzle velocity of a projectile
     */
    public static float getMuzzleVelocityMinimum() { return MUZZLE_VELOCITY_MINIMUM; }

    /**
     * @return the {@code float} amount which the spread of non-missile
     *         projectile weapon must equal or exceed
     */
    public static float getMinimumSpread() {
        return MINIMUM_SPREAD;
    }

    /**
     * @return the {@code float} amount which the spread of non-missile
     *         projectile weapon may at most equal.
     */
    public static float getMaximumSpread() { return MAXIMUM_SPREAD; }

    /**
     * @return {@code float} ceiling of maximum ammunition of a
     *         {@link WeaponSpecAPI}
     */
    public static int getMaxAmmoCeiling(final WeaponCategory weaponCategory,
                                        final WeaponSize weaponSize)
    {
        return AMMUNITION_CAPACITIES.get(weaponCategory).get(weaponSize);
    }

    /**
     * @return {@code float} amount whereby to increase the range of a
     *         launcher {@link WeaponSpecAPI}
     */
    public static float getLauncherRangeBonus(final WeaponCategory weaponCategory,
                                              final WeaponSize weaponSize)
    {
        return LAUNCHER_RANGE_BONUSES.get(weaponCategory).get(weaponSize);
    }

    /**
     * @return {@code float} multiplier of the maximum speed of the
     *         {@link ShipHullSpecAPI.EngineSpecAPI} of a {@link MissileSpecAPI}
     */
    public static float getMaxSpeedFactor(final WeaponCategory weaponCategory) {
        return MAX_SPEED_FACTORS.get(weaponCategory);
    }

    /**
     * @return {@code float} multiplier of the acceleration of the
     *         {@link ShipHullSpecAPI.EngineSpecAPI} of a {@link MissileSpecAPI}
     */
    public static float getAccelerationFactor(final WeaponCategory weaponCategory) {
        return ACCELERATION_FACTORS.get(weaponCategory);
    }

    /**
     * @return {@code float} multiplier of the deceleration of the
     *         {@link ShipHullSpecAPI.EngineSpecAPI} of a {@link MissileSpecAPI}
     */
    public static float getDecelerationFactor(final WeaponCategory weaponCategory) {
        return DECELERATION_FACTORS.get(weaponCategory);
    }

    /**
     * @return {@code float} multiplier of the turn acceleration of the
     *         {@link ShipHullSpecAPI.EngineSpecAPI} of a {@link MissileSpecAPI} based on its
     *         {@link WeaponCategory}
     */
    public static float getTurnAccelerationFactor(final WeaponCategory weaponCategory) {
        return TURN_ACCELERATION_FACTORS.get(weaponCategory);
    }

    /**
     * @return {@code float} multiplier of the turn max turn rate of the
     *         {@link ShipHullSpecAPI.EngineSpecAPI} of a {@link MissileSpecAPI} based on its
     *         {@link WeaponCategory}
     */
    public static float getMaxTurnRateFactor(final WeaponCategory weaponCategory) {
        return MAX_TURN_RATE_FACTORS.get(weaponCategory);
    }

    /**
     * @return {@code float} amount whereby to multiply the intensity
     *         of every beam {@link WeaponSpecAPI} firing a beam
     */
    public static float getBeamIntensityFactor(final boolean burst,
                                               final boolean pointDefense,
                                               final boolean directedEnergyMunition)
    {
        return directedEnergyMunition
                ? burst ? INTENSITY_FACTORS.get(BeamCategory.DIRECTED_ENERGY_MUNITION_BURST)
                        : INTENSITY_FACTORS.get(BeamCategory.DIRECTED_ENERGY_MUNITION_CONTINUOUS)
                : pointDefense
                    ? burst ? INTENSITY_FACTORS.get(BeamCategory.POINT_DEFENSE_BURST)
                            : INTENSITY_FACTORS.get(BeamCategory.POINT_DEFENSE_CONTINUOUS)
                    : burst ? INTENSITY_FACTORS.get(BeamCategory.ANTI_SHIP_BURST)
                            : INTENSITY_FACTORS.get(BeamCategory.ANTI_SHIP_CONTINUOUS);
    }

    public static float getPointDefenseBeamIntensityLimit(final boolean burst)
    {
        return burst ? INTENSITY_LIMIT_POINT_DEFENSE_BURST_BEAM
                     : INTENSITY_LIMIT_POINT_DEFENSE_CONTINUOUS_BEAM;
    }

    /**
     * return {@code float} damage per second below which point defense beam
     *        range should be cut-off
     */
    public static float getAntiShipBeamMinimumFluxEfficiency() {
        return ANTI_SHIP_BEAM_MINIMUM_FLUX_EFFICIENCY;
    }

    /**
     * return {@code float} damage per second below which anti-ship beam range
     *        should be cut-off
     */
    public static float getPointDefenseBeamMinimumFluxEfficiency() {
        return POINT_DEFENSE_BEAM_MINIMUM_FLUX_EFFICIENCY;
    }

    /**
     * @return {@code float} whereby to multiply ammunition
     *         capacity of point-defense burst beams
     */
    public static int getPointDefenseBurstBeamAmmoFactor() {
        return POINT_DEFENSE_BURST_BEAM_AMMO_FACTOR;
    }


    public static void load() throws JSONException, IOException {
        final JSONObject
                settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH),
                projectileWeaponAttributes = settings.getJSONObject("projectileWeaponAttributes"),
                launcherWeaponAttributes = settings.getJSONObject("launcherWeaponAttributes"),
                beamWeaponAttributes = settings.getJSONObject("beamWeaponAttributes"),
                pointDefenseBeamIntensityLimits = beamWeaponAttributes.getJSONObject(
                        "pointDefenseBeamIntensityLimits");

        DAMAGE_BONUS_HIGH_EXPLOSIVE_PROJECTILE = (float) projectileWeaponAttributes.getDouble(
                "damageBonusHighExplosiveProjectile");
        MUZZLE_VELOCITY_FACTOR = (float) projectileWeaponAttributes.getDouble(
                "muzzleVelocityFactor");
        MUZZLE_VELOCITY_MINIMUM = (float) projectileWeaponAttributes.getDouble(
                "muzzleVelocityMinimum");
        MUZZLE_VELOCITY_MAXIMUM = (float) projectileWeaponAttributes.getDouble(
                "muzzleVelocityMaximum");
        MINIMUM_SPREAD = (float) projectileWeaponAttributes.getDouble("minimumSpread");
        MAXIMUM_SPREAD = (float) projectileWeaponAttributes.getDouble("maximumSpread");

        AMMUNITION_CAPACITIES = JSONUtils.getWeaponCategoryByWeaponSizeIntegers(
                launcherWeaponAttributes.getJSONObject("ammunitionCapacities"));
        LAUNCHER_RANGE_BONUSES = JSONUtils.getWeaponCategoryByWeaponSizeFloats(
                launcherWeaponAttributes.getJSONObject("launcherRangeBonuses"));
        MAX_SPEED_FACTORS = JSONUtils.getWeaponCategoryToFloatHashMap(
                launcherWeaponAttributes.getJSONObject("maxSpeed"));
        ACCELERATION_FACTORS = JSONUtils.getWeaponCategoryToFloatHashMap(
                launcherWeaponAttributes.getJSONObject("acceleration"));
        DECELERATION_FACTORS = JSONUtils.getWeaponCategoryToFloatHashMap(
                launcherWeaponAttributes.getJSONObject("deceleration"));
        TURN_ACCELERATION_FACTORS = JSONUtils.getWeaponCategoryToFloatHashMap(
                launcherWeaponAttributes.getJSONObject("turnAcceleration"));
        MAX_TURN_RATE_FACTORS = JSONUtils.getWeaponCategoryToFloatHashMap(
                launcherWeaponAttributes.getJSONObject("maxTurnRate"));

        INTENSITY_FACTORS = JSONUtils.getBeamCategoryToFloatHashMap(
                beamWeaponAttributes.getJSONObject("intensityFactors"));

        INTENSITY_LIMIT_POINT_DEFENSE_BURST_BEAM =
                (float) pointDefenseBeamIntensityLimits.getDouble("burst");
        INTENSITY_LIMIT_POINT_DEFENSE_CONTINUOUS_BEAM =
                (float) pointDefenseBeamIntensityLimits.getDouble("continuous");

        ANTI_SHIP_BEAM_MINIMUM_FLUX_EFFICIENCY = (float) beamWeaponAttributes.getDouble(
                "antiShipBeamMinimumFluxEfficiency");
        POINT_DEFENSE_BEAM_MINIMUM_FLUX_EFFICIENCY = (float) beamWeaponAttributes.getDouble(
                "pointDefenseBeamMinimumFluxEfficiency");
        POINT_DEFENSE_BURST_BEAM_AMMO_FACTOR = beamWeaponAttributes.getInt(
                "pointDefenseBurstBeamAmmoFactor");
    }
}
