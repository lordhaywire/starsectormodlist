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

public class xlu_lithoframe extends BaseHullMod {

        public static final float ARMOR_BONUS_BASE = 33f; //25f;
	public static final float ARMOR_FIGHTER = 50f; // 50f;
	public static final float ARMOR_FRIGATE = 50f; // 50f;
	public static final float ARMOR_DESTROYER = 75f; // 75f;
	public static final float ARMOR_CRUISER = 100f; // 100f;
	public static final float ARMOR_CAPITAL = 150f; // 150f;
        
        public static final float ENERGY_RESIST = 20f;
        public static final float ENERGY_RESIST_NO_SH = 33f;
        public static final float BEAM_RESIST = 15f;
        public static final float ENERGY_FLUX = 10f;
        
        public static final float KINETIC_MALUS = 40f;
	public static final float SUPPLY_USE_MULT = 25f;
        
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

            if (hullSize == HullSize.FIGHTER) {
                stats.getEffectiveArmorBonus().modifyFlat(id, ARMOR_FIGHTER);
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
            
            if (!(stats.getVariant().getHullMods().contains("shield_shunt") || 
                    stats.getVariant().getHullSpec().getShieldType() == ShieldType.NONE)) {
                    stats.getEnergyDamageTakenMult().modifyMult(id, 1f - (ENERGY_RESIST_NO_SH / 100f));
            } else {
                stats.getEnergyDamageTakenMult().modifyMult(id, 1f - (ENERGY_RESIST / 100f));
            }
            stats.getBeamDamageTakenMult().modifyMult(id, 1f - (BEAM_RESIST / 100f));
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (ENERGY_FLUX / 100f));
            
            stats.getKineticDamageTakenMult().modifyMult(id, 1f + (KINETIC_MALUS / 100f));
            stats.getSuppliesToRecover().modifyMult(id, 1f + (SUPPLY_USE_MULT / 100f));
            
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
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
                tooltip.addPara(XLU_MD.lithoframe("flavor"), gray, opad);
                tooltip.addPara(XLU_MD.lithoframe("flavor2"), gray, pad);
                
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading(XLU_MD.base("good"), Alignment.MID, opad);
                bullet = tooltip.addPara(XLU_MD.armorclad("good_arm_str"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) armorsize + "", "" + (int) ARMOR_BONUS_BASE + "%" );
                bullet = tooltip.addPara(XLU_MD.lithoframe("good_energy_rec"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) ENERGY_RESIST + "%", "-" + (int) BEAM_RESIST + "%" );
		bullet.setHighlight("-" + (int) ENERGY_RESIST + "%", "-" + (int) BEAM_RESIST + "%", XLU_MD.lithoframe("good_energy_rec_h"));
		bullet.setHighlightColors(good, good, gray);
                bullet = tooltip.addPara(XLU_MD.lithoframe("good_wep_flux"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "-" + (int) ENERGY_FLUX + "%" );
                
		tooltip.addSectionHeading(XLU_MD.base("bad"), Alignment.MID, opad);
                bullet = tooltip.addPara(XLU_MD.lithoframe("bad_recovery"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) SUPPLY_USE_MULT + "%" );
                bullet = tooltip.addPara(XLU_MD.lithoframe("bad_kinetic_rec"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) (KINETIC_MALUS) + "%" );
                    tooltip.setBulletedListMode("   ");
                    tooltip.addPara("%s", 0f, Global.getSettings().getColor("standardTextColor"), gray,
                        XLU_MD.lithoframe("bad_kineti_rec_extra"));
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading(XLU_MD.base("comp"), Alignment.MID, opad);
                bullet = tooltip.addPara(XLU_MD.lithoframe("comp_energy_rec"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), gray,
                    "-" + (int) ENERGY_RESIST_NO_SH + "%");
		bullet.setHighlight(XLU_MD.base("highlight_shunt"), "-" + (int) ENERGY_RESIST_NO_SH + "%");
		bullet.setHighlightColors(h, good);
                    tooltip.setBulletedListMode("   ");
                    tooltip.addPara("%s", 0f, Global.getSettings().getColor("standardTextColor"), gray, XLU_MD.lithoframe("comp_energy_rec_extra"));
                tooltip.setBulletedListMode(" • ");
                bullet = tooltip.addPara(XLU_MD.lithoframe("comp_armorclad"), LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), gray,
                    "");
		bullet.setHighlight(XLU_MD.base("highlight_armorclad"));
		bullet.setHighlightColors(h);
                
            tooltip.setBulletedListMode(null);
	}
	
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("xlu_");
    }
}
