package data.hullmods.bi;

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
import data.scripts.plugins.xlu_BlockedHullmodDisplayScript;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class xlu_juggernaught_hullmod extends BaseHullMod {
	
	public static final float SENSOR_ADD = 60f;
	public static final float PROFILE_ADD = 60f;
	public static final float SIGHT_BONUS = 20f;
	public static final float RANGE_BONUS = 70f;
	public static final float RANGE_MINUS = 10f;
	public static final float MISSILE_RANGE_BONUS = 25f;
        
	public static final float TURRET_BONUS = 50f;
	public static final float ARMOR_CAPITAL = 100f;
	//public static final float TURRET_PENALTY = -20f;
	public static final float RADIUS_MULT = 1.2f;
	public static final float DAMAGE_MULT = 1.5f;
        
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static
        {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
            BLOCKED_HULLMODS.add("targetingunit");
            BLOCKED_HULLMODS.add("dedicated_targeting_core");
        }
    
        @Override
        public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            stats.getSensorStrength().modifyFlat(id, SENSOR_ADD);
            stats.getSensorProfile().modifyFlat(id, PROFILE_ADD);
            stats.getSightRadiusMod().modifyMult(id, (1 + ((100 + SIGHT_BONUS) / 100)));
            
            stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
            stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
            stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, -RANGE_MINUS);
            stats.getBeamPDWeaponRangeBonus().modifyPercent(id, -RANGE_MINUS);
            
            stats.getMissileWeaponRangeBonus().modifyPercent(id, MISSILE_RANGE_BONUS);
            stats.getMissileMaxSpeedBonus().modifyPercent(id, MISSILE_RANGE_BONUS);
            
            stats.getWeaponHealthBonus().modifyMult(id, (1 + (TURRET_BONUS / 100)));
            stats.getEffectiveArmorBonus().modifyFlat(id, ARMOR_CAPITAL);
            //stats.getWeaponTurnRateBonus().modifyMult(id, (1 + (TURRET_PENALTY / 100)));
            
            stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, DAMAGE_MULT);
            stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, RADIUS_MULT);
        }
	
        @Override
        public String getDescriptionParam(int index, HullSize hullSize) {
            if (index == 0) return "" + (int) RANGE_BONUS + "%";
            if (index == 1) return "" + (int) (RANGE_BONUS - RANGE_MINUS) + "%";
            if (index == 2) return "" + (int) MISSILE_RANGE_BONUS + "%";
            
            /*if (index == 0) return "" + (int) SENSOR_ADD;
            if (index == 1) return "" + (int) SIGHT_BONUS + "%";
            if (index == 2) return "" + (int) RANGE_BONUS + "%";
            if (index == 3) return "" + (int) (RANGE_BONUS / 2) + "%";
            if (index == 4) return "" + (int) (RANGE_BONUS - RANGE_MINUS) + "%";
            if (index == 5) return "" + (int) TURRET_BONUS + "%";
            if (index == 6) return "" + (int) TURRET_PENALTY + "%";
            if (index == 7) return "" + (int) ((SUPPLY_USE_MULT * 100) - 100) + "%";
            if (index == 8) return "range extension hullmods";*/
            
            return null;
        }

        protected static final float LOAD_OF_BULL = 3f;
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		float pad = 3f;
		Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                Color good = Misc.getPositiveHighlightColor();
                
                LabelAPI bullet;
                tooltip.setBulletedListMode(" • ");
		tooltip.addSectionHeading("Features", Alignment.MID, opad);
                bullet = tooltip.addPara("Sensor Profile and Sensor Strength %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "+" + (int) PROFILE_ADD + " units");
                bullet = tooltip.addPara("Combat Sight range %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) SIGHT_BONUS + "%");
                bullet = tooltip.addPara("Armor Strength %s units.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) ARMOR_CAPITAL + "");
                bullet = tooltip.addPara("Weapon Durability %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) TURRET_BONUS + "%");
                
		tooltip.addSectionHeading("Compatibilities", Alignment.MID, opad);
                bullet = tooltip.addPara("Cannot install Dedicated Targeting Core and Integrated Targeting Unit.%s", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + "" + "");
		bullet.setHighlight("Dedicated Targeting Core", "Integrated Targeting Unit");
		bullet.setHighlightColors(h, h);
                
            tooltip.setBulletedListMode(null);
	}

        @Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            for (String tmp : BLOCKED_HULLMODS) {
                if (ship.getVariant().getHullMods().contains(tmp)) {
                    ship.getVariant().removeMod(tmp);
                    xlu_BlockedHullmodDisplayScript.showBlocked(ship);
                }
            }
	}
	
        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
            return ship != null && ship.getHullSpec().getHullId().startsWith("ob_");
        }
}
