package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

public final class AdvancedOptics extends BaseHullMod {

	public static final float BEAM_WEAPON_FLUX_COST_FACTOR = 0.9f;


	@Override
	public boolean isApplicableToShip(final ShipAPI ship) {
	    return !ship.getVariant().getHullMods().contains(HullMods.HIGH_SCATTER_AMP);
	}

	@Override
	public String getUnapplicableReason(final ShipAPI ship) {
	    if (ship.getVariant().getHullMods().contains(HullMods.HIGH_SCATTER_AMP)) {
		return "Incompatible with High Scatter Amplifier";
	    }
	    return null;
	}

	@Override
	public String getDescriptionParam(final int index, final HullSize hullSize) {
	    return "" + (int) (100 * (1 - BEAM_WEAPON_FLUX_COST_FACTOR)) + "%";
	}

	@Override
	public void applyEffectsBeforeShipCreation(final HullSize hullSize,
					           final MutableShipStatsAPI stats,
					           final String id)
	{
	    stats.getBeamWeaponFluxCostMult().modifyMult(id, BEAM_WEAPON_FLUX_COST_FACTOR);
	}
}
