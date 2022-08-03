package scripts.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.AddBarEvent;
import static com.fs.starfarer.api.impl.campaign.rulecmd.salvage.AddBarEvent.getTempEvents;
import com.fs.starfarer.api.util.Misc;
import ids.Roider_Ids.Roider_Fitters;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_MemFlags;
import java.util.List;
import java.util.Map;
import scripts.campaign.econ.Roider_Dives;
import scripts.campaign.retrofit.*;

/**
 * Author: SafariJohn
 */
public class Roider_SWRetrofitAccess extends BaseCommandPlugin {
	private InteractionDialogAPI dialog;
    private MarketAPI market;
	private FactionAPI faction;

    public static final String EVENT_ID = "roider_swRetrofitBarEvent";

    public static final String STRAIGHT_ID = "roider_swRetrofitStraight";
    public static final String LEAVE_ID = "backToBar";

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;

		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;


        market = dialog.getInteractionTarget().getMarket();
        if (market == null) return false;
		faction = market.getFaction();

        switch (command) {
            case "addBarEvent": addBarEvent();
                break;
            case "retrofit": retrofit();
                break;
            case "shipworksFunctional": return shipworksFunctional();
        }

        return true;
    }

    private void retrofit() {
        MemoryAPI memory = market.getMemoryWithoutUpdate();

        // Get retrofit manager
        Roider_ShipworksRetrofitManager manager;
        if (memory.get(Roider_MemFlags.SW_RETROFITTER) != null) {
            manager = (Roider_ShipworksRetrofitManager) memory.get(Roider_MemFlags.SW_RETROFITTER);
        } else {
            manager = new Roider_ShipworksRetrofitManager(
                        Roider_Fitters.ALL,
                        market.getPrimaryEntity(), faction);

            market.getMemoryWithoutUpdate().set(Roider_MemFlags.SW_RETROFITTER, manager);
            Global.getSector().addScript(manager);
        }

        Roider_BaseRetrofitPlugin plugin;
        plugin = new Roider_ShipworksRetrofitPlugin(
                    dialog.getPlugin(),
                    manager,
                    dialog.getPlugin().getMemoryMap());

        dialog.setPlugin(plugin);
        plugin.init(dialog);
    }

    private boolean shipworksFunctional() {
        if (!market.hasIndustry(Roider_Industries.SHIPWORKS)) return false;

        return market.getMemoryWithoutUpdate().getBoolean(Roider_MemFlags.SHIPWORKS_FUNCTIONAL);
    }

    private void addBarEvent() {
        market = dialog.getInteractionTarget().getMarket();

        // Prompt
		String blurb = "You can access your local "
                    + "Rockpiper shipworks' services from here.";

        // Generate option text
        String option = "Access your Rockpiper shipworks retrofits";

        String optionId = STRAIGHT_ID;

		AddBarEvent.BarEventData data = new AddBarEvent.BarEventData(optionId, option, blurb);
		data.optionColor = Misc.getHighlightColor();

		AddBarEvent.TempBarEvents events = getTempEvents(market);
		events.events.put(optionId, data);
    }

    protected PersonAPI createPerson() {
        Roider_Dives unionHQ = (Roider_Dives) market.getIndustry(Roider_Industries.UNION_HQ);
        return unionHQ.getBaseCommander();
    }

}
