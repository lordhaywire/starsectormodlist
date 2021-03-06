package scripts.combat;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spawns extra smoke when a Pile Driver fires or is on cooldown
 *
 * Modified by SafariJohn to not spawn certain effects on certain weapon slots for certain hulls
 *
 * @author Nicke535
 */
public class Roider_PileDriverSmoke implements EveryFrameWeaponEffectPlugin {


    private static final String DEFAULT = "default";

    private static final String SMOKE1 = "SMOKE1";
    private static final String SMOKE2 = "SMOKE2";
    private static final String SMOKE3 = "SMOKE3";
    private static final String SMOKE4 = "SMOKE4";
    private static final String EXHAUST_PUFF = "EXHAUST_PUFF";
    private static final String EXHAUST = "EXHAUST";

    // These are the rear effects
    private static final List<String> REAR_EFFECTS = new ArrayList<>();
    static {
        REAR_EFFECTS.add(EXHAUST_PUFF);
        REAR_EFFECTS.add(EXHAUST);
    }

    // These specific weapon slots shouldn't show the rear effects
    // Also used by Roider_SpikeDriverSmoke
    public static final Map<String, List> PROHIBITED_REAR = new HashMap<>();
    static {
       List<String> slotIds = new ArrayList<>();
       slotIds.add("WS0001");
       slotIds.add("WS0002");
       PROHIBITED_REAR.put("loamt_alastair", slotIds);
       slotIds = new ArrayList<>();
       slotIds.add("WS0005");
       slotIds.add("WS0006");
       PROHIBITED_REAR.put("loamt_macnamara", slotIds);
       slotIds = new ArrayList<>();
       slotIds.add("WS 006");
       slotIds.add("WS 007");
       PROHIBITED_REAR.put("roider_wrecker", slotIds);
       slotIds = new ArrayList<>();
       slotIds.add("WS 001");
       PROHIBITED_REAR.put("roider_marza", slotIds);
       slotIds = new ArrayList<>();
       slotIds.add("WS0018");
       PROHIBITED_REAR.put("vic_apollyon", slotIds);
       slotIds = new ArrayList<>();
       slotIds.add("WS0007");
       PROHIBITED_REAR.put("vic_moloch", slotIds);
       slotIds = new ArrayList<>();
       slotIds.add("WS0005");
       PROHIBITED_REAR.put("vic_valafar", slotIds);
       slotIds = new ArrayList<>();
       slotIds.add("WS0005");
       slotIds.add("WS0006");
       PROHIBITED_REAR.put("vic_kobal", slotIds);
       slotIds = new ArrayList<>();
       slotIds.add("WS0005");
       slotIds.add("WS0013");
       PROHIBITED_REAR.put("vic_thamuz", slotIds);
       slotIds = new ArrayList<>();
       slotIds.add("WS0006");
       PROHIBITED_REAR.put("vic_jezebeth", slotIds);
    }

    /*

        HOW TO USE:
        USED_IDS specifies which IDs to use for the rest of the script; any ID is valid EXCEPT the unique ID DEFAULT. Each ID should only be used once on the same weapon
        The script will spawn one particle "system" for each ID in this list, with the specific attributes of that ID.

        All the different Maps<> specify the attributes of each of the particle "systems"; they MUST have something defined as DEFAULT, and can have specific fields for specific IDs
        in the USED_IDS list; any field not filled in for a specific ID will revert to DEFAULT instead.

    */
    private static final List<String> USED_IDS = new ArrayList<>();
    static {
        USED_IDS.add(SMOKE1);
        USED_IDS.add(SMOKE2);
        USED_IDS.add(SMOKE3);
        USED_IDS.add(SMOKE4);
        USED_IDS.add(EXHAUST_PUFF);
        USED_IDS.add(EXHAUST);
    }

    //The amount of particles spawned immediately when the weapon reaches full charge level
    //  -For projectile weapons, this is when the projectile is actually fired
    //  -For beam weapons, this is when the beam has reached maximum brightness
    private static final Map<String, Integer> ON_SHOT_PARTICLE_COUNT = new HashMap<>();
    static {
        ON_SHOT_PARTICLE_COUNT.put(DEFAULT, 2);
        ON_SHOT_PARTICLE_COUNT.put(SMOKE1, 40);
        ON_SHOT_PARTICLE_COUNT.put(SMOKE2, 30);
        ON_SHOT_PARTICLE_COUNT.put(SMOKE3, 40);
        ON_SHOT_PARTICLE_COUNT.put(SMOKE4, 30);
        ON_SHOT_PARTICLE_COUNT.put(EXHAUST_PUFF, 10);
    }

