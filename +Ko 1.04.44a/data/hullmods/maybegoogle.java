package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class maybegoogle extends BaseHullMod {

	private static Map mgoog = new HashMap();
	static {
		mgoog.put(HullSize.FRIGATE, -4f);
		mgoog.put(HullSize.DESTROYER, -2f);
		mgoog.put(HullSize.CRUISER, 0f);
		mgoog.put(HullSize.CAPITAL_SHIP, 2f);
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getDynamic().getStat(Stats.PHASE_TIME_BONUS_MULT).modifyMult(id, 1.2f);

		boolean sMod = isSMod(stats);
		if (sMod) {
		stats.getPhaseCloakCooldownBonus().modifyMult(id, 1f - 40f / 100f);
		}

///Tweaks to Phase Mine Dispenser for MBaye ships.
		if (!stats.getVariant().getHullSpec().isBuiltInMod("ex_phase_coils")) {
		stats.getSystemUsesBonus().modifyFlat(id, (Float) mgoog.get(hullSize));
		stats.getSystemRegenBonus().modifyMult(id, 0.5f);
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "Phase Lance";
		if (index == 1) return "" + "20%";
		return null;
	}

	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + "40%";
		return null;
	}


}








