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
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;


public class Utic {

	public void generate(SectorAPI sector) {
		
		StarSystemAPI system = sector.createStarSystem("Utic");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background5.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI utic_star = system.initStar("Utic", // unique id for this star 
										"star_yellow",  // id in planets.json
										    700f, 		  // radius (in pixels at default zoom)
										    250); // corona radius, from star edge
		system.setLightColor(new Color(247, 177, 52)); // light color in entire system, affects all entities
		
			SectorEntityToken uticAF1 = system.addTerrain(Terrain.ASTEROID_FIELD,
					new AsteroidFieldParams(
						200f, // min radius
						300f, // max radius
						12, // min asteroid count
						20, // max asteroid count
						4f, // min asteroid radius 
						16f, // max asteroid radius
						"Asteroids Field")); // null for default name
			
			uticAF1.setCircularOrbit(utic_star, 230, 1400, 60);

		
		// Nahr: Toxic world.
		PlanetAPI utic1 = system.addPlanet("nahr", utic_star, "Nahr", "toxic_cold", 0, 130, 2600, 110);


			system.addRingBand(utic1, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 250, 40f, Terrain.RING, null);
			//system.addRingBand(utic1, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 210, 80f);

			PlanetAPI jazhirat = system.addPlanet("jazhirat", utic1, "Jazhirat", "barren_venuslike", 120, 50, 500, 15);

			system.addRingBand(utic_star, "misc", "rings_asteroids0", 256f, 2, Color.white, 256f, 3500, 220f, Terrain.RING, null);
			//system.addRingBand(utic_star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 3550, 80f);


			SectorEntityToken ChibogStation1 = system.addCustomEntity("chibog_station", "Chibog Outpost", "station_side05", "hegemony");
		
			ChibogStation1.setCircularOrbitPointingDown( system.getEntityById("Utic"), 325, 3500, 220);
			ChibogStation1.setCustomDescriptionId("ChibogStation1");


		
		// Nora, Ocean world
		PlanetAPI utic2 = system.addPlanet("nora", utic_star, "Nora", "water", 70, 230, 4500, 335);

			SectorEntityToken NoraStation1 = system.addCustomEntity("nora_abandoned_station1",
				"Abandoned Extraction Platform", "station_side06", "neutral");
		
			NoraStation1.setCircularOrbitPointingDown( system.getEntityById("nora"), 45, 350, 60);
			
			NoraStation1.setCustomDescriptionId("nora_station1");

			NoraStation1.setInteractionImage("illustrations", "filgap_nora");

			SectorEntityToken utic1_location = system.addCustomEntity(null,null, "comm_relay", "independent"); 
			utic1_location.setCircularOrbitPointingDown( utic_star, 250, 4500, 335);		


		// Nepheria, arid world
		PlanetAPI utic3 = system.addPlanet("nepheria", utic_star, "Nepheria", "arid", 0, 170, 5800, 532);
		utic3.setCustomDescriptionId("planet_nepheria");

			PlanetAPI acra = system.addPlanet("acra", utic3, "Acra", "barren-bombarded", 120, 45, 500, 15);

		system.addAsteroidBelt(utic_star, 100, 6800, 256, 200, 300, Terrain.ASTEROID_BELT, null);
			
		system.addRingBand(utic_star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 6800, 80f);


		// Tenas, Frozen world
		PlanetAPI utic4 = system.addPlanet("tenas", utic_star, "Tenas", "frozen1", 55, 120, 7500, 768);
		utic4.setCustomDescriptionId("planet_tenas");

			SectorEntityToken utic2_location = system.addCustomEntity(null,null, "stable_location", "neutral"); 
			utic2_location.setCircularOrbitPointingDown( utic_star, 155, 7500, 768);	


		// L4 & L5 mini-nebulas
		SectorEntityToken utic_L4_nebula = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(
				"  x   " +
				"  xx  " +
				"xxxxx " +
				" xxx  " +
				" xx x " +
				"   x  ",
				6, 6, // size of the nebula grid, should match above string
				"terrain", "nebula_blue", 4, 4, null));
		utic_L4_nebula.setId("utic_L4");
		SectorEntityToken utic_L5_nebula = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(
				"  x   " +
				" xx xx" +
				"x  xx " +
				" x xxx" +
				" x x x" +
				"  xx  ",
				6, 6, // size of the nebula grid, should match above string
				"terrain", "nebula_blue", 4, 4, null));
		utic_L5_nebula.setId("utic_L5");
		utic_L4_nebula.setCircularOrbit(utic_star, 0 + 60, 5800, 532);
		utic_L5_nebula.setCircularOrbit(utic_star, 0 - 60, 5800, 532);



                //Inner System Jump
                        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(
                        "inner_jump",
                        "Inner System Jump");
                        jumpPoint.setCircularOrbit(system.getEntityById("utic"), 10, 3750, 335);
                        jumpPoint.setRelatedPlanet(utic2);
			
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
