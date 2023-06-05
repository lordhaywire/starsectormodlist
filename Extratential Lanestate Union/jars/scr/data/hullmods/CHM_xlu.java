package data.hullmods;

import com.fs.starfarer.api.Global;
import java.util.HashMap;
import java.util.Map;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
//import com.fs.starfarer.api.impl.campaign.ids.Factions;
//import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ids.XLU_HullMods;
import java.awt.Color;

public class CHM_xlu extends BaseHullMod {

    protected static final float PARA_PAD = 10f;
    protected static final float SECTION_PAD = 10f;
    protected static final float INTERNAL_PAD = 4f;
    protected static final float INTERNAL_PARA_PAD = 4f;
    
    protected static final float LOAD_OF_BULL = 3f;
    
	//public static final float BALLISTIC_BONUS_FRIGATE = 15f;
	//public static final float BALLISTIC_BONUS_DESTROYER = 10f;
	//public static final float BALLISTIC_BONUS_CRUISER = 5f;
	//public static final float BALLISTIC_BONUS_CAPITAL = 5f;
	public static final float BALLISTIC_FLUX_FRIGATE = 20f;
	public static final float BALLISTIC_FLUX_DESTROYER = 15f;
	public static final float BALLISTIC_FLUX_CRUISER = 10f;
	public static final float BALLISTIC_FLUX_CAPITAL = 10f;
        //public static final float BALLISTIC_BONUS = 5f;
        public static final float ACCURACY_BUFF = 15f; //10f;
        
        public static final float ENERGY_FLUX_FRIGATE = 10f;
        public static final float ENERGY_FLUX_DESTROYER = 7.5f;
        public static final float ENERGY_FLUX_CRUISER = 5f;
        public static final float ENERGY_FLUX_CAPITAL = 5f;
        
        public static final float HIT_STRENGTH_BUFF = 10f;
        public static final float THRESHOLD_BUFF = 5f;
        
