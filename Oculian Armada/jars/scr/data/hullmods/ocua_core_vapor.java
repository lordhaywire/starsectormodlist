package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ids.OCUA_HullMods;
import data.scripts.plugins.OCUA_BlockedHullmodDisplayScript;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class ocua_core_vapor extends BaseHullMod {

	public static final float CREW_MULT = 0.5f;
	public static final float SUPPLIES_MULT = 0.5f;
	public static final float FUEL_MULT = 0.33f;
	public static final float PROFILE_MULT = 0.2f;
        
	public static final float FLUX_CAPACITY_MULT = 0.75f;
	public static final float FLUX_DISSIPATION_MULT = 0.75f;
	public static final float RATE_PENALTY = 0.75f;
	public static final float SENSOR_PENALTY = 0.67f;
	public static final float BURN_PENALTY = 1f;
        
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static
        {
            // These hullmods will automatically be removed
            // This prevents unexplained hullmod blocking
            BLOCKED_HULLMODS.add("ocua_core_chemical");
            BLOCKED_HULLMODS.add("ocua_core_crystalline");
            BLOCKED_HULLMODS.add("ocua_core_pulse");
            BLOCKED_HULLMODS.add("ocua_core_quantix");
            //BLOCKED_HULLMODS.add("ocua_core_vapor");
        }
    
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMinCrewMod().modifyMult(id, CREW_MULT);
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLIES_MULT);
		stats.getSuppliesToRecover().modifyMult(id, SUPPLIES_MULT);
		stats.getFuelUseMod().modifyMult(id, FUEL_MULT);
		stats.getSensorProfile().modifyMult(id, PROFILE_MULT);
                
		stats.getEnergyRoFMult().modifyMult(id, RATE_PENALTY);
		stats.getFluxCapacity().modifyMult(id, FLUX_CAPACITY_MULT);
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
		stats.getMaxBurnLevel().modifyFlat(id, -BURN_PENALTY);
	}
	
        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            super.applyEffectsAfterShipCreation(ship, id);

            for (String tmp : BLOCKED_HULLMODS) {
                if (!ship.getVariant().getHullMods().contains("ocua_mi_mod")){
                    if (ship.getVariant().getHullMods().contains(tmp)) {
                        ship.getVariant().removeMod(tmp);
                        OCUA_BlockedHullmodDisplayScript.showBlocked(ship);
                    }
                }
            }
        }
    
        protected static final float LOAD_OF_BULL = 3f;
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		float pad = 3f;
		Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                Color good = Misc.getPositiveHighlightColor();
                Color gray = Misc.getGrayColor();
		
                LabelAPI bullet;
                tooltip.addPara("\"Honestly. I have no idea what to do with all these cookies in the middle of hyperspace.\"", gray, opad);
                tooltip.addPara(" - Pirate captain Gaster", gray, pad);
                
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
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Flux Dissipation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (FLUX_DISSIPATION_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Flux Capacity %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (FLUX_CAPACITY_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Energy weapon firing speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (RATE_PENALTY * 100)) + "%" );
                bullet = tooltip.addPara("Burn Level %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) BURN_PENALTY + "" );
                
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
		if (index == 4) return "" + (int) (100 - (FLUX_DISSIPATION_MULT * 100)) + "%";
		if (index == 5) return "" + (int) (100 - (FLUX_CAPACITY_MULT * 100)) + "%";
		if (index == 6) return "" + (int) (100 - (RATE_PENALTY * 100)) + "%";
		if (index == 7) return "" + (int) BURN_PENALTY;
		if (index == 8) return "not considered as a Logistics hullmod";
		return null;
	}

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
		return (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD) &&
                        ((ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_MI_MOD) || ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OVER_MI)) ||
                        !(ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_CHEMICAL) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_CRYSTALLINE) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_PULSE) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_QUANTIX))) &&
                        !(ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CH) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHCR) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHPL) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHQU) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHVP) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CR) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CRPL) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CRQU) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CRVP) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_PL) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_PLVP) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QU) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QUPL) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QUVP) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_VP)));
        }
    
        @Override
        public String getUnapplicableReason(ShipAPI ship) {
            if (ship == null || !ship.getVariant().getHullMods().contains("ocua_oculus_mod")) {
               return "Must be an Oculian hull";
            }
            if (ship.getVariant().getHullMods().contains("ocua_core_chemical") ||
                ship.getVariant().getHullMods().contains("ocua_core_crystalline") ||
                ship.getVariant().getHullMods().contains("ocua_core_pulse") ||
                ship.getVariant().getHullMods().contains("ocua_core_quantix")) {
               return "Core configuration already modified";
            }
        
            return null;
        }
}
