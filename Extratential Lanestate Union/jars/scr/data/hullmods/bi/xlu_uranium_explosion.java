package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class xlu_uranium_explosion extends BaseHullMod {

	public static final float RADIUS_MULT = 2.0f;
	public static final float DAMAGE_MULT = 1.75f;
	public static final float EXPLOSIVE_SIZE = 2.5f;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, DAMAGE_MULT);
		stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, RADIUS_MULT);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
            if(index == 0) return "" + (int) (EXPLOSIVE_SIZE * 100) + "%";
            //if(index == 0) return "" + (int) (RADIUS_MULT * 100) + "%";
            //if(index == 1) return "" + (int) (DAMAGE_MULT * 100) + "%";
            else {
                return null;
            }
	}


}
