package data.hullmods;

import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import data.scripts.ids.OCUA_HullMods;

public class ocua_clockoverdrive extends BaseHullMod {

	public static final float OVERLOAD_PENALTY = 2.0f;
	public static final float WEAPON_REPAIR_MULT = 1.33f;
	public static final float REPAIR_MULT = 1.5f;
	//public static final float EXPLOSIVE_SIZE = 2.0f;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_PENALTY);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, WEAPON_REPAIR_MULT);
		stats.getSuppliesToRecover().modifyMult(id, REPAIR_MULT);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
            if(index == 0) return "cancels the Ordinance penalty for non-Oculian weapons";
            if(index == 1) return "" + (int) (OVERLOAD_PENALTY * 100 - 100) + "%";
            if(index == 2) return "" + (int) (WEAPON_REPAIR_MULT * 100 - 100) + "%";
            if(index == 3) return "removes Shield Movement and EMP resistance bonuses";
            if(index == 4) return "" + (int) (REPAIR_MULT * 100 - 100) + "%";
            
            else {
                return null;
            }
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
		if (ship == null || ship.getVariant() == null) return true; // autofit
		if (!ship.getVariant().hasHullMod("ocua_clockoverdrive")) return true; // can always add

		for (String slotId : ship.getVariant().getFittedWeaponSlots()) {
			WeaponSpecAPI overslot = ship.getVariant().getWeaponSpec(slotId);
			if (!overslot.getWeaponId().contains("ocua_") && !overslot.getType().equals(WeaponAPI.WeaponType.BUILT_IN)) return false;
		}
		return true;
	}

	@Override
	public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
		return "Cannot be uninstalled until all non-Oculian turrets are removed.";
	}
	
        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
            return ship != null && ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD);
        }
    

        @Override
        public String getUnapplicableReason(ShipAPI ship) {
            if (ship == null || !ship.getVariant().getHullMods().contains("ocua_oculus_mod")) {
               return "Must be an Oculian hull";
            }
        
            return null;
        }

}
