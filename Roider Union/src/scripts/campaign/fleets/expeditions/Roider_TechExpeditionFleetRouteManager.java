package scripts.campaign.fleets.expeditions;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.fleets.BaseRouteFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.thoughtworks.xstream.XStream;
import data.scripts.util.MagicSettings;
import ids.Roider_Ids.Roider_Entities;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_FleetTypes;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_Ids.Roider_Settings;
import ids.Roider_MemFlags;
import java.util.ArrayList;
import java.util.List;
import scripts.Roider_Debug;
import scripts.campaign.cleanup.Roider_ExpeditionLootCleaner;
import scripts.campaign.rulecmd.expeditionSpecials.Roider_PingTrapSpecial.Roider_PingTrapSpecialData;
import scripts.campaign.rulecmd.expeditionSpecials.Roider_ThiefTrapSpecial.Roider_ThiefTrapSpecialData;
import scripts.world.systems.Roider_Atka;

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
		super(6f, 14f);
		this.currSystem = pickTargetSystem().getId();
        delay = MagicSettings.getFloat(Roider_Settings.MAGIC_ID,
                    Roider_Settings.EXPEDITION_DELAY);
        if (Roider_Debug.TECH_EXPEDITIONS) delay = 0;
        this.random = new Random();
	}

	protected String getRouteSourceId() {
		return "roider_expedition";
	}

	protected int getMaxFleets() {
        if (Roider_Debug.TECH_EXPEDITIONS) return 1;

//		float salvage = getVeryApproximateSalvageValue(getCurrSystem());
//        int salvageFleets = (int) (1 + Math.min(salvage / 2, 7));

        int max = MagicSettings.getInteger(Roider_Settings.MAGIC_ID,
                    Roider_Settings.MAX_EXPEDITIONS);

        return max;

        // Limit by number of salvage routes in this system
        // and by this manager's total number of routes
//		return Math.min(max, salvageFleets);
	}

	public void advance(float amount) {
        float temp = interval.getElapsed();

        if (Roider_Debug.TECH_EXPEDITIONS) interval.setInterval(1f, 1f);
        else interval.setInterval(1f, 14f);

        interval.setElapsed(temp);

		float days = Misc.getDays(amount);
//        days *= 100;

        // Delay before first major expedition can start
		if (!TutorialMissionIntel.isTutorialInProgress() && delay > 0) {
            delay -= days;
        }

		interval.advance(days);
		if (interval.intervalElapsed()) {
            currSystem = pickTargetSystem().getId();

            if (Roider_Debug.TECH_EXPEDITIONS) currSystem = "Penelope's Star";

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

        if (market.getFactionId().equals(Factions.PLAYER)) extra.factionId = Factions.INDEPENDENT;
        if (majorExpedition) extra.factionId = Roider_Factions.ROIDER_UNION;

		RouteData route = RouteManager.getInstance().addRoute(id,
                    market, seed, extra, this, customData);

		float distLY = Misc.getDistanceLY(market.getLocationInHyperspace(), getCurrSystem().getLocation());
		float travelDays = distLY * 1.5f;

		float prepDays = 2f + (float) Math.random() * 3f;
		float endDays = 8f + (float) Math.random() * 3f; // longer since includes time from jump-point to source

		float totalTravelTime = prepDays + endDays + travelDays * 2f;
		float stayDays = Math.max(20f, totalTravelTime);

        // Departure segments
		route.addSegment(new RouteSegment(prepDays, market.getPrimaryEntity()));
		route.addSegment(new RouteSegment(travelDays, market.getPrimaryEntity(), getCurrSystem().getCenter()));

        // Add loot stashes
        int minor = 1 + random.nextInt(2);
        int major = random.nextInt(1);
        int fake = 1 + random.nextInt(2);

        if (customData.majorExpedition) {
            if (random.nextBoolean()) minor++;
            major++;
        }

        String thiefId = Misc.genUID();

        List<SectorEntityToken> stashes = new ArrayList<>();
        // Minor stashes
        for (int i = 0; i < minor; i++) {
            AddedEntity stash = createLootStash(getCurrSystem(), BaseThemeGenerator.pickHiddenLocationNotNearStar(
                    random, getCurrSystem(), 50f + random.nextFloat() * 100f, null));

//            Roider_ExpeditionTrapCreator creator = new Roider_ExpeditionTrapCreator(random,
//                        0.9f, Roider_FleetTypes.MINING_FLEET,
//                        extra.factionId, market.getId(), 7, 14, false);
//
//            SpecialCreationContext context = new SalvageSpecialAssigner.SpecialCreationContext();
//
//            Object specialData = creator.createSpecial(stash.entity, context);
//            if (specialData != null) {
//                Misc.setSalvageSpecial(stash.entity, specialData);
//            }

            picker.clear();
            picker.add("thiefTrap", 10);
            picker.add("pingTrap", 10);
//            picker.add("droneDefenders", 1);

            String special = picker.pick();
            switch (special) {
                default:
                case "thiefTrap":
                    Misc.setSalvageSpecial(stash.entity, new Roider_ThiefTrapSpecialData());
                    break;
                case "pingTrap":
                    Misc.setSalvageSpecial(stash.entity, new Roider_PingTrapSpecialData());
                    break;
            }

            stash.entity.addTag(Tags.EXPIRES);
            stash.entity.removeTag(Tags.NEUTRINO_LOW);
            stash.entity.removeTag(Tags.NEUTRINO);
            stash.entity.addTag(Tags.NEUTRINO_HIGH);

//            Misc.setSalvageSpecial(stash.entity, new Roider_ThiefTrapSpecialData());

            stash.entity.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_LOOT, true);
//            stash.entity.setSensorProfile(1f);
            stash.entity.getDetectedRangeMod().unmodify();
//            stash.entity.getDetectedRangeMod().modifyMult("roider_loot", 0.5f);

            stash.entity.getMemoryWithoutUpdate().set(Roider_MemFlags.THIEF_KEY, thiefId);

            stashes.add(stash.entity);
        }

        // Major stashes
        for (int i = 0; i < major; i++) {
            stashes.add((createMajorLootStash(totalTravelTime + stayDays,
                        route.getFactionId(), market, thiefId)));
        }

        // Fake stashes
        for (int i = 0; i < fake; i++) {
            EntityLocation loc = BaseThemeGenerator.pickHiddenLocationNotNearStar(
                    random, getCurrSystem(), 50f + random.nextFloat() * 100f, null);

            Vector2f currLoc = loc.location;
            if (loc.orbit != null) currLoc = loc.orbit.computeCurrentLocation();
            if (currLoc == null) currLoc = new Vector2f();

            SectorEntityToken token = getCurrSystem().createToken(currLoc);
            getCurrSystem().addEntity(token);

            if (loc.orbit != null) token.setOrbit(loc.orbit);

            stashes.add(token);
        }

        RouteSegment seg = new RouteSegment(stayDays, getCurrSystem().getCenter());
        seg.custom = stashes;
        route.addSegment(seg);

        // Return segments
		route.addSegment(new RouteSegment(travelDays, getCurrSystem().getCenter(), market.getPrimaryEntity()));
		route.addSegment(new RouteSegment(endDays, market.getPrimaryEntity()));


        // Delay and intel
		float routeDelay = prepDays;
		routeDelay *= 0.75f + (float) Math.random() * 0.5f;
		routeDelay = (int) routeDelay;
		route.setDelay(routeDelay);

        BaseIntelPlugin intel = new Roider_TechExpeditionIntel(route, market.getId(),
                    getCurrSystem().getId(), extra.factionId, thiefId);
		intel.setPostingRangeLY(1f, true);
		intel.setPostingLocation(market.getPrimaryEntity());

		Global.getSector().getIntelManager().queueIntel(intel, routeDelay + 3f);
        Global.getSector().addScript(intel);
	}

    private SectorEntityToken createMajorLootStash(float duration,
                String faction, MarketAPI source, String thiefId) {
        SectorEntityToken stash;

        String id = "roider_stash_" + Misc.genUID();
        stash = getCurrSystem().addCustomEntity(id + "_major", null, Roider_Entities.LOOT_STASH_MAJOR, null);

        stash.addTag(Tags.EXPIRES);

//        stash.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_LOOT, true);
        stash.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_LOOT_MAJOR, true);

        if (faction.equals(Factions.INDEPENDENT)) faction = Factions.PIRATES;
        stash.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_FACTION, faction);

        stash.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_MARKET, source.getId());
		stash.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, random.nextLong());
        stash.getMemoryWithoutUpdate().set(Roider_MemFlags.THIEF_KEY, thiefId);

        // Pick location
        EntityLocation loc = BaseThemeGenerator.pickHiddenLocationNotNearStar(
                    random, getCurrSystem(), 50f + random.nextFloat() * 100f, null);

        Vector2f currLoc = loc.location;
        if (loc.orbit != null) currLoc = loc.orbit.computeCurrentLocation();
        if (currLoc == null) currLoc = new Vector2f();

        SectorEntityToken token = getCurrSystem().createToken(currLoc);
        if (loc.orbit != null) token.setOrbit(loc.orbit);
        getCurrSystem().addEntity(token);
        token.removeTag(Tags.HAS_INTERACTION_DIALOG);

        OrbitAPI zOrbit = Global.getFactory().createCircularOrbit(token, 0, 0, 1);

        stash.setOrbit(zOrbit);
        getCurrSystem().addEntity(stash);

        // Save entity in token's data for later access
        // Have to avoid a ConcurrentModificationException
        token.getCustomData().put(Roider_ExpeditionStashPickupScript.STASH_ENTITY_KEY, stash);

