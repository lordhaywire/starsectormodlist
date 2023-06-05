package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.MuzzleFlashSpec;

/**
 */
public class ocua_hytsuna_effect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    private AnimationAPI Gun_D;
    private static float CHARGE_UP_NEED_TIME = 0f;
    private static float CHARGE_DOWN_NEED_TIME = 0f;

    //EveryFrameWeaponEffectPlugin would be generated for each weapon that implement it, so the local variable could be used correctly
    private boolean init = false;
    private float increaseFactor = 0;
    private float growthFactor = 0;
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
        
        if(weapon.getId().contains("ocua_mi_hytsu")) {
            CHARGE_UP_NEED_TIME = 0.1f;
            CHARGE_DOWN_NEED_TIME = 3f;
        } else if(weapon.getId().contains("ocua_mi_hytsuna")) {
            CHARGE_UP_NEED_TIME = 0.1f;
            CHARGE_DOWN_NEED_TIME = 3f;
        } else if(weapon.getId().contains("ocua_mi_hytsuyuna")) {
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
        if (increaseFactor != 0){
            Gun_D.setFrame(1);
        } else if (increaseFactor > 0.5){
            Gun_D.setFrame(2);
        } else {
            Gun_D.setFrame(0);
        }
    }
    
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        //engine.removeEntity(projectile);
        
        String gun_id;
        if (weapon.getId().contains("ocua_mi_hytsu")) {
            if (increaseFactor < 0.5f){
                gun_id = "ocua_mi_hytsu_low";
            } else {
                gun_id = "ocua_mi_hytsu_high";
            }
        }
        else if (weapon.getId().contains("ocua_mi_hytsuna")) {
            if (increaseFactor < 0.33f){
                gun_id = "ocua_mi_hytsuna_low";
            } else if (increaseFactor < 0.67f){
                gun_id = "ocua_mi_hytsuna_mid";
            } else {
                gun_id = "ocua_mi_hytsuna_high";
            }
        }
        else if (weapon.getId().contains("ocua_mi_hytsuyuna")) {
            if (increaseFactor < 0.25f){
                gun_id = "ocua_mi_hytsuyuna_low";
            } else if (increaseFactor < 0.5f){
                gun_id = "ocua_mi_hytsuyuna_mid";
            } else if (increaseFactor < 0.75f){
                gun_id = "ocua_mi_hytsuyuna_high";
            } else {
                gun_id = "ocua_mi_hytsuyuna_full";
            }
        } else {
            gun_id = "ocua_mi_hytsu_low";
        }
        
        if (weapon.isFiring()) {
            float angle = weapon.getCurrAngle();
            DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile(
                weapon.getShip(), weapon, gun_id, weapon.getFirePoint(0), angle, weapon.getShip().getVelocity());
        }
    }
    
}
