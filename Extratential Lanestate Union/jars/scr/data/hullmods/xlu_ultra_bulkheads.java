package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class xlu_ultra_bulkheads extends BaseHullMod {
	
	public static final float HULL_BONUS = 20f;
	public static final float ARMOR_MIN = 0.05f;
        public static final float KINETIC_RESIST = 5f;
        
        public static final float SMODIFIER = 5f;
        public static final float SMODIFIER2 = 10f;

        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            boolean sMod = isSMod(stats);
            
		stats.getHullBonus().modifyPercent(id, HULL_BONUS);
		stats.getMinArmorFraction().modifyFlat(id, ARMOR_MIN);
		stats.getKineticDamageTakenMult().modifyMult(id, 1f - ((KINETIC_RESIST / 100f) + ((sMod ? SMODIFIER : 0) / 100f)));
                
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - ((sMod ? SMODIFIER : 0) * 0.01f));
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) HULL_BONUS + "%";
		if (index == 1) return "" + (int) (ARMOR_MIN * 100) + "%";
		if (index == 2) return "" + (int) KINETIC_RESIST + "%";
		return null;
	}
        
        @Override
	public String getSModDescriptionParam(int index, HullSize hullSize) {
            if (index == 0) return "" + (int) (KINETIC_RESIST + SMODIFIER) + "%";
            if (index == 1) return "" + (int) SMODIFIER2  + "%";
            return null;
	}
	
}



