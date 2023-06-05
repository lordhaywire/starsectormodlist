package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

/**
 * UNUSED
 */
public class ocua_micromissile_effect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

        private static final float arc = 5;
        private static float pelts = 2;
        private float usedBarrel = 0;
        private float popBarrel = 0;
        
        private boolean init = false;
    
	public static int MAX_S_SHOTS = 2;
	public static int MAX_M_SHOTS = 2;
	public static int MAX_L_SHOTS = 2;
	
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
        
        String gun_id = "ocua_shi_micro";
        if(weapon.getId().contains("ocua_shizuka")) {
            pelts = MAX_S_SHOTS;
            
            if (usedBarrel == 0) { popBarrel = 0; usedBarrel = 1; }
            else { popBarrel = 1; usedBarrel = 0; }
        }
        if(weapon.getId().contains("ocua_shizukesa")) {
            pelts = MAX_M_SHOTS;
            
            if (usedBarrel == 0) { popBarrel = 0; usedBarrel = 1; } 
            else if (usedBarrel == 1) { popBarrel = 1; usedBarrel = 2; } 
            else if (usedBarrel == 2) { popBarrel = 2; usedBarrel = 3; }
            else { popBarrel = 3; usedBarrel = 0; }
        }
        if(weapon.getId().contains("ocua_shimakaze")) {
            pelts = MAX_L_SHOTS;
            
            if (usedBarrel == 0) { popBarrel = 0; usedBarrel = 1; } 
            else if (usedBarrel == 1) { popBarrel = 1; usedBarrel = 2; } 
            else if (usedBarrel == 2) { popBarrel = 2; usedBarrel = 3; }
            else if (usedBarrel == 3) { popBarrel = 3; usedBarrel = 4; } 
            else if (usedBarrel == 4) { popBarrel = 4; usedBarrel = 5; }
            else if (usedBarrel == 5) { popBarrel = 5; usedBarrel = 6; }
            else if (usedBarrel == 6) { popBarrel = 6; usedBarrel = 7; } 
            else if (usedBarrel == 7) { popBarrel = 7; usedBarrel = 8; }
            else { popBarrel = 8; usedBarrel = 0; }
        }
        
        for (int f = 1; f <= pelts; f++){
            if (weapon.isFiring()) {
                float offset = ((float)Math.random() * arc) - (arc * 0.5f);
                float angle = weapon.getCurrAngle() + offset;
                DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile 
                    (weapon.getShip(), weapon, gun_id, weapon.getFirePoint((int) popBarrel), angle, weapon.getShip().getVelocity());
                float spreadSpeed = (float)Math.random() * 0.25f + 0.85f;
                proj.getVelocity().scale(spreadSpeed);
            }
        }
    }
}




