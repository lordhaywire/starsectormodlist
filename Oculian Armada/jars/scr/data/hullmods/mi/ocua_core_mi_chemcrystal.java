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
import data.hullmods.ocua_core_crystalline;
import java.awt.Color;

public class ocua_core_mi_chemcrystal extends ocua_core_crystalline {

	public static final float DAMAGE_MULT = 1.15f; //1.1f;
	public static final float FLUX_MULT = 15f;
	public static final float SUPPLIES_MULT = 1.25f;
	public static final float REPAIR_MULT = 1.67f;
	public static final float DEATH_RANGE_MULT = 1.25f;
	public static final float DEATH_DAMAGE_MULT = 1.5f;
	
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
		Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                Color good = Misc.getPositiveHighlightColor();
		
                LabelAPI bullet;
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Energy and Beam damage %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((DAMAGE_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("Energy and Beam flux generation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) FLUX_MULT + "%" );
                bullet = tooltip.addPara("Shield Damage taken %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) SHIELD_BONUS + "%" );
                bullet = tooltip.addPara("EMP arc resistance %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) (100 - (PIERCE_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Energy weapon range %s (additive).", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) RANGE_BONUS + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Supply Cost for Maintenance %s and for Repair %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((SUPPLIES_MULT * 100) - 100) + "%", "+" + (int) ((REPAIR_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("Top Speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) SPEED_MULT + "%" );
                bullet = tooltip.addPara("Maneuverability %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (TURN_MULT * 100)) + "%" );
                bullet = tooltip.addPara("%s larger explosion upon death.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((DEATH_DAMAGE_MULT * 100) - 100) + "%" );
                
            tooltip.setBulletedListMode(null);
	}
        
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "full";
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
