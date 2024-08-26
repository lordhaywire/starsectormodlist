package data.scripts.hullmods;

// import com.fs.graphics.util.Fader.State;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;



import com.fs.starfarer.api.campaign.CampaignFleetAPI;


public class acs_wandererlogistic extends BaseHullMod {

    private static final float FUEL_ADDED_VAGABOND = 500f;
    private static int BURN_LEVEL_BONUS = 1;
    private static final float MAINTENANCE_MULT_VAGABOND = 0.5f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        if (Global.getSector().getPlayerFleet() == null) {

            return;
        }
        
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        int fleetNumber = fleet.getFleetData().getNumMembers();

        if (fleetNumber <= 6){
            stats.getFuelMod().modifyFlat(id, FUEL_ADDED_VAGABOND);
            stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_BONUS);
            stats.getSuppliesPerMonth().modifyMult(id, MAINTENANCE_MULT_VAGABOND);
        } else{

            stats.getFuelMod().unmodify(id);
            stats.getMaxBurnLevel().unmodify(id);
            stats.getSuppliesPerMonth().unmodify(id);
        };

    }

    public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FUEL_ADDED_VAGABOND + "%";
        if (index == 1) return "" + "50" + "%";
        if (index == 2) return "" + (int) BURN_LEVEL_BONUS;
		return null;
	}
}
