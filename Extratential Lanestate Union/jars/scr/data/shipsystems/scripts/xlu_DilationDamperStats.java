package data.shipsystems.scripts;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import java.util.HashMap;
import java.util.Map;

public class xlu_DilationDamperStats extends BaseShipSystemScript {
	public static final float MAX_TIME_MULT = 0.33f;
	public static final float MIN_TIME_MULT = 0.1f;
	//public static final float READINESS_MULT = 0.5f;
	
	public static final Color JITTER_COLOR = new Color(10,25,155,15);
	public static final Color JITTER_UNDER_COLOR = new Color(10,25,155,55);

	private static final Map mag = new HashMap();
	private static final Map mag2 = new HashMap();
	static {
		mag.put(HullSize.FIGHTER, 0.33f);
		mag.put(HullSize.FRIGATE, 0.33f);
		mag.put(HullSize.DESTROYER, 0.33f);
		mag.put(HullSize.CRUISER, 0.5f);
		mag.put(HullSize.CAPITAL_SHIP, 0.5f);
	}
	static {
		mag2.put(HullSize.FIGHTER, 200f);
		mag2.put(HullSize.FRIGATE, 200f);
		mag2.put(HullSize.DESTROYER, 300f);
		mag2.put(HullSize.CRUISER, 400f);
		mag2.put(HullSize.CAPITAL_SHIP, 500f);
	}
        
	protected Object STATUSKEY1 = new Object();
	protected Object STATUSKEY2 = new Object();
	
        @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;
		//effectLevel = 1f;
		
		float mult = (Float) mag.get(ShipAPI.HullSize.CRUISER);
		float mult2 = (Float) mag2.get(ShipAPI.HullSize.CRUISER);
		if (stats.getVariant() != null) {
			mult = (Float) mag.get(stats.getVariant().getHullSize());
			mult2 = (Float) mag2.get(stats.getVariant().getHullSize());
		}
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		
		float jitterLevel = effectLevel;
		float jitterRangeBonus = 0;
		float maxRangeBonus = 12f;
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
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);
		
	
		float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
		//float shipReadinessMult = 1f + (READINESS_MULT - 1f) * effectLevel;
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
                stats.getEffectiveArmorBonus().modifyFlat(id, mult2 * effectLevel);
		//stats.getCRLossPerSecondPercent().modifyMult(id, shipReadinessMult);
		
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
//			if (ship.areAnyEnemiesInRange()) {
//				Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
//			} else {
//				Global.getCombatEngine().getTimeMult().modifyMult(id, 2f / shipTimeMult);
//			}
			ShipSystemAPI system = get_xluDamper(ship);
			if (system != null) {
				float percent = (1f - mult) * effectLevel * 100;
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
					system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
					(int) Math.round(percent) + "% less damage taken", false);
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
					system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
					"+" + (int) Math.round(mult2 * effectLevel) + " static Armor", false);
			}
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
		
		ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0,0,0,0), effectLevel, 0.5f);
		ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
	}
	
	public static ShipSystemAPI get_xluDamper(ShipAPI ship) {
		ShipSystemAPI system = ship.getPhaseCloak();
		if (system != null && system.getId().equals("xlu_dilationdamper")) return system;
		return ship.getSystem();
	}
	
        @Override
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
		
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
	}
	
        /*@Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
		if (index == 0) {
			return new StatusData("time flow slowed", false);
		}
		if (index == 1) {
			return new StatusData("damage resistance increased", false);
		}
		return null;
	}*/
}








