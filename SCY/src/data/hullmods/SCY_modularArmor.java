package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.HashMap;
import java.util.Map;
import com.fs.starfarer.api.combat.BaseHullMod;

public class SCY_modularArmor extends BaseHullMod {

    private final String SCY_ARMOR_MOD = "SCY_ARMOR_MOD";
    private final float SPEED_BONUS=0.25f;

    private final Map<String, Float> maxArmor = new HashMap<>();
    {
        maxArmor.put("SCY_lamiaA", 3f);
        maxArmor.put("SCY_corocottaA", 5f);
        maxArmor.put("SCY_nemeanlion", 4f);
        maxArmor.put("SCY_keto", 2f);
    }
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        if (Global.getCombatEngine().isPaused() || ship==null || ship.getOriginalOwner() == -1) {
            return;
        }
        
        if (!ship.isAlive()) {
            removeStats(ship);
            return;
        }
        
        float modules =0;
        float alive =0;
        for(ShipAPI s : ship.getChildModulesCopy()){
            modules++;
            if(s.isAlive()){
                alive++;
            }
        }
        
        if(modules!=0){
            //speed bonus applies linearly
            float speedRatio=1 - (alive / modules);
            applyStats(speedRatio, ship);
        }
    }
    
    private void removeStats(ShipAPI ship) {
        ship.getMutableStats().getMaxSpeed().unmodify(SCY_ARMOR_MOD);
        ship.getMutableStats().getAcceleration().unmodify(SCY_ARMOR_MOD);
        ship.getMutableStats().getDeceleration().unmodify(SCY_ARMOR_MOD);
        ship.getMutableStats().getMaxTurnRate().unmodify(SCY_ARMOR_MOD);
        ship.getMutableStats().getTurnAcceleration().unmodify(SCY_ARMOR_MOD);
    }

    private void applyStats(float speedRatio, ShipAPI ship) {
        ship.getMutableStats().getMaxSpeed().modifyMult(SCY_ARMOR_MOD, (1 + (speedRatio * SPEED_BONUS)));
        ship.getMutableStats().getAcceleration().modifyMult(SCY_ARMOR_MOD, (1 + (speedRatio * SPEED_BONUS)));
        ship.getMutableStats().getDeceleration().modifyMult(SCY_ARMOR_MOD, (1 + (speedRatio * SPEED_BONUS)));
        ship.getMutableStats().getMaxTurnRate().modifyMult(SCY_ARMOR_MOD, (1 + (speedRatio * SPEED_BONUS)));
        ship.getMutableStats().getTurnAcceleration().modifyMult(SCY_ARMOR_MOD, (1 + (speedRatio * SPEED_BONUS)));
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return ""+(int)100*SPEED_BONUS;
        return null;
    }
}
