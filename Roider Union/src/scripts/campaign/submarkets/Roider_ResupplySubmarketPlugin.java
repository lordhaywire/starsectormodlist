package scripts.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.SubmarketPlugin.TransferAction;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import java.util.Random;

/**
 * Author: SafariJohn
 */
public class Roider_ResupplySubmarketPlugin extends BaseSubmarketPlugin {
    public static void aliasAttributes(XStream x) {
    }

    @Override
	public void init(SubmarketAPI submarket) {
		super.init(submarket);
	}

    @Override
    public void updateCargoPrePlayerInteraction() {
		float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
		addAndRemoveStockpiledResources(seconds, false, true, true);
		sinceLastCargoUpdate = 0f;

		getCargo().sort();
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
		limit *= 0.2f * sm;

		if (limit < 0) limit = 0;

		return (int) limit;
	}

    @Override
    public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
        return com.getId().equals(Commodities.SUPPLIES)
                    || com.getId().equals(Commodities.FUEL)
                    || com.getId().equals(Commodities.CREW);
    }

    @Override
	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        return !(commodityId.equals(Commodities.SUPPLIES)
                    || commodityId.equals(Commodities.FUEL)
                    || commodityId.equals(Commodities.CREW));
	}

    @Override
	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		if (stack.isCommodityStack()) {
			return isIllegalOnSubmarket((String) stack.getData(), action);
		} else {
            return true;
        }
	}

    @Override
	public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
		return "Can only trade crew, fuel, and supplies here";
	}


    @Override
	public Highlights getIllegalTransferTextHighlights(CargoStackAPI stack, TransferAction action) {
        Highlights h = new Highlights();
        h.append("Can only trade crew, fuel, and supplies here", Misc.getNegativeHighlightColor());
		return h;
	}

    @Override
	public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        return true;
	}

    @Override
	public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
		return "No ship trades";
	}

    @Override
	public Highlights getIllegalTransferTextHighlights(FleetMemberAPI member, TransferAction action) {
        Highlights h = new Highlights();
        h.append("No ship trades", Misc.getNegativeHighlightColor());
		return h;
	}

    @Override
	public boolean isEnabled(CoreUIAPI ui) {
        return true;
	}
}
