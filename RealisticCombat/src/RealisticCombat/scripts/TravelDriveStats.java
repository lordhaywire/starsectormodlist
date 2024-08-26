package RealisticCombat.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public final class TravelDriveStats extends BaseShipSystemScript {

    public void apply(final MutableShipStatsAPI stats,
					  final String id,
					  final State state,
					  final float effectLevel)
	{
		// to slow down ship to its regular top speed while powering drive down
		if (state == ShipSystemStatsScript.State.OUT) stats.getMaxSpeed().unmodify(id);
		else {
			stats.getMaxSpeed().modifyFlat(id, 600f * effectLevel);
			stats.getAcceleration().modifyFlat(id, 600f * effectLevel);
		}
    }

    public void unapply(final MutableShipStatsAPI stats, final String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
    }
	
    public StatusData getStatusData(final int index, final State state, final float effectLevel) {
		return (index == 0) ? new StatusData("increased engine power", false) : null;
    }
}
