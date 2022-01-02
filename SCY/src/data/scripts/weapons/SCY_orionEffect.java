//add extra damage to the ORION artillery's shell depending on the current split stage
package data.scripts.weapons;

//import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
//import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.scripts.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SCY_orionEffect implements OnHitEffectPlugin {
    
    private float damage = 0;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {        
                
//        damage=1000-projectile.getBaseDamageAmount();
//        
//        engine.applyDamage(
//                target,
//                point,            
//                damage,
//                DamageType.KINETIC,
//                0f,
//                false,
//                projectile.isFading(),
//                projectile.getSource()
//        );
//        
//        if(damage>100 && shieldHit){
//            engine.addHitParticle(
//                    point,
//                    new Vector2f(),
//                    100+200*damage/500,
//                    1,
//                    0.1f+0.25f*damage/500,
//                    Color.CYAN
//            );
//            Global.getSoundPlayer().playSound("hit_shield_solid_energy", 1, damage/500, point, target.getVelocity());
//        }
        
        if(MagicRender.screenCheck(0.1f, point)){
            for(int i=0; i<damage/50; i++){
                engine.addHitParticle(
                        point,
                        MathUtils.getPoint(new Vector2f(), i*40, projectile.getFacing()-1+(float)(Math.random()*2)),
                        4+4*(20-i),
                        0.5f,
                        0.2f+(float)(Math.random()/4),
                        new Color(150-6*(20-i), 100, 200)
                );
            }

            for(int i=0; i<8; i++){            
                engine.addHitParticle(
                        point,
                        MathUtils.getRandomPointInCone(new Vector2f(), 100, projectile.getFacing()+135, projectile.getFacing()+225),
                        5+5*(float)Math.random(),
                        1f,
                        1f+(float)Math.random(),
                        new Color(250, 100, 50, 150)
                );            
            }
        }
        
        //debug
//        engine.addFloatingText(point, ""+damage, 60, Color.yellow, projectile.getSource(), 0, 0);
//        engine.addFloatingText(MathUtils.getPoint(projectile.getSource().getLocation(),200,0), ""+projectile.getDamageAmount(), 60, Color.red, projectile.getSource(), 0, 0);
//        engine.addFloatingText(MathUtils.getPoint(projectile.getSource().getLocation(),200,-20), ""+projectile.getBaseDamageAmount(), 60, Color.blue, projectile.getSource(), 0, 0);
    }
}
