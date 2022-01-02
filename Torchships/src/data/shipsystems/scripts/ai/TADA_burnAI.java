package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class TADA_burnAI implements ShipSystemAIScript {
    
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private FluxTrackerAPI flux;
    private final IntervalUtil checkAgain = new IntervalUtil (0.25f,0.5f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){
        
        this.ship = ship;
        this.system = system;
        this.flux = ship.getFluxTracker();
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){  
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        if (engine.isPaused() || ship.getShipAI()==null) {
            return;
        }   
        
        if (flux.isOverloadedOrVenting())return;
        
        checkAgain.advance(amount);
        
        if (checkAgain.intervalElapsed()) {        
            
            if(system.isActive() && (flux.getFluxLevel()>0)){
                ship.useSystem();
                return;
            }
            if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship) && flux.getFluxLevel()==0 && ship.getEngineController().isAccelerating()){
                ship.useSystem();
            }
        }
    }
}
