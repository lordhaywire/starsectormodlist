package hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;

/**
 * Copied banano of doom's idea
 */
public class Roider_FighterNoStrafe extends BaseHullMod {
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
        ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT);
        ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT);
        ship.blockCommandForOneFrame(ShipCommand.DECELERATE);
    }
}
