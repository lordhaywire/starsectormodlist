package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;
import com.fs.starfarer.api.loading.MuzzleFlashSpec;

/**
 * Yeah, this effect is 100% dead, since the weapon bonus does an infinite loop of ever-increasing buffs.
 * Won't be used.
 */
public class ocua_jaeldra_effect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    private AnimationAPI Gun_D;

    //EveryFrameWeaponEffectPlugin would be generated for each weapon that implement it, so the local variable could be used correctly
    private boolean init = false;
    private MuzzleFlashSpec Muzzle;
    private float InFlux = 0;
    private float rangeFlux = 0;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!init) {
            init = true;
            Muzzle = weapon.getMuzzleFlashSpec().clone();
            weapon.ensureClonedSpec();
        }
        if (weapon.getShip().getOriginalOwner() == -1 || weapon.getShip().isHulk()) {
            return;
        }
        ShipAPI ship = weapon.getShip();
        float Flux = ship.getFluxTracker().getFluxLevel();
        
        if (Flux > 0.1 && Flux < 0.5) { 
            InFlux = 0.5f - Flux;
            rangeFlux = 1f + ((Flux - 0.1f) / 2f);
        }
        else if (Flux >= 0.5) { 
            InFlux = 0f; 
            rangeFlux = 1.25f;
        }
        else {
            InFlux = 0.5f;
            rangeFlux = 1f;
        }
	ship.addListener(new WeaponRangeModifier() {
                @Override
		public float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
                @Override
		public float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.getId().equals("ocua_mi_jaeldra")) {
				return Math.max(0, Math.min(2, rangeFlux));
			}
			return 1f;
		}
                @Override
		public float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			return 0f;
		}
	});
    }
    
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        String gun_id = "ocua_mi_jaeldra";
        engine.removeEntity(projectile);
        ShipAPI ship = weapon.getShip();
        float angle = weapon.getCurrAngle();
        /*final float antiDam = weapon.getDamage().getBaseDamage();
        final float antiRange = weapon.getSpec().getMaxRange();
        float modAntiDam = antiDam * InFlux;
        float modAntiRange = antiRange * rangeFlux;
        weapon.getDamage().setDamage(modAntiDam);
        weapon.getSpec().setMaxRange(modAntiRange);*/
        DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile(
                    weapon.getShip(), weapon, gun_id, weapon.getFirePoint(0), angle, weapon.getShip().getVelocity());
    }
}