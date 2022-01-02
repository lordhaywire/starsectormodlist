package scripts.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.IndustryBlueprintItemPlugin;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Industries;

/**
 * Author: SafariJohn
 */
public class Roider_ShipworksBlueprint extends IndustryBlueprintItemPlugin {

    public static void aliasAttributes(XStream x) {
    }
    
	@Override
	public void init(CargoStackAPI stack) {
		this.stack = stack;
        stack.getSpecialDataIfSpecial().setData(Roider_Industries.SHIPWORKS);
		industry = Global.getSettings().getIndustrySpec(Roider_Industries.SHIPWORKS);
	}

    @Override
    public int getPrice(MarketAPI market, SubmarketAPI submarket) {
		if (industry != null) {
            return 140000;
		}
		return super.getPrice(market, submarket);
    }
}
