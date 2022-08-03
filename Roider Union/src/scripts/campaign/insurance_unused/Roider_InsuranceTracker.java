package scripts.campaign.insurance_unused;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidImpact;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import hullmods.Roider_MIDAS;
import static hullmods.Roider_MIDAS.RECENT_IMPACT;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;

/**
 * Author: SafariJohn
 */
public class Roider_InsuranceTracker implements EveryFrameScript, EconomyTickListener, TooltipMakerAPI.TooltipCreator {
    public static final String INSURANCE_KEY = "$roider_insurance";
    public static final String INSURANCE_END_KEY = "$roider_insuranceEndTime";

    public static final float LUMP_PAY_CUTOFF = 180; // 6 months

    public static final float MONTHLY_FEE = 1000f;
    public static final float RATE_A_DAMAGES = 100f;
    public static final float RATE_CLAIMS = 100f;
    public static final float RATE_MIDAS_CREDIT = 5000f;
    public static final float RATE_FEES = 1000f;

	public static final String TIMEOUT = "$asteroidImpactTimeout";
	public static final String INSURANCE_LOCK = "$roider_insuranceLock";
	public static final String PAYOUT_ANTICIPATED = "$roider_insurancePayoutAnticipated";

    // Monthly report nodes
    public static final String NODE_INSURANCE = "roider_node_insurance";
    public static final String NODE_PAYOUT = "roider_node_payout";
    public static final String NODE_MIDAS = "roider_node_midas";
    public static final String NODE_FEES = "roider_node_fees";
    // Ship class fees are described in tooltip
    public static final String NODE_ASTEROID_DAMAGES = "roider_node_asteroids";


    // Types of insurance events
    public static final String TYPE_IMPACT_DAMAGE = "impact";
    public static final String TYPE_CLAIM_PAYOUT = "claims";
    public static final String TYPE_MIDAS_CREDIT = "midas";
    public static final String TYPE_SHIP_FEES = "fees";
    public static final String TYPE_FRIGATE_FEE = "fgFee";
    public static final String TYPE_DESTROYER_FEE = "ddFee";
    public static final String TYPE_CRUISER_FEE = "caFee";
    public static final String TYPE_CAPITAL_FEE = "capFee";

    // Average supply and fuel consumption by ship size
    public static final float FG_S = 5f;
    public static final float DD_S = 10f;
    public static final float CA_S = 20f;
    public static final float CAP_S = 40f;

    public static final float FG_F = 1f;
    public static final float DD_F = 2f;
    public static final float CA_F = 3f;
    public static final float CAP_F = 10f;

    public static class Roider_InsuranceEvent {
        public final String type;
        public final float cost;
        public final long timestamp;

        public Roider_InsuranceEvent(String type, float cost) {
            this.type = type;
            this.cost = cost;
            this.timestamp = Global.getSector().getClock().getTimestamp();
        }
    }

    public static Roider_InsuranceTracker getInsurance() {
        Roider_InsuranceTracker tracker = (Roider_InsuranceTracker) Global.getSector().getMemoryWithoutUpdate().get(INSURANCE_KEY);
        if (tracker == null) {
            tracker = new Roider_InsuranceTracker();
//            Global.getSector().getMemoryWithoutUpdate().set(INSURANCE_KEY, tracker);
        }

        return tracker;
    }

    public static void setPlayerInsured(boolean insured) {
        if (!insured) Global.getSector().getPlayerFaction().getMemoryWithoutUpdate().unset(INSURANCE_KEY);
        else Global.getSector().getPlayerFaction().getMemoryWithoutUpdate().set(INSURANCE_KEY, insured);
    }

    public static boolean isPlayerInsured() {
        return Global.getSector().getPlayerFaction().getMemoryWithoutUpdate().is(INSURANCE_KEY, true);
    }

    public void clearLumpData() {
        lumpPayEvents.clear();
    }

//    public static float getInsuranceEndTime() {
//        MemoryAPI mem = Global.getSector().getPlayerFaction().getMemoryWithoutUpdate();
//        if (mem.contains(INSURANCE_END_KEY)) {
//            return mem.getFloat(INSURANCE_END_KEY);
//        }
//
//        return Global.getSector().getClock().getTimestamp();
//    }

    private final IntervalUtil dayTracker;
    private final List<Roider_InsuranceEvent> lumpPayEvents;
    private final List<Roider_InsuranceEvent> insuredEvents;
//    private final Roider_InsuranceIntel intel;
    private final float sectorRadius;
    private final Vector2f sectorCenter;

