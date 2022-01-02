package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.fs.starfarer.api.combat.BaseHullMod;
import data.scripts.util.MagicIncompatibleHullmods;
import static data.scripts.util.SCY_txt.txt;

public class SCY_lightArmor extends BaseHullMod {

    private final Map<ShipAPI.HullSize, Float> shipSpeed = new HashMap<>();
    {
        shipSpeed.put(ShipAPI.HullSize.DEFAULT, 15f);
        shipSpeed.put(ShipAPI.HullSize.CAPITAL_SHIP, 25f);
        shipSpeed.put(ShipAPI.HullSize.CRUISER, 20f);
        shipSpeed.put(ShipAPI.HullSize.DESTROYER, 15f);
        shipSpeed.put(ShipAPI.HullSize.FRIGATE, 10f);
    }
    private final Map<ShipAPI.HullSize, Float> shipEngines = new HashMap<>();
    {
        shipEngines.put(ShipAPI.HullSize.DEFAULT, 25f);
        shipEngines.put(ShipAPI.HullSize.CAPITAL_SHIP, 15f);
        shipEngines.put(ShipAPI.HullSize.CRUISER, 25f);
        shipEngines.put(ShipAPI.HullSize.DESTROYER, 35f);
        shipEngines.put(ShipAPI.HullSize.FRIGATE, 50f);    
    }
//    private static final Map<ShipAPI.HullSize, Float> shipPeak = new HashMap<>();
//    static {
//        shipPeak.put(ShipAPI.HullSize.DEFAULT, 20f);
//        shipPeak.put(ShipAPI.HullSize.CAPITAL_SHIP, 30f);
//        shipPeak.put(ShipAPI.HullSize.CRUISER, 25f);
//        shipPeak.put(ShipAPI.HullSize.DESTROYER, 20f);
//        shipPeak.put(ShipAPI.HullSize.FRIGATE, 15f);    
//    }
    
//    private float shipHull = 25f;
    private final float shipArmor = 50f;
    private final float shipCost = 10f;
    private final float shipFlux = 15f;
        
    private final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    {
        // These hullmods will automatically be removed
        BLOCKED_HULLMODS.add("heavyarmor");
        BLOCKED_HULLMODS.add("ii_armor_package");
        BLOCKED_HULLMODS.add("SCY_reactiveArmor");
        BLOCKED_HULLMODS.add("SKR_ancientArmor");
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
        stats.getArmorBonus().modifyMult(id,1-shipArmor*0.01f);
//        stats.getHullBonus().modifyMult(id,1-shipHull*0.01f);

        stats.getAcceleration().modifyMult(id, 1f + shipEngines.get(hullSize)*0.01f);
        stats.getDeceleration().modifyMult(id, 1f + shipEngines.get(hullSize)*0.01f);
        stats.getTurnAcceleration().modifyMult(id, 1f + shipEngines.get(hullSize)*0.01f);
        stats.getMaxTurnRate().modifyMult(id, 1f + shipEngines.get(hullSize)*0.01f);
        
        stats.getMaxSpeed().modifyFlat(id, shipSpeed.get(hullSize));
        
//        stats.getCRPerDeploymentPercent().modifyFlat(id, shipCost);
        stats.getSuppliesToRecover().modifyMult(id, 1+(shipCost*0.01f));
        
        stats.getFluxDissipation().modifyMult(id, 1-(shipFlux*0.01f));
        stats.getFluxCapacity().modifyMult(id, 1-(shipFlux*0.01f));
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){   
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {  
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "SCY_lightArmor");  
            }
        }
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "" 
                + (shipEngines.get(ShipAPI.HullSize.FRIGATE)).intValue() 
                + "/" 
                + (shipEngines.get(ShipAPI.HullSize.DESTROYER)).intValue() 
                + "/" 
                + (shipEngines.get(ShipAPI.HullSize.CRUISER)).intValue() 
                + "/" 
                + (shipEngines.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue();
        }

        if (index == 1) {
            return "" 
                + (shipSpeed.get(ShipAPI.HullSize.FRIGATE)).intValue()
                + "/"
                + (shipSpeed.get(ShipAPI.HullSize.DESTROYER)).intValue()
                + "/"
                + (shipSpeed.get(ShipAPI.HullSize.CRUISER)).intValue()
                + "/" 
                + (shipSpeed.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue(); 
        }
        
        if (index == 2) return "" + Math.round(shipArmor);
        if (index == 3) return "" + Math.round(shipFlux);
        if (index == 4) return "" + Math.round(shipCost);
        
//        if (index == 4) {
//            return "" 
//                + (shipPeak.get(ShipAPI.HullSize.FRIGATE)).intValue()
//                + "/"
//                + (shipPeak.get(ShipAPI.HullSize.DESTROYER)).intValue()
//                + "/"
//                + (shipPeak.get(ShipAPI.HullSize.CRUISER)).intValue()
//                + "/"
//                + (shipPeak.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue();
//        }
        
        if (index == 5) return Global.getSettings().getHullModSpec("heavyarmor").getDisplayName();
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
