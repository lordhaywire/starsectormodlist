package data.scripts.campaign.econ.impl;

import com.fs.starfarer.api.impl.campaign.econ.impl.HeavyIndustry;
import data.scripts.campaign.econ.OCUA_industries;

public class OCUA_HI_Checker extends HeavyIndustry {
    //could really use a more compatible method of adapting industry upgrades.
    
    @Override
    public boolean isAvailableToBuild() {
        boolean hasStationaryMatrix = false;
        
        if (market.getPlanetEntity() != null && (market.hasIndustry(OCUA_industries.OCUA_ORBITAL_MATRIX))) {
            hasStationaryMatrix = true;
        }
        
        if (hasStationaryMatrix) {
            return false;
        } else if (!hasStationaryMatrix) {
            return true;
        }
        
        return false;
    }

    @Override
    public String getUnavailableReason() {
        return "Lanestate Battleyards already present";
    }

    @Override
    public boolean showWhenUnavailable() {
        return true;
    }
}
