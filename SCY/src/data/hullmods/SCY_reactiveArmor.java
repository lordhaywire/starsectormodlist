package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import data.scripts.util.MagicIncompatibleHullmods;
import static data.scripts.util.SCY_txt.txt;
import java.util.HashSet;
import java.util.Set;

public class SCY_reactiveArmor extends BaseHullMod {

    private final float explosive = -33f;    
    private final float energy = 25f;
    private final float kinetic = 25f;

    private final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    {
        // These hullmods will automatically be removed
//        BLOCKED_HULLMODS.add("heavyarmor");
        BLOCKED_HULLMODS.add("ii_armor_package");
        BLOCKED_HULLMODS.add("SCY_lightArmor");
        BLOCKED_HULLMODS.add("SKR_ancientArmor");
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
        stats.getHighExplosiveDamageTakenMult().modifyMult(id, 1+(explosive/100));
        
        stats.getEnergyDamageTakenMult().modifyPercent(id, energy);
        stats.getKineticDamageTakenMult().modifyPercent(id, kinetic);
        //stats.getFragmentationDamageTakenMult().modifyMult(id, 1+(frag/100));
        //stats.getArmorDamageTakenMult().modifyMult(id, frag);
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {   
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "SCY_reactiveArmor");
            }
        }
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        
        if (index == 0) return "" + Math.round(explosive);  
        if (index == 1) return "" + Math.round(energy); 
        if (index == 2) return "" + Math.round(kinetic); 
        
        return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {                
                return false;
            }
        }
        return true;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {                
                return txt("hm_incompatible")+Global.getSettings().getHullModSpec(tmp).getDisplayName();
            }
        }
        return null;
    }
}
