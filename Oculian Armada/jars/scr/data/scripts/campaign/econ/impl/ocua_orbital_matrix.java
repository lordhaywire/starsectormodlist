package data.scripts.campaign.econ.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.GenericInstallableItemPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public class ocua_orbital_matrix extends BaseIndustry {
    
	public static float ORBITAL_MATRIX_QUALITY_BONUS = 0.50f;

	public static float DEFENSE_BONUS_STATIONARY_MATRIX = 0.10f;
	public static float FLEET_BONUS_STATIONARY_MATRIX = 0.50f;
        public boolean hasHeavy = false;
        public boolean hasPort = false;
        
        @Override
	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		
		int shipBonus = 0;
		float qualityBonus = ORBITAL_MATRIX_QUALITY_BONUS;
		
		demand(Commodities.METALS, size - 2);
		demand(Commodities.RARE_METALS, size - 3);
		
		supply(Commodities.HEAVY_MACHINERY, size - 2);
		supply(Commodities.SUPPLIES, size - 2);
		supply(Commodities.HAND_WEAPONS, size - 2);
		supply(Commodities.SHIPS, size - 1);
		if (shipBonus > 0) {
			supply(1, Commodities.SHIPS, shipBonus, "Stationary Assembly Matrix");
		}
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.METALS, Commodities.RARE_METALS);
		int maxDeficit = size - 3; // to allow *some* production so economy doesn't get into an unrecoverable state
		if (deficit.two > maxDeficit) deficit.two = maxDeficit;
		
		applyDeficitToProduction(2, deficit,
					Commodities.HEAVY_MACHINERY,
					Commodities.SUPPLIES,
					Commodities.HAND_WEAPONS,
					Commodities.SHIPS);
		
		float mult = getDeficitMult(Commodities.SUPPLIES);
		String extra = "";
		if (mult != 1) {
			String com = getMaxDeficit(Commodities.SUPPLIES).one;
			extra = " (" + getDeficitText(com).toLowerCase() + ")";
		}
                
		float bonus = DEFENSE_BONUS_STATIONARY_MATRIX;
		float bonus_fleet = FLEET_BONUS_STATIONARY_MATRIX;
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
						.modifyMult(getModId(), 1f + bonus * mult, getNameForModifier() + extra);
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT)
                                                .modifyMult(getModId(), 1f + bonus_fleet * mult, getNameForModifier() + extra);
//		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SPAWN_RATE_MULT)
//                                                .modifyMult(getModId(), 1f + bonus_fleet * mult, getNameForModifier() + extra);
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(), qualityBonus, "Orbital Assembly Matrix");
		
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
		
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId());
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(getModId(0));
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId(1));
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId(2));
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId(3));
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyMult(getModId());
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(0));
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyMult(getModId(1));
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyMult(getModId(2));
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyMult(getModId(3));
	}

	@Override
	protected boolean canImproveToIncreaseProduction() {
		return true;
	}
	
	/*@Override
	public List<InstallableIndustryItemPlugin> getInstallableItems() {
		ArrayList<InstallableIndustryItemPlugin> list = new ArrayList<InstallableIndustryItemPlugin>();
		list.add(new GenericInstallableItemPlugin(this));
		return list;
	}
        
	@Override
	public boolean wantsToUseSpecialItem(SpecialItemData data) {
		if (special != null && Items.CORRUPTED_NANOFORGE.equals(special.getId()) &&
				data != null && Items.PRISTINE_NANOFORGE.equals(data.getId())) {
			return true;
		}
		return super.wantsToUseSpecialItem(data);
	}

	@Override
	public void setSpecialItem(SpecialItemData special) {
		super.setSpecialItem(special);
	}*/
	
	@Override
	protected void addPostSupplySection(TooltipMakerAPI tooltip, boolean hasSupply, IndustryTooltipMode mode) {
		super.addPostSupplySection(tooltip, hasSupply, mode);
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			//addStabilityPostDemandSection(tooltip, hasDemand, mode);
			float total = ORBITAL_MATRIX_QUALITY_BONUS;
			float def_bonus = DEFENSE_BONUS_STATIONARY_MATRIX;	
			float fleet_bonus = FLEET_BONUS_STATIONARY_MATRIX;
                        
			String totalDef = "+" + (int)Math.round(def_bonus * 100f) + "%";
			String totalFleet = "+" + (int)Math.round(fleet_bonus * 100f) + "%";
			String totalStr = "+" + (int)Math.round(total * 100f) + "%";
			Color h = Misc.getHighlightColor();
			Color h2 = Misc.getHighlightColor();
			Color h3 = Misc.getHighlightColor();
			float pad = 3f;
			float opad = 10f;
			if (def_bonus < 0) {
				h = Misc.getNegativeHighlightColor();
				totalDef = "" + (int)Math.round(def_bonus * 100f) + "%";
			}
			if (fleet_bonus < 0) {
				h2 = Misc.getNegativeHighlightColor();
				totalFleet = "" + (int)Math.round(fleet_bonus * 100f) + "%";
			}
			if (total < 0) {
				h3 = Misc.getNegativeHighlightColor();
				totalStr = "" + (int)Math.round(total * 100f) + "%";
			}
                        
			if (def_bonus >= 0) tooltip.addPara("Ground Defenses: %s", opad, h, totalDef);
			if (fleet_bonus >= 0) tooltip.addPara("Fleet Size: %s", pad, h2, totalFleet);
			if (total >= 0) {
				tooltip.addPara("Ship quality: %s", pad, h3, totalStr);
				tooltip.addPara("*Quality bonus only applies for the largest ship producer in the faction.", 
						Misc.getGrayColor(), opad);
			}
		}
	}
	
	@Override
	public boolean isAvailableToBuild() {
		boolean canBuild = false;
		for (Industry ind : market.getIndustries()) {
			if (ind == this) continue;
			if (!ind.isFunctional()) continue;
			if ((ind.getSpec().hasTag(Industries.TAG_SPACEPORT)) && (Global.getSector().getPlayerFaction().knowsIndustry(getId()))) {
				canBuild = true;
				break;
			}
		}
                
                if (!Global.getSector().getPlayerFaction().knowsIndustry(getId()) && 
                        (!hasPort || (market.hasIndustry(Industries.HEAVYINDUSTRY)) || 
                        (market.hasIndustry(Industries.ORBITALWORKS)) || 
                        (market.hasIndustry("ms_modularFac")) || 
                        (market.hasIndustry("ms_massIndustry")) || 
                        (market.hasIndustry("xlu_battleyards")))) {
                    return false;
                }
                
		return true;
	}
	
	@Override
	public String getUnavailableReason() {
            if (!super.isAvailableToBuild()) return super.getUnavailableReason();
                
            if (!hasPort) return "Requires a functional spaceport";
            if (!(market.hasIndustry(Industries.HEAVYINDUSTRY)) || 
                        (market.hasIndustry(Industries.ORBITALWORKS)) || 
                        (market.hasIndustry("ms_modularFac")) || 
                        (market.hasIndustry("ms_massIndustry")) || 
                        (market.hasIndustry("xlu_battleyards"))) return "Requires has existing Heavy Industry";
            return "Not available";
	}
        
	public boolean showWhenUnavailable() {
		return Global.getSector().getPlayerFaction().knowsIndustry(getId());
	}

	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public float getPatherInterest() {
		float base = -4f;
		return base + super.getPatherInterest();
	}
	
}
