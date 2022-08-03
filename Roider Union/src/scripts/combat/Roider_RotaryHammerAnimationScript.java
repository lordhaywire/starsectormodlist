package scripts.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Author: SafariJohn
 */
public class Roider_RotaryHammerAnimationScript implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    public static final float SLOW_MULT = 1.25f; // 25% slower
    public static final float SPEED_MULT = 1f / 1.5f; // 50% faster

    public static final String CLICK_SOUND = "roider_rhl_click";
    public static final String CLACK_SOUND = "roider_rhl_clack";
    public static final String LOUD_CLICK_SOUND = "roider_rhl_clickLoud";
    public static final String LOUD_CLACK_SOUND = "roider_rhl_clackLoud";

    private static final List<List<Pair<Integer, Float>>> FRAMES = new ArrayList<>();
    static {
        reloadAnimationFrames();
    }

    private static void reloadAnimationFrames() {
        FRAMES.clear();

        // Roll
        FRAMES.add(asList(new Pair(0, 0.02f),
                    new Pair(1, 0.07f),
                    new Pair(2, 0.06f),
                    new Pair(3, 0.05f),
                    new Pair(4, 0.04f),
                    new Pair(5, 0.03f),
                    new Pair(6, 0.03f),
                    new Pair(0, 0.02f)));

        // Slow roll
        FRAMES.add(asList(new Pair(0, 0.02f),
                    new Pair(1, 0.08f),
                    new Pair(2, 0.07f),
                    new Pair(3, 0.06f),
                    new Pair(4, 0.05f),
                    new Pair(5, 0.04f),
                    new Pair(6, 0.04f),
                    new Pair(0, 0.03f)));

        // Small rollback
        FRAMES.add(asList(new Pair(0, 0.02f),
                    new Pair(1, 0.07f),
                    new Pair(2, 0.06f),
                    new Pair(3, 0.05f),
                    new Pair(4, 0.04f),
                    new Pair(5, 0.03f),
                    new Pair(6, 0.03f),
                    new Pair(0, 0.04f),
                    new Pair(1, 0.05f),
                    new Pair(0, 0.02f)));

        // Large rollback
        FRAMES.add(asList(new Pair(0, 0.02f),
                    new Pair(1, 0.07f),
                    new Pair(2, 0.06f),
                    new Pair(3, 0.05f),
                    new Pair(4, 0.04f),
                    new Pair(5, 0.03f),
                    new Pair(6, 0.03f),
                    new Pair(0, 0.04f),
                    new Pair(1, 0.05f),
                    new Pair(2, 0.07f),
                    new Pair(1, 0.08f),
                    new Pair(0, 0.03f)));
    }

    private static List<Pair<Integer, Float>> asList(Pair<Integer, Float> ... frames) {
        return Arrays.asList(frames);
    }

    private boolean fired;
    private boolean animating;
    private boolean repeat;
    private boolean outOfAmmo;
    private boolean regenCycle;
    private int frame;
    private float playSpeed;
    private IntervalUtil tracker;
    private List<Pair<Integer, Float>> animation;


    public Roider_RotaryHammerAnimationScript() {
        fired = false;
        animating = false;
        repeat = false;
        outOfAmmo = false;
        regenCycle = false;
        frame = 0;
        playSpeed = 1f;
        tracker = new IntervalUtil(0, 0);
        animation = null;
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        fired = true;
        frame = 0;
        weapon.getAnimation().setFrame(0);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null) return;
        if (weapon == null) return;
        if (engine.isPaused()) return;

        if (weapon.getAmmo() == 0) {
            outOfAmmo = true;
            return;
        }

        // When ammo goes from 0 -> 1
        if (outOfAmmo && weapon.getAmmo() > 0) {
            outOfAmmo = false;
            regenCycle = true;
            animating = true;
            repeat = true;
            animation = FRAMES.get(new Random().nextInt(FRAMES.size()));
            playSpeed = getRandomSpeedMult();
        }

        // End of cycle
        if (!animating && !fired) {
            reloadAnimationFrames();

            regenCycle = false;

            weapon.getAnimation().pause();
            frame = 0;
            weapon.getAnimation().setFrame(0);
            return;
        }

        if (weapon.isDisabled()) return;

        if (fired) {
            fired = false;
            animating = true;
            repeat = true;
            animation = FRAMES.get(new Random().nextInt(FRAMES.size()));
            playSpeed = getRandomSpeedMult();
        }

        ShipAPI ship = weapon.getShip();
        if (ship == null) return;

        // Accelerate animation
        float mult = 1f;
        // Random mult for current animation
        mult *= playSpeed;
        // Mult from modified weapon rate of fire
        mult *= ship.getMutableStats().getMissileRoFMult().getModifiedValue();
        // Mult from rapid cooldown (Fast Missile Racks and the like)
        if (weapon.getCooldownRemaining() <= weapon.getCooldown() / 2f) {
            mult *= (weapon.getCooldownRemaining() + (weapon.getCooldown() / 3)) / weapon.getCooldown();
        }

        amount /= mult;

        tracker.advance(amount);
        if (!tracker.intervalElapsed()) return;

        frame++;

        // End of current animation
        if (frame == animation.size()) {
            if (regenCycle) Global.getSoundPlayer().playSound(LOUD_CLACK_SOUND, 1f, 1f, weapon.getLocation(), ship.getVelocity());
            else Global.getSoundPlayer().playSound(CLACK_SOUND, 1f, 1f, weapon.getLocation(), ship.getVelocity());

            frame = 0;
            if (repeat) {
                repeat = false;
            } else {
                animating = false;
            }
        } else {
            // Play ratchet sound each frame
            if (regenCycle) Global.getSoundPlayer().playSound(LOUD_CLICK_SOUND, 1f, 1f, weapon.getLocation(), ship.getVelocity());
            else Global.getSoundPlayer().playSound(CLICK_SOUND, 1f, 1f, weapon.getLocation(), ship.getVelocity());
        }

        weapon.getAnimation().setFrame(animation.get(frame).one);

        // Set delay until next frame
        if (animating && !repeat && frame == 0) {
            tracker.setInterval(0.3f, 0.6f);
        } else {
            tracker.setInterval(animation.get(frame).two,
                        animation.get(frame).two);
        }
    }

    private float getRandomSpeedMult() {
        return SPEED_MULT + new Random().nextFloat()
                    * (SLOW_MULT - SPEED_MULT);
    }

}
