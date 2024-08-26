package data.scripts.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript.MilitaryResponseParams;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignClock;
import com.fs.starfarer.campaign.Faction;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;

import data.scripts.campaign.econ.acs_frontierecon;

import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;


public class acs_industry_frontier_protocol extends BaseIndustry {

	public static float acs_FLEET_SIZE_BONUS_DEFAULT = 0.1f;
	public static float acs_FLEET_SIZE_BONUS_IMPROVE = 0.15f;
	public static float acs_FLEET_SIZE_BONUS_ALPHA = 0.25f;
	public final String ACS_INDUSTRY_NAME = "acs_industry_frontier_protocol";

	public static float acs_IMMIGRATION_PENALTY = 80f;
	// public static int acs_hull = market.getDeman
	public static int acs_STABILITY_BONUS = 1;
	// public boolean HASPASS = true;
	static final public String OUR_FACTION = "independent";

	// public boolean HASSPASSED = true;

	// String a = market.getId();

	// boolean setEcon = market.isInEconomy();

	// public boolean setEcon(boolean market2a){
	// 	return market2 = market2a;
	// };

	// public boolean setEconomy(MarketAPI market){
	// 	if (!market.isInEconomy()) {
	// 		return market.isInEconomy();
	// 	}
	// 	return false;
	// };

	// public void commodityUpdated(String commodityId) {
	// 	CommodityOnMarketAPI com = market.getCommodityData(commodityId);
	// 	int curr = 0;
	// 	String modId = market.getId();
	// 	StatMod mod = com.getAvailableStat().getFlatStatMod(modId);
	// 	if (mod != null) {
	// 		curr = Math.round(mod.value);
	// 	}
		
	// 	int avWithoutPenalties = (int) Math.round(com.getAvailableStat().getBaseValue());
	// 	for (StatMod m : com.getAvailableStat().getFlatMods().values()) {
	// 		if (m.value < 0) continue;
	// 		avWithoutPenalties += (int) Math.round(m.value);
	// 	}
		
	// 	int a = com.getAvailable() - curr;
	// 	a = avWithoutPenalties - curr;
	// 	int d = com.getMaxDemand();
	// 	if (d > a) {
	// 		//int supply = Math.max(1, d - a - 1);
	// 		int supply = Math.max(1, d - a);
	// 		com.getAvailableStat().modifyFlat(modId, supply, "Brought in by raiders");
	// 	}
	// };
	

	//MarketCMD cmd;

	//FactionAPI FACTION_OWNER = market.getFaction();
	
	public void apply() {
		super.apply(true);

		int acs_size = market.getSize();
		String idMarket = market.getId();

		// setEcon(true);
		
		//setEconomy(market);
		//start acs
		// setEcon = true;
		market.setHidden(true);
		market.setEconGroup(market.getId());

		// market.getMemoryWithoutUpdate().set(MEM_FLAG, true);
		// market.getMemoryWithoutUpdate().set(MemFlags.HIDDEN_BASE_MEM_FLAG, true);

		// market.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);

		// if (HASSPASSED) {
		// 	Global.getSector().getEconomy().addMarket(market, true);
		// 	HASSPASSED = false;
		// }

		// Global.getSector().getEconomy().addMarket(market, true);
		// Global.getSector().getEconomy().addUpdateListener(this);

		// setEconomy(market);

		// Global.getSector().getEconomy().addMarket(market, true);
		//Global.getSector().getEconomy().getMarket(idMarket).isInEconomy();
		// Global.getSector().getEconomy().addUpdateListener((EconomyUpdateListener) this);
		// commodityUpdated(id);
		//market.isHidden();
		// market.setEconGroup(null);
		//setEcon(true);
		// Global.getFactory().createMarket(ACS_INDUSTRY_NAME, ACS_INDUSTRY_NAME, acs_size);

		//Global.getSector().getEconomy().addMarket(market, true);

		// market.getDemand(market.getId()).getDemand().setBaseValue(0);
		
		// for (Industry industry : market.getIndustries()) {
        //     for (MutableCommodityQuantity mutableCommodityQuantity : industry.getAllDemand()) {
        //         industry.getDemand(mutableCommodityQuantity.getCommodityId()).getQuantity().modifyMult("acs_no_demand",-100,"No Demand Industry");
        //     }

        // }
		
		
		demand(Commodities.SUPPLIES, acs_size - 2);
		demand(Commodities.FUEL, acs_size - 2);
		demand(Commodities.SHIPS, acs_size - 2);
		
		supply(Commodities.CREW, acs_size - 1);
		supply(Commodities.SHIPS, acs_size + 3);
		supply(Commodities.FUEL, acs_size + 3);
		supply(Commodities.SUPPLIES, acs_size + 1);
		supply(Commodities.ORGANICS, acs_size + 1);
		supply(Commodities.DOMESTIC_GOODS, acs_size + 1);
		supply(Commodities.FOOD, acs_size + 1);

		if (acs_size >= 3) {
			market.setImmigrationClosed(true);
		} else {
			market.setImmigrationClosed(false);
		}

		// if (isFunctional()) {
		// 	market.addCondition("acs_frontierecon");
		// }

		if (!isFunctional()) {
			supply.clear();
			unapply();
		}
	}

