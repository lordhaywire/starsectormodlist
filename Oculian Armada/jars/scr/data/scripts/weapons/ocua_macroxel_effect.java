package data.scripts.weapons;

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

public class ocua_macroxel_effect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean Hit, ApplyDamageResultAPI damageResult,
            CombatEngineAPI engine) {
        
            DamagingProjectileAPI e = engine.spawnDamagingExplosion(createExplosionSpec(), projectile.getSource(), point);
            e.removeDamagedAlready(target);
            //e.addDamagedAlready(target);
    }
    
    public DamagingExplosionSpec createExplosionSpec() {
	float damage = 4000f;
	DamagingExplosionSpec spec = new DamagingExplosionSpec(
		0.5f, // duration
		300f, // radius
		50f, // coreRadius
		damage, // maxDamage
		damage,// / 2f, // minDamage
		CollisionClass.PROJECTILE_FF, // collisionClass
		CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
		3f, // particleSizeMin
		3f, // particleSizeRange
		0.5f, // particleDuration
		25, // particleCount
		new Color(255,165,135,255), // particleColor
		new Color(255,75,125,255)  // explosionColor
	);
	spec.setDamageType(DamageType.FRAGMENTATION);
	spec.setUseDetailedExplosion(false);
	spec.setSoundSetId("");
	return spec;		
    }
}
