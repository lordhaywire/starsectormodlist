package data.campaign.econ.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI.MarketInteractionMode;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import data.campaign.econ.impl.XLU_NanoforgePlugin.NanoforgeEffect;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public class xlu_battle_yards extends BaseIndustry {

	public static float DEFENSE_BONUS_BATTLE_YARDS = 0.25f;
	public static float FLEET_BONUS_BATTLE_YARDS = 0.33f;
        public boolean hasHeavy = false;
        public boolean hasPort = false;
        
	
//	@Override
//	public boolean isHidden() {
//		return !market.getFactionId().equals("xlu");
//	}
	
//	@Override
//	public boolean isFunctional() {
//		return super.isFunctional() && market.getFactionId().equals("xlu");
//	}

        @Override
	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		
		int shipBonus = 0;
		float qualityBonus = 0.25f;
		
		demand(Commodities.METALS, size + 1);
		demand(Commodities.RARE_METALS, size - 1);
		
		supply(Commodities.HEAVY_MACHINERY, size - 1);
		supply(Commodities.SUPPLIES, size - 1);
		supply(Commodities.HAND_WEAPONS, size - 1);
		supply(Commodities.SHIPS, size - 1);
		if (shipBonus > 0) {
			supply(1, Commodities.SHIPS, shipBonus, "Lanestate battleyards");
		}
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.METALS, Commodities.RARE_METALS);
		int maxDeficit = size - 3; // to allow *some* production so economy doesn't get into an unrecoverable state
		if (deficit.two > maxDeficit) deficit.two = maxDeficit;
		
		applyDeficitToProduction(2, deficit,
					Commodities.HEAVY_MACHINERY,
					Commodities.SUPPLIES,
					Commodities.HAND_WEAPONS,
					Commodities.SHIPS);
		
		applyNanoforgeEffects();

		float mult = getDeficitMult(Commodities.SUPPLIES);
		String extra = "";
		if (mult != 1) {
			String com = getMaxDeficit(Commodities.SUPPLIES).one;
			extra = " (" + getDeficitText(com).toLowerCase() + ")";
		}
                
		float bonus = DEFENSE_BONUS_BATTLE_YARDS;
		float bonus_fleet = FLEET_BONUS_BATTLE_YARDS;
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
						.modifyMult(getModId(), 1f + bonus * mult, getNameForModifier() + extra);
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT)
                                                .modifyMult(getModId(), 1f + bonus_fleet * mult, getNameForModifier() + extra);
