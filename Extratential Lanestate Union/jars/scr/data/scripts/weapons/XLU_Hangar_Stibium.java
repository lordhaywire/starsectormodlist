package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.ids.XLU_HullMods;

public class XLU_Hangar_Stibium implements EveryFrameWeaponEffectPlugin {

    private AnimationAPI Hangar;
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon)
    {
        Hangar=weapon.getAnimation();
        ShipAPI ship = weapon.getShip();
        
        if (ship.getVariant().hasHullMod(XLU_HullMods.XLU_TRUSTYBAY_SLOT_1)) {
            Hangar.setFrame(1);
        }
        else {
            Hangar.setFrame(0);
        }
    }
}
