package data.scripts.industry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.PlanetAPI;
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
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ScavengerFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.api.impl.campaign.fleets.BaseRouteFleetManager;


public class acs_industry_scavbase extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {
	
	// @Override
	// public boolean isHidden() {
	// 	return !market.getFactionId().equals(Factions.DIKTAT);
	// }

	protected StarSystemAPI system;
	
	// public acs_industry_scavbase (StarSystemAPI system) {
	// 	super(1f, 14f);
	// 	system = Global.getSector().getStarSystem();

	// 	Vector2f loc = market.getLocationInHyperspace();

	// 	        // if (loc == null) return new ArrayList<>();

	// 	List<PlanetAPI> nearSystem = new ArrayList<>();

		// for (StarSystemAPI s : Global.getSector().getStarSystems()) {
		// 	if (s.getHyperspaceAnchor() == null) continue;
        //     if (s.getHyperspaceAnchor().getLocationInHyperspace() == null) continue;
        //     if (s.getId().equals(market.getStarSystem().getId())) continue;
        //     if (s.hasTag(Tags.THEME_UNSAFE)) continue;
        //     if (s.hasTag(Tags.THEME_HIDDEN)) continue;
        //     if (s.getType() != StarSystemType.NEBULA
        //                 && s.getStar() == null) continue;

    //         float a = loc.getX() - s.getHyperspaceAnchor().getLocationInHyperspace().getX();
    //         float b = loc.getY() - s.getHyperspaceAnchor().getLocationInHyperspace().getY();
    //         float c = (a * a) + (b * b);

    //         float lydist = Global.getSettings().getUnitsPerLightYear();
	// 		if (c <= lydist * lydist * lyr * lyr) nearSystem.addAll(s.getPlanets());
    //         nearSystem.addAll(s.getPlanets());
	// 	}

	// 	// Random randomit = new Random();
	// 	// int selectedSystem = randomit.nextInt(nearSystem.);
	// 	// system = nearSystem[randomit];
	// 	//boolean pirate = random.nextBoolean();
	// 	//List<PlanetAPI> nearSystem = WeightedRandomPicker;
	// 	WeightedRandomPicker
	// 	this.system = market.getStarSystem();
	// }
	public StarSystemAPI systemTargetPickers() {
		//		if (true) {
		//			return Global.getSector().getEconomy().getMarket("jangala");
		//		}
				
				WeightedRandomPicker<StarSystemAPI> selectedSystem = new WeightedRandomPicker<StarSystemAPI>();
				
				for (StarSystemAPI s : Global.getSector().getStarSystems()) {
					// if (s.getHyperspaceAnchor() == null) continue;
					// if (s.getHyperspaceAnchor().getLocationInHyperspace() == null) continue;
					// if (s.getId().equals(market.getStarSystem().getId())) continue;
					// if (s.hasTag(Tags.THEME_UNSAFE)) continue;
					// if (s.hasTag(Tags.THEME_HIDDEN)) continue;
					// if (s.getType() != StarSystemType.NEBULA
								// && s.getStar() == null) continue;
					
					float distLY = Misc.getDistanceLY(s.getLocation(), market.getLocationInHyperspace());
					//float weight = market.getSize();
					
					float f = Math.max(0.1f, 1f - Math.min(1f, distLY / 20f));
					if (distLY <= f) selectedSystem.add(s);
					// f *= f;
					// if (c <= lydist * lydist * ly * ly) pTargets.addAll(s.getPlanets());
					// weight *= f;
					
					// selectedSystem.add(s);
				}
				
				return system = selectedSystem.pick();
				// return selectedSystem.pick();
	}

	// private StarSystemAPI system = systemTargetPickers();



	protected String getRouteSourceId() {
		return "salvage_" + systemTargetPickers().getId();
		//return getMarket().getId() + "_" + "acsplayerscav";
	}

	protected int getMaxFleets() {
		//if (true) return 0;
		
		float salvage = getVeryApproximateSalvageValue(system);
		// float salvage = 1;
		return (int) (1 + Math.min(salvage / 2, 7));
		// return (int) salvage;
	}

	public static float getVeryApproximateSalvageValue(StarSystemAPI system) {
		return system.getEntitiesWithTag(Tags.SALVAGEABLE).size();
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
			
		// Pair<String, Integer> deficit = getMaxDeficit(Commodities.HAND_WEAPONS);
		// applyDeficitToProduction(1, deficit, Commodities.MARINES);
		
		// modifyStabilityWithBaseMod();
		
		MemoryAPI memory = market.getMemoryWithoutUpdate();
		Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
		Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);
		
