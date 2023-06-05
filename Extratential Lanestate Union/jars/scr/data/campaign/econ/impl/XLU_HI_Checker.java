package data.campaign.econ.impl;

import com.fs.starfarer.api.impl.campaign.econ.impl.HeavyIndustry;
import data.campaign.econ.XLU_industries;

public class XLU_HI_Checker extends HeavyIndustry {
    //could really use a more compatible method of adapting industry upgrades.
    
    @Override
    public boolean isAvailableToBuild() {
        boolean hasBattleyards = false;
        
        if (market.getPlanetEntity() != null && (market.hasIndustry(XLU_industries.XLU_YARDS))) {
            hasBattleyards = true;
        }
        
        if (hasBattleyards) {
            return false;
        } else if (!hasBattleyards) {
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
