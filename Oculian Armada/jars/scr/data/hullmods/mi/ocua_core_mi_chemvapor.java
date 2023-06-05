package data.hullmods.mi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.ocua_core_vapor;
import java.awt.Color;

public class ocua_core_mi_chemvapor extends ocua_core_vapor {

	public static final float DAMAGE_MULT = 1.15f; //1.1f;
	public static final float FLUX_MULT = 15f;
	public static final float CHEM_SUPPLIES_MULT = 0.75f;
        
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMinCrewMod().modifyMult(id, CREW_MULT);
		stats.getSuppliesPerMonth().modifyMult(id, CHEM_SUPPLIES_MULT);
		stats.getSuppliesToRecover().modifyMult(id, 1f);
		stats.getFuelUseMod().modifyMult(id, FUEL_MULT);
		stats.getSensorProfile().modifyMult(id, PROFILE_MULT);
                
		stats.getEnergyRoFMult().modifyMult(id, 1f);
		stats.getFluxCapacity().modifyMult(id, FLUX_CAPACITY_MULT);
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
		stats.getMaxBurnLevel().modifyFlat(id, -BURN_PENALTY);
                
		stats.getEnergyWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (FLUX_MULT / 100)));
		stats.getBeamWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
                stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (FLUX_MULT / 100)));
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
                bullet = tooltip.addPara("Energy and Beam damage %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((DAMAGE_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("Energy and Beam flux generation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) FLUX_MULT + "%" );
                bullet = tooltip.addPara("Minimum Crew requirement %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (CREW_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Supply Cost for Maintenance %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (CHEM_SUPPLIES_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Fuel cost per lightyear %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (FUEL_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Sensor Profile size %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (PROFILE_MULT * 100)) + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Flux Dissipation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (FLUX_DISSIPATION_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Flux Capacity %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (FLUX_CAPACITY_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Burn Level %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) BURN_PENALTY + "" );
                
		tooltip.addSectionHeading("Compatibilities", Alignment.MID, opad);
                bullet = tooltip.addPara("Hullmod is %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "not considered a Logistics hullmod" );
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "most";
		if (index == 1) return "" + (int) ((CHEM_SUPPLIES_MULT * 100) - 100) + "%";
		if (index == 2) return "unchanged";
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
        
        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            super.applyEffectsAfterShipCreation(ship, id);
        }
}
