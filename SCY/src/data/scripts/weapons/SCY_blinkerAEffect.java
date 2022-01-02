package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import org.lazywizard.lazylib.FastTrig;

public class SCY_blinkerAEffect implements EveryFrameWeaponEffectPlugin {    
    private boolean lightOn = true, runOnce=false;
    private float range = 0;
    private final float FREQUENCY =2;
    private final float OFFSET =3;
    private final float AMPLITUDE = 5;
    private final IntervalUtil timer = new IntervalUtil(0.75f, 1f);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        
        if (engine.isPaused() || !lightOn) {return;}
        
        if (!runOnce){
            runOnce=true;
            if(weapon.getShip().getOriginalOwner()==-1){
                weapon.getSprite().setColor(Color.BLACK);
                lightOn=false;
                return;
            }
            timer.randomize();
        }
        
        float blink = Math.min(
                1, //upper clamp
                Math.max(
                        0, //lower clamp
                        (float)(-AMPLITUDE*FastTrig.cos(engine.getTotalElapsedTime(false)*FREQUENCY)-OFFSET)-(float)(AMPLITUDE/2*FastTrig.cos(engine.getTotalElapsedTime(false)*8*FREQUENCY)+OFFSET)
//                        OFFSET-(float)Math.cos(engine.getTotalElapsedTime(false)*FREQUENCY)*AMPLITUDE
                )
        );
        
        weapon.getSprite().setColor(new Color (blink,blink,1,blink));
        
        timer.advance(amount);
        if (timer.intervalElapsed()){
            if (weapon.getShip() != null && weapon.getShip().isHulk()){
                if (range>=1){
                    weapon.getSprite().setAdditiveBlend();
                    weapon.getSprite().setColor(Color.BLACK);
                    weapon.getSprite().setAlphaMult(0);
                    lightOn = false;
                }
                range++;
            }
        }
    }    
}