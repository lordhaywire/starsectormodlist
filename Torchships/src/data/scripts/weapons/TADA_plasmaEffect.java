package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class TADA_plasmaEffect implements EveryFrameWeaponEffectPlugin {
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(weapon.getChargeLevel()>0){
            Global.getSoundPlayer().playLoop("TADA_plasma_loop", weapon.getShip(), 1+(1-weapon.getChargeLevel()), 1, weapon.getLocation(), weapon.getShip().getVelocity());
        }
    }
}
