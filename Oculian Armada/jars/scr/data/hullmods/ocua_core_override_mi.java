package data.hullmods;

import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import data.scripts.ids.OCUA_HullMods;

public class ocua_core_override_mi extends BaseHullMod {

        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "Dual Core configuration";
		return null;
	}

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_OCULUS_MOD) &&
                        !ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_MI_MOD);
        }
    
	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
		if (ship == null || ship.getVariant() == null) return true; // autofit
		if (!ship.getVariant().hasHullMod("ocua_core_override_mi")) return true; // can always add
                
                if ((ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CH) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHCR) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHPL) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHQU) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHVP) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CR) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CRPL) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CRQU) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CRVP) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_PL) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_PLVP) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QU) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QUPL) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QUVP) ||
                        ship.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_VP))) return false;
		return true;
	}

	@Override
	public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
		return "Dual Mikanate configuration must be removed before normalizing the core.";
	}
	
        @Override
        public String getUnapplicableReason(ShipAPI ship) {
            if (ship == null || ship.getVariant().getHullMods().contains("ocua_oculus_mod") && !ship.getVariant().getHullMods().contains("ocua_mi_mod")) {
               return "Must be a non-Mikanate Oculian hull";
            }
            return null;
        }
}
