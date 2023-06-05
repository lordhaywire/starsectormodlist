package data.missions.xlu_laughingstocks;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.BattleObjectives;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		api.initFleet(FleetSide.PLAYER, "XLU", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);

		api.setFleetTagline(FleetSide.PLAYER, "Sonny and Grace");
		api.setFleetTagline(FleetSide.ENEMY, "HSS Fist of Chicomoztoc");
		
		api.addBriefingItem("Meister left us with a parting gift, a Hegemony battleship");
		api.addBriefingItem("Just like always, Onslaught's armed to the teeth, a distraction from Sonny will do the trick");
		api.addBriefingItem("If we get through this, I owe you boys a beer");
		
		boolean testMode = false;
		if (!testMode) {
			api.addToFleet(FleetSide.PLAYER, "xlu_zinc_Assault", FleetMemberType.SHIP, "XLU Grace-182", true);
			api.addToFleet(FleetSide.PLAYER, "xlu_alum_Assault", FleetMemberType.SHIP, "XLU Sonny9", false);
			
			// Set up the enemy fleet.
			api.addToFleet(FleetSide.ENEMY, "onslaught_xiv_Elite", FleetMemberType.SHIP, "HSS Fist of Chicomoztoc", false);
			
			api.defeatOnShipLoss("Stranger II");
		}
		
		if (testMode) {
			api.addToFleet(FleetSide.PLAYER, "falcon_Attack", FleetMemberType.SHIP, "Stranger II", true);
			
			api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, "Cherenkov Bloom", false);
			api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, null, false);
			api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, null, false);
			
			api.addObjective(0, 4000, BattleObjectives.SENSOR_JAMMER);
			api.addObjective(4000, 0, BattleObjectives.COMM_RELAY);
			api.addObjective(-3000, -2000, BattleObjectives.NAV_BUOY);
		}
		
		// Set up the map.
		float width = 12000f;
		float height = 12000f;
		
		if (testMode) {
			width += 4000;
			height += 8000;
		}
		
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
		api.addPlanet(0, 0, 50f, StarTypes.ORANGE_GIANT, 250f, true);
		
	}

}
