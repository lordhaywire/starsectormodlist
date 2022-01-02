package scripts.campaign.intel.bar;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Industries;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import scripts.campaign.bases.Roider_RoiderBaseIntelV2;
import scripts.campaign.bases.Roider_RoiderBaseManager;

public class Roider_RoiderBaseRumorBarEvent extends BaseBarEvent {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_RoiderBaseRumorBarEvent.class, "intel", "i");
        x.aliasAttribute(Roider_RoiderBaseRumorBarEvent.class, "potentialBars", "p");
        x.aliasAttribute(Roider_RoiderBaseRumorBarEvent.class, "uid", "d");
    }

	protected Roider_RoiderBaseIntelV2 intel;
    protected List<String> potentialBars;
    protected String uid;

	public Roider_RoiderBaseRumorBarEvent(Roider_RoiderBaseIntelV2 intel) {
		this.intel = intel;
        uid = Misc.genUID();

        potentialBars = new ArrayList<>();

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {

            if (market.getMemoryWithoutUpdate().contains(Roider_RoiderBaseManager.RUMOR_HERE)) continue;

            float weight = 0;

            if (market.hasIndustry(Roider_Industries.DIVES)) weight = 1f;
            if (market.hasIndustry(Roider_Industries.UNION_HQ)) {
                Industry unionHQ = market.getIndustry(Roider_Industries.UNION_HQ);

                if (unionHQ.isFunctional()) weight = 2f;
            }

            if (market.getFaction().isHostileTo(Factions.INDEPENDENT)) weight *= 2;

            if (weight > 0) picker.add(market.getId(), weight);
        }

        float numRumors = 1f + new Random().nextFloat() * 3f;
//        numRumors = 1;
        while (numRumors > 0) {
            if (picker.isEmpty()) break;

            String market = picker.pickAndRemove();
            potentialBars.add(market);
//            market.getMemoryWithoutUpdate().set(Roider_RoiderBaseManager.RUMOR_HERE, uid);
            numRumors--;
        }
	}

	public boolean shouldShowAtMarket(MarketAPI market) {
		if (!super.shouldShowAtMarket(market)) {
            potentialBars.remove(market.getId());
            if (potentialBars.isEmpty()) done = true;
            return false;
        }

        String rumorId = (String) market.getMemoryWithoutUpdate()
                    .get(Roider_RoiderBaseManager.RUMOR_HERE);

        if (rumorId != null) {
            boolean thisRumorHere = rumorId.equals(uid);

            if (!thisRumorHere) potentialBars.remove(market.getId());
            if (potentialBars.isEmpty()) done = true;

            return thisRumorHere;
        }

        if (potentialBars.contains(market.getId())) {
            market.getMemoryWithoutUpdate().set(Roider_RoiderBaseManager.RUMOR_HERE, uid);
            return true;
        }

        return false;
	}

	@Override
	public boolean shouldRemoveEvent() {
		return intel.isEnding() || intel.isEnded() || intel.isPlayerVisible();
	}



	transient protected boolean done = false;
	transient protected Gender gender;

	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		gender = Gender.MALE;
		if ((float) Math.random() > 0.5f) {
			gender = Gender.FEMALE;
		}

		String himOrHer = "him";
		if (gender == Gender.FEMALE) himOrHer = "her";

		TextPanelAPI text = dialog.getTextPanel();
		text.addPara("A hard-up roider sits at the bar, downing shots " +
                    "of what looks like the cheapest liquor available.");

		dialog.getOptionPanel().addOption(
				"Approach the roider and offer to buy " + himOrHer
                            + " something more palatable", this);
	}

	@Override
	public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.init(dialog, memoryMap);

		String himOrHerSelf = "himself";
		if (gender == Gender.FEMALE) himOrHerSelf = "herself";

		TextPanelAPI text = dialog.getTextPanel();
		text.addPara("You keep the drinks going and mostly just listen, " +
					 "letting the roider unburden " + himOrHerSelf + ".");

        MarketAPI market = dialog.getInteractionTarget().getMarket();
		PersonAPI person = market.getFaction().createRandomPerson(gender);
		dialog.getVisualPanel().showPersonInfo(person, true);

		done = true;
		intel.makeKnown();
		intel.sendUpdate(Roider_RoiderBaseIntelV2.DISCOVERED_PARAM, text);
        market.getMemoryWithoutUpdate().unset(Roider_RoiderBaseManager.RUMOR_HERE);

		PortsideBarData.getInstance().removeEvent(this);
	}


	@Override
	public void optionSelected(String optionText, Object optionData) {
	}

	@Override
	public boolean isDialogFinished() {
		return done;
	}
}



