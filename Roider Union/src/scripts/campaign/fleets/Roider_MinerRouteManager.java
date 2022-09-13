package scripts.campaign.fleets;

import scripts.campaign.cleanup.Roider_MinerTokenCleaner;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.BaseRouteFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_FleetTypes;
import java.util.*;
import org.lwjgl.util.vector.Vector2f;
import scripts.Roider_Misc;
import scripts.campaign.econ.Roider_Dives;

/**
 * Author: SafariJohn
 */
public class Roider_MinerRouteManager extends BaseRouteFleetManager {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_MinerRouteManager.class, "market", "m");
        x.aliasAttribute(Roider_MinerRouteManager.class, "random", "r");
        x.aliasAttribute(Roider_MinerRouteManager.class, "functional", "f");
        x.aliasAttribute(Roider_MinerRouteManager.class, "unionHQ", "u");
    }

    public static final int PREPARE = 0;
    public static final int GO_TO = 1;
    public static final int MINE = 2;
    public static final int RETURN = 3;
    public static final int UNLOAD = 4;
    public static final int END = 5;

    public static final String MINER_STARTING_FP = "$roider_mining_starting_fp";

    private final MarketAPI market;
    private final Random random;

    private transient Map<String, Integer> supplyLevels;

    private boolean functional;
    private boolean unionHQ;

    public Roider_MinerRouteManager(MarketAPI market, boolean isUnionHQ) {
        super(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f,
                    Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);
        this.market = market;
        random = new Random();

        supplyLevels = new HashMap<>();

        functional = false;
        unionHQ = isUnionHQ;
    }

    public void advance(float amount, Map<String, Integer> supplyLevels,
                 boolean functional, boolean unionHQ) {
        this.functional = functional;
        this.supplyLevels = supplyLevels;
        this.unionHQ = unionHQ;

        super.advance(amount);
    }

    public void forceIntervalElapsed() {
        interval.forceIntervalElapsed();
    }

    @Override
    protected String getRouteSourceId() {
        return "roider_miners_" + market.getId();
    }

    @Override
    protected int getMaxFleets() {
        if (unionHQ) return 6;
        else return 3;
    }

    @Override
    protected void addRouteFleetIfPossible() {
        if (!functional) return;
        if (supplyLevels == null) supplyLevels = new HashMap<>();

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


		OptionalFleetData extra = new OptionalFleetData(market);
        extra.fleetType = type;

        if (picker.isEmpty()) return;

        float range = Roider_Dives.DIVES_RANGE;
        if (unionHQ) range = Roider_Dives.HQ_RANGE;

        // Create destination
        if (type.equals(Roider_FleetTypes.MINER)) range /= 2; // Halve range for small fleets
        StarSystemAPI system = null;
        BaseThemeGenerator.EntityLocation loc = null;

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
                if (planet.isStar()) continue;

                if (!harvestMarkets.contains(planet.getMarket())) {
                    exclude.add(planet);
                }
            }

            loc = pickLocation(random, system, 200f, exclude, supplyLevels.containsKey(Commodities.VOLATILES));
        }

        assert(system != null);

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

        if (loc.orbit != null && (loc.type == BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT
                    || loc.type == BaseThemeGenerator.LocationType.PLANET_ORBIT)) {
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

        RouteData route = RouteManager.getInstance().addRoute(
                    getRouteSourceId(), market, random.nextLong(),
                    extra, this, supplyLevels);

        SectorEntityToken dest = system.createToken(new Vector2f());
        system.addEntity(dest);
        BaseThemeGenerator.setEntityLocation(dest, loc, null);

        // Script will clean up token when route is no longer active
        Global.getSector().addScript(new Roider_MinerTokenCleaner(route, dest));

        float daysToOrbit = getDaysToOrbit(type) * 0.25f;
        if (daysToOrbit < 0.2f) {
            daysToOrbit = 0.2f;
        }

		float dist = Misc.getDistanceLY(market.getLocationInHyperspace(), dest.getLocationInHyperspace());
        if (dist == 0) dist = Misc.getDistance(route.getMarket().getPrimaryEntity(), dest);
		float travelDays = dist * 1.5f;

        float daysToMine = getDaysToMine(type);

		route.addSegment(new RouteSegment(PREPARE, daysToOrbit, market.getPrimaryEntity()));
		route.addSegment(new RouteSegment(GO_TO, travelDays, market.getPrimaryEntity(), dest));
		route.addSegment(new RouteSegment(MINE, daysToMine, dest));
		route.addSegment(new RouteSegment(RETURN, travelDays, dest, market.getPrimaryEntity()));
		route.addSegment(new RouteSegment(UNLOAD, daysToOrbit * 2, market.getPrimaryEntity()));
//		route.addSegment(new RouteSegment(END, Short.MAX_VALUE, market.getPrimaryEntity()));
    }

	private int getCount(String ... types) {
		int count = 0;
		for (RouteData route : RouteManager.getInstance().getRoutesForSource(getRouteSourceId())) {
            String cType = route.getExtra().fleetType;
//            String cType = route.getActiveFleet().getMemoryWithoutUpdate().getString(MemFlags.MEMORY_KEY_FLEET_TYPE);
            for (String type : types) {
                if (type.equals(cType)) {
                    count++;
                    break;
                }
            }
		}
		return count;
	}

    private float getDaysToOrbit(String fleetType) {
        float daysToOrbit = 0f;
        switch (fleetType) {
        case Roider_FleetTypes.MINER:
            daysToOrbit += 2f;
            break;
        case Roider_FleetTypes.MINING_FLEET:
            daysToOrbit += 4f;
            break;
        case Roider_FleetTypes.MINING_ARMADA:
            daysToOrbit += 6f;
            break;
        }

        daysToOrbit = daysToOrbit * (0.5f + (float) Math.random() * 0.5f);
        return daysToOrbit;
    }

    private float getDaysToMine(String fleetType) {
        float daysToMine = 10f;
        switch (fleetType) {
        case Roider_FleetTypes.MINER:
            daysToMine += 5f;
            break;
        case Roider_FleetTypes.MINING_FLEET:
            daysToMine += 10f;
            break;
        case Roider_FleetTypes.MINING_ARMADA:
            daysToMine += 15f;
            break;
        }

        daysToMine = daysToMine * (0.5f + (float) Math.random() * 0.5f);
        return daysToMine;
    }

    @Override
    public CampaignFleetAPI spawnFleet(RouteManager.RouteData route) {

		float combat = 0f;
		float tanker = 0f;
		float freighter = 0f;
		switch (route.getExtra().fleetType) {
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

        CampaignFleetAPI fleet;
        if (!route.getFactionId().equals(Roider_Factions.ROIDER_UNION)) {
            // Get half the ships from Roider Union's choices
            FleetParamsV3 params = new FleetParamsV3(
                    market,
                    null, // loc in hyper; don't need if have market
                    Roider_Factions.ROIDER_UNION,
                    null, // quality override
                    route.getExtra().fleetType,
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
            fleet = FleetFactoryV3.createFleet(params);

            if (fleet == null || fleet.isEmpty()) return null;


            // Get the other half from the market's faction
            FleetParamsV3 params2 = new FleetParamsV3(
                    market,
                    null, // loc in hyper; don't need if have market
                    null,
                    null, // quality override
                    route.getExtra().fleetType,
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


    //        fleet.getMemoryWithoutUpdate().set(MINER_STARTING_FP, fleet.getFleetPoints());

            FleetFactoryV3.addCommanderAndOfficers(fleet, params, random);
            Roider_Misc.sortFleetByShipSize(fleet);
        } else {
            FleetParamsV3 params = new FleetParamsV3(
                    market,
                    null, // loc in hyper; don't need if have market
                    Roider_Factions.ROIDER_UNION,
                    null, // quality override
                    route.getExtra().fleetType,
                    combat, // combatPts
                    freighter, // freighterPts
                    tanker, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f // qualityMod
            );
            params.timestamp = Global.getSector().getClock().getTimestamp();
            params.random = random;
    //		params.modeOverride = Misc.getShipPickMode(market);
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            fleet = FleetFactoryV3.createFleet(params);

            if (fleet == null || fleet.isEmpty()) return null;
        }

		fleet.setFaction(market.getFactionId(), true);
        fleet.setName(market.getFaction().getFleetTypeName(route.getExtra().fleetType));
//		fleet.setNoFactionInName(true);

		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_TRADE_FLEET, true);

		String postId = Ranks.POST_FLEET_COMMANDER;
		String rankId = Ranks.SPACE_COMMANDER;
		switch (route.getExtra().fleetType) {
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

//		market.getContainingLocation().addEntity(fleet);
//		fleet.setFacing((float) Math.random() * 360f);
//		fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

        if (Misc.isPirateFaction(fleet.getFaction())) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        }

//        fleet.addScript(new Roider_MinerAssignmentAI(fleet, market, dest, supplyLevels));
        fleet.addScript(new Roider_MinerRouteAI(fleet, route, (Map) route.getCustom()));

		return fleet;
    }

    @Override
    public boolean shouldCancelRouteAfterDelayCheck(RouteManager.RouteData route) {
        return false;
    }

    @Override
    public boolean shouldRepeat(RouteManager.RouteData route) {
        return false;
    }

    @Override
    public void reportAboutToBeDespawnedByRouteManager(RouteManager.RouteData route) {

    }

    private StarSystemAPI getHarvestableSystemInRange(Set<StarSystemAPI> alreadyTried, float ly) {
        Vector2f loc = market.getLocationInHyperspace();

        // What systems are in range?
        WeightedRandomPicker<StarSystemAPI> targets = new WeightedRandomPicker<>();
        for (StarSystemAPI s : Global.getSector().getStarSystems()) {
            if (s.getHyperspaceAnchor() == null) continue;
            if (alreadyTried.contains(s)) continue;
            if (s.hasTag(Tags.THEME_UNSAFE)) continue;
            if (s.hasTag(Tags.THEME_HIDDEN)) continue;
            if (s.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) continue;
            if (s.getType() != StarSystemGenerator.StarSystemType.NEBULA
                        && s.getStar() == null) continue;
//            if (s.getId().equals(market.getStarSystem().getId())) {
//                targets.add(market.getStarSystem());
//                continue;
//            }

            float a = loc.getX() - s.getHyperspaceAnchor().getLocationInHyperspace().getX();
            float b = loc.getY() - s.getHyperspaceAnchor().getLocationInHyperspace().getY();
            float c = (a * a) + (b * b);

            if (c == 0) {
                targets.add(s);
                continue;
            }

            float lyDist = Global.getSettings().getUnitsPerLightYear();
            boolean inRange = c <= lyDist * lyDist * ly * ly;

            if (inRange) targets.add(s);
        }

        if (targets.isEmpty()) return null;

        return targets.pick();
    }

    private static BaseThemeGenerator.EntityLocation pickLocation(Random random, StarSystemAPI system, float gap, Set<SectorEntityToken> exclude, boolean miningVolatiles) {
        LinkedHashMap<BaseThemeGenerator.LocationType, Float> weights = new LinkedHashMap<>();
        weights.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, 5f);
        weights.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, 5f);
        weights.put(BaseThemeGenerator.LocationType.IN_RING, 5f);
        weights.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 5f);

        if (miningVolatiles) {
            weights.put(BaseThemeGenerator.LocationType.IN_SMALL_NEBULA, 5f);
            weights.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 5f);
        }

        WeightedRandomPicker<BaseThemeGenerator.EntityLocation> locs = BaseThemeGenerator.getLocations(random, system, exclude, gap, weights);

        if (locs.isEmpty()) {
            return null;
        }

        return locs.pick();
    }

}