//        String salvageSpecId = pickMajorSalvageSpec(stash);
//        stash.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPEC_ID_OVERRIDE, salvageSpecId);

        Global.getSector().addScript(new Roider_ExpeditionLootCleaner(token, duration));


        return token;
    }

    private AddedEntity createLootStash(StarSystemAPI system, EntityLocation loc) {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker();
        picker.add(Entities.WRECK, 50f);
        picker.add(Entities.WEAPONS_CACHE, 4f);
        picker.add(Entities.WEAPONS_CACHE_SMALL, 10f);
        picker.add(Entities.WEAPONS_CACHE_HIGH, 4f);
        picker.add(Entities.WEAPONS_CACHE_SMALL_HIGH, 10f);
        picker.add(Entities.WEAPONS_CACHE_LOW, 4f);
        picker.add(Entities.WEAPONS_CACHE_SMALL_LOW, 10f);
        picker.add(Entities.SUPPLY_CACHE, 4f);
        picker.add(Entities.SUPPLY_CACHE_SMALL, 10f);
        picker.add(Entities.EQUIPMENT_CACHE, 4f);
        picker.add(Entities.EQUIPMENT_CACHE_SMALL, 10f);
        String type = picker.pick();

        if (type.equals(Entities.WRECK)) {
            List<String> factions = MagicSettings.getList(Roider_Settings.MAGIC_ID,
                        Roider_Settings.EXPEDITION_LOOT_FACTIONS);

            picker.clear();
            picker.addAll(factions);
            int iter = 0;
            do {
                if (iter > 110) break;

                String faction = picker.pick();
                DerelictShipData params = DerelictShipEntityPlugin.createRandom(faction, null, random, DerelictShipEntityPlugin.getDefaultSModProb());
                iter++;

                if (params != null) {
                    if (params.ship.getVariant().getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP && iter < 100) {
                        continue;
                    }

                    CustomCampaignEntityAPI wreck = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(random, system,
                                                    Entities.WRECK, Factions.NEUTRAL, params);
                    wreck.setDiscoverable(true);
                    BaseThemeGenerator.setEntityLocation(wreck,
                                loc, Entities.WRECK);

                    BaseThemeGenerator.AddedEntity added = new BaseThemeGenerator.AddedEntity(wreck,
                                null, Entities.WRECK);
                    return added;
                }
            } while (true);
        }


        return BaseThemeGenerator.addEntity(random, system,
                    loc, type, Factions.NEUTRAL);
    }

