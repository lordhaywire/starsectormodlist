package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class filgap_Luddic_Refit extends BaseHullMod {

	private static final float HULL_BONUS_MULT = 1.1f;
	private static final float SUPPLY_USE_MULT = 0.95f;
	private static final float HANDLING_MULT = 0.95f;

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FIGHTER, 0f);
		mag.put(HullSize.FRIGATE, 5f);
		mag.put(HullSize.DESTROYER, 10f);
		mag.put(HullSize.CRUISER, 20f);
		mag.put(HullSize.CAPITAL_SHIP, 50f);
	}
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getHullBonus().modifyMult(id, (Float) HULL_BONUS_MULT);
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT);
		stats.getMaxSpeed().modifyMult(id, HANDLING_MULT);
		stats.getAcceleration().modifyMult(id, HANDLING_MULT);
		stats.getDeceleration().modifyMult(id, HANDLING_MULT);
		stats.getMaxTurnRate().modifyMult(id, HANDLING_MULT);
		stats.getTurnAcceleration().modifyMult(id, HANDLING_MULT);
		stats.getMinCrewMod().modifyFlat(id, (Float) mag.get(hullSize));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((HULL_BONUS_MULT - 1f) * 100f); 
		if (index == 1) return "" + (int) ((1f - SUPPLY_USE_MULT) * 100f); 
		if (index == 2) return "" + (int) ((1f - HANDLING_MULT) * 100f); 
		if (index == 3) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 4) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 5) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 6) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}


}
