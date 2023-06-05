package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;

public class xlu_VectorPunchStats extends BaseShipSystemScript {
    
        public static final float PROJ_SPEED_BONUS = 50f;
        public static final float SPEED_BONUS = 60f;
        public static final float ACCEL_BONUS = 1900f;
    
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
		} else {
			stats.getProjectileSpeedMult().modifyPercent(id, PROJ_SPEED_BONUS * effectLevel);
			stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
			stats.getAcceleration().modifyPercent(id, ACCEL_BONUS * effectLevel);
			stats.getDeceleration().modifyPercent(id, ACCEL_BONUS * effectLevel);
		}
		
		if (stats.getEntity() instanceof ShipAPI && false) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			String key = ship.getId() + "_" + id;
			Object test = Global.getCombatEngine().getCustomData().get(key);
			if (state == State.IN) {
				if (test == null && effectLevel > 0.2f) {
					Global.getCombatEngine().getCustomData().put(key, new Object());
					ship.getEngineController().getExtendLengthFraction().advance(1f);
					for (ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
						if (engine.isSystemActivated()) {
							ship.getEngineController().setFlameLevel(engine.getEngineSlot(), 1f);
						}
					}
				}
			} else {
				Global.getCombatEngine().getCustomData().remove(key);
			}
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getProjectileSpeedMult().unmodify(id);
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		int percent = (int) ((ACCEL_BONUS + 100f) * effectLevel / 100);
		float percent2 = SPEED_BONUS * effectLevel;
		float percent3 = PROJ_SPEED_BONUS * effectLevel;
		if (index == 0) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("x" + Misc.getRoundedValueMaxOneAfterDecimal(percent) + " Acceleration", false);
		}
                else if (index == 1) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("+" + Misc.getRoundedValueMaxOneAfterDecimal(percent2) + " Top Speed", false);
		}
                else if (index == 2) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("+" + Misc.getRoundedValueMaxOneAfterDecimal(percent3) + "% Weapon Velocity", false);
		}
		return null;
	}
}