//		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SPAWN_RATE_MULT)
//                                                .modifyMult(getModId(), 1f + bonus_fleet * mult, getNameForModifier() + extra);
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(), qualityBonus, "Lanestate battleyards");
		
		float stability = market.getPrevStability();
		if (stability < 5) {
			float stabilityMod = (stability - 5f) / 5f;
			stabilityMod *= 0.5f;
			//market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(0), stabilityMod, "Low stability at production source");
			market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(), stabilityMod, getNameForModifier() + " - low stability");
		}
		
		if (!isFunctional()) {
                        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(getModId());
			supply.clear();
			unapply();
		}
	}

	@Override
	public void unapply() {
		super.unapply();
		
		if (nanoforge != null) {
			NanoforgeEffect effect = XLU_NanoforgePlugin.NANOFORGE_EFFECTS.get(nanoforge.getId());
			if (effect != null) {
				effect.unapply(this);
			}
		}
		
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId());
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(getModId(0));
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId(1));
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId(2));
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId(3));
//		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).unmodifyMult(getModId());
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyMult(getModId());
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(0));
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyMult(getModId(1));
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyMult(getModId(2));
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyMult(getModId(3));
//		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(0));
	}

	
	@Override
	protected int getBaseStabilityMod() {
		return 1;
	}
	
	
	@Override
	protected void upgradeFinished(Industry previous) {
		super.upgradeFinished(previous);
		
		if (previous instanceof xlu_battle_yards) {
			setNanoforge(((xlu_battle_yards) previous).getNanoforge());
		}
	}

	protected void applyNanoforgeEffects() {
//		if (Global.getSector().getEconomy().isSimMode()) {
//			return;
//		}
		
		if (nanoforge != null) {
			NanoforgeEffect effect = XLU_NanoforgePlugin.NANOFORGE_EFFECTS.get(nanoforge.getId());
			if (effect != null) {
				effect.apply(this);
			}
		}
	}

	protected SpecialItemData nanoforge = null;
	public void setNanoforge(SpecialItemData nanoforge) {
		if (nanoforge == null && this.nanoforge != null) {
			NanoforgeEffect effect = XLU_NanoforgePlugin.NANOFORGE_EFFECTS.get(this.nanoforge.getId());
			if (effect != null) {
				effect.unapply(this);
			}
		}
		this.nanoforge = nanoforge;
	}

	public SpecialItemData getNanoforge() {
		return nanoforge;
	}
	
	public SpecialItemData getSpecialItem() {
		return nanoforge;
	}
	
	public void setSpecialItem(SpecialItemData special) {
		nanoforge = special;
	}
	
	@Override
	public boolean wantsToUseSpecialItem(SpecialItemData data) {
		if (nanoforge != null && Items.CORRUPTED_NANOFORGE.equals(nanoforge.getId()) &&
				data != null && Items.PRISTINE_NANOFORGE.equals(data.getId())) {
			return true;
		}
		
		return nanoforge == null && 
				data != null &&
				XLU_NanoforgePlugin.NANOFORGE_EFFECTS.containsKey(data.getId());
	}
	
	@Override
	protected void addPostSupplySection(TooltipMakerAPI tooltip, boolean hasSupply, IndustryTooltipMode mode) {
		super.addPostSupplySection(tooltip, hasSupply, mode);
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			addStabilityPostDemandSection(tooltip, hasDemand, mode);
			float bonus = DEFENSE_BONUS_BATTLE_YARDS;
			addGroundDefensesImpactSection(tooltip, bonus, Commodities.SUPPLIES);
		}
	}
	
	@Override
	public void notifyBeingRemoved(MarketInteractionMode mode, boolean forUpgrade) {
		super.notifyBeingRemoved(mode, forUpgrade);
		if (nanoforge != null && !forUpgrade) {
			CargoAPI cargo = getCargoForInteractionMode(mode);
			if (cargo != null) {
				cargo.addSpecial(nanoforge, 1);
			}
		}
	}

        @Override
	public boolean isAvailableToBuild() {
		//if (!super.isAvailableToBuild()) return false;
                //SectorAPI sector = Global.getSector();
        
                //FactionAPI player = sector.getFaction(Factions.PLAYER);
                //FactionAPI XLU = sector.getFaction("xlu");
            
		boolean canBuild = false;
		for (Industry ind : market.getIndustries()) {
			if (ind == this) continue;
			if (!ind.isFunctional()) continue;
			if ((ind.getSpec().hasTag(Industries.TAG_SPACEPORT)) && (Global.getSector().getPlayerFaction().knowsIndustry(getId()))) {
				canBuild = true;
				break;
			}
		}
                
                if (!Global.getSector().getPlayerFaction().knowsIndustry(getId()) && (!hasPort || (market.hasIndustry(Industries.HEAVYINDUSTRY)) || (market.hasIndustry(Industries.ORBITALWORKS)) || (market.hasIndustry("ms_modularFac")) || (market.hasIndustry("ms_massIndustry")))) {
                    return false;
                }
                
            //return market.getPlanetEntity() != null;
            return canBuild;
	}
                //if (market.getPlanetEntity() != null && (market.hasIndustry(Industries.HEAVYINDUSTRY) ||
                //        market.hasIndustry(Industries.ORBITALWORKS) || market.hasIndustry("ms_modularFac"))) { // In case Shadowyards has any ideas...
                //    hasHeavy = true;
                //}
                    

                //if (!hasPort || (player.getRelationshipLevel(XLU).isAtWorst(RepLevel.WELCOMING) ||
                //    Global.getSector().getPlayerFaction().knowsIndustry(getId()))) {
                //    return false;
                //} else if (hasPort && (player.getRelationshipLevel(XLU).isAtWorst(RepLevel.WELCOMING) ||
                //    Global.getSector().getPlayerFaction().knowsIndustry(getId()))) {
                //    return true;
                //}
        
	@Override
	public String getUnavailableReason() {
		if (!super.isAvailableToBuild()) return super.getUnavailableReason();
                
                if (!hasPort ) {
                    return "Requires a functional spaceport";
                }
                else{
                    return "Not available";
                }
	}
        
	public boolean showWhenUnavailable() {
		return Global.getSector().getPlayerFaction().knowsIndustry(getId());
	}

	@Override
	protected boolean addNonAICoreInstalledItems(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
		if (nanoforge == null) return false;

		float opad = 10f;

		FactionAPI faction = market.getFaction();
		Color color = faction.getBaseUIColor();
		Color dark = faction.getDarkUIColor();
		
		
		SpecialItemSpecAPI nanoforgeSpec = Global.getSettings().getSpecialItemSpec(nanoforge.getId());
		
		TooltipMakerAPI text = tooltip.beginImageWithText(nanoforgeSpec.getIconName(), 48);
		NanoforgeEffect effect = XLU_NanoforgePlugin.NANOFORGE_EFFECTS.get(nanoforge.getId());
		effect.addItemDescription(text, nanoforge, InstallableItemDescriptionMode.INDUSTRY_TOOLTIP);
		tooltip.addImageWithText(opad);
		
		return true;
	}

	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}

	@Override
	public List<InstallableIndustryItemPlugin> getInstallableItems() {
		ArrayList<InstallableIndustryItemPlugin> list = new ArrayList<InstallableIndustryItemPlugin>();
		list.add(new XLU_NanoforgePlugin(this));
		return list;
	}

	@Override
	public void initWithParams(List<String> params) {
		super.initWithParams(params);
		
		for (String str : params) {
			if (XLU_NanoforgePlugin.NANOFORGE_EFFECTS.containsKey(str)) {
				setNanoforge(new SpecialItemData(str, null));
				break;
			}
		}
	}
        
	@Override
	protected boolean canImproveToIncreaseProduction() {
		return true;
	}
	
	protected void applyAICoreModifiers() {
		if (aiCoreId == null) {
			applyNoAICoreModifiers();
			return;
		}
		boolean potato = aiCoreId.equals(Commodities.OMEGA_CORE); 
		boolean alpha = aiCoreId.equals(Commodities.ALPHA_CORE); 
		boolean beta = aiCoreId.equals(Commodities.BETA_CORE); 
		boolean gamma = aiCoreId.equals(Commodities.GAMMA_CORE);
		if (potato) applyPotatoCoreModifiers();
		else if (alpha) applyAlphaCoreModifiers();
		else if (beta) applyBetaCoreModifiers();
		else if (gamma) applyGammaCoreModifiers();
	}
        
	public static float POTATO_UPKEEP_MULT = 0.67f;
	protected void applyAICoreToIncomeAndUpkeep() {
		if (aiCoreId == null || Commodities.GAMMA_CORE.equals(aiCoreId)) {
			getUpkeep().unmodifyMult("ind_core");
			return;
		}
		
		float mult = UPKEEP_MULT;
		String name = "AI Core assigned";
		if (aiCoreId.equals(Commodities.OMEGA_CORE)) {
			name = "Omega Core assigned";
                        mult = POTATO_UPKEEP_MULT;
		} else if (aiCoreId.equals(Commodities.ALPHA_CORE)) {
			name = "Alpha Core assigned";
		} else if (aiCoreId.equals(Commodities.BETA_CORE)) {
			name = "Beta Core assigned";
		} else if (aiCoreId.equals(Commodities.GAMMA_CORE)) {
			name = "Gamma Core assigned";
		}
		
		getUpkeep().modifyMult("ind_core", mult, name);
	}
	
	protected void updateAICoreToSupplyAndDemandModifiers() {
		if (aiCoreId == null) {
			return;
		}
		
		boolean potato = aiCoreId.equals(Commodities.OMEGA_CORE); 
		boolean alpha = aiCoreId.equals(Commodities.ALPHA_CORE); 
		boolean beta = aiCoreId.equals(Commodities.BETA_CORE); 
		boolean gamma = aiCoreId.equals(Commodities.GAMMA_CORE);
		
		if (potato) {
			applyPotatoCoreSupplyAndDemandModifiers();
                } else if (alpha) {
			applyAlphaCoreSupplyAndDemandModifiers();
		} else if (beta) {
			applyBetaCoreSupplyAndDemandModifiers();
		} else if (gamma) {
			applyGammaCoreSupplyAndDemandModifiers();
		}
	}
	
	public static float POTATO_CORE_BONUS = 0.25f;
	public static float POTATO_CORE_DEFENSE_BONUS = 0.15f;
	public static int POTATO_DEMAND_REDUCTION = 2;
	public static int POTATO_SUPPLY_BONUS = 2;
        
	protected void applyPotatoCoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(
				getModId(0), 1f + POTATO_CORE_BONUS, "Omega core (" + getNameForModifier() + ")");
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(
				getModId(0), 1f + POTATO_CORE_DEFENSE_BONUS, "Omega core (" + getNameForModifier() + ")");
	}
	
	protected void applyPotatoCoreSupplyAndDemandModifiers() {
		supplyBonus.modifyFlat(getModId(0), POTATO_SUPPLY_BONUS, "Omega core");
		demandReduction.modifyFlat(getModId(0), POTATO_DEMAND_REDUCTION, "Omega core");
	}
	
	public static float ALPHA_CORE_BONUS = 0.15f;
	protected void applyAlphaCoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(
				getModId(0), 1f + ALPHA_CORE_BONUS, "Alpha core (" + getNameForModifier() + ")");
	}
	
	protected void applyAlphaCoreSupplyAndDemandModifiers() {
		supplyBonus.modifyFlat(getModId(0), SUPPLY_BONUS, "Alpha core");
		demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Alpha core");
	}
	
	protected void applyNoAICoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId(0));
	}
	
	public void addAICoreSection(TooltipMakerAPI tooltip, String coreId, AICoreDescriptionMode mode) {
		float opad = 10f;

		FactionAPI faction = market.getFaction();
		Color color = faction.getBaseUIColor();
		Color dark = faction.getDarkUIColor();
		
//		if (mode == AICoreDescriptionMode.TOOLTIP) {
//			tooltip.addSectionHeading("AI Core", color, dark, Alignment.MID, opad);
//		}
		
//		if (mode == AICoreDescriptionMode.TOOLTIP || mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
		if (mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
			if (coreId == null) {
				tooltip.addPara("No AI core currently assigned. Click to assign an AI core from your cargo.", opad);
				return;
			}
		}
		
		boolean potato = coreId.equals(Commodities.OMEGA_CORE); 
		boolean alpha = coreId.equals(Commodities.ALPHA_CORE); 
		boolean beta = coreId.equals(Commodities.BETA_CORE); 
		boolean gamma = coreId.equals(Commodities.GAMMA_CORE);
		
		
		if (potato) {
			addPotatoCoreDescription(tooltip, mode);
		} else if (alpha) {
			addAlphaCoreDescription(tooltip, mode);
		} else if (beta) {
			addBetaCoreDescription(tooltip, mode);
		} else if (gamma) {
			addGammaCoreDescription(tooltip, mode);
		}
	}
        
	protected void addPotatoCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Omega-level AI core currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Omega-level AI core. ";
		}
		float a = POTATO_CORE_BONUS;
		//String str = "" + (int)Math.round(a * 100f) + "%";
		String str = Strings.X + (1f + a);
		String def_str = Strings.X + (1f + a);
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
					"Increases production by %s unit." + "Increases fleet size by %s." +
                                        "Increases defense strength by %s.",
                                        0f, highlight,
					"" + (int)((1f - POTATO_UPKEEP_MULT) * 100f) + "%", 
                                        "" + POTATO_DEMAND_REDUCTION,
					"" + POTATO_SUPPLY_BONUS,
					str,
                                        def_str);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
				"Increases production by %s unit." + "Increases fleet size by %s." +
                                        "Increases defense strength by %s.",
                                opad, highlight,
				"" + (int)((1f - POTATO_UPKEEP_MULT) * 100f) + "%", 
                                "" + POTATO_DEMAND_REDUCTION,
				"" + POTATO_SUPPLY_BONUS,
				str,
                                def_str);
		
	}
	
	protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Alpha-level AI core currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Alpha-level AI core. ";
		}
		float a = ALPHA_CORE_BONUS;
		//String str = "" + (int)Math.round(a * 100f) + "%";
		String str = Strings.X + (1f + a);
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
					"Increases production by %s unit." + "Increases fleet size by %s.",
                                        0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", 
                                        "" + DEMAND_REDUCTION,
					"" + SUPPLY_BONUS,
					str);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
				"Increases production by %s unit." + "Increases fleet size by %s.",
                                opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%",
                                "" + DEMAND_REDUCTION,
				"" + SUPPLY_BONUS,
				str);
		
	}
	
	@Override
	public List<SpecialItemData> getVisibleInstalledItems() {
		List<SpecialItemData> result = super.getVisibleInstalledItems();
		
		if (nanoforge != null) {
			result.add(nanoforge);
		}
		
		return result;
	}
	
	public float getPatherInterest() {
		float base = 1f;
		if (nanoforge != null) base += 2f;
		return 1f + super.getPatherInterest();
	}
	
}
