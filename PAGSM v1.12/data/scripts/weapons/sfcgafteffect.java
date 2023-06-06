package data.scripts.weapons;

import java.awt.Color;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.impl.combat.RiftTrailEffect;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.loading.MissileSpecAPI;

/**
 * IMPORTANT: will be multiple instances of this, one for the the OnFire (per weapon) and one for the OnHit (per torpedo) effects.
 *
 * (Well, no data members, so not *that* important.)
 */
public class sfcgafteffect implements OnFireEffectPlugin, OnHitEffectPlugin {

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        Color color = RiftCascadeEffect.STANDARD_RIFT_COLOR;
        Object o = projectile.getWeapon().getSpec().getProjectileSpec();
        if (o instanceof MissileSpecAPI) {
            MissileSpecAPI spec = (MissileSpecAPI) o;
            color = spec.getExplosionColor();
        }

        NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(color, 40f);
        p.fadeOut = 2f;
        p.hitGlowSizeMult = 1f;
        // want a red rift, but still blue for subtracting from the red clouds
        // or not - actually looks better with the red being inverted and subtracted
        // despite this not matching the trail
        //p.invertForDarkening = NSProjEffect.STANDARD_RIFT_COLOR;
        RiftCascadeMineExplosion.spawnStandardRift(projectile, p);

        Vector2f vel = new Vector2f();
        if (target != null) vel.set(target.getVelocity());
        Global.getSoundPlayer().playSound("sfcgaft_boom", 1f, 1f, point, vel);

        {float dam = projectile.getDamageAmount();

            engine.applyDamage(target, point, (dam * 4f), DamageType.HIGH_EXPLOSIVE, 0, false, false, projectile.getSource());
            engine.addHitParticle(point, new Vector2f(), 100, 0.05f, 1f, Color.MAGENTA);
            engine.addSmoothParticle(point, new Vector2f(), 150, 0.1f, 0.50f, Color.CYAN);
            engine.addSmoothParticle(point, new Vector2f(), 150, 0.1f, 0.50f, Color.BLACK);

            //engine.spawnProjectile(null, null, "plasma", point, 0, new Vector2f(0, 0));
        }
    }

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        RiftTrailEffect trail = new RiftTrailEffect((MissileAPI) projectile, "rifttorpedo_loop");
        ((MissileAPI) projectile).setEmpResistance(1000);
        ((MissileAPI) projectile).setEccmChanceOverride(1f);
        Global.getCombatEngine().addPlugin(trail);
    }
}




