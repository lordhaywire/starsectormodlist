package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

/**
 * UNUSED
 */
public class xlu_shotcannon_effect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

        private static float arc = 10;
        private static float pelts = 1;
        private float usedBarrel = 0;
        private float popBarrel = 0;
        
        private boolean init = false;
    
	public static int MAX_S_SHOTS = 8;
	public static int MAX_M_SHOTS = 10;
	public static int MAX_L_SHOTS = 30;
        
	public static int MAX_S_ARC = 15;
	public static int MAX_M_ARC = 20;
	public static int MAX_L_ARC = 25;
	
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
        
        String gun_id = "xlu_shotcannon_z_s";
        if(weapon.getId().contains("xlu_shotcannon_s")) {
            pelts = MAX_S_SHOTS;
            arc = MAX_S_ARC;
            gun_id = "xlu_shotcannon_z_s";
        }
        if(weapon.getId().contains("xlu_shotcannon_m")) {
            pelts = MAX_M_SHOTS;
            arc = MAX_M_ARC;
            gun_id = "xlu_shotcannon_z_m";
            if (usedBarrel == 0) {
                popBarrel = 0;
                usedBarrel = 1;
            } else {
                popBarrel = 1;
                usedBarrel = 0;
            }
        }
        if(weapon.getId().contains("xlu_shotcannon_l")) {
            pelts = MAX_L_SHOTS;
            arc = MAX_L_ARC;
            gun_id = "xlu_shotcannon_z_l";
        }
        
        for (int f = 1; f <= pelts; f++){
            if (weapon.isFiring()) {
                float offset = ((float)Math.random() * arc) - (arc * 0.5f);
                float angle = weapon.getCurrAngle() + offset;
                DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile 
                    (weapon.getShip(), weapon, gun_id, weapon.getFirePoint((int) popBarrel), angle, weapon.getShip().getVelocity());
                float shotgunSpeed = (float)Math.random() * 0.25f + 0.85f;
                proj.getVelocity().scale(shotgunSpeed);
            }
        }
    }
}




