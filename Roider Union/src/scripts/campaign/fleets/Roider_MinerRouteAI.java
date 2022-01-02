package scripts.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

/**
 * Author: SafariJohn
 */
public class Roider_MinerRouteAI extends RouteFleetAssignmentAI {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_MinerRouteAI.class, "supplyLevels", "s");
    }

    private final Map<String, Integer> supplyLevels;

    public Roider_MinerRouteAI(CampaignFleetAPI fleet,RouteData route,
                Map<String, Integer> supplyLevels) {
        super(fleet, route);
        this.supplyLevels = supplyLevels;

        giveInitialAssignments();
    }

    @Override
    protected void giveInitialAssignments() {
        if (supplyLevels == null) return;
        if (supplyLevels.isEmpty()) supplyLevels.put(Commodities.ORE, 1);
        super.giveInitialAssignments();
    }

    @Override
    protected String getStartingActionText(RouteSegment segment) {
        return "preparing for an expedition";
    }

    @Override
    protected String getInSystemActionText(RouteSegment segment) {
        if (segment.getId() == Roider_MinerRouteManager.PREPARE) {
            return "preparing for an expedition";
        }

        if (segment.getId() == Roider_MinerRouteManager.MINE) {
            return "mining " + getCargoList();
        }
        if (segment.getId() == Roider_MinerRouteManager.UNLOAD) {
            return "unloading " + getCargoList();
        }

        return "travelling";
    }

    @Override
    protected String getTravelActionText(RouteSegment segment) {
        if (segment.getId() == Roider_MinerRouteManager.RETURN) {
            return "returning to " + route.getMarket().getName() + " with " + getCargoList();
        } else {
            return "travelling";
        }
    }

    @Override
    protected String getEndingActionText(RouteSegment segment) {
        if (segment.getId() == Roider_MinerRouteManager.UNLOAD) {
            return "unloading " + getCargoList();
        }
        return super.getEndingActionText(segment); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void addEndingAssignment(RouteSegment current, boolean justSpawned) {
		if (justSpawned) {
			float progress = current.getProgress();
			RouteLocationCalculator.setLocation(fleet, progress,
									current.getDestination(), current.getDestination());
		}

		SectorEntityToken to = current.to;
		if (to == null) to = current.from;
		if (to == null) to = route.getMarket().getPrimaryEntity();

		if (to == null || !to.isAlive()) {
			Vector2f loc = Misc.getPointAtRadius(fleet.getLocationInHyperspace(), 5000);
			SectorEntityToken token = Global.getSector().getHyperspace().createToken(loc);
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, token, 1000f);
			return;
		}


		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, to, 1000f,
							"returning to " + to.getName());
		if (current.daysMax > current.elapsed) {
			fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, to,
								current.daysMax - current.elapsed, getEndingActionText(current));
		}
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, to,
				1000f, super.getEndingActionText(current),
				goNextScript(current));
    }


    private String getCargoList() {
        List<String> strings = new ArrayList<>();

        for (String cid : supplyLevels.keySet()) {
            if (supplyLevels.get(cid) > 0) strings.add(Global.getSettings().getCommoditySpec(cid).getLowerCaseName());
        }

        if (strings.isEmpty()) strings.add(Global.getSettings().getCommoditySpec(Commodities.ORE).getLowerCaseName());

        if (strings.size() > 1) return Misc.getAndJoined(strings);
        else if (strings.size() == 1) return strings.get(0);
        else return ""; // Should not happen
    }
}
