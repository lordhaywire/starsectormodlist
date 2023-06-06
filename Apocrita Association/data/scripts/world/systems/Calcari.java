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
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class Calcari implements SectorGeneratorPlugin {

	public void generate(SectorAPI sector) {
		
		boolean presetConditions = true;
		
		StarSystemAPI system = sector.createStarSystem("Calcari");
		//system.setType(StarSystemType.BINARY_FAR);
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI calcari_star = system.initStar("Calcari", // unique id for this star 
											StarTypes.YELLOW,  // id in planets.json
										    950f, 		  // radius (in pixels at default zoom)
										    500); // corona radius, from star edge
		
		system.setLightColor(new Color(250, 240, 210)); // light color in entire system, affects all entities
		
		PlanetAPI calcari1 = system.addPlanet("ymenn", calcari_star, "Ymenn", "lava_minor", 0, 120, 2000, 100);
		calcari1.setCustomDescriptionId("planet_ymenn");
		
		// Inner Asteroids
		system.addRingBand(calcari_star, "misc", "rings_asteroids0", 256f, 2, Color.white, 256f, 3700, 175f, null, null);
		system.addAsteroidBelt(calcari_star, 75, 3700, 256, 150, 200, Terrain.ASTEROID_BELT, null);
		
		
		PlanetAPI calcari2 = system.addPlanet("oec", calcari_star, "Oec", "irradiated", 10, 140, 4000, 330);
		calcari2.getSpec().setPlanetColor(new Color(180,235,255,255));
		calcari2.getSpec().setAtmosphereColor(new Color(120,100,120,250));
		calcari2.getSpec().setCloudColor(new Color(120,100,120,150));
		//calcari2.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "barren"));
		
		Misc.initConditionMarket(calcari2);
		calcari2.getMarket().addCondition(Conditions.METEOR_IMPACTS);
		calcari2.getMarket().addCondition(Conditions.ORE_MODERATE);
		calcari2.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
		
		SectorEntityToken oec_loc = system.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
		oec_loc.setCircularOrbitPointingDown( calcari_star, 180 - 60, 4400, 330);		
		
		// Outer asteroids
		
		system.addRingBand(calcari_star, "misc", "rings_asteroids0", 256f, 3, Color.white, 256f, 5100, 475f, null, null);
		system.addAsteroidBelt(calcari_star, 100, 5100, 256, 450, 500, Terrain.ASTEROID_BELT, null);
		
		PlanetAPI calcari3 = system.addPlanet("gesomyr", calcari_star, "Gesomyr", "gas_giant", 240, 350, 7300, 500);
		calcari3.getSpec().setPlanetColor(new Color(200,235,245,255));
		calcari3.getSpec().setAtmosphereColor(new Color(210,240,250,250));
		calcari3.getSpec().setCloudColor(new Color(220,250,240,200));
		calcari3.getSpec().setPitch(-22f);
		calcari3.getSpec().setTilt(-9f);
		calcari3.applySpecChanges();
	
			// Gesomyr rings
			system.addRingBand(calcari3, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 600, 31f);
			
			// add one ring that covers all of the above
			SectorEntityToken ring = system.addTerrain(Terrain.RING, new RingParams(150 + 256, 675, null, null));
			ring.setCircularOrbit(calcari3, 0, 0, 100);
			
			PlanetAPI calcari3a = system.addPlanet("apocrita", calcari3, "Apocrita", "jungle", 120, 95, 1500, 120);
			calcari3a.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
			calcari3a.getSpec().setGlowColor( new Color(255,20,60,255) );
			calcari3a.getSpec().setAtmosphereThickness(0.2f);
			calcari3a.getSpec().setUseReverseLightForGlow(true);
			calcari3a.applySpecChanges();
			calcari3a.setCustomDescriptionId("planet_apocrita");
			
			MarketAPI apocrita_market = Global.getFactory().createMarket("apocrita_market", calcari3a.getName(), 0);
			apocrita_market.setPlanetConditionMarketOnly(true);
			apocrita_market.setPrimaryEntity(calcari3a);
			calcari3a.setMarket(apocrita_market);
			
			Misc.initConditionMarket(calcari3a);
			calcari3a.getMarket().addCondition(Conditions.INIMICAL_BIOSPHERE);
			calcari3a.getMarket().addCondition(Conditions.RUINS_EXTENSIVE);
			calcari3a.getMarket().getFirstCondition(Conditions.RUINS_EXTENSIVE).setSurveyed(true);
			calcari3a.getMarket().addCondition(Conditions.ORE_SPARSE);
			calcari3a.getMarket().addCondition(Conditions.FARMLAND_ADEQUATE);
			calcari3a.getMarket().addCondition(Conditions.ORGANICS_ABUNDANT);
			
			//SectorEntityToken calcariStation = system.addCustomEntity("ymennS", "Ymenn Sation", "station_side06", "apocrita_association");
			//calcariStation.setInteractionImage("illustrations", "pirate_station");
			//calcariStation.setCircularOrbitWithSpin(calcari1, 180, 5500, 140, 3, 5);
			
			 SectorEntityToken calcari_buoy = system.addCustomEntity("calcari_buoy", // unique id
				     "Makeshift Nav Buoy", // name - if null, defaultName from custom_entities.json will be used
			    	 "nav_buoy_makeshift", // type of object, defined in custom_entities.json
			    	 "apocrita_association"); // faction
		    calcari_buoy.setCircularOrbitPointingDown( calcari3, 0, 1800, 50);
		
			// Gesomyr trojans
			SectorEntityToken gesomyrL4 = system.addTerrain(Terrain.ASTEROID_FIELD,
					new AsteroidFieldParams(
						500f, // min radius
						700f, // max radius
						20, // min asteroid count
						30, // max asteroid count
						4f, // min asteroid radius 
						16f, // max asteroid radius
						"Gesomyr L4 Asteroids")); // null for default name
			
			SectorEntityToken gesomyrL5 = system.addTerrain(Terrain.ASTEROID_FIELD,
					new AsteroidFieldParams(
						500f, // min radius
						700f, // max radius
						20, // min asteroid count
						30, // max asteroid count
						4f, // min asteroid radius 
						16f, // max asteroid radius
						"Gesomyr L5 Asteroids")); // null for default name
			
			gesomyrL4.setCircularOrbit(calcari_star, 45 + 60, 7300, 450);
			gesomyrL5.setCircularOrbit(calcari_star, 45 - 60, 7300, 450);
			
			// Apocrita Jumppoint
			JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("calcari_jump", "Calcari Jump-point");
			jumpPoint.setCircularOrbit( system.getEntityById("calcari"), 45+60, 7300, 450);
			jumpPoint.setRelatedPlanet(calcari3a);
			system.addEntity(jumpPoint);
			
			// Calcari Gate
			SectorEntityToken gate = system.addCustomEntity("calcari_gate", // unique id
					 "Calcari Gate", // name - if null, defaultName from custom_entities.json will be used
					 "inactive_gate", // type of object, defined in custom_entities.json
					 null); // faction
			gate.setCircularOrbit(system.getEntityById("calcari"), 20 + 5, 6800, 500);
			
			// Ymenn Relay - L5 (behind)
		    SectorEntityToken ymenn_relay = system.addCustomEntity("ymenn_relay", // unique id
				     "Ymenn Relay", // name - if null, defaultName from custom_entities.json will be used
			    	 "comm_relay", // type of object, defined in custom_entities.json
			    	 "apocrita_association"); // faction
		    ymenn_relay.setCircularOrbitPointingDown( calcari_star, 0 - 60, 2000, 100);
		
		system.autogenerateHyperspaceJumpPoints(true, true);
	}
}
