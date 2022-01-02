package data.scripts.world.systems;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;

public class Ivree {

	public void generate(SectorAPI sector) {
		
		StarSystemAPI system = sector.createStarSystem("Ivree");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background6.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI ivree_star = system.initStar("Ivree", // unique id for this star 
										"star_orange",  // id in planets.json
										    900f, 		  // radius (in pixels at default zoom)
										    350); // corona radius, from star edge
		system.setLightColor(new Color(249, 139, 21)); // light color in entire system, affects all entities
		
			SectorEntityToken ivreeAF1 = system.addTerrain(Terrain.ASTEROID_FIELD,
					new AsteroidFieldParams(
						200f, // min radius
						300f, // max radius
						8, // min asteroid count
						16, // max asteroid count
						4f, // min asteroid radius 
						16f, // max asteroid radius
						"Asteroids Field")); // null for default name
			
			ivreeAF1.setCircularOrbit(ivree_star, 130, 1750, 240);



		
		// Nahr: Sasso world.
		PlanetAPI ivree1 = system.addPlanet("sasso", ivree_star, "Sasso", "gas_giant", 0, 350, 3500, 300);



			PlanetAPI ivree1a = system.addPlanet("calanco", ivree1, "Calanco", "barren2", 60, 18, 650, 15);

			PlanetAPI ivree1b = system.addPlanet("barra", ivree1, "Barra", "desert1", 120, 50, 850, 30);

			PlanetAPI ivree1c = system.addPlanet("accona", ivree1, "Accona", "barren-desert", 230, 85, 1200, 45);

			SectorEntityToken BarraStation1 = system.addCustomEntity("barra_station", "Barra Shipyard", "station_side05", "luddic_church");
		
			BarraStation1.setCircularOrbitPointingDown( system.getEntityById("barra"), 45, 100, 80);
			BarraStation1.setCustomDescriptionId("barra_station1");
			
	
		SectorEntityToken relay2 = system.addCustomEntity(null,null, "nav_buoy_makeshift","luddic_church"); 
		relay2.setCircularOrbitPointingDown( ivree1, 300, 850, 30);

		
		// Panarea, Volcanic world
		PlanetAPI ivree2 = system.addPlanet("panarea", ivree_star, "Panarea", "lava", 70, 150, 5500, 355);


			PlanetAPI ivree2a = system.addPlanet("senesi", ivree2, "Senesi", "barren", 20, 15, 350, 13);

			PlanetAPI ivree2b = system.addPlanet("nirut", ivree2, "Nirut", "terran-eccentric", 120, 60, 600, 22);
		ivree2b.setCustomDescriptionId("planet_nirut");


			system.addRingBand(ivree_star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 6500, 40f);
		system.addAsteroidBelt(ivree_star, 150, 6300, 250, 150, 250, Terrain.ASTEROID_BELT, null);





		// Tophane, Ice giant
		PlanetAPI ivree3 = system.addPlanet("tophane", ivree_star, "Tophane", "ice_giant", 0, 400, 8500, 532);

			system.addRingBand(ivree3, "misc", "rings_ice0", 256f, 3, new Color(170,210,255,255), 256f, 600, 40f, Terrain.RING, null);
			PlanetAPI ivree3a = system.addPlanet("scilly", ivree3, "Scilly", "frozen1", 140, 120, 900, 25);
		ivree3a.setCustomDescriptionId("planet_scilly");


			PlanetAPI ivree3b = system.addPlanet("orcia", ivree3, "Orcia", "frozen2", 10, 55, 1200, 55);
		ivree3a.setCustomDescriptionId("planet_scilly");

			SectorEntityToken relay1 = system.addCustomEntity(null,null,"comm_relay_makeshift","hegemony"); // faction

	relay1.setCircularOrbitPointingDown(ivree3, 320, 900, 25);



                //Inner System Jump
                        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(
                        "inner_jump",
                        "Inner System Jump");
                        jumpPoint.setCircularOrbit(system.getEntityById("ivree"), 10, 5500, 335);
                        jumpPoint.setRelatedPlanet(ivree2);
			
                        jumpPoint.setStandardWormholeToHyperspaceVisual();
                        system.addEntity(jumpPoint);
			
		system.autogenerateHyperspaceJumpPoints(true, true);
		
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }


}
