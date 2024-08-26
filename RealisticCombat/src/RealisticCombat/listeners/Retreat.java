package RealisticCombat.listeners;

import RealisticCombat.plugins.Announcer;
import RealisticCombat.scripts.Categorization;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.mission.FleetSide;

import java.awt.*;
import java.util.Random;

public final class Retreat implements AdvanceableListener {

    private static final Random random = new Random(0);

    private final float CRThreshold, overrideDuration;

    private final String personalityId;

    private final ShipAPI ship;

    private final CombatFleetManagerAPI fleetManager;

    private boolean wouldRetreat = false, overridden = false;

    private float orderTime = Float.POSITIVE_INFINITY;


    public Retreat(final ShipAPI ship) {
        this.ship = ship;
        fleetManager = getFleetManager(ship);
        personalityId = (ship.getCaptain() == null ? fleetManager.getDefaultCommander()
                                                   : ship.getCaptain()).getPersonalityAPI().getId();
        CRThreshold = ship.isDrone()
                        ? 0 : RealisticCombat.settings.Retreat.getCRThreshold(personalityId);
        overrideDuration = RealisticCombat.settings.Retreat.getOverrideDuration(personalityId);
    }


    private boolean isProhibited() {
        return Categorization.isPlayerFlown(ship) || ship.getCurrentCR() >= CRThreshold;
    }

    private static boolean isRetreat(final AssignmentInfo assignment) {
        return assignment != null && assignment.getType() == CombatAssignmentType.RETREAT;
    }

    private static String getCRDescription() {
        return RealisticCombat.settings.Retreat.getCombatDescriptions()[random.nextInt(
                RealisticCombat.settings.Retreat.getCombatDescriptions().length)]
                + " "
                + RealisticCombat.settings.Retreat.getCapacityDescriptions()[random.nextInt(
                RealisticCombat.settings.Retreat.getCapacityDescriptions().length)];
    }

    private static String getShipName(final FleetMemberAPI member) {
        return member.isFighterWing()
                ? member.getHullSpec().getHullName() + " wing"
                : member.getShipName()
                + " (" + member.getHullSpec().getHullNameWithDashClass() + ")";
    }

    private static String getStatement(final String personalityId) {
        return RealisticCombat.settings.Retreat.getStatements().get(personalityId)[
                random.nextInt(RealisticCombat.settings.Retreat.getStatements().get(
                        personalityId).length)];
    }

    private static String getAnnouncement(final ShipAPI ship, final String personalityId) {
        return (int) (ship.getCurrentCR() * 100) + "% " + getCRDescription()
                + ".  " + getStatement(personalityId);
    }

    private static CombatFleetManagerAPI getFleetManager(final ShipAPI ship) {
        final FleetSide side = ship.getOwner() == 0 ? FleetSide.PLAYER : FleetSide.ENEMY;
        return Global.getCombatEngine().getFleetManager(side);
    }

    private static Object[] getMessage(final DeployedFleetMemberAPI deployedMember,
                                       final String text) {
        final String shipName = getShipName(deployedMember.getMember());
        return new Object[]{ deployedMember, Global.getSettings().getColor("textFriendColor"),
                shipName, Global.getSettings().getColor("standardTextColor"), ": ", Color.CYAN,
                text };
    }

    @Override
    public void advance(final float amount) {
        if (isProhibited()) return;

        final CombatTaskManagerAPI taskManager = fleetManager.getTaskManager(false);
        final DeployedFleetMemberAPI deployedMember = fleetManager.getDeployedFleetMember(ship);
        if (deployedMember == null) return;
        if (ship.getCurrentCR() > CRThreshold) {
            wouldRetreat = false;
            return;
        }

        final AssignmentInfo assignment = taskManager.getAssignmentFor(ship);
        final float time = Global.getCombatEngine().getTotalElapsedTime(true);
        if (isRetreat(assignment)) return;
        else {
            if (overridden) {
                if (time - orderTime < overrideDuration) return;
                else overridden = false;
            } else if (wouldRetreat) {
                overridden = true;
                return;
            }
        }

        wouldRetreat = true;

        orderTime = time;
        taskManager.orderRetreat(deployedMember, false, false);

        if (ship.getOwner() == 0) {
            Global.getCombatEngine().getCombatUI().addMessage(1, getMessage(deployedMember,
                    getAnnouncement(ship, personalityId)));
            //TODO: Add BlackRaven's voice
            //Announcer.requestAnnouncement(
            //        RealisticCombat.settings.Announcer.EVENT_TYPE.RETREATED_FRIENDLY);
        }
    }
}
