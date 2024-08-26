package data.missions.randisconflict;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "MBG", FleetGoal.ATTACK, false, 2);
		api.initFleet(FleetSide.ENEMY, "KoC", FleetGoal.ATTACK, true, 3);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Phase Fleet");
		api.setFleetTagline(FleetSide.ENEMY, "KoC Convoy");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Eliminate the Convoy");
		api.addBriefingItem("Use your mines to eliminate the drones before they overwhelm you");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "google_geist_Standard", FleetMemberType.SHIP, "MBG Kalidor", true);
		// Mark flagship as essential
		api.defeatOnShipLoss("MBG Kalidor");

		api.addToFleet(FleetSide.PLAYER, "google_phantom_Standard", FleetMemberType.SHIP, "Poledouris", false);
		api.addToFleet(FleetSide.PLAYER, "google_harbinger_Standard", FleetMemberType.SHIP, "Bullshit", false);
		api.addToFleet(FleetSide.PLAYER, "google_harbinger_Standard", FleetMemberType.SHIP, "Cowshit", false);
		api.addToFleet(FleetSide.PLAYER, "google_harbinger_Standard", FleetMemberType.SHIP, "Dogshit", false);
		api.addToFleet(FleetSide.PLAYER, "google_harbinger_Standard", FleetMemberType.SHIP, "Bitchshit", false);
		api.addToFleet(FleetSide.PLAYER, "google_afflictor_Standard", FleetMemberType.SHIP, "Mog", false);
		api.addToFleet(FleetSide.PLAYER, "google_afflictor_Standard", FleetMemberType.SHIP, "Goblin", false);

		
		// Set up the enemy fleet
		
		FleetMemberAPI fleetMember;
		fleetMember = api.addToFleet(FleetSide.ENEMY, "atlas_c_standard", FleetMemberType.SHIP, "KoC", true);
		fleetMember.getCaptain().setPersonality("aggressive");
		
		api.addToFleet(FleetSide.ENEMY, "atlas_c_standard", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.ENEMY, "buffalo_c_standard", FleetMemberType.SHIP, true);
//		api.addToFleet(FleetSide.ENEMY, "buffalo_c_standard", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.ENEMY, "propsector_standard", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.ENEMY, "propsector_standard", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.ENEMY, "shuttlekoc_Standard", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.ENEMY, "strix_Standard", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.ENEMY, "strix_Standard", FleetMemberType.SHIP, true);	
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add two big nebula clouds
		api.addNebula(minX + width * 0.66f, minY + height * 0.5f, 2000);
		api.addNebula(minX + width * 0.25f, minY + height * 0.6f, 1000);
		api.addNebula(minX + width * 0.25f, minY + height * 0.4f, 1000);
		
		// And a few random ones to spice up the playing field.
		for (int i = 0; i < 5; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 400f; 
			api.addNebula(x, y, radius);
		}
		
		// add objectives
		api.addObjective(minX + width * 0.25f + 2000f, minY + height * 0.5f, 
						 "sensor_array");
		api.addObjective(minX + width * 0.75f - 2000f, minY + height * 0.5f,
						 "comm_relay");
		api.addObjective(minX + width * 0.33f + 2000f, minY + height * 0.4f, 
						 "nav_buoy");
		api.addObjective(minX + width * 0.66f - 2000f, minY + height * 0.6f, 
						 "nav_buoy");
		

		api.addAsteroidField(-(minY + height), minY + height, -45, 2000f,
								20f, 70f, 100);
		
		api.addPlanet(0, 0, 400f, "barren", 200f, true);
		api.addRingAsteroids(0,0, 30, 32, 32, 48, 200);
	}

}