	@Override
	public void unapply() {
		super.unapply();

		// market.setImmigrationClosed(false);
		// market.removeCondition("acs_frontierecon");
		
		//start acs
		market.setHidden(false);
		market.setEconGroup(null);

		// HASSPASSED = true;

		// market.getDemand(market.getId()).getDemand().unmodify();

		// market.getMemoryWithoutUpdate().set(MemFlags.HIDDEN_BASE_MEM_FLAG, false);

		// market.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, false);

		// Global.getSector().getEconomy().removeMarket(market);

		// for (Industry industry : market.getIndustries()) {
        //     for (MutableCommodityQuantity mutableCommodityQuantity : industry.getAllDemand()) {
        //         industry.getDemand(mutableCommodityQuantity.getCommodityId()).getQuantity().unmodify();
        //     }

        // }
		
		unmodifyStabilityWithBaseMod();
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(getModId());
		market.getStability().unmodify(getModId());
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
		return acs_STABILITY_BONUS;
	}

	@Override
	protected Pair<String, Integer> getStabilityAffectingDeficit() {
		return getMaxDeficit(Commodities.SUPPLIES, Commodities.FUEL, Commodities.SHIPS);
	}

	
	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isAvailableToBuild() {
		return market.hasSpaceport();
	}
	
	public String getUnavailableReason() {
		return "Requires a functional spaceport";
	}

