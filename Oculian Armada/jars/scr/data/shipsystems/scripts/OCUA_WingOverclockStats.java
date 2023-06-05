package data.shipsystems.scripts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

public class OCUA_WingOverclockStats extends BaseShipSystemScript {
	public static final Object KEY_JITTER = new Object();
	
	public static final float DAMAGE_INCREASE_PERCENT = 10f;
	public static final float SPEED_INCREASE_PERCENT = 25f;
	
	public static final float MAX_TIME_MULT = 1.25f;
	public static final float MIN_TIME_MULT = 0.1f;
	
	public static final Color JITTER_UNDER_COLOR = new Color(255,155,155,200);
	public static final Color JITTER_COLOR = new Color(255,155,155,150);

	
        @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
		
		if (effectLevel > 0) {
			float jitterLevel = effectLevel;
			float maxRangeBonus = 5f;
			float jitterRangeBonus = jitterLevel * maxRangeBonus;
			for (ShipAPI fighter : getFighters(ship)) {
				if (fighter.isHulk()) continue;

				MutableShipStatsAPI fStats = fighter.getMutableStats();
				
				fStats.getBallisticWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getEnergyWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getBeamWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getMissileWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 255), EnumSet.allOf(WeaponType.class));
				
                                if (!fighter.getVariant().getHullSpec().getHullId().startsWith("ocua_")) continue;
                                
                                fStats.getMaxSpeed().modifyPercent(id, SPEED_INCREASE_PERCENT);
                                fStats.getMaxSpeed().modifyFlat(id, SPEED_INCREASE_PERCENT / 2);
                                
                                fStats.getAcceleration().modifyPercent(id, SPEED_INCREASE_PERCENT * 2 * effectLevel);
                                fStats.getDeceleration().modifyPercent(id, SPEED_INCREASE_PERCENT * 2 * effectLevel);
                                
                                fStats.getTurnAcceleration().modifyFlat(id, SPEED_INCREASE_PERCENT * effectLevel);
                                fStats.getTurnAcceleration().modifyPercent(id, SPEED_INCREASE_PERCENT * effectLevel);
                                fStats.getMaxTurnRate().modifyFlat(id, SPEED_INCREASE_PERCENT / 2);
                                fStats.getMaxTurnRate().modifyPercent(id, SPEED_INCREASE_PERCENT / 2);
                                fStats.getTimeMult().modifyMult(id, shipTimeMult);
                        
				if (jitterLevel > 0) {
					//fighter.setWeaponGlow(effectLevel, new Color(255,50,0,125), EnumSet.allOf(WeaponType.class));
					
					fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, jitterRangeBonus);
					fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
					Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
				}
			}
		}
	}
	
	private List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<ShipAPI>();
		
//		this didn't catch fighters returning for refit		
//		for (FighterLaunchBayAPI bay : carrier.getLaunchBaysCopy()) {
//			if (bay.getWing() == null) continue;
//			result.addAll(bay.getWing().getWingMembers());
//		}
		
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
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		for (ShipAPI fighter : getFighters(ship)) {
			if (fighter.isHulk()) continue;
			MutableShipStatsAPI fStats = fighter.getMutableStats();
			fStats.getBallisticWeaponDamageMult().unmodify(id);
			fStats.getEnergyWeaponDamageMult().unmodify(id);
			fStats.getMissileWeaponDamageMult().unmodify(id);
                        
                        fStats.getMaxSpeed().unmodify(id);
                        fStats.getMaxTurnRate().unmodify(id);
                        fStats.getTurnAcceleration().unmodify(id);
                        fStats.getAcceleration().unmodify(id);
                        fStats.getDeceleration().unmodify(id);
                        fStats.getTimeMult().unmodify(id);
		}
	}
	
	
        @Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float percent = DAMAGE_INCREASE_PERCENT * effectLevel;
		float percent2 = SPEED_INCREASE_PERCENT * effectLevel;
		if (index == 0) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + percent * 0.01f) + "x fighter damage", false);
		}
                else if (index == 1) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + percent2 * 0.02f) + "x fighter movement speed", false);
		}
		return null;
	}

	
}








