package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public final class DedicatedTargetingCore extends BaseHullMod {

	@Override
	public String getDescriptionParam(final int index, final HullSize hullSize) {
		final float percent = Math.abs(1 - RealisticCombat.settings.ThreeDimensionalTargeting.getLeadingFactor());
		return "" + (int) (percent * 100) + "%";
	}

	@Override
	public boolean isApplicableToShip(final ShipAPI ship) {
		return (ship.getHullSize() == HullSize.CAPITAL_SHIP
				|| ship.getHullSize() == HullSize.CRUISER)
				&& !(ship.getVariant().getHullMods().contains("targetingunit")
				     || ship.getVariant().getHullMods().contains("advancedcore"));
	}

	@Override
	public String getUnapplicableReason(final ShipAPI ship) {
		if (ship == null
			|| !(ship.getHullSize() == HullSize.CAPITAL_SHIP
			     || ship.getHullSize() == HullSize.CRUISER)) {
			return "Can only be installed on cruisers and capital ships";
		} if (ship.getVariant().getHullMods().contains("targetingunit")) {
			return "Incompatible with Integrated Targeting Unit";
		} if (ship.getVariant().getHullMods().contains("advancedcore")) {
			return "Incompatible with Advanced Targeting Core";
		} return null;
	}
}
