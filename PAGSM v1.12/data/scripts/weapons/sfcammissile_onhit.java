package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class sfcammissile_onhit implements OnHitEffectPlugin {

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
                      Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit) {

            float dam = projectile.getDamageAmount();

            engine.applyDamage(target, point, (dam * 1.5f), DamageType.ENERGY, 0, false, false, projectile.getSource());
            engine.addHitParticle(point, new Vector2f(), 50, 0.0125f, 0.1f, Color.white);
            engine.addSmoothParticle(point, new Vector2f(), 100, 0.025f, 0.05f, Color.PINK);
            engine.addSmoothParticle(point, new Vector2f(), 100, 0.025f, 0.05f, Color.RED);

            //engine.spawnProjectile(null, null, "plasma", point, 0, new Vector2f(0, 0));
        }
    }
}