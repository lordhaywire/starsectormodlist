package RealisticCombat.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

import java.awt.*;

public final class PlasmaJetsStats extends BaseShipSystemScript {

	private static final float SPEED_BONUS = 125f, TURN_BONUS = 20f;
	
	private static final Color color = new Color(100,255,100,255);
	
	public void apply(final MutableShipStatsAPI stats,
					  final String id,
					  final State state,
					  final float effectLevel)
	{
		// to slow down ship to its regular top speed while powering drive down
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id);
			stats.getMaxTurnRate().unmodify(id);
		} else {
			stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
			stats.getAcceleration().modifyPercent(id, SPEED_BONUS * 3f * effectLevel);
			stats.getDeceleration().modifyPercent(id, SPEED_BONUS * 3f * effectLevel);
			stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS * effectLevel);
			stats.getTurnAcceleration().modifyPercent(id, TURN_BONUS * 5f * effectLevel);
			stats.getMaxTurnRate().modifyFlat(id, 15f);
			stats.getMaxTurnRate().modifyPercent(id, 100f);
		}
		
		if (!(stats.getEntity() instanceof ShipAPI)) return;
		final ShipAPI ship = (ShipAPI) stats.getEntity();
		ship.getEngineController().fadeToOtherColor(this, color, new Color(0,0,0,0),
													effectLevel, 0.67f);
		ship.getEngineController().extendFlame(this, 2f * effectLevel, 0f * effectLevel,
											  0f * effectLevel);

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
			case 1: return new StatusData("+" + (int)SPEED_BONUS + " top speed", false);
			default: return null;
		}
	}
}
