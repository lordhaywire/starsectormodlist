package scripts.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.AddBarEvent;
import static com.fs.starfarer.api.impl.campaign.rulecmd.salvage.AddBarEvent.getTempEvents;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.util.Misc;
import ids.Roider_Ids;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_MemFlags;
import scripts.campaign.retrofit.Roider_UnionHQRetrofitPlugin;
import scripts.campaign.retrofit.Roider_UnionHQRetrofitManager;
import java.util.List;
import java.util.Map;
import scripts.campaign.econ.Roider_Dives;
import scripts.campaign.retrofit.Roider_BaseRetrofitPlugin;
import scripts.campaign.retrofit.Roider_ShipworksRetrofitManager;

/**
 * Author: SafariJohn
 */
public class Roider_RetrofitAccess extends BaseCommandPlugin {
	private InteractionDialogAPI dialog;
	private SectorEntityToken entity;
    private MarketAPI market;
	private PersonAPI person;
	private FactionAPI faction;

    public static final String EVENT_ID = "roider_retrofitBarEvent";

//    public static final float RETROFIT_FEE = 20000;
    public static final float RETROFIT_FEE = 0;
    public static final float STORAGE_FEE = 5000;

    public static final String REFUSE_ID = "roider_retrofitRefuse";
    public static final String DESCRIBE_ID = "roider_retrofitDescribe";
    public static final String PAY_ID = "roider_retrofitPay";
    public static final String STRAIGHT_ID = "roider_retrofitStraight";
    public static final String STORAGE_ID = "roider_retrofitStorage";
    public static final String LEAVE_ID = "backToBar";

    private String getHisOrHer() {
        if (person.isFemale()) return "her";
        else return "his";
    }

    private String getHeOrShe() {
        if (person.isFemale()) return "she";
        else return "he";
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
//		this.memoryMap = memoryMap;

		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;

//		memory = getEntityMemory(memoryMap);


        market = dialog.getInteractionTarget().getMarket();
        if (market == null) return false;
		entity = market.getPrimaryEntity();
		person = createPerson();
        if (person == null) return false;
		faction = person.getFaction();

        switch (command) {
            case "addBarEvent": addBarEvent();
                break;
            case "describe": describeRetrofitting();
                break;
            case "refuse": refuseService();
                break;
            case "payFee": payFee();
                break;
            case "retrofit": retrofit();
                break;
            case "roiderHQFunctional": return roiderHQFunctional();
        }

        return true;
    }

    private void retrofit() {
        MemoryAPI memory = market.getMemoryWithoutUpdate();

        // Get retrofit manager
        Roider_UnionHQRetrofitManager manager = null;
        if (memory.get(Roider_MemFlags.RETROFITTER) != null) {
            Object test = memory.get(Roider_MemFlags.RETROFITTER);

            // Heal erroneous assignment
            if (test instanceof Roider_ShipworksRetrofitManager) {
                if (memory.get(Roider_MemFlags.SW_RETROFITTER) == null) {
                    memory.set(Roider_MemFlags.SW_RETROFITTER, test);
                }
            }

            if (test instanceof Roider_UnionHQRetrofitManager) {
                manager = (Roider_UnionHQRetrofitManager) test;
            }
        }

        if (manager == null) {
            if (market.hasIndustry(Industries.HEAVYINDUSTRY)
                        || market.hasIndustry(Industries.ORBITALWORKS)
                        || entity.getMarket().hasIndustry(Roider_Industries.SHIPWORKS)) {
                manager = new Roider_UnionHQRetrofitManager(
                            Roider_Ids.Roider_Fitters.FULL,
                            market.getPrimaryEntity(),
                            getRoiders());
            } else {
                manager = new Roider_UnionHQRetrofitManager(
                            Roider_Ids.Roider_Fitters.LIGHT,
                            market.getPrimaryEntity(),
                            getRoiders());
            }

            memory.set(Roider_MemFlags.RETROFITTER, manager);
            Global.getSector().addScript(manager);
        }

        Roider_BaseRetrofitPlugin plugin;
        plugin = new Roider_UnionHQRetrofitPlugin(
                    dialog.getPlugin(),
                    manager,
                    dialog.getPlugin().getMemoryMap());

        dialog.setPlugin(plugin);
        plugin.init(dialog);
    }

