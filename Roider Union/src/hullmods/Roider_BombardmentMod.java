package hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: SafariJohn
 */
public class Roider_BombardmentMod extends BaseHullMod {
    private static final float RANGE_BONUS = 35f;
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(3);

    static {
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
        BLOCKED_HULLMODS.add("targetingunit");
        BLOCKED_HULLMODS.add("ilk_SensorSuite");
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
            }
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) (RANGE_BONUS);
        return null;
    }
}

