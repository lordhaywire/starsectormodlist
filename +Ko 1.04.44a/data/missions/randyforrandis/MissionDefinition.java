package data.missions.randyforrandis;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "KoC", FleetGoal.ATTACK, false, 2);
		api.initFleet(FleetSide.ENEMY, "MBG", FleetGoal.ATTACK, true, 3);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Big strong KoC");
		api.setFleetTagline(FleetSide.ENEMY, "Frilly Phase Scum");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Crush the enemy");
		api.addBriefingItem("See them driven before you");
		api.addBriefingItem("Hear the lamentations of their women");
		api.addBriefingItem("Red Sonja must survive");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "oddykoc_Standard", FleetMemberType.SHIP, "KoC Red Sonja", true);
		// Mark flagship as essential
		api.defeatOnShipLoss("KoC Red Sonja");

		api.addToFleet(FleetSide.PLAYER, "furykoc_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "valkyrie_ass_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "valkyrie_ass_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "shrikoc_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kocpest_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "strix_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "strix_Standard", FleetMemberType.SHIP, false);


		
		// Set up the enemy fleet
		
		FleetMemberAPI fleetMember;
		fleetMember = api.addToFleet(FleetSide.ENEMY, "google_geist_Standard", FleetMemberType.SHIP, "MBG Kalidor", true);
		fleetMember.getCaptain().setPersonality("aggressive");
		
		api.addToFleet(FleetSide.ENEMY, "google_phantom_Standard", FleetMemberType.SHIP, "Poledouris", true);;
		api.addToFleet(FleetSide.ENEMY, "google_harbinger_Standard", FleetMemberType.SHIP, "Bullshit", true);
		api.addToFleet(FleetSide.ENEMY, "google_harbinger_Standard", FleetMemberType.SHIP, "Cowshit", true);
		api.addToFleet(FleetSide.ENEMY, "google_harbinger_Standard", FleetMemberType.SHIP, "Dogshit", true);
		api.addToFleet(FleetSide.ENEMY, "google_harbinger_Standard", FleetMemberType.SHIP, "Bitchshit", true);
		api.addToFleet(FleetSide.ENEMY, "google_afflictor_Standard", FleetMemberType.SHIP, "Mog", true);		;

		api.addToFleet(FleetSide.ENEMY, "google_bastillon_Standard", FleetMemberType.SHIP, "DC-1a", true);
		api.addToFleet(FleetSide.ENEMY, "google_bastillon_Standard", FleetMemberType.SHIP, "DC-1b", true);
		api.addToFleet(FleetSide.ENEMY, "google_bastillon_Standard", FleetMemberType.SHIP, "DC-1g", true);
		api.addToFleet(FleetSide.ENEMY, "google_bastillon_Standard", FleetMemberType.SHIP, "DC-1d", true);
		api.addToFleet(FleetSide.ENEMY, "google_defender_Standard", FleetMemberType.SHIP, "DC-2a", true);
		api.addToFleet(FleetSide.ENEMY, "google_defender_Standard", FleetMemberType.SHIP, "DC-2b", true);
		api.addToFleet(FleetSide.ENEMY, "google_defender_Standard", FleetMemberType.SHIP, "DC-2g", true);
		api.addToFleet(FleetSide.ENEMY, "google_defender_Standard", FleetMemberType.SHIP, "DC-2d", true);	
		
		
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