    public Roider_InsuranceTracker() {
        dayTracker = new IntervalUtil(1f, 1f);
        lumpPayEvents = new ArrayList<>();
        insuredEvents = new ArrayList<>();
//        intel = new Roider_InsuranceIntel();

        sectorCenter = calculateCoreCenter();
        sectorRadius = calculateSectorRadius(sectorCenter);
    }

    private float calculateSectorRadius(Vector2f center) {
        float radius = 0;

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float dist = Misc.getDistance(system.getLocation(), center);

            if (dist > radius) radius = dist;
        }

        return radius;
    }

    private Vector2f calculateCoreCenter() {
        float x = 0;
        float y = 0;
        int count = 0;

        for (LocationAPI loc : Global.getSector().getEconomy().getLocationsWithMarkets()) {
            if (loc instanceof StarSystemAPI) {
                StarSystemAPI system = (StarSystemAPI) loc;

                if (system.isProcgen()) continue;

                count++;
                x += loc.getLocation().x;
                y += loc.getLocation().y;
            }
        }

        if (count > 0) {
            x /= count;
            y /= count;
        }

        return new Vector2f(x, y);
    }

    @Override
    public void advance(float amount) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        MemoryAPI mem = fleet.getMemoryWithoutUpdate();

        float days = Misc.getDays(amount);
        dayTracker.advance(days);

        if (dayTracker.intervalElapsed()) {
            // Remove any uninsured events that have expired
            CampaignClockAPI clock = Global.getSector().getClock();

            List<Roider_InsuranceEvent> toRemove = new ArrayList<>();
            for (Roider_InsuranceEvent event : lumpPayEvents) {
                if (clock.getElapsedDaysSince(event.timestamp) > LUMP_PAY_CUTOFF) {
                    toRemove.add(event);
                }
            }
            lumpPayEvents.removeAll(toRemove);
        }

        // Checking asteroid impacts
        boolean isTimeout = mem.is(TIMEOUT, true);
        boolean isLocked = mem.is(INSURANCE_LOCK, true);
        boolean impactInProgress = fleet.hasScriptOfClass(AsteroidImpact.class);

        // Nothing happening, skip
        if (!isTimeout && !isLocked && !impactInProgress) {
            if (mem.is(RECENT_IMPACT, false)) mem.unset(PAYOUT_ANTICIPATED);
            return;
        }

        boolean insured = isPlayerInsured();
        boolean willPayout = mem.is(RECENT_IMPACT, true) && mem.is(PAYOUT_ANTICIPATED, true);

        // Impact detected
        if (isTimeout && impactInProgress && !isLocked) {
            mem.set(INSURANCE_LOCK, true);

            float baseCredits = fleet.getFleetPoints() / fleet.getFleetSizeCount();

            float burnMult = 1f + (fleet.getCurrBurnLevel() - Misc.getGoSlowBurnLevel(fleet)) / 10f;
            float damages = baseCredits * burnMult * RATE_A_DAMAGES;

            // Increase damages as player gets closer to center of map
            float dist = Misc.getDistance(fleet.getLocationInHyperspace(), sectorCenter);
            float distMult = 1.5f - dist / sectorRadius;

            damages *= distMult;

            // Register impact
            if (insured) {
                insuredEvents.add(new Roider_InsuranceEvent(TYPE_IMPACT_DAMAGE, damages));
            } else {
                lumpPayEvents.add(new Roider_InsuranceEvent(TYPE_IMPACT_DAMAGE, damages));
            }

            // Register payout
            if (willPayout) {
//                mem.unset(PAYOUT_ANTICIPATED);

                float claim = baseCredits * RATE_CLAIMS;

                // Increase payouts as player gets farther from the core
                distMult = Math.max(0, dist - Misc.getDistance(
                            new Vector2f(0, 0), sectorCenter));
                distMult = 1f + distMult / sectorRadius;

                claim *= distMult;

                if (insured) {
                    Roider_InsuranceEvent event = new Roider_InsuranceEvent(TYPE_CLAIM_PAYOUT, claim);
                    insuredEvents.add(event);
                    informPayout(claim, damages);
                } else {
                    lumpPayEvents.add(new Roider_InsuranceEvent(TYPE_CLAIM_PAYOUT, claim));
                }
            } else {
                mem.set(PAYOUT_ANTICIPATED, true);
            }
        }

        // If out of timeout, clear lock
        if (!isTimeout && isLocked) {
            mem.unset(INSURANCE_LOCK);
        }
    }

    private void informPayout(final float claim, final float damages) {
//        Global.getSector().getCampaignUI().addMessage(new BaseIntelPlugin() {
//
//            @Override
//            protected String getName() {
//                return intel.getName() + " - claim filed";
//            }
//
//            @Override
//            public String getIcon() {
//                return super.getIcon();
//            }
//
//            @Override
//            protected void addBulletPoints(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
//                // Display pluses and minuses
//
//
//                info.addPara("Roider insurance will pay %s", initPad,
//                            Misc.getPositiveHighlightColor(),
//                            "" + Math.round(claim));
//            }
//
//        }, CommMessageAPI.MessageClickAction.INTEL_TAB, intel);
    }

    public void reportEconomyTick(int iterIndex) {
        // Take snapshot of player's fleet
        // Checking MIDAS % and compiling ship fees
        snapshotFleet();
    }

    private void snapshotFleet() {
        float fgFees = 0;
        float fgTally = 0;
        float ddFees = 0;
        float ddTally = 0;
        float caFees = 0;
        float caTally = 0;
        float capFees = 0;
        float capTally = 0;
        for (FleetMemberAPI m : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
            if (m.isFighterWing()) continue;

            // Credit for MIDAS
            if (Roider_MIDAS.hasMIDASStatic(m.getVariant())) {
                float credit;
                switch (m.getHullSpec().getHullSize()) {
                    case FIGHTER:
                    case FRIGATE:
                    default:
                        credit = 1f;
                        break;
                    case DESTROYER:
                        credit = 2f;
                        break;
                    case CRUISER:
                        credit = 4f;
                        break;
                    case CAPITAL_SHIP:
                        credit = 8f;
                        break;
                }

                credit *= RATE_MIDAS_CREDIT;
                credit *= 1 + new Random().nextFloat() * 0.2f;

                credit /= Global.getSettings().getFloat("economyIterPerMonth");

                if (isPlayerInsured()) insuredEvents.add(new Roider_InsuranceEvent(TYPE_MIDAS_CREDIT, credit));
                else lumpPayEvents.add(new Roider_InsuranceEvent(TYPE_MIDAS_CREDIT, credit));
            }

            // Fee for ship
            // Su/mo / fuel/ly (min 1/2/3/10) * size (1/1/1/3) * random increase
            float fee = calculateShipFee(m);

            fee /= Global.getSettings().getFloat("economyIterPerMonth");

            switch (m.getHullSpec().getHullSize()) {
                case FRIGATE:
                    fgFees += fee;
                    fgTally++;
                    break;
                case DESTROYER:
                    ddFees += fee;
                    ddTally++;
                    break;
                case CRUISER:
                    caFees += fee;
                    caTally++;
                    break;
                case CAPITAL_SHIP:
                    capFees += fee;
                    capTally++;
                    break;
            }
        }

        // Store average fee for each ship size
        if (fgTally > 0) {
            float fee = fgFees / fgTally;
            if (isPlayerInsured()) insuredEvents.add(new Roider_InsuranceEvent(TYPE_FRIGATE_FEE, fee));
            else lumpPayEvents.add(new Roider_InsuranceEvent(TYPE_FRIGATE_FEE, fee));
        }
        if (ddTally > 0) {
            float fee = ddFees / ddTally;
            if (isPlayerInsured()) insuredEvents.add(new Roider_InsuranceEvent(TYPE_DESTROYER_FEE, fee));
            else lumpPayEvents.add(new Roider_InsuranceEvent(TYPE_DESTROYER_FEE, fee));
        }
        if (caTally > 0) {
            float fee = caFees / caTally;
            if (isPlayerInsured()) insuredEvents.add(new Roider_InsuranceEvent(TYPE_CRUISER_FEE, fee));
            else lumpPayEvents.add(new Roider_InsuranceEvent(TYPE_CRUISER_FEE, fee));
        }
        if (capTally > 0) {
            float fee = capFees / capTally;
            if (isPlayerInsured()) insuredEvents.add(new Roider_InsuranceEvent(TYPE_CAPITAL_FEE, fee));
            else lumpPayEvents.add(new Roider_InsuranceEvent(TYPE_CAPITAL_FEE, fee));
        }

        // Store random increase on top of sum
        float sum = fgFees + ddFees + caFees + capFees;
        sum *= (1 + new Random().nextFloat() * 0.2f);
        if (isPlayerInsured()) insuredEvents.add(new Roider_InsuranceEvent(TYPE_SHIP_FEES, sum));
        else lumpPayEvents.add(new Roider_InsuranceEvent(TYPE_SHIP_FEES, sum));
    }

    private float calculateShipFee(FleetMemberAPI m) {
        return calculateShipFee(m.getHullSpec().getFuelPerLY(),
                    m.getDeploymentCostSupplies(),
                    m.getHullSpec().getHullSize());
    }

    private float calculateShipFee(float fuelPerLy, float deployCost, HullSize size) {
        float fuelDiv = fuelPerLy;
        switch (size) {
            case FRIGATE: fuelDiv = Math.max(FG_F, fuelDiv); break;
            case DESTROYER: fuelDiv = Math.max(DD_F, fuelDiv); break;
            case CRUISER: fuelDiv = Math.max(CA_F, fuelDiv); break;
            case CAPITAL_SHIP: fuelDiv = Math.max(CAP_F, fuelDiv); break;
        }

        float fee = deployCost / fuelDiv;

        // Weight fees towards average
        // Arbitrarily multiply capital fee
        switch (size) {
            case FRIGATE:
                fee = (fee + (FG_S / FG_F)) / 2f;
                break;
            case DESTROYER:
                fee = (fee + (DD_S / DD_F)) / 2f;
                break;
            case CRUISER:
                fee = (fee + (CA_S / CA_F)) / 2f;
//                fee *= 2;
                break;
            case CAPITAL_SHIP:
                fee = (fee + (CAP_S / CAP_F)) / 2f;
                fee *= 3;
                break;
        }

        fee *= RATE_FEES;
        fee *= (1 + new Random().nextFloat() * 0.2f);

        return fee;
    }

    @Override
    public void reportEconomyMonthEnd() {
        updateReportNodes();
        insuredEvents.clear();
    }

    private void updateReportNodes() {
        if (!isPlayerInsured() && insuredEvents.isEmpty()) return;

        // Charge damages
        // Tally events, also
        float payout = 1f;
        float midas = 1f;
        float fees = 1f;
        float damages = 1f;
        for (Roider_InsuranceEvent event : insuredEvents) {
            switch (event.type) {
                case TYPE_CLAIM_PAYOUT:
                    payout += event.cost;
                    break;
                case TYPE_MIDAS_CREDIT:
                    midas += event.cost;
                    break;
                case TYPE_SHIP_FEES:
                    fees += event.cost;
                    break;
                case TYPE_IMPACT_DAMAGE:
                    damages += event.cost;
                    break;
                default: break;
            }
        }


        MonthlyReport report = SharedData.getData().getCurrentReport();
        FDNode root = report.getNode(MonthlyReport.FLEET, NODE_INSURANCE);
//        root.name = intel.getName();
        root.tooltipCreator = this;
        root.tooltipParam = NODE_INSURANCE;

        FDNode claims = report.getNode(root, NODE_PAYOUT);
        claims.name = "Hazards compensation";
        claims.income = payout;
        claims.tooltipCreator = this;
        claims.tooltipParam = NODE_PAYOUT;

        FDNode credit = report.getNode(root, NODE_MIDAS);
        credit.name = "MIDAS usage";
        credit.income = midas;
        credit.tooltipCreator = this;
        credit.tooltipParam = NODE_MIDAS;

        FDNode regAndFees = report.getNode(root, NODE_FEES);
        regAndFees.name = "Registration and fees";
        regAndFees.upkeep = fees;
        regAndFees.tooltipCreator = this;
        regAndFees.tooltipParam = NODE_FEES;
        // Detail each ship size's fee in the tooltip

        FDNode asteroids = report.getNode(root, NODE_ASTEROID_DAMAGES);
        asteroids.name = "Damages to resource zones";
        asteroids.upkeep = damages;
        asteroids.tooltipCreator = this;
        asteroids.tooltipParam = NODE_ASTEROID_DAMAGES;

    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        float opad = 0f;
        float pad = 10f;

        FDNode node = (FDNode) tooltipParam;

        if (node.tooltipParam.equals(NODE_INSURANCE)) {
            tooltip.addPara("A compensation program to provide support to roiders.", opad);
        } else if (node.tooltipParam.equals(NODE_PAYOUT)) {
            tooltip.addPara("Compensation for asteroid impacts and other hazards.", opad);
        } else if (node.tooltipParam.equals(NODE_MIDAS)) {
            tooltip.addPara("Credit for minimizing environmental damage with MIDAS.", opad);
        } else if (node.tooltipParam.equals(NODE_FEES)) {
            tooltip.addPara("Cost of registering ships and various monthly fees.", opad);

            float fgFee = getHullFee(HullSize.FRIGATE);
            float ddFee = getHullFee(HullSize.DESTROYER);
            float caFee = getHullFee(HullSize.CRUISER);
            float capFee = getHullFee(HullSize.CAPITAL_SHIP);

            tooltip.addPara("Monthly registration fees by ship class:", pad);
            tooltip.setParaFontDefault();
            tooltip.addPara("- %s for frigates", opad, Misc.getHighlightColor(), Misc.getDGSCredits(fgFee));
            tooltip.addPara("- %s for destroyers", opad, Misc.getHighlightColor(), Misc.getDGSCredits(ddFee));
            tooltip.addPara("- %s for cruisers", opad, Misc.getHighlightColor(), Misc.getDGSCredits(caFee));
            tooltip.addPara("- %s for capital ships", opad, Misc.getHighlightColor(), Misc.getDGSCredits(capFee));

        } else if (node.tooltipParam.equals(NODE_ASTEROID_DAMAGES)) {
            tooltip.addPara("Damages to resource zones.", opad);
        }
    }

    public float getEventCostSum(String type) {
        List<Roider_InsuranceEvent> events;
        if (isPlayerInsured()) events = insuredEvents;
        else events = lumpPayEvents;

        float sum = 0;
        for (Roider_InsuranceEvent event : events) {
            if (event.type.equals(type)) {
                sum += event.cost;
            }
        }

        return sum;
    }

    public int getEventCount(String type) {
        List<Roider_InsuranceEvent> events;
        if (isPlayerInsured()) events = insuredEvents;
        else events = lumpPayEvents;

        int count = 0;
        for (Roider_InsuranceEvent event : events) {
            if (event.type.equals(type)) {
                count++;
            }
        }

        return count;

    }

    public float getCompensation() {
        return getEventCostSum(TYPE_CLAIM_PAYOUT);
    }

    public float getMIDASCredit() {
        return getEventCostSum(TYPE_MIDAS_CREDIT);
    }

    public float getMIDASCreditRoundedUp() {
        float credit = getMIDASCredit();

        float months = getFeeMonths();

        if (months == 0) return credit;

        // Get average monthly fee
        float avgFee = credit / months;

        // Round up months
        months = getFeeMonthsRoundedUp();

        return avgFee * months;
    }

    public float getFees() {
        return getEventCostSum(TYPE_SHIP_FEES);
    }

    public float getFeesRoundUp() {
        float fees = getFees();

        float months = getFeeMonths();

        if (months == 0) return fees;

        // Get average monthly fee
        float avgFee = fees / months;

        // Round up months
        months = getFeeMonthsRoundedUp();

        return avgFee * months;
    }

    public float getFeeMonths() {
        int count = getEventCount(TYPE_SHIP_FEES);
        return count / Global.getSettings().getFloat("economyIterPerMonth");
    }

    public int getFeeMonthsRoundedUp() {
        float months = getFeeMonths();
        if (months % 1f < 0.5f) months++;
        return Math.round(months);
    }

    /**
     * @param size
     * @return value of fee for display (never 0)
     */
    public float getHullFee(HullSize size) {
        return getHullFee(size, true);
    }

    public float getHullFee(HullSize size, boolean forDisplay) {
        String type = TYPE_FRIGATE_FEE;
        switch (size) {
            case CAPITAL_SHIP: type = TYPE_CAPITAL_FEE; break;
            case CRUISER: type = TYPE_CRUISER_FEE; break;
            case DESTROYER: type = TYPE_DESTROYER_FEE; break;
            case DEFAULT:
        }

        int count = getEventCount(type);

        // Return "average" value if none
        if (forDisplay && count == 0) {
            switch (size) {
                case CAPITAL_SHIP: return calculateShipFee(CAP_F, CAP_S, size);
                case CRUISER: return calculateShipFee(CA_F, CA_S, size);
                case DESTROYER: return calculateShipFee(DD_F, DD_S, size);
                case DEFAULT: return calculateShipFee(FG_F, FG_S, HullSize.FRIGATE);
            }
        }

        float months = count / Global.getSettings().getFloat("economyIterPerMonth");

        if (months == 0) return getEventCostSum(type);

        return getEventCostSum(type) / months;
    }

    public float getDamages() {
        return getEventCostSum(TYPE_IMPACT_DAMAGE);
    }


    // TooltipCreator
    public boolean isTooltipExpandable(Object tooltipParam) { return false; }
    public float getTooltipWidth(Object tooltipParam) { return 450; }

    // EveryFrameScript
    public boolean runWhilePaused() { return false; }
    public boolean isDone() { return false; }
}
