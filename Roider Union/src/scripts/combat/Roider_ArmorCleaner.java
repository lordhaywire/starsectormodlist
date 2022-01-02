package scripts.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: Deletes destroyed armor plates so we don't have them
 *      floating around or absorbing more damage than they should.
 *      Doesn't affect station armor.
 * Author: SafariJohn
 */
public class Roider_ArmorCleaner extends BaseEveryFrameCombatPlugin {
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

        // Detection loop
        List<ShipAPI> shipsToRemove = new ArrayList<>();
        for (ShipAPI ship : engine.getShips()) {
            if (!ship.isAlive() && ship.getHullSpec().getTags().contains("roider_armor")) {
                shipsToRemove.add(ship);
            }
        }


        // Removal loop
        while (!shipsToRemove.isEmpty()) {
            engine.removeEntity(shipsToRemove.get(0));
            shipsToRemove.remove(0);
        }
    }

}
