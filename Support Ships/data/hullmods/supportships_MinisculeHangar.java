package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import data.scripts.util.MagicIncompatibleHullmods;

public class supportships_MinisculeHangar extends BaseHullMod {

	public static final int ALL_FIGHTER_COST_PERCENT = 50;
	public static final int BOMBER_COST_PERCENT = 100;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		


		stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyPercent(id, BOMBER_COST_PERCENT);
		stats.getDynamic().getMod(Stats.FIGHTER_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
		stats.getDynamic().getMod(Stats.INTERCEPTOR_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
		stats.getDynamic().getMod(Stats.SUPPORT_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
		
		if(stats.getVariant().getHullMods().contains("expanded_deck_crew")){
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "expanded_deck_crew", "supportships_miniscule_hangar");
        }
	}

	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + BOMBER_COST_PERCENT + "%";
		if (index == 1) return "" + ALL_FIGHTER_COST_PERCENT + "%";
		return null;
	}
	
	@Override
	public boolean affectsOPCosts() {
		return true;
	}
}



