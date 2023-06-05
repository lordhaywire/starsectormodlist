package data.hullmods;

import com.fs.starfarer.api.Global;
import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ids.OCUA_HullMods;
import data.scripts.plugins.OCUA_BlockedHullmodDisplayScript;
import java.util.HashSet;
import java.util.Set;

public class ocua_core_pulse extends BaseHullMod {

	public static final float SPEED_FRIGATE = 60f;
	public static final float SPEED_DESTROYER = 40f;
	public static final float SPEED_CRUISER = 30f;
	public static final float SPEED_CAPITAL = 10f;
        
	public static final float OVERLOAD_MULT = 0.8f;
	public static final float ZERO_FLUX_FLAT = 10f;
	public static final float ZERO_FLUX_CAPITAL = 10f;
	public static final float RATE_BONUS = 1.4f; //1.2f;
	public static final float RATE_BONUS_CAPITAL = 1.3f;
	
	public static final float PEAK_MULT = 0.25f;
	public static final float FLUX_DISSIPATION_MULT = 2.5f; //2f;
	//public static final float FLUX_CAPACITY_MULT = 1f;
	
	public static final float CARRIER_GAIN = 0.67f;
	public static final float CARRIER_LOSS = 1.5f;
	public static final float RANGE_THRESHOLD = 500f;
	public static final float RANGE_THRESHOLD_CAPITAL = 700f;
	public static final float RANGE_MULT = 0.25f;
	
	
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static
        {
            // These hullmods will automatically be removed
            // This prevents unexplained hullmod blocking
            BLOCKED_HULLMODS.add("ocua_core_chemical");
            BLOCKED_HULLMODS.add("ocua_core_crystalline");
            //BLOCKED_HULLMODS.add("ocua_core_pulse");
            BLOCKED_HULLMODS.add("ocua_core_quantix");
            BLOCKED_HULLMODS.add("ocua_core_vapor");
        }
    
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
                if (hullSize != HullSize.CAPITAL_SHIP) {
                    stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_MULT);
                    
