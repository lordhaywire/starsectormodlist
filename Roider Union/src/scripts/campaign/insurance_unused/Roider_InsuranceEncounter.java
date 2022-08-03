package scripts.campaign.insurance_unused;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_Industries;
import java.util.List;
import java.util.Map;

/**
 * Author: SafariJohn
 */
public class Roider_InsuranceEncounter extends HubMissionWithBarEvent {

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        if (!barEvent) return false;
        if (Roider_InsuranceTracker.isPlayerInsured()) return false;

        // Must be Union market or have Dives or Union HQ
        boolean isUnion = createdAt.getFactionId().equals(Roider_Factions.ROIDER_UNION);
        boolean hasRoiders = createdAt.hasIndustry(Roider_Industries.DIVES)
                    || createdAt.hasIndustry(Roider_Industries.UNION_HQ);
        if (!(isUnion || hasRoiders)) return false;

        setGiverFaction(Roider_Factions.ROIDER_UNION);
        setGiverPost(Ranks.POST_AGENT);
        setGiverVoice(Voices.BUSINESS);
        findOrCreateGiver(createdAt, false, true);

		PersonAPI person = getPerson();
		if (person == null) return false;

		setRepFactionChangesTiny();

        setDoNotAutoAddPotentialContactsOnSuccess();

        boolean refSet = setPersonMissionRef(person, "$roider_insurance_ref");

		if (!refSet) {
			return false;
		}


        return true;
    }

    @Override
	protected void updateInteractionDataImpl() {
        Roider_InsuranceTracker insTracker = Roider_InsuranceTracker.getInsurance();

        // Claims sum
        float claims = insTracker.getCompensation();
		set("$roider_insuranceClaims", Misc.getDGSCredits(claims));
        // MIDAS sum
        float midas = insTracker.getMIDASCreditRoundedUp();
		set("$roider_insuranceMIDAS", Misc.getDGSCredits(midas));
        // Fees sum
        float fees = insTracker.getFeesRoundUp();
		set("$roider_insuranceFees", Misc.getDGSCredits(fees));
        // per class average
		set("$roider_insuranceFrigateFee", Misc.getDGSCredits(insTracker.getHullFee(HullSize.FRIGATE)));
		set("$roider_insuranceDestroyerFee", Misc.getDGSCredits(insTracker.getHullFee(HullSize.DESTROYER)));
		set("$roider_insuranceCruiserFee", Misc.getDGSCredits(insTracker.getHullFee(HullSize.CRUISER)));
		set("$roider_insuranceCapitalFee", Misc.getDGSCredits(insTracker.getHullFee(HullSize.CAPITAL_SHIP)));
        // Damages sum
        float damages = insTracker.getDamages();
		set("$roider_insuranceDamages", Misc.getDGSCredits(damages));

        // Normal sum
		set("$roider_insuranceSum", claims + midas - fees - damages);
		set("$roider_insuranceSumString", Misc.getDGSCredits(Math.abs(claims + midas - fees - damages)));
        // SP sum
		set("$roider_insuranceSPSum", claims + midas - fees);
		set("$roider_insuranceSPSumString", Misc.getDGSCredits(claims + midas - fees));

        // Coverage period (months)
        int months = insTracker.getFeeMonthsRoundedUp();
		set("$roider_insurancePeriod", "" + months);
		if (months == 1) set("$roider_insuranceMonthOrS", "month's");
        else set("$roider_insuranceMonthOrS", "months'");
	}


	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
							     Map<String, MemoryAPI> memoryMap) {
        if (action.equals("enroll")) {
            Roider_InsuranceTracker.setPlayerInsured(true);

            return true;
        }
        if (action.equals("clearData")) {
            Roider_InsuranceTracker.getInsurance().clearLumpData();

            return true;
        }

		return false;
	}

	@Override
	public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		currentStage = new Object(); // so that the abort() assumes the mission was successful
		abort();
	}
}
