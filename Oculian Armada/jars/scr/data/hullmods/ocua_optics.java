package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.everyframe.OCUA_BlockedHullmodDisplayScript;
import data.scripts.ids.OCUA_HullMods;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class ocua_optics extends BaseHullMod {

	public static final float OCUA_BEAM_DAMAGE_PENALTY = 15f;
	public static final float BEAM_DAMAGE_PENALTY = 20f;
	public static final float OCUA_BEAM_RANGE_BONUS = 150f;
	public static final float BEAM_RANGE_BONUS = 100f;
	
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static {
            BLOCKED_HULLMODS.add("advancedoptics");
        }
    
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getBeamWeaponRangeBonus().modifyFlat(id, BEAM_RANGE_BONUS);
            if (stats.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD) || stats.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCUTEK_MOD)) {
		stats.getBeamShieldDamageTakenMult().modifyPercent(id, -OCUA_BEAM_DAMAGE_PENALTY);
            } else {
		stats.getBeamShieldDamageTakenMult().modifyPercent(id, -BEAM_DAMAGE_PENALTY);
            }
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.addListener(new ocua_opticsRangeMod());
            
            for (String tmp : BLOCKED_HULLMODS) {
                if (ship.getVariant().getHullMods().contains(tmp)) {
                    ship.getVariant().removeMod(tmp);
                    OCUA_BlockedHullmodDisplayScript.showBlocked(ship);
                }
            }
	}
	
	public static class ocua_opticsRangeMod implements WeaponBaseRangeModifier {
		public ocua_opticsRangeMod() {
		}
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.isBeam()) {
                            if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD) || 
                                    ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCUTEK_MOD)) {
				return OCUA_BEAM_RANGE_BONUS;
                            } else {
				return BEAM_RANGE_BONUS;
                            }
			}
			return 0f;
		}
	}
        
        protected static final float LOAD_OF_BULL = 3f;
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		float pad = 3f;
		Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                Color good = Misc.getPositiveHighlightColor();
                Color ocua = new Color(250,100,175,255);
                Color ocutek = new Color(225,50,100,255);
                float beans_range = BEAM_RANGE_BONUS;
                float beans_dam = BEAM_DAMAGE_PENALTY;
                
                if (!(isForModSpec || ship == null)) {
                    if (ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD) || 
                                    ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCUTEK_MOD)) {
                        beans_range = OCUA_BEAM_RANGE_BONUS;
                        beans_dam = OCUA_BEAM_DAMAGE_PENALTY;
                    }
                }
		
                LabelAPI bullet;
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Beam base range %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) beans_range + " units" );
		bullet.setHighlight("base", "+" + (int) beans_range + " units");
		bullet.setHighlightColors(h, good);
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Beam damage against shields %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "-" + (int) beans_dam + "%" );
                
		tooltip.addSectionHeading("Compatibilities", Alignment.MID, opad);
                bullet = tooltip.addPara("Performs optimally on %s/Ocutek vessels.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), ocua,
                    "Oculian" );
		bullet.setHighlight("Oculian", "Ocutek");
		bullet.setHighlightColors(ocua, ocutek);
                bullet = tooltip.addPara("Can be installed along with %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "High Scatter Amplifier" );
                bullet = tooltip.addPara("Cannot be installed along with %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "Advanced Optics" );
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
            //if (index == 0) return "" + (int) BEAM_RANGE_BONUS + "";
            //if (index == 1) return "" + (int) BEAM_DAMAGE_PENALTY + "%";
            return null;
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
            if (ship.getVariant().getHullMods().contains(HullMods.ADVANCEDOPTICS) ||
                        ship.getVariant().getHullMods().contains(OCUA_HullMods.OCUA_OPTICS_B)){
                return false;
            }
		return true;
	}
	
        @Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains(HullMods.ADVANCEDOPTICS))return "Incompatible with Advanced Optics";
		if (ship.getVariant().getHullMods().contains(OCUA_HullMods.OCUA_OPTICS_B)) return "Oculian Optics already improved";
		return null;
	}
}
