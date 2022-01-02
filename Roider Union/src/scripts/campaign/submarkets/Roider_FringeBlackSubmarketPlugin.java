package scripts.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.submarkets.BlackMarketPlugin;
import com.thoughtworks.xstream.XStream;
import java.util.Random;

/**
 * Author: SafariJohn
 */
public class Roider_FringeBlackSubmarketPlugin extends BlackMarketPlugin {
    public static void aliasAttributes(XStream x) {
    }

	@Override
	public int getStockpileLimit(CommodityOnMarketAPI com) {
		int demand = com.getMaxDemand();
		int available = com.getAvailable();

		float limit = BaseIndustry.getSizeMult(available) - BaseIndustry.getSizeMult(Math.max(0, demand - 2));
		limit *= com.getCommodity().getEconUnit();

		//limit *= com.getMarket().getStockpileMult().getModifiedValue();

		Random random = new Random(market.getId().hashCode() + submarket.getSpecId().hashCode() + Global.getSector().getClock().getMonth() * 170000);
		limit *= 0.9f + 0.2f * random.nextFloat();

		float sm = market.getStabilityValue() / 10f;
		limit *= 0.1f * sm;

		if (limit < 0) limit = 0;

		return (int) limit;
	}

	@Override
	public PlayerEconomyImpactMode getPlayerEconomyImpactMode() {
		return PlayerEconomyImpactMode.NONE;
	}
}
