package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ocua_shield_module extends BaseHullMod {

        @Override
        public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

            stats.getFluxCapacity().modifyMult(id, (0.5f));
            stats.getFluxCapacity().modifyPercent(id, (100f));
        }

        @Override
        public String getDescriptionParam(int index, HullSize hullSize) {
            if(index == 0) return "50%";
                
            return null;
        }

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
            return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
        }
}
