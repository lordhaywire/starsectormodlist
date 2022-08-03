package scripts.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import ids.Roider_MemFlags;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import scripts.campaign.retrofit.Roider_ArgosRetrofitManager;
import scripts.campaign.retrofit.Roider_ArgosRetrofitPlugin;
import scripts.campaign.retrofit.Roider_BaseRetrofitPlugin;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper.RetrofitData;

/**
 * Argos Paid Retrofitting
 * Author: SafariJohn
 */
public class Roider_APRAccess extends BaseCommandPlugin {
    public static final String HAVE_CATALOG = "$roider_aprHaveCatalog";

	private MemoryAPI memory;
	private InteractionDialogAPI dialog;
	private SectorEntityToken entity;
	private PersonAPI person;
	private FactionAPI faction;

    protected static final int COLUMNS = 7;


    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;

		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;

		memory = getEntityMemory(memoryMap);


		entity = dialog.getInteractionTarget();
		person = entity.getActivePerson();
        if (person == null) return false;
		faction = person.getFaction();

        switch (command) {
            case "retrofit": retrofit();
                break;
            case "catalog": showCatalogInit();
                break;
            case "catalogCont": showCatalogCont();
                break;
        }

        return true;
    }

    private void retrofit() {
        // Get retrofit manager
        Roider_ArgosRetrofitManager manager;
        if (memory.get(Roider_MemFlags.RETROFITTER) != null) {
            manager = (Roider_ArgosRetrofitManager) memory.get(Roider_MemFlags.RETROFITTER);
        } else {
            manager = new Roider_ArgosRetrofitManager(
                        entity,
                        faction,
                        (List) memory.get(Roider_MemFlags.APR_OFFERINGS));

            memory.set(Roider_MemFlags.RETROFITTER, manager);
            Global.getSector().addScript(manager);
        }

        Roider_BaseRetrofitPlugin plugin;
        plugin = new Roider_ArgosRetrofitPlugin(
                    dialog.getPlugin(),
                    manager,
                    dialog.getPlugin().getMemoryMap());

        dialog.setPlugin(plugin);
        plugin.init(dialog);
    }

    private void showCatalogInit() {
        if (memory.getBoolean(HAVE_CATALOG)) {
            showCatalogCont();
            return;
        }

        memory.set(HAVE_CATALOG, true);

        dialog.getTextPanel().addPara(Misc.ucFirst(person.getRank())
                    + " " + person.getNameString()
                    + " transmits a retrofits catalog to you.");

        showCatalog();
    }

    private void showCatalogCont() {
        dialog.getTextPanel().addPara("You still have the retrofits "
                    + "catalog that " + Misc.ucFirst(person.getRank())
                    + " " + person.getNameString()
                    + " gave you.");

        showCatalog();
    }

    private void showCatalog() {
        // Get retrofit manager
        Roider_ArgosRetrofitManager manager;
        if (memory.get(Roider_MemFlags.RETROFITTER) != null) {
            manager = (Roider_ArgosRetrofitManager) memory.get(Roider_MemFlags.RETROFITTER);
        } else {
            manager = new Roider_ArgosRetrofitManager(
                        entity,
                        faction,
                        (List) memory.get(Roider_MemFlags.APR_OFFERINGS));

            memory.set(Roider_MemFlags.RETROFITTER, manager);
            Global.getSector().addScript(manager);
        }

        CampaignFleetAPI retrofitHulls = FleetFactoryV3.createEmptyFleet(
                    manager.getFaction().getId(),
                    FleetTypes.MERC_PRIVATEER, null);

        List<String> included = new ArrayList<>();
        for (RetrofitData data : manager.getRetrofits()) {
            if (included.contains(data.targetHull)) continue;

            retrofitHulls.getFleetData().addFleetMember(data.targetHull + "_Hull");
            included.add(data.targetHull);
        }

        List<FleetMemberAPI> retrofitMembers = retrofitHulls.getFleetData().getMembersListCopy();

        // Match names
        for (FleetMemberAPI m : retrofitMembers) {
            m.setShipName("Retrofit to");
            m.getRepairTracker().setCR(0.7f); // Looks cleaner
        }

        List<FleetMemberAPI> members = new ArrayList<>();
        members.addAll(retrofitMembers);

        TooltipMakerAPI targetTooltip = dialog.getTextPanel().beginTooltip();
        targetTooltip.addTitle("Available Retrofits");
        int rows = (retrofitMembers.size() / COLUMNS) + 1;
        float iconSize = dialog.getTextWidth() / COLUMNS;

        float pad = 0f; // 10f
        Color color = manager.getFaction().getBaseUIColor();
        targetTooltip.addShipList(COLUMNS, rows, iconSize, color, members, pad);
        dialog.getTextPanel().addTooltip();
    }

}
