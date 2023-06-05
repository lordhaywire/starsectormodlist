package data.scripts.campaign.econ.impl;

import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;
import com.fs.starfarer.api.util.Pair;
import data.scripts.campaign.econ.OCUA_Commodities;


public class ocua_pop_demands extends PopulationAndInfrastructure {

	@Override
	public void apply() {
		int size = market.getSize();
		
		int cookieThreshold = 4;
		
		demand(OCUA_Commodities.OCUA_COOKIES, size - cookieThreshold);
                
		Pair<String, Integer> deficit = getMaxDeficit(OCUA_Commodities.OCUA_COOKIES);
		if (deficit.two <= 0) {
			market.getUpkeepMult().modifyMult(getModId(0), 0.9f, "Oculian cookies demand met");
		} else {
			market.getUpkeepMult().modifyMult(getModId(0), 1f);
		}
		
	}
        
	@Override
	public void unapply() {
		
		market.getUpkeepMult().unmodify(getModId(0));
		
	}
	
}







