package scripts.campaign.fleets.expeditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import ids.Roider_Ids;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_FleetTypes;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;
import scripts.Roider_Misc;

/**
 * Author: SafariJohn
 */
public class Roider_ExpeditionFleetFactory {
    public static class Roider_FleetPointParams {
        public float combat = 0;
        public float freighter = 0;
        public float tanker = 0;
        public float transport = 0;
        public float liner = 0;
        public float utility = 0;

        public float sum() {
            return combat + freighter + tanker + transport + liner + utility;
        }
    }


	public static CampaignFleetAPI createExpedition(String type, Vector2f locInHyper, MarketAPI source, boolean pirate, Random random) {
		return createExpedition(type, locInHyper, null, source, pirate, random);
	}
	public static CampaignFleetAPI createExpedition(String type, Vector2f locInHyper, RouteManager.RouteData route, MarketAPI source, boolean pirate, Random random) {
		if (random == null) random = new Random();

		if (type == null) {
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
			picker.add(Roider_FleetTypes.EXPEDITION, 25f);
//			picker.add(Roider_FleetTypes.MAJOR_EXPEDITION, 0f);
			type = picker.pick();
		}

        if (route == null) {
            route = new RouteManager.RouteData("temp", source, random.nextLong(),
                        new RouteManager.OptionalFleetData());
        }

        Roider_FleetPointParams points = generateFleetPoints(type, random);

//		int combat = 0;
//		int freighter = 0;
//		int tanker = 0;
//		int transport = 0;
//		int utility = 0;
//
//        switch (type) {
//            case Roider_Ids.Roider_FleetTypes.MOTHER_EXPEDITION:
//            case Roider_Ids.Roider_FleetTypes.MAJOR_EXPEDITION:
//                combat = 7 + random.nextInt(8);
//                freighter = 6 + random.nextInt(7);
//                tanker = 5 + random.nextInt(6);
//                transport = 3 + random.nextInt(8);
//                utility = 4 + random.nextInt(5);
//                break;
//            case Roider_Ids.Roider_FleetTypes.EXPEDITION:
//                combat = random.nextInt(2) + 1;
//                tanker = random.nextInt(2) + 1;
//                utility = random.nextInt(2) + 1;
//                break;
//            default:
//                combat = 4 + random.nextInt(5);
//                freighter = 4 + random.nextInt(5);
//                tanker = 3 + random.nextInt(4);
//                transport = random.nextInt(2);
//                utility = 2 + random.nextInt(3);
//                break;
//        }

		if (pirate) {
//			combat += transport;
//			combat += utility;
//			transport = 0;
//            utility = 0;
		}

//		combat *= 5f;
//		freighter *= 3f;
//		tanker *= 3f;
//		transport *= 1.5f;

        CampaignFleetAPI fleet;
        if (!route.getFactionId().equals(Roider_Factions.ROIDER_UNION)) {
            // Get half the ships from Roider Union's choices
            FleetParamsV3 params = new FleetParamsV3(
                    source,
                    null, // loc in hyper; don't need if have market
                    Roider_Factions.ROIDER_UNION,
                    route.getQualityOverride(), // quality override
                    type,
                    points.combat / 2, // combatPts
                    points.freighter / 2, // freighterPts
                    points.tanker / 2, // tankerPts
                    points.transport / 2, // transportPts
                    points.liner / 2, // linerPts
                    points.utility / 2, // utilityPts
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
                    source,
                    null, // loc in hyper; don't need if have market
                    null,
                    null, // quality override
                    type,
                    points.combat / 2, // combatPts
                    points.freighter / 2, // freighterPts
                    points.tanker / 2, // tankerPts
                    points.transport / 2, // transportPts
                    points.liner / 2, // linerPts
                    points.utility / 2, // utilityPts
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
                    source,
                    null, // loc in hyper; don't need if have market
                    Roider_Factions.ROIDER_UNION,
                    route.getQualityOverride(), // quality override
                    type,
                    points.combat, // combatPts
                    points.freighter, // freighterPts
                    points.tanker, // tankerPts
                    points.transport, // transportPts
                    points.liner, // linerPts
                    points.utility, // utilityPts
                    0f // qualityMod
            );
            params.timestamp = route.getTimestamp();
            params.random = random;
    //		params.modeOverride = Misc.getShipPickMode(market);
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            fleet = FleetFactoryV3.createFleet(params);

            if (fleet == null || fleet.isEmpty()) return null;
        }

//        fleet.setFaction(source.getFactionId(), true);
		if (type.equals(Roider_FleetTypes.MAJOR_EXPEDITION)) fleet.setFaction(Roider_Ids.Roider_Factions.ROIDER_UNION, true);
        else fleet.setFaction(source.getFactionId(), true);

        fleet.setName(fleet.getFaction().getFleetTypeName(type));

		return fleet;
	}

    private static Roider_FleetPointParams generateFleetPoints(String type, Random random) {
        Roider_FleetPointParams points = new Roider_FleetPointParams();

        // Expedition has floor size and ceiling size, and tries to match player fleet inbetween
        // Major expedition has floor size and tries to exceed player fleet
        // Mothership expedition has floor size and tries to exceed player fleet
        int expFloor = 20;
        int expCeiling = 100;
        int majorExpFloor = 60;
        int motherExpFloor = 120;

        int playerFleet = Global.getSector().getPlayerFleet().getFleetPoints();

        int expeditionPoints = playerFleet - random.nextInt(5) + random.nextInt(10);

        if (expeditionPoints <= 5) expeditionPoints = 5;

        expeditionPoints *= 1f + random.nextFloat();

        if (type.equals(Roider_FleetTypes.EXPEDITION)) {
            expeditionPoints = Math.max(expeditionPoints, expFloor);
            expeditionPoints = Math.min(expeditionPoints, expCeiling);

            points.combat = expeditionPoints * 0.6f;
            points.freighter = expeditionPoints * 0.1f;
            points.tanker = expeditionPoints * 0.1f;
            points.transport = expeditionPoints * 0.1f;
            points.liner = 0;
            points.utility = expeditionPoints * 0.1f;
        }

        if (type.equals(Roider_FleetTypes.MAJOR_EXPEDITION)) {
            expeditionPoints = Math.max(expeditionPoints, majorExpFloor);

            expeditionPoints *= 1.2f + random.nextFloat() * 0.3;

            points.combat = expeditionPoints * 0.5f;
            points.freighter = expeditionPoints * 0.1f;
            points.tanker = expeditionPoints * 0.2f;
            points.transport = expeditionPoints * 0.1f;
            points.liner = 0;
            points.utility = expeditionPoints * 0.1f;
        }

        if (type.equals(Roider_FleetTypes.MOTHER_EXPEDITION)) {
            expeditionPoints = Math.max(expeditionPoints, motherExpFloor);

            expeditionPoints *= 1.4f + random.nextFloat() * 0.4;

            points.combat = expeditionPoints * 0.5f;
            points.freighter = expeditionPoints * 0.1f;
            points.tanker = expeditionPoints * 0.1f;
            points.transport = expeditionPoints * 0.1f;
            points.liner = expeditionPoints * 0.1f;
            points.utility = expeditionPoints * 0.1f;
        }

        points.combat += Math.max(0, expeditionPoints - points.sum());

        return points;
    }

}