                    stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD);
                }
                
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f); // set to two, meaning boost is always on 
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
                stats.getEnergyRoFMult().modifyMult(id, RATE_BONUS);
                stats.getMissileRoFMult().modifyMult(id, RATE_BONUS);
                stats.getBeamWeaponDamageMult().modifyMult(id, RATE_BONUS);
                
		stats.getPeakCRDuration().modifyMult(id, PEAK_MULT);
		stats.getVentRateMult().modifyMult(id, 0f);
		stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT);
                //stats.getFighterRefitTimeMult().modifyMult(id, CARRIER_LOSS);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyMult(id, CARRIER_GAIN);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, CARRIER_LOSS);
                
                if (hullSize == HullSize.FIGHTER) {
                    stats.getMaxSpeed().modifyFlat(id, SPEED_FRIGATE);
                    stats.getAcceleration().modifyFlat(id, SPEED_FRIGATE * 2f);
                    stats.getDeceleration().modifyFlat(id, SPEED_FRIGATE * 2f);
                    //stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_FLAT);
                } else if (hullSize == HullSize.FRIGATE) {
                    stats.getMaxSpeed().modifyFlat(id, SPEED_FRIGATE);
                    stats.getAcceleration().modifyFlat(id, SPEED_FRIGATE * 2f);
                    stats.getDeceleration().modifyFlat(id, SPEED_FRIGATE * 2f);
                    //stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_FLAT);
                } else if (hullSize == HullSize.DESTROYER) {
                    stats.getMaxSpeed().modifyFlat(id, SPEED_DESTROYER);
                    stats.getAcceleration().modifyFlat(id, SPEED_DESTROYER * 2f);
                    stats.getDeceleration().modifyFlat(id, SPEED_DESTROYER * 2f);
                    //stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_FLAT);
                } else if (hullSize == HullSize.CRUISER) {
                    stats.getMaxSpeed().modifyFlat(id, SPEED_CRUISER);
                    stats.getAcceleration().modifyFlat(id, SPEED_CRUISER * 2f);
                    stats.getDeceleration().modifyFlat(id, SPEED_CRUISER * 2f);
                    //stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_FLAT);
                } else if (hullSize == HullSize.CAPITAL_SHIP) {
                    stats.getMaxSpeed().modifyFlat(id, SPEED_CAPITAL);
                    stats.getAcceleration().modifyFlat(id, SPEED_CAPITAL * 2f);
                    stats.getDeceleration().modifyFlat(id, SPEED_CAPITAL * 2f);
                    //stats.getZeroFluxSpeedBoost().modifyFlat(id, -ZERO_FLUX_CAPITAL);
                    
                    stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD_CAPITAL);
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
                tooltip.addPara("\"Hehehe... Watch this!\"", gray, opad);
                tooltip.addPara(" - 06-S112-0179 Sister \"Rina Scal\" - final recording", gray, pad);
                
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Top Speed %s, depending on hull size.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + (int) (SPEED_FRIGATE) + "/" + (int) (SPEED_DESTROYER) + "/" + (int) (SPEED_CRUISER) + "/" + (int) (SPEED_CAPITAL) );
                bullet = tooltip.addPara("Flux Dissipation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + Misc.getRoundedValue(FLUX_DISSIPATION_MULT) + "x" );
                bullet = tooltip.addPara("Energy and Missile Firing Speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((RATE_BONUS * 100) - 100) + "%" );
                bullet = tooltip.addPara("Overload time %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (OVERLOAD_MULT * 100)) + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Peak Performance time %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "reduced by a factor of 4" );
                bullet = tooltip.addPara("Weapon range reduced by %s after the threshold of %s units (%s units for Capitals).", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "" + (int) (100 - (RANGE_MULT * 100)) + "%", Misc.getRoundedValue(RANGE_THRESHOLD), Misc.getRoundedValue(RANGE_THRESHOLD_CAPITAL) );
                bullet = tooltip.addPara("Replacement recovery rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (CARRIER_GAIN * 100)) + "%" );
                bullet = tooltip.addPara("Replacement degredation rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((CARRIER_LOSS * 100) - 100) + "%" );
                
		tooltip.addSectionHeading("Compatibilities", Alignment.MID, opad);
                bullet = tooltip.addPara("Can be installed on %s ships.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "Capital" );
                bullet = tooltip.addPara("Hullmod cannot be installed on Civilian ships, unless %s is installed.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "Militarized Subsystems" );
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
                if (index == 0) return "" + (int) (SPEED_FRIGATE) + "/" + (int) (SPEED_DESTROYER) + "/" + (int) (SPEED_CRUISER) + "/" + (int) (SPEED_CAPITAL);
		if (index == 1) return Misc.getRoundedValue(FLUX_DISSIPATION_MULT);
		if (index == 2) return "" + (int) ((RATE_BONUS * 100) - 100) + "%";
		if (index == 3) return "" + (int) ((OVERLOAD_MULT * 100) - 100) + "%";
		if (index == 4) return "4";
		if (index == 5) return Misc.getRoundedValue(RANGE_THRESHOLD);
		if (index == 6) return Misc.getRoundedValue(RANGE_THRESHOLD_CAPITAL);
		if (index == 7) return "" + (int) (100 - (RANGE_MULT * 100)) + "%";
		if (index == 8) return "" + (int) ((CARRIER_LOSS * 100) - 100) + "%";
		
		//if (index == 1) return "" + (int) ZERO_FLUX_CAPITAL + " units";
		return null;
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
	public boolean isApplicableToShip(ShipAPI ship) {
		return (((ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD)) &&
                        (ship.getVariant().hasHullMod(HullMods.CIVGRADE) && ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS) || !(ship.getVariant().hasHullMod(HullMods.CIVGRADE))) &&
                        ((ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_MI_MOD) || ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OVER_MI)) ||
                        !(ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_CHEMICAL) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_CRYSTALLINE) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_QUANTIX) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_VAPOR)))) &&
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
            if (ship.getVariant().getHullMods().contains("ocua_core_chemical") ||
                ship.getVariant().getHullMods().contains("ocua_core_crystalline") ||
                ship.getVariant().getHullMods().contains("ocua_core_quantix") ||
                ship.getVariant().getHullMods().contains("ocua_core_vapor")) {
               return "Core configuration already modified";
            }
            if (ship.getVariant().hasHullMod(HullMods.CIVGRADE) && !ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) {
               return "Can not be installed on civilian ships";
            }
		
            return null;
	}
	

	private final Color color = new Color(220,20,20,255);
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
            if (ship.getShield() == null) {
                
            } else {
                ship.getShield().setRingColor(color); //(new Color(255, 255, 255, 255));
            }
		//ship.getFluxTracker().setHardFlux(ship.getFluxTracker().getCurrFlux());
//		if (ship.getEngineController().isAccelerating() || 
//				ship.getEngineController().isAcceleratingBackwards() ||
//				ship.getEngineController().isDecelerating() ||
//				ship.getEngineController().isTurningLeft() ||
//				ship.getEngineController().isTurningRight() ||
//				ship.getEngineController().isStrafingLeft() ||
//				ship.getEngineController().isStrafingRight()) {
			ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.8f);
			ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
//		}
	}

	

}
