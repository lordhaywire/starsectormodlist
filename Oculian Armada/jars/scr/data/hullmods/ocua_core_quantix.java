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

public class ocua_core_quantix extends BaseHullMod {
	public static final float CARRIER_HANGAR = 1f;
	public static final float CARRIER_BONUS = 1.25f;
	public static final float CARRIER_REFIT = 0.75f;
	public static final float CARRIER_REPLACEMENT_GAIN = 1.2f;
	public static final float CARRIER_REPLACEMENT_LOSS = 0.67f;

	public static final float LOSS_PENALTY = 1.5f;
	public static final float READINESS_MULT_PER_DECK = 5f;
	public static final float SUPPLIES_MULT = 1.33f;

        //private static final boolean baseless_module = ship_unit.contains("ocua_baseless_module");
            
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static
        {
            // These hullmods will automatically be removed
            // This prevents unexplained hullmod blocking
            BLOCKED_HULLMODS.add("ocua_core_chemical");
            BLOCKED_HULLMODS.add("ocua_core_crystalline");
            BLOCKED_HULLMODS.add("ocua_core_pulse");
            //BLOCKED_HULLMODS.add("ocua_core_quantix");
            BLOCKED_HULLMODS.add("ocua_core_vapor");
        }
    
        @Override
        public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            if (!(stats.getVariant().getHullSpec().getBuiltInMods().contains("ocua_baseless_module") || (hullSize == HullSize.FRIGATE))) {
                        stats.getNumFighterBays().modifyFlat(id, CARRIER_HANGAR);
            }
        
                stats.getHangarSpaceMod().modifyMult(id, CARRIER_BONUS);
                stats.getFighterRefitTimeMult().modifyMult(id, CARRIER_REFIT);
                
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, CARRIER_REPLACEMENT_LOSS);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyMult(id, CARRIER_REPLACEMENT_GAIN);
                
		stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, LOSS_PENALTY);
                
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLIES_MULT);
		stats.getSuppliesToRecover().modifyMult(id, SUPPLIES_MULT);
		//int reedi = (int) (stats.getNumFighterBays().getBaseValue() * READINESS_MULT_PER_DECK);
		float reedi = (float) ((100 - (stats.getNumFighterBays().getBaseValue() * READINESS_MULT_PER_DECK)) / 100);
		stats.getPeakCRDuration().modifyMult(id, reedi);
            if (!stats.getVariant().getHullSpec().getBuiltInMods().contains(OCUA_HullMods.OCUA_DULCENA_ARRAY)) {
		stats.getZeroFluxSpeedBoost().modifyMult(id, 0);
            }
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
                tooltip.addPara("\"Ashamed, we are. That Mother left us on our own devices, only for us to make most out of it... and allow us pry into the darkest depths of the void. Shame.\"", gray, opad);
                tooltip.addPara(" - 01-S024-005 M-Sister \"Jupina\"", gray, pad);
                
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Hangar bay %s, destroyers and up.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) CARRIER_HANGAR + " slot" );
                bullet = tooltip.addPara("Fighter/Bomber refit time %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (100 *(CARRIER_REFIT))) + "%" );
                bullet = tooltip.addPara("Replacement recovery rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((100 * (CARRIER_REPLACEMENT_GAIN)) - 100) + "%" );
                bullet = tooltip.addPara("Replacement degredation rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (100 * (CARRIER_REPLACEMENT_LOSS))) + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Peak Readiness time %s, %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) READINESS_MULT_PER_DECK + "%", "per deck" );
		bullet.setHighlight("-" + (int) READINESS_MULT_PER_DECK + "%", "per deck");
		bullet.setHighlightColors(bad, h);
                bullet = tooltip.addPara("Zero-flux speed bonus is %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "prevented" );
                bullet = tooltip.addPara("Crew loss from crewed wing losses %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((100 *(LOSS_PENALTY)) - 100) + "%" );
                bullet = tooltip.addPara("Supply Cost for Maintenance/Repair %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((SUPPLIES_MULT * 100) - 100) + "%" );
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
        public String getDescriptionParam(int index, HullSize hullSize) {
		if(index == 0) return "" + (int) CARRIER_HANGAR;
		if(index == 1) return "" + (int) (100 - (100 *(CARRIER_REFIT))) + "%";
		if(index == 2) return "" + (int) (100 - (100 *(CARRIER_REPLACEMENT_LOSS))) + "%";
		if(index == 3) return "" + (int) ((100 *(CARRIER_REPLACEMENT_GAIN)) - 100) + "%";
		if(index == 4) return "" + (int) READINESS_MULT_PER_DECK + "%";
		if(index == 5) return "prevents Zero-flux speed bonus";
		if(index == 6) return "" + (int) ((100 *(LOSS_PENALTY)) - 100) + "%";
		if(index == 7)return "" + (int) ((SUPPLIES_MULT * 100) - 100) + "%";
            else {
                return null;
            }
        }

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
		int bays = (int) ship.getMutableStats().getNumFighterBays().getBaseValue();
                
		return (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD) &&
                        !(((bays < 1) && ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_BASELESS_MODULE)) || ((bays < 1) && ship.isFrigate())) &&
                        ((ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_MI_MOD) || ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OVER_MI)) ||
                        !(ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_CHEMICAL) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_CRYSTALLINE) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_PULSE) ||
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
                        //((bays > 0) && !ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_BASELESS_MODULE)));
        }
    
        @Override
        public String getUnapplicableReason(ShipAPI ship) {
            int bays = (int) ship.getMutableStats().getNumFighterBays().getBaseValue();
            if (ship == null || !ship.getVariant().getHullMods().contains("ocua_oculus_mod")) {
               return "Must be an Oculian hull";
            }
            if (ship.getVariant().getHullMods().contains("ocua_core_chemical") ||
                ship.getVariant().getHullMods().contains("ocua_core_crystalline") ||
                ship.getVariant().getHullMods().contains("ocua_core_pulse") ||
                ship.getVariant().getHullMods().contains("ocua_core_vapor")) {
               return "Core configuration already modified";
            }
            //if (ship == null && ((bays > 0) && !ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_BASELESS_MODULE))) {
            if ((bays < 1) && ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_BASELESS_MODULE)) {
                return "Module has no hangar bays";
            } else if ((bays < 1) && ship.isFrigate()) {
                return "Frigate has no hangar bays";
            }
        
            return null;
        }
}
