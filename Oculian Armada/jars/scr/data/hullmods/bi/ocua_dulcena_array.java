package data.hullmods.bi;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import data.scripts.ids.OCUA_HullMods;

public class ocua_dulcena_array extends BaseHullMod {
    public static final float CARRIER_HANGAR = 2f;
    public static final float ENE_RAN_BONUS = 10f;
    public static final float FLUX_MULT = 0.9f;
   
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //stats.getEnergyWeaponRangeBonus().modifyPercent(id, ENE_RAN_BONUS);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
        stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -2);
        stats.getDynamic().getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, -2);
        stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -3);
        stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -3);
        //stats.getBeamWeaponFluxCostMult().modifyMult(id, FLUX_MULT);
        
        if (stats.getVariant().hasHullMod(OCUA_HullMods.OCUA_CORE_QUANTIX) ||
                stats.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CHQU) ||
                stats.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_CRQU) ||
                stats.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QU) ||
                stats.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QUPL) ||
                stats.getVariant().hasHullMod(OCUA_HullMods.OCUA_C_MI_QUVP)) {
            stats.getNumFighterBays().modifyFlat(id, CARRIER_HANGAR);
            
            stats.getFighterRefitTimeMult().unmodify(OCUA_HullMods.OCUA_CORE_QUANTIX);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).unmodify(OCUA_HullMods.OCUA_CORE_QUANTIX);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).unmodify(OCUA_HullMods.OCUA_CORE_QUANTIX);
            stats.getZeroFluxSpeedBoost().unmodify(OCUA_HullMods.OCUA_CORE_QUANTIX);
            
            stats.getFighterRefitTimeMult().unmodify(OCUA_HullMods.OCUA_C_MI_CHQU);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).unmodify(OCUA_HullMods.OCUA_C_MI_CHQU);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).unmodify(OCUA_HullMods.OCUA_C_MI_CHQU);
            stats.getZeroFluxSpeedBoost().unmodify(OCUA_HullMods.OCUA_C_MI_CHQU);
            
            stats.getFighterRefitTimeMult().unmodify(OCUA_HullMods.OCUA_C_MI_CRQU);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).unmodify(OCUA_HullMods.OCUA_C_MI_CRQU);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).unmodify(OCUA_HullMods.OCUA_C_MI_CRQU);
            stats.getZeroFluxSpeedBoost().unmodify(OCUA_HullMods.OCUA_C_MI_CRQU);
            
            stats.getFighterRefitTimeMult().unmodify(OCUA_HullMods.OCUA_C_MI_QU);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).unmodify(OCUA_HullMods.OCUA_C_MI_QU);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).unmodify(OCUA_HullMods.OCUA_C_MI_QU);
            stats.getZeroFluxSpeedBoost().unmodify(OCUA_HullMods.OCUA_C_MI_QU);
            
            stats.getFighterRefitTimeMult().unmodify(OCUA_HullMods.OCUA_C_MI_QUPL);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).unmodify(OCUA_HullMods.OCUA_C_MI_QUPL);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).unmodify(OCUA_HullMods.OCUA_C_MI_QUPL);
            stats.getZeroFluxSpeedBoost().unmodify(OCUA_HullMods.OCUA_C_MI_QUPL);
            
            stats.getFighterRefitTimeMult().unmodify(OCUA_HullMods.OCUA_C_MI_QUVP);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).unmodify(OCUA_HullMods.OCUA_C_MI_QUVP);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).unmodify(OCUA_HullMods.OCUA_C_MI_QUVP);
            stats.getZeroFluxSpeedBoost().unmodify(OCUA_HullMods.OCUA_C_MI_QUVP);
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if (index == 0) return "" + (int) (100 - (100 * (FLUX_MULT))) + "%";
	if (index == 1) return "2/3";
	if (index == 2) return "2 additional hangar bays";
	if (index == 3) return "does not receive default Quantix properties";
	return null;
    }
	
    @Override
    public boolean affectsOPCosts() {
    	return true;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getHullId().startsWith("ocua_");
    }
}