    private boolean personWillRetrofit() {
        // Reputation check
        return faction.isAtWorst(Factions.PLAYER, RepLevel.NEUTRAL);
    }

    private boolean roiderHQFunctional() {
        if (!market.hasIndustry(Roider_Ids.Roider_Industries.UNION_HQ)) return false;

        return market.getMemoryWithoutUpdate().getBoolean(Roider_MemFlags.UNION_HQ_FUNCTIONAL);
    }

    private void addBarEvent() {
        // Calculate access cost
        float fee = 0;

        market = dialog.getInteractionTarget().getMarket();

        if (isFeePaid() && isStoragePaid(market)) fee = 0;
        else {
            if (!isFeePaid()) fee += RETROFIT_FEE;
            if (!isStoragePaid(market)) fee += STORAGE_FEE;
        }

        // Prompt
		String blurb = "You spy the " + person.getRank() + " in charge"
                    + " of the local Roider Union HQ examining "
                    + getHisOrHer() + " TriPad.";

        // Generate option text
        String option = "Talk retrofits with " + person.getRank()
                    + " " + person.getName().getLast();

        if (isFirstTime() && !isFeePaid()) option = "See what the " + person.getRank() + " is doing here";
        if (isFeePaid() && !isStoragePaid(market)) option = "Pay"
                    + " for storage and talk retrofits ("
                    + Misc.getDGSCredits(fee) + " credits)";

        if (!isFirstTime() && !personWillRetrofit()) {
            option = "Talk with " + person.getRank()
                    + " " + person.getName().getLast();
        }

        String optionId = STRAIGHT_ID;

        if (!personWillRetrofit()) optionId = REFUSE_ID;
        else if (!isFeePaid()) optionId = DESCRIBE_ID;
        else if (!isStoragePaid(market)) optionId = STORAGE_ID;

		AddBarEvent.BarEventData data = new AddBarEvent.BarEventData(optionId, option, blurb);
		data.optionColor = getRoiders().getColor();

		AddBarEvent.TempBarEvents events = getTempEvents(market);
		events.events.put(optionId, data);
    }

    private FactionAPI getRoiders() {
        return Global.getSector().getFaction(Roider_Factions.ROIDER_UNION);
    }

    private void describeRetrofitting() {
		dialog.getInteractionTarget().setActivePerson(person);
		dialog.getVisualPanel().showPersonInfo(person, false, true);

        TextPanelAPI text = dialog.getTextPanel();

        if (isFirstTime()) {
            person.getMemoryWithoutUpdate().set(Roider_MemFlags.TALK_RETROFITS, true);
            text.addPara("\"You here to talk retrofits?\" " + person.getRank() + " " + person.getNameString() + " asks.");
        } else {
            text.addPara("\"Back to talk about retrofits?\" " + person.getRank() + " " + person.getNameString() + " asks.");
        }

        String desc = "Before you can answer, " + getHeOrShe()
                    + " continues, \"We have the facilities here"
                    + " to convert civilian ships into pristine warships";
        if (market.hasIndustry(Industries.HEAVYINDUSTRY)
                    || market.hasIndustry(Industries.ORBITALWORKS)
                    || market.hasIndustry(Roider_Industries.SHIPWORKS)) {
            desc += ", from frigates to capital ships. The whole shebang."
                        + " Other places can only do the small stuff.";
        } else {
            desc += ". Can only do small stuff here, though. Your"
                        + " eyes'll about pop out if you ever see"
                        + " what we Roiders can do with a proper shipyard.";
        }

        text.addPara(desc);

        text.addPara("\"Most stuff we can get right back to you,"
                    + " but if something takes longer we'll have"
                    + " to put it into storage while you're gone.");

        float fee = getFee();

        if (!isFeePaid() && !isStoragePaid(market)) {
//            text.addPara("\"Anyways, it's " + Misc.getDGSCredits(RETROFIT_FEE)
//                        + " credits for access and " + Misc.getDGSCredits(STORAGE_FEE)
//                        + " credits to store stuff here. You interested?\"");
//            text.highlightInLastPara(Misc.getHighlightColor(), Misc.getDGSCredits(RETROFIT_FEE), Misc.getDGSCredits(STORAGE_FEE));
            text.addPara("\"Anyways, it's " + Misc.getDGSCredits(STORAGE_FEE)
                        + " credits to store stuff here. You interested?\"");
            text.highlightInLastPara(Misc.getHighlightColor(), Misc.getDGSCredits(STORAGE_FEE));
        } else {
            text.addPara("\"You interested?\"");
//            text.highlightFirstInLastPara(Misc.getDGSCredits(STORAGE_FEE), Misc.getHighlightColor());
        }


        OptionPanelAPI options = dialog.getOptionPanel();
        options.clearOptions();
        if (getFee() > 0) options.addOption("Pay " + Misc.getDGSCredits(fee) + " credits", PAY_ID);
        else options.addOption("Talk retrofits", PAY_ID);
        options.addOption("Decline and excuse yourself", LEAVE_ID);

        if (Global.getSector().getPlayerFleet().getCargo().getCredits().get() < fee) {
            options.setEnabled(PAY_ID, false);
            options.setTooltip(PAY_ID, "Not enough credits!");
        }
    }

