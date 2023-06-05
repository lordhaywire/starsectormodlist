package data.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ids.XLU_HullMods;
import data.scripts.plugins.xlu_BlockedHullmodDisplayScript;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class xlu_crew_waymakers extends xlu_crew_base {

	public static final float CHARGES_BONUS = 1f;
	public static final float REGEN_PERCENT = 10f;
	//public static final float COOLDOWN_REDUCTION = 15f;
	public static final float RANGE_PERCENT = 10f;
        
	public static final float CREW_REDUCTION = 20f;
	//public static final float CAPACITY_BONUS = 10f;
	public static final float READINESS_BONUS = 10f;
	//public static final float CREW_SURVEY_REDUCTION = 10f;
        
	public static final float SENSOR_BONUS_FRIGATE = 10f;
	public static final float SENSOR_BONUS_DESTROYER = 20f;
	public static final float SENSOR_BONUS_CRUISER = 30f;
	public static final float SENSOR_BONUS_CAPITAL = 50f;
	public static final float SIGHT_BONUS = 25f;
        
        private static float crew_red_bonus = 10f;
        private static float sen_frig_bonus = 5f;
        private static float sen_des_bonus = 10f;
        private static float sen_cru_bonus = 15f;
        private static float sen_cap_bonus = 25f;
        private static float sight_bonus = 15f;
        private static String exotic_red_bonus = "1/2/4";
            
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static
        {
            // These hullmods will automatically be removed
            // This prevents unexplained hullmod blocking
            BLOCKED_HULLMODS.add("xlu_crew_warshots");
            //BLOCKED_HULLMODS.add("xlu_crew_waymakers");
            BLOCKED_HULLMODS.add("xlu_crew_hardyboys");
        }
    
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            if ((stats.getVariant().getHullMods().contains("xlu_armorclad")) || (stats.getVariant().getHullMods().contains("xlu_lithoframe"))){
                sen_frig_bonus = SENSOR_BONUS_FRIGATE; sen_des_bonus = SENSOR_BONUS_DESTROYER;
                sen_cru_bonus = SENSOR_BONUS_CRUISER; sen_cap_bonus = SENSOR_BONUS_CAPITAL;
                sight_bonus = SIGHT_BONUS;
                crew_red_bonus = CREW_REDUCTION;
            }
            
		stats.getSystemUsesBonus().modifyFlat(id, (int) CHARGES_BONUS);
		stats.getSystemRegenBonus().modifyPercent(id, REGEN_PERCENT);
		stats.getSystemRangeBonus().modifyPercent(id, RANGE_PERCENT);
		//stats.getSystemCooldownBonus().modifyMult(id, 1f - COOLDOWN_REDUCTION / 100f);
                
                stats.getSightRadiusMod().modifyMult(id, (1 + ((100 + SIGHT_BONUS) / 100)));
                if (hullSize == HullSize.FRIGATE) {
                    stats.getEffectiveArmorBonus().modifyFlat(id, sen_frig_bonus);
                } else if (hullSize == HullSize.DESTROYER) {
                    stats.getEffectiveArmorBonus().modifyFlat(id, sen_des_bonus);
                } else if (hullSize == HullSize.CRUISER) {
                    stats.getEffectiveArmorBonus().modifyFlat(id, sen_cru_bonus);
                } else if (hullSize == HullSize.CAPITAL_SHIP) {
                    stats.getEffectiveArmorBonus().modifyFlat(id, sen_cap_bonus);
                }
                
		stats.getMinCrewMod().modifyMult(id, 1f - CREW_REDUCTION * 0.01f);
                
                //stats.getCargoMod().modifyPercent(id, CAPACITY_BONUS);
                //stats.getFuelMod().modifyPercent(id, CAPACITY_BONUS);
                //stats.getMaxCrewMod().modifyPercent(id, CAPACITY_BONUS);
                stats.getPeakCRDuration().modifyPercent(id, READINESS_BONUS);
		//int survey = (int) (stats.getVariant().getHullSpec().getMinCrew() * (CREW_SURVEY_REDUCTION * 0.01f));
		//stats.getDynamic().getMod(Stats.getSurveyCostReductionId(Commodities.CREW)).modifyFlat(id, survey); // Yeah, this doesn't work
                
                stats.addListener(new WeaponOPCostModifier() {
                        @Override
			public int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost) {
				//if (!weapon.getType().equals(WeaponType.BALLISTIC)) {
                                //    currCost = currCost - (int) 1f;
                                //    return currCost;
                                //}
                                //It's a nest. RUN!
                                if (!(weapon.getType().equals(WeaponType.BALLISTIC)) ||
                                            weapon.getMountType().equals(WeaponType.COMPOSITE) ||
                                            weapon.getMountType().equals(WeaponType.HYBRID) ||
                                            weapon.getMountType().equals(WeaponType.SYNERGY) ||
                                            weapon.getMountType().equals(WeaponType.UNIVERSAL)) {
                                    if ((weapon.getType().equals(WeaponType.BALLISTIC) || 
                                            weapon.getType().equals(WeaponType.ENERGY) || 
                                            weapon.getType().equals(WeaponType.MISSILE)) &&
                                            !(weapon.getMountType().equals(WeaponType.COMPOSITE) ||
                                            weapon.getMountType().equals(WeaponType.HYBRID) ||
                                            weapon.getMountType().equals(WeaponType.SYNERGY) ||
                                            weapon.getMountType().equals(WeaponType.UNIVERSAL))) {
                                                currCost = currCost - (int) 1f;
                                    } else {
                                        if (stats.getVariant().getHullMods().contains("xlu_armorclad") || (stats.getVariant().getHullMods().contains("xlu_lithoframe"))) {
                                            if (weapon.getSize().equals(WeaponAPI.WeaponSize.SMALL)) {
                                                return (currCost - (int) 1);
                                            } else if (weapon.getSize().equals(WeaponAPI.WeaponSize.MEDIUM)) {
                                                return (currCost - (int) 2);
                                            } else if (weapon.getSize().equals(WeaponAPI.WeaponSize.LARGE)) {
                                                return (currCost - (int) 4);
                                            }
                                        } else {
                                            if (weapon.getSize().equals(WeaponAPI.WeaponSize.MEDIUM)) {
                                                return (currCost - (int) 1);
                                            } else if (weapon.getSize().equals(WeaponAPI.WeaponSize.LARGE)) {
                                                return (currCost - (int) 2);
                                            }
                                        }
                                    }
                                    if (currCost < (int) 0f) {
                                        currCost = (int) 0f;
                                    }
                                    return currCost;
				}
				return currCost;
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
		if (index == 0) return "1";
		if (index == 1) return exotic_red_bonus;
		if (index == 2) return "" + (int) CHARGES_BONUS + " use";
		if (index == 3) return "" + (int) REGEN_PERCENT + "%";
		if (index == 4) return "" + (int) RANGE_PERCENT + "%";
		if (index == 5) return "" + (int) COOLDOWN_REDUCTION + "%";
                if (index == 6) return "" + (int) SIGHT_BONUS + "%";
                if (index == 7) {
                    if (hullSize == HullSize.FRIGATE) return "" + (int) SENSOR_BONUS_FRIGATE;
                    else if(hullSize == HullSize.DESTROYER) return "" + (int) SENSOR_BONUS_DESTROYER;
                    else if(hullSize == HullSize.CRUISER) return "" + (int) SENSOR_BONUS_CRUISER;
                    else if(hullSize == HullSize.CAPITAL_SHIP) return "" + (int) SENSOR_BONUS_CAPITAL;
                }
		if (index == 8) return "" + (int) CAPACITY_BONUS + "%";
		if (index == 9) return "" + (int) CREW_REDUCTION + "%";
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
                sen_frig_bonus = SENSOR_BONUS_FRIGATE; sen_des_bonus = SENSOR_BONUS_DESTROYER;
                sen_cru_bonus = SENSOR_BONUS_CRUISER; sen_cap_bonus = SENSOR_BONUS_CAPITAL;
                sight_bonus = SIGHT_BONUS;
                crew_red_bonus = CREW_REDUCTION;
                
                exotic_red_bonus = "1/2/4";
            } else {
                sen_frig_bonus = 10f; sen_des_bonus = 20f;
                sen_cru_bonus = 30f; sen_cap_bonus = 50f;
                sight_bonus = 15f;
                crew_red_bonus = 10f;
                
                exotic_red_bonus = "0/1/2";
            }

            LabelAPI bullet;
            tooltip.setBulletedListMode(" • ");
            bullet = tooltip.addPara("Non-Ballistic, Non-Exotic Ordinance costs %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "-" + Math.round(1f));
            bullet = tooltip.addPara("Exotic Weapon (Hybrid, Composite, etc.) Ordinance costs %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "-" + exotic_red_bonus);
            bullet = tooltip.addPara("Ship System charges %s, charge regeneration %s and range %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "+" + Math.round(CHARGES_BONUS),
                "+" + Math.round(REGEN_PERCENT) + "%",
                "+" + Math.round(RANGE_PERCENT) + "%");
                //"+" + Math.round(COOLDOWN_REDUCTION) + "%");
            bullet = tooltip.addPara("Sensor range bonus %s units.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "+" + Math.round(sen_frig_bonus) + "/" + Math.round(sen_des_bonus) + "/" + Math.round(sen_cru_bonus) + "/" + Math.round(sen_cap_bonus));
            bullet = tooltip.addPara("Combat Sight range %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "+" + Math.round(sight_bonus) + "%");
            bullet = tooltip.addPara("Skeleton Crew reduced by %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                "-" + Math.round(crew_red_bonus) + "%");
            //bullet = tooltip.addPara("Cargo/Fuel/Crew Capacity %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
            //    "+" + Math.round(CAPACITY_BONUS) + "%");
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
            if (!(ship.getVariant().hasHullMod(XLU_HullMods.XLU_CREW_HARDYBOYS) || 
                    (ship.getVariant().hasHullMod(XLU_HullMods.XLU_CREW_WARSHOTS)))){
                return true;
            } else {
                return false;
            }
        }
    
	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
		if (ship == null || ship.getVariant() == null) return true; // autofit
		if (!ship.getVariant().hasHullMod("xlu_crew_waymakers")) return true; // can always add

		for (String slotId : ship.getVariant().getNonBuiltInWeaponSlots()) {
			WeaponSpecAPI spec = ship.getVariant().getWeaponSpec(slotId);
			if (!spec.getType().equals(WeaponType.BALLISTIC)) return false;
		}
		return true;
	}

	@Override
	public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
		return "The Waymakers demand to remove all non-Ballistic weapons from the ship before leaving";
	}
}
