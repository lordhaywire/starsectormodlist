package scripts.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import ids.Roider_Ids.Roider_Categories;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;
import scripts.Roider_Misc;

/**
 * Author: SafariJohn
 */
public class Roider_PhasenetStats extends BaseShipSystemScript {

    public static final String ASSIST_KEY = "roider_phasenetActive";

    public static final String ARC_SOUND = "roider_system_phasenet_arc";
    public static final String ARC_START_SOUND = "roider_system_phasenet_arc_start";

    private static final String GLOW_1_SPRITE_ID = "wrecker_glow1";
    private static final String GLOW_2_SPRITE_ID = "wrecker_glow2";

    private static final Color GLOW_1_COLOR = new Color(255, 175, 255, 255);
    private static final Color GLOW_2_COLOR = new Color(255, 0, 255, 150);

    //How long does the "normal" phasing take, and what is the opacity for the ship there?
    private static final float PHASE_TIME = 0.2f;
    private static final float PHASE_OPACITY = 0.2f;

    public static final float MAX_FORCE = 10000f;
    public static final float PEAK_FORCE_MULT = 2f;

    // Restrict top grab speed for fighters and the like
    public static final float MAX_PULL_SPEED = 100f;

    public static final float MAX_ACCEL = 80f;
    public static final float MIN_ACCEL = 20f;

    public static final float RANGE = 2000f;

    private CombatEntityAPI target;

    private boolean hasSpawnedFadeInGlow = false;
    private boolean hasSpawnedArcs = false;

    private IntervalUtil interval = new IntervalUtil(0.01f, 0.01f);
    private IntervalUtil blinkInterval = new IntervalUtil(0.5f, 0.5f);
    private IntervalUtil arcInterval = new IntervalUtil(0.05f, 2f);

    private List<WeaponSlotAPI> arcSlotsFired = new ArrayList<>();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        if (Global.getCombatEngine().isPaused()) return;

        interval.advance(amount);

        if (!interval.intervalElapsed()) return;

        //Ensures we have a ship
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        // Copied visuals code with permission from MesoTroniK's tiandong_fluxOverrideStats.java
        WeaponAPI glowDeco1 = null;
        WeaponAPI glowDeco2 = null;
        for (WeaponAPI weapon : ship.getAllWeapons())
        {
            if (weapon.getId().endsWith("_glow1") && weapon.getId().startsWith("roider_"))
            {
                glowDeco1 = weapon;
            }
            if (weapon.getId().endsWith("_glow2") && weapon.getId().startsWith("roider_"))
            {
                glowDeco2 = weapon;
            }
        }

        ship.setJitter(ship, GLOW_1_COLOR, effectLevel / 30f, 2, 20f);
        ship.setJitterUnder(ship, GLOW_1_COLOR, effectLevel / 8f, 4, 30f);
        ship.setJitter(ship, GLOW_2_COLOR, effectLevel / 30f, 6, 30f);
        ship.setJitterUnder(ship, GLOW_2_COLOR, effectLevel / 8f, 10, 50f);
        if (glowDeco1 != null && glowDeco1.getAnimation() != null)
        {
            glowDeco1.getSprite().setAdditiveBlend();
            glowDeco1.getSprite().setColor(GLOW_1_COLOR);
            glowDeco1.getAnimation().setAlphaMult(effectLevel);
            glowDeco1.getAnimation().setFrame(1);
            renderWithJitter(glowDeco1.getSprite(), 0, 0, 10, 1);
        }
        if (glowDeco2 != null && glowDeco2.getAnimation() != null)
        {
            glowDeco2.getSprite().setAdditiveBlend();
            glowDeco2.getSprite().setColor(GLOW_2_COLOR);
            glowDeco2.getAnimation().setAlphaMult(effectLevel);
            glowDeco2.getAnimation().setFrame(1);
            renderWithJitter(glowDeco2.getSprite(), 0, 0, 10, 1);
        }

