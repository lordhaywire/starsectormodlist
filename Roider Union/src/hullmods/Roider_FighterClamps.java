package hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.DefectiveManufactory;
import ids.Roider_Ids.Roider_Hullmods;
import org.magiclib.util.MagicIncompatibleHullmods;

public class Roider_FighterClamps extends BaseHullMod {

	//public static final int CARGO_REQ = 80;
	public static final int ALL_FIGHTER_COST_PERCENT = 50;
	public static final int BOMBER_COST_MOD = 10000;

	private static final Map<HullSize, Float> CREW_REQ = new HashMap<>();
	static {
		CREW_REQ.put(HullSize.FRIGATE, 10f);
		CREW_REQ.put(HullSize.DESTROYER, 10f);
		CREW_REQ.put(HullSize.CRUISER, 30f);
		CREW_REQ.put(HullSize.CAPITAL_SHIP, 70f);
	}

	private static final Map<HullSize, Float> BAYS = new HashMap<>();
	static {
		BAYS.put(HullSize.FRIGATE, 1f);
		BAYS.put(HullSize.DESTROYER, 1f);
		BAYS.put(HullSize.CRUISER, 2f);
		BAYS.put(HullSize.CAPITAL_SHIP, 4f);
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMinCrewMod().modifyFlat(id, CREW_REQ.get(hullSize));

		stats.getFighterRefitTimeMult().modifyMult(id, 1000000f);
		stats.getNumFighterBays().modifyFlat(id, BAYS.get(hullSize));
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, 1000000f);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyMult(id, 0f);

		//stats.getDynamic().getMod(Stats.ALL_FIGHTER_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
		stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyFlat(id, BOMBER_COST_MOD);
		stats.getDynamic().getMod(Stats.FIGHTER_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
		stats.getDynamic().getMod(Stats.INTERCEPTOR_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
		stats.getDynamic().getMod(Stats.SUPPORT_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
		//stats.getCargoMod().modifyFlat(id, -CARGO_REQ);
	}

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship.getVariant().hasHullMod(HullMods.CONVERTED_HANGAR)) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(),
                    HullMods.CONVERTED_HANGAR, Roider_Hullmods.FIGHTER_CLAMPS);
        }

        // Trying to block Fighter Clamps on armor modules.
        if (ship.getHullSpec().getOrdnancePoints(null) <= 1) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(),
                    Roider_Hullmods.FIGHTER_CLAMPS, Roider_Hullmods.EXTREME_MODS);
        }
    }

	public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null) return false;

        if (ship.getHullSpec().getBaseHullId().startsWith("swp_arcade")) return false;

        if (ship.getVariant().hasHullMod(Roider_Hullmods.MIDAS_ARMOR)) return false;
        if (ship.getEngineController().getShipEngines().isEmpty()
                    && ship.getHullSpec().getOrdnancePoints(null) <= 1) return false;


        return ship.getHullSpec().getFighterBays() <= 0
                    && !ship.getVariant().hasHullMod(HullMods.CONVERTED_HANGAR)
                    && !ship.getVariant().hasHullMod(HullMods.CONVERTED_BAY)
                    && !ship.getVariant().hasHullMod(HullMods.PHASE_FIELD);
	}

	public String getUnapplicableReason(ShipAPI ship) {
        if (ship == null) return "Can not be installed";

        if (ship.getHullSpec().getBaseHullId().startsWith("swp_arcade")) {
            return "Can not be installed on arcade ships";
        }

        String armorDesc = "Can not be installed on an armor module";

        if (ship.getVariant().hasHullMod(Roider_Hullmods.MIDAS_ARMOR)) return armorDesc;
        if (ship.getEngineController().getShipEngines().isEmpty()
                    && ship.getHullSpec().getOrdnancePoints(null) <= 1) return armorDesc;

		if (ship.getHullSpec().getFighterBays() > 0) return "Ship has standard fighter bays";
		if (ship.getVariant().hasHullMod(HullMods.CONVERTED_HANGAR)) return "Ship has fighter bays";
		if (ship.getVariant().hasHullMod(HullMods.CONVERTED_BAY)) return "Ship has fighter bays";
		if (ship.getVariant().hasHullMod(HullMods.PHASE_FIELD)) return "Can not be installed on a phase ship";
		//if (ship.getNumFighterBays() > 0) return "Ship has fighter bays";
		return "Can not be installed";
	}

	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		new DefectiveManufactory().applyEffectsToFighterSpawnedByShip(fighter, ship, id);
	}

	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "1/1/2/4";
		if (index == 1) return "" + CREW_REQ.get(hullSize).intValue();
		if (index == 2) return "" + ALL_FIGHTER_COST_PERCENT + "%";
		return new DefectiveManufactory().getDescriptionParam(index - 3, hullSize, ship);
//		if (index == 0) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
//		if (index == 1) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
//		if (index == 2) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
//		if (index == 3) return "" + CREW_REQ;
//		return null;
		//if (index == 0) return "" + ((Float) mag.get(hullSize)).intValue();
		//return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}
}



