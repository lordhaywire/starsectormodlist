package data.hullmods.mi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class ocua_core_mi_vapor extends BaseHullMod {

	public static final float CREW_MULT = 0.5f;
	public static final float SUPPLIES_MULT = 0.5f;
	public static final float FUEL_MULT = 0.33f;
	public static final float PROFILE_MULT = 0.2f;
        
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMinCrewMod().modifyMult(id, CREW_MULT);
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLIES_MULT);
		stats.getSuppliesToRecover().modifyMult(id, SUPPLIES_MULT);
		stats.getFuelUseMod().modifyMult(id, FUEL_MULT);
		stats.getSensorProfile().modifyMult(id, PROFILE_MULT);
	}
	
        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            super.applyEffectsAfterShipCreation(ship, id);
        }
    
    
        protected static final float LOAD_OF_BULL = 3f;
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                Color good = Misc.getPositiveHighlightColor();
		
                LabelAPI bullet;
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Minimum Crew requirement %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (CREW_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Supply Cost for Maintenance/Repair %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (SUPPLIES_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Fuel cost per lightyear %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (FUEL_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Sensor Profile size %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (PROFILE_MULT * 100)) + "%" );
                
		tooltip.addSectionHeading("Compatibilities", Alignment.MID, opad);
                bullet = tooltip.addPara("Hullmod is %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "not considered a Logistics hullmod" );
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) (100 - (CREW_MULT * 100)) + "%";
                if (index == 1) return "" + (int) (100 - (SUPPLIES_MULT * 100)) + "%";
		if (index == 2) return "" + (int) (100 - (FUEL_MULT * 100)) + "%";
		if (index == 3) return "" + (int) (100 - (PROFILE_MULT * 100)) + "%";
		if (index == 4) return "does not receive penalties";
		if (index == 5) return "not considered as a Logistics hullmod";
		return null;
	}

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
		return true;
        }
    
        @Override
        public String getUnapplicableReason(ShipAPI ship) {
            return null;
        }
}
