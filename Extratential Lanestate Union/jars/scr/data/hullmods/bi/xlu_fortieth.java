package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponOPCostModifier;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public class xlu_fortieth extends BaseHullMod {

        public static final float ARMOR_BONUS_BASE = 1.1f;
	public static final float ARMOR_REDUCT_BONUS = 20f;
	public static final float HE_PAIN = 10f;
	public static final float DEGRADE_INCREASE_PERCENT = 100f;
        //public static final float BALLISTIC_FLUX = 10f;
        
        //Too much, shifted to base cost instead
	//public static final float SUPPLY_USE_MULT = 20f;
        
        @Override
        public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            //stats.getEffectiveArmorBonus().modifyMult(id, ARMOR_BONUS_BASE);
            stats.getArmorDamageTakenMult().modifyMult(id, (1 - (ARMOR_REDUCT_BONUS / 100)));
            stats.getHighExplosiveDamageTakenMult().modifyMult(id, (1 + (HE_PAIN / 100)));
            //stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 - (BALLISTIC_FLUX / 100)));
            
            stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);
            
            stats.addListener(new WeaponOPCostModifier() {
                @Override
		public int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost) {
                    if (weapon.getTags().contains("xlu_40th")){
                        if (weapon.getSize().equals(WeaponAPI.WeaponSize.SMALL)) {
                            return (currCost - (int) 2);
                        } else if (weapon.getSize().equals(WeaponAPI.WeaponSize.MEDIUM)) {
                            return (currCost - (int) 4);
                        } else if (weapon.getSize().equals(WeaponAPI.WeaponSize.LARGE)) {
                            return (currCost - (int) 6);
                        }
                    }
                    return currCost;
		}
            });
        }

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

        @Override
        public String getDescriptionParam(int index, HullSize hullSize) {
            if (index == 0) return "" + (int) ARMOR_REDUCT_BONUS + "%";
            if (index == 1) return "2/4/6";
            if (index == 2) return "" + (int) DEGRADE_INCREASE_PERCENT + "%";
            else {
                return null;
            }
        }

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
            return ship != null && ship.getHullSpec().getHullId().startsWith("xlu_");
        }
}
