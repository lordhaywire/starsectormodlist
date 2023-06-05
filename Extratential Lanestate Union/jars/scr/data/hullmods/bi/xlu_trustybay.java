package data.hullmods.bi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import data.scripts.plugins.xlu_BlockedHullmodDisplayScript;

import java.util.HashSet;
import java.util.Set;

public class xlu_trustybay extends BaseHullMod {
    
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("TSC_converted_hangar");
    }
    
    public static final float COST_REDUCTION  = 10;
	
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION);
	stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION);
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getLargeHardpointCover().setAlphaMult(1f);
        
        int emptySlots = 1;
        for (String slotID : ship.getVariant().getFittedWeaponSlots()) {
            if (ship.getVariant().getSlot(slotID).getSlotSize().equals(WeaponAPI.WeaponSize.LARGE)) {
                emptySlots--;
            }
        }
        
        if (emptySlots == 0) {
            if (ship.getVariant().getWing(0) != null) {
                if (Global.getSector() != null) {
                    if (Global.getSector().getPlayerFleet() != null) {
                        Global.getSector().getPlayerFleet().getCargo().addFighters(ship.getVariant().getWingId(0), 1);
                    }
                }
            ship.getVariant().setWingId(0 ,null);
            }
            ship.getVariant().removeMod("xlu_trustybay_slot_1"); //Hullmod name and script name are different, derp
        }
        
        if (emptySlots == 1) {
            ship.getVariant().addMod("xlu_trustybay_slot_1");
        }
        
        super.applyEffectsAfterShipCreation(ship, id);

        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                xlu_BlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
    }
    
    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if(index == 0) return "allows a Hangar bay in trade of the Large mount";
	if(index == 1) return "20 units";
	if(index == 2) return "" + (int) COST_REDUCTION + "";
	if(index == 3) return "prevents Converted Hangar";
        
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("xlu_");
    }
}
