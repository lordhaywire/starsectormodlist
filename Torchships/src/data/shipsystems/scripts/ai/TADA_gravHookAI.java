/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Potter
 */
public class TADA_gravHookAI implements ShipSystemAIScript{
    
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipSystemAPI system;

    private IntervalUtil tracker = new IntervalUtil(0.5f, 2f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
            this.ship = ship;
            this.flags = flags;
            this.engine = engine;
            this.system = system;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if(engine.isPaused()) return;
        
        if(!system.isActive()){
            //activation AI
            tracker.advance(amount);
            if(tracker.intervalElapsed()){
                
                if(target==null || !target.isAlive()) return;
                if(target.isFighter() || target.isDrone()) return;
                if(!AIUtils.canUseSystemThisFrame(ship)) return;
                if(ship.getFluxTracker().getFluxLevel()>0.33f) return;
                if(MathUtils.getDistanceSquared(ship, target)<250000) return;
                if(
                        flags.hasFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET)
                        ||
                        flags.hasFlag(ShipwideAIFlags.AIFlags.PURSUING)
                        ||
                        flags.hasFlag(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN)
                        ){
                    
                    ship.useSystem();
                }
            }
        }
    }
}
