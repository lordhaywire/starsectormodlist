package RealisticCombat.com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public final class BallisticRangefinder extends BaseHullMod {

	@Override
	public String getDescriptionParam(final int index, final HullSize hullSize) {
		final float percent = Math.abs(1 - RealisticCombat.settings.ThreeDimensionalTargeting.getRangingFactor());
		return "" + (int) (percent * 100) + "%";
	}

    @Override
    public boolean isApplicableToShip(final ShipAPI ship) {
		return getUnapplicableReason(ship) == null;
	}

	@Override
    public String getUnapplicableReason(final ShipAPI ship) {
        return (ship != null
				&& ship.getHullSize() != HullSize.CAPITAL_SHIP
				&& ship.getHullSize() != HullSize.DESTROYER
				&& ship.getHullSize() != HullSize.CRUISER)
					? "Can only be installed on destroyer-class hulls and larger"
					: null;
    }
}