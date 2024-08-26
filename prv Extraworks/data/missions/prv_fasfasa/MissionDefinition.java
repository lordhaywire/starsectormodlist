package data.missions.prv_fasfasa;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.Misc;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import prv.lib.util.prvColors;

import java.awt.*;
import java.util.List;

public class MissionDefinition implements MissionDefinitionPlugin {

	private FleetMemberAPI jammer = null;

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true);

		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 3);
		api.getDefaultCommander(FleetSide.ENEMY).getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 3);
		
		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Pariabyrån specops detachment");
		api.setFleetTagline(FleetSide.ENEMY, "Too-clever Tri-Tachyon blockade");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat the Tri-Tachyon interception team.");
		api.addBriefingItem("The AI core carried on the 'irreal' courier ship must not be destroyed.");
		api.addBriefingItem("The TTS Great Quantum has a phase field disruptor - avoid or disable it!");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
		api.addToFleet(FleetSide.PLAYER, "prv_fasvinge_flicker", FleetMemberType.SHIP, "Kan, bör, måste, skall", true);
		api.addToFleet(FleetSide.PLAYER, "prv_ilbud_stealth", FleetMemberType.SHIP, "irreal", false);
		api.addToFleet(FleetSide.PLAYER, "prv_fasklot_flicker", FleetMemberType.SHIP, "nirvaaNa", false);
		api.addToFleet(FleetSide.PLAYER, "prv_fasnod_flicker", FleetMemberType.SHIP, "Witchmakers", false);
		api.addToFleet(FleetSide.PLAYER, "prv_ande_phase", FleetMemberType.SHIP, "sång", false);

		// Set up the enemy fleet.
		jammer = api.addToFleet(FleetSide.ENEMY,"prv_apogee_jammer",FleetMemberType.SHIP,"TTS Great Quantum", true);
		api.addToFleet(FleetSide.ENEMY, "aurora_Balanced", FleetMemberType.SHIP, "TTS Post-Perfect", false);
		api.addToFleet(FleetSide.ENEMY, "medusa_PD", FleetMemberType.SHIP, "TTS Immaculate", false);
		api.addToFleet(FleetSide.ENEMY, "prv_shrike_3g", FleetMemberType.SHIP, "TTS Ionizing Radiation", false);
		api.addToFleet(FleetSide.ENEMY, "prv_gryning_tt_balanced", FleetMemberType.SHIP, "TTS Kintsugi", false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, "TTS Deferred Revenue", false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Overdriven", FleetMemberType.SHIP, "TTS Peak Performance", false);

		api.defeatOnShipLoss("irreal");
		
		// Set up the map.
		float width = 12000f;
		float height = 12000f;
		api.initMap(-width/2f, width/2f, -height/2f, height/2f);
		
		float minX = -width/2;
		float minY = -height/2;

		api.addPlanet(0, 0, 12.5f, StarTypes.WHITE_DWARF, 250f, true);
		api.setPlanetBgSize(200f,100f);

		api.addObjective(1000f,4000f,"sensor_array");

		api.addPlugin(new EveryFrameCombatPlugin() {

			private final float SINE_FREQUENCY = 0.125f; //Hz
			private float sineTimer = 1.451678f; // start offset
			private boolean active = false;
			private boolean setAvoid = false;
			private RippleDistortion ripple = null;
			private CombatEngineAPI engine;

			private float getSine() { // returns 0 ... 1
				return 0.5f * (1+(float)Math.sin(3.14 * 2 * SINE_FREQUENCY * sineTimer));
			}

			@Override
			public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {

			}

			private void addRippleDistorsion(Vector2f location){
				ripple = new RippleDistortion();
				ripple.setIntensity(150f);
				ripple.setLocation(location);
				ripple.setSize(750f);
				ripple.fadeOutIntensity(2f);
				ripple.flip(true);
				DistortionShader.addDistortion(ripple);
			}

			@Override
			public void advance(float amount, List<InputEventAPI> events) {
				if (Global.getCombatEngine().isPaused()) return;
				ShipAPI source = engine.getFleetManager(1).getShipFor(jammer);

				if (source == null || !source.isAlive()) return;
				if (source.getFluxTracker().isOverloadedOrVenting()) {
					sineTimer = 0f;
					return;
				}

				sineTimer += amount;

				if (!setAvoid && CombatUtils.isVisibleToSide(source,0)) {
					setAvoid = true;
					Vector2f textLoc = new Vector2f(engine.getPlayerShip().getLocation());
					textLoc.y += 160f;
					engine.addFloatingText(textLoc,
						"Careful around that Apogee! It'll disrupt our phase systems if we get too close.",
						32f,Misc.getNegativeHighlightColor(),engine.getPlayerShip(),0f,0f);

					// actually makes them WAY too careful
//					DeployedFleetMemberAPI sourceAssignment =
//						engine.getFleetManager(1).getDeployedFleetMember(source);
//					engine.getFleetManager(0).getTaskManager(false)
//						.createAssignment(CombatAssignmentType.AVOID,sourceAssignment,false);
				}

				setEmitterJitter(source);
//				setEmitterJitter(engine.getPlayerShip());

				if (getSine() > 0.95f) {
					if (!active) {
						addRippleDistorsion(source.getLocation());
						active = true;
						Global.getSoundPlayer().playSound(
							"system_interdictor",
							1.25f,
							1.25f,
							source.getLocation(),
							source.getVelocity());

						List<ShipAPI> ships = AIUtils.getNearbyEnemies(source,1000f);
//						ShipAPI candidate = null;
//						float closest = 1000000f;
						for (ShipAPI ship : ships) {
							if (ship.isPhased()) {
								disruptPhaseSystem(source,ship);

//								float distance = MathUtils.getDistanceSquared(
//									ship.getLocation(),
//									source.getLocation());
//								if (distance < closest) {
//									candidate = ship;
//									closest = distance;
//								}
							}
						}

//						if (candidate != null) {
//							disruptPhaseSystem(source, candidate);
//						}
					}
				} else active = false;
			}

			private void setEmitterJitter(ShipAPI source) {
				Color jammerJitterColor = prvColors.blend(
					prvColors.NOVA_PINK,
					prvColors.UV_PURPLE,
					getSine()
				);

//				Color jammerJitterColor = prvColors.getColorNovaPink();

				float sine = getSine();
				source.setJitterShields(false);
				source.setJitterUnder(
					this, jammerJitterColor,
					1f, Math.round(14f * sine),5f, 15f + 10f * sine);
			}

			private void disruptPhaseSystem(ShipAPI source, ShipAPI ship) {
				float baseDuration = 5f;
				ship.getFluxTracker().beginOverloadWithTotalBaseDuration(baseDuration);
				ship.getFluxTracker().showOverloadFloatyIfNeeded("Phase rejection!", new Color(255,155,255,255), 4f, true); // vanilla's acausal color

				for (int i = 0; i < 6; i++) {
					Vector2f point = MathUtils.getRandomPointOnCircumference(
						ship.getLocation(),
						ship.getCollisionRadius() * 2f);
					spawnJammerArc(source, point, ship);
				}

				float distratio = baseDuration * (MathUtils.getDistanceSquared(source.getLocation(),ship.getLocation()) / 1E6f);
				spawnJammerArc(source, source.getLocation(), ship);
				engine.addPlugin(new jitterShip(ship,distratio,baseDuration));
			}

			private void spawnJammerArc(ShipAPI source, Vector2f point, ShipAPI ship) {
				engine.spawnEmpArc(
					source,
					point,
					null,
					ship,
					DamageType.ENERGY,
					50f,
					50f,
					1200f,
					"tachyon_lance_emp_impact",
					24f,
					prvColors.UV_PURPLE,
					Color.WHITE);
			}

			@Override
			public void renderInWorldCoords(ViewportAPI viewport) {

			}

			@Override
			public void renderInUICoords(ViewportAPI viewport) {

			}

			@Override
			public void init(CombatEngineAPI engine) {
				this.engine = Global.getCombatEngine();
			}
		});
		
	}

	private class jitterShip implements EveryFrameCombatPlugin {

		private final ShipAPI ship;
		private final ShipAPI source;
		private float delay;
		private float duration;

		jitterShip(ShipAPI ship, float delay, float duration) {
			this.delay = delay;
			this.duration = duration;
			this.ship = ship;
			this.source = Global.getCombatEngine().getFleetManager(1).getShipFor(jammer);
		}

		@Override
		public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {

		}

		@Override
		public void advance(float amount, List<InputEventAPI> events) {
			ship.setJitter(source, prvColors.UV_PURPLE,1f,1,10f-duration*2f);
			ship.setJitterUnder(source, prvColors.UV_PURPLE,1f,1,10f-duration*2f);

			delay -= amount;
			if (delay <= 0f) {
				duration -= amount;
				if (duration <= 0f) {
					Global.getCombatEngine().removePlugin(this);
				}
			}
		}

		@Override
		public void renderInWorldCoords(ViewportAPI viewport) {

		}

		@Override
		public void renderInUICoords(ViewportAPI viewport) {

		}

		@Override
		public void init(CombatEngineAPI engine) {

		}
	}

}
