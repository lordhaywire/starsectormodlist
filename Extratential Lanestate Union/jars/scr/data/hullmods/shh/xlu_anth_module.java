package data.hullmods.shh;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class xlu_anth_module extends BaseHullMod {
        //private static final String xlu_anthy_engine = "xlu_anth_module";
    

        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.MODULE_DETACH_CHANCE_MULT).modifyFlat(id, 100f);
	}
	
        @Override
        public void advanceInCombat(ShipAPI ship, float amount) {
            super.advanceInCombat(ship, amount);
            if (!ship.isAlive()) return;
            if (ship.getTravelDrive().getState() == ShipSystemAPI.SystemState.ACTIVE) return;
        
            if (ship.isStationModule() && ship.getHitpoints() > (ship.getMaxHitpoints() * 0.4f)) {
                ship.getEngineController().forceFlameout();
            }
            
            if (ship.getHitpoints() < (ship.getMaxHitpoints() * 0.4f)) {
                ship.setRenderEngines(true);
                ship.setParentStation(null);
                ship.setStationSlot(null);
            }
        }

        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

}
