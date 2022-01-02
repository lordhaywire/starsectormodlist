package scripts.campaign.intel.bar;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_MemFlags;
import java.util.Map;
import scripts.campaign.econ.Roider_Dives;
import scripts.campaign.retrofit.Roider_BaseRetrofitPlugin;
import scripts.campaign.retrofit.Roider_UnionHQRetrofitManager;
import scripts.campaign.retrofit.Roider_UnionHQRetrofitPlugin;

/**
 * Author: SafariJohn
 */
public class Roider_RetrofitBarEvent extends BaseBarEventWithPerson {
    public static void aliasAttributes(XStream x) {
//        x.aliasAttribute(Roider_RetrofitBarEvent.class, "roiderFaction", "f");
    }

    public static class RetrofitBarEventCreator implements BarEventManager.GenericBarEventCreator {
        private final PortsideBarEvent event;

        public RetrofitBarEventCreator(PortsideBarEvent event) {
            this.event = event;
        }

        @Override
        public PortsideBarEvent createBarEvent() {
            return event;
        }

        @Override
        public float getBarEventFrequencyWeight() {
            return Short.MAX_VALUE;
        }

        @Override
        public float getBarEventActiveDuration() {
            return Short.MAX_VALUE;
        }

        @Override
        public float getBarEventTimeoutDuration() {
            return 0f;
        }

        @Override
        public float getBarEventAcceptedTimeoutDuration() {
            return 0f;
        }

        @Override
        public boolean isPriority() {
            return true;
        }

        @Override
        public String getBarEventId() {
            return EVENT_ID;
        }

        @Override
        public boolean wasAutoAdded() {
            return false;
        }

    }

    public static final String EVENT_ID = "roider_retrofitBarEvent";

    public static final float RETROFIT_FEE = 20000;
    public static final float STORAGE_FEE = 5000;

    private enum OptionId {
        DESCRIBE,
        PAY,
        STRAIGHT,
        STORAGE,
        LEAVE
    }

    public Roider_RetrofitBarEvent() {
    }

    @Override
    public String getBarEventId() {
        return EVENT_ID;
    }

    @Override
    protected PersonAPI createPerson() {
        Roider_Dives unionHQ = (Roider_Dives) market.getIndustry(Roider_Industries.UNION_HQ);
        return unionHQ.getBaseCommander();
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);

		done = false;

        OptionId option = OptionId.STRAIGHT;

        if (!isFeePaid()) option = OptionId.DESCRIBE;
        else if (!isStoragePaid(market)) option = OptionId.STORAGE;

		optionSelected(null, option);
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        // Calculate access cost
        float fee = 0;

        market = dialog.getInteractionTarget().getMarket();
        person = createPerson(); // Get person for this location

        if (isFeePaid() && isStoragePaid(market)) fee = 0;
        else {
            if (!isFeePaid()) fee += RETROFIT_FEE;
            if (!isStoragePaid(market)) fee += STORAGE_FEE;
        }

        // Prompt
		TextPanelAPI text = dialog.getTextPanel();
		text.addPara("You spy the " + person.getRank() + " in charge"
                    + " of the local Roider Union HQ examining "
                    + getHisOrHer() + " TriPad.");

        // Generate option text
        String option = "Talk retrofits with " + person.getRank()
                    + " " + person.getName().getLast();

        if (isFirstTime() && !isFeePaid()) option = "See what the " + person.getRank() + " is doing here";
        if (isFeePaid() && !isStoragePaid(market)) option = "Pay"
                    + " for storage and talk retrofits ("
                    + Misc.getDGSCredits(fee) + " credits)";

//        if (fee > 0) option += ". Access fee: " + Misc.getDGSCredits(fee);


		dialog.getOptionPanel().addOption(option, this, getRoiders().getColor(), null);

