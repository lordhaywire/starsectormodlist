package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class ocua_blita_hit_effect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean Hit, ApplyDamageResultAPI damageResult,
            CombatEngineAPI engine) {
        if (target == null || point == null) {
            return;
        }
        
        if (target instanceof ShipAPI && ((ShipAPI) target).isFighter()){
            float emp = projectile.getEmpAmount();
            float dam = projectile.getDamageAmount();
            
            Global.getCombatEngine().applyDamage(target, point, dam * 0.75f, DamageType.HIGH_EXPLOSIVE, 0f, false, false,
                projectile.getSource());
            
            if ((float) Math.random() > 0.75f && (((ShipAPI) target).isFighter())) {
                engine.spawnEmpArc(projectile.getSource(), point, target, target,
                            DamageType.ENERGY, dam * 0.5f, emp * 2f,
                            100000f, // max range 
                            "tachyon_lance_emp_impact",
                            20f, // thickness
                            new Color(25,100,155,255),
                            new Color(255,255,255,255) );
            }
        }
        if (target instanceof MissileAPI){
            float dam = projectile.getDamageAmount();
            
            Global.getCombatEngine().applyDamage(target, point, dam * 1.0f, DamageType.ENERGY, 0f, false, false,
                projectile.getSource());
        }
    }
}