	@Override
	protected void applyAlphaCoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult("acs_alpha_" + getModId(), 1f + acs_FLEET_SIZE_BONUS_ALPHA, "Alpha core (" + getNameForModifier() + ")");
	}
	
	@Override
	protected void applyNoAICoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult("acs_alpha_" + getModId());
	}
	
	@Override
	protected void applyAlphaCoreSupplyAndDemandModifiers() {
		demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Alpha core");
	}
	
	protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float acs_opad = 1f;
		
		String acs_pre = "Alpha-level AI core currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			acs_pre = "Alpha-level AI core. ";
		}

		String acs_str = Strings.X + (1f + acs_FLEET_SIZE_BONUS_ALPHA);
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI acs_coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI acs_text = tooltip.beginImageWithText(acs_coreSpec.getIconName(), 48);
			acs_text.addPara(acs_pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Increases fleet size by %s.",
					0f, Misc.getHighlightColor(), "" + 25 + "%", "" + 1 + "x", acs_str);
			tooltip.addImageWithText(acs_opad);
			return;
		}
		
		tooltip.addPara(acs_pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Increases fleet size by %s.",
				acs_opad, Misc.getHighlightColor(), "" + 25 + "%", "" + 1 + "x", acs_str);
		
	}
	
	
	@Override
	public boolean canImprove() {
		return true;
	}
	
	protected void applyImproveModifiers() {
		if (isImproved()) {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(getModId(), 1f + acs_FLEET_SIZE_BONUS_DEFAULT + acs_FLEET_SIZE_BONUS_IMPROVE, "Auxiliary ship-bays");
		} else {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(getModId(), 1f + acs_FLEET_SIZE_BONUS_DEFAULT, "Auxiliary ship-bays");
		}
	}
	
	public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
		float acs_opad = 1f;
		
		if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
			info.addPara("Increases the fleet size multiplier to %s.", 0f, Misc.getHighlightColor(), Strings.X + (1f + acs_FLEET_SIZE_BONUS_DEFAULT + acs_FLEET_SIZE_BONUS_IMPROVE));
		}
		else {
			info.addPara("Increases the fleet size multiplier to %s.", 0f, Misc.getHighlightColor(), Strings.X + (1f + acs_FLEET_SIZE_BONUS_DEFAULT + acs_FLEET_SIZE_BONUS_IMPROVE));
		}

		info.addSpacer(acs_opad);
		super.addImproveDesc(info, mode);
	}


	//TempData temp = new TempData();

	// protected CampaignFleetAPI fleet;
	// protected float elapsed = 0f;
	
	// public AutoDespawnScript(CampaignFleetAPI fleet) {
	// 	this.fleet = fleet;
	// }

	// public void advance(float amount) {
	// 	if (!fleet.isInCurrentLocation()) {
	// 		elapsed += Global.getSector().getClock().convertToDays(amount);
	// 		if (elapsed > 30 && fleet.getBattle() == null) {
	// 			fleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null);
	// 			elapsed = -1;
	// 		}
	// 	} else {
	// 		elapsed = 0f;
	// 	}
	// }

	// public boolean isDone() {
	// 	return elapsed < 0;
	// }

	// public boolean runWhilePaused() {
	// 	return false;
	// } 

	
	// public void advance(float amount){

	// 	//boolean DAYHASPASS = false;
	// 	float hasElapse = 0f;
	// 	float adjustedNum = 0f;

	// 	if (HASPASS) {
	// 		hasElapse += Global.getSector().getClock().convertToDays(amount);

	// 		if (hasElapse >= 1) {
	// 			adjustLevel(adjustedNum, null);
	// 			hasElapse = -1;
	// 		}

	// 	} else {
	// 		hasElapse = 0f;
	// 	}
		
	// }

	// public float adjustLevel(float adjusted, RaidDangerLevel levels) {

	// 	//float raid = 0;
		
	// 	return levels.disruptionDays = adjusted;
	// }

	// public static boolean canRaidnegative(TempData data){

	// 	boolean can = true;
	// 	return data.canRaid = can;
	// }

	// @Override
	// protected addMilitaryResponse addMilitaryResponseHADINDUSTRY(addMilitaryResponse a) {
	// 	if (market == null) return;

	// 	//List axs = market.getIndustries();

	// 	if (market.hasIndustry(ACS_INDUSTRY_NAME)) {
	// 		return;
	// 	}
		
	// 	if (!market.getFaction().getCustomBoolean(Factions.CUSTOM_NO_WAR_SIM)) {
	// 		MilitaryResponseParams params = new MilitaryResponseParams(ActionType.HOSTILE, 
	// 				"player_ground_raid_" + market.getId(), 
	// 				market.getFaction(),
	// 				market.getPrimaryEntity(),
	// 				0.75f,
	// 				30f);
	// 		market.getContainingLocation().addScript(new MilitaryResponseScript(params));
	// 	}
	// 	List<CampaignFleetAPI> fleets = market.getContainingLocation().getFleets();
	// 	for (CampaignFleetAPI other : fleets) {
	// 		if (other.getFaction() == market.getFaction()) {
	// 			MemoryAPI mem = other.getMemoryWithoutUpdate();
	// 			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF, "raidAlarm", true, 1f);
	// 		}
	// 	}
	// }
	
	// @Override
	// public boolean doIndustryRaid noIndustryRaid(String commodityId, doIndustryRaid level) {
	// 	return level.next();
	// }



	@Override
	public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
		return level.next();
	}

	@Override
	public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
		return level.next();
	}
	

	
}