    //How many particles are spawned each second the weapon is firing, on average
    private static final Map<String, Float> PARTICLES_PER_SECOND = new HashMap<>();
    static {
        PARTICLES_PER_SECOND.put(DEFAULT, 0f);
        PARTICLES_PER_SECOND.put(EXHAUST, 25f);
    }

    //Does the PARTICLES_PER_SECOND field get multiplied by the weapon's current chargeLevel?
    private static final Map<String, Boolean> AFFECTED_BY_CHARGELEVEL = new HashMap<>();
    static {
        AFFECTED_BY_CHARGELEVEL.put(DEFAULT, false);
        AFFECTED_BY_CHARGELEVEL.put(EXHAUST, true);
    }

    //Does the PARTICLE_SIZE fields get multiplied by the weapon's current chargeLevel?
    private static final Map<String, Boolean> SIZE_AFFECTED_BY_CHARGELEVEL = new HashMap<>();
    static {
        SIZE_AFFECTED_BY_CHARGELEVEL.put(DEFAULT, false);
        SIZE_AFFECTED_BY_CHARGELEVEL.put(EXHAUST, true);
    }

    //When are the particles spawned (only used for PARTICLES_PER_SECOND)? Valid values are "CHARGEUP", "FIRING", "CHARGEDOWN", "READY" (not on cooldown or firing) and "COOLDOWN".
    //  Multiple of these values can be combined via "-" inbetween; "CHARGEUP-CHARGEDOWN" is for example valid
    private static final Map<String, String> PARTICLE_SPAWN_MOMENT = new HashMap<>();
    static {
        PARTICLE_SPAWN_MOMENT.put(DEFAULT, "FIRING");
        PARTICLE_SPAWN_MOMENT.put(EXHAUST, "CHARGEDOWN-COOLDOWN");
    }

    //If this is set to true, the particles spawn with regard to *barrel*, not *center*. Only works for ALTERNATING barrel types on weapons: for LINKED barrels you
    //  should instead set up their coordinates manually with PARTICLE_SPAWN_POINT_TURRET and PARTICLE_SPAWN_POINT_HARDPOINT
    private static final Map<String, Boolean> SPAWN_POINT_ANCHOR_ALTERNATION = new HashMap<>();
    static {
        SPAWN_POINT_ANCHOR_ALTERNATION.put(DEFAULT, false);
    }

    //The position the particles are spawned (or at least where their arc originates when using offsets) compared to their weapon's center [or shot offset, see
    //SPAWN_POINT_ANCHOR_ALTERNATION above], if the weapon is a turret (or HIDDEN)
    private static final Map<String, Vector2f> PARTICLE_SPAWN_POINT_TURRET = new HashMap<>();
    static {
        PARTICLE_SPAWN_POINT_TURRET.put(DEFAULT, new Vector2f(22f, 0f));
        PARTICLE_SPAWN_POINT_TURRET.put(SMOKE1, new Vector2f(10f, 22f));
        PARTICLE_SPAWN_POINT_TURRET.put(SMOKE2, new Vector2f(16f, 20f));
        PARTICLE_SPAWN_POINT_TURRET.put(SMOKE3, new Vector2f(-10f, 22f));
        PARTICLE_SPAWN_POINT_TURRET.put(SMOKE4, new Vector2f(-16f, 20f));
        PARTICLE_SPAWN_POINT_TURRET.put(EXHAUST_PUFF, new Vector2f(0f, -20f));
        PARTICLE_SPAWN_POINT_TURRET.put(EXHAUST, new Vector2f(0f, -20f));
    }

