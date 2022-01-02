package scripts.combat;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;

/**
 * Author: SafariJohn
 */
public class Roider_RotaryHammerReloadScript implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    public static final int MAX_AMMO = 2;
    public static final float RELOAD_TIME = 120f; // seconds
    public static final int RELOAD_SIZE = 1;

    private Roider_RotaryHammerAnimationScript animation;
    private IntervalUtil reload;

    /**
     * Tracking weapon cooldown to halve artificial 0 -> 1 reload
     * cooldown if not already cooling down from firing
     */
    private IntervalUtil cooldown;
    private boolean onCooldown;
    private boolean rapidCooldown;

    public Roider_RotaryHammerReloadScript() {
        animation = new Roider_RotaryHammerAnimationScript();
        reload = new IntervalUtil(RELOAD_TIME, RELOAD_TIME);

        onCooldown = false;
        rapidCooldown = false;
        cooldown = new IntervalUtil(0, 0);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null) return;
        if (weapon == null) return;

        if (cooldown.getMaxInterval() == 0) {
            cooldown.setInterval(weapon.getCooldown(), weapon.getCooldown());
        }

        animation.advance(amount, engine, weapon);

        if (engine.isPaused()) return;

        // Only reloads up to 2
        if (weapon.getAmmo() >= MAX_AMMO) {
            reload.setElapsed(0);
            return;
        }

        reload.advance(amount);
        if (rapidCooldown) cooldown.advance(amount * 2f);
        else if (onCooldown) cooldown.advance(amount);

        if (cooldown.intervalElapsed()) {
            onCooldown = false;
            rapidCooldown = false;
            cooldown.setElapsed(0);
        }

        // Show reload if no ammo
        if (weapon.getAmmo() == 0) weapon.setRemainingCooldownTo(weapon.getCooldown() * (reload.getElapsed() / RELOAD_TIME));

        if (weapon.getAmmo() == 1 && rapidCooldown) {
            weapon.setRemainingCooldownTo(weapon.getCooldown() - cooldown.getElapsed());
        }

        if (reload.intervalElapsed()) {
            if (weapon.getAmmo() == 0 && !onCooldown) {
                rapidCooldown = true;
            }

            weapon.setAmmo(weapon.getAmmo() + RELOAD_SIZE);
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        animation.onFire(projectile, weapon, engine);

        onCooldown = true;
        cooldown.setElapsed(0);
    }

}
