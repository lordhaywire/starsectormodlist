package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.MuzzleFlashSpec;

/**
 */
public class xlu_gattling_effect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    private static final float FIRING_SPEED = 1f;
    private static float CHARGE_UP_NEED_TIME = 0f;
    private static float CHARGE_DOWN_NEED_TIME = 0f;

    //EveryFrameWeaponEffectPlugin would be generated for each weapon that implement it, so the local variable could be used correctly
    private float increaseFactor = 0;
    private float coolDownRemain = 0;
    private float growthFactor = 0;
    private float usedBarrel = 0;
    
    private static final float pelts = 3;
    private static final float arc = 5;
    
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
        
        if(weapon.getId().contains("xlu_gattling_s")) {
            CHARGE_UP_NEED_TIME = 0.33f;
            CHARGE_DOWN_NEED_TIME = 0.75f;
        }
        if(weapon.getId().contains("xlu_gattling_m")) {
            CHARGE_UP_NEED_TIME = 0.5f;
            CHARGE_DOWN_NEED_TIME = 1f;
        }
        if(weapon.getId().contains("xlu_gattling_l")) {
            CHARGE_UP_NEED_TIME = 0.75f;
            CHARGE_DOWN_NEED_TIME = 1.5f;
        }
        
        coolDownRemain = weapon.getCooldownRemaining();
        if (!weapon.isFiring() && weapon.getChargeLevel() <= 0) {
            increaseFactor -= amount / CHARGE_DOWN_NEED_TIME;
        } else {
            increaseFactor += amount / CHARGE_UP_NEED_TIME;
        }
        
        increaseFactor = Math.max(0, Math.min(1, increaseFactor));
        growthFactor = (int) (Math.max(0, Math.min(1, increaseFactor)) * pelts);
        
        if (coolDownRemain > 0) {
            coolDownRemain -= increaseFactor * amount * FIRING_SPEED;
            coolDownRemain = Math.max(0, Math.min(1, coolDownRemain));
            weapon.setRemainingCooldownTo(coolDownRemain);
        }

        weapon.getMuzzleFlashSpec().setLength(Muzzle.getLength() * (1f + increaseFactor * FIRING_SPEED * 0.1f));
    }
    
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        String gun_id;
        if (weapon.getId().contains("xlu_gattling_l")) {
            gun_id = "xlu_gattling_l";
            if (usedBarrel == 0) {
                usedBarrel = 1;
            } else {
                usedBarrel = 0;
            }
        }
        else if (weapon.getId().contains("xlu_gattling_m")) {
            gun_id = "xlu_gattling_m";
        } else {
            gun_id = "xlu_gattling_m";
        }
        
            for (int f = 1; f <= growthFactor; f++){
                if (weapon.isFiring()) {
                    float offset = ((float)Math.random() * arc) - (arc * 0.5f);
                    float angle = weapon.getCurrAngle() + offset;
                    float shotgunSpeed = (float)Math.random() * 0.4f + 0.7f;
                        DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile(
                                weapon.getShip(), weapon, gun_id, weapon.getFirePoint((int) usedBarrel), angle, weapon.getShip().getVelocity());
                        proj.getVelocity().scale(shotgunSpeed);
                }
            }
    }
    
}
            /*

    private Vector2f calculateMuzzle_dual(WeaponAPI weapon){
        float muzzle;
        
        if (weapon.getSlot().isHardpoint()) {
            muzzle = weapon.getSpec().getHardpointFireOffsets().get(0).getX();
        } else {
            muzzle = weapon.getSpec().getTurretFireOffsets().get(0).getX();
        }
        
        double angle = Math.toRadians(weapon.getCurrAngle());
        Vector2f dir = new Vector2f((float)Math.cos(angle),(float)Math.sin(angle));
        if (dir.lengthSquared() > 0f) dir.normalise();
        dir.scale(muzzle);
     
        if (usedBarrel == 0) {
            usedBarrel = 1;
            return Vector2f.add(weapon.getFirePoint(0),dir,new Vector2f());
        } else {
            usedBarrel = 0;
             return Vector2f.add(weapon.getFirePoint(1),dir,new Vector2f());
        }
    }
    
    private Vector2f calculateMuzzle(WeaponAPI weapon){
        float muzzle;
        if (weapon.getSlot().isHardpoint()) {
            muzzle = weapon.getSpec().getHardpointFireOffsets().get(0).getX();
        } else {
            muzzle = weapon.getSpec().getTurretFireOffsets().get(0).getX();
        }
        double angle = Math.toRadians(weapon.getCurrAngle());
        Vector2f dir = new Vector2f((float)Math.cos(angle),(float)Math.sin(angle));
        if (dir.lengthSquared() > 0f) dir.normalise();
        dir.scale(muzzle);
        Vector2f loc = new Vector2f(weapon.getLocation());
        return Vector2f.add(loc,dir,new Vector2f());
    }
    
        String gun_id;
        Vector2f bulletspawn;
        if (weapon.getId().contains("xlu_gattling_l")) {
            gun_id = "xlu_gattling_l";
            bulletspawn = calculateMuzzle_dual(weapon);
        }
        else {
            gun_id = "xlu_gattling_m";
            bulletspawn = calculateMuzzle(weapon);
        }
            for (int f = 1; f <= growthFactor; f++){
                if (weapon.isFiring()) {
                    float offset = ((float)Math.random() * arc) - (arc * 0.5f);
                    float angle = weapon.getCurrAngle() + offset;
                    float shotgunSpeed = (float)Math.random() * 0.4f + 0.7f;
                    /*if (weapon.getId().contains("xlu_gattling_l")) {
                        DamagingProjectileAPI proj1 = (DamagingProjectileAPI) engine.spawnProjectile 
                            (weapon.getShip(), weapon, gun_id, bulletspawn, angle, weapon.getShip().getVelocity());
                        DamagingProjectileAPI proj2 = (DamagingProjectileAPI) engine.spawnProjectile 
                            (weapon.getShip(), weapon, gun_id, bulletspawn, angle, weapon.getShip().getVelocity());
                        proj1.getVelocity().scale(shotgunSpeed);
                        proj2.getVelocity().scale(shotgunSpeed);
                    } else {
                        DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile 
                            (weapon.getShip(), weapon, gun_id, bulletspawn, angle, weapon.getShip().getVelocity());
                        proj.getVelocity().scale(shotgunSpeed);
                    //}
                }
            }*/
