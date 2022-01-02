package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class TADA_plasma_hitEffect implements OnHitEffectPlugin {
    
    private final Color COLOR = new Color(200,75,50);
    
    private final SpriteAPI impactFlame = Global.getSettings().getSprite("fx", "TADA_plasma_splash_B");
    private final SpriteAPI impactPuff = Global.getSettings().getSprite("fx", "TADA_plasma_splash_C");
    private final SpriteAPI impactBurst = Global.getSettings().getSprite("fx", "TADA_plasma_splash_A");
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
                
        if(MagicRender.screenCheck(0.25f, point)){
//            explosion
            if(Math.random()>0.8){
                engine.spawnExplosion(point, target.getVelocity(), COLOR, MathUtils.getRandomNumberInRange(150, 250), MathUtils.getRandomNumberInRange(0.4f, 0.8f));
            }
            
            //burst
            if(Math.random()>0.8){
                float dir = projectile.getFacing()+MathUtils.getRandomNumberInRange(90, 270);
                
                MagicRender.battlespace(
                        impactBurst,
                        new Vector2f(point),
                        MathUtils.getPoint(new Vector2f(), MathUtils.getRandomNumberInRange(200, 300), dir),
                        new Vector2f(6,48),
                        new Vector2f(64,-16),
                        dir-90,
                        0,
                        Color.WHITE,
                        true,
                        0f,
                        0.1f,
                        MathUtils.getRandomNumberInRange(0.2f, 0.4f)
                );
            }
            
            //impact
            if(Math.random()>0.8){
                float dir2 = projectile.getFacing()+MathUtils.getRandomNumberInRange(170, 190);
                Vector2f vel = MathUtils.getPoint(new Vector2f(), MathUtils.getRandomNumberInRange(5, 10), dir2);
                Vector2f size = new Vector2f(24,24);
                float growth = MathUtils.getRandomNumberInRange(3, 6);
                float spin = MathUtils.getRandomNumberInRange(-30, 30);
                
                MagicRender.battlespace(
                        impactFlame,
                        new Vector2f(point),
                        vel,
                        size,
                        (Vector2f) new Vector2f(size).scale(growth),
                        dir2-MathUtils.getRandomNumberInRange(-30, 30),
                        spin,
                        Color.WHITE,
                        true,
                        0,
                        growth/20,
                        growth/10
                );
                
                MagicRender.battlespace(
                        impactPuff,
                        new Vector2f(point),
                        vel,
                        size,
                        (Vector2f) new Vector2f(size).scale(growth),
                        dir2-MathUtils.getRandomNumberInRange(-30, 30),
                        spin,
                        //new Color(255,255,255,64),
                        //false,
                        Color.WHITE,
                        true,
                        growth/10,
                        0,
                        growth/10
                );
            }
        }
    }
}
