package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

// more things borrowed from smarter modders

public class sfckweencores extends BaseCommandPlugin {

    public boolean turnin;

    protected String moddity;
    protected String moddity2;
    protected String moddity3;

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected CargoAPI playerCargo;
    protected MemoryAPI memory;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected PersonAPI person;
    protected FactionAPI faction;

    protected boolean buysAICores;
    protected float valueMult;
    protected float repMult;
    //protected float repMult2;

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        person = dialog.getInteractionTarget().getActivePerson();
        faction = person.getFaction();

        //buysAICores = faction.getCustomBoolean("buysAICores");
        valueMult = 2.5f; //faction.getCustomFloat("AICoreValueMult");
        repMult = 0.75f;
        //repMult2 = 1f;
        switch (command) {
            case "selectCores":
                WeightedRandomPicker<String> comm = new WeightedRandomPicker<String>();
                comm.add("graphics/icons/cargo/ai_core_gamma.png", 3);
                comm.add("graphics/icons/cargo/ai_core_beta.png", 2);
                comm.add("graphics/icons/cargo/ai_core_alpha.png", 1);
                moddity = comm.pick();
                moddity2 = comm.pick();
                moddity3 = comm.pick();
                turnin = true;
                selectCores();
                break;
            case "selectSMatter":
                WeightedRandomPicker<String> comm2 = new WeightedRandomPicker<String>();
                comm2.add("graphics/ISTL/icons/cargo/istl_sigma_unstable.png", 3);
                comm2.add("graphics/ISTL/icons/cargo/istl_sigma_low.png", 2);
                comm2.add("graphics/ISTL/icons/cargo/istl_sigma_high.png", 1);
                moddity = comm2.pick();
                moddity2 = comm2.pick();
                moddity3 = comm2.pick();
                selectGrindset();
                break;
            case "playerHasGrindset":
                return playerHasGrindset();
            case "selectKeycard":
                WeightedRandomPicker<String> comm3 = new WeightedRandomPicker<String>();
                comm3.add("graphics/icons/cargo/uaf_arv_keycard_red.png", 1);
                comm3.add("graphics/icons/cargo/uaf_arv_keycard_blue.png", 1);
                comm3.add("graphics/icons/cargo/uaf_arv_keycard_green.png", 1);
                moddity = comm3.pick();
                moddity2 = comm3.pick();
                moddity3 = comm3.pick();
                selectKeycard();
                break;
            case "playerHasTarkov":
                return playerHasTarkov();
            default:
                break;
        }

