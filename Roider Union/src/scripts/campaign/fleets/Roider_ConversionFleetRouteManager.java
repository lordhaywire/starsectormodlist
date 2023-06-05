package scripts.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.BaseRouteFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Equipment;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_FleetTypes;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_Ids.Roider_Settings;
import ids.Roider_Ids.Roider_Tags;
import ids.Roider_MemFlags;
import java.util.*;
import org.magiclib.util.MagicSettings;
import scripts.campaign.fleets.expeditions.Roider_ExpeditionFleetFactory;
import scripts.campaign.intel.Roider_ConversionFleetIntel;

/**
 * Author: SafariJohn
 */
public class Roider_ConversionFleetRouteManager extends BaseRouteFleetManager implements FleetEventListener {

    public static void aliasAttributes(XStream x) {
        x.alias("roider_cfdata", Roider_ConvFleetData.class);
        x.aliasAttribute(Roider_ConvFleetData.class, "offerings", "o");
        x.aliasAttribute(Roider_ConvFleetData.class, "loc", "l");
    }

    public static final String ROUTE_ID = "roider_conversionFleets";
    public static final float OFFERINGS_CHANCE = 0.8f;

    public class Roider_ConvFleetData {
        public final List<String> offerings;
        public final EntityLocation loc;

        public Roider_ConvFleetData(List<String> offerings, EntityLocation loc) {
            this.offerings = offerings;
            this.loc = loc;
        }
    }

    public Roider_ConversionFleetRouteManager(float minInterval, float maxInterval) {
        super(minInterval, maxInterval);
    }

    @Override
    protected String getRouteSourceId() {
        return ROUTE_ID;
    }

    @Override
    protected int getMaxFleets() {
        return MagicSettings.getInteger(Roider_Settings.MAGIC_ID, Roider_Settings.APR_MAX_FLEETS);
    }

    protected Map<String, Float> getFactions() {
        return MagicSettings.getFloatMap(Roider_Settings.MAGIC_ID, Roider_Settings.APR_FACTIONS);
    }

    @Override
    protected void addRouteFleetIfPossible() {
        // Recalculate faction weights
        Map<String, Float> fWeights = new HashMap<>();
        Map<String, Float> factions = getFactions();
        for (String id : factions.keySet()) {
            Float tally = 0f;

            for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
                if (m.getFactionId().equals(id) && (m.hasIndustry(Roider_Industries.DIVES)
                            || m.hasIndustry(Roider_Industries.UNION_HQ))) {
                    tally += 1f * factions.get(id);
                }
            }

            fWeights.put(id, tally);
        }

        // Pick faction
        WeightedRandomPicker picker = new WeightedRandomPicker<>();
        for (String id : fWeights.keySet()) {
            picker.add(id, fWeights.get(id));
        }

        String factionId = (String) picker.pick();

        if (factionId == null) factionId = Factions.PIRATES;


        picker.clear();
        MarketAPI market = null;
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (m.getFactionId().equals(factionId)) {
                float weight = m.getShipQualityFactor();

                if (m.hasIndustry(Roider_Industries.UNION_HQ)) weight += weight;
                else if (m.hasIndustry(Roider_Industries.DIVES)) weight += weight / 10f;

                picker.add(m, weight);
            }
        }

        market = (MarketAPI) picker.pick();

        if (market == null) return;


        // Pick location
        StarSystemAPI system = pickSystem();

        if (system == null) {
            return;
        }

