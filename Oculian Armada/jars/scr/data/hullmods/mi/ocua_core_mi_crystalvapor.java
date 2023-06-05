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
import data.hullmods.ocua_core_vapor;
import java.awt.Color;

public class ocua_core_mi_crystalvapor extends ocua_core_vapor {

	public static final float PIERCE_MULT = 0.75f;
	public static final float SHIELD_BONUS = 20f;
	public static final float RANGE_BONUS = 10f;
	public static final float SPEED_MULT = 20f;
	public static final float TURN_MULT = 0.67f;
        
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMinCrewMod().modifyMult(id, CREW_MULT);
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLIES_MULT);
		stats.getSuppliesToRecover().modifyMult(id, SUPPLIES_MULT);
		stats.getSuppliesToRecover().modifyMult(id, 1f);
		stats.getFuelUseMod().modifyMult(id, FUEL_MULT);
		stats.getSensorProfile().modifyMult(id, PROFILE_MULT);
                
		stats.getEnergyRoFMult().modifyMult(id, RATE_PENALTY);
		stats.getFluxCapacity().modifyMult(id, 1f);
		stats.getFluxDissipation().modifyMult(id, 1f);
		stats.getMaxBurnLevel().modifyFlat(id, -BURN_PENALTY);
                
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyMult(id, PIERCE_MULT);	
                stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
                
		stats.getMaxSpeed().modifyPercent(id, -SPEED_MULT);
		//stats.getZeroFluxSpeedBoost().modifyMult(id, 1f / SPEED_MULT); //Zero Flux compensation, since somehow, MaxSpeed affects both on Mult
		stats.getAcceleration().modifyMult(id, TURN_MULT);
		stats.getMaxTurnRate().modifyMult(id, TURN_MULT);
		stats.getTurnAcceleration().modifyMult(id, TURN_MULT);
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
                bullet = tooltip.addPara("Energy weapon range %s (additive).", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) RANGE_BONUS + "%" );
                bullet = tooltip.addPara("Minimum Crew requirement %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (CREW_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Supply Cost for Maintenance/Repair %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (SUPPLIES_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Fuel cost per lightyear %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (FUEL_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Sensor Profile size %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (PROFILE_MULT * 100)) + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Top Speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) SPEED_MULT + "%" );
                bullet = tooltip.addPara("Maneuverability %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (TURN_MULT * 100)) + "%" );
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
		if (index == 0) return "full";
		if (index == 1) return "remove Flux penalties";
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
        
	private final Color color = new Color(40, 255, 40, 75);
        @Override
        public void advanceInCombat(ShipAPI ship, float amount) {
            if (ship.getShield() == null) {
                
            } else {
                ship.getShield().setRingColor(color);
            }
            ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
        }
}
