package data.weapons.proj;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class MaidenEmpArc implements OnHitEffectPlugin {


	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
/// HIT ON SHIELD OR SHIP		
			if ((float) Math.random() > 0.00f && target instanceof ShipAPI) {
/// Base damage = from .csv
				float emp = projectile.getEmpAmount();
				float dam = projectile.getDamageAmount();

				engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
								   DamageType.ENERGY, 
//								   ((dam) * 0.00f), /// damage dealt
//								   ((emp) * 0.50f), // emp damage
								   (75f), /// damage dealt
								   (750f), // emp damage
								   100000f, // max range 
								   "tachyon_lance_emp_impact",
								   20f, // thickness
								   new Color(25,100,155,255),
								   new Color(255,255,255,255)
								   );
			



		}
	}
}
