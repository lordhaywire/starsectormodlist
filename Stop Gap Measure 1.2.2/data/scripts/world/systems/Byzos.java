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

public class Byzos {

	public void generate(SectorAPI sector) {
		
		StarSystemAPI system = sector.createStarSystem("Byzos");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background6.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI byzos_star = system.initStar("Byzos", // unique id for this star 
										"star_white",  // id in planets.json
										    500f, 		  // radius (in pixels at default zoom)
										    200); // corona radius, from star edge
		system.setLightColor(new Color(255, 252, 200)); // light color in entire system, affects all entities
		
		
		// Lepia: Toxic arid world.
		PlanetAPI byzos1 = system.addPlanet("lepia", byzos_star, "Lepia", "arid", 30, 140, 2000, 110);
		byzos1.setCustomDescriptionId("planet_lepia");

			PlanetAPI beotia = system.addPlanet("beotia", byzos1, "Beotia", "toxic", 80, 80, 500, 25);

			PlanetAPI caryst = system.addPlanet("caryst", byzos1, "Caryst", "barren_venuslike", 150, 40, 700, 32);


			SectorEntityToken byzosAF1 = system.addTerrain(Terrain.ASTEROID_FIELD,
					new AsteroidFieldParams(
						200f, // min radius
						300f, // max radius
						12, // min asteroid count
						18, // max asteroid count
						4f, // min asteroid radius 
						16f, // max asteroid radius
						"Asteroids Field")); // null for default name
			
			byzosAF1.setCircularOrbit(byzos_star, 230, 3300, 200);
		
		// Pharos, desert world
		PlanetAPI byzos2 = system.addPlanet("pharos", byzos_star, "Pharos", "barren-desert", 70, 170, 5000, 335);

			PlanetAPI poros = system.addPlanet("poros", byzos2, "Poros", "barren", 60, 40, 900, 50);

			PlanetAPI bythinia = system.addPlanet("bythinia", byzos2, "Bythinia", "desert", 150, 85, 550, 32);
		bythinia.setCustomDescriptionId("planet_bythinia");

			bythinia.setInteractionImage("illustrations", "filgap_bythinia");

		SectorEntityToken byzos2_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldParams(byzos2.getRadius() + 160f, // terrain effect band width 
				(byzos2.getRadius() + 160f) / 2f, // terrain effect middle radius
				byzos2, // entity that it's around
				byzos2.getRadius() + 50f, // visual band start
				byzos2.getRadius() + 50f + 200f, // visual band end
				new Color(50, 20, 100, 50), // base color
				0.5f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
				new Color(90, 180, 40),
				new Color(130, 145, 90),
				new Color(165, 110, 145), 
				new Color(95, 55, 160), 
				new Color(45, 0, 130),
				new Color(20, 0, 130),
				new Color(10, 0, 150)));
		byzos2_field.setCircularOrbit(byzos2, 0, 0, 100);



			SectorEntityToken byzosAF2 = system.addTerrain(Terrain.ASTEROID_FIELD,
					new AsteroidFieldParams(
						400f, // min radius
						600f, // max radius
						16, // min asteroid count
						24, // max asteroid count
						4f, // min asteroid radius 
						16f, // max asteroid radius
						"Asteroids Field")); // null for default name
			
			byzosAF2.setCircularOrbit(byzos_star, 180, 6500, 450);

		SectorEntityToken byzos_location = system.addCustomEntity(null,null, "comm_relay_makeshift","luddic_church"); 
		byzos_location.setCircularOrbitPointingDown( byzos_star, 330, 5000, 335);

                //Inner System Jump
                        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(
                        "inner_jump",
                        "Inner System Jump");
                        jumpPoint.setCircularOrbit(system.getEntityById("Byzos"), 130, 5000, 335);
                        jumpPoint.setRelatedPlanet(byzos2);
			
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
