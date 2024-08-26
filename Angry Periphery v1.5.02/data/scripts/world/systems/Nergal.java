package data.scripts.world.systems;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class Nergal implements SectorGeneratorPlugin {

		public void generate(SectorAPI sector) {

		    	boolean presetConditions = true;

			StarSystemAPI system = sector.createStarSystem("Nergal");
			LocationAPI hyper = Global.getSector().getHyperspace();
		
			system.setBackgroundTextureFilename("data/scripts/world/systems/periphery.jpg");

//Set star

		PlanetAPI star = system.initStar(	"nergal", 					// unique id for star
							StarTypes.BROWN_DWARF, 				// type planets.json
							400f,						// radius (in pixels at default zoom)
							270); 						// corona radius, from star edge
//							star.setCustomDescriptionId("nergal_star"); 	// stars desciption
		system.setLightColor(new Color(216, 236, 153)); 					// light color in entire system, affects all entities

// Set Corona
		SectorEntityToken nergal_corona = system.addTerrain(Terrain.CORONA,
				new StarCoronaTerrainPlugin.CoronaParams(763, //Range
									381,	//Extra range?
									star,	//ORBITAL ENTITY
									1f, 	//WindStrength;
									0f, 	//flareProbability?;
									1f)	//crLossMult;
				);
//Add barrycenter

			SectorEntityToken barry_scott = system.addCustomEntity(	"barry_scott", // unique id
										"Binary Pair", // name - if null, defaultName from custom_entities.json will be used
										"sensor_ghost", // type of object, defined in custom_entities.json
										null); // faction
			barry_scott.setCircularOrbitPointingDown( system.getEntityById("nergal"), 180 + 60, 3500, 200);
// Set Planet

		PlanetAPI nergalP1 = system.addPlanet("nergal_planet_1",		//id
							barry_scott,				//orbital target
							"Lugalirra", 			//name
							"rocky_metallic", 			//type
							90, 110, 740, 24); 		//angle/size/distance/period (days) 4880
								nergalP1.setCustomDescriptionId("Nergal_Lugalirra");
//								nergalP1.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "surface_randis"));
//								peripheryP1.applySpecChanges();

							MarketAPI nergal_planet_1_market = Global.getFactory().createMarket("nergal_planet_1_market", nergalP1.getName(), 0);
							nergal_planet_1_market.setPlanetConditionMarketOnly(true);
							nergal_planet_1_market.setPrimaryEntity(nergalP1);
							nergalP1.setMarket(nergal_planet_1_market);

		PlanetAPI nergalP2 = system.addPlanet("nergal_planet_2",		//id
							barry_scott,				//orbital target
							"Meslamtaea", 			//name
							"rocky_ice", 			//type
							270, 130, 750, 24); 		//angle/size/distance/period (days) 4880
								nergalP2.setCustomDescriptionId("Nergal_Meslamtaea");
//								peripheryP1.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "surface_randis"));
//								peripheryP1.applySpecChanges();

							MarketAPI nergal_planet_2_market = Global.getFactory().createMarket("nergal_planet_2_market", nergalP2.getName(), 0);
							nergal_planet_2_market.setPlanetConditionMarketOnly(true);
							nergal_planet_2_market.setPrimaryEntity(nergalP1);
							nergalP2.setMarket(nergal_planet_2_market);

//Add a comm relay

			SectorEntityToken relay = system.addCustomEntity(	"barry_relay", // unique id
									 	"Gemini Relay", // name - if null, defaultName from custom_entities.json will be used
					 					"comm_relay_makeshift", // type of object, defined in custom_entities.json
					 					"sindrian_diktat"); // faction
			relay.setCircularOrbitPointingDown( system.getEntityById("barry_scott"), 70, 1330, 18);

//Set Station

		SectorEntityToken nergal_obs = system.addCustomEntity("nergal_obs", "Nergal Observatory", "station_side05", "independent");
		nergal_obs.setCircularOrbitPointingDown(system.getEntityById("nergal"), 45, 2050, 50);		
		nergal_obs.setCustomDescriptionId("Nergal_Observatory");
//		SectorEntityToken peripheryS1 = system.addCustomEntity("periphery_station", "Hope", "station_side06", "luddic_path");
//		peripheryS1.setCustomDescriptionId("periphery_station");
//		peripheryS1.setInteractionImage("illustrations", "pirate_station");
//		peripheryS1.setCircularOrbitWithSpin(	star, // orbital target
//							180,
//							5500, //distance
//							140, //period (days)
//							3,
//							5);

//								peripheryS1.getMarket().addCondition(Conditions.FARMLAND_POOR);
//								peripheryS1.getMarket().addCondition(Conditions.ORGANICS_COMMON);
//								peripheryS1.getMarket().addCondition(Conditions.ORE_ABUNDANT);
//								peripheryS1.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
//								peripheryS1.getMarket().addCondition(Conditions.LOW_GRAVITY);
//								peripheryS1.getMarket().addCondition(Conditions.METEOR_IMPACTS);

//							MarketAPI periphery_station_market = Global.getFactory().createMarket("periphery_station_market", peripheryS1.getName(), 0);
//							periphery_station_market.setPlanetConditionMarketOnly(true);
//							periphery_station_market.setPrimaryEntity(peripheryS1);
//							peripheryS1.setMarket(periphery_station_market);

// Set Asteroid Belt (INNER)
        	system.addAsteroidBelt(		star,			//ORBITAL ENTITY	
						200,			//Density
						1721,			//Distance (centre)
						72,			//Width
						77,			//Orbital Speed (low)
						155,			//Orbital Speed (high)
						Terrain.ASTEROID_BELT,
						"Dioscuri"); 			//default name	null	OR	"inner"
//Undeylying ring (INNER) (visual only)
        	system.addRingBand(		star,			//ORBITAL ENTITY
						"misc",			//visual cat (settings.json)
						"rings_asteroids0",	//effect
						256f,			//width			
						2,			//which one (see img)
						Color.gray,
						256f,			//width
						1672,			//distance
						110f);			//orbital period

        	system.addRingBand(		star,			//ORBITAL ENTITY
						"misc",			//visual cat (settings.json)
						"rings_asteroids0",	//effect
						256f,			//width			
						0,			//which one (see img)
						Color.gray,
						256f,			//width
						1771,			//distance
						104f);			//orbital period

// Set Asteroid Belt (OUTER)
//        	system.addAsteroidBelt(		star,			//ORBITAL ENTITY	
//						1024,			//Density
//						6250,			//Distance (centre)
//						600,			//Width
//						144,			//Orbital Speed (low)
//						208,			//Orbital Speed (high)
//						Terrain.ASTEROID_BELT,
//						null); 	 		//default name	null	OR	"outer"

//Undeylying ring (OUTER) (visual only)
//        	system.addRingBand(		star,			//ORBITAL ENTITY
//						"misc",			//visual cat (settings.json)
//						"rings_dust0",		//effect
//						256f,			//texture width			
//						2,			//which iteration (see img)
//						Color.gray,
//						500f,			//width
//						6250,			//distance
//						192f);			//orbital period
//
//      	system.addRingBand(		star,			//ORBITAL ENTITY
//						"misc",			//visual cat (settings.json)
//						"rings_asteroids0",	//effect
//						256f,			//width			
//						0,			//which iteration (see img)
//						Color.gray,
//						500f,			//width
//						6250,			//distance
//						160f);			//orbital period

//set nebula

		StarSystemGenerator.addSystemwideNebula(system, StarAge.OLD);

//set jumpgate
//			JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("periphery_gate","Pious Way");
//			OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 180 - 60, 4000, 200);
//			jumpPoint.setOrbit(orbit);	
//			jumpPoint.setRelatedPlanet(peripheryP1);
//			jumpPoint.setStandardWormholeToHyperspaceVisual();
//			system.addEntity(jumpPoint);


// And a jumpgate
//			SectorEntityToken ring = system.addCustomEntity("periphery_ring", // unique id
//					 "Reverent Ring", // name - if null, defaultName from custom_entities.json will be used
//					 "inactive_gate", // type of object, defined in custom_entities.json
//					 null); // faction
//			ring.setCircularOrbitPointingDown( system.getEntityById("periphery"), 30, 3000, 140);

// Add a few random planets on the outskirts

		float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.YOUNG,
				5, 7, // min/max entities to add
				5500, // radius to start adding at 
				2, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
				true, // whether to use custom or system-name based names
				false); // whether to allow habitable worlds

//set hyperspace jump points

		system.autogenerateHyperspaceJumpPoints(true, true);

	}

}
