package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

public class sfclobsterresort extends BaseIndustry {

    public static float BASE_BONUS = 25f;
    public static float ALPHA_CORE_BONUS = 25f;
    public static float IMPROVE_BONUS = 25f;

    //public static float STABILITY_BONUS = 2f;

    //protected transient SubmarketAPI saved = null;

    public void apply() {
        super.apply(true);

        int size = market.getSize();

        demand(Commodities.LOBSTER, size - 4);
        demand(Commodities.ORGANICS, size - 2);

        supply(Commodities.LUXURY_GOODS, size - 4);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.LOBSTER);
        applyDeficitToProduction(1, deficit, Commodities.LUXURY_GOODS);

        modifyStabilityWithBaseMod();

        //market.getStability().modifyFlat(getModId(), STABILITY_BONUS, getNameForModifier());

        market.getIncomeMult().modifyPercent(getModId(0), BASE_BONUS, getNameForModifier());

        if (!isFunctional()) {
            unapply();
        }
    }


    @Override
    public void unapply() {
        super.unapply();
        //market.getStability().unmodifyFlat(getModId());
        market.getIncomeMult().unmodifyPercent(getModId(0));
        unmodifyStabilityWithBaseMod();
    }
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return mode != IndustryTooltipMode.NORMAL || isFunctional();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
            addStabilityPostDemandSection(tooltip, hasDemand, mode);
        }
    }

    @Override
    protected int getBaseStabilityMod() {
        return 1;
    }

    protected void addStabilityPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        Color h = Misc.getHighlightColor();
        float opad = 10f;

        float a = BASE_BONUS;
        String aStr = "+" + (int)Math.round(a * 1f) + "%";
        tooltip.addPara("Colony income: %s", opad, h, aStr);
    }


    @Override
    public boolean isAvailableToBuild() {
        return false;
    }

    public boolean showWhenUnavailable() {
        return false;
    }

    @Override
    public String getCurrentImage() {
        return super.getCurrentImage();
    }

    @Override
    protected void applyAlphaCoreModifiers() {
        market.getIncomeMult().modifyPercent(getModId(1), ALPHA_CORE_BONUS, "Alpha core (" + getNameForModifier() + ")");
    }

    @Override
    protected void applyNoAICoreModifiers() {
        market.getIncomeMult().unmodifyPercent(getModId(1));
    }

    @Override
    protected void applyAlphaCoreSupplyAndDemandModifiers() {
        demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Alpha core");
    }

    protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }
        float a = ALPHA_CORE_BONUS;
        String str = "" + (int) Math.round(a) + "%";

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
                            "Increases colony income by %s.", 0f, highlight,
                    "" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
                    str);
            tooltip.addImageWithText(opad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
                        "Increases colony income by %s.", opad, highlight,
                "" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
                str);

    }


    @Override
    public boolean canImprove() {
        return true;
    }

    protected void applyImproveModifiers() {
        if (isImproved()) {
            market.getIncomeMult().modifyPercent(getModId(2), IMPROVE_BONUS,
                    getImprovementsDescForModifiers() + " (" + getNameForModifier() + ")");
        } else {
            market.getIncomeMult().unmodifyPercent(getModId(2));
        }
    }

    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        float a = IMPROVE_BONUS;
        String aStr = "" + (int)Math.round(a * 1f) + "%";

        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
            info.addPara("Colony income increased by %s.", 0f, highlight, aStr);
        } else {
            info.addPara("Increases colony income by %s.", 0f, highlight, aStr);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}





