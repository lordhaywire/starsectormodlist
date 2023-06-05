package data.scripts.weapons;

import com.fs.starfarer.api.combat.CollisionClass;
import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class xlu_stratuscannon_hit_effect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult,
            CombatEngineAPI engine) {
            DamagingProjectileAPI e = engine.spawnDamagingExplosion(createExplosionSpec(), projectile.getSource(), point);
            e.addDamagedAlready(target);
		if (!shieldHit && target instanceof ShipAPI) {
			
                    float emp = projectile.getEmpAmount();
                    float dam = projectile.getDamageAmount();
                    for (int i = 0; i < 2; i++) {
                        if ((float) Math.random() > 0.65f) {
			engine.spawnEmpArc(projectile.getSource(), point, target, target,
							   DamageType.ENERGY, 
							   dam / 10,
							   emp, // emp 
							   100000f, // max range 
							   "tachyon_lance_emp_impact",
							   20f, // thickness
							   new Color(25,100,155,255),
							   new Color(255,255,255,255)
							   );
                        }
                    }
		}
	}
    
    public DamagingExplosionSpec createExplosionSpec() {
	float damage = 750f;
	DamagingExplosionSpec spec = new DamagingExplosionSpec(
		0.1f, // duration
		75f, // radius
		25f, // coreRadius
		damage, // maxDamage
		damage / 2f, // minDamage
		CollisionClass.PROJECTILE_FF, // collisionClass
		CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
		3f, // particleSizeMin
		3f, // particleSizeRange
		0.5f, // particleDuration
		25, // particleCount
		new Color(255,165,125,255), // particleColor
		new Color(255,165,125,125)  // explosionColor
	);
	spec.setDamageType(DamageType.HIGH_EXPLOSIVE);
	spec.setUseDetailedExplosion(false);
	spec.setSoundSetId("");
	return spec;		
    }
}
