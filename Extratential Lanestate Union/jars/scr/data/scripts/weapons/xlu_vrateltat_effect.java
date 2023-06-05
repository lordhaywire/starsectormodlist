package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lwjgl.util.vector.Vector2f;

/**
 * UNUSED
 */
public class xlu_vrateltat_effect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

        private static final float arc = 30;
        private static float pelts = 1;
        private float usedBarrel = 0;
        
	public static int MAX_SHOTS = 8;
        
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
        
        String gun_id = "xlu_vrateltat_z";
        pelts = MAX_SHOTS;
        
        for (int f = 1; f <= pelts; f++){
            if (weapon.isFiring()) {
                float offset = ((float)Math.random() * arc) - (arc * 0.5f);
                float angle = weapon.getCurrAngle() + offset;
                Vector2f bulletspawn = calculateMuzzle(weapon);
                DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile 
                    (weapon.getShip(), weapon, gun_id, bulletspawn, angle, weapon.getShip().getVelocity());
                float shotgunSpeed = (float)Math.random() * 0.25f + 0.85f;
                proj.getVelocity().scale(shotgunSpeed);
            }
        }
    }
        
    private Vector2f calculateMuzzle(WeaponAPI weapon){
        float muzzle = 0f;
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
        } else if (usedBarrel == 1) {
            usedBarrel = 2;
            return Vector2f.add(weapon.getFirePoint(1),dir,new Vector2f());
        } else if (usedBarrel == 2) {
            usedBarrel = 3;
            return Vector2f.add(weapon.getFirePoint(2),dir,new Vector2f());
        } else if (usedBarrel == 3) {
            usedBarrel = 4;
            return Vector2f.add(weapon.getFirePoint(3),dir,new Vector2f());
        } else {
            usedBarrel = 0;
            return Vector2f.add(weapon.getFirePoint(4),dir,new Vector2f());
        }
    }
}




