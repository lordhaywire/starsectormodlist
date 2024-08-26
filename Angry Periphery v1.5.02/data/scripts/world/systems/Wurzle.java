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
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;

public class Wurzle implements SectorGeneratorPlugin {

		public void generate(SectorAPI sector) {

		    	boolean presetConditions = true;

			StarSystemAPI system = sector.createStarSystem("Wurzle");
			LocationAPI hyper = Global.getSector().getHyperspace();
		
			system.setBackgroundTextureFilename("data/scripts/world/systems/periphery.jpg");

//Set star

		PlanetAPI star = system.initStar(	"wurzle", 					// unique id for star
							StarTypes.BROWN_DWARF, 				// type planets.json
							421f,						// radius (in pixels at default zoom)
							427); 						// corona radius, from star edge
//							star.setCustomDescriptionId("wurzle_star"); 	// stars desciption
		system.setLightColor(new Color(189, 213 ,143)); 					// light color in entire system, affects all entities

// Set Corona
		SectorEntityToken wurzle_corona = system.addTerrain(Terrain.CORONA,
				new StarCoronaTerrainPlugin.CoronaParams(584, //Range
									427,	//Extra range?
									star,	//ORBITAL ENTITY
									1f, 	//WindStrength;
									0f, 	//flareProbability?;
									1f)	//crLossMult;
				);
//Add barrycenter

//			SectorEntityToken barry_scott = system.addCustomEntity(	"barry_scott", // unique id
//										"Binary Pair", // name - if null, defaultName from custom_entities.json will be used
//										"sensor_ghost", // type of object, defined in custom_entities.json
//										null); // faction
//			barry_scott.setCircularOrbitPointingDown( system.getEntityById("nergal"), 180 + 60, 3500, 200);
// Set Planet

		PlanetAPI big_k = system.addPlanet("big_k",		//id
							star,				//orbital target
							"Klendathu", 			//name
							"gas_giant", 			//type
							327, 342, 4478, 163); 		//angle/size/distance/period (days) 4880
// Set Planet (moon)
		PlanetAPI little_k = system.addPlanet("little_k",		//id
							big_k,				//orbital target
							"Harvester", 			//name
							"rocky_ice", 			//type
							58, 111, 1413, 46); 		//angle/size/distance/period (days) 4880

								little_k.setCustomDescriptionId("little_k_planet");
//								little_k.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "surface_lido"));
//
//								little_k.applySpecChanges();
//Set Station

		SectorEntityToken big_k_syphon = system.addCustomEntity("big_k_syphon", "Knomoore", "station_side07", "independent");
		big_k_syphon.setCustomDescriptionId("big_k_syphon_station");
		big_k_syphon.setInteractionImage("illustrations", "cargo_loading");
		big_k_syphon.setCircularOrbitWithSpin(	big_k, // orbital target
							180,
							380, //distance
							38, //period (days)
							3,
							5);
// Set Planet
		PlanetAPI scrumpy = system.addPlanet("scrumpy",		//id
							star,				//orbital target
							"Scrumpy", 			//name
							"water", 			//type
							100, 210, 3870, 163); 		//angle/size/distance/period (days) 4880
		                    scrumpy.setCustomDescriptionId("planet_scrumpy");

			SectorEntityToken scrumpy_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
					new MagneticFieldParams(200f, // terrain effect band width 
					160f, // terrain effect middle radius
					scrumpy, // entity that it's around
					60f, // visual band start
					260f, // visual band end
					new Color(50, 20, 100, 50), // base color
					0.25f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
					new Color(90, 180, 140),
					new Color(130, 145, 190),
					new Color(165, 110, 225), 
					new Color(95, 55, 240), 
					new Color(45, 0, 250),
					new Color(20, 0, 240),
					new Color(10, 0, 150)));
// Set Planet
//		PlanetAPI laganP2 = system.addPlanet("lagan_planet_2",		//id
//							star,				//orbital target
//							"Ligan", 			//name
//							"rocky_metallic",		//type
//							43, 95, 1627, 59); 		//angle/size/distance/period (days) 4880

// Pirate Station (Near)
//			SectorEntityToken jetsom_pirate_station = system.addCustomEntity("jetsam_pirate_station",
//					"Jetsam", "station_pirate_type", "pirates");
//			
//			jetsom_pirate_station.setCircularOrbitPointingDown(system.getEntityById("lagan_planet_1"), 240, 1672, 125);		
//			jetsom_pirate_station.setCustomDescriptionId("station_jetsam");
//			jetsom_pirate_station.setInteractionImage("illustrations", "pirate_station");

// Pirate Station (Far)
//			SectorEntityToken flotsam_pirate_station = system.addCustomEntity("flotsam_pirate_station",
//					"Flotsam", "station_pirate_type", "pirates");
			
//			flotsam_pirate_station.setCircularOrbitPointingDown(system.getEntityById("lagan"), 120, 6250, 125);		
//			flotsam_pirate_station.setCustomDescriptionId("station_flotsam");
//			flotsam_pirate_station.setInteractionImage("illustrations", "pirate_station");
//								nergalP1.setCustomDescriptionId("periphery_planet");
//								nergalP1.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "surface_randis"));
//								peripheryP1.applySpecChanges();

//							MarketAPI nergal_planet_1_market = Global.getFactory().createMarket("nergal_planet_1_market", nergalP1.getName(), 0);
//							nergal_planet_1_market.setPlanetConditionMarketOnly(true);
//							nergal_planet_1_market.setPrimaryEntity(nergalP1);
//							nergalP1.setMarket(nergal_planet_1_market);

//		PlanetAPI nergalP2 = system.addPlanet("nergal_planet_2",		//id
//							barry_scott,				//orbital target
//							"Meslamtaea", 			//name
//							"rocky_ice", 			//type
//							270, 130, 460, 24); 		//angle/size/distance/period (days) 4880
//								nergalP1.setCustomDescriptionId("periphery_planet");
//								peripheryP1.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "surface_randis"));
//								peripheryP1.applySpecChanges();

//							MarketAPI nergal_planet_2_market = Global.getFactory().createMarket("nergal_planet_2_market", nergalP2.getName(), 0);
//							nergal_planet_2_market.setPlanetConditionMarketOnly(true);
//							nergal_planet_2_market.setPrimaryEntity(nergalP1);
//							nergalP2.setMarket(nergal_planet_2_market);

//Add a comm relay

