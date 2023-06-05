package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

public class xlu_assault_deck extends BaseHullMod {

	public static final float COST_REDUCTION  = 4;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION);
		stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION);
		stats.getDynamic().getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION);
                
		stats.addListener(new FighterOPCostModifier() {
                    @Override
                    public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
                        if (fighter.getId().equals("xlu_mattock_wing"))
                            return (int) currCost - 2;
                        return currCost;
                    }
		});
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) COST_REDUCTION + "";
		if (index == 1) return "2";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}








