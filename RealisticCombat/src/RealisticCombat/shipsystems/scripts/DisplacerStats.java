package RealisticCombat.shipsystems.scripts;

import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public final class DisplacerStats extends BaseShipSystemScript {
    public StatusData getStatusData(final int index, final State state, final float effectLevel) {
	    return (index == 0) ? new StatusData("out of phase", false) : null;
    }
}
