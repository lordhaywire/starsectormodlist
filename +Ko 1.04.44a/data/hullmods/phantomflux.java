package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class phantomflux extends BaseLogisticsHullMod {
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

			stats.getCargoMod().modifyMult(id, 0.85f);
			stats.getSuppliesPerMonth().modifyMult(id, 1.15f);

		boolean sMod = isSMod(stats);
		if (sMod) {
			stats.getDynamic().getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id, -1f);
			stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -2f);
			stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -4f);
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "15%";
		if (index == 1) return "15%";
		return null;
	}

	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "1/2/4";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}
