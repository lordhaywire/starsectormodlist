package scripts.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.List;

/**
 * Trying to stop fighters from auto-escorting armor modules
 *
 * Author: SafariJohn
 */
public class Roider_ArmorNoEscort extends BaseEveryFrameCombatPlugin {
    private float elapsed = 0;
    private final float INTERVAL = 0.05f;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine.isPaused()) return;
        if (engine.isUIShowingDialog()) return;

        elapsed += amount;
        if (elapsed < INTERVAL) return;
        elapsed -= INTERVAL;

        for (ShipAPI ship : engine.getShips()) {
            ShipwideAIFlags flags = ship.getAIFlags();
            if (ship.getHullSpec().getTags().contains("roider_armor")) {
                flags.unsetFlag(AIFlags.NEEDS_HELP);
                flags.removeFlag(AIFlags.NEEDS_HELP);
            }

            if (flags.hasFlag(AIFlags.CARRIER_FIGHTER_TARGET)) {
                CombatEntityAPI target = (CombatEntityAPI) flags.getCustom(AIFlags.CARRIER_FIGHTER_TARGET);
                if (target instanceof ShipAPI) {
                    ShipAPI tShip = (ShipAPI) target;
                    if (tShip.getHullSpec().getTags().contains("roider_armor")
                                && ship.getOwner() == tShip.getOwner()) {
                        flags.unsetFlag(AIFlags.CARRIER_FIGHTER_TARGET);
                    }
                }
            }
        }
    }

}