    //The position the particles are spawned (or at least where their arc originates when using offsets) compared to their weapon's center [or shot offset, see
    //SPAWN_POINT_ANCHOR_ALTERNATION above], if the weapon is a hardpoint
    private static final Map<String, Vector2f> PARTICLE_SPAWN_POINT_HARDPOINT = new HashMap<>();
    static {
        PARTICLE_SPAWN_POINT_HARDPOINT.put(DEFAULT, new Vector2f(22f, 0f));
        PARTICLE_SPAWN_POINT_HARDPOINT.put(SMOKE1, new Vector2f(10f, 13f));
        PARTICLE_SPAWN_POINT_HARDPOINT.put(SMOKE2, new Vector2f(16f, 12f));
        PARTICLE_SPAWN_POINT_HARDPOINT.put(SMOKE3, new Vector2f(-10f, 13f));
        PARTICLE_SPAWN_POINT_HARDPOINT.put(SMOKE4, new Vector2f(-16f, 12f));
        PARTICLE_SPAWN_POINT_HARDPOINT.put(EXHAUST_PUFF, new Vector2f(0f, -25f));
        PARTICLE_SPAWN_POINT_HARDPOINT.put(EXHAUST, new Vector2f(0f, -25f));
    }

    //Which kind of particle is spawned (valid values are "EXPLOSION", "SMOOTH", "BRIGHT" and "SMOKE")
    private static final Map<String, String> PARTICLE_TYPE = new HashMap<>();
    static {
        PARTICLE_TYPE.put(DEFAULT, "BRIGHT");
    }

    //What color does the particles have?
    private static final Map<String, Color> PARTICLE_COLOR = new HashMap<>();
    static {
        PARTICLE_COLOR.put(DEFAULT, new Color(255, 225, 225, 215));
    }

    //What's the smallest size the particles can have?
    private static final Map<String, Float> PARTICLE_SIZE_MIN = new HashMap<>();
    static {
        PARTICLE_SIZE_MIN.put(DEFAULT, 5f);
        PARTICLE_SIZE_MIN.put(EXHAUST, 5f);
    }

    //What's the largest size the particles can have?
    private static final Map<String, Float> PARTICLE_SIZE_MAX = new HashMap<>();
    static {
        PARTICLE_SIZE_MAX.put(DEFAULT, 20f);
        PARTICLE_SIZE_MAX.put(SMOKE2, 15f);
        PARTICLE_SIZE_MAX.put(SMOKE4, 15f);
        PARTICLE_SIZE_MAX.put(EXHAUST, 15f);
    }

    //What's the lowest velocity a particle can spawn with (can be negative)?
    private static final Map<String, Float> PARTICLE_VELOCITY_MIN = new HashMap<>();
    static {
        PARTICLE_VELOCITY_MIN.put(DEFAULT, 5f);
        PARTICLE_VELOCITY_MIN.put(EXHAUST, 100f);
    }

    //What's the highest velocity a particle can spawn with (can be negative)?
    private static final Map<String, Float> PARTICLE_VELOCITY_MAX = new HashMap<>();
    static {
        PARTICLE_VELOCITY_MAX.put(DEFAULT, 50f);
        PARTICLE_VELOCITY_MAX.put(EXHAUST, 300f);
    }

    //The shortest duration a particle will last before completely fading away
    private static final Map<String, Float> PARTICLE_DURATION_MIN = new HashMap<>();
    static {
        PARTICLE_DURATION_MIN.put(DEFAULT, 0.2f);
        PARTICLE_DURATION_MIN.put(EXHAUST, 0.05f);
    }

    //The longest duration a particle will last before completely fading away
    private static final Map<String, Float> PARTICLE_DURATION_MAX = new HashMap<>();
    static {
        PARTICLE_DURATION_MAX.put(DEFAULT, 0.85f);
        PARTICLE_DURATION_MAX.put(EXHAUST, 0.2f);
    }

    //The shortest along their velocity vector any individual particle is allowed to spawn (can be negative to spawn behind their origin point)
    private static final Map<String, Float> PARTICLE_OFFSET_MIN = new HashMap<>();
    static {
        PARTICLE_OFFSET_MIN.put(DEFAULT, 0f);
    }

    //The furthest along their velocity vector any individual particle is allowed to spawn (can be negative to spawn behind their origin point)
    private static final Map<String, Float> PARTICLE_OFFSET_MAX = new HashMap<>();
    static {
        PARTICLE_OFFSET_MAX.put(DEFAULT, 25f);
        PARTICLE_OFFSET_MAX.put(DEFAULT, 10f);
        PARTICLE_OFFSET_MAX.put(EXHAUST, 0f);
    }

