package data.hullmods.shh;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import data.scripts.ids.XLU_HullMods;
import java.util.HashSet;
import java.util.Set;

public class xlu_z_strikecraft_hullmod extends BaseHullMod {

        public static final float BALLISTIC_AMMO = 100f;
        public static final float BALLISTIC_FLUX = 80f;
        
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

            stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 -(BALLISTIC_FLUX / 100)));
            
            if (!stats.getVariant().getHullSpec().getBuiltInMods().contains("xlu_breezer_rounds")) {
                stats.getBallisticAmmoBonus().modifyPercent(id, BALLISTIC_AMMO);
            }
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
	if(index == 0) return "" + (int) BALLISTIC_AMMO + "%";
	if(index == 1) return "" + (int) BALLISTIC_FLUX + "%";
        else {
            return null;
        }
    }
}
