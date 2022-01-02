package hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

/**
 * Author: SafariJohn
 */
public class Roider_ArmatureCore extends BaseHullMod {
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getAutofireAimAccuracy().modifyFlat(id, 1f);
        
		stats.getEccmChance().modifyFlat(id, 1f);
		stats.getDynamic().getMod(Stats.PD_IGNORES_FLARES).modifyFlat(id, 1f);
	}
}
