package hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class Roider_HeavyArmor extends BaseHullMod {

    public final static Map<String, HullSize> HULL_SIZE = new HashMap<>();
    static {
        HULL_SIZE.put("roider_roach_armor", HullSize.FRIGATE);
        HULL_SIZE.put("roider_onager_armor", HullSize.DESTROYER);
        HULL_SIZE.put("roider_aurochs_armor", HullSize.DESTROYER);
        HULL_SIZE.put("roider_firestorm_left", HullSize.DESTROYER);
        HULL_SIZE.put("roider_firestorm_right", HullSize.DESTROYER);
        HULL_SIZE.put("roider_gambit_armor", HullSize.CRUISER);
        HULL_SIZE.put("roider_ranch_armor", HullSize.CRUISER);
        HULL_SIZE.put("roider_wrecker_armor", HullSize.CRUISER);
        HULL_SIZE.put("roider_telamon_front", HullSize.CAPITAL_SHIP);
        HULL_SIZE.put("roider_telamon_left", HullSize.CAPITAL_SHIP);
        HULL_SIZE.put("roider_telamon_right", HullSize.CAPITAL_SHIP);
    }

	public static final float MANEUVER_PENALTY = 10f;

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 150f);
		mag.put(HullSize.DESTROYER, 300f);
		mag.put(HullSize.CRUISER, 400f);
		mag.put(HullSize.CAPITAL_SHIP, 500f);
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        String hullId = stats.getVariant().getHullSpec().getHullId();

        if (hullId != null) hullSize = HULL_SIZE.get(hullId);

		stats.getArmorBonus().modifyFlat(id, (Float) mag.get(hullSize));

		//stats.getCargoMod().modifyFlat(id, -70);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		if (index == 4) return "" + (int) MANEUVER_PENALTY + "%";
		return null;
		//if (index == 0) return "" + ((Float) mag.get(hullSize)).intValue();
		//return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
        String hullId = ship.getHullSpec().getHullId();

        if (hullId != null) return HULL_SIZE.containsKey(hullId);

		return false;
	}
}
