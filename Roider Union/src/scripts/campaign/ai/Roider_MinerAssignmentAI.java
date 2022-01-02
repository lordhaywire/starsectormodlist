package scripts.campaign.ai;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_FleetTypes;
import scripts.campaign.fleets.Roider_MinerManager;
import java.util.*;
import org.lwjgl.util.vector.Vector2f;

/**
 * Author: SafariJohn
 */
public class Roider_MinerAssignmentAI implements EveryFrameScript {

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_MinerAssignmentAI.class, "fleet", "f");
        x.aliasAttribute(Roider_MinerAssignmentAI.class, "source", "s");
        x.aliasAttribute(Roider_MinerAssignmentAI.class, "dest", "d");
        x.aliasAttribute(Roider_MinerAssignmentAI.class, "supplyLevels", "su");
        x.aliasAttribute(Roider_MinerAssignmentAI.class, "orderedReturn", "rn");
        x.aliasAttribute(Roider_MinerAssignmentAI.class, "orderedRetreat", "rt");
        x.aliasAttribute(Roider_MinerAssignmentAI.class, "preparing", "p");
        x.aliasAttribute(Roider_MinerAssignmentAI.class, "mining", "m");
    }

    private CampaignFleetAPI fleet;
    private MarketAPI source;
    private SectorEntityToken dest;
    private Map<String, Integer> supplyLevels;
    private boolean orderedReturn = false, orderedRetreat = false;
    private boolean preparing = true, mining = true;

//    Logger logger = Global.getLogger(Roider_MinerAssignmentAI.class);

    @Override
    public boolean isDone() {
        return !fleet.isAlive();
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

	public Roider_MinerAssignmentAI(CampaignFleetAPI fleet, MarketAPI source,
                SectorEntityToken dest, Map<String, Integer> supplyLevels) {
        this.fleet = fleet;
        this.source = source;
        this.dest = dest;
        this.supplyLevels = supplyLevels;

        giveAssignments();
	}

    private void giveAssignments() {
        float daysToOrbit = getDaysToOrbit() * 0.25f;
        if (daysToOrbit < 0.2f) {
                daysToOrbit = 0.2f;
        }

        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source.getPrimaryEntity(),
                    daysToOrbit, "preparing for an expedition");
        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, dest, 1000, "travelling");

        float daysToMine = getDaysToMine();
		String list = getCargoList();
        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, dest, daysToMine,
                    "mining " + list);

        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, source.getPrimaryEntity(), 1000f,
                    "returning to " + source.getName() + " with " + list);
        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source.getPrimaryEntity(), daysToOrbit * 2,
                    "unloading " + list);
        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source.getPrimaryEntity(), daysToOrbit,
                    "returning to " + source.getName());
    }

    private float getDaysToOrbit() {
        float daysToOrbit = 0f;
        switch (fleet.getMemoryWithoutUpdate().getString(MemFlags.MEMORY_KEY_FLEET_TYPE)) {
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

    private float getDaysToMine() {
        float daysToMine = 10f;
        switch (fleet.getMemoryWithoutUpdate().getString(MemFlags.MEMORY_KEY_FLEET_TYPE)) {
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

    private String getCargoList() {
        List<String> strings = new ArrayList<>();

        for (String cid : supplyLevels.keySet()) {
            if (supplyLevels.get(cid) > 0) strings.add(Global.getSettings().getCommoditySpec(cid).getLowerCaseName());
        }

        if (strings.size() > 1) return Misc.getAndJoined(strings);
        else if (strings.size() == 1) return strings.get(0);
        else return ""; // Should not happen
    }

    @Override
    public void advance(float amount) {

        if (fleet.getAI().getCurrentAssignment() != null) {
            float fp = fleet.getFleetPoints();
            float startingFP = fleet.getMemoryWithoutUpdate().getFloat(Roider_MinerManager.MINER_STARTING_FP);
            if (fp < startingFP * 0.5f && !orderedReturn && !orderedRetreat) {
                orderedReturn = true;
                preparing = false;
                mining = false;
                fleet.clearAssignments();

                if (source.getPrimaryEntity() != null) {
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, source.getPrimaryEntity(), 1000,
                                                            "returning to " + source.getName());
                    fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source.getPrimaryEntity(), 1f,
                                                            "standing down");
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source.getPrimaryEntity(), 1000);
                } else {
                    getLost();
                }
            } else {
                if (preparing && fleet.getAI().getCurrentAssignment().getTarget() == dest) {
                    preparing = false;
                    loadHeavyMachinery();
                }

                if (!preparing && mining && fleet.getAI().getCurrentAssignment().getTarget() == source.getPrimaryEntity()) {
                    mining = false;
                    loadResources();
                }

                if (source.getPrimaryEntity() == null && !orderedRetreat) {
                    orderedRetreat = true;
                    fleet.clearAssignments();
                    getLost();
                }
            }
        } else {
            if (source.getPrimaryEntity() != null && !orderedReturn && !orderedRetreat) {
                // Don't think this case can actually happen.
                orderedReturn = true;
                fleet.clearAssignments();

                if (source.getPrimaryEntity() != null) {
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, source.getPrimaryEntity(), 1000,
                                                            "returning to " + source.getName());
                    fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source.getPrimaryEntity(), 1f,
                                                            "standing down");
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source.getPrimaryEntity(), 1000);
                }
            } else {
                orderedRetreat = true;
                fleet.clearAssignments();
                getLost();
            }
        }
    }

    private void loadHeavyMachinery() {
		CargoAPI cargo = fleet.getCargo();
		float maxCargo = cargo.getMaxCapacity();
        // 10% of max cargo for heavy machinery
        cargo.addCommodity(Commodities.HEAVY_MACHINERY, maxCargo * 0.1f);
//        logger.info("asddssd heavy machinery loaded");
    }

    private void loadResources() {

		float total = 0f;

		for (String cid : supplyLevels.keySet()) {
			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(cid);
			float qty = (int) (BaseIndustry.getSizeMult(supplyLevels.get(cid)) * spec.getEconUnit());

			total += qty;
            supplyLevels.put(cid, (int) qty);
		}

		if (total <= 0) return;

		CargoAPI cargo = fleet.getCargo();
		float maxCargo = cargo.getMaxCapacity();
        maxCargo *= 0.9f; // 10% taken up by heavy machinery

		for (String cid : supplyLevels.keySet()) {
			float qty = supplyLevels.get(cid);

			cargo.addCommodity(cid, ((int) qty * Math.min(1f, maxCargo / total)));
//            logger.info("asddssd " + cid + " loaded");
		}
    }

    private void getLost() {
        // Add instant despawning code if player far away


        Vector2f loc = Misc.getPointAtRadius(fleet.getLocationInHyperspace(), 5000);
        SectorEntityToken token = Global.getSector().getHyperspace().createToken(loc);
        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, token, 1000f, "travelling");
    }
}
