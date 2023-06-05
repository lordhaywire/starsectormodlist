package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.MuzzleFlashSpec;

/**
 */
public class ocua_stredia_effect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    private AnimationAPI Gun_D;
    private static float CHARGE_UP_NEED_TIME = 0f;
    private static float CHARGE_DOWN_NEED_TIME = 0f;

    //EveryFrameWeaponEffectPlugin would be generated for each weapon that implement it, so the local variable could be used correctly
    private float increaseFactor = 0;
    private float growthFactor = 0;
    private boolean init = false;
    private MuzzleFlashSpec Muzzle;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getShip().getOriginalOwner() == -1 || weapon.getShip().isHulk()) {
            return;
        }
        if (!init) {
            init = true;
            Muzzle = weapon.getMuzzleFlashSpec().clone();
            weapon.ensureClonedSpec();
        }
        
        if(weapon.getId().contains("ocua_mi_stredia")) {
            CHARGE_UP_NEED_TIME = 0.1f;
            CHARGE_DOWN_NEED_TIME = 3f;
        }
        
        if (!weapon.isFiring() && weapon.getChargeLevel() >= 0f) {
            increaseFactor -= amount / CHARGE_DOWN_NEED_TIME;
        } else {
            increaseFactor += amount / CHARGE_UP_NEED_TIME;
        }
        
        increaseFactor = Math.max(0, Math.min(1, increaseFactor));
        growthFactor = (int) (Math.max(0, Math.min(1, increaseFactor)));
        
        Gun_D = weapon.getAnimation();
        if (increaseFactor == 0){
            Gun_D.setFrame(1);
        } else if (increaseFactor > 0){
            Gun_D.setFrame(2);
        } else {
            Gun_D.setFrame(0);
        }
    }
    
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        String gun_id = "ocua_mi_stredia_z";
        if (weapon.isFiring() && growthFactor == 0) {
            engine.removeEntity(projectile);
            float angle = weapon.getCurrAngle();
            DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile(
                    weapon.getShip(), weapon, gun_id, weapon.getFirePoint(0), angle, weapon.getShip().getVelocity());
            Global.getSoundPlayer().playSound("hypervel_driver_fire", 1f, 0.9f, weapon.getFirePoint(0), weapon.getShip().getVelocity());
        }
    }
}