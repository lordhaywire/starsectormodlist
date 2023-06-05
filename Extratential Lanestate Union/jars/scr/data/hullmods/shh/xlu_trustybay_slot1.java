
package data.hullmods.shh;

import com.fs.starfarer.api.combat.*;

public class xlu_trustybay_slot1 extends BaseHullMod {
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getNumFighterBays().modifyFlat(id, 1);
        stats.getMaxSpeed().modifyFlat(id, -20f);
        stats.getAcceleration().modifyFlat(id, -15f);
        stats.getDeceleration().modifyFlat(id, -10f);
        stats.getTurnAcceleration().modifyFlat(id, -10f);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }
}