    private void refuseService() {
		dialog.getInteractionTarget().setActivePerson(person);
		dialog.getVisualPanel().showPersonInfo(person, false, true);

        TextPanelAPI text = dialog.getTextPanel();

        if (!isFirstTime()) {
            text.addPara("\"You're on the No Service list. I have nothing to discuss with you,\" " + person.getRank() + " " + person.getNameString() + " says.");
            return;
        }

        person.getMemoryWithoutUpdate().set(Roider_MemFlags.TALK_RETROFITS, true);

        text.addPara("\"You here to talk retrofits?\" " + person.getRank() + " " + person.getNameString() + " asks.");

        text.addPara("Before you can answer, " + getHeOrShe()
                    + " continues, \"We have the facilities here"
                    + " to convert civilian ships into pristine warships.\"");

        text.addPara(Misc.ucFirst(getHisOrHer()) + " TriPad buzzes angrily and " + getHeOrShe() + " frowns when " + getHeOrShe() + " glances at it.");

        text.addPara("\"You're on the No Service list, looks like, so we have nothing further to discuss,\" " + getHeOrShe() + " says with finality.");
    }

    private void payFee() {
        float fee = getFee();
        if (fee > 0) {
            Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(fee);

        }

        getRoiders().getMemoryWithoutUpdate().set(Roider_MemFlags.FEE_PAID, true);
        ((StoragePlugin) Misc.getStorage(market)).setPlayerPaidToUnlock(true);

        retrofit();
    }

    private float getFee() {
        float fee = 0;
        if (isFeePaid() && isStoragePaid(market)) fee = 0;
        else {
            if (!isFeePaid()) fee += RETROFIT_FEE;
            if (!isStoragePaid(market)) fee += STORAGE_FEE;
        }

        return fee;
    }

    private void payStorage() {
        // Confirmation
//        boolean pay = Global.getSector().getCampaignUI().showConfirmDialog(
//                    "Are you sure you want to pay " + Misc.getDGSCredits(STORAGE_FEE) + "credits?",
//                    "Confirm", "Cancel", null, null);

        OptionPanelAPI options = dialog.getOptionPanel();
        options.clearOptions();
        options.addOption("Pay " + Misc.getDGSCredits(STORAGE_FEE) + " credits", PAY_ID);
        options.addOption("Nevermind", LEAVE_ID);

        if (Global.getSector().getPlayerFleet().getCargo().getCredits().get() < STORAGE_FEE) {
            options.setEnabled(PAY_ID, false);
            options.setTooltip(PAY_ID, "Not enough credits!");
        }
    }

    protected PersonAPI createPerson() {
        Roider_Dives unionHQ = (Roider_Dives) market.getIndustry(Roider_Industries.UNION_HQ);
        if (unionHQ == null) return null;
        return unionHQ.getBaseCommander();
    }

    private boolean isFirstTime() {
        return !person.getMemoryWithoutUpdate().getBoolean(Roider_MemFlags.TALK_RETROFITS);
    }

    private boolean isFeePaid() {
        return getRoiders().getMemoryWithoutUpdate().getBoolean(Roider_MemFlags.FEE_PAID);
    }

    private boolean isStoragePaid(MarketAPI market) {
        return Misc.playerHasStorageAccess(market);
    }

}
