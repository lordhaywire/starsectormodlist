package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;


public class xlu_SurfluxShieldStats extends BaseShipSystemScript {

	private final float BASE_ARMOR_SHIELD_FIGHTER = 200f;
	private final float BASE_ARMOR_SHIELD_FRIGATE = 500f;
	private final float BASE_ARMOR_SHIELD_DESTROYER = 650f;
	private final float BASE_ARMOR_SHIELD_CRUISER = 800f;
	private final float BASE_ARMOR_SHIELD_CAPITAL = 1000f;
	private final float ARMOR_SHIELD = 2.0f;
	private final float SHIELD_DMG_RESIST = 0.5f;
	private final float SHIELD_DMG_TAKEN = 75f;
	private final float SHIELD_EMP_RESIST = 0.67f;
	private float TIMER = 10000f;
        
	protected Object STATUSKEY1 = new Object();

    	//private CombatEngineAPI engine;

	private void maintainStatus(ShipAPI playerShip, State state, float effectLevel) {

            Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, "graphics/Extratential Lanestate Union/icons/xlu_surflux_barrier.png", "surflux barrier", "absorbing " + (int) (SHIELD_DMG_RESIST) + "% of hull damage", false);
		//}

	}

        @Override
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
		
		if (player) {
			maintainStatus(ship, state, effectLevel);
		}
		
		if (Global.getCombatEngine().isPaused()) {
			return;
		}

		if (state == State.COOLDOWN || state == State.IDLE) {
			unapply(stats, id);
			return;
		}

		if (state == State.IN || state == State.ACTIVE) {
			//ship.setPhased(true);
		} else if (state == State.OUT) {
			//ship.setPhased(true);
		}

		TIMER = TIMER - 1f;

		stats.getEffectiveArmorBonus().modifyMult(id, ARMOR_SHIELD);
		stats.getHullDamageTakenMult().modifyMult(id, SHIELD_DMG_RESIST);
		stats.getEngineDamageTakenMult().modifyMult(id, SHIELD_DMG_RESIST);
		stats.getWeaponDamageTakenMult().modifyMult(id, SHIELD_DMG_RESIST);
		stats.getEmpDamageTakenMult().modifyMult(id, SHIELD_EMP_RESIST);
                if (ship.isFrigate()) stats.getArmorBonus().modifyMult(id, BASE_ARMOR_SHIELD_FRIGATE);
                else if (ship.isDestroyer()) stats.getArmorBonus().modifyMult(id, BASE_ARMOR_SHIELD_DESTROYER);
                else if (ship.isCruiser()) stats.getArmorBonus().modifyMult(id, BASE_ARMOR_SHIELD_CRUISER);
                else if (ship.isCapital()) stats.getArmorBonus().modifyMult(id, BASE_ARMOR_SHIELD_CAPITAL);
                else stats.getArmorBonus().modifyMult(id, BASE_ARMOR_SHIELD_FIGHTER);

	}
	
        @Override
	public void unapply(MutableShipStatsAPI stats, String id) {

		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
                
		stats.getEffectiveArmorBonus().unmodify(id);
		stats.getArmorBonus().unmodify(id);
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getEngineDamageTakenMult().unmodify(id);
		stats.getWeaponDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
	}
	
	@Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) return new StatusData("increased " + (int) (ARMOR_SHIELD) + "% armor build-up", false);
		if (index == 1) return new StatusData("absorbing " + (int) (SHIELD_DMG_RESIST) + "% of hull damage", false);
		return null;
	}
}
