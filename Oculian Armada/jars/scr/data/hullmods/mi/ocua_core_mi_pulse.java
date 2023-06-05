package data.hullmods.mi;

import com.fs.starfarer.api.Global;
import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.ArrayList;
import java.util.List;

public class ocua_core_mi_pulse extends BaseHullMod {
        private static final String ocua_id = "mi_pulseID";

	public static final float MAX_TIME_MULT = 1.25f;
	public static final float MIN_TIME_MULT = 0.1f;
	
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
	public static final float FLUX_DISSIPATION_MULT = 2.5f;
	
	public static final float CARRIER_GAIN = 0.67f;
	public static final float CARRIER_LOSS = 1.5f;
	public static final float RANGE_THRESHOLD = 500f;
	public static final float RANGE_THRESHOLD_CAPITAL = 700f;
	public static final float RANGE_MULT = 0.25f;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String ocua_id) {
                if (hullSize != HullSize.CAPITAL_SHIP) {
                    stats.getOverloadTimeMod().modifyMult(ocua_id, OVERLOAD_MULT);
                    
                    stats.getWeaponRangeThreshold().modifyFlat(ocua_id, RANGE_THRESHOLD);
                }
                
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(ocua_id, 2f); // set to two, meaning boost is always on 
		stats.getFluxDissipation().modifyMult(ocua_id, FLUX_DISSIPATION_MULT);
                stats.getEnergyRoFMult().modifyMult(ocua_id, RATE_BONUS);
                stats.getMissileRoFMult().modifyMult(ocua_id, RATE_BONUS);
                stats.getBeamWeaponDamageMult().modifyMult(ocua_id, RATE_BONUS);
                
		stats.getPeakCRDuration().modifyMult(ocua_id, PEAK_MULT);
		stats.getVentRateMult().modifyMult(ocua_id, 0f);
		stats.getWeaponRangeMultPastThreshold().modifyMult(ocua_id, RANGE_MULT);
                //stats.getFighterRefitTimeMult().modifyMult(ocua_id, CARRIER_LOSS);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyMult(ocua_id, CARRIER_GAIN);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(ocua_id, CARRIER_LOSS);
                
                if (hullSize == HullSize.FIGHTER) {
                    stats.getMaxSpeed().modifyFlat(ocua_id, SPEED_FRIGATE);
                    stats.getAcceleration().modifyFlat(ocua_id, SPEED_FRIGATE * 2f);
                    stats.getDeceleration().modifyFlat(ocua_id, SPEED_FRIGATE * 2f);
                } else if (hullSize == HullSize.FRIGATE) {
                    stats.getMaxSpeed().modifyFlat(ocua_id, SPEED_FRIGATE);
                    stats.getAcceleration().modifyFlat(ocua_id, SPEED_FRIGATE * 2f);
                    stats.getDeceleration().modifyFlat(ocua_id, SPEED_FRIGATE * 2f);
                } else if (hullSize == HullSize.DESTROYER) {
                    stats.getMaxSpeed().modifyFlat(ocua_id, SPEED_DESTROYER);
                    stats.getAcceleration().modifyFlat(ocua_id, SPEED_DESTROYER * 2f);
                    stats.getDeceleration().modifyFlat(ocua_id, SPEED_DESTROYER * 2f);
                } else if (hullSize == HullSize.CRUISER) {
                    stats.getMaxSpeed().modifyFlat(ocua_id, SPEED_CRUISER);
                    stats.getAcceleration().modifyFlat(ocua_id, SPEED_CRUISER * 2f);
                    stats.getDeceleration().modifyFlat(ocua_id, SPEED_CRUISER * 2f);
                } else if (hullSize == HullSize.CAPITAL_SHIP) {
                    stats.getMaxSpeed().modifyFlat(ocua_id, SPEED_CAPITAL);
                    stats.getAcceleration().modifyFlat(ocua_id, SPEED_CAPITAL * 2f);
                    stats.getDeceleration().modifyFlat(ocua_id, SPEED_CAPITAL * 2f);
                    
                    stats.getWeaponRangeThreshold().modifyFlat(ocua_id, RANGE_THRESHOLD_CAPITAL);
                }
	}
	
	

	private final Color color = new Color(220,20,20,255);
        @Override
        public void advanceInCombat(ShipAPI ship, float amount) {
            if (ship.getShield() == null) {
                
            } else {
                ship.getShield().setRingColor(color);
                ship.getShield().setInnerColor(color);
            }
		ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.8f);
		ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
                
                if (Global.getCombatEngine().isPaused()) return;
		boolean player = ship == Global.getCombatEngine().getPlayerShip();
                if (!ship.isAlive() || ship.isPiece()) return;
		
                if (player) { //Overclock magic
                    ship.getMutableStats().getTimeMult().modifyMult(ocua_id, MAX_TIME_MULT);
                    Global.getCombatEngine().getTimeMult().modifyMult(ocua_id, 1f / MAX_TIME_MULT);
                    Global.getCombatEngine().maintainStatusForPlayerShip(ocua_id, "graphics/icons/hullsys/temporal_shell.png", "Mikanate Pulse", "1.25x Timeflow", false);
                } else {
                    ship.getMutableStats().getTimeMult().modifyPercent(ocua_id, MAX_TIME_MULT);
                    Global.getCombatEngine().getTimeMult().unmodify(ocua_id);
                }
                
		for (ShipAPI fighter : getFighters(ship)) {
			if (fighter.isHulk()) continue;
			if (!fighter.isAlive()) return;
                                
			MutableShipStatsAPI fStats = fighter.getMutableStats();
                        fStats.getTimeMult().modifyMult(ocua_id, MAX_TIME_MULT);
		}
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
                bullet = tooltip.addPara("Time Flow %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + (float) MAX_TIME_MULT + "x" );
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
                if (index == 0) return "" + (float) MAX_TIME_MULT + "x";
                if (index == 1) return "" + (int) (SPEED_FRIGATE) + "/" + (int) (SPEED_DESTROYER) + "/" + (int) (SPEED_CRUISER) + "/" + (int) (SPEED_CAPITAL);
		if (index == 2) return Misc.getRoundedValue(FLUX_DISSIPATION_MULT);
		if (index == 3) return "" + (int) ((RATE_BONUS * 100) - 100) + "%";
		if (index == 4) return "" + (int) ((OVERLOAD_MULT * 100) - 100) + "%";
		if (index == 5) return "4";
		if (index == 6) return Misc.getRoundedValue(RANGE_THRESHOLD);
		if (index == 7) return Misc.getRoundedValue(RANGE_THRESHOLD_CAPITAL);
		if (index == 8) return "" + (int) (100 - (RANGE_MULT * 100)) + "%";
		if (index == 9) return "" + (int) ((CARRIER_LOSS * 100) - 100) + "%";
		return null;
	}

	public static List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<ShipAPI>();
		
		for (ShipAPI ship : Global.getCombatEngine().getShips()) {
			if (!ship.isFighter()) continue;
			if (ship.getWing() == null) continue;
			if (ship.getWing().getSourceShip() == carrier) {
				result.add(ship);
			}
		}
		
		return result;
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
}