        SpriteAPI sprite1 = Global.getSettings().getSprite(Roider_Categories.GRAPHICS_COMBAT, GLOW_1_SPRITE_ID);
        SpriteAPI sprite2 = Global.getSettings().getSprite(Roider_Categories.GRAPHICS_COMBAT, GLOW_2_SPRITE_ID);
        CombatEngineLayers layer = CombatEngineLayers.ABOVE_SHIPS_LAYER;

        if (state == State.IN && !hasSpawnedFadeInGlow) {
            hasSpawnedFadeInGlow = true;
            // Fade up and down
            MagicRender.objectspace(sprite1, ship, new Vector2f(0f, 0f), new Vector2f(0f, 0f),
                    new Vector2f(sprite1.getWidth(), sprite1.getHeight()), new Vector2f(0f, 0f),
                    180f, 0f, true, GLOW_1_COLOR, true,
                    2f, 1f,
                    0f, 0f,
                    0f,
                    PHASE_TIME * 0.666f, 0, PHASE_TIME * 0.333f,
                    true, layer);
            MagicRender.objectspace(sprite2, ship, new Vector2f(0f, 0f), new Vector2f(0f, 0f),
                    new Vector2f(sprite2.getWidth(), sprite2.getHeight()), new Vector2f(0f, 0f),
                    180f, 0f, true, GLOW_2_COLOR, true,
                    2f, 1f,
                    0f, 0f,
                    0f,
                    PHASE_TIME * 0.666f, 0, PHASE_TIME * 0.333f,
                    true, layer);

            // Fade up
            MagicRender.objectspace(sprite1, ship, new Vector2f(0f, 0f), new Vector2f(0f, 0f),
                    new Vector2f(sprite1.getWidth(), sprite1.getHeight()), new Vector2f(0f, 0f),
                    180f, 0f, true, GLOW_1_COLOR, true,
                    2f, 1f,
                    0f, 0f,
                    0f,
                    PHASE_TIME, 0, 0.1f,
                    true, layer);
            MagicRender.objectspace(sprite2, ship, new Vector2f(0f, 0f), new Vector2f(0f, 0f),
                    new Vector2f(sprite2.getWidth(), sprite2.getHeight()), new Vector2f(0f, 0f),
                    180f, 0f, true, GLOW_2_COLOR, true,
                    2f, 1f,
                    0f, 0f,
                    0f,
                    PHASE_TIME, 0, 0.1f,
                    true, layer);

        }

        // Regardless of target, spawn the phase-in glow and cooldown glow

        // phase-in glow is two pairs of glows
        // one glows up for 2/3 then down for 1/3
        // the other just goes up over the same period


        // cooldown glow is a simple fade out


//        if (true) return;

        if (state == State.IN && target == null) {
            target = pickTarget(ship);
        }

        if (target != null && target.isExpired()) target = null;

        if (target == null) {
            ship.getSystem().deactivate();
            return;
        }

		if (!isTargetInRange(ship, target)) {
            ship.getSystem().deactivate();
            return;
        }

        if (state == State.IN && !hasSpawnedArcs) {
            hasSpawnedArcs = true;
            // Create arcs to target
            List<WeaponSlotAPI> arcEmitters = findArcSlots(ship);
            for (WeaponSlotAPI slot : arcEmitters) {
                Vector2f slotLoc = slot.computePosition(ship);
                EmpArcEntityAPI arc = (EmpArcEntityAPI)Global.getCombatEngine().spawnEmpArcPierceShields(ship, slotLoc, ship, target,
                                   DamageType.ENERGY,
                                   0,
                                   0, // emp
                                   100000f, // max range
                                   ARC_START_SOUND,
                                   40f, // thickness
//                                   new Color(100,165,255,255),
                                   GLOW_1_COLOR,
                                   new Color(255,255,255,255)
                                   );
                if (target instanceof ShipAPI) arc.setTargetToShipCenter(slotLoc, (ShipAPI) target);
                arc.setCoreWidthOverride(30f);

                Global.getSoundPlayer().playSound(ARC_START_SOUND, 1f, 1f, slotLoc, ship.getVelocity());
            }
        }


