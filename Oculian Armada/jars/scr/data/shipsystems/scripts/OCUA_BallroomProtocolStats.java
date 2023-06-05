package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class OCUA_BallroomProtocolStats extends BaseShipSystemScript {
	
	public static float EXTRA_FIGHTER_DURATION = 20;
	public static final Object KEY_JITTER = new Object();
	
	public static final float DAMAGE_INCREASE_PERCENT = 50;
	
	public static final Color JITTER_UNDER_COLOR = new Color(255,175,175,175);
	public static final Color JITTER_COLOR = new Color(255,175,175,125);
	
        @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		if (effectLevel > 0) {
			float jitterLevel = effectLevel;
			float maxRangeBonus = 5f;
			float jitterRangeBonus = jitterLevel * maxRangeBonus;
			for (ShipAPI fighter : getFighters(ship)) {
				if (fighter.isHulk()) continue;
				MutableShipStatsAPI fStats = fighter.getMutableStats();
//				fStats.getBallisticWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
//				fStats.getEnergyWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
//				fStats.getMissileWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
				
				fStats.getBallisticWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getEnergyWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getMissileWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				
				if (jitterLevel > 0) {
					//fighter.setWeaponGlow(effectLevel, new Color(255,50,0,125), EnumSet.allOf(WeaponType.class));
					fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 255), EnumSet.allOf(WeaponType.class));
					
					fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, jitterRangeBonus);
					fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
					Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
				}
			}
		}
                
		if (effectLevel == 1) {
			for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
				if (bay.getWing() == null) continue;
				
				bay.makeCurrentIntervalFast();
				FighterWingSpecAPI spec = bay.getWing().getSpec();
				
				int addForWing = getAdditionalFor(spec);
				int maxTotal = spec.getNumFighters() + addForWing;
				int actualAdd = maxTotal - bay.getWing().getWingMembers().size();
				actualAdd = Math.min(spec.getNumFighters(), actualAdd);
				if (actualAdd > 0) {
					bay.setFastReplacements(bay.getFastReplacements() + addForWing);
					bay.setExtraDeployments(actualAdd);
					bay.setExtraDeploymentLimit(maxTotal);
					bay.setExtraDuration(EXTRA_FIGHTER_DURATION);
				}
			}
		}
	}
	
	private List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<>();
		
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
	
	public static int getAdditionalFor(FighterWingSpecAPI spec) {
		int size = spec.getNumFighters();
		if (size <= 2) return 1;
		if (size <= 4) return 2;
		return 3;
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
		}
	}
	
	

	
        @Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float percent = DAMAGE_INCREASE_PERCENT * effectLevel;
		if (index == 0) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + DAMAGE_INCREASE_PERCENT * effectLevel * 0.01f) + "x fighter damage", false);
		}
		return null;
	}


	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return true;
	}
	

	
}








