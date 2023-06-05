package data.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ids.XLU_HullMods;
import data.scripts.plugins.xlu_BlockedHullmodDisplayScript;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class xlu_crew_warshots extends xlu_crew_base {

	//public static final float EFFICIENCY_PENALTY = 1.1f;
        public static final float SMALL_BALLISTIC_MOD = 1f;
        public static final float MEDIUM_BALLISTIC_MOD = 2f;
        public static final float LARGE_BALLISTIC_MOD = 4f;
        
	public static final float TURRET_SPEED_BONUS = 25f;
	public static final float TURRET_HEALTH_BONUS = 40f;
	//public static final float EMP_RESISTANCE = 20f;
	public static final float VENT_SPEED = 15f;
	//public static final float SHIELD_BONUS = 10f;
        
	public static final float SUPPLIES_MULT = 1.25f;
	public static final float DAMAGE_BONUS_PERCENT = 5f;
	
        
	public static float uncle_vent = 10f;
	//public static float tur_speed = 10f;
	//public static float shield_bon = 5f;
        
        private static String ball_red_bonus = "1/2/4";
        
            
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static
        {
            // These hullmods will automatically be removed
            // This prevents unexplained hullmod blocking
            //BLOCKED_HULLMODS.add("xlu_crew_warshots");
            BLOCKED_HULLMODS.add("xlu_crew_waymakers");
            BLOCKED_HULLMODS.add("xlu_crew_hardyboys");
        }
    
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            if ((stats.getVariant().getHullMods().contains("xlu_armorclad")) || (stats.getVariant().getHullMods().contains("xlu_lithoframe"))){
                uncle_vent = VENT_SPEED;
                //tur_speed = TURRET_SPEED_BONUS;
                //shield_bon = SHIELD_BONUS;
                
                stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyFlat(id, -SMALL_BALLISTIC_MOD);
                stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -MEDIUM_BALLISTIC_MOD);
                stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -LARGE_BALLISTIC_MOD);
            } else {
                stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -1f);
                stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -2f);
            }
            
		stats.getVentRateMult().modifyPercent(id, uncle_vent);
		stats.getWeaponHealthBonus().modifyPercent(id, TURRET_HEALTH_BONUS);
		stats.getWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
		//stats.getEmpDamageTakenMult().modifyMult(id, 1f - EMP_RESISTANCE * 0.01f);
                
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLIES_MULT);
		stats.getSuppliesToRecover().modifyMult(id, SUPPLIES_MULT);
	}
        
        @Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
            if (index == 0) return "XLU Armorclad Works";
            return null;
        }
        
/*        @Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return ball_red_bonus;
		if (index == 1) return "" + (int) VENT_SPEED + "%";
		if (index == 2) return "" + (int) TURRET_SPEED_BONUS + "%";
		if (index == 3) return "" + (int) TURRET_HEALTH_BONUS + "%";
		if (index == 4) return "" + (int) EMP_RESISTANCE + "%";
		if (index == 5) return "" + (int) SHIELD_BONUS + "%";
		if (index == 6) return "" + (int) ((SUPPLIES_MULT * 100) - 100) + "%";
		return null;
	}*/

	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}

        @Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
            Color good = Misc.getPositiveHighlightColor();
            Color bad = Misc.getNegativeHighlightColor();
            if (isForModSpec || ship == null) return;
            
            if ((ship.getVariant().getHullMods().contains("xlu_armorclad")) || (ship.getVariant().getHullMods().contains("xlu_lithoframe"))){ //My God, what are you doing?!
                uncle_vent = VENT_SPEED;
                
                ball_red_bonus = "1/2/4";
            } else {
                uncle_vent = 10f;
                
                ball_red_bonus = "0/1/2";
            }

            LabelAPI bullet;
            tooltip.setBulletedListMode(" • ");
            bullet = tooltip.addPara("Ballistic Weapon Ordinance costs %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "-" + ball_red_bonus);
            bullet = tooltip.addPara("Vent speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "+" + Math.round(uncle_vent) + "%");
            bullet = tooltip.addPara("Turret health %s, turn speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "+" + Math.round(TURRET_HEALTH_BONUS) + "%",
                "+" + Math.round(TURRET_SPEED_BONUS) + "%");
            bullet = tooltip.addPara("Supply Maintenance/Repair costs %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                "+" + Math.round((SUPPLIES_MULT * 100) - 100) + "%");
            //bullet = tooltip.addPara("EMP resistance %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
            //    "+" + Math.round(EMP_RESISTANCE) + "%");
            tooltip.setBulletedListMode(null);
        }

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
            /*if (ship.getVariant().hasHullMod(XLU_HullMods.XLU_ARMORCLAD)){
                return true;
            }*/
            if ((ship.getVariant().hasHullMod(HullMods.AUTOMATED) && ship.getVariant().getHullSpec().getMinCrew() == 0f) ||
                    (ship.getVariant().hasHullMod("ocua_drone_mod") && ship.getHullSpec().getMinCrew() <= 1f)){
                return false;
            }
            if (!(ship.getVariant().hasHullMod(XLU_HullMods.XLU_CREW_HARDYBOYS) ||
                    (ship.getVariant().hasHullMod(XLU_HullMods.XLU_CREW_WAYMAKERS)))){
                return true;
            } else {
                return false;
            }
        }
    
        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            super.applyEffectsAfterShipCreation(ship, id);

            for (String tmp : BLOCKED_HULLMODS) {
                if (ship.getVariant().getHullMods().contains(tmp)) {
                    ship.getVariant().removeMod(tmp);
                    xlu_BlockedHullmodDisplayScript.showBlocked(ship);
                }
            }
        }
    
	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null || ship.getVariant() == null) return true; // autofit
		if (!ship.getVariant().hasHullMod("xlu_crew_warshots")) return true; // can always add

		for (String slotId : ship.getVariant().getNonBuiltInWeaponSlots()) {
			WeaponSpecAPI spec = ship.getVariant().getWeaponSpec(slotId);
			if (spec.getType().equals(WeaponType.BALLISTIC)) return false;
		}
		return true;
	}

	@Override
	public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		return "Warshots cannot leave until they have their Ballistic weapons removed from the ship";
	}
}
