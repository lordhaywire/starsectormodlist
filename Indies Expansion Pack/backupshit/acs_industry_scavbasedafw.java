package data.scripts.industry;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ScavengerFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;


public class acs_industry_scavbase extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {
	
	// @Override
	// public boolean isHidden() {
	// 	return !market.getFactionId().equals(Factions.DIKTAT);
	// }

	// protected StarSystemAPI system;

	// protected StarSystemAPI system = market.getStarSystem();
	private MarketAPI marketsystem = this.getMarket();
	private StarSystemAPI system = marketsystem.getStarSystem();
	
	// public acs_industry_scavbase(StarSystemAPI system) {
	// 	//super();
	// 	this.system = system;
	// 	//this.system = market.getStarSystem();
	// }

	protected String getRouteSourceId() {
		return "salvage_" + system.getId();
	}
	
	// @Override
	// public boolean isFunctional() {
	// 	return super.isFunctional() && market.getFactionId().equals(Factions.DIKTAT);
	// }

	public void apply() {
		super.apply(true);
		
		int size = market.getSize();

		demand(Commodities.SUPPLIES, 1);
		demand(Commodities.FUEL, 1);
		
		// demand(Commodities.SUPPLIES, size - 1);
		// demand(Commodities.FUEL, size - 1);
		// demand(Commodities.SHIPS, size - 1);
		
		supply(Commodities.CREW, size);
		
		// demand(Commodities.HAND_WEAPONS, size);
		//supply(Commodities.MARINES, size);
			
		//Pair<String, Integer> deficit = getMaxDeficit(Commodities.HAND_WEAPONS);
		//applyDeficitToProduction(1, deficit, Commodities.MARINES);
		
		//modifyStabilityWithBaseMod();
		
		MemoryAPI memory = market.getMemoryWithoutUpdate();
		//Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
		//Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);
		
		if (!isFunctional()) {
			supply.clear();
			unapply();
		}

	}

	@Override
	public void unapply() {
		super.unapply();
		
		MemoryAPI memory = market.getMemoryWithoutUpdate();
		//Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
		//Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);
		
		//unmodifyStabilityWithBaseMod();
	}
	
	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	// @Override
	// protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
	// 	if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
	// 		addStabilityPostDemandSection(tooltip, hasDemand, mode);
	// 	}
	// }
	
	
	
