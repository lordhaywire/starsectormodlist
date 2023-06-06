package com.fs.starfarer.api.impl.campaign.econ.impl;

import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseInstallableIndustryItemPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.impl.campaign.ids.sfc_items;


/*
public class sfc_specialItemsEffectRepo {

    public static int AQUATIC_STIMULATOR_BONUS = 1;
    public static Map<String, InstallableItemEffect> ITEM_EFFECTS = new HashMap<String, InstallableItemEffect>() {
        {
            put("sfc_aquaticstimulator", new BoostIndustryInstallableItemEffect(
                    "sfc_aquaticstimulator", AQUATIC_STIMULATOR_BONUS, 0) {
                protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
                                                      InstallableItemDescriptionMode mode, String pre, float pad) {
                    text.addPara(pre + "Increases aquaculture production by %s unit.",
                            pad, Misc.getHighlightColor(),
                            "" + (int) AQUATIC_STIMULATOR_BONUS);
                }
                @Override
                public String[] getSimpleReqs(Industry industry) {
                    return new String[]{NO_TRANSPLUTONIC_ORE_DEPOSITS, NO_VOLATILES_DEPOSITS};
                }
            });
        }
    };
}*/

public class sfc_specialItemsEffectRepo {
    public static final int SFC_ITEM_PROD_BONUS = 2;
    public static final int SFC_ITEM_PROD_LOWER_BONUS = 1;
    public static String NOT_A_GAS_GIANT = "not a gas giant";
    public static void addItemEffectsToVanillaRepo() {
        ItemEffectsRepo.ITEM_EFFECTS.putAll(ITEM_EFFECTS);
    }

    public static final Map<String, InstallableItemEffect> ITEM_EFFECTS = new HashMap<String, InstallableItemEffect>() {{

        /*put(sfc_items.AQUATIC_STIMULATOR, new BaseInstallableItemEffect(sfc_items.AQUATIC_STIMULATOR) {

            @Override
            public void apply(final Industry industry) {
                industry.supply(id + "_0", Commodities.FOOD, SFC_ITEM_PROD_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
                industry.supply(id + "_0", Commodities.LOBSTER, SFC_ITEM_PROD_LOWER_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
            }

            @Override
            public void unapply(final Industry industry) {
                industry.getSupply(Commodities.FOOD).getQuantity().unmodifyFlat(id + "_0");
                industry.getSupply(Commodities.LOBSTER).getQuantity().unmodifyFlat(id + "_0");
            }

            @Override
            protected void addItemDescriptionImpl(final Industry industry, final TooltipMakerAPI text, final SpecialItemData data, final InstallableIndustryItemPlugin.InstallableItemDescriptionMode mode, final String pre, final float pad) {
                //if(industry == null){
                text.addPara(pre + "Increases Aquaculture production by %s units, and adds production of Volturnain Lobsters.",
                        pad, Misc.getHighlightColor(),
                        "" + (int) SFC_ITEM_PROD_BONUS );
                // }
            }});*/

        put(sfc_items.AQUATIC_STIMULATOR, new BoostIndustryInstallableItemEffect(
                sfc_items.AQUATIC_STIMULATOR, SFC_ITEM_PROD_BONUS, 0) {

            @Override
            protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
                                                  InstallableIndustryItemPlugin.InstallableItemDescriptionMode mode, String pre, float pad) {
                text.addPara(pre + "Increases aquaculture production by %s units.",
                        pad, Misc.getHighlightColor(),
                        "" + (int) SFC_ITEM_PROD_BONUS);
            }
        });

        put(sfc_items.MOTE_MEGACONDENSER, new BaseInstallableItemEffect(sfc_items.MOTE_MEGACONDENSER) {

            @Override
            public void apply(final Industry industry) {
                industry.supply(id + "_0", Commodities.VOLATILES, SFC_ITEM_PROD_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
            }

            @Override
            public void unapply(final Industry industry) {
                industry.getSupply(Commodities.VOLATILES).getQuantity().unmodifyFlat(id + "_0");
            }

            @Override
            protected void addItemDescriptionImpl(final Industry industry, final TooltipMakerAPI text, final SpecialItemData data, final InstallableIndustryItemPlugin.InstallableItemDescriptionMode mode, final String pre, final float pad) {
                text.addPara(pre + "Increases Mining Volatiles production by %s unit(s).",
                        pad, Misc.getHighlightColor(),
                        "" + (int) SFC_ITEM_PROD_BONUS );}
            @Override
            public String[] getSimpleReqs(Industry industry) {
                return new String [] {NOT_A_GAS_GIANT};
            }

        });

    }};
}