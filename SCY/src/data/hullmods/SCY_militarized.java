package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import data.scripts.util.MagicIncompatibleHullmods;
import java.util.HashSet;
import java.util.Set;

public class SCY_militarized extends BaseHullMod {
    
    private final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    {
        // These hullmods will automatically be removed
        BLOCKED_HULLMODS.add("assault_package");
        BLOCKED_HULLMODS.add("escort_package");
        BLOCKED_HULLMODS.add("militarized_subsystems");
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSensorStrength().unmodify(HullMods.CIVGRADE);
        stats.getSensorProfile().unmodify(HullMods.CIVGRADE);
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {   
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "SCY_militarized");
            }
        }
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return null;
    }
}
