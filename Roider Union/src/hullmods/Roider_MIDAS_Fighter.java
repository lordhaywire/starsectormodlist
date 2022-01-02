package hullmods;


import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import static hullmods.Roider_MIDAS.EMP_REDUCTION;
import static hullmods.Roider_MIDAS.MASS_BONUS;
import static hullmods.Roider_MIDAS.MOD_ID;

/**
 * Author: SafariJohn
 */
public class Roider_MIDAS_Fighter extends BaseHullMod {

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEmpDamageTakenMult().modifyMult(MOD_ID, 1f - EMP_REDUCTION / 100f);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.setMass(ship.getMass() * (1f + MASS_BONUS / 100f));
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }
}
