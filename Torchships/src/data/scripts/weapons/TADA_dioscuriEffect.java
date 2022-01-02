package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.util.MagicAnim;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class TADA_dioscuriEffect implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false, hidden=false, fired=false;
    private SpriteAPI barrel;
    private AnimationAPI theAnim;
    private int frame,maxFrame;
    private float barrelHeight=0, barrelWidth=0, rearMuzzle=0;
    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1 || hidden){return;}
        
        if(!runOnce){
            runOnce=true;
            if(weapon.getSlot().isHidden()){
                hidden=true;
            } else {
                
                theAnim=weapon.getAnimation();
                maxFrame=theAnim.getNumFrames();                
                
                barrel=weapon.getBarrelSpriteAPI();
                barrelWidth = barrel.getWidth()/2;
                if(weapon.getSlot().isTurret()){
                    barrelHeight=barrel.getHeight()/2;
                    rearMuzzle=23;
                } else {                    
                    barrelHeight=barrel.getHeight()/4;
                    rearMuzzle=14;
                }
                weapon.ensureClonedSpec();
            }
            return;
        }
        
        float reload = 1-weapon.getChargeLevel();
        
        if(reload<1){
            if(reload==0){
                fired=true;
                
                //rear blast
                float angle = 180+weapon.getCurrAngle();
                Vector2f point= MathUtils.getPoint(weapon.getLocation(), rearMuzzle, angle);
                
                //smoke
                for(int i=0; i<20; i++){                    
                    Vector2f vel = MathUtils.getRandomPointInCone(new Vector2f(), 50, angle-10, angle+10);
                    vel.scale((float)Math.random());
                    Vector2f.add(vel, weapon.getShip().getVelocity(), vel);
                    float grey = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
                    engine.addSmokeParticle(
                            MathUtils.getRandomPointInCircle(point, 5),
                            vel,
                            MathUtils.getRandomNumberInRange(5, 30),
                            MathUtils.getRandomNumberInRange(0.25f, 0.75f),
                            MathUtils.getRandomNumberInRange(0.25f, 1f),
                            new Color(grey,grey,grey,MathUtils.getRandomNumberInRange(0.25f, 0.75f))
                    );
                }
                //debris
                for(int i=0; i<10; i++){                    
                    Vector2f vel = MathUtils.getRandomPointInCone(new Vector2f(), 250, angle-20, angle+20);
                    Vector2f.add(vel, weapon.getShip().getVelocity(), vel);
                    engine.addHitParticle(
                            point,
                            vel,
                            MathUtils.getRandomNumberInRange(3, 6),
                            1,
                            MathUtils.getRandomNumberInRange(0.05f, 0.25f),
                            new Color(255,125,50)
                    );
                }
                //flash
                engine.addHitParticle(
                        point,
                        weapon.getShip().getVelocity(),
                        120,
                        0.5f,
                        0.5f,
                        new Color(255,20,10)
                );
                engine.addHitParticle(
                        point,
                        weapon.getShip().getVelocity(),
                        100,
                        0.75f,
                        0.15f,
                        new Color(255,150,50)
                );
                engine.addHitParticle(
                        point,
                        weapon.getShip().getVelocity(),
                        75,
                        1,
                        0.05f,
                        new Color(255,200,150)
                );
            }
            
            //barrels anim on first half of the cooldown
            frame=Math.round(-0.4f + reload*maxFrame*2);
            if(frame>=maxFrame)frame=0;
            theAnim.setFrame(frame);
            
            //loader anim on second half of the cooldown
            float centerY = barrelHeight - 4*MagicAnim.smoothNormalizeRange(reload, 0.35f, 0.60f) + 4*MagicAnim.smoothNormalizeRange(reload, 0.7f, 0.95f);
            float centerX = barrelWidth -2*MagicAnim.smoothNormalizeRange(reload, 0.35f, 0.45f) +2*MagicAnim.smoothNormalizeRange(reload, 0.85f, 0.95f);

//            float centerX = barrelWidth;

            barrel.setCenter(centerX, centerY);
            
        } else if(fired){
            fired=false;
            frame=0;
            theAnim.setFrame(0);
            barrel.setCenter(barrelWidth, barrelHeight);
        }
    }
}