package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DiableAvionicsMount extends BaseHullMod {

    private final float RANGE_BOOST=200;
    private final float DAMAGE_TAKEN=100;
    private final float FIRERATE_REDUCTION=-20;
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getMutableStats().getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        ship.getMutableStats().getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        ship.getMutableStats().getBeamWeaponRangeBonus().modifyFlat(id, -RANGE_BOOST);
        
        ship.getMutableStats().getBallisticRoFMult().modifyPercent(id, FIRERATE_REDUCTION);
        ship.getMutableStats().getEnergyRoFMult().modifyPercent(id, FIRERATE_REDUCTION);
        
        ship.getMutableStats().getWeaponDamageTakenMult().modifyPercent(id, DAMAGE_TAKEN);
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return RANGE_BOOST+" "+txt("su");
        }
        if (index == 1) {
            return 100+FIRERATE_REDUCTION+txt("%");
        }
        if (index == 2) {
            return DAMAGE_TAKEN+txt("%");
        }
        return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if(ship==null) return false;
        return !ship.getVariant().getHullMods().contains("diableavionics_mountBI"); 
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return txt("hm_builtin");
    }
}