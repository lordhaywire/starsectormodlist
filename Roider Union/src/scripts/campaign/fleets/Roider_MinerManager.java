package scripts.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_FleetTypes;
import scripts.campaign.ai.Roider_MinerAssignmentAI;
import java.util.*;
import org.lwjgl.util.vector.Vector2f;
import scripts.Roider_Misc;
import scripts.campaign.econ.Roider_Dives;

/**
 * Author: SafariJohn
 */
public class Roider_MinerManager implements FleetEventListener {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_MinerManager.class, "tracker", "t");
        x.aliasAttribute(Roider_MinerManager.class, "market", "m");
        x.aliasAttribute(Roider_MinerManager.class, "miners", "f");
        x.aliasAttribute(Roider_MinerManager.class, "random", "rand");
        x.aliasAttribute(Roider_MinerManager.class, "returningMinerValue", "r");
        x.aliasAttribute(Roider_MinerManager.class, "functional", "n");
    }

    public static final String MINER_STARTING_FP = "$roider_mining_starting_fp";

//    public static final int ROIDER_PREPARE = 1;
//    public static final int ROIDER_TRAVEL_TO = 2;
//    public static final int ROIDER_MINE_COMMON = 3;
//    public static final int ROIDER_MINE_RARE = 4;
//    public static final int ROIDER_RETURN = 5;
//    public static final int ROIDER_UNLOAD = 6;

	private final IntervalUtil tracker;
    private final MarketAPI market;
    private final List<CampaignFleetAPI> miners;
    private final Random random;

	private float returningMinerValue;
    private boolean functional;

    public Roider_MinerManager(MarketAPI market) {
        tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f,
													  Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);
        this.market = market;
        miners = new ArrayList<>();
        random = new Random();
        returningMinerValue = 0f;
        functional = true;
    }

    public IntervalUtil getTracker() {
        return tracker;
    }

    public void advance(boolean unionHQ, float amount, boolean functional,
                float range, Map<String, Integer> supplyLevels) {
        this.functional = functional;

        if (!functional) return;
        if (!market.isInEconomy()) return;

		float spawnRate = 1f;
		float rateMult;
        if (market != null) rateMult = market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).getModifiedValue();
        else rateMult = 1;
		spawnRate *= rateMult;

		float days = Global.getSector().getClock().convertToDays(amount);

		float extraTime = 0f;
		if (returningMinerValue > 0) {
			// apply "returned patrols" to spawn rate, at a maximum rate of 1 interval per day
			float interval = tracker.getIntervalDuration();
			extraTime = interval * days;
			returningMinerValue -= days;
			if (returningMinerValue < 0) returningMinerValue = 0;
		}
		tracker.advance(days * spawnRate + extraTime);

        //tracker.advance(days * spawnRate * 100f);

		if (tracker.intervalElapsed()) {
            // Clean up orphaned fleets
//            List<CampaignFleetAPI> remove = new ArrayList<>();
//            for (CampaignFleetAPI fleet : miners) {
//                if (fleet.getContainingLocation() == null ||
//                    !fleet.getContainingLocation().getFleets().contains(fleet)) {
//                    remove.add(fleet);
//                }
//            }
//            miners.removeAll(remove);

			int light = getCount(Roider_FleetTypes.MINER);
			int medium = getCount(Roider_FleetTypes.MINING_FLEET);
			int heavy = getCount(Roider_FleetTypes.MINING_ARMADA);

			int maxLight = 2;
			int maxMedium = 1;
			int maxHeavy = 0;

            if (unionHQ) {
                maxLight++;
                maxMedium++;
                maxHeavy++;
            }

			WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
			picker.add(Roider_FleetTypes.MINING_ARMADA, maxHeavy - heavy);
			picker.add(Roider_FleetTypes.MINING_FLEET, maxMedium - medium);
			picker.add(Roider_FleetTypes.MINER, maxLight - light);
			String type = picker.pick();

			if (picker.isEmpty()) return;

            CampaignFleetAPI fleet = spawnFleet(type);
            miners.add(fleet);

            // Create destination
            if (type.equals(Roider_FleetTypes.MINER)) range /= 2; // Halve range for small fleets
            StarSystemAPI system = null;
            EntityLocation loc = null;

            // Keep checking systems until we find a location
            // because there must be one
        Set<StarSystemAPI> alreadyTried = new HashSet<>();
            while (loc == null) {
            system = getHarvestableSystemInRange(alreadyTried, range);

            if (system == null) return;

            alreadyTried.add(system);

                // Exclude any non-harvestable planets
                List<MarketAPI> harvestMarkets = Roider_Dives.getHarvestTargetsInRange(market, range);
                Set<SectorEntityToken> exclude = new HashSet<>();
                for (PlanetAPI planet : system.getPlanets()) {
                    if (!harvestMarkets.contains(planet.getMarket())) {
                        exclude.add(planet);
                    }
                }

                loc = pickLocation(random, system, 200f, exclude, supplyLevels.containsKey(Commodities.VOLATILES));
            }

            assert (system != null);

            switch (loc.type) {
                case GAS_GIANT_ORBIT:
                case PLANET_ORBIT: break;
                case IN_SMALL_NEBULA:
                    supplyLevels.remove(Commodities.ORE);
                    supplyLevels.remove(Commodities.RARE_ORE);
                    supplyLevels.remove(Commodities.ORGANICS);
                    break;
                case IN_ASTEROID_BELT:
                case IN_ASTEROID_FIELD:
                case IN_RING:
                default:
                    supplyLevels.remove(Commodities.VOLATILES);
            }

            if (loc.type == LocationType.GAS_GIANT_ORBIT
                        || loc.type == LocationType.PLANET_ORBIT) {
                PlanetAPI planet = (PlanetAPI) loc.orbit.getFocus();

                boolean hasOre = false;
                boolean hasRareOre = false;
                boolean hasOrganics = false;
                boolean hasVolatiles = false;

                for (MarketConditionAPI c : planet.getMarket().getConditions()) {
                    switch (c.getId()) {
                        case (Conditions.ORE_ULTRARICH):
                        case (Conditions.ORE_RICH):
                        case (Conditions.ORE_ABUNDANT):
                        case (Conditions.ORE_MODERATE): hasOre = true;
                        case (Conditions.ORE_SPARSE): break;

                        case (Conditions.ORGANICS_PLENTIFUL):
                        case (Conditions.ORGANICS_ABUNDANT):
                        case (Conditions.ORGANICS_COMMON): hasOrganics = true;
                        case (Conditions.ORGANICS_TRACE): break;

                        // Reduced supply for rare ores and volatiles
                        case (Conditions.RARE_ORE_ULTRARICH):
                        case (Conditions.RARE_ORE_RICH): hasRareOre = true;
                        case (Conditions.RARE_ORE_ABUNDANT): break;
                        case (Conditions.RARE_ORE_MODERATE): break;
                        case (Conditions.RARE_ORE_SPARSE): break;

                        case (Conditions.VOLATILES_PLENTIFUL): hasVolatiles = true;
                        case (Conditions.VOLATILES_ABUNDANT): break;
                        case (Conditions.VOLATILES_DIFFUSE): break;
                        case (Conditions.VOLATILES_TRACE): break;
                    }
                }

                if (!hasOre) supplyLevels.remove(Commodities.ORE);
                if (!hasRareOre) supplyLevels.remove(Commodities.RARE_ORE);
                if (!hasOrganics) supplyLevels.remove(Commodities.ORGANICS);
                if (!hasVolatiles) supplyLevels.remove(Commodities.VOLATILES);
            }

            SectorEntityToken dest = system.createToken(new Vector2f());
            system.addEntity(dest);
            BaseThemeGenerator.setEntityLocation(dest, loc, null);

            if (Misc.isPirateFaction(fleet.getFaction())) {
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
            }

            fleet.addScript(new Roider_MinerAssignmentAI(fleet, market, dest, supplyLevels));


//            route.addSegment(new RouteSegment(ROIDER_PREPARE, orbitDays, market.getPrimaryEntity()));
//            route.addSegment(new RouteSegment(ROIDER_TRAVEL_TO, market.getPrimaryEntity(), dest));
//            route.addSegment(new RouteSegment(ROIDER_MINE_COMMON, orbitDays * 5f, dest));
//            if (rareCargo) route.addSegment(new RouteSegment(ROIDER_MINE_RARE, orbitDays * 5f, dest));
//            route.addSegment(new RouteSegment(ROIDER_RETURN, dest, market.getPrimaryEntity()));
//            route.addSegment(new RouteSegment(ROIDER_UNLOAD, orbitDays, market.getPrimaryEntity()));
		}
    }

	private int getCount(String ... types) {
		int count = 0;
		for (CampaignFleetAPI fleet : miners) {
				String cType = fleet.getMemoryWithoutUpdate().getString(MemFlags.MEMORY_KEY_FLEET_TYPE);
				for (String type : types) {
					if (type.equals(cType)) {
						count++;
						break;
					}
				}
		}
		return count;
	}

    private StarSystemAPI getHarvestableSystemInRange(Set<StarSystemAPI> alreadyTried, float ly) {
        Vector2f loc = market.getLocationInHyperspace();

        // What systems are in range?
        WeightedRandomPicker<StarSystemAPI> targets = new WeightedRandomPicker<>();
        targets.add(market.getStarSystem());
        for (StarSystemAPI s : Global.getSector().getStarSystems()) {
            if (alreadyTried.contains(s)) continue;
            if (s.getId().equals(market.getStarSystem().getId())) continue;
            if (s.hasTag(Tags.THEME_UNSAFE)) continue;
            if (s.hasTag(Tags.THEME_HIDDEN)) continue;
            if (s.getType() != StarSystemType.NEBULA
                        && s.getStar() == null) continue;

            float a = loc.getX() - s.getHyperspaceAnchor().getLocationInHyperspace().getX();
            float b = loc.getY() - s.getHyperspaceAnchor().getLocationInHyperspace().getY();
            float c = (a * a) + (b * b);

            float lyDist = Global.getSettings().getUnitsPerLightYear();
            boolean inRange = c <= lyDist * lyDist * ly * ly;

            if (inRange) targets.add(s);
        }

        if (targets.isEmpty()) return null;

        return targets.pick();
    }

    private static EntityLocation pickLocation(Random random, StarSystemAPI system, float gap, Set<SectorEntityToken> exclude, boolean miningVolatiles) {
        LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<>();
        weights.put(LocationType.IN_ASTEROID_BELT, 5f);
        weights.put(LocationType.IN_ASTEROID_FIELD, 5f);
        weights.put(LocationType.IN_RING, 5f);
        weights.put(LocationType.PLANET_ORBIT, 5f);

        if (miningVolatiles) {
            weights.put(LocationType.IN_SMALL_NEBULA, 5f);
            weights.put(LocationType.GAS_GIANT_ORBIT, 5f);
        }

        WeightedRandomPicker<EntityLocation> locs = BaseThemeGenerator.getLocations(random, system, exclude, gap, weights);

        if (locs.isEmpty()) {
            return null;
        }

        return locs.pick();
    }

    public CampaignFleetAPI spawnFleet(String fleetType) {

		float combat = 0f;
		float tanker = 0f;
		float freighter = 0f;
		switch (fleetType) {
		case Roider_FleetTypes.MINER:
			combat = Math.round(1f + (float) random.nextFloat() * 2f) * 5f;
			freighter = Math.round((float) random.nextFloat()) * 5f;
			break;
		case Roider_FleetTypes.MINING_FLEET:
			combat = Math.round(3f + (float) random.nextFloat() * 3f) * 5f;
			tanker = Math.round((float) random.nextFloat()) * 5f;
			freighter = Math.round((float) random.nextFloat()) * 15f;
			break;
		case Roider_FleetTypes.MINING_ARMADA:
			combat = Math.round(5f + (float) random.nextFloat() * 5f) * 5f;
			tanker = Math.round((float) random.nextFloat()) * 10f;
			freighter = Math.round((float) random.nextFloat()) * 25f;
			break;
		}

        // Get half the ships from Roider Union's choices
		FleetParamsV3 params = new FleetParamsV3(
				market,
				null, // loc in hyper; don't need if have market
				Roider_Factions.ROIDER_UNION,
				null, // quality override
				fleetType,
				combat / 2, // combatPts
				freighter / 2, // freighterPts
				tanker / 2, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
        );
		params.timestamp = Global.getSector().getClock().getTimestamp();
		params.random = random;
//		params.modeOverride = Misc.getShipPickMode(market);
		params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

		if (fleet == null || fleet.isEmpty()) return null;


        // Get the other half from the market's faction
		FleetParamsV3 params2 = new FleetParamsV3(
				market,
				null, // loc in hyper; don't need if have market
				null,
				null, // quality override
				fleetType,
				combat / 2, // combatPts
				freighter / 2, // freighterPts
				tanker / 2, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
        );
		params2.timestamp = Global.getSector().getClock().getTimestamp();
		params2.random = random;
//		params.modeOverride = Misc.getShipPickMode(market);
		params2.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
		CampaignFleetAPI fleet2 = FleetFactoryV3.createFleet(params2);

		if (fleet2 != null && !fleet2.isEmpty()) {
            for (FleetMemberAPI member : fleet2.getMembersWithFightersCopy()) {
                if (member.isFighterWing()) continue;

                fleet.getFleetData().addFleetMember(member);
            }
        }


        fleet.getMemoryWithoutUpdate().set(MINER_STARTING_FP, fleet.getFleetPoints());

        Roider_Misc.sortFleetByShipSize(fleet);
        FleetFactoryV3.addCommanderAndOfficers(fleet, params, random);

		fleet.setFaction(market.getFactionId(), true);
        fleet.setName(market.getFaction().getFleetTypeName(fleetType));
//		fleet.setNoFactionInName(true);

		fleet.addEventListener(this);

		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_TRADE_FLEET, true);

		String postId = Ranks.POST_FLEET_COMMANDER;
		String rankId = Ranks.SPACE_COMMANDER;
		switch (fleetType) {
		case Roider_FleetTypes.MINER:
			rankId = Ranks.CITIZEN;
			break;
		case Roider_FleetTypes.MINING_FLEET:
			rankId = Ranks.SPACE_CAPTAIN;
			break;
		case Roider_FleetTypes.MINING_ARMADA:
			rankId = Ranks.SPACE_ADMIRAL;
			break;
		}

		fleet.getCommander().setPostId(postId);
		fleet.getCommander().setRankId(rankId);

		market.getContainingLocation().addEntity(fleet);
		fleet.setFacing((float) Math.random() * 360f);
		fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

		return fleet;
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        miners.remove(fleet);

//		if (!functional) return;

		if (reason == CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION) {
            int spawnFP = (int) fleet.getMemoryWithoutUpdate().get(MINER_STARTING_FP);
            if (spawnFP > 0) {
                float fraction  = fleet.getFleetPoints() / spawnFP;
                returningMinerValue += fraction;
            }
		}
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {}

}
