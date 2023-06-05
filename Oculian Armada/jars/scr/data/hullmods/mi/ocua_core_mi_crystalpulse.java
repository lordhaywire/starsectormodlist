package data.hullmods.mi;

import com.fs.starfarer.api.Global;
import java.awt.Color;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.ocua_core_pulse;

public class ocua_core_mi_crystalpulse extends ocua_core_pulse {
	public static final float CRYPULSE_RNG_THRESH = 700f;
	public static final float CRYPULSE_RNG_THRESH_CAPITAL = 900f;
	public static final float CRYPULSE_RANGE_MULT = 0.33f;
        
	public static final float CRYPULSE_SPD_FRI = 40f;
	public static final float CRYPULSE_SPD_DES = 20f;
	public static final float CRYPULSE_SPD_CSR = 10f;
        
	public static final float PIERCE_MULT = 0.75f;
	public static final float SHIELD_BONUS = 20f; //15f;
	public static final float RANGE_BONUS = 10f;
	public static final float SPEED_MULT = 0.8f;
	public static final float TURN_MULT = 0.67f;

        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
                if (hullSize != HullSize.CAPITAL_SHIP) {
                    stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_MULT);
                    
                    stats.getWeaponRangeThreshold().modifyFlat(id, CRYPULSE_RNG_THRESH);
                }
                
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f); // set to two, meaning boost is always on 
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
                stats.getEnergyRoFMult().modifyMult(id, RATE_BONUS);
                stats.getMissileRoFMult().modifyMult(id, RATE_BONUS);
                stats.getBeamWeaponDamageMult().modifyMult(id, RATE_BONUS);
                
		stats.getPeakCRDuration().modifyMult(id, PEAK_MULT);
		stats.getVentRateMult().modifyMult(id, 0f);
		stats.getWeaponRangeMultPastThreshold().modifyMult(id, CRYPULSE_RANGE_MULT);
                stats.getFighterRefitTimeMult().modifyMult(id, CARRIER_LOSS);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, CARRIER_LOSS);
                
                if (hullSize == HullSize.FIGHTER) {
                    stats.getMaxSpeed().modifyFlat(id, CRYPULSE_SPD_FRI);
                    stats.getAcceleration().modifyFlat(id, CRYPULSE_SPD_FRI * 2f);
                    stats.getDeceleration().modifyFlat(id, CRYPULSE_SPD_FRI * 2f);
                } else if (hullSize == HullSize.FRIGATE) {
                    stats.getMaxSpeed().modifyFlat(id, CRYPULSE_SPD_FRI);
                    stats.getAcceleration().modifyFlat(id, CRYPULSE_SPD_FRI * 2f);
                    stats.getDeceleration().modifyFlat(id, CRYPULSE_SPD_FRI * 2f);
                } else if (hullSize == HullSize.DESTROYER) {
                    stats.getMaxSpeed().modifyFlat(id, CRYPULSE_SPD_DES);
                    stats.getAcceleration().modifyFlat(id, CRYPULSE_SPD_DES * 2f);
                    stats.getDeceleration().modifyFlat(id, CRYPULSE_SPD_DES * 2f);
                } else if (hullSize == HullSize.CRUISER) {
                    stats.getMaxSpeed().modifyFlat(id, CRYPULSE_SPD_CSR);
                    stats.getAcceleration().modifyFlat(id, CRYPULSE_SPD_CSR * 2f);
                    stats.getDeceleration().modifyFlat(id, CRYPULSE_SPD_CSR * 2f);
                } else if (hullSize == HullSize.CAPITAL_SHIP) {
                    //stats.getMaxSpeed().modifyFlat(id, SPEED_CAPITAL);
                    //stats.getAcceleration().modifyFlat(id, SPEED_CAPITAL * 2f);
                    //stats.getDeceleration().modifyFlat(id, SPEED_CAPITAL * 2f);
                    
                    stats.getWeaponRangeThreshold().modifyFlat(id, CRYPULSE_RNG_THRESH_CAPITAL);
                }
                
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyMult(id, PIERCE_MULT);
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
                bullet = tooltip.addPara("Shield Damage taken %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) SHIELD_BONUS + "%" );
                bullet = tooltip.addPara("EMP arc resistance %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) (100 - (PIERCE_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Top Speed %s, depending on hull size, no bonus for Capitals.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + (int) (CRYPULSE_SPD_FRI) + "/" + (int) (CRYPULSE_SPD_DES) + "/" + (int) (CRYPULSE_SPD_CSR) + "/");
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
                    "" + (int) (100 - (CRYPULSE_RANGE_MULT * 100)) + "%", Misc.getRoundedValue(CRYPULSE_RNG_THRESH), Misc.getRoundedValue(CRYPULSE_RNG_THRESH_CAPITAL) );
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
		if (index == 0) return "most";
                if (index == 1) return "" + (int) (CRYPULSE_SPD_FRI) + "/" + (int) (CRYPULSE_SPD_DES) + "/" + (int) (CRYPULSE_SPD_CSR);// + "/" + (int) (SPEED_CAPITAL);
		if (index == 2) return Misc.getRoundedValue(CRYPULSE_RNG_THRESH);
		if (index == 3) return Misc.getRoundedValue(CRYPULSE_RNG_THRESH_CAPITAL);
		if (index == 4) return "" + (int) (100 - (CRYPULSE_RANGE_MULT * 100)) + "%";
		return null;
	}

        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            super.applyEffectsAfterShipCreation(ship, id);
        }
    
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return true;
		
	}
	
        @Override
	public String getUnapplicableReason(ShipAPI ship) {
            return null;
	}
	

	private final Color color = new Color(40, 255, 40, 75);
	private final Color color2 = new Color(220,20,20,255);
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
            if (ship.getShield() == null) {
                
            } else {
                ship.getShield().setRingColor(color);
                ship.getShield().setInnerColor(color2);
            }
			ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.8f);
			ship.getEngineController().extendFlame(this, 0.1f, 0.1f, 0.1f);
	}
}
