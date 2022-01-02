package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import java.util.Collection;

public class SCY_armorChild extends BaseHullMod {
    
    private final String id = "SCY_childModule";
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        
        if(!ship.isAlive())return;
        
        if(Global.getCombatEngine().getTotalElapsedTime(false)<=2.05 && Global.getCombatEngine().getTotalElapsedTime(false)>2){
            if(ship.getParentStation()!=null && ship.getParentStation().isAlive()){

                Collection <String> mods = ship.getParentStation().getVariant().getHullMods();
                
                if(mods.contains("heavyarmor")){
//                    ship.getMutableStats().getArmorBonus().modifyFlat(id, 150);
                    float fakeArmor = ship.getHullSpec().getArmorRating()/(ship.getHullSpec().getArmorRating()+150);
                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult(id, fakeArmor);
                }
                if(mods.contains("reinforcedhull")){
//                    ship.getMutableStats().getHullBonus().modifyPercent(id, 40);
                    ship.getMutableStats().getHullDamageTakenMult().modifyMult(id, 0.72f);
                }
                if(mods.contains("SCY_lightArmor")){
//                    ship.getMutableStats().getArmorBonus().modifyMult(id,0.5f);
                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult(id, 2);
                }
                if(mods.contains("SCY_reactiveArmor")){
                    ship.getMutableStats().getHighExplosiveDamageTakenMult().modifyMult(id, 0.66f);
                    ship.getMutableStats().getEnergyDamageTakenMult().modifyPercent(id, 25);
                    ship.getMutableStats().getKineticDamageTakenMult().modifyPercent(id, 25);
                }
            }
        }
    }
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
}
