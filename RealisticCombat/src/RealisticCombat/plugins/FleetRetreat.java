package RealisticCombat.plugins;

import RealisticCombat.settings.Toggles;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;

import java.awt.*;
import java.util.List;

import static RealisticCombat.settings.FleetRetreat.getLossThreshold;
import static RealisticCombat.settings.FleetRetreat.getOutnumberThreshold;

public final class FleetRetreat extends BaseEveryFrameCombatPlugin {

    private boolean retreatAnnounced = false, initialized = false;

    private float retreatThresholdLossEnemy, retreatThresholdOutnumberEnemy;

    private CombatFleetManagerAPI fleetManagerFriendly, fleetManagerEnemy;

    private boolean isProhibited() {
        return Global.getCurrentState() != GameState.COMBAT
                || Global.getCombatEngine() == null
                || Global.getCombatEngine().isPaused()
                || !Toggles.isEnemyFleetToRetreat()
                || Global.getCombatEngine().isEnemyInFullRetreat()
                || !initialized;
    }

    private static boolean isOutnumbered(final float remaining,
                                         final float remainingOpposed,
                                         final float retreatThresholdLossEnemy) {
        final float outnumberFraction = remaining / remainingOpposed;
        return outnumberFraction < retreatThresholdLossEnemy;
    }

    private static boolean isMissingTooMany(final float remaining,
                                            final float lost,
                                            final float retreatThresholdLossEnemy) {
        final float lossFraction = getLossFraction(lost, remaining);
        return lossFraction > retreatThresholdLossEnemy;
    }

    private static float getDeploymentPointsCombat(final List<FleetMemberAPI> fleetMembers) {
        int deploymentPoints = 0;
        for (FleetMemberAPI fleetMember : fleetMembers)
            if (!fleetMember.isCivilian())
                deploymentPoints += fleetMember.getDeploymentPointsCost();
        return deploymentPoints;
    }

    private static float getDeploymentPointsRemaining(final CombatFleetManagerAPI fleetManager) {
        return getDeploymentPointsCombat(fleetManager.getDeployedCopy())
                + getDeploymentPointsCombat(fleetManager.getReservesCopy());
    }

    private static float getDeploymentPointsLost(final CombatFleetManagerAPI fleetManager) {
        return getDeploymentPointsCombat(fleetManager.getRetreatedCopy())
                + getDeploymentPointsCombat(fleetManager.getDisabledCopy())
                + getDeploymentPointsCombat(fleetManager.getDestroyedCopy());
    }

    private static float getLossFraction(final float lost, final float remaining) {
        return lost / (lost + remaining);
    }

    private static void retreat(final CombatFleetManagerAPI fleetManager) {
        final CombatTaskManagerAPI taskManager = fleetManager.getTaskManager(false);
        for (DeployedFleetMemberAPI deployedMember : fleetManager.getDeployedCopyDFM()) {
            final CombatFleetManagerAPI.AssignmentInfo
                    assignment = taskManager.getAssignmentFor(deployedMember.getShip());
            if (!(assignment == null || assignment.getType() == CombatAssignmentType.RETREAT))
                taskManager.orderRetreat(deployedMember, false, false);
        }
    }

    @Override
    public void init(final CombatEngineAPI engine) {
        if (Global.getCurrentState() != GameState.COMBAT || engine == null) return;
        fleetManagerFriendly = Global.getCombatEngine().getFleetManager(0);
        fleetManagerEnemy = Global.getCombatEngine().getFleetManager(1);
        final PersonAPI enemyCommander = fleetManagerEnemy.getFleetCommander();
        
        if (enemyCommander != null) {
            final String enemyCommanderPersonalityId = enemyCommander.getPersonalityAPI().getId();
            if (enemyCommanderPersonalityId != null) {
                retreatThresholdOutnumberEnemy = getOutnumberThreshold(enemyCommanderPersonalityId);
                retreatThresholdLossEnemy = getLossThreshold(enemyCommanderPersonalityId);
            }
        } else {
            retreatThresholdOutnumberEnemy = 0;
            retreatThresholdLossEnemy = 1;
        }
    }

    @Override
    public void advance(final float amount, final List<InputEventAPI> list) {
        if (isProhibited()) return;

        final float remainingEnemy = getDeploymentPointsRemaining(fleetManagerEnemy),
                    lostEnemy = getDeploymentPointsLost(fleetManagerEnemy),
                    remainingFriendly = getDeploymentPointsRemaining(fleetManagerFriendly);

        if (isMissingTooMany(remainingEnemy, lostEnemy, retreatThresholdLossEnemy)
            || isOutnumbered(remainingEnemy, remainingFriendly, retreatThresholdOutnumberEnemy))
        {
            if (!retreatAnnounced) {
                Global.getCombatEngine().getCombatUI().addMessage(1,
                        "The enemy is in full retreat!", Color.WHITE);
                retreatAnnounced = true;
            }
            if (!Global.getCombatEngine().isEnemyInFullRetreat())
                retreat(fleetManagerEnemy);
        }
    }
}
