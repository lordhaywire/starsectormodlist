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

public class ocua_core_mi_pulsevapor extends BaseHullMod {

	public static final float DAMAGE_MULT = 1.1f;
	public static final float RATE_BONUS = 1.1f;
	public static final float ENERGY_FLUX_MULT = 10f;
	public static final float RANGE_BONUS = 10f;
	public static final float FLUX_DISSIPATION_MULT = 1.25f;
	public static final float FLUX_CAPACITY_MULT = 1.1f;
        
	public static final float SHIELD_BONUS = 15f;
	public static final float PIERCE_MULT = 0.85f;
        
	public static final float REPLACE_GAIN = 1.1f;
	public static final float REPLACE_LOSS = 0.9f;
	public static final float ZERO_FLUX_FLAT = 10f;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (ENERGY_FLUX_MULT / 100)));
		stats.getBeamWeaponDamageMult().modifyMult(id, ((DAMAGE_MULT + RATE_BONUS) - 1));
                stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (ENERGY_FLUX_MULT / 100)));
                stats.getEnergyRoFMult().modifyMult(id, RATE_BONUS);
                stats.getMissileRoFMult().modifyMult(id, RATE_BONUS);
                stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
                
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
		stats.getFluxCapacity().modifyMult(id, FLUX_CAPACITY_MULT);
                
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyMult(id, PIERCE_MULT);
		stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_FLAT);
                
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyMult(id, REPLACE_GAIN);
                stats.getHangarSpaceMod().modifyMult(id, REPLACE_GAIN);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, REPLACE_LOSS);
                stats.getFighterRefitTimeMult().modifyMult(id, REPLACE_LOSS);
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
                bullet = tooltip.addPara("Energy and Missile Firing Speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((RATE_BONUS * 100) - 100) + "%" );
                bullet = tooltip.addPara("Energy and Beam flux generation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) ENERGY_FLUX_MULT + "%" );
                bullet = tooltip.addPara("Energy weapon range %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) RANGE_BONUS + "%" );
                bullet = tooltip.addPara("Flux Dissipation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + Misc.getRoundedValue(FLUX_DISSIPATION_MULT) + "x" );
                bullet = tooltip.addPara("Flux Capacity %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + Misc.getRoundedValue(FLUX_CAPACITY_MULT) + "x" );
                bullet = tooltip.addPara("Shield Damage taken %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) SHIELD_BONUS + "%" );
                bullet = tooltip.addPara("EMP arc resistance %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) (100 - (PIERCE_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Replacement recovery rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((100 * (REPLACE_GAIN)) - 100) + "%" );
                bullet = tooltip.addPara("Replacement degredation rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (100 * (REPLACE_LOSS))) + "%" );
                bullet = tooltip.addPara("Zero Flux Speed bonus %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ZERO_FLUX_FLAT + " units" );
                
            tooltip.setBulletedListMode(null);
	}
	
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((DAMAGE_MULT * 100) - 100) + "%";
		if (index == 1) return "" + (int) ((RATE_BONUS * 100) - 100) + "%";
		if (index == 2) return "" + (int) (ENERGY_FLUX_MULT) + "%";
		if (index == 3) return "" + (int) (RANGE_BONUS) + "%";
		if (index == 4) return "" + (float) FLUX_DISSIPATION_MULT + "x";
		if (index == 5) return "" + (float) FLUX_CAPACITY_MULT + "x";
		if (index == 6) return "" + (int) SHIELD_BONUS + "%";
		if (index == 7) return "" + (int) (100 - (PIERCE_MULT * 100)) + "%";
		if (index == 8) return "10%";
		if (index == 9) return "" + (int) ZERO_FLUX_FLAT;
		return null;
	}

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
		return true;
        }
    
        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            super.applyEffectsAfterShipCreation(ship, id);
        }
    
        @Override
        public String getUnapplicableReason(ShipAPI ship) {
            return null;
        }
        
	private final Color color = new Color(220,20,20,255);
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
            if (ship.getShield() == null) {
                
            } else {
                ship.getShield().setRingColor(color); //(new Color(255, 255, 255, 255));
            }
			ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.8f);
			ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
	}
}
