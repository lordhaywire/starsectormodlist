package scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;

/**
 * Author: SafariJohn
 */
public class Roider_Misc {
    public enum FleetSize {
        SMALL,
        MEDIUM,
        LARGE
    }

    public static FleetParamsV3 createPatrolParams(RouteData route, FleetSize size) {
        Random random = new Random();

        MarketAPI market = route.getMarket();

        String fleetType = FleetTypes.PATROL_SMALL;

		float combat = 0f;
		float tanker = 0f;
		float freighter = 0f;
		switch (size) {
		case SMALL:
			combat = Math.round(1f + (float) random.nextFloat() * 2f) * 5f;
			freighter = Math.round((float) random.nextFloat()) * 5f;
            fleetType = FleetTypes.PATROL_SMALL;
			break;
		case MEDIUM:
			combat = Math.round(3f + (float) random.nextFloat() * 3f) * 5f;
			tanker = Math.round((float) random.nextFloat()) * 5f;
			freighter = Math.round((float) random.nextFloat()) * 15f;
            fleetType = FleetTypes.PATROL_MEDIUM;
			break;
		case LARGE:
			combat = Math.round(5f + (float) random.nextFloat() * 5f) * 5f;
			tanker = Math.round((float) random.nextFloat()) * 10f;
			freighter = Math.round((float) random.nextFloat()) * 25f;
            fleetType = FleetTypes.PATROL_LARGE;
			break;
		}

		FleetParamsV3 params = new FleetParamsV3(
				market,
				null, // loc in hyper; don't need if have market
				route.getFactionId(),
				null, // quality override
				fleetType,
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
		params.modeOverride = Misc.getShipPickMode(market);

        return params;
    }

    public static CampaignFleetAPI createPatrolFleet(FleetSize size,
                MarketAPI sourceMarket, OptionalFleetData extra) {
        RouteData route = new RouteData(Misc.genUID(), sourceMarket,
                    new Random().nextLong(), extra);

        FleetParamsV3 params = createPatrolParams(route, size);

        return createPatrolFleet(route, params, size);
    }

    public static CampaignFleetAPI createPatrolFleet(RouteData route,
                FleetParamsV3 params, FleetSize size) {

        MarketAPI market = route.getMarket();

		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

		if (fleet == null || fleet.isEmpty()) return null;

//		fleet.setFaction(market.getFactionId(), true);
        fleet.setName(market.getFaction().getFleetTypeName(params.fleetType));

		String postId = Ranks.POST_FLEET_COMMANDER;
		String rankId = Ranks.SPACE_COMMANDER;
		switch (size) {
            case SMALL:
                rankId = Ranks.SPACE_COMMANDER;
                break;
            case MEDIUM:
                rankId = Ranks.SPACE_CAPTAIN;
                break;
            case LARGE:
                rankId = Ranks.SPACE_ADMIRAL;
                break;
		}

		fleet.getCommander().setPostId(postId);
		fleet.getCommander().setRankId(rankId);

//		market.getContainingLocation().addEntity(fleet);
//		fleet.setFacing((float) Math.random() * 360f);
//		fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

		return fleet;
    }

    public static void highlight(LabelAPI label, Color color,
                String ... highlights) {
        label.setHighlight(highlights);
        label.setHighlightColor(color);
    }

    public static void sortFleetByShipSize(CampaignFleetAPI fleet) {
        fleet.getFleetData().sort();
        if (true) return;

        List<FleetMemberAPI> sortedMembers = fleet.getFleetData().getMembersListCopy();

        Collections.sort(sortedMembers, new Comparator<FleetMemberAPI>() {
            public int compare(FleetMemberAPI o1, FleetMemberAPI o2) {
                if (o1.isFighterWing() || o2.isFighterWing()) return 0;

                int o1Size = 0;
                int o2Size = 0;
                switch (o1.getHullSpec().getHullSize()) {
                    case CAPITAL_SHIP: o1Size = 4; break;
                    case CRUISER: o1Size = 3; break;
                    case DESTROYER: o1Size = 2; break;
                    case FRIGATE: o1Size = 1; break;
                }
                switch (o2.getHullSpec().getHullSize()) {
                    case CAPITAL_SHIP: o2Size = 4; break;
                    case CRUISER: o2Size = 3; break;
                    case DESTROYER: o2Size = 2; break;
                    case FRIGATE: o2Size = 1; break;
                }

                if (o1.isCivilian()) o1Size -= 4;
                if (o2.isCivilian()) o2Size -= 4;

                if (o2Size == o1Size) {
                    return o1.getHullSpec().getNameWithDesignationWithDashClass().compareToIgnoreCase(o2.getHullSpec().getNameWithDesignationWithDashClass());
                }

                return o2Size - o1Size;
            }
        });

        for (FleetMemberAPI m : sortedMembers) {
            fleet.getFleetData().removeFleetMember(m);
        }

        for (FleetMemberAPI m : sortedMembers) {
            fleet.getFleetData().addFleetMember(m);
        }

    }

    public static float clamp(float x, float min, float max) {
        return Math.min(max, Math.max(x, min));
    }

    public static float getDistanceSquared(SectorEntityToken from, SectorEntityToken to) {
        return getDistanceSquared(from.getLocation(), to.getLocation());
    }

    public static float getDistanceSquared(Vector2f v1, Vector2f v2) {
        return (v1.x - v2.x) * (v1.x - v2.x) + (v1.y - v2.y) * (v1.y - v2.y);
    }
}
