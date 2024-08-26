package RealisticCombat.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.PhaseCloakSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public final class PhaseCloakStats extends BaseShipSystemScript {

	private static final boolean FLUX_LEVEL_AFFECTS_SPEED = true;

    private static final float
			SHIP_ALPHA_MULT = 0.25f,
			VULNERABLE_FRACTION = 0,

			MAX_TIME_MULT = 3,

			MIN_SPEED_MULT = 1,
			MAX_SPEED_MULT = 2,
			SPEED_MULT_RANGE = MAX_SPEED_MULT - MIN_SPEED_MULT,
			BASE_FLUX_LEVEL_FOR_MIN_SPEED = 0.5f;
	
    protected Object
		STATUSKEY1 = new Object(),
		STATUSKEY2 = new Object(),
		STATUSKEY3 = new Object(),
		STATUSKEY4 = new Object();
	
	
    public static float getMaxTimeMult(final MutableShipStatsAPI stats) {
		return 1f + (MAX_TIME_MULT - 1f) * stats.getDynamic().getValue(Stats.PHASE_TIME_BONUS_MULT);
    }

    protected boolean isDisruptable(final ShipSystemAPI cloak) {
		return cloak.getSpecAPI().hasTag(Tags.DISRUPTABLE);
    }
	
    protected float getDisruptionLevel(final ShipAPI ship) {
		if (!(FLUX_LEVEL_AFFECTS_SPEED)) return 0f;
		float threshold = ship.getMutableStats().getDynamic().getMod(
				Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).computeEffective(
					BASE_FLUX_LEVEL_FOR_MIN_SPEED);
		if (threshold <= 0) return 1f;
		float level = ship.getHardFluxLevel() / threshold;
		return (level > 1f) ? 1f : level;
    }
	
    protected void maintainStatus(final ShipAPI playerShip, final float effectLevel) {
		ShipSystemAPI cloak = playerShip.getPhaseCloak();
		if (cloak == null) cloak = playerShip.getSystem();
		if (cloak == null) return;

		if (effectLevel > VULNERABLE_FRACTION)
			Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
				cloak.getSpecAPI().getIconSpriteName(), cloak.getDisplayName(),
			"time flow altered", false);

		if (!FLUX_LEVEL_AFFECTS_SPEED || effectLevel < VULNERABLE_FRACTION) return;
		if (getDisruptionLevel(playerShip) <= 0f)
			Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
				cloak.getSpecAPI().getIconSpriteName(), "phase coils stable",
				"top speed at 100%", false);
		else {
			String speedPercentStr = Math.round(getSpeedMult(playerShip, effectLevel) * 100f) + "%";
			Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
				cloak.getSpecAPI().getIconSpriteName(), "phase coil stress",
				"top speed at " + speedPercentStr, true);
		}
    }
	
    public float getSpeedMult(final ShipAPI ship, final float effectLevel) {
		if (getDisruptionLevel(ship) <= 0f) return 1f;
		return MIN_SPEED_MULT
				+ SPEED_MULT_RANGE
				* (1f - getDisruptionLevel(ship) * effectLevel);
    }
		
    public void apply(final MutableShipStatsAPI stats,
		      		  String id,
					  final State state,
					  final float effectLevel)
    {
		if (!(stats.getEntity() instanceof ShipAPI)) return;
		final ShipAPI ship = (ShipAPI) stats.getEntity();

		final boolean player = ship == Global.getCombatEngine().getPlayerShip();
		id = id + "_" + ship.getId();

		if (player) maintainStatus(ship, effectLevel);

		if (Global.getCombatEngine().isPaused()) return;

		ShipSystemAPI cloak = ship.getPhaseCloak();
		if (cloak == null) cloak = ship.getSystem();
		if (cloak == null) return;

		if (FLUX_LEVEL_AFFECTS_SPEED
			&& (state == State.ACTIVE || state == State.OUT || state == State.IN)) {
				final float mult = getSpeedMult(ship, effectLevel);
				if (mult < 1f) stats.getMaxSpeed().modifyMult(id + "_2", mult);
				else stats.getMaxSpeed().unmodifyMult(id + "_2");
				((PhaseCloakSystemAPI) cloak).setMinCoilJitterLevel(getDisruptionLevel(ship));
			}

		if (state == State.COOLDOWN || state == State.IDLE) { unapply(stats, id); return; }

		final float
			speedPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f),
			accelPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).computeEffective(0f);
		stats.getMaxSpeed().modifyPercent(id, speedPercentMod * effectLevel);
		stats.getAcceleration().modifyPercent(id, accelPercentMod * effectLevel);
		stats.getDeceleration().modifyPercent(id, accelPercentMod * effectLevel);

		final float
			speedMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).getMult(),
			accelMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).getMult();
		stats.getMaxSpeed().modifyMult(id, speedMultMod * effectLevel);
		stats.getAcceleration().modifyMult(id, accelMultMod * effectLevel);
		stats.getDeceleration().modifyMult(id, accelMultMod * effectLevel);

		if (state == State.IN || state == State.ACTIVE) ship.setPhased(true);
		else if (state == State.OUT) {
			ship.setPhased(effectLevel > 0.5f);

			ship.setExtraAlphaMult(1f - (1f - SHIP_ALPHA_MULT) * effectLevel);
			ship.setApplyExtraAlphaToEngines(true);

			final float shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * effectLevel;
			stats.getTimeMult().modifyMult(id, shipTimeMult);
			if (player) Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
			else Global.getCombatEngine().getTimeMult().unmodify(id);
		}
    }
    
    public void unapply(final MutableShipStatsAPI stats, final String id) {
		if (!(stats.getEntity() instanceof ShipAPI)) return;

		final ShipAPI ship = (ShipAPI) stats.getEntity();

		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);

		stats.getMaxSpeed().unmodify(id);
		stats.getMaxSpeed().unmodifyMult(id + "_2");
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);

		ship.setPhased(false);
		ship.setExtraAlphaMult(1f);

		ShipSystemAPI cloak = ship.getPhaseCloak();
		if (cloak == null) cloak = ship.getSystem();
		((PhaseCloakSystemAPI) cloak).setMinCoilJitterLevel(0f);
    }
    
    public StatusData getStatusData(final int index, final State state, final float effectLevel) {
		return null;
	}
}
