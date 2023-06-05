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
import data.hullmods.ocua_core_quantix;
import data.scripts.ids.OCUA_HullMods;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ocua_core_mi_crystix extends ocua_core_quantix {
    
	public static final float PIERCE_MULT = 0.75f;
	public static final float SHIELD_BONUS = 20f; //15f;
	public static final float RANGE_BONUS = 10f;
	public static final float SPEED_MULT = 20f;
	public static final float TURN_MULT = 0.67f;
	
	public static final float OCUA_SHIELD_BONUS = 50f;
	public static final float OCUA_RANGE_BONUS = 25f;
	public static final float FIGHTER_SHIELD_BONUS = 33f;
	public static final float FIGHTER_RANGE_BONUS = 10f;
        
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
		float reedi = (float) ((100 - (stats.getNumFighterBays().getBaseValue() * READINESS_MULT_PER_DECK)) / 100);
		stats.getPeakCRDuration().modifyMult(id, reedi);
            if (!stats.getVariant().getHullSpec().getBuiltInMods().contains(OCUA_HullMods.OCUA_DULCENA_ARRAY)) {
		stats.getZeroFluxSpeedBoost().modifyMult(id, 0);
            }
                
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
        public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
            MutableShipStatsAPI stats = fighter.getMutableStats();

                if (stats.getVariant().getHullSpec().getHullId().startsWith("ocua_")){
                    stats.getShieldDamageTakenMult().modifyMult(id, 1f - OCUA_SHIELD_BONUS * 0.01f);
                    stats.getEnergyWeaponRangeBonus().modifyPercent(id, OCUA_RANGE_BONUS);
                } else {
                    stats.getShieldDamageTakenMult().modifyMult(id, 1f - FIGHTER_SHIELD_BONUS * 0.01f);
                    stats.getEnergyWeaponRangeBonus().modifyPercent(id, FIGHTER_RANGE_BONUS);
                }
        }

        
        protected static final float LOAD_OF_BULL = 3f;
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                Color good = Misc.getPositiveHighlightColor();
                Color ocua = new Color(250,100,175,255);
		
                LabelAPI bullet;
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Shield Damage taken %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) SHIELD_BONUS + "%" );
                bullet = tooltip.addPara("EMP arc resistance %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) (100 - (PIERCE_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Energy weapon range %s (additive).", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) RANGE_BONUS + "%" );
                bullet = tooltip.addPara("Hangar bay %s, destroyers and up.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) CARRIER_HANGAR + " slot" );
                bullet = tooltip.addPara("Fighter/Bomber refit time %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (100 *(CARRIER_REFIT))) + "%" );
                bullet = tooltip.addPara("Replacement recovery rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((100 * (CARRIER_REPLACEMENT_GAIN)) - 100) + "%" );
                bullet = tooltip.addPara("Replacement degredation rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (100 * (CARRIER_REPLACEMENT_LOSS))) + "%" );
                bullet = tooltip.addPara("Shield Damage taken for Oculian Fighters/Bombers %s. (%s for non-Oculian LPCs)", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) OCUA_SHIELD_BONUS + "%" , "-" + (int) FIGHTER_SHIELD_BONUS + "%" );
		bullet.setHighlight("Oculian", "-" + (int) OCUA_SHIELD_BONUS + "%", "-" + (int) FIGHTER_SHIELD_BONUS + "%");
		bullet.setHighlightColors(ocua, good, good);
                bullet = tooltip.addPara("Oculian Fighter/Bomber range %s (additive). (%s for non-Oculian LPCs)", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) OCUA_RANGE_BONUS + "%" , "-" + (int) FIGHTER_RANGE_BONUS + "%" );
		bullet.setHighlight("Oculian", "+" + (int) OCUA_RANGE_BONUS + "%", "+" + (int) FIGHTER_RANGE_BONUS + "%");
		bullet.setHighlightColors(ocua, good, good);
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Peak Readiness time %s, %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) READINESS_MULT_PER_DECK + "%", "per deck" );
		bullet.setHighlight("-" + (int) READINESS_MULT_PER_DECK + "%", "per deck");
		bullet.setHighlightColors(bad, h);
                bullet = tooltip.addPara("Top Speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) SPEED_MULT + "%" );
                bullet = tooltip.addPara("Maneuverability %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (TURN_MULT * 100)) + "%" );
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
		if (index == 0) return "full";
		if (index == 1) return "" + (int) OCUA_SHIELD_BONUS + "%";
		if (index == 2) return "" + (int) FIGHTER_SHIELD_BONUS + "%";
		if (index == 3) return "" + (int) OCUA_RANGE_BONUS + "%";
		if (index == 4) return "" + (int) FIGHTER_RANGE_BONUS + "%";
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
                ship.getShield().setRingColor(color); //(new Color(255, 255, 255, 255));
            }
            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.getShield() == null) {
                } else {
                    fighter.getShield().setRingColor(color);
                }
            }
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
	
}
