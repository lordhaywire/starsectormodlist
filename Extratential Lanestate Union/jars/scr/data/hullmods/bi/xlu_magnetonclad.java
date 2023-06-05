package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

// Woah! This is no longer worthless. Haha.
public class xlu_magnetonclad extends BaseHullMod {
    public static float ARMOR_MIN = 0.1f;
    public static float ARMOR_MULT = 3f;
    public static float ARMOR_STRENGTH_MULT = 2.5f;
    public static float ARMOR_STRENGTH_FLAT = 625f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMinArmorFraction().modifyFlat(id, ARMOR_MIN);
        stats.getArmorBonus().modifyMult(id, ARMOR_MULT);
        stats.getEffectiveArmorBonus().modifyMult(id, 1f / ARMOR_STRENGTH_MULT);
        stats.getEffectiveArmorBonus().modifyFlat(id, ARMOR_STRENGTH_FLAT); //Divided by the mult.
    }
	
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        //Excuse me, how did that end up here
	//ship.setInvalidTransferCommandTarget(true);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if(index == 0) return "" + (int) ARMOR_MULT + "x";
	if(index == 1) return "" + (int) (ARMOR_MIN * 100) + "%";
	if(index == 2) return "" + (int) (ARMOR_STRENGTH_FLAT / ARMOR_STRENGTH_MULT) + "";
	if(index == 3) return "" + (float) ARMOR_STRENGTH_MULT + "";
	if(index == 4) return "Armorclad Works armor effectiveness has no effect.";
	return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("xlu_");
    }
}