//    private void initWreckPlugin(SectorEntityToken stash, DerelictShipData params) {
//        float profile = stash.getSensorProfile();
//        StatMod genProfile = stash.getDetectedRangeMod().getFlatBonus("gen");
//
//        DerelictShipEntityPlugin plugin = new DerelictShipEntityPlugin();
//        plugin.init(stash, params);
//
//        stash.setDiscoveryXP(0f);
//        stash.setSensorProfile(profile);
//        if (genProfile == null) stash.getDetectedRangeMod().modifyFlat("gen", 0);
//        else stash.getDetectedRangeMod().modifyFlat("gen", genProfile.getValue());
//        stash.getMemoryWithoutUpdate().set(Roider_MemFlags.EXPEDITION_WRECK_PLUGIN, plugin);
//    }

	public static float getVeryApproximateSalvageValue(StarSystemAPI system) {
		return system.getEntitiesWithTag(Tags.SALVAGEABLE).size();
	}

	public static MarketAPI pickSourceMarket() {
        if (Roider_Debug.TECH_EXPEDITIONS) return Global.getSector()
                    .getStarSystem(Roider_Atka.PRIMARY.name)
                    .getEntityById(Roider_Atka.KOROVIN.id).getMarket();

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
            if (system.getHyperspaceAnchor() == null) continue;
            if (system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) continue;
            if (system.getJumpPoints().isEmpty()) continue;
            if (!system.isProcgen()) continue;
            if (!system.hasTag(Tags.THEME_REMNANT)) continue;
            if (system.hasTag(Tags.THEME_HIDDEN)) continue;
            if (!Global.getSector().getEconomy().getMarkets(system).isEmpty()) continue;
            if (system.getType() != StarSystemGenerator.StarSystemType.NEBULA
                        && system.getStar() == null) continue;

            picker.add(system, getVeryApproximateSalvageValue(system) + 1f);
        }

        StarSystemAPI pick = picker.pick();
        if (pick != null) return pick;


        // Try without picking remnant systems
        picker.clear();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (Global.getSector().getPlayerFleet().getContainingLocation() == system) continue;
            if (system.getHyperspaceAnchor() == null) continue;
            if (system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) continue;
            if (system.getJumpPoints().isEmpty()) continue;
            if (!system.isProcgen()) continue;
            if (system.hasTag(Tags.THEME_HIDDEN)) continue;
            if (!Global.getSector().getEconomy().getMarkets(system).isEmpty()) continue;
            if (system.getType() != StarSystemGenerator.StarSystemType.NEBULA
                        && system.getStar() == null) continue;

            picker.add(system, getVeryApproximateSalvageValue(system) + 1f);
        }

        pick = picker.pick();
        if (pick != null) return pick;

        // If this fails it is the player's fault.
        return Global.getSector().getStarSystems().get(new Random().nextInt(Global.getSector().getStarSystems().size()));
    }

    public float getMajorExpeditionWeight(MarketAPI market) {
        if (!market.hasIndustry(Roider_Industries.UNION_HQ)) return 0f;
        if (!market.getIndustry(Roider_Industries.UNION_HQ).isFunctional()) return 0f;
        if (market.getFaction().isHostileTo(Roider_Factions.ROIDER_UNION)) return 0f;
        if (delay > 0) return 0;

        return 5f;
    }


	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random rRand = route.getRandom();

        // Chance for indie expedition to secretly be pirates
		boolean pirate = false;
        if (route.getFactionId().equals(Factions.INDEPENDENT)) pirate = rRand.nextBoolean();

        CustomData data = (CustomData) route.getCustom();

        String type = Roider_FleetTypes.EXPEDITION;
        if (data.majorExpedition) type = Roider_FleetTypes.MAJOR_EXPEDITION;

        StarSystemAPI system = Global.getSector().getStarSystem(data.system);

		CampaignFleetAPI fleet = Roider_ExpeditionFleetFactory.createExpedition(
                    type, system.getLocation(),
                    route, route.getMarket(), pirate, rRand);

		if (fleet == null) return null;

        fleet.setNoAutoDespawn(true);

		fleet.addScript(new Roider_TechExpeditionFleetAssignmentAI(fleet, route, pirate));

        // If fleet spawns while returning, then add random drops and mothballed ships
        if ((route.getCurrent().from == route.getMarket() || route.getCurrent().to == route.getMarket())
                    && route.getCurrentIndex() > 1) {
            int limit = random.nextInt(4);
            for (int i = 0; i < limit; i++) {
                Roider_ExpeditionStashPickupScript.genMinorStashAndAdd(fleet, random);
            }
            if (data.majorExpedition) {
                Roider_ExpeditionStashPickupScript.genMajorStashAndAdd(fleet, random);
            }
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







