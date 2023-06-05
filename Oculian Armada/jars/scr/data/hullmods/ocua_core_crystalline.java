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

public class ocua_core_crystalline extends BaseHullMod {
	//public static final float RANGE_FRIGATE = 5f;
	//public static final float RANGE_DESTROYER = 10f;
	//public static final float RANGE_CRUISER = 15f;
	//public static final float RANGE_CAPITAL = 20f;

	public static final float PIERCE_MULT = 0.75f;
	public static final float SHIELD_BONUS = 15f;
	public static final float RANGE_BONUS = 10f;
	public static final float SPEED_MULT = 20f;
	public static final float TURN_MULT = 0.67f;
	
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static
        {
            // These hullmods will automatically be removed
            // This prevents unexplained hullmod blocking
            BLOCKED_HULLMODS.add("ocua_core_chemical");
            //BLOCKED_HULLMODS.add("ocua_core_crystalline");
            BLOCKED_HULLMODS.add("ocua_core_pulse");
            BLOCKED_HULLMODS.add("ocua_core_quantix");
            BLOCKED_HULLMODS.add("ocua_core_vapor");
        }
    
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyMult(id, PIERCE_MULT);	
                stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
                
		stats.getMaxSpeed().modifyPercent(id, -SPEED_MULT);
		//stats.getZeroFluxSpeedBoost().modifyMult(id, 1f / SPEED_MULT); //Zero Flux compensation, since somehow, MaxSpeed affects both on Mult
                
		stats.getAcceleration().modifyMult(id, TURN_MULT);
		stats.getMaxTurnRate().modifyMult(id, TURN_MULT);
		stats.getTurnAcceleration().modifyMult(id, TURN_MULT);
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
                tooltip.addPara("\"Corps defense protocols clearly state: To stay in a safe distance between you and your target. Not because of return fire, but how the temperature rises beyond livable conditions.\"", gray, opad);
                tooltip.addPara(" - 03-S002-041 Sister \"Mami\"", gray, pad);
                
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Shield Damage taken %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) SHIELD_BONUS + "%" );
                bullet = tooltip.addPara("EMP arc resistance %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) (100 - (PIERCE_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Energy weapon range %s (additive).", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) RANGE_BONUS + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Top Speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) SPEED_MULT + "%" );
                bullet = tooltip.addPara("Maneuverability %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (TURN_MULT * 100)) + "%" );
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SHIELD_BONUS + "%";
		if (index == 1) return "" + (int) (100 - (PIERCE_MULT * 100)) + "%";
                if (index == 2) return "" + (int) (RANGE_BONUS) + "%";
		if (index == 3) return "" + (int) (100 - (SPEED_MULT * 100)) + "%";
		if (index == 4) return "" + (int) (100 - (TURN_MULT * 100)) + "%";
		return null;
	}

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
		return (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD) &&
                        ((ship.getShield() != null) || (ship.getHullSpec().getHullId().contains("galalixia_m"))) &&
                        ((ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_MI_MOD) || ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OVER_MI)) ||
                        !(ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_CHEMICAL) ||
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
        public String getUnapplicableReason(ShipAPI ship) {
            if (ship == null || !ship.getVariant().getHullMods().contains("ocua_oculus_mod")) {
               return "Must be an Oculian hull";
            }
            if (ship.getVariant().getHullMods().contains("ocua_core_chemical") ||
                ship.getVariant().getHullMods().contains("ocua_core_pulse") ||
                ship.getVariant().getHullMods().contains("ocua_core_quantix") ||
                ship.getVariant().getHullMods().contains("ocua_core_vapor")) {
               return "Core configuration already modified";
            }
            if (!(ship.getShield() != null)) {
                return "Ship has no shields";
            }
        
            return null;
        }
        
	private final Color color = new Color(40, 255, 40, 75);
        @Override
        public void advanceInCombat(ShipAPI ship, float amount) {
            if (ship.getShield() == null) {
                
            } else {
                ship.getShield().setRingColor(color); //(new Color(255, 255, 255, 255));
            }
            //ship.getShield().setRingColor(new Color(40, 255, 40, 75)); //(new Color(255, 255, 255, 255));
            //ship.getShield().setInnerColor(new Color(40, 255, 40, 75));
            //ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.4f);v
            ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
        }
        
}
