package RealisticCombat.com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

public final class HighScatterAmp extends BaseHullMod {
	
    @Override
    public boolean isApplicableToShip(final ShipAPI ship) {
		return !ship.getVariant().getHullMods().contains(HullMods.ADVANCEDOPTICS);
    }

    @Override
    public String getUnapplicableReason(final ShipAPI ship) {
		return (ship.getVariant().getHullMods().contains(HullMods.ADVANCEDOPTICS))
	         	  ? "Incompatible with Advanced Optics"
	           	  : null;
    }

    @Override
    public String getDescriptionParam(final int index, final HullSize hullSize) { return null; }

    @Override
    public void applyEffectsAfterShipCreation(final ShipAPI ship, final String id) {
        for (WeaponAPI weapon : ship.getAllWeapons()) weapon.getDamage().setForceHardFlux(true);
    }
}