        public static final float DAMAGE_RES = 10f;
        public static final float DAMAGE_RES_XLU = 5f;
        
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            if (hullSize == HullSize.FRIGATE) {
                //stats.getBallisticRoFMult().modifyPercent(id, BALLISTIC_BONUS_FRIGATE);
                stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 - (BALLISTIC_FLUX_FRIGATE / 100)));
            } else if (hullSize == HullSize.DESTROYER) {
                //stats.getBallisticRoFMult().modifyPercent(id, BALLISTIC_BONUS_DESTROYER);
                stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 - (BALLISTIC_FLUX_DESTROYER / 100)));
            } else if (hullSize == HullSize.CRUISER) {
                //stats.getBallisticRoFMult().modifyPercent(id, BALLISTIC_BONUS_CRUISER);
                stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 - (BALLISTIC_FLUX_CRUISER / 100)));
            } else if (hullSize == HullSize.CAPITAL_SHIP) {
                //stats.getBallisticRoFMult().modifyPercent(id, BALLISTIC_BONUS_CAPITAL);
                stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 - (BALLISTIC_FLUX_CAPITAL / 100)));
            }
            stats.getAutofireAimAccuracy().modifyPercent(id, ACCURACY_BUFF);
            
            //stats.getBallisticRoFMult().modifyPercent(id, BALLISTIC_BONUS);
            if ((stats.getVariant().getHullMods().contains("xlu_crew_waymakers"))){
                if (hullSize == HullSize.FRIGATE)
                    stats.getBallisticRoFMult().modifyPercent(id, ENERGY_FLUX_FRIGATE);
                else if (hullSize == HullSize.DESTROYER)
                    stats.getBallisticRoFMult().modifyPercent(id, ENERGY_FLUX_DESTROYER);
                else if (hullSize == HullSize.CRUISER)
                    stats.getBallisticRoFMult().modifyPercent(id, ENERGY_FLUX_CRUISER);
                else if (hullSize == HullSize.CAPITAL_SHIP)
                    stats.getBallisticRoFMult().modifyPercent(id, ENERGY_FLUX_CAPITAL);
            }
            
            if ((stats.getVariant().getHullMods().contains("xlu_crew_warshots"))){
                stats.getHitStrengthBonus().modifyPercent(id, HIT_STRENGTH_BUFF);
                stats.getWeaponRangeMultPastThreshold().modifyMult(id, 1f + (THRESHOLD_BUFF * 0.01f));
            }
    }
    
        @Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		MutableShipStatsAPI stats = fighter.getMutableStats();
		
            if ((ship.getVariant().getHullMods().contains("xlu_crew_hardyboys"))){
                if (stats.getVariant().getHullSpec().getHullId().startsWith("xlu_")){
                    stats.getArmorDamageTakenMult().modifyMult(id, 1f - (DAMAGE_RES_XLU / 100));
                    stats.getShieldDamageTakenMult().modifyPercent(id, 1f - (DAMAGE_RES_XLU / 100));
                    stats.getHullDamageTakenMult().modifyPercent(id, 1f - (DAMAGE_RES_XLU / 100));
                } else {
                    stats.getArmorDamageTakenMult().modifyMult(id, 1f - (DAMAGE_RES / 100));
                    stats.getShieldDamageTakenMult().modifyPercent(id, 1f - (DAMAGE_RES / 100));
                    stats.getHullDamageTakenMult().modifyPercent(id, 1f - (DAMAGE_RES / 100));
                }
            }
	}
	
        @Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
            float opad = 10f;
            Color good = Misc.getPositiveHighlightColor();
            Color bad = Misc.getNegativeHighlightColor();
            if (isForModSpec || ship == null) return;
            //if (Global.getSettings().getCurrentState() == GameState.TITLE) return;
            
            LabelAPI bullet;
            tooltip.setBulletedListMode(" • ");
            TooltipMakerAPI text;
            if ((ship.getVariant().getHullMods().contains("xlu_crew_waymakers"))){
		tooltip.addSectionHeading("XLCrew Waymakers", Alignment.MID, opad);
                bullet = tooltip.addPara("Energy Flux efficiency %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + ENERGY_FLUX_FRIGATE + "%/" + ENERGY_FLUX_DESTROYER + "%/"
                    + ENERGY_FLUX_CRUISER + "%/" + ENERGY_FLUX_CAPITAL + "%");
            }
            if ((ship.getVariant().getHullMods().contains("xlu_crew_warshots"))){
		tooltip.addSectionHeading("XLCrew Warshots", Alignment.MID, opad);
                bullet = tooltip.addPara("Hit strength for armor damage reduction %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + Math.round(HIT_STRENGTH_BUFF) + "%");
                bullet = tooltip.addPara("Range Threshold Limit %s, should it exist.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "x" + (1 + Math.round(THRESHOLD_BUFF) / 100f));
            }
            if ((ship.getVariant().getHullMods().contains("xlu_crew_hardyboys"))){
		tooltip.addSectionHeading("XLCrew Hardyboys", Alignment.MID, opad);
                bullet = tooltip.addPara("Fighter Damage Resistance %s. (%s for XLU manfactured Wings.)", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + Math.round(DAMAGE_RES) + "%", "+" + Math.round(DAMAGE_RES_XLU) + "%");
            }
            tooltip.setBulletedListMode(null);
        }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
                if (ship.getVariant().hasHullMod("CHM_commission")) {
                    ship.getVariant().removeMod("CHM_commission");
                }
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
	if(index == 0) return "" + (int) ACCURACY_BUFF + "%";
	if(index == 1) return "" + 
                (int) (BALLISTIC_FLUX_FRIGATE) + "%/" + 
                (int) (BALLISTIC_FLUX_DESTROYER) + "%/" + 
                (int) (BALLISTIC_FLUX_CRUISER) + "%/" + 
                (int) (BALLISTIC_FLUX_CAPITAL) + "%";
	//if(index == 0) return "" + (int) (BALLISTIC_BONUS_FRIGATE) + "%/" + (int) (BALLISTIC_BONUS_DESTROYER) + "%/" + (int) (BALLISTIC_BONUS_CRUISER) + "%/" + (int) (BALLISTIC_BONUS_CAPITAL) + "%";
	//if (index == 2) return "" + (int) ACCURACY_DEBUFF + "%";
	//if (index == 4) return "" + (int) BALLISTIC_BONUS + "%";
	//if (index == 5) return "" + (int) BALLISTIC_FLUX + "%";
        else {
            return null;
        }
    }

}
