package RealisticCombat.util;

import RealisticCombat.settings.Categorization.BeamCategory;
import RealisticCombat.settings.Categorization.WeaponCategory;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;

public final class JSONUtils {

    /**
     * @param array three {@code int} {@link JSONArray}
     * <p></p>
     * @return {@link Color} of a three {@code int} {@link JSONArray}
     * <p></p>
     * @throws JSONException something went wrong when reading the JSON
     */
    public static Color toColor(final JSONArray array) throws JSONException {
        return new Color(array.getInt(0),
                         array.getInt(1),
                         array.getInt(2),
                         (array.length() == 4 ? array.getInt(3) : 255));
    }

    public static HashMap<Boolean, Float> getBooleanToFloatHashmap(final JSONObject json) {
        final HashMap<Boolean, Float> hashMap = new HashMap<>();
        try {
            hashMap.put(false, (float) json.getDouble("false"));
            hashMap.put(true, (float) json.getDouble("true"));
        } catch (Exception e) { e.printStackTrace(); }
        return hashMap;
    }

    public static HashMap<String, Float> getStringToFloatHashmap(final JSONObject json) {
        final HashMap<String, Float> hashMap = new HashMap<>();
        for (Iterator it = json.keys(); it.hasNext(); ) {
            final String string = (String) it.next();
            try {hashMap.put(string, (float) json.getDouble(string)); }
            catch (JSONException e) { e.printStackTrace(); }
        } return hashMap;
    }

    public static HashMap<WeaponCategory, Float> getWeaponCategoryToFloatHashMap(
            final JSONObject categories
    ) {
        final HashMap<WeaponCategory, Float> hashMap = new HashMap<WeaponCategory, Float>() {};
        for (WeaponCategory category : WeaponCategory.values()) {
            try {
                if (categories.has(category.toString()))
                    hashMap.put(category, (float) categories.getDouble(category.toString()));
            } catch (Throwable t) { t.printStackTrace(); }
        } return hashMap;
    }

    public static HashMap<BeamCategory, Float> getBeamCategoryToFloatHashMap(
            final JSONObject categories
    ) {
        final HashMap<BeamCategory, Float> hashMap = new HashMap<BeamCategory, Float>() {};
        for (BeamCategory category : BeamCategory.values()) {
            try {
                if (categories.has(category.toString()))
                    hashMap.put(category, (float) categories.getDouble(category.toString()));
            } catch (Throwable t) { t.printStackTrace(); }
        } return hashMap;
    }

    public static HashMap<WeaponSize, Float> getWeaponSizeToFloatHashMap(
            final JSONObject sizes
    ) {
        final HashMap<WeaponSize, Float> hashMap = new HashMap<WeaponSize, Float>() {};
        for (WeaponSize size : WeaponSize.values()) {
            try {
                if (sizes.has(size.toString()))
                    hashMap.put(size, (float) sizes.getDouble(size.toString()));
            } catch (final Throwable t) { t.printStackTrace(); }
        } return hashMap;
    }

    public static HashMap<WeaponSize, Integer> getWeaponSizeToIntegerHashMap(
            final JSONObject sizes
    ) {
        final HashMap<WeaponSize, Integer> hashMap = new HashMap<WeaponSize, Integer>() {};
        for (WeaponSize size : WeaponSize.values()) {
            try {
                if (sizes.has(size.toString()))
                    hashMap.put(size, sizes.getInt(size.toString()));
            } catch (final Throwable t) { t.printStackTrace(); }
        } return hashMap;
    }

    public static HashMap<DamageType, Float> getDamageTypeToFloatHashMap(final JSONObject types) {
        final HashMap<DamageType, Float> hashMap = new HashMap<DamageType, Float>() {};
        for (DamageType type : DamageType.values()) {
            try { if (types.has(type.toString()))
                hashMap.put(type, (float) types.getDouble(type.toString()));
            } catch (final Throwable t) { t.printStackTrace(); }
        } return hashMap;
    }

    public static HashMap<HullSize, Float> getHullSizeToFloatHashmap(final JSONObject sizes) {
        final HashMap<HullSize, Float> hashMap = new HashMap<HullSize, Float>() {};
        for (HullSize size : HullSize.values()) {
            try { if (sizes.has(size.toString()))
                hashMap.put(size, (float) sizes.getDouble(size.toString())); }
            catch (Throwable t) { t.printStackTrace(); }
        } return hashMap;
    }

    public static HashMap<WeaponCategory, HashMap<DamageType, Float>>
        getWeaponCategoryByDamageTypeFloats(final JSONObject categories)
    {
        final HashMap <WeaponCategory, HashMap<DamageType, Float>> floats =
                new HashMap <WeaponCategory, HashMap<DamageType, Float>>() {};
        for (WeaponCategory category : WeaponCategory.values()) {
            try {
                if (categories.has(category.toString()))
                    floats.put(category, getDamageTypeToFloatHashMap(
                            categories.getJSONObject(category.toString())));
            } catch (final Throwable t) { t.printStackTrace(); }
        } return floats;
    }

    public static HashMap<WeaponCategory, HashMap<WeaponSize, Float>>
        getWeaponCategoryByWeaponSizeFloats(final JSONObject categories)
    {
        final HashMap <WeaponCategory, HashMap<WeaponSize, Float>> floats =
                new HashMap <WeaponCategory, HashMap<WeaponSize, Float>>() {};
        for (WeaponCategory category : WeaponCategory.values()) {
            try {
                if (categories.has(category.toString()))
                    floats.put(category, getWeaponSizeToFloatHashMap(
                            categories.getJSONObject(category.toString())));
            } catch (final Throwable t) { t.printStackTrace(); }
        } return floats;
    }

    public static HashMap<WeaponCategory, HashMap<WeaponSize, Integer>>
        getWeaponCategoryByWeaponSizeIntegers(final JSONObject categories)
    {
        final HashMap <WeaponCategory, HashMap<WeaponSize, Integer>> integers =
                new HashMap <WeaponCategory, HashMap<WeaponSize, Integer>>() {};
        for (WeaponCategory category : WeaponCategory.values()) {
            try {
                if (categories.has(category.toString()))
                    integers.put(category, getWeaponSizeToIntegerHashMap(
                            categories.getJSONObject(category.toString())));
            } catch (final Throwable t) { t.printStackTrace(); }
        } return integers;
    }
}
