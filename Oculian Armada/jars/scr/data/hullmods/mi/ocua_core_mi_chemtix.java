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

public class ocua_core_mi_chemtix extends ocua_core_quantix {
    
	public static final float DAMAGE_MULT = 1.15f; //1.1f;
	public static final float WING_DAMAGE_MULT = 1.25f;
	public static final float FLUX_MULT = 15f;
	public static final float CHEM_SUPPLIES_MULT = 1.5f;
	public static final float CHEM_REC_SUPPLIES_MULT = 1.75f;
	public static final float REPAIR_MULT = 1.75f;
	public static final float DEATH_RANGE_MULT = 1.25f;
	public static final float DEATH_DAMAGE_MULT = 1.5f;

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
                
		stats.getSuppliesPerMonth().modifyMult(id, CHEM_SUPPLIES_MULT);
		stats.getSuppliesPerMonth().modifyMult(id, CHEM_REC_SUPPLIES_MULT);
		float reedi = (float) ((100 - (stats.getNumFighterBays().getBaseValue() * READINESS_MULT_PER_DECK)) / 100);
		stats.getPeakCRDuration().modifyMult(id, reedi);
            if (!stats.getVariant().getHullSpec().getBuiltInMods().contains(OCUA_HullMods.OCUA_DULCENA_ARRAY)) {
		stats.getZeroFluxSpeedBoost().modifyMult(id, 0);
            }
                
		stats.getEnergyWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (FLUX_MULT / 100)));
		stats.getBeamWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
                stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (FLUX_MULT / 100)));
		stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, DEATH_RANGE_MULT);
		stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, DEATH_DAMAGE_MULT);
        }

        @Override
        public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
            MutableShipStatsAPI stats = fighter.getMutableStats();

		stats.getEnergyWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
		stats.getBeamWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
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
                bullet = tooltip.addPara("Fighter/Bomber damage output %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((WING_DAMAGE_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("Energy and Beam damage %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((DAMAGE_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("Energy and Beam flux generation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) FLUX_MULT + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Peak Readiness time %s, %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) READINESS_MULT_PER_DECK + "%", "per deck" );
		bullet.setHighlight("-" + (int) READINESS_MULT_PER_DECK + "%", "per deck");
		bullet.setHighlightColors(bad, h);
                bullet = tooltip.addPara("Zero-flux speed bonus is %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "prevented" );
                bullet = tooltip.addPara("Crew loss from crewed wing losses %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((100 *(LOSS_PENALTY)) - 100) + "%" );
                bullet = tooltip.addPara("Supply Cost for Maintenance %s and for Repair %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((CHEM_SUPPLIES_MULT * 100) - 100) + "%", "+" + (int) ((CHEM_REC_SUPPLIES_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("%s larger explosion upon death.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((DEATH_DAMAGE_MULT * 100) - 100) + "%" );
                
            tooltip.setBulletedListMode(null);
	}
        
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "most";
		if (index == 1) return "" + (int) ((CHEM_SUPPLIES_MULT * 100) - 100) + "%";
		if (index == 2) return "" + (int) ((CHEM_REC_SUPPLIES_MULT * 100) - 100) + "%";
		if (index == 3) return "" + (int) ((WING_DAMAGE_MULT * 100) - 100) + "%";
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
