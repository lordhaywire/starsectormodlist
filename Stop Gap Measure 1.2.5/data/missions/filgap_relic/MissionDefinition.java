package data.missions.filgap_relic;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "CGR", FleetGoal.ATTACK, false, 10);
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Knights of Ludd reclamation force");
		api.setFleetTagline(FleetSide.ENEMY, "Hegemony security fleet");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		api.addBriefingItem("CGR Sword of Truth must survive");

		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "filgap_cathar_Assault", FleetMemberType.SHIP, "CGR Sword of Truth", true);
		api.addToFleet(FleetSide.PLAYER, "filgap_era_lc_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "filgap_bogomil_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "filgap_templar_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "filgap_fulk_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "filgap_fulk_Standard", FleetMemberType.SHIP, false);

		
	
		// Mark player flagship as essential
		api.defeatOnShipLoss("CGR Sword of Truth");



		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "filgap_adjudicator_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Escort", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "filgap_copernic_Hegemony", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "filgap_huntsman_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "filgap_huntsman_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
	

		
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		for (int i = 0; i < 15; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 900f; 
			api.addNebula(x, y, radius);
		}
		
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.4f, 2000);
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.5f, 2000);
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.6f, 2000);
		
		api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.4f, "nav_buoy");
		api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.6f, "nav_buoy");
		api.addObjective(minX + width * 0.5f + 1000, minY + height * 0.7f, "comm_relay");
		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");

		
		// Add an asteroid field
		api.addAsteroidField(minX + width * 0.3f, minY, 90, 3000f,
								20f, 70f, 50);
		
		// Add some planets.  These are defined in data/config/planets.json.
		api.addPlanet(0, 0, 200f, "frozen", 350f, true);
	}

}






