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

public class Periphery implements SectorGeneratorPlugin {

		public void generate(SectorAPI sector) {

		    	boolean presetConditions = true;

			StarSystemAPI system = sector.createStarSystem("Periphery");
			LocationAPI hyper = Global.getSector().getHyperspace();
		
			system.setBackgroundTextureFilename("data/scripts/world/systems/periphery.jpg");

//Set star

		PlanetAPI star = system.initStar(	"periphery", 					// unique id for star
							StarTypes.NEUTRON_STAR, 			// type planets.json
							180f,						// radius (in pixels at default zoom)
							270); 						// corona radius, from star edge
							star.setCustomDescriptionId("periphery_star"); 	// stars desciption
		system.setLightColor(new Color(200, 200, 200)); 					// light color in entire system, affects all entities

// Set Pulsar Beams
		SectorEntityToken peripherybeams = system.addTerrain(Terrain.PULSAR_BEAM,
				new StarCoronaTerrainPlugin.CoronaParams(13500, //Range
									100,	//Extra range?
									star,	//ORBITAL ENTITY
									5f, 	//WindStrength;
									0f, 	//flareProbability?;
									2.5f)	//crLossMult;
				);

// Set Planet

		PlanetAPI peripheryP1 = system.addPlanet("periphery_planet",		//id
							star,				//orbital target
							"Brotherhood", 			//name
							"barren-bombarded", 		//type
							180, 120, 5000, 140); 		//angle/size/distance/period (days) 4880
								peripheryP1.setCustomDescriptionId("periphery_planet");
								peripheryP1.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "surface_randis"));
								peripheryP1.applySpecChanges();

							MarketAPI periphery_planet_market = Global.getFactory().createMarket("periphery_planet_market", peripheryP1.getName(), 0);
							periphery_planet_market.setPlanetConditionMarketOnly(true);
							periphery_planet_market.setPrimaryEntity(peripheryP1);
							peripheryP1.setMarket(periphery_planet_market);

//Set Station

		SectorEntityToken peripheryS1 = system.addCustomEntity("periphery_station", "Hope", "station_side06", "luddic_path");
		peripheryS1.setCustomDescriptionId("periphery_station");
		peripheryS1.setInteractionImage("illustrations", "pirate_station");
		peripheryS1.setCircularOrbitWithSpin(	star, // orbital target
							180,
							5500, //distance
							140, //period (days)
							3,
							5);

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
						1024,			//Density
						4750,			//Distance (centre)
						300,			//Width
						128,			//Orbital Speed (low)
						112,			//Orbital Speed (high)
						Terrain.ASTEROID_BELT,
						null); 			//default name	null	OR	"inner"
//Undeylying ring (INNER) (visual only)
        	system.addRingBand(		star,			//ORBITAL ENTITY
						"misc",			//visual cat (settings.json)
						"rings_dust0",	//effect
						256f,			//width			
						0,			//which one (see img)
						Color.gray,
						250f,			//width
						4750,			//distance
						160f);			//orbital period

        	system.addRingBand(		star,			//ORBITAL ENTITY
						"misc",			//visual cat (settings.json)
						"rings_asteroids0",	//effect
						256f,			//width			
						0,			//which one (see img)
						Color.gray,
						250f,			//width
						4750,			//distance
						128f);			//orbital period

// Set Asteroid Belt (OUTER)
        	system.addAsteroidBelt(		star,			//ORBITAL ENTITY	
						1024,			//Density
						6250,			//Distance (centre)
						600,			//Width
						144,			//Orbital Speed (low)
						208,			//Orbital Speed (high)
						Terrain.ASTEROID_BELT,
						null); 	 		//default name	null	OR	"outer"

//Undeylying ring (OUTER) (visual only)
        	system.addRingBand(		star,			//ORBITAL ENTITY
						"misc",			//visual cat (settings.json)
						"rings_dust0",		//effect
						256f,			//texture width			
						2,			//which iteration (see img)
						Color.gray,
						500f,			//width
						6250,			//distance
						192f);			//orbital period

        	system.addRingBand(		star,			//ORBITAL ENTITY
						"misc",			//visual cat (settings.json)
						"rings_asteroids0",	//effect
						256f,			//width			
						0,			//which iteration (see img)
						Color.gray,
						500f,			//width
						6250,			//distance
						160f);			//orbital period

//set nebula

		StarSystemGenerator.addSystemwideNebula(system, StarAge.OLD);

//set jumpgate
			JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("periphery_gate","Pious Way");
			OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 180 - 60, 4000, 200);
			jumpPoint.setOrbit(orbit);	
			jumpPoint.setRelatedPlanet(peripheryP1);
			jumpPoint.setStandardWormholeToHyperspaceVisual();
			system.addEntity(jumpPoint);

//Add a comm relay

			SectorEntityToken relay = system.addCustomEntity("periphery_relay", // unique id
					 "Litany Relay", // name - if null, defaultName from custom_entities.json will be used
					 "comm_relay_makeshift", // type of object, defined in custom_entities.json
					 "luddic_path"); // faction
			relay.setCircularOrbitPointingDown( system.getEntityById("periphery"), 180 + 60, 7750, 200);

			SectorEntityToken ring = system.addCustomEntity("periphery_ring", // unique id
					 "Reverent Ring", // name - if null, defaultName from custom_entities.json will be used
					 "inactive_gate", // type of object, defined in custom_entities.json
					 null); // faction
			ring.setCircularOrbitPointingDown( system.getEntityById("periphery"), 30, 3000, 140);

// Add a few random planets on the outskirts

		float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.YOUNG,
				3, 4, // min/max entities to add
				9000, // radius to start adding at 
				2, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
				true, // whether to use custom or system-name based names
				false); // whether to allow habitable worlds

//set hyperspace jump points

		system.autogenerateHyperspaceJumpPoints(true, true);

	}

}