    //The width of the "arc" the particles spawn in; affects both offset and velocity. 360f = full circle, 0f = straight line
    private static final Map<String, Float> PARTICLE_ARC = new HashMap<>();
    static {
        PARTICLE_ARC.put(DEFAULT, 15f);
        PARTICLE_ARC.put(EXHAUST_PUFF, 10f);
        PARTICLE_ARC.put(EXHAUST, 5f);
    }

    //The offset of the "arc" the particles spawn in, compared to the weapon's forward facing.
    //  For example: 90f = the center of the arc is 90 degrees clockwise around the weapon, 0f = the same arc center as the weapon's facing.
    private static final Map<String, Float> PARTICLE_ARC_FACING = new HashMap<>();
    static {
        PARTICLE_ARC_FACING.put(DEFAULT, 0f);
        PARTICLE_ARC_FACING.put(SMOKE1, 3f);
        PARTICLE_ARC_FACING.put(SMOKE2, 6f);
        PARTICLE_ARC_FACING.put(SMOKE3, -3f);
        PARTICLE_ARC_FACING.put(SMOKE4, -6f);
        PARTICLE_ARC_FACING.put(EXHAUST_PUFF, 180f);
        PARTICLE_ARC_FACING.put(EXHAUST, 180f);
    }


    //-----------------------------------------------------------You don't need to touch stuff beyond this point!------------------------------------------------------------


    //These ones are used in-script, so don't touch them!
    private boolean hasFiredThisCharge = false;
    private int currentBarrel = 0;
    private boolean shouldOffsetBarrelExtra = false;


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //Don't run while paused, or without a weapon
        if (weapon == null || amount <= 0f) {return;}

        //Determine if this slot prohibits rear effects
        String hullId = weapon.getShip().getHullSpec().getHullId();
        String weaponSlotId = weapon.getSlot().getId();
        boolean noRearEffects = PROHIBITED_REAR.containsKey(hullId)
                    && PROHIBITED_REAR.get(hullId).contains(weaponSlotId);

        //Hidden slots never have rear effects
        noRearEffects |= weapon.getSlot().isHidden();

        //Saves handy variables used later
        float chargeLevel = weapon.getChargeLevel();
        String sequenceState = "READY";
        if (chargeLevel > 0 && (!weapon.isBeam() || weapon.isFiring())) {
            if (chargeLevel >= 1f) {
                sequenceState = "FIRING";
            } else if (!hasFiredThisCharge) {
                sequenceState = "CHARGEUP";
            } else {
                sequenceState = "CHARGEDOWN";
            }
        } else if (weapon.getCooldownRemaining() > 0) {
            sequenceState = "COOLDOWN";
        }

        //Adjustment for burst beams, since they are a pain
        if (weapon.isBurstBeam() && sequenceState.contains("CHARGEDOWN")) {
            chargeLevel = Math.max(0f, Math.min(Math.abs(weapon.getCooldownRemaining()-weapon.getCooldown()) / weapon.getSpec().getDerivedStats().getBurstFireDuration(), 1f));
        }

        //The sequenceStates "CHARGEDOWN" and "COOLDOWN" counts its barrel as 1 earlier than usual, due to code limitations
        shouldOffsetBarrelExtra = sequenceState.contains("CHARGEDOWN") || sequenceState.contains("COOLDOWN");

