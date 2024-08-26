package data.scripts.industry;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
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


public class acs_industry_scavbase2 extends BaseIndustry {
	
	// @Override
	// public boolean isHidden() {
	// 	return !market.getFactionId().equals(Factions.DIKTAT);
	// }
	
	// @Override
	// public boolean isFunctional() {
	// 	return super.isFunctional() && market.getFactionId().equals(Factions.DIKTAT);
	// }

	public String fleetKeyMarket() {
		return "$acs_salvage_" + market.getId();
	}

	public void apply() {
		super.apply(true);
		
		int size = market.getSize();

		demand(Commodities.SUPPLIES, 1);
		demand(Commodities.FUEL, 1);
		
	
		
		supply(Commodities.CREW, size);
		

		
		// MemoryAPI memory = market.getMemoryWithoutUpdate();
		// Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
		// Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);
		
		if (!isFunctional()) {
			supply.clear();
			unapply();
		}

	}

	@Override
	public void unapply() {
		super.unapply();
		
		// MemoryAPI memory = market.getMemoryWithoutUpdate();
		// Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
		// Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);
		
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
	// 	return getMaxDeficit(Commodities.SUPPLIES, Commodities.FUEL, Commodities.SHIPS, Commodities.HAND_WEAPONS);
	// }
	
	@Override
	public String getCurrentImage() {
		return super.getCurrentImage();
	}

	
	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}

	protected IntervalUtil tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f,
													  Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);
	
	protected float returningPatrolValue = 0f;
	
	@Override
	protected void buildingFinished() {
		super.buildingFinished();
		
		tracker.forceIntervalElapsed();
	}
	
	@Override
	protected void upgradeFinished(Industry previous) {
		super.upgradeFinished(previous);
		
		tracker.forceIntervalElapsed();
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (Global.getSector().getEconomy().isSimMode()) return;

		if (!isFunctional()) return;

		SectorEntityToken home = market.getPrimaryEntity();

		// SectorEntityToken hole = getSpec();

		int numFleets = 0;
        for (CampaignFleetAPI fleet : home.getContainingLocation().getFleets()) {
            if (fleet.getMemory().contains(fleetKeyMarket())) {
                numFleets++;
                // if (Misc.getDistance(fleet.getLocation(), hole.getLocation()) < 300f) {
                //     fleet.despawn();
                } else if (fleet.getFleetPoints() < 200) {
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, home, 10000f);
                }
            }
			if (numFleets < 1) {
				//            logger.info("size=" + numFleets);
				//            logger.info("fleets=" + FLEETS);
							//spawn one fleet
							spawnFleet(null, null);
						}
		}
	

	public void spawnFleet(String type, Random random) {
        //TODO test

		//Random random;
		if (random == null) random = new Random();

		
		if (type == null) {
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
			// picker.add(FleetTypes.SCAVENGER_SMALL, 10f);
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

		combat *= 5f;
		freighter *= 3f;
		tanker *= 3f;
		transport *= 1.5f;
		
// 		if (pirate) {
// //			combat += transport;
// //			combat += utility;
// 			transport = utility = 0;
// 		}


        //float fp = (float) (120f + (Math.random() - .5) * 100f);
        FleetParamsV3 fleetParamsV3 = new FleetParamsV3(
				new Vector2f(0, 0), 
				Factions.INDEPENDENT,
                1.0f,
                type,
                combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				transport, // transportPts
				0f, // linerPts
				utility, // utilityPts
				0f // qualityMod
        );

		SectorEntityToken as = market.getPrimaryEntity();

		String fleetKey =  fleetKeyMarket();

		
        fleetParamsV3.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(fleetParamsV3);
        fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, as, 100f);
        as.getContainingLocation().addEntity(fleet);
        fleet.setLocation(as.getLocation().getX(), as.getLocation().getY());
        fleet.setFaction(Factions.INDEPENDENT);
        fleet.getMemory().set(fleetKey,true);
		// Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_PATROL_FLEET, t, 999);

		// fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
        // Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_HOSTILE, "thquest_ttr", true, 999);
        // Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_LOW_REP_IMPACT, "thquest_ttr", true, 999);//TODO maybe this is the right key

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
// 				Factions.INDEPENDENT,
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
		
// 		fleet.setFaction(market.getFactionId(), true);
// 		fleet.setNoFactionInName(true);
		
// 		// fleet.addEventListener(this);
		
// //		PatrolAssignmentAIV2 ai = new PatrolAssignmentAIV2(fleet, custom);
// //		fleet.addScript(ai);
		
// 		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
// 		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.3f);

// 		if (type == PatrolType.FAST || type == PatrolType.COMBAT) {
// 			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
// 		}
		
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
	
	public String getRouteSourceId() {
		return getMarket().getId() + "_" + "acs_scavbase";
	}

	@Override
	public boolean isAvailableToBuild() {
		return market.hasSpaceport();
	}

	public String getUnavailableReason() {
		return "Requires a functional spaceport";
	}
	
	// public boolean showWhenUnavailable() {
	// 	return false;
	// }

	@Override
	public boolean canImprove() {
		return false;
	}
	
	@Override
	public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
		return level.next();
	}

	@Override
	public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
		return level.next();
	}
	
}
