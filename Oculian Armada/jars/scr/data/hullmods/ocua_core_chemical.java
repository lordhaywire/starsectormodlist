package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ids.OCUA_HullMods;
import data.scripts.plugins.OCUA_BlockedHullmodDisplayScript;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class ocua_core_chemical extends BaseHullMod {

	//public static final float EFFICIENCY_PENALTY = 1.1f;
	public static final float DAMAGE_MULT = 1.15f; //1.1f;
	public static final float FLUX_MULT = 15f;
	public static final float SUPPLIES_MULT = 1.25f;
	public static final float REPAIR_MULT = 1.67f;
	public static final float DEATH_RANGE_MULT = 1.25f;
	public static final float DEATH_DAMAGE_MULT = 1.5f;
	
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static
        {
            // These hullmods will automatically be removed
            // This prevents unexplained hullmod blocking
            //BLOCKED_HULLMODS.add("ocua_core_chemical");
            BLOCKED_HULLMODS.add("ocua_core_crystalline");
            BLOCKED_HULLMODS.add("ocua_core_pulse");
            BLOCKED_HULLMODS.add("ocua_core_quantix");
            BLOCKED_HULLMODS.add("ocua_core_vapor");
        }
    
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (FLUX_MULT / 100)));
		stats.getBeamWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
                stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (FLUX_MULT / 100)));
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLIES_MULT);
		stats.getSuppliesToRecover().modifyMult(id, REPAIR_MULT);
		stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, DEATH_RANGE_MULT);
		stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, DEATH_DAMAGE_MULT);
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
                tooltip.addPara("\"It's hard to tell where all this energy is coming from. Could be the gas here, crystals there or just some batteries left in the pantry.\"", gray, opad);
                tooltip.addPara(" - 02-S031-184 Sister \"Shika\"", gray, pad);
                
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Energy and Beam damage %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((DAMAGE_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("Energy and Beam flux generation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) FLUX_MULT + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Supply Cost for Maintenance %s and for Repair %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((SUPPLIES_MULT * 100) - 100) + "%", "+" + (int) ((REPAIR_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("%s larger explosion upon death.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((DEATH_DAMAGE_MULT * 100) - 100) + "%" );
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((DAMAGE_MULT * 100) - 100) + "%";
		if (index == 1) return "" + (int) (FLUX_MULT) + "%";
		if (index == 2) return "" + (int) ((SUPPLIES_MULT * 100) - 100) + "%";
		if (index == 3) return "" + (int) ((REPAIR_MULT * 100) - 100) + "%";
		if (index == 4) return "" + (int) ((DEATH_DAMAGE_MULT * 100) - 100) + "%";
		return null;
	}

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
		return (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD) &&
                        ((ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_MI_MOD) || ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OVER_MI)) ||
                        !(ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_CRYSTALLINE) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_PULSE) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_QUANTIX) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_VAPOR))) &&
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
    
        @Override
        public String getUnapplicableReason(ShipAPI ship) {
            if (ship == null || !ship.getVariant().getHullMods().contains("ocua_oculus_mod")) {
               return "Must be an Oculian hull";
            }
            if (ship.getVariant().getHullMods().contains("ocua_core_crystalline") ||
                ship.getVariant().getHullMods().contains("ocua_core_pulse") ||
                ship.getVariant().getHullMods().contains("ocua_core_quantix") ||
                ship.getVariant().getHullMods().contains("ocua_core_vapor")) {
               return "Core configuration already modified";
            }
        
            return null;
        }
}
