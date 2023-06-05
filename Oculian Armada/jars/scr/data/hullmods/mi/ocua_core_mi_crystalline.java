package data.hullmods.mi;

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
import java.awt.Color;

public class ocua_core_mi_crystalline extends BaseHullMod {

	public static final float PIERCE_MULT = 0.65f;
	public static final float SHIELD_BONUS = 25f;
	public static final float RANGE_BONUS = 15f;
	public static final float SPEED_MULT = 25f;
	public static final float TURN_MULT = 0.67f;
        
	public static final float DAMAGE_MULT = 10f;
	public static final float FLUX_MULT = 10f;
	public static final float RATE_PENALTY = 10f;
	
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
                
		stats.getEnergyWeaponDamageMult().modifyMult(id, (1 + (DAMAGE_MULT / 100)));
		stats.getBeamWeaponDamageMult().modifyMult(id, (1 + (DAMAGE_MULT / 100)));
                
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (FLUX_MULT / 100)));
                stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (FLUX_MULT / 100)));
                
                stats.getEnergyRoFMult().modifyMult(id, (1 - (RATE_PENALTY / 100)));
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
                bullet = tooltip.addPara("Energy and Beam damage %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) DAMAGE_MULT + "%" );
                bullet = tooltip.addPara("Energy and Beam flux generation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) FLUX_MULT + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Top Speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) SPEED_MULT + "%" );
                bullet = tooltip.addPara("Maneuverability %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (TURN_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Energy and Missile Firing Speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) RATE_PENALTY + "%" );
                
            tooltip.setBulletedListMode(null);
	}
	
    
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SHIELD_BONUS + "%";
		if (index == 1) return "" + (int) (100 - (PIERCE_MULT * 100)) + "%";
                if (index == 2) return "" + (int) (RANGE_BONUS) + "%";
		if (index == 3) return "" + (int) DAMAGE_MULT + "%";
		if (index == 4) return "" + (int) FLUX_MULT + "%";
		if (index == 5) return "" + (int) RATE_PENALTY + "%";
		if (index == 6) return "" + (int) (100 - (SPEED_MULT * 100)) + "%";
		if (index == 7) return "" + (int) (100 - (TURN_MULT * 100)) + "%";
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
        
	private final Color color = new Color(40, 255, 40, 75);
	private final Color color2 = new Color(120, 255, 120, 85);
        @Override
        public void advanceInCombat(ShipAPI ship, float amount) {
            if (ship.getShield() == null) {
                
            } else {
                ship.getShield().setRingColor(color);
                ship.getShield().setInnerColor(color2);
            }
            ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
        }
        
}
