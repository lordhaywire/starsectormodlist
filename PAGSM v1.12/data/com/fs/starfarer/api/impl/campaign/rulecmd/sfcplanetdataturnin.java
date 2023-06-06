package com.fs.starfarer.api.impl.campaign.rulecmd;

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

import java.util.*;

// more things borrowed from smarter modders

public class sfcplanetdataturnin extends BaseCommandPlugin {

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

    protected float valueMult;
    protected float repMult;

    /*public static final Set<String> ACTUAL_DATA = new HashSet<>(Arrays.asList(new String[] {
            Commodities.SURVEY_DATA_1,
            Commodities.SURVEY_DATA_2,
            Commodities.SURVEY_DATA_3,
            Commodities.SURVEY_DATA_4,
            Commodities.SURVEY_DATA_5
    }));*/

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
        valueMult = 1.5f;
        repMult = 1f;
        switch (command) {
            case "selectData":
                WeightedRandomPicker<String> comm3 = new WeightedRandomPicker<String>();
                comm3.add("graphics/icons/cargo/survey_data_1.png", 1);
                comm3.add("graphics/icons/cargo/survey_data_3.png", 1);
                comm3.add("graphics/icons/cargo/survey_data_5.png", 1);
                moddity = comm3.pick();
                moddity2 = comm3.pick();
                moddity3 = comm3.pick();
                selectData();
                break;
            case "playerHasPlanetData":
                return playerHasPlanetData();
            default:
                break;
        }

        return true;
    }

    public static boolean isData(CargoStackAPI stack) {
        CommoditySpecAPI spec = stack.getResourceIfResource();
        if (spec == null) {
            return false;
        } else {
            return spec.getId().equals(Commodities.SURVEY_DATA_1)
                    || spec.getId().equals(Commodities.SURVEY_DATA_2)
                    || spec.getId().equals(Commodities.SURVEY_DATA_3)
                    || spec.getId().equals(Commodities.SURVEY_DATA_4)
                    || spec.getId().equals(Commodities.SURVEY_DATA_5);
        }
    }

    protected boolean playerHasPlanetData() {
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec != null && isData(stack)) {
                return true;
            }
        }
        return false;
    }

    protected void selectData() {
        CargoAPI copy = Global.getFactory().createCargo(false);
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec != null && isData(stack)) {
                copy.addFromStack(stack);
            }
        }
        copy.sort();

        final float width = 310f;
        dialog.showCargoPickerDialog("Select Planetary Data to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
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
                        impact.delta *= 0.25f;
                        if (impact.delta >= 0.01f) {
                            Global.getSector().adjustPlayerReputation(
                                    new RepActionEnvelope(RepActions.CUSTOM, impact,
                                            null, text, true),
                                    faction.getId());
                        }
                    }
                }
                if (bounty == 0 || repChange < 1f) {
                    FireAll.fire(null, dialog, memoryMap, "DataTurnedInJenkinsLow");
                } else {FireBest.fire(null, dialog, memoryMap, "DataTurnedInJenkins");}
            }
            @Override
            public void cancelledCargoSelection() {
            }
            @Override
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {

                float bounty = computeCoreCreditValue(combined);
                float repChange = computeCoreReputationValue(combined);

                float pad = 3f;
                float opad = 10f;

                panel.setParaFontOrbitron();
                panel.addPara(Misc.ucFirst("Sindrian Fuel"), faction.getBaseUIColor(), 1f);
                panel.setParaFontDefault();
                panel.addImage(faction.getLogo(), width * 1f, 3f);
                panel.addPara("Compared to dealing with others, turning Planetary Data in to Velmarie Jenkins will result in: ", opad);
                panel.beginGridFlipped(width, 1, 40f, 10f);
                panel.addToGrid(0, 0, "Bounty value", "" + (int)(valueMult * 100f) + "%");
                panel.addToGrid(0, 1, "Relation gain", "" + (int)(repMult * 100f) + "%");
                panel.addGrid(pad);

                panel.addPara("If you turn in the selected Planetary Data, you will receive a %s bounty reward " +
                                "and your relations with Velmarie Jenkins will improve by %s points. ",
                        opad * 1f, Misc.getHighlightColor(),
                        Misc.getWithDGS(bounty) + Strings.C,
                        "" + (int) repChange);
            }
        });
    }

    protected float computeCoreCreditValue(CargoAPI cargo) {
        float bounty = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec != null && spec.getDemandClass().equals(Commodities.SURVEY_DATA)) {
                bounty += getBaseCreditValue(spec.getId()) * stack.getSize();
            }
        }
        bounty *= valueMult;
        return bounty;
    }

    public static float getBaseCreditValue(String coreType) {
        if (Commodities.SURVEY_DATA_1.equals(coreType)) {
            return 1000;
        }
        if (Commodities.SURVEY_DATA_2.equals(coreType)) {
            return 3000;
        }
        if (Commodities.SURVEY_DATA_3.equals(coreType)) {
            return 5000;
        }
        if (Commodities.SURVEY_DATA_4.equals(coreType)) {
            return 10000;
        }
        if (Commodities.SURVEY_DATA_5.equals(coreType)) {
            return 30000;
        }
        return 1f;
    }

    protected float computeCoreReputationValue(CargoAPI cargo) {
        float rep = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec != null && spec.getDemandClass().equals(Commodities.SURVEY_DATA)) {
                rep += getBaseRepValue(spec.getId()) * stack.getSize();
            }
        }
        rep *= repMult;
        return rep;
    }

    public static float getBaseRepValue(String coreType) {
        if (Commodities.SURVEY_DATA_1.equals(coreType)) {
            return 0.2f;
        }
        if (Commodities.SURVEY_DATA_2.equals(coreType)) {
            return 0.25f;
        }
        if (Commodities.SURVEY_DATA_3.equals(coreType)) {
            return 0.39f;
        }
        if (Commodities.SURVEY_DATA_4.equals(coreType)) {
            return 0.5f;
        }
        if (Commodities.SURVEY_DATA_5.equals(coreType)) {
            return 1f;
        }
        return 1f;
    }
}
