        //We go through each of our particle systems and handle their particle spawning
        for (String ID : USED_IDS) {
            //If prohibited, skip rear effects
            if (noRearEffects && REAR_EFFECTS.contains(ID)) continue;

            //Store all the values used for this check, and use default values if we don't have specific values for our ID specified
            //Note that particle count, specifically, is not declared here and is only used in more local if-cases
            boolean affectedByChargeLevel = AFFECTED_BY_CHARGELEVEL.get(DEFAULT);
            if (AFFECTED_BY_CHARGELEVEL.keySet().contains(ID)) { affectedByChargeLevel = AFFECTED_BY_CHARGELEVEL.get(ID); }

            boolean sizeAffectedByChargeLevel = SIZE_AFFECTED_BY_CHARGELEVEL.get(DEFAULT);
            if (SIZE_AFFECTED_BY_CHARGELEVEL.keySet().contains(ID)) { sizeAffectedByChargeLevel = SIZE_AFFECTED_BY_CHARGELEVEL.get(ID); }

            String particleSpawnMoment = PARTICLE_SPAWN_MOMENT.get(DEFAULT);
            if (PARTICLE_SPAWN_MOMENT.keySet().contains(ID)) { particleSpawnMoment = PARTICLE_SPAWN_MOMENT.get(ID); }

            boolean spawnPointAnchorAlternation = SPAWN_POINT_ANCHOR_ALTERNATION.get(DEFAULT);
            if (SPAWN_POINT_ANCHOR_ALTERNATION.keySet().contains(ID)) { spawnPointAnchorAlternation = SPAWN_POINT_ANCHOR_ALTERNATION.get(ID); }

            //Here, we only store one value, depending on if we're a hardpoint or not
            Vector2f particleSpawnPoint = PARTICLE_SPAWN_POINT_TURRET.get(DEFAULT);
            if (weapon.getSlot().isHardpoint()) {
                particleSpawnPoint = PARTICLE_SPAWN_POINT_HARDPOINT.get(DEFAULT);
                if (PARTICLE_SPAWN_POINT_HARDPOINT.keySet().contains(ID)) { particleSpawnPoint = PARTICLE_SPAWN_POINT_HARDPOINT.get(ID); }
            } else {
                if (PARTICLE_SPAWN_POINT_TURRET.keySet().contains(ID)) { particleSpawnPoint = PARTICLE_SPAWN_POINT_TURRET.get(ID); }
            }

            String particleType = PARTICLE_TYPE.get(DEFAULT);
            if (PARTICLE_TYPE.keySet().contains(ID)) { particleType = PARTICLE_TYPE.get(ID); }

            Color particleColor = PARTICLE_COLOR.get(DEFAULT);
            if (PARTICLE_COLOR.keySet().contains(ID)) { particleColor = PARTICLE_COLOR.get(ID); }

            float particleSizeMin = PARTICLE_SIZE_MIN.get(DEFAULT);
            if (PARTICLE_SIZE_MIN.keySet().contains(ID)) { particleSizeMin = PARTICLE_SIZE_MIN.get(ID); }
            float particleSizeMax = PARTICLE_SIZE_MAX.get(DEFAULT);
            if (PARTICLE_SIZE_MAX.keySet().contains(ID)) { particleSizeMax = PARTICLE_SIZE_MAX.get(ID); }

            float particleVelocityMin = PARTICLE_VELOCITY_MIN.get(DEFAULT);
            if (PARTICLE_VELOCITY_MIN.keySet().contains(ID)) { particleVelocityMin = PARTICLE_VELOCITY_MIN.get(ID); }
            float particleVelocityMax = PARTICLE_VELOCITY_MAX.get(DEFAULT);
            if (PARTICLE_VELOCITY_MAX.keySet().contains(ID)) { particleVelocityMax = PARTICLE_VELOCITY_MAX.get(ID); }

            float particleDurationMin = PARTICLE_DURATION_MIN.get(DEFAULT);
            if (PARTICLE_DURATION_MIN.keySet().contains(ID)) { particleDurationMin = PARTICLE_DURATION_MIN.get(ID); }
            float particleDurationMax = PARTICLE_DURATION_MAX.get(DEFAULT);
            if (PARTICLE_DURATION_MAX.keySet().contains(ID)) { particleDurationMax = PARTICLE_DURATION_MAX.get(ID); }

            float particleOffsetMin = PARTICLE_OFFSET_MIN.get(DEFAULT);
            if (PARTICLE_OFFSET_MIN.keySet().contains(ID)) { particleOffsetMin = PARTICLE_OFFSET_MIN.get(ID); }
            float particleOffsetMax = PARTICLE_OFFSET_MAX.get(DEFAULT);
            if (PARTICLE_OFFSET_MAX.keySet().contains(ID)) { particleOffsetMax = PARTICLE_OFFSET_MAX.get(ID); }

            float particleArc = PARTICLE_ARC.get(DEFAULT);
            if (PARTICLE_ARC.keySet().contains(ID)) { particleArc = PARTICLE_ARC.get(ID); }
            float particleArcFacing = PARTICLE_ARC_FACING.get(DEFAULT);
            if (PARTICLE_ARC_FACING.keySet().contains(ID)) { particleArcFacing = PARTICLE_ARC_FACING.get(ID); }
            //---------------------------------------END OF DECLARATIONS-----------------------------------------

            //First, spawn "on full firing" particles, since those ignore sequence state
            if (chargeLevel >= 1f && !hasFiredThisCharge) {
                //Count spawned particles: only trigger if the spawned particles are more than 0
                float particleCount = ON_SHOT_PARTICLE_COUNT.get(DEFAULT);
                if (ON_SHOT_PARTICLE_COUNT.keySet().contains(ID)) { particleCount = ON_SHOT_PARTICLE_COUNT.get(ID); }

                if (particleCount > 0) {
                    spawnParticles(engine, weapon, particleCount, particleType, spawnPointAnchorAlternation, particleSpawnPoint, particleColor, particleSizeMin, particleSizeMax, particleVelocityMin, particleVelocityMax,
                            particleDurationMin, particleDurationMax, particleOffsetMin, particleOffsetMax, particleArc, particleArcFacing);
                }
            }

            //Then, we check if we should spawn particles over duration; only spawn if our spawn moment is in the declaration
            if (particleSpawnMoment.contains(sequenceState)) {
                //Get how many particles should be spawned this frame
                float particleCount = PARTICLES_PER_SECOND.get(DEFAULT);
                if (PARTICLES_PER_SECOND.keySet().contains(ID)) { particleCount = PARTICLES_PER_SECOND.get(ID); }
                particleCount *= amount;
                if (affectedByChargeLevel && (sequenceState.contains("CHARGEUP") || sequenceState.contains("CHARGEDOWN"))) { particleCount *= chargeLevel; }
                if (affectedByChargeLevel && sequenceState.contains("COOLDOWN")) { particleCount *= (weapon.getCooldownRemaining()/weapon.getCooldown()); }
                if (sizeAffectedByChargeLevel) { particleSizeMin *= chargeLevel; particleSizeMax *= chargeLevel;}

                //Then, if the particle count is greater than 0, we actually spawn the particles
                if (particleCount > 0f) {
                    spawnParticles(engine, weapon, particleCount, particleType, spawnPointAnchorAlternation, particleSpawnPoint, particleColor, particleSizeMin, particleSizeMax,
                            particleVelocityMin, particleVelocityMax, particleDurationMin, particleDurationMax, particleOffsetMin, particleOffsetMax,
                            particleArc, particleArcFacing);
                }
            }
        }