        // If can't pay storage fee
//        if (Global.getSector().getPlayerFleet().getCargo().getCredits().get() < fee
//                    && isFeePaid() && !isStoragePaid(market)) {
//            dialog.getOptionPanel().setEnabled(this, false);
//            dialog.getOptionPanel().setTooltip(this, "Not enough credits!");
//        }
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

    @Override
    public void optionSelected(String optionText, Object optionData) {
        dialog.getInteractionTarget().setActivePerson(null);

        if (!(optionData instanceof OptionId)) {
            done = true;
            return;
        }

        OptionId option = (OptionId) optionData;

        switch (option) {
            case DESCRIBE: describeRetrofitting(); break;
            case PAY: payFee(); break;
            case STRAIGHT: showRetrofitting(); break;
            case STORAGE: payStorage(); break;
            default: done = true;
        }
    }

    private void showRetrofitting() {
        MemoryAPI memory = market.getMemoryWithoutUpdate();

        // Get retrofit manager
        Roider_UnionHQRetrofitManager manager;
        if (memory.get(Roider_MemFlags.RETROFITTER) != null) {
            manager = (Roider_UnionHQRetrofitManager) memory.get(Roider_MemFlags.RETROFITTER);
        } else {
            if (market.hasIndustry(Industries.HEAVYINDUSTRY)
                        || market.hasIndustry(Industries.ORBITALWORKS)
                        || market.hasIndustry(Roider_Industries.SHIPWORKS)) {
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

            market.getMemoryWithoutUpdate().set(Roider_MemFlags.RETROFITTER, manager);
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

    private void describeRetrofitting() {
        // Visuals
        dialog.getInteractionTarget().setActivePerson(person);
        dialog.getVisualPanel().showPersonInfo(person);

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
            text.addPara("\"Anyways, it's " + Misc.getDGSCredits(RETROFIT_FEE)
                        + " credits for access and " + Misc.getDGSCredits(STORAGE_FEE)
                        + " credits to store stuff here. You interested?\"");
            text.highlightInLastPara(Misc.getHighlightColor(), Misc.getDGSCredits(RETROFIT_FEE), Misc.getDGSCredits(STORAGE_FEE));
        } else {
            text.addPara("\"Anyways, it's " + Misc.getDGSCredits(RETROFIT_FEE)
                        + " credits for access. You interested?\"");
            text.highlightFirstInLastPara(Misc.getDGSCredits(RETROFIT_FEE), Misc.getHighlightColor());
        }


        OptionPanelAPI options = dialog.getOptionPanel();
        options.clearOptions();
        options.addOption("Pay " + Misc.getDGSCredits(fee) + " credits", OptionId.PAY);
        options.addOption("Decline and excuse yourself", OptionId.LEAVE);

        if (Global.getSector().getPlayerFleet().getCargo().getCredits().get() < fee) {
            options.setEnabled(OptionId.PAY, false);
            options.setTooltip(OptionId.PAY, "Not enough credits!");
        }
    }

    private void payFee() {
        float fee = getFee();
        if (fee > 0) {
            Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(fee);

            getRoiders().getMemoryWithoutUpdate().set(Roider_MemFlags.FEE_PAID, true);
            ((StoragePlugin) Misc.getStorage(market)).setPlayerPaidToUnlock(true);
        }

        showRetrofitting();
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
        options.addOption("Pay " + Misc.getDGSCredits(STORAGE_FEE) + " credits", OptionId.PAY);
        options.addOption("Nevermind", OptionId.LEAVE);

        if (Global.getSector().getPlayerFleet().getCargo().getCredits().get() < STORAGE_FEE) {
            options.setEnabled(OptionId.PAY, false);
            options.setTooltip(OptionId.PAY, "Not enough credits!");
        }
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        return market.hasIndustry(Roider_Industries.UNION_HQ)
                    && market.getIndustry(Roider_Industries.UNION_HQ).isFunctional();
    }

    @Override
    public boolean endWithContinue() {
        return false;
    }

    private FactionAPI getRoiders() {
        return Global.getSector().getFaction(Roider_Factions.ROIDER_UNION);
    }

}
