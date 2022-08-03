package scripts.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;
import static scripts.combat.Roider_PhasenetStats.ASSIST_KEY;
import static scripts.combat.Roider_PhasenetStats.MAX_FORCE;
import static scripts.combat.Roider_PhasenetStats.MAX_PULL_SPEED;

/**
 * Author: SafariJohn
 */
public class Roider_PhasenetAI implements ShipSystemAIScript {
    public static final float DO_NOTHING_WEIGHT = 0f;

    public static final float COLLIDER_WEIGHT_MULT = 1f;
    public static final float WRECKING_WEIGHT_MULT = 1f;
    public static final float SLOW_WEIGHT_MULT = 1f;
    public static final float SHIP_SHIELD_WEIGHT_MULT = 1f;
    public static final float SHIELD_WEIGHT_MULT = 1f;
    public static final float ASSIST_WEIGHT_MULT = 2f;

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipwideAIFlags flags;
	private ShipSystemAPI system;
    private Roider_PhasenetStats stats;

	private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system,
                ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
        this.stats = (Roider_PhasenetStats) system.getSpecAPI().getStatsScript();
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir,
                Vector2f collisionDangerDir, ShipAPI target) {
		tracker.advance(amount);

		if (tracker.intervalElapsed()) {
            // Check if should activate system
			if (system.getState() == ShipSystemAPI.SystemState.IDLE) {
                // Fail fast if nothing in range
                if (!isUsable()) return;

                pickResultIdle(isRetreating());

                // Retreating logic
                    // Choices:
                    // use weak enemy as shield
                    // assist allied Phasenet
                    // use hulk or asteroid as shield
                    // do nothing


                // Attacking logic
                // Choices:
                // pull small enemy into another enemy
                // grab enemy to beat up
                // assist allied Phasenet
                // slow down retreating enemy
                // do nothing
            }

            // Check if should deactivate system
            else if (system.getState() == ShipSystemAPI.SystemState.ACTIVE) {
                pickResultActive(isRetreating());
            }
        }
    }

    private boolean isRetreating() {
        return isRetreating(ship);
    }

    private boolean isRetreating(ShipAPI s) {
        return s.getAIFlags().hasFlag(AIFlags.RUN_QUICKLY);
    }

    private boolean isUsable() {
        if (ship.getFluxTracker().isOverloadedOrVenting()) return false;

        CombatEntityAPI pick = ship.getShipTarget();
        if (!Roider_PhasenetStats.isTargetInRange(ship, pick)) pick = null;

        if (pick == null) {
            List<CombatEntityAPI> entities = new ArrayList<>();
            entities.addAll(engine.getAsteroids());
            entities.addAll(engine.getShips());

            for (CombatEntityAPI entity : entities) {
                if (entity == ship) continue;

                if (Roider_PhasenetStats.isTargetInRange(entity, ship)) {
                    pick = entity;

                    if (pick instanceof ShipAPI) {
                        ShipAPI tShip = (ShipAPI) pick;

                        if (tShip.isStation()) {
                            pick = null;
                        } else if (tShip.isStationModule()) {
                            ShipAPI parent = tShip.getParentStation();

                            if (parent != ship && !parent.isStation() && tShip.getStationSlot() != null) {
                                pick = parent;
                            } else {
                                pick = null;
                            }
                        }
                    }

                    if (pick != null) break;
                }
            }
        }

        if (pick == ship) pick = null;

        return pick != null;
    }

    private void pickResultIdle(boolean retreating) {
        float count = 6f;
        if (retreating) count = 4f;

        // Attacking choices
        float bestCollideOdds = 0;
        CombatEntityAPI bestCollider = null;
        float bestWreckingOdds = 0;
        ShipAPI bestWreckingTarget = null;
        float bestSlowOdds = 0;
        ShipAPI bestSlowTarget = null;
        float bestRescueOdds = 0;
        ShipAPI bestRescueTarget = null;
        // Retreating choices
        float bestShipShieldOdds = 0;
        ShipAPI bestShipShieldTarget = null;
        float bestShieldOdds = 0;
        ShipAPI bestShieldTarget = null;
        // Common choices
        float bestAssistOdds = 0;
        ShipAPI bestAssistTarget = null;
        float doNothingOdds = DO_NOTHING_WEIGHT;


        List<CombatEntityAPI> entities = new ArrayList<>();
        entities.addAll(engine.getAsteroids());
        entities.addAll(engine.getShips());
        for (CombatEntityAPI entity : entities) {
            if (entity == ship) continue;
            if (!Roider_PhasenetStats.isTargetInRange(entity, ship)) continue;

            if (entity instanceof ShipAPI) {
                ShipAPI target = (ShipAPI) entity;

                if (target.isStation()) {
                    continue;
                } else if (target.isStationModule()) {
                    ShipAPI parent = target.getParentStation();

                    if ((parent == ship || parent.isStation())
                                || target.getStationSlot() == null) {
                        continue;
                    }

                    target = parent;
                }

                if (target.isFighter()) {
                    if (target.getWing() == null) continue;
                    if (target.getWing().getSpec() == null) continue;
                }

                float odds;

                odds = getAssistOdds(target);
                if (odds > bestAssistOdds) {
                    bestAssistOdds = odds;
                    bestAssistTarget = target;
                }

                if (retreating) {
                    odds = getShipShieldOdds(target);
                    if (odds > bestShipShieldOdds) {
                        bestShipShieldOdds = odds;
                        bestShipShieldTarget = target;
                    }

                    odds = getShieldOdds(target);
                    if (odds > bestShieldOdds) {
                        bestShieldOdds = odds;
                        bestShieldTarget = target;
                    }
                } else {
                    odds = getWreckingOdds(target);
                    if (odds > bestWreckingOdds) {
                        bestWreckingOdds = odds;
                        bestWreckingTarget = target;
                    }
                    odds = getSlowOdds(target);
                    if (odds > bestSlowOdds) {
                        bestSlowOdds = odds;
                        bestSlowTarget = target;
                    }

                    odds = getRescueOdds(target);
                    if (odds > bestRescueOdds) {
                        bestRescueOdds = odds;
                        bestRescueTarget = target;
                    }
                }
            }
        }

        // Doctor the results to improve consistency
        // by removing unlikely choices
        float avg = bestCollideOdds + bestWreckingOdds
                    + bestAssistOdds + bestRescueOdds
                    + bestShipShieldOdds + bestShieldOdds
                    + doNothingOdds;
        avg /= count;

        if (bestCollideOdds < avg) { bestCollider = null; }
        if (bestWreckingOdds < avg) { bestWreckingTarget = null; }
        if (bestSlowOdds < avg) { bestSlowTarget = null; }
        if (bestRescueOdds < avg) { bestRescueTarget = null; }
        if (bestShipShieldOdds < avg) { bestShipShieldTarget = null; }
        if (bestShieldOdds < avg) { bestShieldTarget = null; }
        if (bestAssistOdds < avg) { bestAssistTarget = null; }
        if (doNothingOdds < avg) { doNothingOdds = Float.MIN_VALUE; }

        // Randomly pick one of the results
        WeightedRandomPicker<CombatEntityAPI> picker = new WeightedRandomPicker<>();
        if (bestCollider != null) picker.add(bestCollider, bestCollideOdds);
        if (bestWreckingTarget != null) picker.add(bestWreckingTarget, bestWreckingOdds);
        if (bestSlowTarget != null) picker.add(bestSlowTarget, bestSlowOdds);
        if (bestRescueTarget != null) picker.add(bestRescueTarget, bestRescueOdds);
        if (bestShipShieldTarget != null) picker.add(bestShipShieldTarget, bestShipShieldOdds);
        if (bestShieldTarget != null) picker.add(bestShieldTarget, bestShieldOdds);
        if (bestAssistTarget != null) picker.add(bestAssistTarget, bestAssistOdds);
        picker.add(null, doNothingOdds);

        CombatEntityAPI pick = picker.pick();

        if (pick != null) {
//            if (pick instanceof ShipAPI) ship.setShipTarget((ShipAPI) pick);
            ship.getAIFlags().setFlag(AIFlags.SYSTEM_TARGET_COORDS, 0.1f, pick.getLocation());
            if (stats.isUsable(system, ship)) {
//                if (pick instanceof ShipAPI) ship.setShipTarget((ShipAPI) pick);
                ship.useSystem();
//                Global.getCombatEngine().addFloatingText(ship.getLocation(), "\"Phasenet activate!\"", 25f, Color.BLUE, ship, 0f, 0.2f);
//                Global.getCombatEngine().addFloatingText(pick.getLocation(), "\"I've been grabbed!\"", 25f, Color.RED, pick, 0f, 0.2f);
//                Global.getCombatEngine().addFloatingText(pick.getLocation(), "Phasenet", 25f, Color.RED, pick, 0f, 0.2f);
            } else {
//                Global.getCombatEngine().addFloatingText(ship.getLocation(), "\"Didn't work!\"", 25f, Color.RED, ship, 0f, 0.2f);
//                Global.getCombatEngine().addFloatingText(pick.getLocation(), "\"lol, fail!\"", 25f, Color.BLUE, pick, 0f, 0.2f);
            }
        }
    }

    private float getWreckingOdds(ShipAPI target) {
        if (target == null) return 0f;
        if (ship.getOwner() == target.getOwner()) return 0f;
        if (!target.isAlive()) return 0f;

        // Should avoid targets that are already netted
        for (String key : target.getCustomData().keySet()) {
            if (key.startsWith(ASSIST_KEY)) return 0f;
        }

        // Only target fighters that are worth killing
        if (target.isFighter()) {
            FighterWingSpecAPI spec = target.getWing().getSpec();
            float cooldown = system.getSpecAPI().getCooldown(ship.getMutableStats());
            if (spec.getRefitTime() / 2f < cooldown) return 0f;
        }

        float odds = 1f / estimatePullTime(target);

        if (target.isFighter()) odds /= 2f;
        if (isRetreating(target)) odds *= 2f;
        if (target == ship.getShipTarget()) odds *= 2f;

        return odds * WRECKING_WEIGHT_MULT;
    }

    private float getSlowOdds(ShipAPI target) {
        if (target == null) return 0f;
        if (ship.getOwner() == target.getOwner()) return 0f;
        if (!target.isAlive()) return 0f;

        // Should avoid targets that are already netted
        for (String key : target.getCustomData().keySet()) {
            if (key.startsWith(key)) return 0f;
        }

        return 0f;
    }

    private float getRescueOdds(ShipAPI target) {
        if (target == null) return 0f;
        if (ship.getOwner() != target.getOwner()) return 0f;
        if (!target.isAlive()) return 0f;

        // Should avoid targets that are already netted
        for (String key : target.getCustomData().keySet()) {
            if (key.startsWith(key)) return 0f;
        }

        return 0f;
    }

    private float getShipShieldOdds(ShipAPI target) {
        if (target == null) return 0f;
        if (ship.getOwner() == target.getOwner()) return 0f;
        if (!target.isAlive()) return 0f;

        // Should avoid targets that are already netted
        for (String key : target.getCustomData().keySet()) {
            if (key.startsWith(key)) return 0f;
        }

        // Only target fighters that are worth killing
        if (target.isFighter()) {
            FighterWingSpecAPI spec = target.getWing().getSpec();
            if (spec.getRefitTime() / 2f < system.getSpecAPI().getCooldown(ship.getMutableStats())) return 0f;
        }

        return 0f;
    }

    private float getShieldOdds(ShipAPI target) {
        if (target == null) return 0f;
        if (ship.getOwner() == target.getOwner()) return 0f;
        if (!target.isAlive()) return 0f;

        // Should avoid targets that are already netted
        for (String key : target.getCustomData().keySet()) {
            if (key.startsWith(key)) return 0f;
        }

        // Only target fighters that are worth killing
        if (target.isFighter()) {
            FighterWingSpecAPI spec = target.getWing().getSpec();
            if (spec.getRefitTime() / 2f < system.getSpecAPI().getCooldown(ship.getMutableStats())) return 0f;
        }

        return 0f;
    }

    private float getAssistOdds(ShipAPI target) {
        if (target == null) return 0f;
        if (ship.getOwner() == target.getOwner()) return 0f;
        if (!target.isAlive()) return 0f;

        Map<String, Object> custom = target.getCustomData();

        int phasenetsActive = 0;
        float avgPullAngle = 0;
        for (String key : custom.keySet()) {
            if (key.startsWith(ASSIST_KEY)) {
                phasenetsActive++;
                avgPullAngle += (float) custom.get(key);
            }
        }

        // No one to assist
        if (phasenetsActive == 0) return 0f;

        // Target is already overwhelmed
        if ((estimatePullSpeed(target) * phasenetsActive) / getTargetsSpeed(target) > 1f) return 0f;

        // Confirm angle is +- 60 degrees
        avgPullAngle /= phasenetsActive;
        float currentPullAngle = Misc.getAngleInDegrees(Roider_PhasenetStats.getFocusPoint(ship, target), target.getLocation());
        if (currentPullAngle < 0) currentPullAngle += 360f;
        if (currentPullAngle > avgPullAngle + 60f) return 0f;
        if (currentPullAngle < avgPullAngle - 60f) return 0f;

        // Only target fighters that are worth killing
        if (target.isFighter()) {
            FighterWingSpecAPI spec = target.getWing().getSpec();
            if (spec.getRefitTime() / 2f < system.getSpecAPI().getCooldown(ship.getMutableStats())) return 0f;
        }

        float odds = 1f / estimatePullTime(target);

        if (target.isFighter()) odds /= 2f;
        if (isRetreating(target)) odds *= 2f;
        if (target == ship.getShipTarget()) odds *= 2f;

        return odds * ASSIST_WEIGHT_MULT;
    }

    private void pickResultActive(boolean retreating) {

    }

    private ShipAPI pickWreckingTimeTarget() {
        if (system.isActive()) return null;

        float bestPullTime = Short.MAX_VALUE;
        ShipAPI bestPick = null;

        MAIN:
        for (ShipAPI target : engine.getShips()) {
            if (target == ship) continue;
            if (engine.getFleetManager(FleetSide.PLAYER).getDeployedCopy().contains(target.getFleetMember())) continue;
            if (!target.isAlive()) continue;


            // Should avoid targets that are already netted
            for (String key : target.getCustomData().keySet()) {
                if (key.startsWith(key)) continue MAIN;
            }


            if (Roider_PhasenetStats.isTargetInRange(target, ship)) {
                ShipAPI pick = target;

                if (target.isStation()) {
                    pick = null;
                } else if (target.isStationModule()) {
                    ShipAPI parent = target.getParentStation();

                    if (parent != ship && !parent.isStation() && target.getStationSlot() != null) {
                        pick = parent;
                    } else {
                        pick = null;
                    }
                }

                if (pick != null && pick.isFighter()) {
                    FighterWingSpecAPI spec = pick.getWing().getSpec();
                    if (spec.getRefitTime() / 2f < system.getSpecAPI().getCooldown(ship.getMutableStats())) pick = null;
                }

                if (pick == null)  continue;

                float pullTime = estimatePullTime(pick);

                if (pullTime < bestPullTime) {
                    bestPick = pick;
                    bestPullTime = pullTime;
                }
            }
        }

        return bestPick;
    }

    private ShipAPI pickAssistTarget() {
        if (system.isActive()) return null;

        float bestPullTime = Short.MAX_VALUE;
        ShipAPI bestPick = null;

        for (ShipAPI target : engine.getShips()) {
            if (target == ship) continue;
            if (engine.getFleetManager(FleetSide.PLAYER).getDeployedCopy().contains(target.getFleetMember())) continue;
            if (!target.isAlive()) continue;

            Map<String, Object> custom = target.getCustomData();

            int phasenetsActive = 0;
            for (String key : custom.keySet()) {
                if (key.startsWith(key)) phasenetsActive++;
            }

            // No one to assist
            if (phasenetsActive == 0) continue;

            // Target is already overwhelmed
            if ((estimatePullSpeed(target) * phasenetsActive) / getTargetsSpeed(target) > 1f) continue;


            // Pick best assist target
            if (Roider_PhasenetStats.isTargetInRange(target, ship)) {
                ShipAPI pick = target;

                if (target.isStation()) {
                    pick = null;
                } else if (target.isStationModule()) {
                    ShipAPI parent = target.getParentStation();

                    if (parent != ship && !parent.isStation() && target.getStationSlot() != null) {
                        pick = parent;
                    } else {
                        pick = null;
                    }
                }

                if (pick != null && pick.isFighter()) {
                    FighterWingSpecAPI spec = pick.getWing().getSpec();
                    if (spec.getRefitTime() / 2f < system.getSpecAPI().getCooldown(ship.getMutableStats())) pick = null;
                }

                if (pick == null)  continue;

                // Need to adjust based on other Phasenets' vectors

                // Don't want to pull in opposite directions generally



                float pullTime = estimatePullTime(pick);

                if (pullTime < bestPullTime) {
                    bestPick = pick;
                    bestPullTime = pullTime;
                }
            }
        }

        return bestPick;
    }

    private float getTargetsSpeed(CombatEntityAPI target) {
        if (!(target instanceof ShipAPI)) return 0;

        ShipAPI tShip = (ShipAPI) target;

        if (tShip.isFighter() || tShip.getEngineController().isFlamedOut()
                || !tShip.isAlive()) return 0;

        return tShip.getMaxSpeed();
    }

    public static float estimatePullSpeed(CombatEntityAPI target) {
        float speed = MAX_FORCE / Roider_PhasenetStats.getNormalizedMass(target.getMass());

        boolean noEngines = !(target instanceof ShipAPI);

        if (!noEngines) {
            ShipAPI tShip = (ShipAPI) target;
            noEngines = tShip.isFighter() || tShip.getEngineController().isFlamedOut()
                || !tShip.isAlive();

            if (noEngines && speed > MAX_PULL_SPEED) {
                speed = MAX_PULL_SPEED;
            } else if (speed > tShip.getMaxSpeed() + MAX_PULL_SPEED) {
                speed = tShip.getMaxSpeed() + MAX_PULL_SPEED;
            }

//            if (tShip.isFighter()) speed /= 10f;
        }

        return speed;
    }

    private float estimatePullTime(CombatEntityAPI target) {
        float speed = estimatePullSpeed(target);

        return Misc.getDistance(target.getLocation(), Roider_PhasenetStats.getFocusPoint(ship, target)) / speed;
    }

}
