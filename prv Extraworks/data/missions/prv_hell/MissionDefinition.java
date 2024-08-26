package data.missions.prv_hell;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;
import java.util.Random;

public class MissionDefinition implements MissionDefinitionPlugin {

	private String[] idList = {
		"prv_tystnad_ryak",
		"prv_gnista_cs",
		"prv_sunder_ryak_core",
		"prv_ljus_strike",
		"prv_fasnod_strike",
		"prv_sorl_attack"
	};

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "prv", FleetGoal.ESCAPE, false);
		api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 3);
		api.getDefaultCommander(FleetSide.ENEMY).getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 3);
		
		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Nedan-HEL volatiles convoy");
		api.setFleetTagline(FleetSide.ENEMY, "Head4 Syndicate materiél liberators");

		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Preserve the cargo ships. prv Blue Star must not be lost.");
		api.addBriefingItem("The first reinforcements will arrive in just under two minutes.");
		api.addBriefingItem("Escape the ambush before the slow Ruster cruisers catch you.");
		api.addBriefingItem("'Blue Star' is not a fast ship - keep it moving!");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
		api.addToFleet(FleetSide.PLAYER, "prv_middag_mission", FleetMemberType.SHIP, "prv Blue Star", false);
		api.addToFleet(FleetSide.PLAYER, "prv_eld_missile", FleetMemberType.SHIP, "prv Att bita igenom", true);
		api.addToFleet(FleetSide.PLAYER, "prv_flamma_beam", FleetMemberType.SHIP, "prv Upon an Oaken Throne", false);
		api.addToFleet(FleetSide.PLAYER, "prv_gnista_missile", FleetMemberType.SHIP, "prv Integration under the moon", false);
		api.addToFleet(FleetSide.PLAYER, "prv_ljus_strike", FleetMemberType.SHIP, "prv Att arbeta sig uppåt", false);
		api.addToFleet(FleetSide.PLAYER, "prv_tiger_aux_c", FleetMemberType.SHIP, "prv the gods are itchy", false);
		api.addToFleet(FleetSide.PLAYER, "prv_gemini_aux_b", FleetMemberType.SHIP, "prv world without words", false);
		api.addToFleet(FleetSide.PLAYER, "prv_munsbit_armed", FleetMemberType.SHIP, "prv Virtual Pilgrim", false);

		api.defeatOnShipLoss("prv Blue Star");

		// Set up the enemy fleet.
		api.addToFleet(FleetSide.ENEMY, "prv_bergslag_p_suspicious",FleetMemberType.SHIP,"DO WHAT I WANT", true);
		api.addToFleet(FleetSide.ENEMY, "prv_dominator_rb_syndicate",FleetMemberType.SHIP,"Duty and Vocation", false);
		api.addToFleet(FleetSide.ENEMY, "prv_hornfels_belt", FleetMemberType.SHIP, "friendly strangers", false);
		api.addToFleet(FleetSide.ENEMY, "prv_gabbro_mil", FleetMemberType.SHIP, "Stray", false);
		api.addToFleet(FleetSide.ENEMY, "prv_gremlin_rb_attack", FleetMemberType.SHIP, "Different Road", false);
		api.addToFleet(FleetSide.ENEMY, "prv_tystnad_p_ambush", FleetMemberType.SHIP, "Belt Brave", false);
		api.addToFleet(FleetSide.ENEMY, "prv_gnejs_support", FleetMemberType.SHIP, "1st samurai", false);
		api.addToFleet(FleetSide.ENEMY, "prv_skara_belt", FleetMemberType.SHIP, "nightshift", false);
		api.addToFleet(FleetSide.ENEMY, "prv_basalt_attack", FleetMemberType.SHIP, "what we can gain", false);
		api.addToFleet(FleetSide.ENEMY, "prv_visent_smuggler", FleetMemberType.SHIP, "Unto a better wharf", false);
		api.addToFleet(FleetSide.ENEMY, "prv_skiffer_belt", FleetMemberType.SHIP, "Damn Damn Drum", false);
		api.addToFleet(FleetSide.ENEMY, "prv_skiffer_belt", FleetMemberType.SHIP, "Wellcheers", false);
		
		// Set up the map.
		float width = 9000f;
		float height = 18000f;
		api.initMap(-width/2f, width/2f, -height/2f, height/2f);
		
		float minX = -width/2;
		float minY = -height/2;

		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			Vector2f loc = new Vector2f(random.nextFloat()*width,random.nextFloat()*height);
			api.addNebula(loc.x,loc.y,50f+random.nextFloat()*250f);
		}

		// vvv DO NOT FUCK WITH
		// NO               vvv
		api.addPlanet(0f,0f,2400f,"prv_ryak",250f,true);
		api.setPlanetBgSize(900f,900f);

		api.addPlugin(new missionplugin());

	}

	private class missionplugin implements EveryFrameCombatPlugin {

		private float delay = 90f;
		private int wave = 0;
		private CombatEngineAPI engine;

		private final Random random = new Random();

		@Override
		public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {

		}

		@Override
		public void advance(float amount, List<InputEventAPI> events) {
			if (engine.isPaused()) return;
			if (engine.getFleetManager(0).getTaskManager(false).isInFullRetreat()) return;
			delay -= amount;

			if (delay <= 0f) {
				switch (wave) {
					case 0:
						spawnNextReinforcement(0);
						spawnRusterReinforcement();
						wave = 1;
						addReinforcementBlurb(wave, true);
//						delay = 45f + random.nextFloat()*30f; // 1 min
						delay = 30f;
						break;
					case 1:
						spawnNextReinforcement(1);
						spawnNextReinforcement(2);
						wave = 3;
						addReinforcementBlurb(wave,false);
//						delay = 100f + random.nextFloat()*40f; // 2 min
						delay = 90f;
						break;
					case 3:
						spawnNextReinforcement(3);
						wave = 4;
						addReinforcementBlurb(wave, false);
//						delay = 100f + random.nextFloat()*40f; // 2 min
						delay = 90f;
						break;
					case 4:
						spawnNextReinforcement(4);
						spawnNextReinforcement(5);
						wave = 6;
						addReinforcementBlurb(wave, false);
						engine.removePlugin(this); // we're done here
						break;
				}
			}
		}

		private void spawnRusterReinforcement() {
			CombatFleetManagerAPI fleetman = engine.getFleetManager(1);
			Vector2f reinfpoint1 = new Vector2f(engine.getMapWidth() * 0.5f, -engine.getMapHeight()* 0.125f);
			Vector2f reinfpoint2 = new Vector2f(-engine.getMapWidth() * 0.5f, engine.getMapHeight() * 0.125f);
			fleetman.spawnShipOrWing("prv_kvarts_attack",reinfpoint1,180f,3f);
			fleetman.spawnShipOrWing("prv_gissel_siege",reinfpoint2,0f,3f);
			//fleetman.spawnShipOrWing("prv_hornfels_belt",reinfpoint3,90f,9f);
		}

		private void spawnNextReinforcement(int wave) {
			if (wave >= idList.length) return;

			CombatFleetManagerAPI fleetman = engine.getFleetManager(0);
			Vector2f reinfpoint = new Vector2f(0f, engine.getMapHeight()* 0.5f);
			fleetman.spawnShipOrWing(idList[wave],reinfpoint,270f,6f);

		}

		private void addReinforcementBlurb(int wave, boolean withEnemy) {
			Global.getSoundPlayer().playUISound("cr_playership_warning",2f,0.5f);
			Vector2f textLoc;
			if (engine.getPlayerShip() != null) {
				textLoc = new Vector2f(engine.getPlayerShip().getLocation());
				textLoc.y += 160f;
			} else {
				textLoc = engine.getViewport().getCenter();
			}
			if (withEnemy) {
				engine.addFloatingText(textLoc,
					"We've got reinforcements incoming, but so do they!",
					32f, Misc.getNegativeHighlightColor(), engine.getPlayerShip(),
					0f, 0f);
			} else if (wave >= idList.length) {
				engine.addFloatingText(textLoc,
					"That's the last help we can expect. Let's finish this.",
					32f, Misc.getNegativeHighlightColor(), engine.getPlayerShip(),
					0f, 0f);
			} else {
				engine.addFloatingText(textLoc,
					"More reinforcements have arrived!",
					32f, Misc.getHighlightColor(), engine.getPlayerShip(),
					0f, 0f);
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
			this.engine = engine;
		}
	}
}
