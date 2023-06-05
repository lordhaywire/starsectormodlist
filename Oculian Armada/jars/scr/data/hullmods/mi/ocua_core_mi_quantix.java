package data.hullmods.mi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ids.OCUA_HullMods;
import java.awt.Color;

public class ocua_core_mi_quantix extends BaseHullMod {
        public static final float WING_TIME = 86400f;
        
	public static final float CARRIER_HANGAR = 1f;
	public static final float CARRIER_BONUS = 1.33f;
	public static final float CARRIER_REFIT = 0.67f;
	public static final float CARRIER_REPLACEMENT_GAIN = 1.3f;
	public static final float CARRIER_REPLACEMENT_LOSS = 0.5f;

	public static final float LOSS_PENALTY = 1.33f;
	public static final float READINESS_MULT_PER_DECK = 6f;
	public static final float SUPPLIES_MULT = 1.5f;

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
		//int reedi = (int) (stats.getNumFighterBays().getBaseValue() * READINESS_MULT_PER_DECK);
		float reedi = (float) ((100 - (stats.getNumFighterBays().getBaseValue() * READINESS_MULT_PER_DECK)) / 100);
		stats.getPeakCRDuration().modifyMult(id, reedi);
            if (!stats.getVariant().getHullSpec().getBuiltInMods().contains(OCUA_HullMods.OCUA_DULCENA_ARRAY)) {
		stats.getZeroFluxSpeedBoost().modifyMult(id, 0);
            }
        }

        //The code has to run every frame to work properly. Also, i have the distinct feeling it will not work on a ship with Reserve Deployments.
        @Override
        public void advanceInCombat(ShipAPI ship, float amount) {
            if (ship != null) {
                for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
                    if (bay.getWing() == null) continue;

                    //bay.makeCurrentIntervalFast();
                    FighterWingSpecAPI wing = bay.getWing().getSpec();

                    //Only affects drones: delete this if you want it to affect fighters with crew, too
                    if (wing.getVariant().getHullSpec().getMinCrew() != 0) continue;
                    if (!wing.getVariant().getHullSpec().getHullId().startsWith("ocua_")) continue;

                    //The actual code which adds fighters: slightly differs from the vanilla Reserve Deployments, due to it not deploying extra fighters faster than normal
                    int addForWing = getAdditionalFor(wing);
                    int maxTotal = wing.getNumFighters() + addForWing;
                    int actualAdd = maxTotal - bay.getWing().getWingMembers().size();
                    if (actualAdd > 0) {
                        bay.setExtraDeployments(actualAdd);
                        bay.setExtraDeploymentLimit(maxTotal);
                        bay.setExtraDuration(WING_TIME);
                    }
                }
            }
        }
        
	public static int getAdditionalFor(FighterWingSpecAPI spec) {
		int size = spec.getNumFighters();
		if (size == 6) return 2;
                else if (size >= 3) return 1;
		return 0;
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
                bullet = tooltip.addPara("Every Oculian wing get %s in a squadron.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "1 additional unit for every 3 units" );
		bullet.setHighlight("Oculian", "1 additional unit for every 3 units" );
		bullet.setHighlightColors(ocua, good);
                bullet = tooltip.addPara("Fighter/Bomber refit time %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (100 *(CARRIER_REFIT))) + "%" );
                bullet = tooltip.addPara("Replacement recovery rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((100 * (CARRIER_REPLACEMENT_GAIN)) - 100) + "%" );
                bullet = tooltip.addPara("Replacement degredation rate %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) (100 - (100 * (CARRIER_REPLACEMENT_LOSS))) + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Peak Readiness time %s, %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) READINESS_MULT_PER_DECK + "%", "per deck" );
		bullet.setHighlight("-" + (int) READINESS_MULT_PER_DECK + "%", "per deck");
		bullet.setHighlightColors(bad, h);
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
		if (index == 0) return "" + (int) CARRIER_HANGAR;
                if (index == 1) return "1 fighter for every three";
		if (index == 2) return "" + (int) (100 - (100 *(CARRIER_REFIT))) + "%";
		if (index == 3) return "" + (int) (100 - (100 *(CARRIER_REPLACEMENT_LOSS))) + "%";
		if (index == 4) return "" + (int) ((100 *(CARRIER_REPLACEMENT_GAIN)) - 100) + "%";
		if (index == 5) return "" + (int) READINESS_MULT_PER_DECK + "%";
		if (index == 6) return "prevents Zero-flux speed bonus";
		if (index == 7) return "" + (int) ((100 *(LOSS_PENALTY)) - 100) + "%";
		if (index == 8) return "" + (int) ((SUPPLIES_MULT * 100) - 100) + "%";
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
