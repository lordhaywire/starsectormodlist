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

public class TADA_spikeSecondaryEffect implements OnHitEffectPlugin {    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
//        engine.applyDamage(target, point, projectile.getWeapon().getDamage().getDamage(), DamageType.FRAGMENTATION, 0, true, false, projectile.getSource());
        //AOE frag splash damage 
        DamagingExplosionSpec boom = new DamagingExplosionSpec(
                    0.1f,
                    150,
                    50,
                    projectile.getDamageAmount(),
                    projectile.getDamageAmount()/2,
                    CollisionClass.MISSILE_FF,
                    CollisionClass.MISSILE_FF,
                    2,
                    5,
                    0.5f,
                    15,
                    new Color(225,100,0,128),
                    new Color(20,10,2,1)
            );
        boom.setDamageType(DamageType.FRAGMENTATION);
        boom.setShowGraphic(true);
        boom.setUseDetailedExplosion(false);
        engine.spawnDamagingExplosion(boom, projectile.getSource(), point);
    }
}