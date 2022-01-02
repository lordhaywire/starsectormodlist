package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import data.scripts.util.MagicTargeting;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class TADA_pilumAI implements MissileAIPlugin, GuidedMissileAI {
    
    private final CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    //data
    private final float MAX_SPEED;
    private final float DAMPING = 0.1f;
    //delay between target actualisation
    private boolean launch=true;
    private float eccm=2f, timer=0, check=0.25f, offset=0;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public TADA_pilumAI(MissileAPI missile, ShipAPI launchingShip) {
        this.engine = Global.getCombatEngine();
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            eccm=0.25f;
        }
        offset=(float)(MathUtils.FPI*Math.random()*2);
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        //skip the AI if the game is paused or the missile is fading
        if (engine.isPaused() || missile.isFading() || missile.isFizzling()) return;
        
        //assigning a target if there is none or it got destroyed
        if (target == null
                || (target instanceof ShipAPI && !((ShipAPI)target).isAlive())
                || target.getOwner()==missile.getOwner()
                || !engine.isEntityInPlay(target)
                ){
            setTarget(MagicTargeting.pickMissileTarget(missile,
                    MagicTargeting.targetSeeking.NO_RANDOM,
                    (int)missile.getWeapon().getRange(),
                    360,
                    0,
                    1,
                    3,
                    4,
                    5
            ));
            return;
        }
       
        timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer=0;
            
            float dist=MathUtils.getDistanceSquared(missile, target);
                    
            check = Math.min(
                    0.25f,
                    Math.max(
                            0.05f,
                            1.5f*dist/6000000)
            );
            
            lead = AIUtils.getBestInterceptPoint(
                    missile.getLocation(),
                    MAX_SPEED*eccm,
                    target.getLocation(),
                    target.getVelocity()
            );
            if (lead == null ) {
                lead = target.getLocation(); 
            }
        }
        
        //best angle for interception
        float correctAngle = VectorUtils.getAngle(
                        missile.getLocation(),
                        lead
                );
        
        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), correctAngle);
        
        //waving
        aimAngle+=15*FastTrig.sin(missile.getFlightTime()+offset);
        
        missile.giveCommand(ShipCommand.ACCELERATE);            
        if (aimAngle < 0) {
            missile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            missile.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * DAMPING) {
            missile.setAngularVelocity(aimAngle / DAMPING);
        }
    }
    
    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
}
