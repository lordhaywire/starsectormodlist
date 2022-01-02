//By Tartiflette.
package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicRender;
import data.scripts.weapons.TADA_spikeThrowerEffect;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class TADA_spikeAI implements MissileAIPlugin, GuidedMissileAI {
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private CombatEntityAPI anchor;
    
    private Vector2f offset = new Vector2f();
    private float angle = 0;
    
    private final IntervalUtil timer = new IntervalUtil(0.1f,0.2f);
    
    private boolean runOnce = false;
    
    private Vector2f projected = new Vector2f();
    private Vector2f previousLoc = new Vector2f();
    private final IntervalUtil accelerationCheck = new IntervalUtil(0.1f,0.1f);
    private boolean tearOff=false;
    
    private final float TEAR_OFF_THRESHOLD = 35f;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public TADA_spikeAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused()) {return;}
                
        if(!runOnce){
            runOnce=true;
            List<CombatEntityAPI>list = ((TADA_spikeThrowerEffect) missile.getWeapon().getEffectPlugin()).getHITS();
            
            if(list.isEmpty()){
                missile.flameOut();
                return;
            }
            
            //get the anchor
            float range=1000000;
            for(CombatEntityAPI e : list){
                if(MathUtils.getDistanceSquared(missile, e)<range){
                    target=e;
                    anchor=e; //some scripts change the target so I can't really use that for the anchor
                }
            }
            
            if(anchor==null){
                return;
            }
            
            //put the anchor in the weapon's detonation list
            ((TADA_spikeThrowerEffect) missile.getWeapon().getEffectPlugin()).setDetonation(anchor);
            
            projected=new Vector2f(anchor.getVelocity());
            projected.scale(0.1f);
            Vector2f.add(missile.getLocation(), projected, projected);
            previousLoc=new Vector2f(missile.getLocation());
            
            offset=new Vector2f(missile.getLocation());
            Vector2f.sub(offset, new Vector2f(anchor.getLocation()), offset);
            VectorUtils.rotate(offset, -anchor.getFacing(), offset);
            
            angle = MathUtils.getShortestRotation(anchor.getFacing(), missile.getFacing());
            return;
        } else {
            if(anchor==null || ((TADA_spikeThrowerEffect) missile.getWeapon().getEffectPlugin()).getDetonation(anchor)){
                missile.setCollisionClass(CollisionClass.MISSILE_FF);
                return;
            }
        }
        
        if(tearOff){
            return;
        }
        
        //acceleration check for tear off
        accelerationCheck.advance(amount);
        float elapsed = accelerationCheck.getElapsed();
        if(accelerationCheck.intervalElapsed()){
            //acceleration tear off
            
            boolean acceleration = !MathUtils.isWithinRange(missile.getLocation(),projected,TEAR_OFF_THRESHOLD*elapsed);

            //debug
//            float dist = MathUtils.getDistance(missile.getLocation(),projected);
//            engine.addHitParticle(projected, new Vector2f(), 20, 2, 0.2f, Color.GREEN);
//            engine.addFloatingText(missile.getLocation(), ""+(float)(Math.round(dist*10000))/10000,12, Color.green, missile,0.1f,0.1f);
            
            boolean fooled = (target !=anchor);
            boolean escaped = (anchor.getCollisionClass()==CollisionClass.NONE);
            boolean emped = missile.isFizzling();
            boolean outlasted = missile.isFading();
            
            if( 
                    acceleration || 
                    fooled ||
                    escaped ||
                    emped ||
                    outlasted
                    ){
                tearOff=true;
                missile.setArmingTime(missile.getElapsed()+0.25f);
                missile.setCollisionClass(CollisionClass.MISSILE_FF);
                return;
            }
            
            Vector2f.sub(new Vector2f(missile.getLocation()),previousLoc, projected);
            Vector2f.add(new Vector2f(missile.getLocation()),projected,projected);
            previousLoc= new Vector2f(missile.getLocation());
        } 
        
        //death priority triggers
        if (
                anchor.getHullLevel()<=0 ||
                !engine.isEntityInPlay(anchor)
                ){
//            engine.applyDamage(
//                    missile,
//                    missile.getLocation(),
//                    1000,
//                    DamageType.FRAGMENTATION,
//                    0,
//                    true,
//                    false,
//                    target
//            );
            engine.removeEntity(missile);
            return;
        }
              
        //stuck effect
        Vector2f loc=new Vector2f(offset);
        VectorUtils.rotate(offset, anchor.getFacing(), loc);
        Vector2f.add(loc, anchor.getLocation(), loc);
        missile.getLocation().set(loc);
        missile.setFacing(anchor.getFacing()+angle);
        
        //detonation
        if(missile.getElapsed()>4){            
            missile.setCollisionClass(CollisionClass.MISSILE_FF);
            ((TADA_spikeThrowerEffect) missile.getWeapon().getEffectPlugin()).applyDetonation(anchor);
            return;
        }
        
        //visual effect   
        if(MagicRender.screenCheck(0.25f, loc)){
            timer.advance(amount);
            if(timer.intervalElapsed()){
                //smoke
                Vector2f vel = new Vector2f(anchor.getVelocity());
                vel.scale(0.8f);
                if(Math.random()>0.75){
                    float grey = 0.25f+(missile.getElapsed()/6)*(float)Math.random();
                    engine.addSmokeParticle(
                            missile.getLocation(),
                            new Vector2f(vel),
                            10+20*(float)Math.random(),
                            0.01f+0.05f*(float)Math.random(),
                            0.5f+(float)Math.random(),
                            new Color(grey,grey,grey)
                    );
                }
                //sparkles
                Vector2f.add(vel, MathUtils.getRandomPointInCone(new Vector2f(), (6-missile.getElapsed())*15, missile.getFacing()+160, missile.getFacing()+200), vel);
                engine.addHitParticle(
                        loc,
                        vel,
                        3+3*(float)Math.random(),
                        1,
                        0.1f+0.5f*(float)Math.random(),
                        new Color(0.75f+0.25f*(float)Math.random(),0.25f+0.5f*(float)Math.random(),0.25f*(float)Math.random())
                );    
            }
        }        
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        //can't just detach it with flares
//        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
}