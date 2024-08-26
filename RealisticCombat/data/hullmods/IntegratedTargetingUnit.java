package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public final class IntegratedTargetingUnit extends BaseHullMod {

	@Override
	public boolean isApplicableToShip(final ShipAPI ship) {
		return !ship.getVariant().getHullMods().contains("dedicated_targeting_core")
				&& !ship.getVariant().getHullMods().contains("advancedcore");
	}

	@Override
	public String getUnapplicableReason(final ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains("dedicated_targeting_core"))
			return "Incompatible with Dedicated Targeting Core";
		if (ship.getVariant().getHullMods().contains("advancedcore"))
			return "Incompatible with Advanced Targeting Core";
		return null;
	}

	@Override
	public String getDescriptionParam(final int index, final HullSize hullSize) {
		final float percent = Math.abs(1 - RealisticCombat.settings.ThreeDimensionalTargeting.getLeadingFactor());
		return "" + (int) (percent * 100) + "%";
	}
}
