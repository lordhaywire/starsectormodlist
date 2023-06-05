package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class xlu_hyvel_hit_effect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean Hit, ApplyDamageResultAPI damageResult,
            CombatEngineAPI engine) {
            float dam = projectile.getDamageAmount();
            
            Global.getCombatEngine().applyDamage(target, point, dam * 0.5f, DamageType.FRAGMENTATION, 0f, false, false,
                projectile.getSource());
    }
}
