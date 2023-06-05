package data.scripts.world.systems;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
/*
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import data.scripts.world.xlu.addMarketplace;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import java.util.ArrayList;
import java.util.Arrays;
*/

public class xlu_Phosmon {

	public void generate(SectorAPI sector) {
		
		StarSystemAPI system = sector.createStarSystem("Phosmon");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("phosmon",
				"star_white", // id in planets.json
				100f,
				150, // extent of corona outside star
				8f, // solar wind burn level
				0.5f, // flare probability
				1.5f); // CR loss multiplier, good values are in the range of 1-5
		
		system.setLightColor(new Color(255, 210, 200)); // light color in entire system, affects all entities
		
		
		PlanetAPI phos1 = system.addPlanet("phos1", star, "Lubrin", "arid", 0, 200, 2000, 360);
		
		JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("lubrin_gate", "Lubrin Gate");
		OrbitAPI orbit2 = Global.getFactory().createCircularOrbit(phos1, 30, 2000, 360);
		jumpPoint2.setOrbit(orbit2);
		jumpPoint2.setRelatedPlanet(phos1);
		jumpPoint2.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint2);
		
		PlanetAPI phos2 = system.addPlanet("phos2", star, "Gerfix", "frozen", 0, 100, 6000, 1200);
		
		SectorEntityToken relay = system.addCustomEntity("phosmon_relay", "Phosmon Relay", "comm_relay", "xlu");
		relay.setCircularOrbitPointingDown(system.getEntityById("phos2"), 0,  // angle
                                                                                            300, // orbit radius
                                                                                            240); // orbit days		
                
		/* The asteroid belt - some notable large ones? */ 
		system.addAsteroidBelt(star, 100, 9000, 1200, 150, 250);
                
		PlanetAPI phos3 = system.addPlanet("phos3", star, "Jheoff", "ice_giant", 0, 500, 12000, 2120);
                
                system.addRingBand(phos3, "misc", "rings_dust0", 256f, 1, Color.LIGHT_GRAY, 256f, 800, 7.9f);
                
		PlanetAPI phos3a = system.addPlanet("phos3a", phos3, "Jheoff I", "frozen", 0, 75, 1100, 60);
                
		system.autogenerateHyperspaceJumpPoints(true, true);
	}
	
}
