package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

/**
 * UNUSED
 */
public class ocua_manndie_effect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

        private float usedBarrel = 0;
        private float popBarrel = 0;
        private float chargeBarrel = 0;
        private static final float arc = 5;
        
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
        chargeBarrel++;
        
        String gun_id = "ocua_mi_manndie_z";
        if(weapon.getId().contains("ocua_mi_manndie")) {
            if (usedBarrel == 0) { popBarrel = 2; usedBarrel = 1; }
            else if (usedBarrel == 1) { popBarrel = 0; usedBarrel = 2; }
            else { popBarrel = 1; usedBarrel = 0; }
        }
        
        float offset = ((float)Math.random() * arc) - (arc * 0.5f);
        float recset = ((float)Math.random() * weapon.getCurrSpread() - (weapon.getCurrSpread() * 0.5f));
        float angle = weapon.getCurrAngle();
        if (weapon.isFiring() && chargeBarrel != 6) {
                angle = angle + recset;
            DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile 
                    (weapon.getShip(), weapon, gun_id, weapon.getFirePoint((int) usedBarrel), angle, weapon.getShip().getVelocity());
            DamagingProjectileAPI proj1 = (DamagingProjectileAPI) engine.spawnProjectile 
                    (weapon.getShip(), weapon, gun_id, weapon.getFirePoint((int) popBarrel), angle, weapon.getShip().getVelocity());
        } else if (chargeBarrel == 6){
            gun_id = "ocua_mi_manndie_za";
            for (int f = 1; f <= 2; f++){
                angle = angle + offset;
                DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile 
                    (weapon.getShip(), weapon, gun_id, weapon.getFirePoint(0), angle, weapon.getShip().getVelocity());
                DamagingProjectileAPI proj1 = (DamagingProjectileAPI) engine.spawnProjectile 
                    (weapon.getShip(), weapon, gun_id, weapon.getFirePoint(1), angle, weapon.getShip().getVelocity());
                DamagingProjectileAPI proj2 = (DamagingProjectileAPI) engine.spawnProjectile 
                    (weapon.getShip(), weapon, gun_id, weapon.getFirePoint(2), angle, weapon.getShip().getVelocity());
            }
            chargeBarrel = 0;
        }
    }
}




