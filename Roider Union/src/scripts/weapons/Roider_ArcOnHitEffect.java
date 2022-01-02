package scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

// Modified from SPP_LightningGunOnHitEffect
    // Credit: DarkRevenant

public class Roider_ArcOnHitEffect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile,
                CombatEntityAPI target, Vector2f point,
                boolean shieldHit, ApplyDamageResultAPI damageResult,
                CombatEngineAPI engine) {

        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(point);
            float pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.4f;

            pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
            boolean piercedShield = hitShield && (float) Math.random() < pierceChance;
//                piercedShield = true;

            float distance = MathUtils.getDistance(target.getLocation(), point);
            float range = projectile.getWeapon().getRange() * 2f;//
            int brightness = (int) (255f * Math.max(Math.min((range - distance) / distance, 0f), 1f));

            if ((!hitShield && (float) Math.random() < 0.75f) || piercedShield) {
                float emp = projectile.getWeapon().getDamage().getFluxComponent() * 0.2f;
//                        float dam = projectile.getWeapon().getDamage().getDamage() * 0.05f;
                engine.spawnEmpArcPierceShields(
                           projectile.getSource(), projectile.getLocation(), projectile.getDamageTarget(), projectile.getDamageTarget(),
                           DamageType.ENERGY,
                           0f, // damage
                           emp, // emp
                           100000f, // max range
                           "tachyon_lance_emp_impact",
                           20f,
                           new Color(100, 125, 200, brightness),
                           new Color(240, 250, 255, brightness)
                           );
            }
        }
    }
}
