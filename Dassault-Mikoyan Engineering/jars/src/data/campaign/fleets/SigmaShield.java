package data.campaign.fleets;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.istl_Factions;



public class SigmaShield extends BaseIndustry {

	public static float DEFENSE_BONUS = 4f;
	
        @Override
	public boolean isHidden() {
		return !market.getFactionId().equals(istl_Factions.BREAKERS);
	}
	
	@Override
	public boolean isFunctional() {
		return super.isFunctional() && market.getFactionId().equals(istl_Factions.BREAKERS);
	}
        
	public void apply() {
		super.apply(false);
		
		int size = 5;
		applyIncomeAndUpkeep(size);
		
		float bonus = DEFENSE_BONUS;
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
						.modifyMult(getModId(), 1f + bonus, getNameForModifier());
		
		if (isFunctional()) {
			applyVisuals(market.getPlanetEntity());
		} else {
			unapply();
		}
	}
	
	public static void applyVisuals(PlanetAPI planet) {
		if (planet == null) return;
		planet.getSpec().setShieldTexture(Global.getSettings().getSpriteName("industry", "istl_shield_texture"));
		//planet.getSpec().setShieldThickness(0.07f);
		//planet.getSpec().setShieldColor(new Color(255,0,0,255));
		planet.getSpec().setShieldThickness(0.06f);
		planet.getSpec().setShieldColor(new Color(255,255,255,100));
		planet.applySpecChanges();
	}
	
	public static void unapplyVisuals(PlanetAPI planet) {
		if (planet == null) return;
		planet.getSpec().setShieldTexture(null);
		planet.getSpec().setShieldThickness(0f);
		planet.getSpec().setShieldColor(null);
		planet.applySpecChanges();
	}

	
	@Override
	public void unapply() {
		super.unapply();
		
		unapplyVisuals(market.getPlanetEntity());
		
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
	}

	@Override
	public boolean isAvailableToBuild() {
		return false;
	}
	
	public boolean showWhenUnavailable() {
		return Global.getSector().getPlayerFaction().knowsIndustry(getId());
	}

	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			
			float bonus = DEFENSE_BONUS;
			addGroundDefensesImpactSection(tooltip, bonus, (String[])null);
		}
	}
        
        @Override
	protected int getBaseStabilityMod() {
		return 5;
	}
	
	public static float ALPHA_CORE_BONUS = 0.5f;
	@Override
	protected void applyAlphaCoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(
				getModId(1), 1f + ALPHA_CORE_BONUS, "Alpha core (" + getNameForModifier() + ")");
	}
	
	@Override
	protected void applyNoAICoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId(1));
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
		//String str = Strings.X + (int)Math.round(a * 100f) + "%";
		String str = Strings.X + (1f + a) + "";
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
					"Increases ground defenses by %s.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
					str);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
				"Increases ground defenses by %s.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
				str);
		
	}
}