        return true;
    }
	/*protected boolean personCanAcceptCores() {
		if (person == null || !buysAICores) return false;

		return Ranks.POST_BASE_COMMANDER.equals(person.getPostId()) ||
			   Ranks.POST_STATION_COMMANDER.equals(person.getPostId()) ||
			   Ranks.POST_ADMINISTRATOR.equals(person.getPostId()) ||
			   Ranks.POST_OUTPOST_COMMANDER.equals(person.getPostId());
	}*/

    protected void selectCores() {
        CargoAPI copy = Global.getFactory().createCargo(false);
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec != null && spec.getDemandClass().equals(Commodities.AI_CORES)) {
                copy.addFromStack(stack);
            }
        }
        copy.sort();

        final float width = 310f;
        dialog.showCargoPickerDialog("Select AI cores to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
            public void pickedCargo(CargoAPI cargo) {
                cargo.sort();
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
                    if (stack.isCommodityStack()) {
                        AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), text);
                    }
                }

                float bounty = computeCoreCreditValue(cargo);
                float repChange = computeCoreReputationValue(cargo);

                if (bounty > 0) {
                    playerCargo.getCredits().add(bounty);
                    AddRemoveCommodity.addCreditsGainText((int)bounty, text);
                }

                if (repChange >= 1f) {
                    CustomRepImpact impact = new CustomRepImpact();
                    impact.delta = repChange * 0.01f;
                    if (impact.delta >= 0.01f) {
                        Global.getSector().adjustPlayerReputation(
                                new RepActionEnvelope(RepActions.CUSTOM, impact,
                                        null, text, true),
                                person);
                    }
                }
                if (bounty == 0 || repChange < 1f) {
                    FireAll.fire(null, dialog, memoryMap, "AICoresTurnedInKween");
                } else {FireBest.fire(null, dialog, memoryMap, "AICoresTurnedInKween");}

            }
            public void cancelledCargoSelection() {
            }
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {

                /*float bounty = computeCoreCreditValue(combined);
                float repChange = computeCoreReputationValue(combined);

                float pad = 3f;
                float opad = 10f;

                panel.setParaFontOrbitron();
                panel.addPara(Misc.ucFirst("Sindrian Fuel Company?"), faction.getBaseUIColor(), 1f);
                panel.setParaFontDefault();
                panel.addImage("graphics/icons/cargo/sfc_sd_logo.png", width, 3f);
                panel.addImages(width, 80, 3f, 0f, moddity, moddity2, moddity3);
                panel.addPara("Compared to dealing with others, turning AI cores in to Yunris Kween will result in: ", opad);
                panel.beginGridFlipped(width, 1, 40f, 10f);
                panel.addToGrid(0, 0, "Bounty Received", "" + (int)(valueMult * 100f) + "%");
                panel.addToGrid(0, 1, "Reputation Increased", "" + (int)(repMult * 100f) + "%");
                panel.addGrid(pad);

                panel.addPara("If you turn in the selected AI cores, you will receive a %s bounty reward " +
                                "and your standing with Yunris Kween will improve by %s points.",
                        opad, Misc.getHighlightColor(),
                        Misc.getWithDGS(bounty) + Strings.C,
                        "" + (int) repChange);*/

                float bounty = computeCoreCreditValue(combined);
                float repChange = computeCoreReputationValue(combined);

                float pad = 3f;
                float small = 5f;
                float opad = 10f;

                panel.setParaFontOrbitron();
                panel.addPara(Misc.ucFirst("Sindrian Fuel?"), faction.getBaseUIColor(), 1f);
                panel.setParaFontDefault();
                panel.addImage(faction.getLogo(), width * 1f, 3f);
                panel.addPara("Compared to dealing with others, turning AI cores in to Yunris Kween will result in: ", opad);
                panel.beginGridFlipped(width, 1, 40f, 10f);
                //panel.beginGrid(150f, 1);
                panel.addToGrid(0, 0, "Bounty value", "" + (int)(valueMult * 100f) + "%");
                panel.addToGrid(0, 1, "Relation gain", "" + (int)(repMult * 100f) + "%");
                panel.addGrid(pad);

                panel.addPara("If you turn in the selected AI cores, you will receive a %s bounty reward " +
                                "and your relations with Yunris Kween will improve by %s points.",
                        opad * 1f, Misc.getHighlightColor(),
                        Misc.getWithDGS(bounty) + Strings.C,
                        "" + (int) repChange);
            }
        });
    }

    protected boolean playerHasGrindset() {
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec != null && spec.hasTag("sigma_matter")) {
                return true;
            }
        }
        return false;
    }

    protected void selectGrindset() {
        CargoAPI copy = Global.getFactory().createCargo(false);
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec != null && spec.hasTag("sigma_matter")) {
                copy.addFromStack(stack);
            }
        }
        copy.sort();

        final float width = 310f;
        dialog.showCargoPickerDialog("Select Sigma matter to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
            @Override
            public void pickedCargo(CargoAPI cargo) {
                cargo.sort();
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
                    if (stack.isCommodityStack()) { // should be always, but just in case
                        AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), text);
                    }
                }

                float bounty = computeCoreCreditValue(cargo);
                float repChange = computeCoreReputationValue(cargo);

                if (bounty > 0) {
                    playerCargo.getCredits().add(bounty);
                    AddRemoveCommodity.addCreditsGainText((int)bounty, text);
                }

                if (repChange >= 1f) {
                    CustomRepImpact impact = new CustomRepImpact();
                    impact.delta = repChange * 0.01f;
                    if (impact.delta >= 0.01f) {
                        Global.getSector().adjustPlayerReputation(
                                new RepActionEnvelope(RepActions.CUSTOM, impact,
                                        null, text, true),
                                person);
                    }
                }
                if (bounty == 0 || repChange < 1f) {
                    FireAll.fire(null, dialog, memoryMap, "GrindsetTurnedInKween");
                } else {FireBest.fire(null, dialog, memoryMap, "GrindsetTurnedInKween");}
            }
            @Override
            public void cancelledCargoSelection() {
            }
            @Override
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {

                /*float bounty = computeCoreCreditValue(combined);
                float repChange = computeCoreReputationValue(combined);

                float pad = 3f;
                float opad = 10f;

                panel.setParaFontOrbitron();
                panel.addPara(Misc.ucFirst("Sindrian Fuel Company?"), faction.getBaseUIColor(), 1f);
                panel.setParaFontDefault();
                panel.addImage("graphics/icons/cargo/sfc_sd_logo.png", width * 1f, 3f);
                panel.addImages(width, 80, 3f, 0f, moddity, moddity2, moddity3);
                panel.addPara("Compared to dealing with others, turning Sigma matter in to Yunris Kween will result in: ", opad);
                panel.beginGridFlipped(width, 1, 40f, 10f);
                panel.addToGrid(0, 0, "Bounty Received", "" + (int)(valueMult * 100f) + "%");
                panel.addToGrid(0, 1, "Reputation Increased", "" + (int)(repMult * 100f) + "%");
                panel.addGrid(pad);

                panel.addPara("If you turn in the selected Sigma matter, you will receive a %s bounty reward " +
                                "and your standing with Yunris Kween will improve by %s points.",
                        opad * 1f, Misc.getHighlightColor(),
                        Misc.getWithDGS(bounty) + Strings.C,
                        "" + (int) repChange);*/

                float bounty = computeCoreCreditValue(combined);
                float repChange = computeCoreReputationValue(combined);

                float pad = 3f;
                float small = 5f;
                float opad = 10f;

                panel.setParaFontOrbitron();
                panel.addPara(Misc.ucFirst("Sindrian Fuel?"), faction.getBaseUIColor(), 1f);
                panel.setParaFontDefault();
                panel.addImage(faction.getLogo(), width * 1f, 3f);
                panel.addPara("Compared to dealing with others, turning Sigma matter in to Yunris Kween will result in: ", opad);
                panel.beginGridFlipped(width, 1, 40f, 10f);
                //panel.beginGrid(150f, 1);
                panel.addToGrid(0, 0, "Bounty value", "" + (int)(valueMult * 100f) + "%");
                panel.addToGrid(0, 1, "Relation gain", "" + (int)(repMult * 100f) + "%");
                panel.addGrid(pad);

                panel.addPara("If you turn in the selected Sigma matter, you will receive a %s bounty reward " +
                                "and your relations with Yunris Kween will improve by %s points.",
                        opad * 1f, Misc.getHighlightColor(),
                        Misc.getWithDGS(bounty) + Strings.C,
                        "" + (int) repChange);
            }
        });
    }
    protected boolean playerHasTarkov() {
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            SpecialItemSpecAPI spec = stack.getSpecialItemSpecIfSpecial();
            if (spec != null && spec.hasTag("uaf_keycard")) {
                return true;
            }
        }
        return false;
    }

    protected void selectKeycard() {
        CargoAPI copy = Global.getFactory().createCargo(false);
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            SpecialItemSpecAPI spec = stack.getSpecialItemSpecIfSpecial();
            if (spec != null && spec.hasTag("uaf_keycard")) {
                copy.addFromStack(stack);
            }
        }
        copy.sort();

        final float width = 310f;
        dialog.showCargoPickerDialog("Select Keycards to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
            @Override
            public void pickedCargo(CargoAPI cargo) {
                cargo.sort();
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
                    if (stack.isCommodityStack()) { // should be always, but just in case
                        AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), text);
                    }
                }

                float bounty = computeCoreCreditValue(cargo);
                float repChange = computeCoreReputationValue(cargo);

                if (bounty > 0) {
                    playerCargo.getCredits().add(bounty);
                    AddRemoveCommodity.addCreditsGainText((int)bounty, text);
                }

                if (repChange >= 1f) {
                    CustomRepImpact impact = new CustomRepImpact();
                    impact.delta = repChange * 0.01f;
                    if (impact.delta >= 0.01f) {
                        Global.getSector().adjustPlayerReputation(
                                new RepActionEnvelope(RepActions.CUSTOM, impact,
                                        null, text, true),
                                person);
                    }
                }

                if (repChange >= 1f) {
                    CustomRepImpact impact = new CustomRepImpact();
                    impact.delta = repChange * -0.01f;
                    if (impact.delta <= -0.01f) {
                        Global.getSector().adjustPlayerReputation(
                                new RepActionEnvelope(RepActions.CUSTOM, impact,
                                        null, text, true),
                                "uaf");
                    }
                }

                if (bounty == 0 || repChange < 1f) {
                    FireAll.fire(null, dialog, memoryMap, "TarkovTurnedInKween");
                } else {FireBest.fire(null, dialog, memoryMap, "TarkovTurnedInKween");}
            }
            @Override
            public void cancelledCargoSelection() {
            }
            @Override
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {

                /*float bounty = computeCoreCreditValue(combined);
                float repChange = computeCoreReputationValue(combined);

                float pad = 3f;
                float opad = 10f;

                panel.setParaFontOrbitron();
                panel.addPara(Misc.ucFirst("Sindrian Fuel Company?"), faction.getBaseUIColor(), 1f);
                panel.setParaFontDefault();
                panel.addImage("graphics/icons/cargo/sfc_sd_logo.png", width * 1f, 3f);
                panel.addImages(width, 80, 3f, 0f, moddity, moddity2, moddity3);
                panel.addPara("Compared to dealing with others, turning Sigma matter in to Yunris Kween will result in: ", opad);
                panel.beginGridFlipped(width, 1, 40f, 10f);
                panel.addToGrid(0, 0, "Bounty Received", "" + (int)(valueMult * 100f) + "%");
                panel.addToGrid(0, 1, "Reputation Increased", "" + (int)(repMult * 100f) + "%");
                panel.addGrid(pad);

                panel.addPara("If you turn in the selected Sigma matter, you will receive a %s bounty reward " +
                                "and your standing with Yunris Kween will improve by %s points.",
                        opad * 1f, Misc.getHighlightColor(),
                        Misc.getWithDGS(bounty) + Strings.C,
                        "" + (int) repChange);*/

                float bounty = computeCoreCreditValue(combined);
                float repChange = computeCoreReputationValue(combined);

                float pad = 3f;
                float small = 5f;
                float opad = 10f;

                panel.setParaFontOrbitron();
                panel.addPara(Misc.ucFirst("Sindrian Fuel?"), faction.getBaseUIColor(), 1f);
                panel.setParaFontDefault();
                panel.addImage(faction.getLogo(), width * 1f, 3f);
                panel.addPara("Compared to dealing with others, turning Keycards in to Yunris Kween will result in: ", opad);
                panel.beginGridFlipped(width, 1, 40f, 10f);
                //panel.beginGrid(150f, 1);
                panel.addToGrid(0, 0, "Bounty value", "" + (int)(valueMult * 100f) + "%");
                panel.addToGrid(0, 1, "Relation gain", "" + (int)(repMult * 100f) + "%");
                panel.addToGrid(0, 2, "UAF Relation loss", "" + (int)(repMult * -100f) + "%");
                panel.addGrid(pad);

                panel.addPara("If you turn in the selected UAF Keycards, you will receive a %s bounty reward " +
                                "and your relations with Yunris Kween will improve by %s points. " +
                                "However, turning in UAF Keycards here will lower relations with the United Auroran Federation.",
                        opad * 1f, Misc.getHighlightColor(),
                        Misc.getWithDGS(bounty) + Strings.C,
                        "" + (int) repChange);
            }
        });
    }

    protected float computeCoreCreditValue(CargoAPI cargo) {
        float bounty = 0;
        float sigmabounty = 0;
        float keycardbounty = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            SpecialItemSpecAPI spec2 = stack.getSpecialItemSpecIfSpecial();
            if (spec != null && spec.getDemandClass().equals(Commodities.AI_CORES)) {
                bounty += getBaseCreditValue(spec.getId()) * stack.getSize();
            }
            if (spec != null && spec.hasTag("sigma_matter")) {
                sigmabounty += getBaseCreditValue(spec.getId()) * stack.getSize();
            }
            if (spec2 != null && spec2.hasTag("uaf_keycard")) {
                keycardbounty += getBaseCreditValue(spec2.getId()) * stack.getSize();
            }
        }
        bounty *= valueMult;
        sigmabounty *= valueMult;
        keycardbounty *= valueMult;
        bounty += sigmabounty;
        bounty += keycardbounty;
        return bounty;
    }

    public static float getBaseCreditValue(String coreType) {
        if (Commodities.OMEGA_CORE.equals(coreType)) {
            return 0f; //one day
        }
        if (Commodities.ALPHA_CORE.equals(coreType)) {
            return 150000;
        }
        if (Commodities.BETA_CORE.equals(coreType)) {
            return 30000;
        }
        if (Commodities.GAMMA_CORE.equals(coreType)) {
            return 10000;
        }
        if ("istl_sigma_matter2".equals(coreType)) {
            return 50000;
        }

        if ("istl_sigma_matter1".equals(coreType)) {
            return 25000;
        }
        if ("istl_sigma_matter3".equals(coreType)) {
            return 5000;
        }
        if ("uaf_keycard_orange".equals(coreType)) {
            return 100000;
        }
        if ("uaf_keycard_violet".equals(coreType)) {
            return 150000;
        }
        if ("uaf_keycard_green".equals(coreType)) {
            return 250000;
        }
        if ("uaf_keycard_blue".equals(coreType)) {
            return 350000;
        }
        if ("uaf_keycard_red".equals(coreType)) {
            return 500000;
        }
        return 1f;
    }

    protected float computeCoreReputationValue(CargoAPI cargo) {
        float rep = 0;
        float sigmarep = 0;
        float keycardrep = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            SpecialItemSpecAPI spec2 = stack.getSpecialItemSpecIfSpecial();
            if (spec != null && spec.getDemandClass().equals(Commodities.AI_CORES)) {
                rep += getBaseRepValue(spec.getId()) * stack.getSize();
            }
            if (spec != null && spec.hasTag("sigma_matter")) {
                sigmarep += getBaseRepValue(spec.getId()) * stack.getSize();
            }
            if (spec2 != null && spec2.hasTag("uaf_keycard")) {
                keycardrep += getBaseRepValue(spec2.getId()) * stack.getSize();
            }
        }
        rep *= repMult;
        sigmarep *= repMult;
        keycardrep *= repMult;
        rep += sigmarep;
        rep += keycardrep;
        return rep;
    }

    public static float getBaseRepValue(String coreType) {
        if (Commodities.OMEGA_CORE.equals(coreType)) {
            return 0f; //one day
        }
        if (Commodities.ALPHA_CORE.equals(coreType)) {
            return 5f;
        }
        if (Commodities.BETA_CORE.equals(coreType)) {
            return 2f;
        }
        if (Commodities.GAMMA_CORE.equals(coreType)) {
            return 1f;
        }
        if ("istl_sigma_matter2".equals(coreType)) {
            return 1.75f;
        }
        if ("istl_sigma_matter1".equals(coreType)) {
            return 1.25f;
        }
        if ("istl_sigma_matter3".equals(coreType)) {
            return 0.5f;
        }
        if ("uaf_keycard_violet".equals(coreType)) {
            return 1f;
        }
        if ("uaf_keycard_orange".equals(coreType)) {
            return 2f;
        }
        if ("uaf_keycard_green".equals(coreType)) {
            return 3f;
        }
        if ("uaf_keycard_blue".equals(coreType)) {
            return 4f;
        }
        if ("uaf_keycard_red".equals(coreType)) {
            return 5f;
        }
        return 1f;
    }

    //Technically don't need this.
	/*protected boolean playerHasCores() {
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.AI_CORES)) {
				return true;
			}
		}
		return false;
	}*/



}
















