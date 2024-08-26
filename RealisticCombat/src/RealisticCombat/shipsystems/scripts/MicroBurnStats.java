package RealisticCombat.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public final class MicroBurnStats extends BaseShipSystemScript {

    public void apply(final MutableShipStatsAPI stats,
                      final String id,
                      final State state,
                      final float effectLevel)
    {
		// to slow down ship to its regular top speed while powering drive down
		if (state == State.OUT) stats.getMaxSpeed().unmodify(id);
		else {
			stats.getMaxSpeed().modifyFlat(id, 600f * effectLevel);
			stats.getAcceleration().modifyFlat(id, 1200f * effectLevel);
		}
    }

    public void unapply(final MutableShipStatsAPI stats, final String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
    }
	
    public StatusData getStatusData(final int index, final State state, final float effectLevel) {
		return (index == 0) ? new StatusData("increased engine power", false) : null;
    }

    public float getActiveOverride(final ShipAPI ship) { return -1; }
    
    public float getInOverride(final ShipAPI ship) { return -1; }
    
    public float getOutOverride(final ShipAPI ship) { return -1; }
	
    public float getRegenOverride(final ShipAPI ship) { return -1; }

    public int getUsesOverride(final ShipAPI ship) {
        switch (ship.getHullSize()) {
            case FRIGATE:
            case DESTROYER:
            case CRUISER: return 2;
            default: return -1;
        }
    }
}


