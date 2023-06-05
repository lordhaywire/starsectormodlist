package data.shipsystems.scripts;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static com.fs.starfarer.api.impl.combat.RecallDeviceStats.getFighters;

public class OCUA_TemporalOverclockStats extends BaseShipSystemScript {
	public static final Object KEY_JITTER = new Object();
        
	public static final float MAX_TIME_MULT = 4f;
	public static final float MIN_TIME_MULT = 0.1f;
	
	public static final Color JITTER_COLOR = new Color(90,165,255,55);
	public static final Color JITTER_UNDER_COLOR = new Color(255,155,155,150);
	public static final Color JITTER_FINAL_COLOR = new Color(90,165,255,155);

	public static final float BREAK_SPEED = 0.1f;
	public static final float BREAK_DECELERATION = 3f;
	
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		
		float jitterLevel = effectLevel;
		float jitterRangeBonus = 0;
		float maxRangeBonus = 10f;
		if (state == State.IN) {
			jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
			if (jitterLevel > 1) {
				jitterLevel = 1f;
			}
			jitterRangeBonus = jitterLevel * maxRangeBonus;
		} else if (state == State.ACTIVE) {
			jitterLevel = 1f;
			jitterRangeBonus = maxRangeBonus;
		} else if (state == State.OUT) {
			jitterRangeBonus = jitterLevel * maxRangeBonus;
		}
		jitterLevel = (float) Math.sqrt(jitterLevel);
		effectLevel *= effectLevel;
		
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
		
	
		float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
                if (player) {
                   Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
                } else {
                    Global.getCombatEngine().getTimeMult().unmodify(id);
                }
		if (state == State.IN) {
                    //stats.getTimeMult().modifyMult(id, 1f);
                    stats.getMaxSpeed().modifyMult(id, BREAK_SPEED);
                    stats.getAcceleration().modifyMult(id, BREAK_SPEED);
                    stats.getTurnAcceleration().modifyMult(id, BREAK_SPEED * 2);
                    stats.getMaxTurnRate().modifyMult(id, BREAK_SPEED * 2);
                    stats.getDeceleration().modifyMult(id, BREAK_DECELERATION);
                    
                    ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);
                } else if (state == State.ACTIVE) {
                    stats.getMaxSpeed().unmodify(id);
                    stats.getAcceleration().unmodify(id);
                    stats.getTurnAcceleration().unmodify(id);
                    stats.getDeceleration().unmodify(id);
                    stats.getMaxTurnRate().unmodify(id);
                    
                    stats.getTimeMult().modifyMult(id, shipTimeMult);
                    
			float fighterjitterLevel = effectLevel;
			float maxFighterRangeBonus = 5f;
			float jitterFighterRangeBonus = fighterjitterLevel * maxFighterRangeBonus;
			for (ShipAPI fighter : getFighters(ship)) {
				if (fighter.isHulk()) continue;
                                
				MutableShipStatsAPI fStats = fighter.getMutableStats();
				
                                fStats.getTimeMult().modifyMult(id, shipTimeMult);
                        
				if (fighterjitterLevel > 0.75) {
					fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, fighterjitterLevel, 5, 0f, jitterFighterRangeBonus);
					fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, fighterjitterLevel, 2, 0f, 0 + jitterFighterRangeBonus * 1f);
				}
			}
                        
                    ship.setJitterUnder(this, JITTER_FINAL_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);
                } else if (state == State.OUT) {
                    stats.getTimeMult().unmodify(id);
                    for (ShipAPI fighter : getFighters(ship)) {
                        MutableShipStatsAPI fStats = fighter.getMutableStats();
                        fStats.getTimeMult().unmodify(id);
                    }
                    Global.getCombatEngine().getTimeMult().unmodify(id);
                }
		
                ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0,0,0,0), effectLevel, 0.5f);
		ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}

		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
                
		for (ShipAPI fighter : getFighters(ship)) {
                    MutableShipStatsAPI fStats = fighter.getMutableStats();
                    fStats.getTimeMult().unmodify(id);
                }
                    
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
		if ((index == 0) && (state == State.IN)) {
			return new StatusData("engine power diverting", false);
		}
		if ((index == 0) && (state == State.ACTIVE)) {
			return new StatusData("time flow altered", false);
		}
		return null;
	}
}








