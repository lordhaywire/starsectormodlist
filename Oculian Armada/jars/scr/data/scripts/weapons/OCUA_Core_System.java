package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.ids.OCUA_HullMods;

public class OCUA_Core_System implements EveryFrameWeaponEffectPlugin {

    private AnimationAPI Core;
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon)
    {
        Core=weapon.getAnimation();
        ShipAPI ship = weapon.getShip();
        
        if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_CHEMICAL)) Core.setFrame(1);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_CRYSTALLINE)) Core.setFrame(2);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_PULSE)) Core.setFrame(3);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_QUANTIX)) Core.setFrame(4);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_VAPOR)) Core.setFrame(5);
        
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CH)) Core.setFrame(6);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHCR)) Core.setFrame(7);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHQU)) Core.setFrame(8);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHPL)) Core.setFrame(9);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHVP)) Core.setFrame(10);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CR)) Core.setFrame(11);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CRQU)) Core.setFrame(12);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CRPL)) Core.setFrame(13);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CRVP)) Core.setFrame(14);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QU)) Core.setFrame(15);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QUPL)) Core.setFrame(16);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QUVP)) Core.setFrame(17);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_PL)) Core.setFrame(18);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_PLVP)) Core.setFrame(19);
        else if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_VP)) Core.setFrame(20);
        
        else Core.setFrame(0);
    }
}
