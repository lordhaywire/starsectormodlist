package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import data.scripts.plugins.xlu_BlockedHullmodDisplayScript;
import java.util.HashSet;
import java.util.Set;
public class xlu_baseless_module extends BaseHullMod {

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("auxiliarythrusters");
        BLOCKED_HULLMODS.add("safetyoverrides");
        BLOCKED_HULLMODS.add("unstable_injector");
        BLOCKED_HULLMODS.add("additional_berthing");
        BLOCKED_HULLMODS.add("auxiliary_fuel_tanks");
        BLOCKED_HULLMODS.add("expanded_cargo_holds");
        BLOCKED_HULLMODS.add("efficiency_overhaul");
        BLOCKED_HULLMODS.add("hiressensors");
        BLOCKED_HULLMODS.add("insulatedengine");
        BLOCKED_HULLMODS.add("augmentedengines");
        BLOCKED_HULLMODS.add("solar_shielding");
        BLOCKED_HULLMODS.add("surveying_equipment");
        BLOCKED_HULLMODS.add("operations_center");
        BLOCKED_HULLMODS.add("ecm");
        BLOCKED_HULLMODS.add("nav_relay");
        BLOCKED_HULLMODS.add("converted_hangar");
    }
    
     @Override
     public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
         super.applyEffectsAfterShipCreation(ship, id);
         
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                xlu_BlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
         

//         if (ship.getVariant().getHullMods().contains("auxiliarythrusters")) {
//             ship.getVariant().removeMod("auxiliarythrusters");
//         }
        
//         if (ship.getVariant().getHullMods().contains("safetyoverrides")) {
//             ship.getVariant().removeMod("safetyoverrides");
//         }
        
//         if (ship.getVariant().getHullMods().contains("unstable_injector")) {
//             ship.getVariant().removeMod("unstable_injector");
//         }
        
//         if (ship.getVariant().getHullMods().contains("additional_berthing")) {
//             ship.getVariant().removeMod("additional_berthing");
//         }
        
//         if (ship.getVariant().getHullMods().contains("auxiliary_fuel_tanks")) {
//             ship.getVariant().removeMod("auxiliary_fuel_tanks");
//         }
        
//         if (ship.getVariant().getHullMods().contains("expanded_cargo_holds")) {
//             ship.getVariant().removeMod("expanded_cargo_holds");
//         }
        
//         if (ship.getVariant().getHullMods().contains("efficiency_overhaul")) {
//             ship.getVariant().removeMod("efficiency_overhaul");
//         }
        
//         if (ship.getVariant().getHullMods().contains("hiressensors")) {
//             ship.getVariant().removeMod("hiressensors");
//         }
        
//         if (ship.getVariant().getHullMods().contains("insulatedengine")) {
//             ship.getVariant().removeMod("insulatedengine");
//         }
        
//         if (ship.getVariant().getHullMods().contains("augmentedengines")) {
//             ship.getVariant().removeMod("augmentedengines");
//         }
        
//         if (ship.getVariant().getHullMods().contains("militarized_subsystems")) {
//             ship.getVariant().removeMod("militarized_subsystems");
//         }
        
//         if (ship.getVariant().getHullMods().contains("solar_shielding")) {
//             ship.getVariant().removeMod("solar_shielding");
//         }
        
//         if (ship.getVariant().getHullMods().contains("surveying_equipment")) {
//             ship.getVariant().removeMod("surveying_equipment");
//         }
        
//         if (ship.getVariant().getHullMods().contains("operations_center")) {
//             ship.getVariant().removeMod("operations_center");
//         }
        
//         if (ship.getVariant().getHullMods().contains("ecm")) {
//             ship.getVariant().removeMod("ecm");
//         }
        
//         if (ship.getVariant().getHullMods().contains("nav_relay")) {
//            ship.getVariant().removeMod("nav_relay");
//        }
        
//        if (ship.getVariant().getHullMods().contains("converted_hangar")) {
//            ship.getVariant().removeMod("converted_hangar");
//        }
    }
	
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "logistics, engine and command hull mods and improvised hangars";
	return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("xlu_");
    }
}
