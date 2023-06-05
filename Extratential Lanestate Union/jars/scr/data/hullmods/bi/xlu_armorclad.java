package data.hullmods.bi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.XLU_MD;
import java.awt.Color;

public class xlu_armorclad extends BaseHullMod {

        public static final float ARMOR_BONUS_BASE = 25f; //30f;
	public static final float ARMOR_FIGHTER = 50f; // 100f;
	public static final float ARMOR_FRIGATE = 50f; // 250f;
	public static final float ARMOR_DESTROYER = 75f; // 350f;
	public static final float ARMOR_CRUISER = 100f; // 500f;
	public static final float ARMOR_CAPITAL = 150f; // 700f;
        
        public static final float KINETIC_RESIST = 25f;
        public static final float WEAPON_HEALTH = 50f;
        public static final float TURRET_PENALTY = 0.33f;
        public static final float EXPLOSIVE_PENALTY = 20f;
        
	public static final float SUPPLY_USE_MULT = 20f;
        
        public static final float ENGINE_HEALTH_FIGHTER = 1.75f;
        public static final float WEAPON_HEALTH_FIGHTER = 1.5f;
	public static final float REPAIR_RATE_FIGHTER = 67f;
        
        
        public static final float ENERGY_RESIST = 0.15f;
	public static final float REPAIR_WEAPON_BONUS = 40f;
	public static final float EMP_RESISTANCE = 33f;
//        public static final float FRAG_RESIST = 0.25f;
//	  public static final float ARMOR_COMPOSITE = 0.25f;
//        public static final float SHIELD_MOVEMENT_PENALTY = 0.5f;
//        public static final float SHIELD_PENALTY = 0.5f;
        
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

            if (hullSize == HullSize.FIGHTER) {
                stats.getEffectiveArmorBonus().modifyFlat(id, ARMOR_FIGHTER);
                stats.getEngineHealthBonus().modifyMult(id, ENGINE_HEALTH_FIGHTER);
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - REPAIR_RATE_FIGHTER * 0.01f);
            }
            
            if (!stats.getVariant().getHullMods().contains("xlu_magnetonclad")) {
                stats.getEffectiveArmorBonus().modifyPercent(id, ARMOR_BONUS_BASE);
                if (hullSize == HullSize.FRIGATE) {
                    stats.getEffectiveArmorBonus().modifyFlat(id, ARMOR_FRIGATE);
                } else if (hullSize == HullSize.DESTROYER) {
                    stats.getEffectiveArmorBonus().modifyFlat(id, ARMOR_DESTROYER);
                } else if (hullSize == HullSize.CRUISER) {
                    stats.getEffectiveArmorBonus().modifyFlat(id, ARMOR_CRUISER);
                } else if (hullSize == HullSize.CAPITAL_SHIP) {
                    stats.getEffectiveArmorBonus().modifyFlat(id, ARMOR_CAPITAL);
                }
            }
            
            stats.getKineticArmorDamageTakenMult().modifyMult(id, 1f - (KINETIC_RESIST / 100f));
            
            stats.getWeaponTurnRateBonus().modifyMult(id, (1 - TURRET_PENALTY));
            stats.getWeaponHealthBonus().modifyPercent(id, WEAPON_HEALTH);
            if (!(stats.getVariant().getHullMods().contains("shield_shunt") || 
                    stats.getVariant().getHullSpec().getShieldType() == ShieldType.NONE)) {
                stats.getHighExplosiveDamageTakenMult().modifyMult(id, 1f + (EXPLOSIVE_PENALTY / 100f));
            }
            stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, 1.25f);
            stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, 1.1f);
            
            stats.getSuppliesPerMonth().modifyMult(id, 1f - (SUPPLY_USE_MULT / 100f));
            stats.getSuppliesToRecover().modifyMult(id, 1f - (SUPPLY_USE_MULT / 100f));
            
//            stats.getFragmentationDamageTakenMult().modifyMult(id, (1 - FRAG_RESIST));
//            stats.getArmorBonus().modifyMult(id, (1 - ARMOR_COMPOSITE));
//            stats.getArmorBonus().modifyPercent(id, (100 / (1 - ARMOR_COMPOSITE)) - 100);
//            stats.getShieldTurnRateMult().modifyMult(id, (1 - SHIELD_MOVEMENT_PENALTY));
//            stats.getShieldUnfoldRateMult().modifyMult(id, (1 - SHIELD_MOVEMENT_PENALTY));
//            stats.getShieldArcBonus().modifyMult(id, (1 - SHIELD_PENALTY));
//            stats.getShieldArcBonus().modifyPercent(id, (100 / (1 - SHIELD_PENALTY)) - 100);
            
//            if ((stats.getVariant().getHullMods().contains("shield_shunt")) ||
//                    (stats.getVariant().getHullSpec().getShieldType().equals(ShieldType.NONE))) {
//                stats.getEnergyDamageTakenMult().modifyMult(id, (1 - ENERGY_RESIST));
//		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - REPAIR_WEAPON_BONUS * 0.01f);
//            }
            
            //I don't know how the logic of removing shields or adding a hull to remove shields work. But, eh. I'm just smacking it in there. Or not.
            //Yeah, I figured. You still need the hullmod to get the EMP bonus, so I'm slapping this in just for the hell of it.
