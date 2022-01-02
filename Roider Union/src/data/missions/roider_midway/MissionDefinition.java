package data.missions.roider_midway;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import static data.missions.Roider_DModManager.addDShipToFleet;
import java.awt.Color;
import java.util.Random;
import scripts.world.systems.Roider_Atka;

// Midway
public class MissionDefinition implements MissionDefinitionPlugin {
    @Override
	public void defineMission(MissionDefinitionAPI api) {
        Random random = new Random(436233456);

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Roider Detachment");
		api.setFleetTagline(FleetSide.ENEMY, "Jagar's Red Raiders");

        String flagship = "ISS Hornet";
        String lewisShip = "ISS Clarke";
        String pirateShip = "Red Castle II";

		// These show up as items in the bulleted list under
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Destroy Warlord Jagar's flagship, the " + pirateShip);
		api.addBriefingItem("The " + flagship + " must survive");
        api.addBriefingItem("Use the Fighter Strike command to coordinate your carriers");


		// Set up the player's fleet
        // Cyclops, Pepperbox, Gambit, and Colossus are Early
        addDShipToFleet(1, FleetSide.PLAYER, "roider_ranch_Attack", flagship, true, random, api);
		api.defeatOnShipLoss(flagship);
        addDShipToFleet(2, FleetSide.PLAYER, "roider_colossus2_Roider", null, false, random, api);
        addDShipToFleet(3, FleetSide.PLAYER, "roider_cowboy_Support", null, false, random, api);
        addDShipToFleet(2, FleetSide.PLAYER, "roider_cowboy_Support", null, false, random, api);
        addDShipToFleet(2, FleetSide.PLAYER, "roider_cyclops_early_Balanced", null, false, random, api);
        addDShipToFleet(2, FleetSide.PLAYER, "roider_cyclops_early_Outdated", lewisShip, false, new Random(22345), api);
        addDShipToFleet(1, FleetSide.PLAYER, "roider_pepperbox_Escort", null, false, random, api);
        addDShipToFleet(1, FleetSide.PLAYER, "lasher_d_CS", null, false, random, api);



		// Set up the enemy fleet
//        addDShipToFleet(1, FleetSide.ENEMY, "atlas2_Standard", pirateShip, false, random, api);
        addDShipToFleet(1, FleetSide.ENEMY, "falcon_p_Strike", pirateShip, false, random, api);
        api.defeatOnShipLoss(pirateShip);
        addDShipToFleet(2, FleetSide.ENEMY, "colossus3_Pirate", null, false, random, api);
        addDShipToFleet(3, FleetSide.ENEMY, "colossus3_Pirate", null, false, random, api);
//        addDShipToFleet(2, FleetSide.ENEMY, "condor_Attack", null, false, random, api);
        addDShipToFleet(2, FleetSide.ENEMY, "condor_Strike", null, false, random, api);
        addDShipToFleet(3, FleetSide.ENEMY, "condor_Strike", null, false, random, api);
        addDShipToFleet(4, FleetSide.ENEMY, "buffalo2_FS", null, false, random, api);
        addDShipToFleet(1, FleetSide.ENEMY, "wolf_d_pirates_Attack", null, false, random, api);
        addDShipToFleet(1, FleetSide.ENEMY, "lasher_d_CS", null, false, random, api);
        addDShipToFleet(3, FleetSide.ENEMY, "mudskipper2_CS", null, false, random, api);
        addDShipToFleet(2, FleetSide.ENEMY, "hound_d_pirates_Standard", null, false, random, api);



		// Set up the map.
		float width = 16000f;
		float height = 14000f;
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

//		api.addPlugin(new BaseEveryFrameCombatPlugin() {
//            @Override
//			public void init(CombatEngineAPI engine) {
//				engine.getContext().setStandoffRange(10000f);
//			}
//		});

        StarSystemAPI fakeSys = Global.getSector().createStarSystem("wf");

        PlanetAPI kalekhta = fakeSys.addPlanet(Roider_Atka.KALEKHTA.id,
                    fakeSys.createToken(0,0), Roider_Atka.KALEKHTA.name,
                    "ice_giant", 130, 290, 8000, 400);
        kalekhta.getSpec().setPlanetColor(new Color(255,210,170,255));
        kalekhta.getSpec().setPitch(20f);
        kalekhta.getSpec().setTilt(10f);
        kalekhta.applySpecChanges();

        api.addPlanet(-55, 144, kalekhta.getRadius(), kalekhta, 0, true);
	}

}






