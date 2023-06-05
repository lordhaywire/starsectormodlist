package data.shipsystems.scripts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

public class xlu_UllanaghDriveStats extends BaseShipSystemScript {
	public static final Object KEY_JITTER = new Object();
	
	public static final float DAMAGE_INCREASE_PERCENT = 10f;
	
	public static final float MAX_TIME_MULT = 1.5f;
	public static final float MIN_TIME_MULT = 0.1f;
	
	public static final Color JITTER_UNDER_COLOR = new Color(35,25,200,150);
	public static final Color JITTER_COLOR = new Color(35,25,200,50);

	
        @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;
                
		float jitterLevel = effectLevel;
		float maxRangeBonus = 5f;
		float jitterRangeBonus = jitterLevel * maxRangeBonus;
		float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
                
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		
		
		effectLevel *= effectLevel;
		if (effectLevel == 1) {
			for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
					bay.setFastReplacements(bay.getFastReplacements());
				}
		}
                
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
		
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 3f + jitterRangeBonus);
		
		ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0,0,0,0), effectLevel, 0.5f);
		ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
                
		if (effectLevel > 0) {
			for (ShipAPI fighter : getFighters(ship)) {
				if (fighter.isHulk()) continue;
				MutableShipStatsAPI fStats = fighter.getMutableStats();
				
				fStats.getBallisticWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getEnergyWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getBeamWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getMissileWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
                                fStats.getTimeMult().modifyMult(id, shipTimeMult);
                                
				if (jitterLevel > 0) {
					//fighter.setWeaponGlow(effectLevel, new Color(255,50,0,125), EnumSet.allOf(WeaponType.class));
					fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 155), EnumSet.allOf(WeaponType.class));
					
					fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, jitterRangeBonus);
					fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
					Global.getSoundPlayer().playLoop("xlu_system_ullanaghdrive_loop", ship, 1f, 0.3f, fighter.getLocation(), fighter.getVelocity());
				}
			}
		}
	}
	
	private List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<ShipAPI>();
		
		for (ShipAPI ship : Global.getCombatEngine().getShips()) {
			if (!ship.isFighter()) continue;
			if (ship.getWing() == null) continue;
			if (ship.getWing().getSourceShip() == carrier) {
				result.add(ship);
			}
		}
		
		return result;
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
		for (ShipAPI fighter : getFighters(ship)) {
			if (fighter.isHulk()) continue;
			MutableShipStatsAPI fStats = fighter.getMutableStats();
                        Global.getCombatEngine().getTimeMult().unmodify(id);
			fStats.getBallisticWeaponDamageMult().unmodify(id);
			fStats.getEnergyWeaponDamageMult().unmodify(id);
			fStats.getMissileWeaponDamageMult().unmodify(id);
                        
                        fStats.getTimeMult().unmodify(id);
		}
                
		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);
	}
	
	
        @Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float percent = DAMAGE_INCREASE_PERCENT * effectLevel;
		float percent2 = ((MAX_TIME_MULT - 1) * 100) * effectLevel;
		if (index == 0) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + percent * 0.01f) + "x fighter damage", false);
		}
                else if (index == 2) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + percent2 * 0.01f) + "x angry", false);
		}
		return null;
	}

	
}








