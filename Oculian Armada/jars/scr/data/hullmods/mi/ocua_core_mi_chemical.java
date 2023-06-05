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

public class ocua_core_mi_chemical extends BaseHullMod {

	public static final float DAMAGE_MULT = 1.25f;
	public static final float MISSILE_DAMAGE_MULT = 1.1f;
	public static final float BOOSTER_DAMAGE = 10f;
	public static final float FLUX_MULT = 20f;
	public static final float SUPPLIES_MULT = 1.33f;
	public static final float REPAIR_MULT = 2.0f;
	public static final float DEATH_RANGE_MULT = 1.33f;
	public static final float DEATH_DAMAGE_MULT = 1.67f;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (FLUX_MULT / 100)));
		stats.getBeamWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
                stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (FLUX_MULT / 100)));
                stats.getHitStrengthBonus().modifyPercent(id, BOOSTER_DAMAGE);
		stats.getMissileWeaponDamageMult().modifyMult(id, MISSILE_DAMAGE_MULT);
                stats.getMissileWeaponFluxCostMod().modifyMult(id, (1 - (FLUX_MULT / 100)));
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
                bullet = tooltip.addPara("Missile damage %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ((MISSILE_DAMAGE_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("Energy and Beam flux generation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) FLUX_MULT + "%" );
                bullet = tooltip.addPara("Effective damage for armor calculation %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) BOOSTER_DAMAGE + "%" );
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Supply Cost for Maintenance %s and for Repair %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((SUPPLIES_MULT * 100) - 100) + "%", "+" + (int) ((REPAIR_MULT * 100) - 100) + "%" );
                bullet = tooltip.addPara("%s larger explosion upon death.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) ((DEATH_DAMAGE_MULT * 100) - 100) + "%" );
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((DAMAGE_MULT * 100) - 100) + "%";
		if (index == 1) return "" + (int) ((MISSILE_DAMAGE_MULT * 100) - 100) + "%";
		if (index == 2) return "" + (int) BOOSTER_DAMAGE + "%";
		if (index == 3) return "" + (int) (FLUX_MULT) + "%";
		if (index == 4) return "" + (int) ((SUPPLIES_MULT * 100) - 100) + "%";
		if (index == 5) return "" + (int) ((REPAIR_MULT * 100) - 100) + "%";
		if (index == 6) return "" + (int) ((DEATH_DAMAGE_MULT * 100) - 100) + "%";
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
