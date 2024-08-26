package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public final class TargetingSupercomputer extends BaseHullMod {
	@Override
	public String getDescriptionParam(final int index, final HullSize hullSize) {
		final float percent = Math.abs(1 - RealisticCombat.settings.ThreeDimensionalTargeting.getLeadingFactor());
		return "" + (int) (percent * 100) + "%";
	}
}
