package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class xlu_thunderfist_effect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean Hit, ApplyDamageResultAPI damageResult,
            CombatEngineAPI engine) {
            float dam = projectile.getDamageAmount();
            
            Global.getCombatEngine().applyDamage(target, point, dam * 0.75f, DamageType.FRAGMENTATION, 0f, false, false,
                projectile.getSource());
            
            DamagingProjectileAPI e = engine.spawnDamagingExplosion(createExplosionSpec(), projectile.getSource(), point);
            e.addDamagedAlready(target);
            DamagingProjectileAPI ee = engine.spawnDamagingExplosion(createExplosionSpec2(), projectile.getSource(), point);
            ee.addDamagedAlready(target);
    }
    
    public DamagingExplosionSpec createExplosionSpec() {
	float damage = 400f;
	DamagingExplosionSpec spec = new DamagingExplosionSpec(
		0.1f, // duration
		75f, // radius
		35f, // coreRadius
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
	spec.setSoundSetId("devastator_explosion");
	return spec;		
    }
    public DamagingExplosionSpec createExplosionSpec2() {
	float damage = 300f;
	DamagingExplosionSpec spec = new DamagingExplosionSpec(
		0.1f, // duration
		75f, // radius
		35f, // coreRadius
		damage, // maxDamage
		damage / 2f, // minDamage
		CollisionClass.PROJECTILE_FF, // collisionClass
		CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
		3f, // particleSizeMin
		3f, // particleSizeRange
		0.5f, // particleDuration
		25, // particleCount
		new Color(255,255,255,255), // particleColor
		new Color(255,100,100,175)  // explosionColor
	);
	spec.setDamageType(DamageType.FRAGMENTATION);
	spec.setUseDetailedExplosion(false);
	spec.setSoundSetId("");
	return spec;		
    }
}
