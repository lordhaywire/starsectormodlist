package scripts.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import ids.Roider_Ids.Roider_Categories;
import ids.Roider_Ids.Roider_Settings;
import java.util.*;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 * Author: SafariJohn
 */
public class Roider_PhasenetEffectManager extends BaseEveryFrameCombatPlugin {
    public static final String KEY = "roider_phasenetTarget";
    public static final String EFFECT_KEY = "roider_phasenetEffect_";

    private CombatEngineAPI engine;
    private Map<String, Vector2f> trueVelocities;
    private Map<String, Vector2f> prevVelocities;
    private Map<String, Vector2f> prevAccelerations;
    private Map<String, Float> baseMaxSpeeds;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        trueVelocities = new HashMap<>();
        prevVelocities = new HashMap<>();
        prevAccelerations = new HashMap<>();
        baseMaxSpeeds = new HashMap<>();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.isCombatOver()) return;

        // Find ships with phasenet active on them
        for (ShipAPI ship : engine.getShips()) {
            Map<String, Object> custom = ship.getCustomData();
            if (!custom.containsKey(KEY)) continue;

            // Get all phasenet target vectors
            Set<Vector2f> targetVectors = new HashSet<>();
            boolean affected = false;
            for (String key : new ArrayList<String>(custom.keySet())) {
                if (key.startsWith(EFFECT_KEY)) {
                    affected = true;
                    targetVectors.add((Vector2f) custom.get(key));

                    // Unset key here instead of trying to do it in the system script
                    ship.removeCustomData(key);
                }
            }

            if (!affected) {
                ship.getCustomData().remove(KEY);
                trueVelocities.remove(ship.getId());
                prevVelocities.remove(ship.getId());
                prevAccelerations.remove(ship.getId());
                baseMaxSpeeds.remove(ship.getId());
                ship.getMutableStats().getMaxSpeed().unmodify(KEY);
                continue;
            }

            // Sum target vectors into one vector
            Vector2f targetVectorSum = new Vector2f();
            for (Vector2f v : targetVectors) {
                targetVectorSum.x += v.x;
                targetVectorSum.y += v.y;
            }

            // Calculate previous acceleration effect on "true" veloicty
            Vector2f trueVelocity = trueVelocities.get(ship.getId());
            if (trueVelocity == null) {
                trueVelocity = new Vector2f(ship.getVelocity());
                trueVelocities.put(ship.getId(), trueVelocity);
            }

            Vector2f prevVelocity = prevVelocities.get(ship.getId());
            if (prevVelocity == null) {
                prevVelocity = new Vector2f(ship.getVelocity());
                prevVelocities.put(ship.getId(), prevVelocity);
            }

            Vector2f acceleration = Vector2f.sub(ship.getVelocity(), prevVelocity, null);

            prevVelocities.put(ship.getId(), new Vector2f(ship.getVelocity()));

            Vector2f prevAccel = prevAccelerations.get(ship.getId());
            if (prevAccel == null) prevAccel = new Vector2f();

            // Add difference in actual velocities (aka acceleration)
            // Subtract previous phasenet acceleration vector
            trueVelocity.x += acceleration.x - prevAccel.x;
            trueVelocity.y += acceleration.y - prevAccel.y;

            // Store base max speed so max can be tweaked later
            Float baseMaxSpeed = baseMaxSpeeds.get(ship.getId());
            if (baseMaxSpeed == null) {
                baseMaxSpeed = ship.getMaxSpeed();
                baseMaxSpeeds.put(ship.getId(), baseMaxSpeed);
            }

            if (ship.getEngineController().isFlamedOut() || !ship.isAlive()) {
                baseMaxSpeed = ship.getHullSpec().getEngineSpec().getMaxSpeed();
            }

            // If ship is not accelerating, shift its true velocity towards the focus
            if (acceleration.length() == 0 && trueVelocity.length() < baseMaxSpeed) {
                Vector2f oneStep = Misc.getUnitVectorAtDegreeAngle(VectorUtils.getFacing(targetVectorSum));
                oneStep.scale(5f);
                trueVelocity.x += oneStep.x;
                trueVelocity.y += oneStep.y;
            }

            // If ship is dead or flamed out, shift its true velocity more
            if ((ship.getEngineController().isFlamedOut()
                    || !ship.isAlive()) && trueVelocity.length() < baseMaxSpeed) {
                Vector2f oneStep = Misc.getUnitVectorAtDegreeAngle(VectorUtils.getFacing(targetVectorSum));
                oneStep.scale(5f);
                trueVelocity.x += oneStep.x;
                trueVelocity.y += oneStep.y;
            }

            // True velocity needs to be sharply limited to max speed
            if (trueVelocity.length() > baseMaxSpeed) {
                trueVelocity.scale(baseMaxSpeed / (trueVelocity.length() * 2f));
            }


            // Calculate target velocity from target vector and true velocity
            Vector2f targetVelocity = Vector2f.add(trueVelocity, targetVectorSum, null);

            // Calculate acceleration based on difference between target velocity and actual velocity
            Vector2f phasenetAcceleration = Vector2f.sub(targetVelocity, ship.getVelocity(), null);

            // Scale acceleration by time
//            phasenetAcceleration.x *= amount;
//            phasenetAcceleration.y *= amount;

            prevAccelerations.put(ship.getId(), phasenetAcceleration);

//            Map<String, Vector2f> velocities = new HashMap<>();
//            velocities.put("Target Vector", targetVectorSum);
//            velocities.put("True Velocity", trueVelocity);
//            velocities.put("Previous Velocity", prevVelocity);
//            velocities.put("Current Velocity", new Vector2f(ship.getVelocity()));
//            velocities.put("Acceleration", acceleration);
//            velocities.put("Previous Acceleration", prevAccel);
//            velocities.put("Target Velocity", targetVelocity);
//            velocities.put("Current Acceleration", phasenetAcceleration);

            // Apply acceleration to target's velocity
            ship.getVelocity().x += phasenetAcceleration.x;
            ship.getVelocity().y += phasenetAcceleration.y;

            if (ship.getVelocity().length() > baseMaxSpeed
                        && targetVelocity.length() > baseMaxSpeed) {
                ship.getMutableStats().getMaxSpeed().unmodify(KEY);
                baseMaxSpeed = ship.getMaxSpeed();
                baseMaxSpeeds.put(ship.getId(), baseMaxSpeed);
                ship.getMutableStats().getMaxSpeed().modifyFlat(KEY, targetVelocity.length() - baseMaxSpeed);
            } else {
                ship.getMutableStats().getMaxSpeed().unmodify(KEY);
                baseMaxSpeed = ship.getMaxSpeed();
                baseMaxSpeeds.put(ship.getId(), baseMaxSpeed);
            }

            if (ship == engine.getPlayerShip()) {
                String sprite = Global.getSettings().getSpriteName(Roider_Categories.GRAPHICS_COMBAT, Roider_Settings.ICON_PHASENET);
                if (targetVectors.size() > 1) {
                    engine.maintainStatusForPlayerShip(KEY, sprite, "Phasenet", "Dragged at " + (int) targetVectorSum.length() + " su/sec "
                                + "by " + targetVectors.size() + " phasenets", true);
                } else {
                    engine.maintainStatusForPlayerShip(KEY, sprite, "Phasenet", "Dragged at " + (int) targetVectorSum.length() + " su/sec", true);
                }
            }
        }
    }

}
