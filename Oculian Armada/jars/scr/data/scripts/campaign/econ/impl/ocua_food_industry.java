package data.scripts.campaign.econ.impl;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import data.scripts.campaign.econ.OCUA_Commodities;


public class ocua_food_industry extends BaseIndustry {
        public boolean hasPort = false;

	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		
		
		demand(Commodities.HEAVY_MACHINERY, size - 2); // have to keep it low since it can be circular
		demand(Commodities.VOLATILES, size - 2);
		demand(Commodities.ORGANICS, size - 1);
		
		supply(Commodities.FOOD, size - 1);
		supply(Commodities.SUPPLIES, size - 2);
		if (!market.isIllegal(OCUA_Commodities.OCUA_COOKIES)) {
			supply(OCUA_Commodities.OCUA_COOKIES, size - 4);
		} else {
			supply(OCUA_Commodities.OCUA_COOKIES, 0);
		}
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.HEAVY_MACHINERY, Commodities.ORGANICS, Commodities.VOLATILES);
		applyDeficitToProduction(1, deficit,    Commodities.FOOD,
                                                        Commodities.SUPPLIES,
                                                        OCUA_Commodities.OCUA_COOKIES);
		
		if (!isFunctional()) {
			supply.clear();
		}
	}

	@Override
	public void unapply() {
		super.unapply();
	}

        @Override
        protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
            return mode != IndustryTooltipMode.NORMAL || isFunctional();
        }
	
	@Override
	public void createTooltip(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltip(mode, tooltip, expanded);
		
	}

        @Override
        public boolean isDemandLegal(CommodityOnMarketAPI com) {
            return true;
        }

        @Override
        public boolean isSupplyLegal(CommodityOnMarketAPI com) {
            return true;
        }
        
	
	public float getPatherInterest() {
		return -2f + super.getPatherInterest();
	}

}