		if (!isFunctional()) {
			supply.clear();
			unapply();
		}

	}

	@Override
	public void unapply() {
		super.unapply();
		
		MemoryAPI memory = market.getMemoryWithoutUpdate();
		Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
		Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);
		
		// unmodifyStabilityWithBaseMod();
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
	
	// @Override
	// protected int getBaseStabilityMod() {
	// 	return 2;
	// }
	
	// public String getNameForModifier() {
	// 	if (getSpec().getName().contains("HQ")) {
	// 		return getSpec().getName();
	// 	}
	// 	return Misc.ucFirst(getSpec().getName());
	// }
	
	// @Override
	// protected Pair<String, Integer> getStabilityAffectingDeficit() {
	// 	return getMaxDeficit(Commodities.SUPPLIES, Commodities.FUEL);
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

	protected IntervalUtil tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f,
													  Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);
	
	protected float returningPatrolValue = 0f;
	
	// @Override
	// protected void buildingFinished() {
	// 	super.buildingFinished();
		
	// 	tracker.forceIntervalElapsed();
	// }
	
	// @Override
	// protected void upgradeFinished(Industry previous) {
	// 	super.upgradeFinished(previous);
		
	// 	tracker.forceIntervalElapsed();
	// }

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (Global.getSector().getEconomy().isSimMode()) return;

		if (!isFunctional()) return;

		//MarketAPI market = pickSourceMarket();
		if (market == null) return;

		if (market.getPrimaryEntity() == null) return;
        if (market.getStarSystem() == null) return;
		
		Long seed = new Random().nextLong();
		String id = getRouteSourceId();

		//StarSystemAPI systemmbaybe = getScavengeTargetsInRange.random();
		
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
		
		// float days = Global.getSector().getClock().convertToDays(amount);
		
		// float spawnRate = 1f;
		// float rateMult = market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).getModifiedValue();
		// spawnRate *= rateMult;
		
		
		// float extraTime = 0f;
		// if (returningPatrolValue > 0) {
		// 	// apply "returned patrols" to spawn rate, at a maximum rate of 1 interval per day
		// 	float interval = tracker.getIntervalDuration();
		// 	extraTime = interval * days;
		// 	returningPatrolValue -= days;
		// 	if (returningPatrolValue < 0) returningPatrolValue = 0;
		// }
		// tracker.advance(days * spawnRate + extraTime);
		
		// //tracker.advance(days * spawnRate * 100f);
		
		// if (tracker.intervalElapsed()) {
		// 	String sid = getRouteSourceId();
			
		// 	int light = getCount(PatrolType.FAST);
		// 	int medium = getCount(PatrolType.COMBAT);
		// 	int heavy = getCount(PatrolType.HEAVY);

		// 	int maxLight = 3;
		// 	int maxMedium = 2;
		// 	int maxHeavy = 1;
			
		// 	WeightedRandomPicker<PatrolType> picker = new WeightedRandomPicker<PatrolType>();
		// 	picker.add(PatrolType.HEAVY, maxHeavy - heavy); 
		// 	picker.add(PatrolType.COMBAT, maxMedium - medium); 
		// 	picker.add(PatrolType.FAST, maxLight - light); 
			
		// 	if (picker.isEmpty()) return;
			
		// 	PatrolType type = picker.pick();
		// 	PatrolFleetData custom = new PatrolFleetData(type);
			
		// 	OptionalFleetData extra = new OptionalFleetData(market);
		// 	extra.fleetType = type.getFleetType();
			
		// 	RouteData route = RouteManager.getInstance().addRoute(sid, market, Misc.genRandomSeed(), extra, this, custom);
		// 	float patrolDays = 35f + (float) Math.random() * 10f;
			
		// 	route.addSegment(new RouteSegment(patrolDays, market.getPrimaryEntity()));
		// }
	}

	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random random = route.getRandom();
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		picker.add(FleetTypes.SCAVENGER_SMALL, 10f);
		picker.add(FleetTypes.SCAVENGER_MEDIUM, 15f);
		picker.add(FleetTypes.SCAVENGER_LARGE, 5f);
		
		String type = picker.pick();
		
		//boolean pirate = random.nextBoolean();
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
	
	// public int getCount(PatrolType ... types) {
	// 	int count = 0;
	// 	for (RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId())) {
	// 		if (data.getCustom() instanceof PatrolFleetData) {
	// 			PatrolFleetData custom = (PatrolFleetData) data.getCustom();
	// 			for (PatrolType type : types) {
	// 				if (type == custom.type) {
	// 					count++;
	// 					break;
	// 				}
	// 			}
	// 		}
	// 	}
	// 	return count;
	// }

	// public int getMaxPatrols(PatrolType type) {
	// 	if (type == PatrolType.FAST) {
	// 		return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).computeEffective(0);
	// 	}
	// 	if (type == PatrolType.COMBAT) {
	// 		return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).computeEffective(0);
	// 	}
	// 	if (type == PatrolType.HEAVY) {
	// 		return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).computeEffective(0);
	// 	}
	// 	return 0;
	// }
	
	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}

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
	
