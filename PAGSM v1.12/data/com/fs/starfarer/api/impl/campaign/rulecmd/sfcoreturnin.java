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

public class sfcoreturnin extends BaseCommandPlugin {

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
            case "selectOre":
                WeightedRandomPicker<String> comm3 = new WeightedRandomPicker<String>();
                comm3.add("graphics/icons/cargo/ore.png", 1);
                comm3.add("graphics/icons/cargo/rareore.png", 1);
                comm3.add("graphics/icons/cargo/ore.png", 1);
                moddity = comm3.pick();
                moddity2 = comm3.pick();
                moddity3 = comm3.pick();
                selectOre();
                break;
            case "playerHasOre":
                return playerHasOre();
            default:
                break;
        }

        return true;
    }

    public static boolean isOre(CargoStackAPI stack) {
        CommoditySpecAPI spec = stack.getResourceIfResource();
        if (spec == null) {
            return false;
        } else {
            return spec.getId().equals(Commodities.ORE)
                    || spec.getId().equals(Commodities.RARE_ORE);
        }
    }

    protected boolean playerHasOre() {
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec != null && isOre(stack)) {
                return true;
            }
        }
        return false;
    }

    protected void selectOre() {
        CargoAPI copy = Global.getFactory().createCargo(false);
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec != null && isOre(stack)) {
                copy.addFromStack(stack);
            }
        }
        copy.sort();

        final float width = 310f;
        dialog.showCargoPickerDialog("Select Ore to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
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
                    FireAll.fire(null, dialog, memoryMap, "sfcOreTurnedInDunnLow");
                } else {FireBest.fire(null, dialog, memoryMap, "sfcOreTurnedInDunn");}
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
                panel.addPara("Compared to dealing with others, turning Ore in to Meridin Dunn will result in: ", opad);
                panel.beginGridFlipped(width, 1, 40f, 10f);
                panel.addToGrid(0, 0, "Bounty value", "" + (int)(valueMult * 100f) + "%");
                panel.addToGrid(0, 1, "Relation gain", "" + (int)(repMult * 100f) + "%");
                panel.addGrid(pad);

                panel.addPara("If you turn in the selected Ore, you will receive a %s bounty reward " +
                                "and your relations with Meridin Dunn will improve by %s points. ",
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
            if (spec != null && spec.getDemandClass().equals(Commodities.ORE)) {
                bounty += getBaseCreditValue(spec.getId()) * stack.getSize();
            }
            if (spec != null && spec.getDemandClass().equals(Commodities.RARE_ORE)) {
                bounty += getBaseCreditValue(spec.getId()) * stack.getSize();
            }
        }
        bounty *= valueMult;
        return bounty;
    }

    public static float getBaseCreditValue(String coreType) {
        if (Commodities.ORE.equals(coreType)) {
            return 10;
        }
        if (Commodities.RARE_ORE.equals(coreType)) {
            return 75;
        }
        return 1f;
    }

    protected float computeCoreReputationValue(CargoAPI cargo) {
        float rep = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec != null && spec.getDemandClass().equals(Commodities.ORE)) {
                rep += getBaseRepValue(spec.getId()) * stack.getSize();
            }
            if (spec != null && spec.getDemandClass().equals(Commodities.RARE_ORE)) {
                rep += getBaseRepValue(spec.getId()) * stack.getSize();
            }
        }
        rep *= repMult;
        return rep;
    }

    public static float getBaseRepValue(String coreType) {
        if (Commodities.ORE.equals(coreType)) {
            return 0.0001f;
        }
        if (Commodities.RARE_ORE.equals(coreType)) {
            return 0.0004f;
        }
        return 1f;
    }
}
















