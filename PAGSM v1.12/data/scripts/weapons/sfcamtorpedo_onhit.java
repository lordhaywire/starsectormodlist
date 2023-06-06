package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class sfcamtorpedo_onhit implements OnHitEffectPlugin {

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
                      Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit) {

            float dam = projectile.getDamageAmount();

            engine.applyDamage(target, point, (dam * 5f), DamageType.HIGH_EXPLOSIVE, 0, false, false, projectile.getSource());
            engine.addHitParticle(point, new Vector2f(), 100, 0.05f, 1f, Color.RED);
            engine.addSmoothParticle(point, new Vector2f(), 150, 0.1f, 0.50f, Color.ORANGE);
            engine.addSmoothParticle(point, new Vector2f(), 150, 0.1f, 0.50f, Color.WHITE);

            //engine.spawnProjectile(null, null, "plasma", point, 0, new Vector2f(0, 0));
        }
    }
}