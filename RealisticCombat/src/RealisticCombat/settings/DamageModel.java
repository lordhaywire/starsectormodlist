package RealisticCombat.settings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.util.HashMap;

public final class DamageModel {

    private static final String SETTINGS_FILE_PATH = "data/config/DamageModel.json";

    private static float
            ARMOR_OVERMATCH_FACTOR,
            COMPARTMENT_DAMAGE_FACTOR,
            COMPARTMENT_DAMAGE_OVERFLOW_FACTOR,
            MINIMUM_COMBAT_READY_HULL_LEVEL;

    private static HashMap<DamageType, Float>
            DAMAGE_FACTORS,
            SHIELD_DAMAGE_FACTORS,
            ARMOR_THICKNESS_FACTORS;

    private static final Vector2f
            FLOATING_TEXT_OFFSET_COMPARTMENT_DAMAGE = new Vector2f(25, -50),
            FLOATING_TEXT_OFFSET_HULL_DAMAGE = new Vector2f(-25, 0);

    private static final int[][] POSSIBLE_NEARBY_COMPARTMENTS = {
                     {1, -2}, {0, -2}, {-1, -2},
            {2, -1}, {1, -1}, {0, -1}, {-1, -1}, {-2, -1},
            {2,  0}, {1,  0}, {0,  0}, {-1,  0}, {-2,  0},
            {2,  1}, {1,  1}, {0,  1}, {-1,  1}, {-2,  1},
                     {1,  2}, {0,  2}, {-1,  2},
    };

    private static final float[] COMPARTMENT_DAMAGE_DISTRIBUTION = {
                   0.02f, 0.04f, 0.02f,
            0.02f, 0.05f, 0.08f, 0.05f, 0.02f,
            0.04f, 0.08f, 0.15f, 0.08f, 0.04f,
            0.02f, 0.05f, 0.08f, 0.05f, 0.02f,
                   0.02f, 0.03f, 0.02f,
    };


    /**
     * @return {@code float} how many times greater the base damage of a
     *         {@link DamagingProjectileAPI} or {@link MissileAPI}, or
     *         diffracted intensity of a {@link BeamAPI} must be than the
     *         {@link DamageType} adjusted base thickness of a {@link ShipAPI}
     *         armor layer to penetrate it at all angles
     */
    public static float getArmorOvermatchFactor() { return ARMOR_OVERMATCH_FACTOR; }

    /**
     * @return {@code float} how many times composite armor is thicker or
     *         thinner than rolled homogeneous armor is against a
     *         {@link DamageType}
     */
    public static float getArmorThicknessFactor(final DamageType damageType) {
        return ARMOR_THICKNESS_FACTORS.get(damageType);
    }

    /**
     * @return {@code float} how many times more or less damage the
     *         {@link DamageType} deals
     */
    public static float getDamageFactor(final DamageType damageType) {
        return DAMAGE_FACTORS.get(damageType);
    }

    /**
     * @return {@code float} how many times more or less damage the
     *         {@link DamageType} deals to a shield
     */
    public static float getShieldDamageFactor(final DamageType damageType) {
        return SHIELD_DAMAGE_FACTORS.get(damageType);
    }

    /**
     * @return {@code float} factor of {@link DamagingProjectileAPI},
     *         {@link MissileAPI}, or {@link BeamAPI} damage that
     *         becomes additional potential damage to compartments
     */
    public static float getCompartmentDamageFactor() {
        return COMPARTMENT_DAMAGE_FACTOR;
    }

    /**
     * @return {@code float} proportion of damage to a compartment
     *         exceeding its integrity that becomes hull damage
     */
    public static float getCompartmentDamageOverflowFactor() {
        return COMPARTMENT_DAMAGE_OVERFLOW_FACTOR;
    }

    /**
     * @return {@code float} hull level, from 0 to 1, at which combat
     *         readiness should be 0
     */
    public static float getMinimumCombatReadyHullLevel() { return MINIMUM_COMBAT_READY_HULL_LEVEL; }

    /**
     * @author Saltydupler
     */
    public static String getDamageTypeDescription(final DamageType damageType) {
        return String.format(
                "\n%s%% Penetration. %s%% Ship Damage.\n%s%% Shield Damage.",
                Math.round(100 / getArmorThicknessFactor(damageType)),
                Math.round(100 * getDamageFactor(damageType)),
                Math.round(100 * getShieldDamageFactor(damageType))
        );
    }

    public static Vector2f getFloatingTextOffsetCompartmentDamage() {
        return FLOATING_TEXT_OFFSET_COMPARTMENT_DAMAGE;
    }

    public static Vector2f getFloatingTextOffsetHullDamage() {
        return FLOATING_TEXT_OFFSET_HULL_DAMAGE;
    }

    public static int[][] getPossibleNearbyCompartments() { return POSSIBLE_NEARBY_COMPARTMENTS; }

    public static float[] getCompartmentDamageDistribution() {
        return COMPARTMENT_DAMAGE_DISTRIBUTION;
    }


    private static HashMap<DamageType, Float> getDamageTypeFloats(final JSONObject damageTypeFloats)
            throws JSONException
    {
        final HashMap<DamageType, Float> hashMap = new HashMap<>();
        for (DamageType damageType : DamageType.values())
            hashMap.put(damageType, (float) damageTypeFloats.getDouble(damageType.toString()));
        return hashMap;
    }


    public static void load() throws JSONException, IOException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);

        DAMAGE_FACTORS = getDamageTypeFloats(settings.getJSONObject("damageFactors"));
        SHIELD_DAMAGE_FACTORS = getDamageTypeFloats(settings.getJSONObject("shieldDamageFactors"));
        ARMOR_THICKNESS_FACTORS = getDamageTypeFloats(settings.getJSONObject(
                "armorThicknessFactors"));

        ARMOR_OVERMATCH_FACTOR = (float) settings.getDouble("armorOvermatchFactor");
        COMPARTMENT_DAMAGE_FACTOR = (float) settings.getDouble("compartmentDamageFactor");
        COMPARTMENT_DAMAGE_OVERFLOW_FACTOR = (float) settings.getDouble(
                "compartmentDamageOverflowFactor");
        MINIMUM_COMBAT_READY_HULL_LEVEL = (float) settings.getDouble(
                "minimumCombatReadyHullLevel");
    }
}
