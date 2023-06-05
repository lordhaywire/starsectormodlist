package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

public class ocua_cookies extends BaseMarketConditionPlugin {
    
    @Override
    public void apply(String id) {
        super.apply(id);
        
        market.getCommodityData(id).getCommodityMarketData();
        market.getCommodityData(id).getUtilityOnMarket();
        market.getCommodityData(id).getDemand();
        market.getCommodityData(id).getExportIncome();
    }
    
    @Override
    public void unapply(String id) {
        super.unapply(id);
    }
}