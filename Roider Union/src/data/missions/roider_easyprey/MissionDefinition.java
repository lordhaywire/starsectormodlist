package data.missions.roider_easyprey;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import static data.missions.Roider_DModManager.addDShipToFleet;
import java.util.Random;

// Easy Prey
public class MissionDefinition implements MissionDefinitionPlugin {
    @Override
	public void defineMission(MissionDefinitionAPI api) {
        Random random = new Random(3534634);

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "The ISS Archimedes");
		api.setFleetTagline(FleetSide.ENEMY, "Dirty pirates");

		// These show up as items in the bulleted list under
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat the pirates");


		// Set up the player's fleet
        addDShipToFleet(2, FleetSide.PLAYER, "roider_hound_Starfarer", "ISS Archimedes", true, random, api);

		// Mark player flagship as essential
		api.defeatOnShipLoss("ISS Archimedes");

		// Set up the enemy fleet
        addDShipToFleet(2, FleetSide.ENEMY, "buffalo2_FS", null, false, random, api);
        addDShipToFleet(1, FleetSide.ENEMY, "lasher_d_CS", null, false, random, api);
        addDShipToFleet(2, FleetSide.ENEMY, "mudskipper2_Hellbore", null, false, random, api);
//        addDShipToFleet(1, FleetSide.ENEMY, "hound_d_pirates_Standard", null, false, random, api);



		// Set up the map.
		float width = 10000f;
		float height = 9000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

		float minX = -width/2;
		float minY = -height/2;

//		for (int i = 0; i < 15; i++) {
//			float x = (float) Math.random() * width - width/2;
//			float y = (float) Math.random() * height - height/2;
//			float radius = 100f + (float) Math.random() * 900f;
//			api.addNebula(x, y, radius);
//		}

		api.addAsteroidField(0f, 0f, 0f, height,
									10f, 20f, 200);
		api.addAsteroidField(0f, 0f, 150f, height,
									20f, 40f, 50);

		api.addPlugin(new BaseEveryFrameCombatPlugin() {
            @Override
			public void init(CombatEngineAPI engine) {
				engine.getContext().setStandoffRange(8000f);
			}
		});
	}

}