        Vector2f focus = getFocusPoint(ship, target);

        // Save phasenet angle for other ships to assist
        float angle = Misc.getAngleInDegrees(focus, target.getLocation());
        if (angle < 0) angle += 360f;
        target.setCustomData(ASSIST_KEY + "_" + ship.getId(), angle);

        // Debug: show where focus point is
//        SpriteAPI sprite1 = Global.getSettings().getSprite(Roider_Categories.GRAPHICS_COMBAT, GLOW_1_SPRITE_ID);
//        MagicRender.singleframe(sprite1, focus, new Vector2f(sprite1.getWidth(), sprite1.getHeight()), ship.getFacing(), GLOW_1_COLOR, true);

        float forceMult = getForceMult(state, effectLevel);

        applyForce(id, focus, ship, state, target, forceMult, interval.getElapsed());


        if (state == State.ACTIVE) {
            MagicRender.objectspace(sprite1, ship, new Vector2f(0f, 0f), new Vector2f(0f, 0f),
                    new Vector2f(sprite1.getWidth(), sprite1.getHeight()), new Vector2f(0f, 0f),
                    180f, 0f, true, GLOW_1_COLOR, true,
                    2f, 1f,
                    0f, 0f,
                    0f,
                    0, 0.02f, 0,
                    true, layer);
            MagicRender.objectspace(sprite1, ship, new Vector2f(0f, 0f), new Vector2f(0f, 0f),
                    new Vector2f(sprite1.getWidth(), sprite1.getHeight()), new Vector2f(0f, 0f),
                    180f, 0f, true, GLOW_1_COLOR, true,
                    2f, 1f,
                    0f, 0f,
                    0f,
                    0, 0.02f, 0,
                    true, layer);
            MagicRender.objectspace(sprite2, ship, new Vector2f(0f, 0f), new Vector2f(0f, 0f),
                    new Vector2f(sprite2.getWidth(), sprite2.getHeight()), new Vector2f(0f, 0f),
                    180f, 0f, true, GLOW_2_COLOR, true,
                    2f, 1f,
                    0f, 0f,
                    0f,
                    0, 0.02f, 0,
                    true, layer);
        }

