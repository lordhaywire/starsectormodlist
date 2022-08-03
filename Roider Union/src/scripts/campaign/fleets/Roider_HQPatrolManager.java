package scripts.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;
import com.fs.starfarer.api.impl.campaign.fleets.*;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_FleetTypes;
import java.util.Random;
import scripts.Roider_Misc;

/**
 * Author: SafariJohn
 */
public class Roider_HQPatrolManager implements RouteFleetSpawner, FleetEventListener {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_HQPatrolManager.class, "tracker", "t");
        x.aliasAttribute(Roider_HQPatrolManager.class, "market", "m");
        x.aliasAttribute(Roider_HQPatrolManager.class, "returningPatrolValue", "r");
    }

	private final IntervalUtil tracker;
    private final MarketAPI market;

	private float returningPatrolValue;

    public Roider_HQPatrolManager(MarketAPI market) {
        tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f,
					Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);
        returningPatrolValue = 0f;
        this.market = market;
    }

    public IntervalUtil getTracker() {
        return tracker;
    }

    public void advance(float amount, boolean functional) {
        if (!functional) return;
        if (!market.isInEconomy()) return;

		float spawnRate = 1f;
		float rateMult;
        if (market != null) rateMult = market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).getModifiedValue();
        else rateMult = 1;
		spawnRate *= rateMult;

		float days = Global.getSector().getClock().convertToDays(amount);

		float extraTime = 0f;
		if (returningPatrolValue > 0) {
			// apply "returned patrols" to spawn rate, at a maximum rate of 1 interval per day
			float interval = tracker.getIntervalDuration();
			extraTime = interval * days;
			returningPatrolValue -= days;
			if (returningPatrolValue < 0) returningPatrolValue = 0;
		}
		tracker.advance(days * spawnRate + extraTime);

		//tracker.advance(days * spawnRate * 100f);

		if (tracker.intervalElapsed()) {
			int maxLight = 0;
			int maxMedium = 0;
			int maxHeavy = 0;

            int size = market.getSize();

            if (size <= 3) {
                maxLight = 2;
                maxMedium = 0;
                maxHeavy = 0;
            } else if (size == 4) {
                maxLight = 2;
                maxMedium = 0;
                maxHeavy = 0;
            } else if (size == 5) {
                maxLight = 2;
                maxMedium = 1;
                maxHeavy = 0;
            } else if (size == 6) {
                maxLight = 3;
                maxMedium = 1;
                maxHeavy = 0;
            } else if (size == 7) {
                maxLight = 3;
                maxMedium = 2;
                maxHeavy = 0;
            } else if (size == 8) {
                maxLight = 3;
                maxMedium = 3;
                maxHeavy = 0;
            } else if (size >= 9) {
                maxLight = 4;
                maxMedium = 3;
                maxHeavy = 0;
            }

            maxMedium = Math.max(maxMedium + 1, size / 2 - 1);
            maxHeavy = Math.max(maxHeavy, maxMedium - 1);

			int light = getCount(FleetFactory.PatrolType.FAST);
			int medium = getCount(FleetFactory.PatrolType.COMBAT);
			int heavy = getCount(FleetFactory.PatrolType.HEAVY);

			WeightedRandomPicker<FleetFactory.PatrolType> picker = new WeightedRandomPicker<>();
			if (maxHeavy - heavy > 0) picker.add(FleetFactory.PatrolType.HEAVY, maxHeavy - heavy);
			if (maxMedium - medium > 0) picker.add(FleetFactory.PatrolType.COMBAT, maxMedium - medium);
			if (maxLight - light > 0) picker.add(FleetFactory.PatrolType.FAST, maxLight - light);

			if (picker.isEmpty()) return;

			FleetFactory.PatrolType type = picker.pick();
			MilitaryBase.PatrolFleetData custom = new MilitaryBase.PatrolFleetData(type);

			OptionalFleetData extra = new OptionalFleetData(market);
			extra.fleetType = type.getFleetType();

			String sid = getRouteSourceId();
			RouteData route = RouteManager.getInstance().addRoute(sid, market, Misc.genRandomSeed(), extra, this, custom);
			float patrolDays = 35f + (float) Math.random() * 10f;

			route.addSegment(new RouteSegment(patrolDays, market.getPrimaryEntity()));
		}
	}

	private int getCount(FleetFactory.PatrolType ... types) {
		int count = 0;
		for (RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId())) {
			if (data.getCustom() instanceof MilitaryBase.PatrolFleetData) {
				MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData) data.getCustom();
				for (FleetFactory.PatrolType type : types) {
					if (type == custom.type) {
						count++;
						break;
					}
				}
			}
		}
		return count;
	}

    @Override
    public CampaignFleetAPI spawnFleet(RouteData route) {

		MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData) route.getCustom();
		FleetFactory.PatrolType type = custom.type;

		Random random = route.getRandom();

		float combat = 0f;
		float tanker = 0f;
		float freighter = 0f;
		String fleetType = Roider_FleetTypes.PATROL_SMALL;
		switch (type) {
		case FAST:
			combat = Math.round(3f + (float) random.nextFloat() * 2f) * 5f;
			break;
		case COMBAT:
            fleetType = Roider_FleetTypes.PATROL_MEDIUM;
			combat = Math.round(6f + (float) random.nextFloat() * 3f) * 5f;
			tanker = Math.round((float) random.nextFloat()) * 5f;
			break;
		case HEAVY:
            fleetType = Roider_FleetTypes.PATROL_LARGE;
			combat = Math.round(10f + (float) random.nextFloat() * 5f) * 5f;
			tanker = Math.round((float) random.nextFloat()) * 10f;
			freighter = Math.round((float) random.nextFloat()) * 10f;
			break;
		}

        CampaignFleetAPI fleet;
        if (!route.getFactionId().equals(Roider_Factions.ROIDER_UNION)) {
            // Get half the ships from Roider Union's choices
            FleetParamsV3 params = new FleetParamsV3(
                    market,
                    null, // loc in hyper; don't need if have market
                    Roider_Factions.ROIDER_UNION,
                    route.getQualityOverride(), // quality override
                    fleetType,
                    combat / 2, // combatPts
                    freighter / 2, // freighterPts
                    tanker / 2, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f // qualityMod
            );
            params.timestamp = route.getTimestamp();
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

                    member.setCaptain(null);
                    if (member.isFlagship()) member.setFlagship(false);

                    fleet.getFleetData().addFleetMember(member);
                }
            }

            FleetFactoryV3.addCommanderAndOfficers(fleet, params, random);
            Roider_Misc.sortFleetByShipSize(fleet);
        } else {
            FleetParamsV3 params = new FleetParamsV3(
                    market,
                    null, // loc in hyper; don't need if have market
                    Roider_Factions.ROIDER_UNION,
                    route.getQualityOverride(), // quality override
                    fleetType,
                    combat, // combatPts
                    freighter, // freighterPts
                    tanker, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f // qualityMod
            );
            params.timestamp = route.getTimestamp();
            params.random = random;
    //		params.modeOverride = Misc.getShipPickMode(market);
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            fleet = FleetFactoryV3.createFleet(params);

            if (fleet == null || fleet.isEmpty()) return null;
        }

		fleet.setFaction(market.getFactionId(), true);
        fleet.setName(market.getFaction().getFleetTypeName(fleetType));