//        if (true) system = Global.getSector().getStarSystem("Ounalashka");

        LinkedHashMap<BaseThemeGenerator.LocationType, Float> lWeights = new LinkedHashMap<>();
        lWeights.put(LocationType.IN_ASTEROID_BELT, 10f);
        lWeights.put(LocationType.IN_ASTEROID_FIELD, 10f);
        lWeights.put(LocationType.IN_RING, 10f);
        lWeights.put(LocationType.IN_SMALL_NEBULA, 10f);
        lWeights.put(LocationType.GAS_GIANT_ORBIT, 10f);
        lWeights.put(LocationType.PLANET_ORBIT, 10f);
        WeightedRandomPicker<EntityLocation> locs = BaseThemeGenerator.getLocations(null, system, null, 100f, lWeights);

        EntityLocation loc;
        do {
            if (locs.isEmpty()) return;

            loc = locs.pickAndRemove();

            if (loc == null) return;

            // orbit focus must not be hostile to fleet faction
        } while (loc.orbit != null && loc.orbit.getFocus().getFaction().isHostileTo(factionId));

        if (loc.location == null) loc.location = loc.orbit.getFocus().getLocation();


        Random random = new Random();

        // Generate offerings
        List<String> offerings = new ArrayList<>();
        for (String known : Global.getSector().getFaction(factionId).getKnownShips()) {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(known);
            if (spec == null) continue;

            if (!spec.hasTag(Roider_Tags.RETROFIT)) continue;
            if (random.nextFloat() > OFFERINGS_CHANCE) continue;

            offerings.add(known);
        }

        for (String known : Global.getSector().getFaction(Roider_Factions.ROIDER_UNION).getKnownShips()) {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(known);
            if (spec == null) continue;

            if (!spec.hasTag(Roider_Tags.RETROFIT)) continue;
            if (random.nextFloat() > OFFERINGS_CHANCE) continue;

            offerings.add(known);
        }

        if (offerings.isEmpty()) offerings.add("roider_cyclops");


        Roider_ConvFleetData data = new Roider_ConvFleetData(offerings, loc);

        OptionalFleetData extra = new OptionalFleetData();
        extra.factionId = factionId;
        extra.fleetType = Roider_FleetTypes.MOTHER_EXPEDITION;

        RouteData route = RouteManager.getInstance().addRoute(getRouteSourceId(), market, new Random().nextLong(), extra, this, data);

        route.addSegment(new RouteManager.RouteSegment(60f, system.createToken(loc.location)));
    }

	protected StarSystemAPI pickSystem() {
        Random random = new Random();
		WeightedRandomPicker<StarSystemAPI> far = new WeightedRandomPicker<>(random);
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>(random);

		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float days = Global.getSector().getClock().getElapsedDaysSince(system.getLastPlayerVisitTimestamp());
			if (days < 45f) continue;

			float weight = 0f;
			if (system.hasTag(Tags.THEME_MISC_SKIP)) {
				weight = 2f;
			} else if (system.hasTag(Tags.THEME_MISC)) {
				weight = 6f;
			} else if (system.hasTag(Tags.THEME_REMNANT_NO_FLEETS)) {
				weight = 6f;
			} else if (system.hasTag(Tags.THEME_RUINS)) {
				weight = 10f;
			} else if (system.hasTag(Tags.THEME_CORE_UNPOPULATED)) {
				weight = 1f;
			} else if (system.hasTag(Tags.THEME_CORE_POPULATED)) {
				weight = 1f;
			}
			if (weight <= 0f) continue;

			if (Misc.hasPulsar(system)) continue;

			float dist = system.getLocation().length();



//			float distMult = 1f - dist / 20000f;
//			if (distMult > 1f) distMult = 1f;
//			if (distMult < 0.1f) distMult = 0.1f;

			float distMult = 1f;

			if (dist > 36000f) {
				far.add(system, weight * distMult);
			} else {
				picker.add(system, weight * distMult);
			}
		}

		if (picker.isEmpty()) {
			picker.addAll(far);
		}

		return picker.pick();
	}

    @Override
    public CampaignFleetAPI spawnFleet(RouteData route) {
        Roider_ConvFleetData data = (Roider_ConvFleetData) route.getCustom();
        StarSystemAPI system = (StarSystemAPI) data.loc.orbit.getFocus().getContainingLocation();

        Random random = new Random(route.getSeed());

		CampaignFleetAPI fleet = Roider_ExpeditionFleetFactory.createExpedition(
                    route.getExtra().fleetType, system.getLocation(),
                    route, route.getMarket(), false, random);

        if (fleet == null) return null;

        if (!hasAnArgos(fleet)) {
            List<String> argosVariants = new ArrayList<>();
            argosVariants.add("roider_argos_Outdated");
            argosVariants.add("roider_argos_Balanced");
            argosVariants.add("roider_argos_Support");

            String variant = argosVariants.get(random.nextInt(argosVariants.size()));

            FleetMemberAPI argos = fleet.getFleetData().addFleetMember(variant);
            argos.getRepairTracker().setCR(0.7f);

            FleetMemberAPI fFlag = fleet.getFlagship();
            // Move captain
            argos.setCaptain(fFlag.getCaptain());
            fFlag.setCaptain(null);
            // Change flagship
            fFlag.setFlagship(false);
            argos.setFlagship(true);
            fleet.getFleetData().setFlagship(argos);

            fleet.getFleetData().sort();
            fleet.getFleetData().setSyncNeeded();
        }


        fleet.clearAssignments();

        SectorEntityToken token = route.getCurrent().from;
        token.setOrbit(data.loc.orbit);

        fleet.setLocation(token.getLocation().x, token.getLocation().y);
        system.addEntity(fleet);

        float daysRemaining = route.getCurrent().daysMax;
        daysRemaining -= route.getCurrent().elapsed;

        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, token, daysRemaining, "offering retrofit services");
        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, token, Short.MAX_VALUE, "Dispersing");

        MemoryAPI fleetMemory = fleet.getMemoryWithoutUpdate();
        fleetMemory.set(Roider_MemFlags.APR_OFFERINGS, data.offerings);
        fleetMemory.set(Roider_MemFlags.APR_RETROFITTING, true);
        Misc.setFlagWithReason(fleetMemory, MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE,
                    ROUTE_ID, true, Short.MAX_VALUE);
        Misc.setFlagWithReason(fleetMemory, MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE,
                    ROUTE_ID, true, Short.MAX_VALUE);

        if (!fleet.getFaction().getCustomBoolean(Factions.CUSTOM_OFFERS_COMMISSIONS)) {
            fleetMemory.set(Roider_MemFlags.APR_IGNORE_COM, true);
        }

        if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
            fleetMemory.set(Roider_MemFlags.APR_IGNORE_REP, true);
        }

        if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE)) {
            fleetMemory.set(Roider_MemFlags.APR_IGNORE_TRANSPONDER, true);
        }

        if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
            fleet.setTransponderOn(false);
        } else {
            fleet.setTransponderOn(true);
        }

        fleet.removeAbility(Abilities.TRANSPONDER);

        Roider_ConversionFleetIntel intel = new Roider_ConversionFleetIntel(fleet, daysRemaining);
