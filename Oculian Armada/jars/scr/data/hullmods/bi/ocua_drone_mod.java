package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ocua_drone_mod extends BaseHullMod {

	public static final float VISION_MULT = 25f;
        
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

            stats.getMinCrewMod().modifyMult(id, 0);
            stats.getMaxCrewMod().modifyMult(id, 0);
            stats.getSightRadiusMod().modifyMult(id, 1 + (VISION_MULT / 100));
            if (hullSize == HullSize.FRIGATE) {
                stats.getSensorStrength().modifyFlat(id, 20.0f);
            } else if (hullSize == HullSize.DESTROYER) {
                stats.getSensorStrength().modifyFlat(id, 15.0f);
            } else if (hullSize == HullSize.CRUISER) {
                stats.getSensorStrength().modifyFlat(id, 10.0f);
            } else if (hullSize == HullSize.CAPITAL_SHIP) {
                stats.getSensorStrength().modifyFlat(id, 25.0f);
            }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
//		if(index == 0) return "" + (int) RANK_BONUS + "%";
		if(index == 0) return "does not suffer from Automation penalties";
		if(index == 1) return "" + (int) VISION_MULT + "%";
        else {
            return null;
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
