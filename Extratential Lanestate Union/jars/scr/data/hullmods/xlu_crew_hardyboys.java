package data.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WingRole;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ids.XLU_HullMods;
import data.scripts.plugins.xlu_BlockedHullmodDisplayScript;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class xlu_crew_hardyboys extends xlu_crew_base {

        //public static final float ARMOR_BONUS_BASE = 15f;
        public static final float REPAIR_BONUS = 25f;
        
	public static final float CARRIER_BONUS = 1.15f;
	public static final float CARRIER_REFIT = 0.85f;
	public static final float CARRIER_REPLACEMENT_GAIN = 1.1f;
	public static final float GROUND_BONUS = 10f;
        
	public static final float CREW_PENALTY = 25f;
	public static final float CREW_PER_DECK = 10f;
	
        //private static float armor_eff = 10f;
        private static float gron_bon = 5f;
        private static float car_rep = 1.05f;
        private static String wing_red_bonus = "10%";
        private static String bomb_red_bonus = "5%";
        
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static
        {
            // These hullmods will automatically be removed
            // This prevents unexplained hullmod blocking
            BLOCKED_HULLMODS.add("xlu_crew_warshots");
            BLOCKED_HULLMODS.add("xlu_crew_waymakers");
            //BLOCKED_HULLMODS.add("xlu_crew_hardyboys");
        }
    
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            if ((stats.getVariant().getHullMods().contains("xlu_armorclad")) || (stats.getVariant().getHullMods().contains("xlu_lithoframe"))){
                //armor_eff = ARMOR_BONUS_BASE;
                car_rep = CARRIER_REPLACEMENT_GAIN;
                gron_bon = GROUND_BONUS;
            }
            
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
                
                stats.getHangarSpaceMod().modifyMult(id, CARRIER_BONUS);
                stats.getFighterRefitTimeMult().modifyMult(id, CARRIER_REFIT);
                
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyMult(id, car_rep);
                
		int groundcrew = (int) (stats.getVariant().getHullSpec().getMinCrew() * (gron_bon * 0.01f));
		stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, groundcrew);
                
		int crew = (int) (stats.getNumFighterBays().getBaseValue() * CREW_PER_DECK);
		stats.getMinCrewMod().modifyPercent(id, CREW_PENALTY);
		stats.getMinCrewMod().modifyFlat(id, crew);
                
		stats.addListener(new FighterOPCostModifier() {
                        @Override
			public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
                            if (stats.getVariant().getHullMods().contains("xlu_armorclad") || (stats.getVariant().getHullMods().contains("xlu_lithoframe"))) {
				if (fighter.getRole().equals(WingRole.BOMBER))
					return (int) Math.ceil(currCost * 0.9);
                                else return (int) Math.ceil(currCost * 0.8); 
                            } else {
				if (fighter.getRole().equals(WingRole.BOMBER))
					return (int) Math.ceil(currCost * 0.95);
                                else return (int) Math.ceil(currCost * 0.9); 
				//return currCost;
                            }
			}
		});
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
            if (index == 0) return "XLU Armorclad Works";
            return null;
        }
        
/*        @Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) ARMOR_BONUS_BASE + "%";
		if (index == 1) return "" + (int) REPAIR_BONUS + "%";
		if (index == 2) return "20%, roundup";
		if (index == 3) return "10%";
		if (index == 4) return "" + (int) (100 - (100 *(CARRIER_REFIT))) + "%";
		if (index == 5) return "" + (int) ((100 *(CARRIER_REPLACEMENT_GAIN)) - 100) + "%";
		if (index == 6) return "" + (int) GROUND_BONUS + "%";
		if (index == 7) return "" + (int) CREW_PENALTY + "%";
		if (index == 8) return "" + (int) CREW_PER_DECK + "";
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
            //if (Global.getSettings().getCurrentState() == GameState.TITLE) return;
            
            if ((ship.getVariant().getHullMods().contains("xlu_armorclad")) || (ship.getVariant().getHullMods().contains("xlu_lithoframe"))){ //My God, what are you doing?!
                car_rep = CARRIER_REPLACEMENT_GAIN;
                gron_bon = GROUND_BONUS;
                
                wing_red_bonus = "20%, roundup";
                bomb_red_bonus = "10%";
            } else {
                gron_bon = 5f;
                car_rep = 1.05f;
                
                wing_red_bonus = "10%, roundup";
                bomb_red_bonus = "5%";
            }

            LabelAPI bullet;
            tooltip.setBulletedListMode(" • ");
            bullet = tooltip.addPara("Wing OP cost reduction %s, Bombers %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "-" + wing_red_bonus,
                "-" + bomb_red_bonus);
            bullet = tooltip.addPara("Weapon and Engine Repair speed %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "+" + Math.round(REPAIR_BONUS) + "%");
            bullet = tooltip.addPara("Wing refit times %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "-" + Math.round(100 - ((CARRIER_REFIT) * 100)) + "%");
            bullet = tooltip.addPara("Wing Replacement recovery %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "+" + Math.round(((car_rep * 100)) - 100) + "%");
            bullet = tooltip.addPara("Minimum Crew as Ground Support bonus %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "+" + Math.round(gron_bon) + "%");
            bullet = tooltip.addPara("Minimum Crew %s and %s per Deck.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                "+" + Math.round(CREW_PENALTY) + "%",
                "" + Math.round(CREW_PER_DECK));
            tooltip.setBulletedListMode(null);
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
        public boolean isApplicableToShip(ShipAPI ship) {
            /*if (ship.getVariant().hasHullMod(XLU_HullMods.XLU_ARMORCLAD)){
                return true;
            }*/
            if ((ship.getVariant().hasHullMod(HullMods.AUTOMATED) && ship.getVariant().getHullSpec().getMinCrew() == 0f) ||
                    (ship.getVariant().hasHullMod("ocua_drone_mod") && ship.getHullSpec().getMinCrew() <= 1f)){
                return false;
            }
            if (!(ship.getVariant().hasHullMod(XLU_HullMods.XLU_CREW_WARSHOTS) ||
                    (ship.getVariant().hasHullMod(XLU_HullMods.XLU_CREW_WAYMAKERS)))){
                return true;
            } else {
                return false;
            }
        }
    
	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null || ship.getVariant() == null) return true; // autofit
		if (!ship.getVariant().hasHullMod("xlu_crew_hardyboys")) return true; // can always add
                
		for (String slotId : ship.getVariant().getNonBuiltInWings()) {
			if (!(slotId == null)) return false;
		}
		return true;
	}

	@Override
	public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		return "The Hardy Boys want their Wing LPCs gone first before you unassign them";
	}
}
