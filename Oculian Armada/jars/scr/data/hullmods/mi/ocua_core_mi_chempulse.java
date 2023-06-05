package data.hullmods.mi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.ocua_core_pulse;
import java.awt.Color;

public class ocua_core_mi_chempulse extends ocua_core_pulse {
	public static final float DAMAGE_MULT = 1.15f;
	public static final float BEAM_DAMAGE_MULT = 1.65f;
	public static final float FLUX_MULT = 15f;
	public static final float SUPPLIES_MULT = 1.25f;
	public static final float REPAIR_MULT = 1.67f;
	public static final float DEATH_RANGE_MULT = 1.25f;
	public static final float DEATH_DAMAGE_MULT = 1.5f;
        
	public static final float FLUX_CAPACITY_MULT = 1.25f; //1.5f;
	public static final float SHIELD_PENALTY = 33f; //15f;
        
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
                
		stats.getPeakCRDuration().modifyMult(id, PEAK_MULT);
		stats.getVentRateMult().modifyMult(id, 0f);
		stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT);
                stats.getFighterRefitTimeMult().modifyMult(id, CARRIER_LOSS);
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
                
		stats.getEnergyWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (FLUX_MULT / 100)));
		stats.getBeamWeaponDamageMult().modifyMult(id, BEAM_DAMAGE_MULT);
                stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (FLUX_MULT / 100)));
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLIES_MULT);
		stats.getSuppliesToRecover().modifyMult(id, REPAIR_MULT);
		stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, DEATH_RANGE_MULT);
		stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, DEATH_DAMAGE_MULT);
                
		stats.getFluxCapacity().modifyMult(id, FLUX_CAPACITY_MULT);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f + SHIELD_PENALTY * 0.01f);
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
                bullet = tooltip.addPara("Top Speed %s, depending on hull size.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + (int) (SPEED_FRIGATE) + "/" + (int) (SPEED_DESTROYER) + "/" + (int) (SPEED_CRUISER) + "/" + (int) (SPEED_CAPITAL) );
                bullet = tooltip.addPara("Flux Dissipation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + Misc.getRoundedValue(FLUX_DISSIPATION_MULT) + "x" );
                bullet = tooltip.addPara("Flux Capacity %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + Misc.getRoundedValue(FLUX_CAPACITY_MULT) + "x" );
                bullet = tooltip.addPara("Energy and Beam damage %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((DAMAGE_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("Energy and Missile Firing Speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((RATE_BONUS * 100) - 100) + "%" );
                bullet = tooltip.addPara("Energy and Beam flux generation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) FLUX_MULT + "%" );
                bullet = tooltip.addPara("Overload time %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (OVERLOAD_MULT * 100)) + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Shield Damage taken %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) (int) SHIELD_PENALTY + "%" );
                bullet = tooltip.addPara("Peak Performance time %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "reduced by a factor of 4" );
                bullet = tooltip.addPara("Weapon range reduced by %s after the threshold of %s units (%s units for Capitals).", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "" + (int) (100 - (RANGE_MULT * 100)) + "%", Misc.getRoundedValue(RANGE_THRESHOLD), Misc.getRoundedValue(RANGE_THRESHOLD_CAPITAL) );
                bullet = tooltip.addPara("Replacement recovery rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (CARRIER_GAIN * 100)) + "%" );
                bullet = tooltip.addPara("Replacement degredation rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((CARRIER_LOSS * 100) - 100) + "%" );
                bullet = tooltip.addPara("Supply Cost for Maintenance %s and for Repair %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((SUPPLIES_MULT * 100) - 100) + "%", "+" + (int) ((REPAIR_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("%s larger explosion upon death.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((DEATH_DAMAGE_MULT * 100) - 100) + "%" );
                
		tooltip.addSectionHeading("Compatibilities", Alignment.MID, opad);
                bullet = tooltip.addPara("Can be installed on %s ships.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "Capital" );
                bullet = tooltip.addPara("Hullmod cannot be installed on Civilian ships, unless %s is installed.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "Militarized Subsystems" );
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "full";
		
		if (index == 1) return "" + Misc.getRoundedValue(FLUX_CAPACITY_MULT) + "";
		if (index == 2) return "" + (int) SHIELD_PENALTY + "%";
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
