package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

/**
 * UNUSED
 */
public class xlu_manganate_cannon_effect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
        private static final float arc = 10;
        private static final float pelts = 30;
        
        private boolean init = false;
        
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getShip().getOriginalOwner() == -1 || weapon.getShip().isHulk()) {
            return;
        }
        if (!init) {
            init = true;
        }
    }
    
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        engine.removeEntity(projectile);
        
        String gun_id = "xlu_manganate_cannon_z";
            
        for (int f = 1; f <= pelts; f++){
            if (weapon.isFiring()) {
                float offset = ((float)Math.random() * arc) - (arc * 0.5f);
                float angle = weapon.getCurrAngle() + offset;
                DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile 
                    (weapon.getShip(), weapon, gun_id, weapon.getFirePoint(0), angle, weapon.getShip().getVelocity());
                float shotgunSpeed = (float)Math.random() * 0.35f + 0.75f;
                proj.getVelocity().scale(shotgunSpeed);
            }
        }
    }
}




