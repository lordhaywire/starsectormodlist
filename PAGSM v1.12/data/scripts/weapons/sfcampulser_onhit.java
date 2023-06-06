package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class sfcampulser_onhit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit) {
            engine.applyDamage(target, point, 45, DamageType.HIGH_EXPLOSIVE, 0, false, false, projectile.getSource());
            engine.addHitParticle(
                    point,
                    new Vector2f(),
                    100,
                    0.25f,
                    1f,
                    Color.white);
            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    150,
                    0.50f,
                    0.50f,
                    Color.red);
            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    150,
                    0.50f,
                    0.50f,
                    Color.orange);
        }
    }
}

