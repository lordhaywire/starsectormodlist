//By Tartiflette

package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
//import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
//import com.fs.starfarer.api.util.IntervalUtil;
//import data.scripts.util.MagicRender;
//import java.awt.Color;
//import org.lazywizard.lazylib.MathUtils;
//import org.lwjgl.util.vector.Vector2f;

public class SCY_boarEffect implements EveryFrameWeaponEffectPlugin {
    
    private boolean rotate = true;
//    private boolean ping=false;
    private boolean runOnce=false;
//    private final IntervalUtil timer = new IntervalUtil (0.25f,0.5f);
//    private ShipSystemAPI SYSTEM;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        if (engine.isPaused() || !rotate) {return;}
        
        if(!runOnce){
            runOnce=true;
            if(weapon.getShip().getOriginalOwner()!=-1 && weapon.getShip().getOwner()!=-1){
                weapon.setCurrAngle((float)Math.random()*360);
                weapon.getShip().getMutableStats().getSightRadiusMod().modifyMult("SCY_eboar_radar", 2);
            }
//            SYSTEM=weapon.getShip().getSystem();
        }
        
        if(!weapon.getShip().isAlive()){
            rotate=false;
            return;
        }
        
        weapon.setCurrAngle(weapon.getCurrAngle()+amount*25);
        
//        if(SYSTEM.isActive()){
//            
//            timer.advance(amount);
//            if(!ping)timer.forceIntervalElapsed();
//            
//            if(timer.intervalElapsed()){
//                if(MagicRender.screenCheck(0.25f, weapon.getLocation())){
//                    if(!ping){
//                        for(int i=0; i<36; i++){
//                            engine.addSmoothParticle(
//                                    MathUtils.getPoint(weapon.getLocation(), 50, 10*i),
//                                    MathUtils.getPoint(new Vector2f(), 150, 10*i),
//                                    50,
//                                    1,
//                                    0.5f,
//                                    new Color(100,200,250,50)
//                            );
//
//                            engine.addSmoothParticle(
//                                    weapon.getLocation(),
//                                    MathUtils.getPoint(new Vector2f(), 200, 10*i),
//                                    50,
//                                    1,
//                                    1,
//                                    new Color(100,200,250,50)
//                            );
//                        }
//                    }
//                    engine.addSmoothParticle(
//                            weapon.getLocation(),
//                            weapon.getShip().getVelocity(),
//                            (float)Math.random()*25+50*SYSTEM.getEffectLevel(),
//                            1,
//                            0.2f,
//                            new Color(100,200,250,50)
//                    );
//                    weapon.beginSelectionFlash();
//                }
//            }
//            ping=true;
//        } else {
//            ping=false;
//        }
    }
}