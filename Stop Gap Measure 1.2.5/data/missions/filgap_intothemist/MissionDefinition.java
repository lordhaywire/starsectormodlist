package data.missions.filgap_intothemist;

import java.util.List;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true, 5);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Pirate attack fleet");
		api.setFleetTagline(FleetSide.ENEMY, "Salvage fleet");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat the enemy forces");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "filgap_bahamut_pirate", FleetMemberType.SHIP, "Hammer", true);
		api.addToFleet(FleetSide.PLAYER, "filgap_wagner_Pirate", FleetMemberType.SHIP, "Bloody Hand", true);
		api.addToFleet(FleetSide.PLAYER, "filgap_adjudicator_Support", FleetMemberType.SHIP, "Rightful Claim", false);
		api.addToFleet(FleetSide.PLAYER, "filgap_opportunity_Assault", FleetMemberType.SHIP, "Rightful Claim", false);
		api.addToFleet(FleetSide.PLAYER, "filgap_wardlaw_attack", FleetMemberType.SHIP, "Surprise", false);
		api.addToFleet(FleetSide.PLAYER, "filgap_ballista_CS", FleetMemberType.SHIP, "Last Chance", false);
		api.addToFleet(FleetSide.PLAYER, "filgap_copernic_Combat", FleetMemberType.SHIP, "Flicking Mind", false);
		api.addToFleet(FleetSide.PLAYER, "filgap_velite_Pirate", FleetMemberType.SHIP, "Second Chance", false);
		api.addToFleet(FleetSide.PLAYER, "filgap_velite_Pirate", FleetMemberType.SHIP, "False Hope", false);
		api.addToFleet(FleetSide.PLAYER, "filgap_pyrphoros_Escort", FleetMemberType.SHIP, "Vulkan", 
false);
		api.addToFleet(FleetSide.PLAYER, "filgap_pyrphoros_Strike", FleetMemberType.SHIP, "Hammer", false);
		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "filgap_kuunlan_fontier", FleetMemberType.SHIP, "ISS Somtaaw", false);
		api.addToFleet(FleetSide.ENEMY, "filgap_auspicious_balanced", FleetMemberType.SHIP, "ISS Zeus", false);
		api.addToFleet(FleetSide.ENEMY, "filgap_era_defense", FleetMemberType.SHIP, "ISS Artemis", false);
		api.addToFleet(FleetSide.ENEMY, "filgap_foundation_Support", FleetMemberType.SHIP, "ISS Skyscrapper", false);
		api.addToFleet(FleetSide.ENEMY, "filgap_nitassinan_standard", FleetMemberType.SHIP, "ISS Heavy Duty", false);
		api.addToFleet(FleetSide.ENEMY, "filgap_huntsman_Standard", FleetMemberType.SHIP, "ISS Duelist", false);
		api.addToFleet(FleetSide.ENEMY, "filgap_huntsman_Standard", FleetMemberType.SHIP, "ISS Duelist", false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_Standard", FleetMemberType.SHIP, "ISS Why Not?", false);
		api.addToFleet(FleetSide.ENEMY, "filgap_dove_Frontier", FleetMemberType.SHIP, "ISS Orb of Light", false);

		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		for (int i = 0; i < 300; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/4;
			
			if (x > -1000 && x < 1500 && y < -1000) continue;
			float radius = 200f + (float) Math.random() * 900f; 
			api.addNebula(x, y, radius);
		}
		
		
		api.addObjective(minX + width * 0.7f - 3000, minY + height * 0.65f, "nav_buoy");
		api.addObjective(minX + width * 0.5f, minY + height * 0.35f + 2000, "nav_buoy");
		api.addObjective(minX + width * 0.2f + 3000, minY + height * 0.6f, "sensor_array");
		
		api.addPlugin(new BaseEveryFrameCombatPlugin() {
			public void init(CombatEngineAPI engine) {
				engine.getContext().setStandoffRange(12000f);
			}
			public void advance(float amount, List events) {
			}
		});
			
	}

}