// 	public CampaignFleetAPI spawnFleet(RouteData route) {
		
// 		PatrolFleetData custom = (PatrolFleetData) route.getCustom();
// 		PatrolType type = custom.type;
		
// 		Random random = route.getRandom();
		
// 		float combat = 0f;
// 		float tanker = 0f;
// 		float freighter = 0f;
// 		String fleetType = type.getFleetType();
// 		switch (type) {
// 		case FAST:
// 			combat = Math.round(3f + (float) random.nextFloat() * 2f) * 5f;
// 			break;
// 		case COMBAT:
// 			combat = Math.round(6f + (float) random.nextFloat() * 3f) * 5f;
// 			tanker = Math.round((float) random.nextFloat()) * 5f;
// 			break;
// 		case HEAVY:
// 			combat = Math.round(10f + (float) random.nextFloat() * 5f) * 5f;
// 			tanker = Math.round((float) random.nextFloat()) * 10f;
// 			freighter = Math.round((float) random.nextFloat()) * 10f;
// 			break;
// 		}
		
// 		FleetParamsV3 params = new FleetParamsV3(
// 				market, 
// 				null, // loc in hyper; don't need if have market
// 				Factions.SCAVENGERS,
// 				route.getQualityOverride(), // quality override
// 				fleetType,
// 				combat, // combatPts
// 				freighter, // freighterPts 
// 				tanker, // tankerPts
// 				0f, // transportPts
// 				0f, // linerPts
// 				0f, // utilityPts
// 				0f // qualityMod - since the Lion's Guard is in a different-faction market, counter that penalty
// 				);
// 		params.timestamp = route.getTimestamp();
// 		params.random = random;
// 		params.modeOverride = Misc.getShipPickMode(market);
// 		params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
// 		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
// 		if (fleet == null || fleet.isEmpty()) return null;
		
// 		// fleet.setFaction(market.getFactionId(), true);
// 		fleet.setFaction(Factions.INDEPENDENT, true);
// 		fleet.setNoFactionInName(true);
		
// 		fleet.addEventListener(this);
		
// //		PatrolAssignmentAIV2 ai = new PatrolAssignmentAIV2(fleet, custom);
// //		fleet.addScript(ai);
		
// 		// fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
// 		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SCAVENGER, true);
// 		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.3f);

// 		// if (type == PatrolType.FAST || type == PatrolType.COMBAT) {
// 		// 	fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
// 		// }
		
// 		String postId = Ranks.POST_PATROL_COMMANDER;
// 		String rankId = Ranks.SPACE_COMMANDER;
// 		switch (type) {
// 		case FAST:
// 			rankId = Ranks.SPACE_LIEUTENANT;
// 			break;
// 		case COMBAT:
// 			rankId = Ranks.SPACE_COMMANDER;
// 			break;
// 		case HEAVY:
// 			rankId = Ranks.SPACE_CAPTAIN;
// 			break;
// 		}
		
// 		fleet.getCommander().setPostId(postId);
// 		fleet.getCommander().setRankId(rankId);
		
// 		market.getContainingLocation().addEntity(fleet);
// 		fleet.setFacing((float) Math.random() * 360f);
// 		// this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
// 		fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
		
// 		fleet.addScript(new PatrolAssignmentAIV4(fleet, route));
		
// 		//market.getContainingLocation().addEntity(fleet);
// 		//fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
		
// 		if (custom.spawnFP <= 0) {
// 			custom.spawnFP = fleet.getFleetPoints();
// 		}
		
// 		return fleet;
// 	}
	
	// public String getRouteSourceId() {
	// 	return getMarket().getId() + "_" + "acsplayerscav";
	// }

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
	
}
