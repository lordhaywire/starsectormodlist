package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class eridani extends BaseHullMod {
	
	public static final float SPEED_MULT = 0.9f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getMaxSpeed().modifyFlat(id, (Float) mag.get(hullSize) * -1f);
		stats.getMaxSpeed().modifyMult(id, SPEED_MULT);
	}



	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) {
			return "90";
		}
		if (index == 1) {
			return "" + (int) Math.round((1f - SPEED_MULT) * 100f) + "%";
		}		
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		//return ship != null && ship.getShield() == null && ship.getPhaseCloak() == null;
		return ship != null && ship.getHullSpec().getDefenseType() == ShieldType.NONE;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship != null && ship.getHullSpec().getDefenseType() == ShieldType.PHASE) {
			return "Ship can not have shields";
		} 
		return "Ship already has shields";
	}
}
