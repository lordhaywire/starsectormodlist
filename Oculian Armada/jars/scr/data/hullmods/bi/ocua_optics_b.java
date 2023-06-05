package data.hullmods.bi;

import data.hullmods.*;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.everyframe.OCUA_BlockedHullmodDisplayScript;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class ocua_optics_b extends BaseHullMod {

	public static final float OCUA_BEAM_RANGE_BONUS = 150f;
	
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static {
            BLOCKED_HULLMODS.add("advancedoptics");
        }
    
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
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
				return OCUA_BEAM_RANGE_BONUS;
			}
			return 0f;
		}
	}
        
        protected static final float LOAD_OF_BULL = 3f;
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
                Color good = Misc.getPositiveHighlightColor();
                float beans_range = OCUA_BEAM_RANGE_BONUS;
                
                LabelAPI bullet;
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Beam base range %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) beans_range + " units" );
		bullet.setHighlight("base", "+" + (int) beans_range + " units");
		bullet.setHighlightColors(h, good);
                
		tooltip.addSectionHeading("Compatibilities", Alignment.MID, opad);
                bullet = tooltip.addPara("Cannot be improved further with the modular %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "Oculian Optimized Optics" );
                
            tooltip.setBulletedListMode(null);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
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