//		fleet.setNoFactionInName(true);

		fleet.addEventListener(this);

		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);

		String postId = Ranks.POST_PATROL_COMMANDER;
		String rankId = Ranks.SPACE_COMMANDER;
		switch (type) {
		case FAST:
			rankId = Ranks.SPACE_LIEUTENANT;
			break;
		case COMBAT:
			rankId = Ranks.SPACE_COMMANDER;
			break;
		case HEAVY:
			rankId = Ranks.SPACE_CAPTAIN;
			break;
		}

		fleet.getCommander().setPostId(postId);
		fleet.getCommander().setRankId(rankId);

		market.getContainingLocation().addEntity(fleet);
		fleet.setFacing((float) Math.random() * 360f);
		// this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
		fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

		fleet.addScript(new PatrolAssignmentAIV4(fleet, route));

		//market.getContainingLocation().addEntity(fleet);
		//fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

		if (custom.spawnFP <= 0) {
			custom.spawnFP = fleet.getFleetPoints();
		}

		return fleet;
    }

    @Override
    public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
        return false;
    }

    @Override
    public boolean shouldRepeat(RouteData route) {
        return false;
    }

	public String getRouteSourceId() {
		return market.getId() + "_" + "military";
	}

    @Override
    public void reportAboutToBeDespawnedByRouteManager(RouteData route) {}

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
//		if (!functional) return;

		if (reason == CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION) {
			RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
			if (route.getCustom() instanceof MilitaryBase.PatrolFleetData) {
				MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData) route.getCustom();
				if (custom.spawnFP > 0) {
					float fraction  = fleet.getFleetPoints() / custom.spawnFP;
					returningPatrolValue += fraction;
				}
			}
		}
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {}
}
