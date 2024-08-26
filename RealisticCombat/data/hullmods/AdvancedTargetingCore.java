package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public final class AdvancedTargetingCore extends BaseHullMod {

    @Override
    public boolean isApplicableToShip(final ShipAPI ship) {
	return true;
    }

    @Override
    public String getUnapplicableReason(final ShipAPI ship) {
	return null;
    }

    @Override
    public String getDescriptionParam(final int index, final HullSize hullSize) {
	final float percent = (1 - RealisticCombat.settings.ThreeDimensionalTargeting.getLeadingFactor()) * 100;
	return "" + (int) Math.round(percent) + "%";
    }
}
