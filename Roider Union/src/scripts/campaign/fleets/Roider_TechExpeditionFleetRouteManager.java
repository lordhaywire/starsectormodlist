package scripts.campaign.fleets;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.BaseRouteFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.thoughtworks.xstream.XStream;
import data.scripts.util.MagicSettings;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_FleetTypes;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_Ids.Roider_Settings;
import ids.Roider_MemFlags;
import scripts.Roider_Misc;

public class Roider_TechExpeditionFleetRouteManager extends BaseRouteFleetManager {
    public static class CustomData {
        public static void aliasAttributes(XStream x) {
            x.aliasAttribute(CustomData.class, "system", "s");
            x.aliasAttribute(CustomData.class, "majorExpedition", "m");
        }

        public final String system;
        public final boolean majorExpedition;

        public CustomData(StarSystemAPI system, boolean majorExpedition) {
            this.system = system.getId();
            this.majorExpedition = majorExpedition;
        }
    }

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_TechExpeditionFleetRouteManager.class, "currSystem", "s");
        x.aliasAttribute(Roider_TechExpeditionFleetRouteManager.class, "delay", "d");
        x.aliasAttribute(Roider_TechExpeditionFleetRouteManager.class, "random", "r");
    }

	protected String currSystem; // Changes each time a route is created
    private float delay;
    private final Random random;

	public Roider_TechExpeditionFleetRouteManager() {
		super(1f, 14f);
		this.currSystem = pickTargetSystem().getId();
        delay = MagicSettings.getFloat(Roider_Settings.MAGIC_ID,
                    Roider_Settings.EXPEDITION_DELAY);
        this.random = new Random();
	}

	protected String getRouteSourceId() {
		return "salvage_" + currSystem;
	}

	protected int getMaxFleets() {
		float salvage = getVeryApproximateSalvageValue(getCurrSystem());
        int salvageFleets = (int) (1 + Math.min(salvage / 2, 7));

        int max = MagicSettings.getInteger(Roider_Settings.MAGIC_ID,
                    Roider_Settings.MAX_EXPEDITIONS);

        // Limit by number of salvage routes in this system
        // and by this manager's total number of routes
		return Math.min(max, salvageFleets);
	}

	public void advance(float amount) {
		float days = Misc.getDays(amount);
//        days *= 100;

        // Delay before first major expedition can start
		if (!TutorialMissionIntel.isTutorialInProgress() && delay > 0) {
            delay -= days;
        }

		interval.advance(days);
		if (interval.intervalElapsed()) {
            currSystem = pickTargetSystem().getId();

			String id = getRouteSourceId();
			int max = getMaxFleets();
            int maxTotal = MagicSettings.getInteger(Roider_Settings.MAGIC_ID,
                    Roider_Settings.MAX_EXPEDITIONS);

            RouteManager man = RouteManager.getInstance();

            // Check that this system's salvage routes don't surpass max
			int curr = man.getNumRoutesFor(id);
            if (curr >= max) return;

            // Check that this manager's routes across all systems don't surpass max
            int totalRoutes = 0;
            for (LocationAPI loc : Global.getSector().getAllLocations()) {
                for (RouteData route : man.getRoutesInLocation(loc)) {
                    if (route.getSpawner() == this) totalRoutes++;
                }
            }
			if (totalRoutes >= maxTotal) return;


			addRouteFleetIfPossible();
		}
	}

	protected void addRouteFleetIfPossible() {
		if (TutorialMissionIntel.isTutorialInProgress()) {
			return;
		}

		MarketAPI market = pickSourceMarket();
		if (market == null) return;

		Long seed = new Random().nextLong();
		String id = getRouteSourceId();

		OptionalFleetData extra = new OptionalFleetData(market);

		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		picker.add(Roider_FleetTypes.EXPEDITION, 25f);
		picker.add(Roider_FleetTypes.MAJOR_EXPEDITION, getMajorExpeditionWeight(market));

		String type = picker.pick();

        boolean majorExpedition = type.equals(Roider_FleetTypes.MAJOR_EXPEDITION);

        CustomData customData = new CustomData(getCurrSystem(), majorExpedition);

        if (majorExpedition) extra.factionId = Roider_Factions.ROIDER_UNION;

		RouteData route = RouteManager.getInstance().addRoute(id,
                    market, seed, extra, this, customData);

		float distLY = Misc.getDistanceLY(market.getLocationInHyperspace(), getCurrSystem().getLocation());
		float travelDays = distLY * 1.5f;

		float prepDays = 2f + (float) Math.random() * 3f;
		float endDays = 8f + (float) Math.random() * 3f; // longer since includes time from jump-point to source

		float totalTravelTime = prepDays + endDays + travelDays * 2f;
		float stayDays = Math.max(20f, totalTravelTime);

		route.addSegment(new RouteSegment(prepDays, market.getPrimaryEntity()));
		route.addSegment(new RouteSegment(travelDays, market.getPrimaryEntity(), getCurrSystem().getCenter()));
		route.addSegment(new RouteSegment(stayDays, getCurrSystem().getCenter()));
		route.addSegment(new RouteSegment(travelDays, getCurrSystem().getCenter(), market.getPrimaryEntity()));
		route.addSegment(new RouteSegment(endDays, market.getPrimaryEntity()));


        // Add loot debris clouds
        int minor = 1 + random.nextInt(2);
        int major = 0;

        if (customData.majorExpedition) {
            if (random.nextBoolean()) minor++;
            major++;
        }

        for (int i = 0; i < minor; i++) {
            addLootDebrisField(totalTravelTime + stayDays, false,
                        route.getFactionId(), market);
        }

        for (int i = 0; i < major; i++) {
            addLootDebrisField(totalTravelTime + stayDays, true,
                        route.getFactionId(), market);
        }
	}

    private void addLootDebrisField(float duration, boolean major,
                String faction, MarketAPI source) {
        float diameter = 150f + random.nextFloat() * 50f;
        DebrisFieldParams params = new DebrisFieldParams(diameter,
                    1f, duration, 0);

		SectorEntityToken lootField = getCurrSystem().addTerrain(Terrain.DEBRIS_FIELD, params);
		lootField.setSensorProfile(1f);
		lootField.setDiscoverable(false);
		lootField.setName(((CampaignTerrainAPI) lootField).getPlugin().getTerrainName());

        lootField.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_LOOT, true);
        if (major) lootField.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_LOOT_MAJOR, true);


        if (faction.equals(Factions.INDEPENDENT)) faction = Factions.PIRATES;
        lootField.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_FACTION, faction);

        lootField.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_MARKET, source.getId());

		float range = DebrisFieldTerrainPlugin.computeDetectionRange(params.bandWidthInEngine);
		lootField.getDetectedRangeMod().modifyFlat("gen", range);

		lootField.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, random.nextLong());

        // Pick location
        EntityLocation loc = BaseThemeGenerator.pickHiddenLocationNotNearStar(
                    random, getCurrSystem(), 50f + random.nextFloat() * 100f, null);

        lootField.setOrbit(loc.orbit);
        getCurrSystem().addEntity(lootField);
    }


	public static float getVeryApproximateSalvageValue(StarSystemAPI system) {
		return system.getEntitiesWithTag(Tags.SALVAGEABLE).size();
	}

	public static MarketAPI pickSourceMarket() {
		WeightedRandomPicker<MarketAPI> markets = new WeightedRandomPicker<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            float weight = 0;

//			if (market.isHidden()) continue;
			if (!market.hasSpaceport()) continue; // markets w/o spaceports don't launch fleets

            if (market.hasIndustry(Roider_Industries.UNION_HQ)
                        && market.getIndustry(Roider_Industries.UNION_HQ).isFunctional()) {
                weight += market.getSize() * 2;
            }

            if (market.hasIndustry(Roider_Industries.DIVES)) {
                weight += market.getSize();
            }

            if (market.getFactionId().equals(Roider_Factions.ROIDER_UNION)) {
                weight += market.getSize() / 2;
                weight *= 2;
            }

            markets.add(market, weight);
		}
		return markets.pick();
	}

    public static StarSystemAPI pickTargetSystem() {
        WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>();

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (Global.getSector().getPlayerFleet().getContainingLocation() == system) continue;
            if (system.getJumpPoints().isEmpty()) continue;
            if (!system.isProcgen()) continue;
            if (system.hasTag(Tags.THEME_UNSAFE)) continue;
            if (system.hasTag(Tags.THEME_HIDDEN)) continue;
            if (!Global.getSector().getEconomy().getMarkets(system).isEmpty()) continue;
            if (system.getType() != StarSystemGenerator.StarSystemType.NEBULA
                        && system.getStar() == null) continue;


            picker.add(system, getVeryApproximateSalvageValue(system) + 1);
        }

        return picker.pick();
    }

    public float getMajorExpeditionWeight(MarketAPI market) {
        if (!market.hasIndustry(Roider_Industries.UNION_HQ)) return 0f;
        if (!market.getIndustry(Roider_Industries.UNION_HQ).isFunctional()) return 0f;
        if (market.getFaction().isHostileTo(Roider_Factions.ROIDER_UNION)) return 0f;
        if (delay > 0) return 0;

        return 5f;
    }


	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random random = route.getRandom();

        // Chance for indie expedition to secretly be pirates
		boolean pirate = false;
        if (route.getFactionId().equals(Factions.INDEPENDENT)) pirate = random.nextBoolean();

        CustomData data = (CustomData) route.getCustom();

        String type = Roider_FleetTypes.EXPEDITION;
        if (data.majorExpedition) type = Roider_FleetTypes.MAJOR_EXPEDITION;

        StarSystemAPI system = Global.getSector().getStarSystem(data.system);

		CampaignFleetAPI fleet = createScavenger(type, system.getLocation(),
                    route, route.getMarket(), pirate, random);

		if (fleet == null) return null;

		fleet.addScript(new Roider_TechExpeditionFleetAssignmentAI(fleet, route, pirate));

		return fleet;
	}

	public static CampaignFleetAPI createScavenger(String type, Vector2f locInHyper, MarketAPI source, boolean pirate, Random random) {
		return createScavenger(type, locInHyper, null, source, pirate, random);
	}
	public static CampaignFleetAPI createScavenger(String type, Vector2f locInHyper, RouteData route, MarketAPI source, boolean pirate, Random random) {
		if (random == null) random = new Random();


		if (type == null) {
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
			picker.add(Roider_FleetTypes.EXPEDITION, 25f);
//			picker.add(Roider_FleetTypes.MAJOR_EXPEDITION, 0f);
			type = picker.pick();
		}

        if (route == null) {
            route = new RouteData("temp", source, random.nextLong(),
                        new OptionalFleetData());
        }


		int combat = 0;
		int freighter = 0;
		int tanker = 0;
		int transport = 0;
		int utility = 0;

        switch (type) {
            case Roider_FleetTypes.MOTHER_EXPEDITION:
            case Roider_FleetTypes.MAJOR_EXPEDITION:
                combat = 7 + random.nextInt(8);
                freighter = 6 + random.nextInt(7);
                tanker = 5 + random.nextInt(6);
                transport = 3 + random.nextInt(8);
                utility = 4 + random.nextInt(5);
                break;
            case Roider_FleetTypes.EXPEDITION:
                combat = random.nextInt(2) + 1;
                tanker = random.nextInt(2) + 1;
                utility = random.nextInt(2) + 1;
                break;
            default:
                combat = 4 + random.nextInt(5);
                freighter = 4 + random.nextInt(5);
                tanker = 3 + random.nextInt(4);
                transport = random.nextInt(2);
                utility = 2 + random.nextInt(3);
                break;
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

        // Get half the ships from Roider Union's choices
		FleetParamsV3 params = new FleetParamsV3(
				source,
				null, // loc in hyper; don't need if have market
				Roider_Factions.ROIDER_UNION,
				route.getQualityOverride(), // quality override
				type,
				combat / 2, // combatPts
				freighter / 2, // freighterPts
				tanker / 2, // tankerPts
				transport, // transportPts
				0f, // linerPts
				utility, // utilityPts
				0f // qualityMod
        );
		params.timestamp = route.getTimestamp();
		params.random = random;
//		params.modeOverride = Misc.getShipPickMode(market);
		params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

		if (fleet == null || fleet.isEmpty()) return null;


        // Get the other half from the market's faction
		FleetParamsV3 params2 = new FleetParamsV3(
				source,
				null, // loc in hyper; don't need if have market
				null,
				null, // quality override
				type,
				combat / 2, // combatPts
				freighter / 2, // freighterPts
				tanker / 2, // tankerPts
				transport, // transportPts
				0f, // linerPts
				utility, // utilityPts
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

        Roider_Misc.sortFleetByShipSize(fleet);
        FleetFactoryV3.addCommanderAndOfficers(fleet, params, random);

//        fleet.setFaction(source.getFactionId(), true);
		if (type.equals(Roider_FleetTypes.MAJOR_EXPEDITION)) fleet.setFaction(Roider_Factions.ROIDER_UNION, true);
        else fleet.setFaction(source.getFactionId(), true);

        fleet.setName(fleet.getFaction().getFleetTypeName(type));

		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SCAVENGER, true);

		if (pirate) {
			Misc.makeLowRepImpact(fleet, "scav");
		}

		return fleet;
	}

    private StarSystemAPI getCurrSystem() {
        return Global.getSector().getStarSystem(currSystem);
    }

	public boolean shouldCancelRouteAfterDelayCheck(RouteData data) {
		return false;
	}

	public boolean shouldRepeat(RouteData route) {
		return false;
	}

	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
	}
}







