package RealisticCombat.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import static RealisticCombat.settings.Categorization.WeaponCategory;
import static RealisticCombat.settings.Categorization.getDamageThreshold;

public final class Categorization {

    public static boolean isAutopilotOn() { return !Global.getCombatEngine().isUIAutopilotOn(); }

    public static boolean isStrikeCraft(final ShipAPI ship) {
        return ship.isFighter() || ship.isDrone();
    }

    public static boolean isFlying(final ShipAPI ship) {
        return !(ship == null || ship.isPiece() || ship.isHulk() || !ship.isAlive());
    }

    public static boolean isStation(final ShipAPI ship) {
        return ship.isStationModule() || ship.isStation();
    }

    public static boolean isPlayerFlown(final ShipAPI ship) {
        return ship == Global.getCombatEngine().getPlayerShip()
                && Global.getCombatEngine().isUIAutopilotOn();
    }

    public static boolean isModule(final ShipHullSpecAPI shipHullSpec) {
        try { return shipHullSpec.getModuleAnchor() != null
                      || (shipHullSpec.getEngineSpec().getMaxSpeed() == 0
                      && shipHullSpec.getEngineSpec().getAcceleration() == 0
                      && shipHullSpec.getEngineSpec().getDeceleration() == 0);
        } catch (Throwable t) { return false; }
    }

    public static boolean isStation(final ShipHullSpecAPI shipHullSpec) {
        return shipHullSpec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.STATION);
    }

    public static boolean isFighter(final ShipHullSpecAPI shipHullSpec) {
        return shipHullSpec.getHullSize() == ShipAPI.HullSize.FIGHTER;
    }

    public static boolean isTorpedo(final MissileSpecAPI missileSpec) {
        return missileSpec.getDamage().getBaseDamage()
                > getDamageThreshold(WeaponCategory.LAUNCHER_TORPEDO,
                                     missileSpec.getDamage().getType());
    }

    public static RealisticCombat.settings.Categorization.WeaponCategory getLauncherWeaponCategory(
            final WeaponSpecAPI weaponSpec
    ) {
        return isTorpedo((MissileSpecAPI) weaponSpec.getProjectileSpec())
                ? WeaponCategory.LAUNCHER_TORPEDO : WeaponCategory.LAUNCHER_MISSILE;
    }
}
