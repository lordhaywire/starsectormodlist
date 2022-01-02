//By Tartiflette

package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.util.MagicRender;
import data.scripts.util.TADA_graphicLibEffects;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class TADA_whirlwindEffect implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce = false, activation=true, SHADER=false;
    private ShipSystemAPI system;
    private ShipAPI ship;
    private SpriteAPI SYSTEM;       
    private final float RANGE=1500, PULL=200;    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine.isPaused()) {return;}
        
        if (!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system=ship.getSystem();      
            
            //distortion + light effects
            SHADER = Global.getSettings().getModManager().isModEnabled("shaderLib");
            
            weapon.getAnimation().setFrame(1);
            SYSTEM=weapon.getSprite();
            SYSTEM.setColor(Color.BLACK);  
        }
        
        if(system.isActive()){
            float charge=system.getEffectLevel();
            
            if(charge<1){
                SYSTEM.setColor(new Color(1.0f,1.0f,1.0f,charge));                
            }
            
            if(charge==1){
                activation=true;
                
                Vector2f displacement = MathUtils.getPoint(new Vector2f(), PULL, 180+ship.getFacing());                    

                //VISUAL EFFECT
                if(MagicRender.screenCheck(0.5f, weapon.getLocation())){
                    for(int i=0; i<25; i++){
                        engine.addHitParticle(
                                MathUtils.getRandomPointInCone(weapon.getLocation(), RANGE, ship.getFacing()-15, ship.getFacing()+15),
                                (Vector2f)(new Vector2f(displacement)).scale((float)Math.random()),
                                5+15*(float)Math.random(),
                                1.0f,
                                0.5f+(float)Math.random(),
                                Color.pink);
                    }
                    
                    if(SHADER){                        
                        TADA_graphicLibEffects.whirlwindWave(weapon);                        
                    }
                
                    for(int u=0; u<15; u++){

                        float scaling=0.25f+0.75f*(float)Math.random();

                        Vector2f loc = MathUtils.getRandomPointInCone(weapon.getLocation(), RANGE, ship.getFacing()-7, ship.getFacing()+7);
                        Vector2f vel = new Vector2f(weapon.getLocation());
                        Vector2f.sub(loc, vel, vel);
                        vel.normalise();
                        vel.scale(-325*scaling);

                        MagicRender.battlespace(
                                Global.getSettings().getSprite("fx","sweetener"),
                                loc,                             
                                vel,
                                new Vector2f(512*scaling,24*scaling), 
                                new Vector2f(-512*scaling,-8*scaling), 
                                VectorUtils.getFacing(vel), 
                                0,
                                Color.WHITE,
                                true,
                                0f, 
                                0.25f, 
                                scaling
                        );
                    }
                }
                    
                // GAMEPLAY EFFECT

                for(CombatEntityAPI e : CombatUtils.getEntitiesWithinRange(ship.getLocation(), RANGE)){
                    if(e==ship)continue;
                    if(e instanceof ShipAPI && ((ShipAPI)e).getParentStation()!=null)continue;
                    
                    if(
                        Math.abs(MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(ship.getLocation(), e.getLocation())))<15
                            ){

                        Vector2f.add(e.getVelocity(), displacement, e.getVelocity());
                        
                        if(e instanceof ShipAPI){
                            ShipAPI s = (ShipAPI)e;
                            if(s.getEngineController().getShipEngines().size()>0){
                                for(ShipEngineAPI en : s.getEngineController().getShipEngines()){
                                    if(en.isDisabled())continue;
                                    if(Math.random()<0.2)en.disable();
                                }
                            }
                        }
                        
                        for(int i=0; i<5; i++){
                            engine.addHitParticle(
                                    MathUtils.getRandomPointInCircle(e.getLocation(), e.getCollisionRadius()),
                                    (Vector2f)(new Vector2f(displacement)).scale(1+(float)Math.random()),
                                    5+5*(float)Math.random(),
                                    1.0f,
                                    0.25f+0.25f*(float)Math.random(),
                                    Color.pink);
                        }
                        
                    }
                }
            }    
        } else if(activation){
            activation=false;
            SYSTEM.setColor(Color.BLACK);
        }
    }
}