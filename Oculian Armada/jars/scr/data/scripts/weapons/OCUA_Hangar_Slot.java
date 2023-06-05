package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.ids.OCUA_HullMods;

public class OCUA_Hangar_Slot implements EveryFrameWeaponEffectPlugin {

    private AnimationAPI Hangar;
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon)
    {
        Hangar=weapon.getAnimation();
        ShipAPI ship = weapon.getShip();
        
        if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_QUANTIX) ||
                ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHQU) ||
                ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CRQU) ||
                ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QU) ||
                ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QUPL) ||
                ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QUVP)) {
            Hangar.setFrame(1);
        }
        else {
            Hangar.setFrame(0);
        }
    }
}
