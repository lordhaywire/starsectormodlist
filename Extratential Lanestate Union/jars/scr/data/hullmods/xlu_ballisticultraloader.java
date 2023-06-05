package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;

public class xlu_ballisticultraloader extends BaseHullMod {

    public static final float RELOAD_TIME = 60f;

    public static String DA_KEE = "ugh_aux_ord_forge_key";

    public static class AmmoRegenClass {
        IntervalUtil interval = new IntervalUtil(RELOAD_TIME, RELOAD_TIME);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return "reloads the magazines";
        if (index == 1) return "one fifth";
        if (index == 2) return "" + (int) RELOAD_TIME;
        return null;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);
        if (!ship.isAlive()) return;
        CombatEngineAPI engine = Global.getCombatEngine();

        String key = DA_KEE + "_" + ship.getId();
        AmmoRegenClass amreg = (AmmoRegenClass) engine.getCustomData().get(key);
        if (amreg == null) {
            amreg = new AmmoRegenClass();
            engine.getCustomData().put(key, amreg);
        }

        boolean hasAmmo = false;
        for (WeaponAPI gun : ship.getAllWeapons()) {
            if (gun.getType() != WeaponType.ENERGY || gun.getType() != WeaponType.BALLISTIC) continue;
            if (gun.usesAmmo() && gun.getAmmo() < gun.getMaxAmmo()) hasAmmo = true;
        }

        if (hasAmmo) {
            amreg.interval.advance(amount);
            if (amreg.interval.intervalElapsed()) {
                for (WeaponAPI gun : ship.getAllWeapons()) {
                    if (gun.getType() != WeaponType.ENERGY || gun.getType() != WeaponType.BALLISTIC) continue;

                    if (gun.usesAmmo() && gun.getAmmo() < gun.getMaxAmmo()) {
                        int reload = (int) Math.max(1f, (float) gun.getMaxAmmo() / 5f);
                        gun.setAmmo(gun.getAmmo() + reload);
                    }
                }
            }
        } else amreg.interval.setElapsed(0f);
    }
}
