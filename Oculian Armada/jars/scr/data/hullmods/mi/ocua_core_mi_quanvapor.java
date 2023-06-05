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
import data.scripts.ids.OCUA_HullMods;
import java.awt.Color;

public class ocua_core_mi_quanvapor extends ocua_core_vapor {
	public static final float CARRIER_HANGAR = 1f;
	public static final float CARRIER_BONUS = 1.25f;
	public static final float CARRIER_REFIT = 0.75f;
	public static final float CARRIER_REPLACEMENT_GAIN = 1.2f;
	public static final float CARRIER_REPLACEMENT_LOSS = 0.67f;

	public static final float LOSS_PENALTY = 1.5f;
	public static final float READINESS_MULT_PER_DECK = 5f;
	public static final float QUANTIX_SUPPLIES_MULT = 1.1f;
        
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMinCrewMod().modifyMult(id, CREW_MULT);
		stats.getSuppliesPerMonth().modifyMult(id, QUANTIX_SUPPLIES_MULT);
		stats.getSuppliesToRecover().modifyMult(id, QUANTIX_SUPPLIES_MULT);
		stats.getFuelUseMod().modifyMult(id, FUEL_MULT);
		stats.getSensorProfile().modifyMult(id, PROFILE_MULT);
                
		stats.getEnergyRoFMult().modifyMult(id, RATE_PENALTY);
		stats.getFluxDissipation().modifyMult(id, FLUX_CAPACITY_MULT);
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
		stats.getMaxBurnLevel().modifyFlat(id, -BURN_PENALTY);
                
                if (!(stats.getVariant().getHullSpec().getBuiltInMods().contains("ocua_baseless_module") || (hullSize == HullSize.FRIGATE))) {
                    stats.getNumFighterBays().modifyFlat(id, CARRIER_HANGAR);
                }
        
                stats.getHangarSpaceMod().modifyMult(id, CARRIER_BONUS);
                stats.getFighterRefitTimeMult().modifyMult(id, CARRIER_REFIT);
                
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, CARRIER_REPLACEMENT_LOSS);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyMult(id, CARRIER_REPLACEMENT_GAIN);
                
		stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, LOSS_PENALTY);
                
		float reedi = (float) ((100 - (stats.getNumFighterBays().getBaseValue() * READINESS_MULT_PER_DECK)) / 100);
		stats.getPeakCRDuration().modifyMult(id, reedi);
            if (!stats.getVariant().getHullSpec().getBuiltInMods().contains(OCUA_HullMods.OCUA_DULCENA_ARRAY)) {
		stats.getZeroFluxSpeedBoost().modifyMult(id, 0.5f);
            }
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
		
                LabelAPI bullet;
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Hangar bay %s, destroyers and up.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) CARRIER_HANGAR + " slot" );
                bullet = tooltip.addPara("Fighter/Bomber refit time %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (100 *(CARRIER_REFIT))) + "%" );
                bullet = tooltip.addPara("Replacement recovery rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((100 * (CARRIER_REPLACEMENT_GAIN)) - 100) + "%" );
                bullet = tooltip.addPara("Replacement degredation rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (100 * (CARRIER_REPLACEMENT_LOSS))) + "%" );
                bullet = tooltip.addPara("Minimum Crew requirement %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (CREW_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Fuel cost per lightyear %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (FUEL_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Sensor Profile size %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (PROFILE_MULT * 100)) + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Peak Readiness time %s, %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) READINESS_MULT_PER_DECK + "%", "per deck" );
		bullet.setHighlight("-" + (int) READINESS_MULT_PER_DECK + "%", "per deck");
		bullet.setHighlightColors(bad, h);
                bullet = tooltip.addPara("Flux Dissipation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (FLUX_DISSIPATION_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Flux Capacity %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (FLUX_CAPACITY_MULT * 100)) + "%" );
                bullet = tooltip.addPara("Energy weapon firing speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 - (RATE_PENALTY * 100)) + "%" );
                bullet = tooltip.addPara("Burn Level %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) BURN_PENALTY + "" );
                bullet = tooltip.addPara("Crew loss from crewed wing losses %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((100 *(LOSS_PENALTY)) - 100) + "%" );
                bullet = tooltip.addPara("Supply Cost for Maintenance/Repair %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((QUANTIX_SUPPLIES_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("Zero-flux speed bonus is %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "halved" );
                
		tooltip.addSectionHeading("Compatibilities", Alignment.MID, opad);
                bullet = tooltip.addPara("Hullmod is %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "not considered a Logistics hullmod" );
                
            tooltip.setBulletedListMode(null);
	}
	
    
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "full";
		if (index == 1) return "half";
		if (index == 2) return "" + (int) ((QUANTIX_SUPPLIES_MULT * 100) - 100) + "%";
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
}
