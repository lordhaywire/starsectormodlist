package scripts.combat;

import com.fs.starfarer.api.combat.*;

/**
 * Author: SafariJohn
 */
public class Roider_SpikeDriverReload implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    private Roider_SpikeDriverSmoke smoke = new Roider_SpikeDriverSmoke();

    private boolean fired = false;
    private float currentCooldown = 0f;
    private float cooldownRemaining = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        smoke.advance(amount, engine, weapon);

        if (fired) {
            fired = false;

            float baseCooldown = weapon.getCooldown();
            float maxSpread = weapon.getSpec().getMaxSpread() * weapon.getShip().getMutableStats().getMaxRecoilMult().getModifiedValue();
            if (weapon.getSlot().isHardpoint()) maxSpread /= 2f;

            currentCooldown = baseCooldown + (baseCooldown - (weapon.getCurrSpread() / maxSpread) * baseCooldown);
            cooldownRemaining = currentCooldown;
        }

        if (cooldownRemaining > 0) {
            // Cooldown is off for some reason compared to unmodified weapon cooldown
            if (cooldownRemaining < amount * 3f) {
                cooldownRemaining = 0;
                weapon.setRemainingCooldownTo(0);
            } else {
                cooldownRemaining -= amount * weapon.getShip().getMutableStats().getBallisticRoFMult().getModifiedValue();
                weapon.setRemainingCooldownTo(cooldownRemaining / currentCooldown);
            }
        }
    }


    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        fired = true;
    }

}
