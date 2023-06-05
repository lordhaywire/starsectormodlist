package hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import ids.Roider_Ids.Roider_Hullmods;
import org.magiclib.util.MagicIncompatibleHullmods;

/**
 * Author: SafariJohn
 */
public class Roider_FirestormMod extends BaseHullMod {

	public static final float COST_MOD = 4;

	public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -COST_MOD);
		stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyFlat(id, -COST_MOD);
	}

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship.getVariant().hasHullMod("ballistic_rangefinder")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(),
                    "ballistic_rangefinder", Roider_Hullmods.FIRESTORM_MOD);
        }
    }

	public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 0) return "" + (int) COST_MOD + "";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}
