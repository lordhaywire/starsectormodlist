/*package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoPickerListener;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.awt.Color;
//import data.scripts.utils.BBPlus_CargoMarket;

public class sfcplanetdataturninredo extends BaseCommandPlugin {

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
    protected boolean buysData;
    protected float valueMult;
    protected float repMult;
    public static final String SFC_VELMARIE = "Velmarie Jenkins";
    public static Logger log = Global.getLogger(SpecialDataTrader.class);
    private static final String INDEVO = "IndEvo";
    private static final String INDEVO_RARE_PARTS = "IndEvo_rare_parts";
    public static final Set<String> SURVEY_DATA = new HashSet<>(Arrays.asList(new String[] {
            Commodities.SURVEY_DATA_1,
            Commodities.SURVEY_DATA_2,
            Commodities.SURVEY_DATA_3,
            Commodities.SURVEY_DATA_4,
            Commodities.SURVEY_DATA_5
    }));;
    private final Color green = new Color(55,245,65,255);

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Token> params, final Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        this.memory = getEntityMemory(memoryMap);
        this.entity = dialog.getInteractionTarget();
        this.text = dialog.getTextPanel();
        this.options = dialog.getOptionPanel();
        this.playerFleet = Global.getSector().getPlayerFleet();
        this.playerCargo = this.playerFleet.getCargo();
        this.playerFaction = Global.getSector().getPlayerFaction();
        this.entityFaction = this.entity.getFaction();
        this.person = dialog.getInteractionTarget().getActivePerson();
        this.faction = this.person.getFaction();
        this.buysData = this.faction.getCustomBoolean("buysData");
        this.valueMult = this.faction.getCustomFloat("DataValueMult");
        this.repMult = this.faction.getCustomFloat("DataRepMult");
        final String command = params.get(0).getString((Map)memoryMap);
        if (command == null) {
            return false;
        }
        switch (command) {
            case "playerHasData":
                return playerHasData();
            case "personCanAcceptData":
                return personCanAcceptData();
            case "selectData":
                this.selectData();
                break;
            default:
                break;
        }
        return true;
    }

    protected boolean personCanAcceptData() {
        if (person == null || !buysData) {
            return false;
        }
        return BBPlus_People.BERREC.equals(person.getId());
    }

    protected void selectData() {
        final CargoAPI copy = Global.getFactory().createCargo(false);
        for (final CargoStackAPI stack : this.playerCargo.getStacksCopy()) {
            if (isMetals(stack)) {
                copy.addFromStack(stack);
            }
        }
        copy.sort();
        final float width = 310f;
        this.dialog.showCargoPickerDialog("Select commodity to sell", "Confirm", "Cancel", true, width, copy, (CargoPickerListener) new CargoPickerListener() {
            @Override
            public void pickedCargo(final CargoAPI cargo) {
                cargo.sort();
                final float bounty = SpecialDataTrader.this.computeMetalsCreditValue(cargo);
                final float repChange = SpecialDataTrader.this.computeMetalsReputationValue(cargo);
                //final MarketAPI market = Global.getSector().getEconomy().getMarket("deserter_starkeep");
                for (final CargoStackAPI stack : cargo.getStacksCopy()) {
                    SpecialDataTrader.this.playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
                    //BBPlus_CargoMarket.addCommodityStockpile(market, stack.getCommodityId(), stack.getSize());
                    if (stack.isCommodityStack()) {
                        AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), SpecialDataTrader.this.text);
                    }
                }

                if (bounty > 0.0f) {
                    SpecialDataTrader.this.playerCargo.getCredits().add(bounty);
                    AddRemoveCommodity.addCreditsGainText((int)bounty, SpecialDataTrader.this.text);
                }

                if (repChange >= 1f) {
                    final CoreReputationPlugin.CustomRepImpact impact = new CoreReputationPlugin.CustomRepImpact();
                    final CoreReputationPlugin.CustomRepImpact impact2 = new CoreReputationPlugin.CustomRepImpact();
                    impact.delta = repChange * 0.01f;
                    impact2.delta = repChange * 0.01f;
                    impact2.delta *= 0.50f; //impact2.delta *= 0.50f;
                    if (impact.delta >= 0.01f) {
                        Global.getSector().adjustPlayerReputation(
                                (Object)new CoreReputationPlugin.RepActionEnvelope(
                                        CoreReputationPlugin.RepActions.CUSTOM, (Object)impact,
                                        (CommMessageAPI)null, SpecialDataTrader.this.text, true),
                                SpecialDataTrader.this.person);
                    }
                }
                FireBest.fire((String)null, SpecialDataTrader.this.dialog, (Map)SpecialDataTrader.this.memoryMap, "MetalsTurnedIn");
            }

            @Override
            public void cancelledCargoSelection() {
                //return;
            }

            @Override
            public void recreateTextPanel(final TooltipMakerAPI panel, final CargoAPI cargo, final CargoStackAPI pickedUp, final boolean pickedUpFromSource, final CargoAPI combined) {
                final float bounty = SpecialDataTrader.this.computeMetalsCreditValue(combined);
                final float repChange = SpecialDataTrader.this.computeMetalsReputationValue(combined);
                final float pad = 3f;
                final float opad = 10f;
                panel.setParaFontOrbitron();
                panel.setParaFontDefault();
                final int metal_price = Math.round((int)Global.getSettings().getCommoditySpec("metals").getBasePrice());
                final int rare_metal_price = Math.round((int)Global.getSettings().getCommoditySpec("rare_metals").getBasePrice());
                panel.addImage(Global.getSettings().getSpriteName("tooltips", "berrec_orbital_works"), width * 1f, 3f);
                if (Global.getSettings().getModManager().isModEnabled(INDEVO)) {
                    final int relic_component_price = Math.round((int)Global.getSettings().getCommoditySpec("IndEvo_rare_parts").getBasePrice());
                    final TooltipMakerAPI text = panel.beginImageWithText("graphics/icons/cargo/materials.png", 45f);
                    text.addPara("%s", 0, Misc.getHighlightColor(), new String[] { "Metals" });
                    text.addPara("Base price" + " (+%s) per unit.", 0, green, new String[] { Misc.getRoundedValue(metal_price) + Strings.C });
                    panel.addImageWithText(pad);
                    final TooltipMakerAPI text1 = panel.beginImageWithText("graphics/icons/cargo/raremetals.png", 45f);
                    text1.addPara("%s", 0, Misc.getHighlightColor(), new String[] { "Transplutonics" });
                    text1.addPara("Base price" + " (+%s) per unit.", 0, green, new String[] { Misc.getRoundedValue(rare_metal_price) + Strings.C });
                    panel.addImageWithText(1f);
                    final TooltipMakerAPI text2 = panel.beginImageWithText("graphics/icons/cargo/rare_parts.png", 45f);
                    text2.addPara("%s", 0, Misc.getHighlightColor(), new String[] { "Relic Components" });
                    text2.addPara("Base price" + " (+%s) per unit.", 0, green, new String[] { Misc.getRoundedValue(relic_component_price) + Strings.C });
                    panel.addImageWithText(1f);
                    //panel.addImages(width, 80f, 3f, 0f,
                    //        "graphics/icons/cargo/materials.png",
                    //        "graphics/icons/cargo/raremetals.png",
                    //        "graphics/icons/cargo/rare_parts.png"
                    //);
                }
                else {
                    final TooltipMakerAPI text = panel.beginImageWithText("graphics/icons/cargo/materials.png", 50f);
                    text.addPara("%s", 0, Misc.getHighlightColor(), new String[] { "Metals" });
                    text.addPara("Base price" + " (+%s) per unit.", 0, green, new String[] { Misc.getRoundedValue(metal_price) + Strings.C });
                    panel.addImageWithText(pad);
                    final TooltipMakerAPI text1 = panel.beginImageWithText("graphics/icons/cargo/raremetals.png", 50f);
                    text1.addPara("%s", 0, Misc.getHighlightColor(), new String[] { "Transplutonics" });
                    text1.addPara("Base price" + " (+%s) per unit.", 0, green, new String[] { Misc.getRoundedValue(rare_metal_price) + Strings.C });
                    panel.addImageWithText(1f);
                    //panel.addImages(width, 80f, 3f, 0f, "graphics/icons/cargo/materials.png", "graphics/icons/cargo/raremetals.png");
                }
                //panel.addPara(
                //        "Selling metals to " +
                //        SpecialDataTrader.this.faction.getDisplayNameLongWithArticle() + " " +
                //        "will help the shortage in the %s.", opad, Misc.getHighlightColor(), new String[] { ORBITALWORKS }
                //    );
                panel.beginGridFlipped(width, 1, 40f, 10f);
                panel.addToGrid(0, 0, "Commodity value", "" + (int)(SpecialDataTrader.this.valueMult * 100f) + "%");
                panel.addGrid(pad);
                panel.addPara(
                        "Compared to dealing with open market, selling it here will net you %s as payment " +
                                "and your relationship with %s will improve by %s points.",
                        opad * 1f, green,
                        Misc.getWithDGS(bounty) + Strings.C, SFC_VELMARIE + "", "" + (int) repChange
                );
            }
        });
    }

    protected float computeMetalsCreditValue(final CargoAPI cargo) {
        float bounty = 0;
        for (final CargoStackAPI stack : cargo.getStacksCopy()) {
            final CommoditySpecAPI spec = stack.getResourceIfResource();
            final String id = spec.getId();
            if (FUCKING_METALS.contains(id)) {
                bounty += spec.getBasePrice() * stack.getSize();
            }
            if (Global.getSettings().getModManager().isModEnabled(INDEVO)) {
                if (INDEVO_RARE_PARTS.contains(id)) {
                    bounty += spec.getBasePrice() * stack.getSize();
                }
            }
        }
        bounty *= valueMult;
        return bounty;
    }

    protected float computeMetalsReputationValue(final CargoAPI cargo) {
        float rep = 0;
        for (final CargoStackAPI stack : cargo.getStacksCopy()) {
            final CommoditySpecAPI spec = stack.getResourceIfResource();
            final String id = spec.getId();
            if (FUCKING_METALS.contains(id)) {
                rep += getBaseRepValue(spec.getId()) * stack.getSize();
            }
            if (Global.getSettings().getModManager().isModEnabled(INDEVO)) {
                if (INDEVO_RARE_PARTS.contains(id)) {
                    rep += getBaseRepValue(spec.getId()) * stack.getSize();
                }
            }
        }
        rep *= repMult;
        return rep;
    }

    public static float getBaseRepValue(final String MetalsType) {
        if (Commodities.METALS.equals(MetalsType)) {
            return 0.1f;
        }
        if (Commodities.RARE_METALS.equals(MetalsType)) {
            return 0.1f;
        }
        if (Global.getSettings().getModManager().isModEnabled(INDEVO)) {
            if (INDEVO_RARE_PARTS.equals(MetalsType)) {
                return 0.1f;
            }
        }
        return 0.1f;
    }

    protected boolean playerHasMetals() {
        for (final CargoStackAPI stack : playerCargo.getStacksCopy()) {
            final CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec != null && spec.getDemandClass().equals(Commodities.METALS)){
                return true;
            }
            if (spec != null && spec.getDemandClass().equals(Commodities.RARE_METALS)){
                return true;
            }
            if (Global.getSettings().getModManager().isModEnabled(INDEVO)) {
                if (spec != null && spec.getDemandClass().equals(INDEVO_RARE_PARTS)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isMetals(final CargoStackAPI stack) {
        final CommoditySpecAPI spec = stack.getResourceIfResource();
        if (spec == null) {
            return false;
        }
        if (Global.getSettings().getModManager().isModEnabled(INDEVO)) {
            if (spec.getDemandClass().equals(INDEVO_RARE_PARTS)){
                return true; //com.fs.starfarer.api.impl.campaign.ids.IndEvo_Items.RARE_PARTS
            }
        }
        return spec.getDemandClass().equals(Commodities.METALS) && !spec.getDemandClass().equals(Commodities.RARE_METALS);
        //!(!spec.getDemandClass().equals(Commodities.METALS) && !spec.getDemandClass().equals(Commodities.RARE_METALS));
    }

}
*/