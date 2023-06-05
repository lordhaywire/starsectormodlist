package data.shipsystems.scripts.ai;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;


/**
 * Sample ship system AI. 
 * 
 * THIS CODE IS NOT ACTUALLY USED, AND IS PROVIDED AS AN EXAMPLE ONLY.
 * 
 * To enable it, uncomment the relevant lines in fastmissileracks.system.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public class xlu_CuratorOverloaderAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipwideAIFlags flags;
	private ShipSystemAPI system;
	
	private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);
	
        @Override
	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
	}
	
	private float sinceLast = 0f;
	
	@SuppressWarnings("unchecked")
        @Override
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		tracker.advance(amount);
		
		sinceLast += amount;
		
		if (tracker.intervalElapsed()) {
			float fluxLevel = ship.getFluxTracker().getFluxLevel();
			
			boolean MinorEnemyIsClose = target != null && ((target.isFrigate() || target.isDestroyer()) 
                                && target.getMaxSpeedWithoutBoost() > 100f) && target.areAnyEnemiesInRange();
			boolean targetIsDetected = target != null && target.isTargetable();
			boolean targetIsSlow = target != null && (target.getMaxSpeedWithoutBoost() <= 41f || (target.isCruiser() && target.getMaxSpeedWithoutBoost() <= 51f));
			boolean targetIsVulnerable = target != null && target.getFluxTracker().isOverloadedOrVenting() && 
			 							(target.getFluxTracker().getOverloadTimeRemaining() > 5f || 
			 							target.getFluxTracker().getTimeToVent() > 5f);
			
			if (!system.isActive() && (fluxLevel < 0.4f && sinceLast > 5f) && (!MinorEnemyIsClose && sinceLast > 10f)) {
                            if ((targetIsDetected && (targetIsSlow || targetIsVulnerable))) {
                                ship.useSystem();
                            }
                        }
                        
			if (system.isActive() && (MinorEnemyIsClose && sinceLast > 5f)) {
                                ship.useSystem();
                                sinceLast = 0f;
                        }
                        
			if (((target == null || !target.isAlive()
                                || (target.isAlive() && fluxLevel > 0.65f)))
                                && system.isActive()) {
                            ship.useSystem();
                            return;
                        }
		}
	}

}
