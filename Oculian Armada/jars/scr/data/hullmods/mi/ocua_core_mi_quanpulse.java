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
import data.scripts.ids.OCUA_HullMods;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ocua_core_mi_quanpulse extends ocua_core_pulse {
	public static final float CARRIER_HANGAR = 1f;
	public static final float CARRIER_BONUS = 1.25f;
	public static final float CARRIER_REFIT = 0.75f;
	public static final float CARRIER_REPLACEMENT_GAIN = 1.2f;
	public static final float CARRIER_REPLACEMENT_LOSS = 0.67f;

	public static final float LOSS_PENALTY = 1.5f;
	public static final float READINESS_MULT_PER_DECK = 5f;
	public static final float SUPPLIES_MULT = 1.33f;

        //private static final boolean baseless_module = ship_unit.contains("ocua_baseless_module");
            
        @Override
        public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f); // set to two, meaning boost is always on 
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
                stats.getEnergyRoFMult().modifyMult(id, RATE_BONUS);
                stats.getMissileRoFMult().modifyMult(id, RATE_BONUS);
                stats.getBeamWeaponDamageMult().modifyMult(id, RATE_BONUS);
                
		//float reedi = (float) ((100 - (stats.getNumFighterBays().getBaseValue() * READINESS_MULT_PER_DECK)) / 100);
		stats.getPeakCRDuration().modifyMult(id, PEAK_MULT);
		stats.getVentRateMult().modifyMult(id, 0f);
		stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT);
                
                stats.getHangarSpaceMod().modifyMult(id, CARRIER_BONUS);
                stats.getFighterRefitTimeMult().modifyMult(id, 1f);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, 1f);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyMult(id, 1f);
                
                
                
		stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, LOSS_PENALTY);
                
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLIES_MULT);
		stats.getSuppliesToRecover().modifyMult(id, SUPPLIES_MULT);
            if (!stats.getVariant().getHullSpec().getBuiltInMods().contains(OCUA_HullMods.OCUA_DULCENA_ARRAY)) {
		stats.getZeroFluxSpeedBoost().modifyMult(id, 0);
            }
                
                if (hullSize != HullSize.CAPITAL_SHIP) {
                    stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_MULT);
                    
                    stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD);
                }
                if (!(stats.getVariant().getHullSpec().getBuiltInMods().contains("ocua_baseless_module") || (hullSize == HullSize.FRIGATE))) {
                        stats.getNumFighterBays().modifyFlat(id, CARRIER_HANGAR);
                }
                if (hullSize == HullSize.FIGHTER) {
                    stats.getMaxSpeed().modifyFlat(id, SPEED_FRIGATE);
                    stats.getAcceleration().modifyFlat(id, SPEED_FRIGATE * 2f);
                    stats.getDeceleration().modifyFlat(id, SPEED_FRIGATE * 2f);
                } else if (hullSize == HullSize.FRIGATE) {
                    stats.getMaxSpeed().modifyFlat(id, SPEED_FRIGATE);
                    stats.getAcceleration().modifyFlat(id, SPEED_FRIGATE * 2f);
                    stats.getDeceleration().modifyFlat(id, SPEED_FRIGATE * 2f);
                } else if (hullSize == HullSize.DESTROYER) {
                    stats.getMaxSpeed().modifyFlat(id, SPEED_DESTROYER);
                    stats.getAcceleration().modifyFlat(id, SPEED_DESTROYER * 2f);
                    stats.getDeceleration().modifyFlat(id, SPEED_DESTROYER * 2f);
                } else if (hullSize == HullSize.CRUISER) {
                    stats.getMaxSpeed().modifyFlat(id, SPEED_CRUISER);
                    stats.getAcceleration().modifyFlat(id, SPEED_CRUISER * 2f);
                    stats.getDeceleration().modifyFlat(id, SPEED_CRUISER * 2f);
                } else if (hullSize == HullSize.CAPITAL_SHIP) {
                    stats.getMaxSpeed().modifyFlat(id, SPEED_CAPITAL);
                    stats.getAcceleration().modifyFlat(id, SPEED_CAPITAL * 2f);
                    stats.getDeceleration().modifyFlat(id, SPEED_CAPITAL * 2f);
                    
                    stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD_CAPITAL);
                }
        }

        @Override
        public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
            MutableShipStatsAPI stats = fighter.getMutableStats();

                stats.getMaxSpeed().modifyFlat(id, SPEED_FRIGATE);
                stats.getAcceleration().modifyFlat(id, SPEED_FRIGATE * 2f);
                stats.getDeceleration().modifyFlat(id, SPEED_FRIGATE * 2f);
                
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
                stats.getEnergyRoFMult().modifyMult(id, RATE_BONUS);
                stats.getMissileRoFMult().modifyMult(id, RATE_BONUS);
                stats.getBeamWeaponDamageMult().modifyMult(id, RATE_BONUS);
        }

        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            super.applyEffectsAfterShipCreation(ship, id);
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
                bullet = tooltip.addPara("Hangar bay %s, destroyers and up.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) CARRIER_HANGAR + " slot" );
                bullet = tooltip.addPara("Top Speed %s, depending on hull size.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + (int) (SPEED_FRIGATE) + "/" + (int) (SPEED_DESTROYER) + "/" + (int) (SPEED_CRUISER) + "/" + (int) (SPEED_CAPITAL) );
                bullet = tooltip.addPara("Flux Dissipation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + Misc.getRoundedValue(FLUX_DISSIPATION_MULT) + "x" );
                bullet = tooltip.addPara("Energy and Missile Firing Speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((RATE_BONUS * 100) - 100) + "%" );
                bullet = tooltip.addPara("Overload time %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (OVERLOAD_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Oculian wings get %s bonus while also receiving %s bonuses.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "frigate top speed", "Pulse dissipation and firing speed" );
		bullet.setHighlight("Oculian", "frigate top speed", "Pulse dissipation and firing speed" );
		bullet.setHighlightColors(ocua, good, good);
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Peak Performance time %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "reduced by a factor of 4" );
                bullet = tooltip.addPara("Weapon range reduced by %s after the threshold of %s units (%s units for Capitals).", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "" + (int) (100 - (RANGE_MULT * 100)) + "%", Misc.getRoundedValue(RANGE_THRESHOLD), Misc.getRoundedValue(RANGE_THRESHOLD_CAPITAL) );
                bullet = tooltip.addPara("Zero-flux speed bonus is %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "prevented" );
                bullet = tooltip.addPara("Crew loss from crewed wing losses %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((100 *(LOSS_PENALTY)) - 100) + "%" );
                bullet = tooltip.addPara("Supply Cost for Maintenance/Repair %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((SUPPLIES_MULT * 100) - 100) + "%" );
                
		tooltip.addSectionHeading("Compatibilities", Alignment.MID, opad);
                bullet = tooltip.addPara("Can be installed on %s ships.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "Capital" );
                bullet = tooltip.addPara("Hullmod cannot be installed on Civilian ships, unless %s is installed.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "Militarized Subsystems" );
                
            tooltip.setBulletedListMode(null);
	}
    
        @Override
        public String getDescriptionParam(int index, HullSize hullSize) {
		if(index == 0) return "most";
		if(index == 1) return "normalized";
		if(index == 2) return "frigate-level Pulse core drives";
		if(index == 3) return "Pulse-core Peak readiness loss";
            else {
                return null;
            }
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
	private final Color color2 = new Color(70, 255, 70, 75);
        @Override
        public void advanceInCombat(ShipAPI ship, float amount) {
            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.getShield() == null) {
                } else {
                    fighter.getShield().setRingColor(color);
                    fighter.getShield().setInnerColor(color2);
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
