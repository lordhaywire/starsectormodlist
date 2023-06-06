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
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;

public class Alyro implements SectorGeneratorPlugin{

	public void generate(SectorAPI sector) {
		
		StarSystemAPI system = sector.createStarSystem("Alyro");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI alyro_star = system.initStar("Alyro", // unique id for this star 
											    "star_orange",  // id in planets.json
											    620f, 		  // radius (in pixels at default zoom)
											    200, // corona
											    5f, // solar wind burn level
												0.65f, // flare probability
												2f); // CR loss multiplier, good values are in the range of 1-5
		
		system.setLightColor(new Color(255, 220, 200)); // light color in entire system, affects all entities
		
		SectorEntityToken alyro_stable1 = system.addCustomEntity(null, null, "stable_location", "neutral");
		alyro_stable1.setCircularOrbitPointingDown(alyro_star, 0, 2750, 160);
		
		PlanetAPI alyro_a = system.addPlanet("oruss", alyro_star, "Oruss", "arid", 100, 90, 5450, 360);
		alyro_a.setCustomDescriptionId("planet_oruss");
		
		// alyro jump-point
		JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("alyro_jump", "Alyro Jump-point");
		jumpPoint1.setCircularOrbit( system.getEntityById("alyro"), 60 + 60, 4600, 140);
		jumpPoint1.setRelatedPlanet(alyro_a);
		system.addEntity(jumpPoint1);
		
		
		PlanetAPI alyro_b = system.addPlanet("rasion", alyro_star, "Rasion", "barren", 70 - 70, 65, 4600, 140);
		alyro_b.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "aurorae"));
		alyro_b.getSpec().setGlowColor(new Color(255,60,240,200));
		alyro_b.getSpec().setUseReverseLightForGlow(true);
		alyro_b.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "barren02"));
		alyro_b.applySpecChanges();

		// Alyro Relay
		SectorEntityToken relay = system.addCustomEntity("alyro_relay", "Alyro Relay", "comm_relay", "apocrita_association");
		relay.setCircularOrbitPointingDown(system.getEntityById("alyro"), 60 + 180, 4600, 140);
		
		float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, alyro_star, StarAge.AVERAGE,
				4, 5, // min/max entities to add
				6500, // radius to start adding at 
				3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
				true, // whether to use custom or system-name based names
				false); // whether to allow habitable worlds
		
		//StarSystemGenerator.addSystemwideNebula(system, StarAge.OLD);
		system.autogenerateHyperspaceJumpPoints(true, true);
	}
}