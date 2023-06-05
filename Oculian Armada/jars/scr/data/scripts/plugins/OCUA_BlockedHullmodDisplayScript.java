package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;

public class OCUA_BlockedHullmodDisplayScript extends BaseEveryFrameCombatPlugin {

    public static void showBlocked(ShipAPI blocked) {
        data.scripts.everyframe.OCUA_BlockedHullmodDisplayScript.showBlocked(blocked);
    }

    public static void stopDisplaying() {
        data.scripts.everyframe.OCUA_BlockedHullmodDisplayScript.stopDisplaying();
    }
}