//        Global.getSector().getIntelManager().addIntel(intel, true);
        Global.getSector().addScript(intel);

        fleet.addEventListener(this);
        fleet.addEventListener(intel);

        return fleet;
    }

    private boolean hasAnArgos(CampaignFleetAPI fleet) {
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            if (m.getHullId().contains("roider_argos")) return true;
        }

        return false;
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        // If player and fleet on opposite sides, no longer offer retrofit services
        if (battle.isPlayerInvolved() && !battle.onPlayerSide(fleet)) {
//            fleet.getMemoryWithoutUpdate().unset(Roider_MemFlags.APR_OFFERINGS);
            fleet.getMemoryWithoutUpdate().unset(Roider_MemFlags.APR_RETROFITTING);
//            Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE,
//                        ROUTE_ID, false, Short.MAX_VALUE);
//            Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE,
//                        ROUTE_ID, false, Short.MAX_VALUE);
        }

        // If lost all Argosi, disperse
        boolean hasArgos = false;
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            if (m.getHullId().startsWith(Roider_Equipment.ARGOS)) {
                hasArgos = true;
                break;
            }
        }
        if (!hasArgos) {
            SectorEntityToken token = fleet.getContainingLocation().createToken(fleet.getLocation());

            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, token, Short.MAX_VALUE, "Dispersing");
        }
    }

    @Override
    public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
        return false;
    }

    @Override
    public boolean shouldRepeat(RouteData route) {
        return false;
    }

    @Override
    public void reportAboutToBeDespawnedByRouteManager(RouteData route) {}

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {}

}
