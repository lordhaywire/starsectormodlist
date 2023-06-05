package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class xlu_PowerBurnStats extends BaseShipSystemScript {
    
	public static final float SPEED_BONUS = 1f;
	public static final float ACCEL_BONUS = 1f;
	public static final float FLUX_BONUS = 0.5f;

        @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
                float speedMult = 1f + SPEED_BONUS * effectLevel;
                float accelMult = 1f + ACCEL_BONUS * effectLevel;
                float fluxMult = 1f + FLUX_BONUS * effectLevel;
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
		} else {
                        stats.getMaxSpeed().modifyMult(id, speedMult);
			stats.getMaxSpeed().modifyFlat(id, 50f * effectLevel);
                        stats.getAcceleration().modifyMult(id, accelMult);
			stats.getAcceleration().modifyFlat(id, 25f * effectLevel);
                        stats.getFluxDissipation().modifyMult(id, fluxMult * effectLevel);
		}
	}
        @Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		stats.getFluxDissipation().unmodify(id);
	}
	
        @Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
                float fluxMult = 1f + FLUX_BONUS * effectLevel;
		float fluxDi = (int) ((fluxMult - 1f) * 100f);
		if (index == 0) return new StatusData("increased engine power", false); 
                if (index == 1) return new StatusData("flux dissipation +" + (int) fluxDi + "%", false);
		return null;
	}
}
