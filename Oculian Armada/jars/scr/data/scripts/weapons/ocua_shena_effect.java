package data.scripts.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class ocua_shena_effect extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin, OnHitEffectPlugin {

	public ocua_shena_effect() {
	}
	
        @Override
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, 
                boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		Color color = projectile.getProjectileSpec().getFringeColor();
		color = Misc.setAlpha(color, 100);
		
		Vector2f vel = new Vector2f();
		if (target instanceof ShipAPI) {
			vel.set(target.getVelocity());
		}
		
		float sizeMult = Misc.getHitGlowSize(100f, projectile.getDamage().getBaseDamage(), damageResult) / 100f;
		
		for (int i = 0; i < 4; i++) {
			//float size = projectile.getProjectileSpec().getWidth() * (0.75f + (float) Math.random() * 0.5f);
			float size = 10f * (0.75f + (float) Math.random() * 0.5f);
			
			float dur = 1f;
			//dur = 0.25f;
			float rampUp = 0f;
			Color c = Misc.scaleAlpha(color, projectile.getBrightness());
			engine.addNebulaParticle(point, vel, size, 3f + 2f * sizeMult,
											rampUp, 0f, dur, c, true);
		}
                
		if (!shieldHit && target instanceof ShipAPI) {
                    float emp = projectile.getEmpAmount();
                    float dam = projectile.getDamageAmount() / 5f;
                    for (int i = 0; i < 3; i++) {
                        if ((float) Math.random() > 0.75f) {
                            engine.spawnEmpArc(projectile.getSource(), point, target, target,
							   DamageType.ENERGY, 
							   dam,
							   emp, // emp 
							   100000f, // max range 
							   "tachyon_lance_emp_impact",
							   20f, // thickness
							   new Color(255,120,200,255),
							   new Color(255,255,255,255)
							   );
                            i = 3;
                        }
                    }
		}
	}
	
        @Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ocua_shena_effect trail = new ocua_shena_effect(projectile);
		CombatEntityAPI e = engine.addLayeredRenderingPlugin(trail);
		e.getLocation().set(projectile.getLocation());
	}
	
	
	public static class ParticleData {
		public SpriteAPI sprite;
		public Vector2f offset = new Vector2f();
		public Vector2f vel = new Vector2f();
		public float scale = 1f;
		public DamagingProjectileAPI proj;
		public float scaleIncreaseRate = 1f;
		public float turnDir = 1f;
		public float angle = 1f;
		
		public float maxDur;
		public Vector2f origVel;
		public FaderUtil fader;
		public Vector2f dirVelChange;
		
		public ParticleData(DamagingProjectileAPI proj) {
			this.proj = proj;
			sprite = Global.getSettings().getSprite("misc", "nebula_particles");
			float i = Misc.random.nextInt(4);
			float j = Misc.random.nextInt(4);
			sprite.setTexWidth(0.25f);
			sprite.setTexHeight(0.25f);
			sprite.setTexX(i * 0.25f);
			sprite.setTexY(j * 0.25f);
			sprite.setAdditiveBlend();
			
			angle = (float) Math.random() * 360f;
			
			maxDur = proj.getWeapon().getRange() / proj.getWeapon().getProjectileSpeed();
			scaleIncreaseRate = 2.5f / maxDur;
			scale = 1f;
			
			turnDir = Math.signum((float) Math.random() - 0.5f) * 30f * (float) Math.random();
			//turnDir = 0f;
			
			float driftDir = proj.getFacing() + 180f + ((float) Math.random() * 30f - 15f);
			vel = Misc.getUnitVectorAtDegreeAngle(driftDir);
			vel.scale(80f / maxDur * (0f + (float) Math.random() * 3f));
			
			origVel = new Vector2f(vel);
			dirVelChange = Misc.getUnitVectorAtDegreeAngle(proj.getFacing() + 180f);
			
			fader = new FaderUtil(0f, 0.25f, 0.05f);
			fader.fadeIn();
		}
		
		public void advance(float amount) {
			scale += scaleIncreaseRate * amount;
			
			offset.x += vel.x * amount;
			offset.y += vel.y * amount;
				
			if (!proj.didDamage()) {
				float speed = vel.length();
				if (speed > 0) {
					float speedIncrease = proj.getMoveSpeed() / maxDur * 0.5f;
					Vector2f dir = new Vector2f(dirVelChange);
					dir.scale(speedIncrease * amount);
					Vector2f.add(vel, dir, vel);
				}
			}
			
			angle += turnDir * amount;
			
			fader.advance(amount);
		}
	}
	
	protected List<ParticleData> particles = new ArrayList<ParticleData>();
	
	protected DamagingProjectileAPI proj;
	protected Vector2f projVel;
	protected Vector2f projLoc;
	public ocua_shena_effect(DamagingProjectileAPI proj) {
		this.proj = proj;
		
		projVel = new Vector2f(proj.getVelocity());
		projLoc = new Vector2f(proj.getLocation());
		
		int num = 10;
		for (int i = 0; i < num; i++) {
			particles.add(new ParticleData(proj));
		}
		
		float index = 0;
		for (ParticleData p : particles) {
			p.offset = Misc.getPointWithinRadius(p.offset, 20f);
			index++;
		}
	}
	
        @Override
	public float getRenderRadius() {
		return 700f;
	}
	
	protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return layers;
	}

        @Override
	public void init(CombatEntityAPI entity) {
		super.init(entity);
	}
	
	protected boolean resetTrailSpeed = false;
        @Override
	public void advance(float amount) {
		if (Global.getCombatEngine().isPaused()) return;
		
		entity.getLocation().set(proj.getLocation());
		
		float max = 0f;
		for (ParticleData p : particles) {
			p.advance(amount);
			max = Math.max(max, p.offset.lengthSquared());
		}
		
		if (proj.getElapsed() < 0.1f) {
			projVel.set(proj.getVelocity());
			projLoc.set(proj.getLocation());
		} else {
			projLoc.x += projVel.x * amount;
			projLoc.y += projVel.y * amount;
			
			if (proj.didDamage()) {
				if (!resetTrailSpeed) {
					for (ParticleData p : particles) {
						Vector2f.add(p.vel, projVel, p.vel);
					}
					projVel.scale(0f);
					resetTrailSpeed = true;
				}
				for (ParticleData p : particles) {
					float dist = p.offset.length();
					p.vel.scale(Math.min(1f, dist / 100f));
				}
			}
		}
	}


        @Override
	public boolean isExpired() {
		return proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj);
	}

        @Override
	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		float x = projLoc.x;
		float y = projLoc.y;
		
		Color color = proj.getProjectileSpec().getFringeColor();
		color = Misc.setAlpha(color, 30);
		float b = proj.getBrightness();
		b *= viewport.getAlphaMult();
		
		for (ParticleData p : particles) {
			float size = 15f;
			size *= p.scale;
			
			Vector2f loc = new Vector2f(x + p.offset.x, y + p.offset.y);
			
			float alphaMult = 1f;
			
			float a = alphaMult;
			
			p.sprite.setAngle(p.angle);
			p.sprite.setSize(size, size);
			p.sprite.setAlphaMult(b * a * p.fader.getBrightness());
			p.sprite.setColor(color);
			p.sprite.renderAtCenter(loc.x, loc.y);
		}
	}

}




