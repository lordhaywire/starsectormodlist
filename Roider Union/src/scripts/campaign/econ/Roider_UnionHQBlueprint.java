package scripts.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.IndustryBlueprintItemPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_Industries;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: SafariJohn
 */
public class Roider_UnionHQBlueprint extends IndustryBlueprintItemPlugin {

    public static void aliasAttributes(XStream x) {
    }
    
	@Override
	public void init(CargoStackAPI stack) {
		this.stack = stack;
        stack.getSpecialDataIfSpecial().setData(Roider_Industries.UNION_HQ);
		industry = Global.getSettings().getIndustrySpec(Roider_Industries.UNION_HQ);
	}

    @Override
    public int getPrice(MarketAPI market, SubmarketAPI submarket) {
		if (industry != null) {
            return 200000;
//            return (int) (250000f / 1.2f) + 1; // bullshit magic numbers to get 350k

//			float base = super.getPrice(market, submarket);
//			return (int) ((base + industry.getCost() * getItemPriceMult()) / 4.5);
		}
		return super.getPrice(market, submarket);
    }


	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
		createTooltip(tooltip, expanded, transferHandler, stackSource, true);

		float pad = 3f;
		float opad = 10f;
		float small = 5f;
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color b = Misc.getButtonTextColor();
		b = Misc.getPositiveHighlightColor();

		String weaponId = stack.getSpecialDataIfSpecial().getData();
		boolean known = Global.getSector().getPlayerFaction().knowsWeapon(weaponId);

		List<String> weapons = new ArrayList<String>();
		weapons.add(weaponId);

		tooltip.addPara(industry.getDesc(), opad);

        String desc = "Built by upgrading a Roider Dives!";
        Color c = Global.getSector().getFaction(Roider_Factions.ROIDER_UNION).getBaseUIColor();

		tooltip.addPara(desc, c, opad);

		addCostLabel(tooltip, opad, transferHandler, stackSource);

		if (known) {
			tooltip.addPara("Already known", g, opad);
		} else {
			tooltip.addPara("Right-click to learn", b, opad);
		}
	}
}
