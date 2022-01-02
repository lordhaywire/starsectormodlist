package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import data.scripts.util.MagicIncompatibleHullmods;
import static data.scripts.util.SCY_txt.txt;
import java.util.HashSet;
import java.util.Set;

public class SCY_minimalPrep extends BaseHullMod {
    
    private final float shipMaintenance = 50f;
    private final float shipRecovery = 50f;
    private final float shipCrLoss = 40f;
    private final float shipPPTLoss = 75f;
        
    private final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    {
        // These hullmods will automatically be removed
        BLOCKED_HULLMODS.add("supply_conservation_program");
        BLOCKED_HULLMODS.add("militarized_subsystems");
        BLOCKED_HULLMODS.add("efficiency_overhaul");
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSuppliesPerMonth().modifyMult(id,shipMaintenance*0.01f);
        stats.getRepairRatePercentPerDay().modifyMult(id,shipRecovery*0.01f);
        stats.getMaxCombatReadiness().modifyMult(id, 1-(shipCrLoss*0.01f));
        
        stats.getPeakCRDuration().modifyMult(id, 1-(shipPPTLoss*0.01f));
        
        stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1-shipRecovery);
        stats.getCombatEngineRepairTimeMult().modifyMult(id, 1-shipRecovery);
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){     
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "SCY_minimalPrep");  
            }
        }
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int)shipMaintenance;  
        if (index == 1) return "" + (int)shipRecovery;  
        if (index == 2) return "" + (int)shipCrLoss;  
        if (index == 3) return "" + (int)shipPPTLoss;  
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