        //If this was our "reached full charge" frame, register that
        if (chargeLevel >= 1f && !hasFiredThisCharge) {
            hasFiredThisCharge = true;
        }

        //Increase our current barrel if we have <= 0 chargeLevel OR have ceased firing for now, if we alternate, and have fired at least once since we last increased it
        //Also make sure the barrels "loop around", and reset our hasFired variable
        if (hasFiredThisCharge && (chargeLevel <= 0f || !weapon.isFiring())) {
            hasFiredThisCharge = false;
            currentBarrel++;

            //We can *technically* have different barrel counts for hardpoints, hiddens and turrets, so take that into account
            int barrelCount = weapon.getSpec().getTurretAngleOffsets().size();
            if (weapon.getSlot().isHardpoint()) {
                barrelCount = weapon.getSpec().getHardpointAngleOffsets().size();
            } else if (weapon.getSlot().isHidden()) {
                barrelCount = weapon.getSpec().getHiddenAngleOffsets().size();
            }

            if (currentBarrel >= barrelCount) {
                currentBarrel = 0;
            }
        }
    }


    //Shorthand function for actually spawning the particles
    private void spawnParticles (CombatEngineAPI engine, WeaponAPI weapon, float count, String type, boolean anchorAlternation, Vector2f spawnPoint, Color color, float sizeMin, float sizeMax,
                                 float velocityMin, float velocityMax, float durationMin, float durationMax,
                                 float offsetMin, float offsetMax, float arc, float arcFacing) {
        //First, ensure we take barrel position into account if we use Anchor Alternation (note that the spawn location is actually rotated 90 degrees wrong, so we invert their x and y values)
        Vector2f trueCenterLocation = new Vector2f(spawnPoint.y, spawnPoint.x);
        float trueArcFacing = arcFacing;
        int trueCurrentBarrel = currentBarrel;
        if (currentBarrel > 0 && shouldOffsetBarrelExtra) { trueCurrentBarrel -= 1; }
        if (anchorAlternation) {
            if (weapon.getSlot().isHardpoint()) {
                if (currentBarrel <= 0 && shouldOffsetBarrelExtra) { trueCurrentBarrel = weapon.getSpec().getHardpointAngleOffsets().size()-1; }
                trueCenterLocation.x += weapon.getSpec().getHardpointFireOffsets().get(currentBarrel).x;
                trueCenterLocation.y += weapon.getSpec().getHardpointFireOffsets().get(currentBarrel).y;
                trueArcFacing += weapon.getSpec().getHardpointAngleOffsets().get(currentBarrel);
            } else if (weapon.getSlot().isTurret()) {
                if (currentBarrel <= 0 && shouldOffsetBarrelExtra) { trueCurrentBarrel = weapon.getSpec().getTurretAngleOffsets().size()-1; }
                trueCenterLocation.x += weapon.getSpec().getTurretFireOffsets().get(currentBarrel).x;
                trueCenterLocation.y += weapon.getSpec().getTurretFireOffsets().get(currentBarrel).y;
                trueArcFacing += weapon.getSpec().getTurretAngleOffsets().get(currentBarrel);
            } else {
                if (currentBarrel <= 0 && shouldOffsetBarrelExtra) { trueCurrentBarrel = weapon.getSpec().getHiddenAngleOffsets().size()-1; }
                trueCenterLocation.x += weapon.getSpec().getHiddenFireOffsets().get(currentBarrel).x;
                trueCenterLocation.y += weapon.getSpec().getHiddenFireOffsets().get(currentBarrel).y;
                trueArcFacing += weapon.getSpec().getHiddenAngleOffsets().get(currentBarrel);
            }
        }

        //Then, we offset the true position and facing with our weapon's position and facing, while also rotating the position depending on facing
        trueArcFacing += weapon.getCurrAngle();
        trueCenterLocation = VectorUtils.rotate(trueCenterLocation, weapon.getCurrAngle(), new Vector2f(0f, 0f));
        trueCenterLocation.x += weapon.getLocation().x;
        trueCenterLocation.y += weapon.getLocation().y;

        //Then, we can finally start spawning particles
        float counter = count;
        while (Math.random() < counter) {
            //Ticks down the counter
            counter--;

            //Gets a velocity for the particle
            float arcPoint = MathUtils.getRandomNumberInRange(trueArcFacing-(arc/2f), trueArcFacing+(arc/2f));
            Vector2f velocity = MathUtils.getPointOnCircumference(weapon.getShip().getVelocity(), MathUtils.getRandomNumberInRange(velocityMin, velocityMax),
                    arcPoint);

            //Gets a spawn location in the cone, depending on our offsetMin/Max
            Vector2f spawnLocation = MathUtils.getPointOnCircumference(trueCenterLocation, MathUtils.getRandomNumberInRange(offsetMin, offsetMax),
                    arcPoint);

            //Gets our duration
            float duration = MathUtils.getRandomNumberInRange(durationMin, durationMax);

            //Gets our size
            float size = MathUtils.getRandomNumberInRange(sizeMin, sizeMax);

            //Finally, determine type of particle to actually spawn and spawns it
            switch (type) {
                case "SMOOTH":
                    engine.addSmoothParticle(spawnLocation, velocity, size, 1f, duration, color);
                    break;
                case "SMOKE":
                    engine.addSmokeParticle(spawnLocation, velocity, size, 1f, duration, color);
                    break;
                case "EXPLOSION":
                    engine.spawnExplosion(spawnLocation, velocity, color, size, duration);
                    break;
                default:
                    engine.addHitParticle(spawnLocation, velocity, size, 10f, duration, color);
                    break;
            }
        }
    }
}