package RealisticCombat.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public final class ManeuveringJetsStats extends BaseShipSystemScript {

    public void apply(final MutableShipStatsAPI stats,
					  final String id,
					  final State state,
					  final float effectLevel)
	{
		if (state == State.OUT) {
			// to slow down ship to its regular top speed while powering drive down
			stats.getMaxSpeed().unmodify(id);
			stats.getMaxTurnRate().unmodify(id);
		} else {
			stats.getMaxSpeed().modifyFlat(id, 50f);
			stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
			stats.getDeceleration().modifyPercent(id, 200f * effectLevel);
			stats.getTurnAcceleration().modifyFlat(id, 30f * effectLevel);
			stats.getTurnAcceleration().modifyPercent(id, 200f * effectLevel);
			stats.getMaxTurnRate().modifyFlat(id, 15f);
			stats.getMaxTurnRate().modifyPercent(id, 100f);
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
		switch (index) {
			case 0: return new StatusData("improved maneuverability", false);
			case 1: return new StatusData("+50 top speed", false);
			default: return null;
		}
    }
}