        if (state == State.OUT && hasSpawnedFadeInGlow) {
            hasSpawnedFadeInGlow = false;
            MagicRender.objectspace(sprite1, ship, new Vector2f(0f, 0f), new Vector2f(0f, 0f),
                    new Vector2f(sprite1.getWidth(), sprite1.getHeight()), new Vector2f(0f, 0f),
                    180f, 0f, true, GLOW_1_COLOR, true,
                    2f, 1f,
                    0f, 0f,
                    0f,
                    0f, 0f, 1f,
                    true, layer);
            MagicRender.objectspace(sprite1, ship, new Vector2f(0f, 0f), new Vector2f(0f, 0f),
                    new Vector2f(sprite1.getWidth(), sprite1.getHeight()), new Vector2f(0f, 0f),
                    180f, 0f, true, GLOW_1_COLOR, true,
                    2f, 1f,
                    0f, 0f,
                    0f,
                    0f, 0f, 1f,
                    true, layer);
            MagicRender.objectspace(sprite2, ship, new Vector2f(0f, 0f), new Vector2f(0f, 0f),
                    new Vector2f(sprite2.getWidth(), sprite2.getHeight()), new Vector2f(0f, 0f),
                    180f, 0f, true, GLOW_2_COLOR, true,
                    2f, 1f,
                    0f, 0f,
                    0f,
                    0f, 0f, 1f,
                    true, layer);
        }
    }

	public void renderWithJitter(SpriteAPI s, float x, float y, float maxJitter, int numCopies) {
		for (int i = 0; i < numCopies; i++) {
			Vector2f jv = new Vector2f();
			jv.x = (float) Math.random() * maxJitter - maxJitter/2f;
			jv.y = (float) Math.random() * maxJitter - maxJitter/2f;
			//if (jv.lengthSquared() != 0) jv.normalise();
			s.render(x + jv.x, y + jv.y);
		}
	}

	protected List<WeaponSlotAPI> findArcSlots(ShipAPI ship) {
        List<WeaponSlotAPI> arcEmitters = new ArrayList<>();
		for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			if (slot.isSystemSlot()) {
                if (slot.getId().startsWith("PN")) arcEmitters.add(slot);
			}
		}

        return arcEmitters;
	}

	protected void applyForce(String id, Vector2f focus, ShipAPI ship, State state,
                CombatEntityAPI target, float forceMult, float amount) {
        if (!isTargetInRange(ship, target)) return;

//        float distMult = 1f + dist / RANGE;

		float targetSpeed;
        if (target.getMass() < 1) targetSpeed = MAX_FORCE;
        else targetSpeed = MAX_FORCE / getNormalizedMass(target.getMass());

        Vector2f loc = target.getLocation();

//        float wMass = Math.max(0, ship.getMass());
//        float tMass = target.getMass();
//
//        float mult = tMass / (wMass * 2);
//        mult = Roider_Misc.clamp(mult, 0, 1);
//
//        float phaseAccel = MAX_ACCEL - ((MAX_ACCEL - MIN_ACCEL) * mult);
//
//        float tAccel = getTargetAccelInline(ship, target);
//
//        acceleration = phaseAccel - tAccel;

        float focusDist = Misc.getDistance(focus, ship.getLocation());
        float shipDist = Misc.getDistance(ship.getLocation(), loc);

        boolean tooClose = focusDist > shipDist;
        boolean farAway = shipDist > focusDist * 2;
        // With a tiny zone in-between



        float angleToSource = Misc.getAngleInDegrees(ship.getLocation(), loc);

        // Give some leeway
        boolean wrongAngleClock = angleToSource - 30 > ship.getFacing();
        boolean wrongAngleCounter = angleToSource + 30 < ship.getFacing();

        boolean wrongAngle = wrongAngleClock || wrongAngleCounter;

        float targetAngle = Misc.getAngleInDegrees(loc, focus);

        float wrongAngleDegrees = angleToSource - ship.getFacing();

        if (wrongAngleDegrees < 0) wrongAngleDegrees += 360;

        // Target is far away
        if (farAway) {
//            if (acceleration > shipDist - focusDist) acceleration = shipDist - focusDist;

        }
        // Target is too close and at wrong angle
        else if (tooClose && wrongAngle) {
            if (wrongAngleDegrees <= 180) targetAngle += 90 * (wrongAngleDegrees / 360);
            else targetAngle -= 90 * (wrongAngleDegrees / 360);
//            if (acceleration > focusDist - shipDist) acceleration = focusDist - shipDist;
//            acceleration *= 2;

        }
        // Target is too close
        else if (tooClose) {
//            if (acceleration > focusDist - shipDist) acceleration = focusDist - shipDist;

//            acceleration *= 2;
        }
        // Target is close enough, but at wrong angle
        else if (wrongAngle) {
            if (wrongAngleDegrees <= 180) targetAngle += 45 * (wrongAngleDegrees / 360);
            else targetAngle -= 45 * (wrongAngleDegrees / 360);
        }
        // Target is close enough
        else {
        }


        ShipAPI tShip;

        boolean noEngines = !(target instanceof ShipAPI);
        // If target is a ship, check its engines
        if (!noEngines) {
            tShip = (ShipAPI) target;

            // Knock fighter engines out, if possible
            if (tShip.isFighter() && tShip.getMutableStats().getEngineDamageTakenMult().computeMultMod() > 0) {
                tShip.getEngineController().forceFlameout(true);
            }


            noEngines |= tShip.getEngineController().isFlamedOut()
                    || !tShip.isAlive();

            // Cap pull speed
            if (noEngines && targetSpeed > MAX_PULL_SPEED) {
                targetSpeed = MAX_PULL_SPEED;
            } else if (targetSpeed > tShip.getMaxSpeed() + MAX_PULL_SPEED) {
                targetSpeed = tShip.getMaxSpeed() + MAX_PULL_SPEED;
            }
        }

		Vector2f targetVector = Misc.getUnitVectorAtDegreeAngle(targetAngle);
		targetVector.scale(targetSpeed * forceMult);

        target.setCustomData(Roider_PhasenetEffectManager.KEY, true);
        target.setCustomData(Roider_PhasenetEffectManager.EFFECT_KEY + id, targetVector);

        Vector2f vel = target.getVelocity();

        blinkInterval.advance(amount);

        if (blinkInterval.intervalElapsed() && target instanceof ShipAPI) {
            ((ShipAPI) target).addAfterimage(GLOW_1_COLOR,
                        0,
                        0,
                        targetVector.x * forceMult,
                        targetVector.y * forceMult,
                        5f, 0.1f, 0.5f, 0.5f, true,
                        false, false);

            ((ShipAPI) target).addAfterimage(new Color(255, 175, 255, 100),
                        0,
                        0,
                        0,
                        0,
                        5f, 0.1f, 0.5f, 0.1f, true,
                        true, true);
        }

        // Create arcs to target
        arcInterval.advance(amount);
        if (arcInterval.intervalElapsed() && state != State.OUT) {
            List<WeaponSlotAPI> arcEmitters = findArcSlots(ship);
            Random rand = new Random();

            WeaponSlotAPI slot = arcEmitters.get(rand.nextInt(arcEmitters.size()));
            if (arcEmitters.size() > 1) {
                while (arcSlotsFired.contains(slot)) {
                    slot = arcEmitters.get(rand.nextInt(arcEmitters.size()));
                }
            }

            if (arcSlotsFired.size() == arcEmitters.size() - 1) {
                arcSlotsFired.clear();
            }

            arcSlotsFired.add(slot);

            Vector2f slotLoc = slot.computePosition(ship);
            EmpArcEntityAPI arc = (EmpArcEntityAPI)Global.getCombatEngine().spawnEmpArcPierceShields(ship, slotLoc, ship, target,
                               DamageType.ENERGY,
                               0,
                               0, // emp
                               100000f, // max range
                               ARC_SOUND,
                               40f, // thickness
//                               new Color(100,165,255,255),
                               GLOW_1_COLOR,
                               new Color(255,255,255,255)
                               );
            if (target instanceof ShipAPI) arc.setTargetToShipCenter(slotLoc, (ShipAPI) target);
            arc.setCoreWidthOverride(30f);

            Global.getSoundPlayer().playSound(ARC_START_SOUND, 1f, 1f, slotLoc, ship.getVelocity());
        }

//		if (Math.abs(loc.x - focus.x) > 100 || Math.abs(loc.y - focus.y) > 100) {
//            loc.x += targetVector.x * forceMult * amount;
//            loc.y += targetVector.y * forceMult * amount;
//        } else {
//            vel.x += dir.x * forceMult / 100f;
//            vel.y += dir.y * forceMult / 100f;
//        }

        if (vel.lengthSquared() == 0) return;

//        if (noEngines && (Math.abs(vel.x) > acceleration / 2 || Math.abs(vel.y) > acceleration / 2)) {
        if (!(target instanceof ShipAPI)) {
            // Add source ship's velocity to movement
//            loc.x += ship.getVelocity().x * amount;
//            loc.y += ship.getVelocity().y * amount;
            loc.x += targetVector.x * forceMult * amount;
            loc.y += targetVector.y * forceMult * amount;

            // Reduce target's velocity to 0
            if (vel.x > 0) vel.x -= 1;
            if (vel.x < 0) vel.x += 1;

            if (vel.y > 0) vel.y -= 1;
            if (vel.y < 0) vel.y += 1;
        }
	}

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        super.unapply(stats, id);

        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            id = ASSIST_KEY + "_" + ship.getId();
        }

        if (target != null && target.getCustomData().keySet().contains(id)) target.removeCustomData(id);

        hasSpawnedFadeInGlow = false;
        hasSpawnedArcs = false;
        target = null;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isActive()) return "ACTIVE";
        if (system.isCoolingDown()) return "";
        if (isUsable(system, ship)) return "LOCKED";
        return "READY";
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (target == null) return null;

        int speed = (int) Roider_PhasenetAI.estimatePullSpeed(target);

        if (target instanceof ShipAPI) {
            ShipAPI tShip = (ShipAPI) target;
            if (index == 0) return new StatusData("Dragging "
                        + tShip.getHullSpec().getHullName() + " at " + speed + " su/sec", false);
        } else {
            if (index == 0) return new StatusData("Dragging asteroid at "
                        + speed + " speed", false);
        }
        return null;
    }

    private CombatEntityAPI pickTarget(ShipAPI ship) {
        CombatEntityAPI pick = null;

        if (pick == null && ship == Global.getCombatEngine().getPlayerShip() && ship.getAI() == null) {
            pick = ship.getShipTarget();
        } else {
            pick = getClosestTargetToPoint((Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS));
        }

        if (pick == null) {
            pick = getClosestTargetToPoint(ship.getMouseTarget());
        }

        if (pick == null) return null;

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

        if (pick == ship) pick = null;

        return pick;
    }

    public static boolean isTargetInRange(CombatEntityAPI ship, CombatEntityAPI t) {
        if (t == null) return false;

		float dist = Misc.getDistance(ship.getLocation(), t.getLocation());
		dist -= (ship.getCollisionRadius() + t.getCollisionRadius()) * 2;
		return dist <= RANGE;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (ship.getFluxTracker().isOverloadedOrVenting()) return false;

        if (target != null) {
            boolean inRange = isTargetInRange(ship, target);
            if (!inRange) target = null;
            return inRange;
        }

        target = pickTarget(ship);

        if (target == null) return false;

		if (!isTargetInRange(ship, target)) return false;

        // Reset target
        target = null;

        return true;
    }

    public static Vector2f getFocusPoint(CombatEntityAPI source, CombatEntityAPI target) {
        float radius = source.getCollisionRadius() + target.getCollisionRadius();
        radius *= 1.1f;
        float facing = source.getFacing();

        float rx = (float) Math.cos(Math.toRadians(facing)) * radius;
        float ry = (float) Math.sin(Math.toRadians(facing)) * radius;

        Vector2f focus = new Vector2f(source.getLocation());
        focus.x += rx;
        focus.y += ry;

        return focus;
    }

    private float getForceMult(State state, float effectLevel) {
        // Force spikes above max during activation
        if (state == State.IN) {
            float peakLevel = 0.666f;
            // Rises fast during first 2/3
            if (effectLevel < peakLevel) {
                float levelMult = 1f / peakLevel;
                return effectLevel * levelMult * PEAK_FORCE_MULT;
            }
            // Falls down to normal max in last 1/3
            else {
                // Reduce current level
                float level = (effectLevel - peakLevel);
                // Mult level so it reduces PEAK force from max to none
                level *= (PEAK_FORCE_MULT - 1f) / (1f - peakLevel);

                return PEAK_FORCE_MULT - level;
            }
        }

        // Otherwise it follows effectLevel
        return effectLevel;
    }

    private float getTargetAccelInline(CombatEntityAPI source, CombatEntityAPI target) {

        Vector2f focus = getFocusPoint(source, target);
        float angle = Misc.getAngleInDegrees(target.getLocation(), focus);

        float tAccel = getTargetAccel(target);
        float tAccelAngle = getTargetAccelAngle(target);

        angle = tAccelAngle - angle;

        return (float) Math.cos(Math.toRadians(angle)) * tAccel;
    }

    /**
     *
     * @param target
     * @return angle in degrees
     */
    private float getTargetAccelAngle(CombatEntityAPI target) {
        if (!(target instanceof ShipAPI)) return 0f;

        float facing = target.getFacing();

        ShipAPI tShip = (ShipAPI) target;
        ShipEngineControllerAPI eng = tShip.getEngineController();

        if (eng.isFlamedOut()) return 0;

        if (eng.isAccelerating() && eng.isStrafingLeft()) return facing + 45f;
        if (eng.isAccelerating() && eng.isStrafingRight()) return facing + -45f;
        if (eng.isDecelerating() && eng.isStrafingLeft()) return facing + 135f;
        if (eng.isDecelerating() && eng.isStrafingRight()) return facing + -135f;
        if (eng.isAccelerating()) return facing + 0f;
        if (eng.isDecelerating()) return facing + 180f;
        if (eng.isStrafingLeft()) return facing + 90f;
        if (eng.isStrafingRight()) return facing + -90f;

        return 0;
    }

    private float getTargetAccel(CombatEntityAPI target) {
        if (!(target instanceof ShipAPI)) return 0f;

        ShipAPI tShip = (ShipAPI) target;
        ShipEngineControllerAPI eng = tShip.getEngineController();

        float accel = tShip.getAcceleration();
        float decel = tShip.getDeceleration();

        if (eng.isFlamedOut()) return 0;

        if (eng.isAccelerating() && (eng.isStrafingLeft() || eng.isStrafingRight())) {
            return (float) Math.sqrt(Math.pow(accel, 2) + Math.pow(accel, 2));
        }
        if (eng.isDecelerating() && (eng.isStrafingLeft() || eng.isStrafingRight())) {
            return (float) Math.sqrt(Math.pow(accel, 2) + Math.pow(decel, 2));
        }
        if (eng.isAccelerating() || eng.isStrafingLeft() || eng.isStrafingRight()) return accel;
        if (eng.isDecelerating()) return decel;

        return 0;
    }

    private float getGoalSpeed(CombatEntityAPI ship, CombatEntityAPI target) {
        float wMass = Math.max(0, ship.getMass());
        float tMass = target.getMass();

        float mult = tMass / (wMass * 2);
        mult = Roider_Misc.clamp(mult, 0, 1);


        if (!(target instanceof ShipAPI)) return MAX_FORCE;
        ShipAPI tShip = (ShipAPI) target;

        float maxSpeed = tShip.getMaxSpeed();

        float angle = getTargetAccelAngle(target);

        float speed = (float) Math.cos(Math.toRadians(angle)) * maxSpeed * (1 + mult);

        return MAX_FORCE - speed;
    }

    public static float getNormalizedMass(float mass) {
        if (MAX_FORCE / mass > MAX_PULL_SPEED / 2f) {
            float result = mass + (MAX_FORCE / (MAX_PULL_SPEED / 2f) - mass) / 2f;
            return result;
        } else if (MAX_FORCE / mass < MAX_PULL_SPEED / 5f) {
            return mass - (mass - MAX_FORCE / (MAX_PULL_SPEED / 5f)) / 2f;
        }

        return mass;
    }

    private CombatEntityAPI getClosestTargetToPoint(Vector2f point) {
        if (point == null) return null;

        List<CombatEntityAPI> entities = new ArrayList<>();
        entities.addAll(Global.getCombatEngine().getAsteroids());
        entities.addAll(Global.getCombatEngine().getShips());

        CombatEntityAPI closest = null;
        float dist = Short.MAX_VALUE;
        for (CombatEntityAPI entity : entities) {
            float prox = Misc.getDistance(entity.getLocation(), point);
            if (prox <= entity.getCollisionRadius() * 3 && prox < dist) {
                closest = entity;
                dist = prox;
            }
        }

        return closest;
    }
}
