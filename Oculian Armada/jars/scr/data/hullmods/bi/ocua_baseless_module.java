package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import data.scripts.everyframe.OCUA_BlockedHullmodDisplayScript;
import java.util.HashSet;
import java.util.Set;

public class ocua_baseless_module extends BaseHullMod {

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        //BLOCKED_HULLMODS.add("targetingunit");
        //BLOCKED_HULLMODS.add("dedicated_targeting_core");
        BLOCKED_HULLMODS.add("auxiliarythrusters");
        //BLOCKED_HULLMODS.add("safetyoverrides");
        BLOCKED_HULLMODS.add("unstable_injector");
        BLOCKED_HULLMODS.add("additional_berthing");
        BLOCKED_HULLMODS.add("auxiliary_fuel_tanks");
        BLOCKED_HULLMODS.add("expanded_cargo_holds");
        BLOCKED_HULLMODS.add("efficiency_overhaul");
        BLOCKED_HULLMODS.add("hiressensors");
        BLOCKED_HULLMODS.add("insulatedengine");
        BLOCKED_HULLMODS.add("augmentedengines");
        BLOCKED_HULLMODS.add("militarized_subsystems");
        BLOCKED_HULLMODS.add("solar_shielding");
        BLOCKED_HULLMODS.add("surveying_equipment");
        BLOCKED_HULLMODS.add("operations_center");
        BLOCKED_HULLMODS.add("ecm");
        BLOCKED_HULLMODS.add("nav_relay");
        //BLOCKED_HULLMODS.add("converted_hangar");
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

    }

        @Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            for (String tmp : BLOCKED_HULLMODS) {
                if (ship.getVariant().getHullMods().contains(tmp)) {
                    ship.getVariant().removeMod(tmp);
                    OCUA_BlockedHullmodDisplayScript.showBlocked(ship);
                }
            }
	}
	
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
            if (index == 0) return "standard logistics, engine and command hullmods";
            
            return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
