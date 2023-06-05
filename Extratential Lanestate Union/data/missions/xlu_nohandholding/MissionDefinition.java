package data.missions.xlu_nohandholding;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "XLU", FleetGoal.ATTACK, false, 10);
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "XLU Anthracite-2");
		api.setFleetTagline(FleetSide.ENEMY, "The Hegemony Gauntlet fleet");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		api.addBriefingItem("The Anthracite must survive");
		api.addBriefingItem("Harry the Hegemony fast, so we can get out of this mess.");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "xlu_anthracite_Standard", FleetMemberType.SHIP, "XLU Golem-2", true);
		
		// Mark player flagship as essential
		api.defeatOnShipLoss("XLU Anthracite-2");
		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "onslaught_xiv_Elite", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.ENEMY, "onslaught_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "onslaught_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "onslaught_Standard", FleetMemberType.SHIP, false);
		
		// Set up the map.
		float width = 10000f;
		float height = 12000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
	}

}






