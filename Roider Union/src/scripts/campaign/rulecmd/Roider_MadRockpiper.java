package scripts.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import data.missions.Roider_DModManager;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_Hullmods;
import ids.Roider_Ids.Roider_Industries;
import java.util.List;
import java.util.Map;
import static scripts.campaign.rulecmd.Roider_APRAccess.COLUMNS;

/**
 * Author: SafariJohn
 */
public class Roider_MadRockpiper extends HubMissionWithBarEvent {
    public static final String MADMAN_KEY = "$roider_madRockpiperData";
    public static final String SP_REQUIRED = "$roider_madRockpiperSPReq";
    public static final float EVENT_DAYS = 40;


    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        if (!barEvent) return false;

        // Must be Union market or have Dives or Union HQ
        boolean isUnion = createdAt.getFactionId().equals(Roider_Factions.ROIDER_UNION);
        boolean hasRoiders = createdAt.hasIndustry(Roider_Industries.DIVES)
                    || createdAt.hasIndustry(Roider_Industries.UNION_HQ);
        if (!(isUnion || hasRoiders)) return false;

        setGiverFaction(Roider_Factions.ROIDER_UNION);
        setGiverPost(Ranks.POST_CITIZEN);
        setGiverVoice(Voices.SPACER);
        findOrCreateGiver(createdAt, false, true);

		PersonAPI person = getPerson();

		setRepFactionChangesTiny();

        boolean refSet = setPersonMissionRef(person, "$roider_madRockpiper_ref");

        setDoNotAutoAddPotentialContactsOnSuccess();

//        if (genRandom.nextFloat() < 0.1f) setSPRequired(true);
        if (!Global.getSector().getMemoryWithoutUpdate().contains(SP_REQUIRED)
                    && genRandom.nextFloat() < 0.1f) setSPRequired(true);

        return refSet;
    }

    @Override
    public PersonAPI getPerson() {
        PersonAPI person = (PersonAPI) Global.getSector().getMemoryWithoutUpdate().get(MADMAN_KEY);
        if (person == null) {
            person = Global.getSector().getFaction(Roider_Factions.ROIDER_UNION).createRandomPerson();

            person.setName(new FullName("Mad Rockpiper", "", person.getGender()));

            if (person.getGender() == Gender.MALE) {
                person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "roider_madRockpiperMale"));
            } else {
                person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "roider_madRockpiperFemale"));
            }

            Global.getSector().getMemoryWithoutUpdate().set(MADMAN_KEY, person);
        }

        return person;
    }

    public static boolean getSPRequired() {
        return Global.getSector().getMemoryWithoutUpdate().getBoolean(SP_REQUIRED);
    }

    public static void setSPRequired(boolean required) {
        Global.getSector().getMemoryWithoutUpdate().set(SP_REQUIRED, required, EVENT_DAYS);
    }

    @Override
	protected void updateInteractionDataImpl() {
        String himOrHer = getPerson().getHimOrHer();
        String themself = himOrHer.equals("him") ? "himself" : "herself";
        String mad = himOrHer.equals("him") ? "madman" : "madwoman";

        set("$roider_mrp_himOrHer", himOrHer);
        set("$roider_mrp_himselfOrHerself", themself);
        set("$roider_mrp_madmanOrWoman", mad);

        set("$roider_mrp_requiresSP", getSPRequired());
        set("$roider_mrp_isInhosp", Global.getSector().getPlayerFaction()
                    .getRelationshipLevel(Roider_Factions.ROIDER_UNION)
                    .isAtBest(RepLevel.INHOSPITABLE));
    }

    @Override
    protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (action.equals("showPicker")) {
            showPicker(dialog, memoryMap);
            return true;
        }
        if (action.equals("spentSP")) {
            set("$roider_mrp_requiresSP", false);
            setSPRequired(false);
            return true;
        }

        if (action.equals("canPick")) {
            if (getAvailableShips().isEmpty()) {
                dialog.getOptionPanel().setEnabled("roider_mRPShowPicker", false);
                dialog.getOptionPanel().setTooltip("roider_mRPShowPicker", "No ships that can install MIDAS");
            }

            return true;
        }

        return true;
    }

	protected void showPicker(final InteractionDialogAPI dialog, final Map<String, MemoryAPI> memoryMap) {
        List<FleetMemberAPI> avail = getAvailableShips();

        int rows = avail.size() / 7 + 1;

        dialog.showFleetMemberPickerDialog("Pick ship for MIDAS", "Ok", "Cancel", rows, COLUMNS, 58f, true, false, avail, new FleetMemberPickerListener() {

            @Override
            public void pickedFleetMembers(List<FleetMemberAPI> members) {
                if (members.isEmpty()) return;

                ShipVariantAPI variant = members.get(0).getVariant();
                if (variant.getSMods().contains(Roider_Hullmods.MIDAS)) variant.removePermaMod(Roider_Hullmods.MIDAS);
                variant.addPermaMod(Roider_Hullmods.MIDAS, false);
                if (genRandom.nextBoolean()) {
                    variant.addPermaMod("ill_advised");
                    Roider_DModManager.setDHull(variant);

                    memoryMap.get(MemKeys.LOCAL).set("$roider_mRPIllAdvised", true, 0);
                }

                variant.setSource(VariantSource.REFIT);

                FireBest.fire(null, dialog, memoryMap, "Roider_MRPPostText");
                memoryMap.get(MemKeys.LOCAL).set("$roider_mRPPicked", true, 0);
                FireBest.fire(null, dialog, memoryMap, "Roider_MRPPicked");
            }

            @Override
            public void cancelledFleetMemberPicking() {
                memoryMap.get(MemKeys.LOCAL).set("$roider_mRPPicked", false, 0);
                FireBest.fire(null, dialog, memoryMap, "Roider_MRPPicked");
            }
        });
    }

    protected List<FleetMemberAPI> getAvailableShips() {
        CampaignFleetAPI pool = FleetFactoryV3.createEmptyFleet(Factions.PLAYER, FleetTypes.MERC_PRIVATEER, null);
        for (FleetMemberAPI m : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (hasBuiltInMIDAS(m)) continue;

            pool.getFleetData().addFleetMember(m);
        }

        return pool.getFleetData().getMembersListCopy();
    }

    /**
     * Does not count S-mods as "built-in"
     * @param variant the ship variant to check
     * @return whether the variant has a MIDAS hullmod built-in
     */
    public static boolean hasBuiltInMIDAS(ShipVariantAPI variant) {
        if (variant.hasHullMod(Roider_Hullmods.MIDAS_1)
                    || variant.hasHullMod(Roider_Hullmods.MIDAS_2)
                    || variant.hasHullMod(Roider_Hullmods.MIDAS_3)
                    || variant.hasHullMod(Roider_Hullmods.MIDAS_ARMOR)) return true;

        return variant.hasHullMod(Roider_Hullmods.MIDAS)
                    && !(variant.getSMods().contains(Roider_Hullmods.MIDAS)
                    || variant.getNonBuiltInHullmods().contains(Roider_Hullmods.MIDAS));
    }

    public static boolean hasBuiltInMIDAS(FleetMemberAPI member) {
        return hasBuiltInMIDAS(member.getVariant());
    }

	@Override
	public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		currentStage = new Object(); // so that the abort() assumes the mission was successful
		abort();
	}
}