	// @Override
	// public String getCurrentImage() {
	// 	return super.getCurrentImage();
	// }

	
	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}

	

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (Global.getSector().getEconomy().isSimMode()) return;

		if (!isFunctional()) return;
		
		MarketAPI market = getMarket();
		if (market == null) return;
		
		Long seed = new Random().nextLong();
		String id = getRouteSourceId();
		
		OptionalFleetData extra = new OptionalFleetData(market);
		
		RouteData route = RouteManager.getInstance().addRoute(id, market, seed, extra, this);
		
		float distLY = Misc.getDistanceLY(market.getLocationInHyperspace(), system.getLocation());
		float travelDays = distLY * 1.5f;
		
		float prepDays = 2f + (float) Math.random() * 3f;
		float endDays = 8f + (float) Math.random() * 3f; // longer since includes time from jump-point to source

		float totalTravelTime = prepDays + endDays + travelDays * 2f;
		float stayDays = Math.max(20f, totalTravelTime);
		
		route.addSegment(new RouteSegment(prepDays, market.getPrimaryEntity()));
		route.addSegment(new RouteSegment(travelDays, market.getPrimaryEntity(), system.getCenter()));
		route.addSegment(new RouteSegment(stayDays, system.getCenter()));
		route.addSegment(new RouteSegment(travelDays, system.getCenter(), market.getPrimaryEntity()));
		route.addSegment(new RouteSegment(endDays, market.getPrimaryEntity()));
	}

	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random random = route.getRandom();
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		picker.add(FleetTypes.SCAVENGER_SMALL, 10f);
		picker.add(FleetTypes.SCAVENGER_MEDIUM, 15f);
		picker.add(FleetTypes.SCAVENGER_LARGE, 5f);
		
		String type = picker.pick();
		
		// boolean pirate = random.nextBoolean();
		boolean pirate = false;
		CampaignFleetAPI fleet = createScavenger(type, system.getLocation(), route, route.getMarket(), pirate, random);
		if (fleet == null) return null;;
		
		fleet.addScript(new ScavengerFleetAssignmentAI(fleet, route, pirate));
		
		return fleet;
	}

	public static CampaignFleetAPI createScavenger(String type, Vector2f locInHyper, MarketAPI source, boolean pirate, Random random) {
		return createScavenger(type, locInHyper, null, source, pirate, random);
	}
	public static CampaignFleetAPI createScavenger(String type, Vector2f locInHyper, RouteData route, MarketAPI source, boolean pirate, Random random) {
		if (random == null) random = new Random();

		
		if (type == null) {
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
			picker.add(FleetTypes.SCAVENGER_SMALL, 10f);
			picker.add(FleetTypes.SCAVENGER_MEDIUM, 15f);
			picker.add(FleetTypes.SCAVENGER_LARGE, 5f);
			type = picker.pick();
		}
		
		
		int combat = 0;
		int freighter = 0;
		int tanker = 0;
		int transport = 0;
		int utility = 0;
		
		
		if (type.equals(FleetTypes.SCAVENGER_SMALL)) {
			combat = random.nextInt(2) + 1;
			tanker = random.nextInt(2) + 1;
			utility = random.nextInt(2) + 1;
		} else if (type.equals(FleetTypes.SCAVENGER_MEDIUM)) {
			combat = 4 + random.nextInt(5);
			freighter = 4 + random.nextInt(5);
			tanker = 3 + random.nextInt(4);
			transport = random.nextInt(2);
			utility = 2 + random.nextInt(3);
		} else if (type.equals(FleetTypes.SCAVENGER_LARGE)) {
			combat = 7 + random.nextInt(8);
			freighter = 6 + random.nextInt(7);
			tanker = 5 + random.nextInt(6);
			transport = 3 + random.nextInt(8);
			utility = 4 + random.nextInt(5);
		}
		
		if (pirate) {
//			combat += transport;
//			combat += utility;
			transport = utility = 0;
		}
		
		combat *= 5f;
		freighter *= 3f;
		tanker *= 3f;
		transport *= 1.5f;
		
		FleetParamsV3 params = new FleetParamsV3(
				route != null ? route.getMarket() : source, 
				locInHyper,
				Factions.SCAVENGERS, // quality will always be reduced by non-market-faction penalty, which is what we want 
				route == null ? null : route.getQualityOverride(),
				type,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				transport, // transportPts
				0f, // linerPts
				utility, // utilityPts
				0f // qualityMod
				);
		if (route != null) {
			params.timestamp = route.getTimestamp();
		}
		params.random = random;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		fleet.setFaction(Factions.INDEPENDENT, true);
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SCAVENGER, true);
		
		if (pirate || true) {
			Misc.makeLowRepImpact(fleet, "scav");
		}
		
		return fleet;
	}

	
	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
	}
	
	public boolean shouldRepeat(RouteData route) {
		return false;
	}
	



	public static float getVeryApproximateSalvageValue(StarSystemAPI system) {
		return system.getEntitiesWithTag(Tags.SALVAGEABLE).size();
	}


	protected int getMaxFleets() {
		//if (true) return 0;
		
		float salvage = getVeryApproximateSalvageValue(system);
		return (int) (1 + Math.min(salvage / 2, 7));
	}

	
	
	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}

	

	@Override
	public boolean isAvailableToBuild() {
		return true;
	}
	
	public boolean showWhenUnavailable() {
		return true;
	}

	@Override
	public boolean canImprove() {
		return false;
	}
	
	// @Override
	// public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
	// 	return level.next();
	// }

	// @Override
	// public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
	// 	return level.next();
	// }

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
	}

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (!isFunctional()) return;
		
		// if (reason == FleetDespawnReason.REACHED_DESTINATION) {
		// 	RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
		// 	if (route.getCustom() instanceof PatrolFleetData) {
		// 		PatrolFleetData custom = (PatrolFleetData) route.getCustom();
		// 		if (custom.spawnFP > 0) {
		// 			float fraction  = fleet.getFleetPoints() / custom.spawnFP;
		// 			returningPatrolValue += fraction;
		// 		}
		// 	}
		// }
	}

	// @Override
	// public void reportFleetDespawnedToListener(CampaignFleetAPI arg0, FleetDespawnReason arg1, Object arg2) {
	// 	// TODO Auto-generated method stub
	// 	throw new UnsupportedOperationException("Unimplemented method 'reportFleetDespawnedToListener'");
	// }

	
}