			SectorEntityToken relay = system.addCustomEntity(	"wurzle_relay", // unique id
									 	null, // name - if null, defaultName from custom_entities.json will be used
					 					"comm_relay_makeshift", // type of object, defined in custom_entities.json
					 					"independent"); // faction
			relay.setCircularOrbitPointingDown( system.getEntityById("wurzle"), 70, 2400, 110);

//Set Station

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
//        	system.addAsteroidBelt(		laganP1,			//ORBITAL ENTITY	
//						25,			//Density
//						1721,			//Distance (centre)
//						72,			//Width
//						77,			//Orbital Speed (low)
//						155,			//Orbital Speed (high)
//						Terrain.RING,
//						null); 			//default name	null	OR	"inner"
//Undeylying ring (INNER) (visual only)
//        	system.addRingBand(		laganP1,			//ORBITAL ENTITY
//						"misc",			//visual cat (settings.json)
//						"rings_dust0",	//effect
//						256f,			//width			
//						2,			//which one (see img)
//						Color.gray,
//						256f,			//width
//						1672,			//distance
//						110f);			//orbital period
//
  //      	system.addRingBand(		laganP1,			//ORBITAL ENTITY
	//					"misc",			//visual cat (settings.json)
	//					"rings_ice0",	//effect
	//					256f,			//width			
	//					0,			//which one (see img)
	///					Color.gray,
	//					256f,			//width
	//					1771,			//distance
	//					104f);			//orbital period
//
// Set Asteroid Belt (OUTER)
        	system.addAsteroidBelt(		star,			//ORBITAL ENTITY	
						25,			//Density
						1800,			//Distance (centre)
						72,			//Width
						77,			//Orbital Speed (low)
						155,			//Orbital Speed (high)
						Terrain.RING,
						null); 			//default name	null	OR	"inner"
//Undeylying ring (OUTER) (visual only)
        	system.addRingBand(		star,			//ORBITAL ENTITY
						"misc",			//visual cat (settings.json)
						"rings_dust0",	//effect
						256f,			//width			
						2,			//which one (see img)
						Color.gray,
						256f,			//width
						1800,			//distance
						110f);			//orbital period

        	system.addRingBand(		star,			//ORBITAL ENTITY
						"misc",			//visual cat (settings.json)
						"rings_ice0",	//effect
						256f,			//width			
						0,			//which one (see img)
						Color.gray,
						256f,			//width
						1800,			//distance
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

//		StarSystemGenerator.addSystemwideNebula(system, StarAge.OLD);

//set jumpgate
			JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("wurzle_gate","Cider Path Jump-Gate");
			OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 215, 4000, 163);
			jumpPoint.setOrbit(orbit);	
			jumpPoint.setRelatedPlanet(big_k);
			jumpPoint.setStandardWormholeToHyperspaceVisual();
			system.addEntity(jumpPoint);


// And a jumpgate
//			SectorEntityToken ring = system.addCustomEntity("periphery_ring", // unique id
//					 "Reverent Ring", // name - if null, defaultName from custom_entities.json will be used
//					 "inactive_gate", // type of object, defined in custom_entities.json
//					 null); // faction
//			ring.setCircularOrbitPointingDown( system.getEntityById("periphery"), 30, 3000, 140);

// Add a few random planets on the outskirts

		float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
				3, 5, // min/max entities to add
				6250, // radius to start adding at 
				2, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
				true, // whether to use custom or system-name based names
				false); // whether to allow habitable worlds

//set hyperspace jump points

		system.autogenerateHyperspaceJumpPoints(true, true);

	}

}