//            if (!(stats.getVariant().getHullMods().contains("shield_shunt")) &&
//                    stats.getVariant().getHullSpec().getShieldType().equals(ShieldType.NONE)) {
//		stats.getEmpDamageTakenMult().modifyMult(id, 1f - EMP_RESISTANCE * 0.01f);
//            }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
	if(index == 0) return "" + (int) ARMOR_BONUS_BASE + "%";
	if(index == 1) return "" + (int) KINETIC_RESIST + "%";
	if(index == 2) return "" + (int) (SUPPLY_USE_MULT) + "%";
	if(index == 3) return "" + (int) (EXPLOSIVE_PENALTY * 100 * 2) + "%";
	if(index == 4) return "" + (int) (TURRET_PENALTY * 100) + "%";
	if(index == 5) return "Shield Shunt";
	if(index == 6) return "negating the increased High Explosive damage";
//	if(index == 1) {
//            if (hullSize == HullSize.FRIGATE) return "" + (int) ARMOR_FRIGATE;
//            else if(hullSize == HullSize.DESTROYER) return "" + (int) ARMOR_DESTROYER;
//            else if(hullSize == HullSize.CRUISER) return "" + (int) ARMOR_CRUISER;
//            else if(hullSize == HullSize.CAPITAL_SHIP) return "" + (int) ARMOR_CAPITAL;
//            else return "" + (int) ARMOR_FIGHTER;
//        }
//	if(index == 7) return "" + (int) (ENERGY_RESIST * 100) + "%";
//	if(index == 8) return "" + (int) (REPAIR_WEAPON_BONUS) + "%";
//	if(index == 9) return "" + (int) (EMP_RESISTANCE) + "%";
//	if(index == 6) return "" + (int) (SHIELD_MOVEMENT_PENALTY * 100) + "%";
//	if(index == 7) return "" + (int) (SHIELD_PENALTY * 100) + "%";
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
                Color gray = Misc.getGrayColor();
                float armorsize = ARMOR_FRIGATE;
                
                if (!(isForModSpec || ship == null)) {
                    if (hullSize == HullSize.FRIGATE) {
                        armorsize = ARMOR_FRIGATE;
                    } else if (hullSize == HullSize.DESTROYER) {
                        armorsize = ARMOR_DESTROYER;
                    } else if (hullSize == HullSize.CRUISER) {
                        armorsize = ARMOR_CRUISER;
                    } else if (hullSize == HullSize.CAPITAL_SHIP) {
                        armorsize = ARMOR_CAPITAL;
                    }
                }
		
                LabelAPI bullet;
                tooltip.addPara(XLU_MD.armorclad("flavor"), gray, opad);
                tooltip.addPara(XLU_MD.armorclad("flavor2"), gray, pad);
                
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading(XLU_MD.base("good"), Alignment.MID, opad);
                bullet = tooltip.addPara(XLU_MD.armorclad("good_wep_dur"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) WEAPON_HEALTH + "%" );
                bullet = tooltip.addPara(XLU_MD.armorclad("good_arm_str"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) armorsize + "", "" + (int) ARMOR_BONUS_BASE + "%" );
                bullet = tooltip.addPara(XLU_MD.armorclad("good_kin_rec"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) KINETIC_RESIST + "%" );
                bullet = tooltip.addPara(XLU_MD.armorclad("good_supply"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) SUPPLY_USE_MULT + "%" );
                
		tooltip.addSectionHeading(XLU_MD.base("bad"), Alignment.MID, opad);
                if (!(ship.getVariant().getHullMods().contains("shield_shunt") || 
                    ship.getVariant().getHullSpec().getShieldType() == ShieldType.NONE)){
                    bullet = tooltip.addPara(XLU_MD.armorclad("bad_he_rec"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                        "+" + (int) EXPLOSIVE_PENALTY + "%" );
                    tooltip.setBulletedListMode("   ");
                    tooltip.addPara("%s", 0f, Global.getSettings().getColor("standardTextColor"), gray,
                        XLU_MD.armorclad("bad_he_rec_extra"));
                    tooltip.setBulletedListMode(" • ");
                }
                bullet = tooltip.addPara(XLU_MD.armorclad("bad_tur_rot"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) (100 *(TURRET_PENALTY)) + "%" );
                
		tooltip.addSectionHeading(XLU_MD.base("comp"), Alignment.MID, opad);
                bullet = tooltip.addPara(XLU_MD.armorclad("comp_he_rec"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), gray,
                    XLU_MD.armorclad("comp_he_rec_extra"));
		bullet.setHighlight(XLU_MD.base("highlight_shunt"), XLU_MD.armorclad("comp_he_rec_extra"));
		bullet.setHighlightColors(h, gray);
                
            tooltip.setBulletedListMode(null);
	}
	
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("xlu_");
    }
}
