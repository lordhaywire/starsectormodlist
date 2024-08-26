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

import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;

import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.util.Misc;

public class Arcadia {

	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Arcadia");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
		
		//system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_title");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("arcadia", // unique id for star
										 StarTypes.WHITE_DWARF, // id in planets.json
										 180f,		// radius (in pixels at default zoom)
										 300); // corona radius, from star edge
		
		system.setLightColor(new Color(200, 200, 200)); // light color in entire system, affects all entities
		//star.setCustomDescriptionId("star_white");
		
		
		PlanetAPI arcadia1 = system.addPlanet("nomios", star, "Nomios", "frozen", 90, 130, 3000, 100);
		arcadia1.setCustomDescriptionId("planet_nomios");
		
			SectorEntityToken nomios_location = system.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
			nomios_location.setCircularOrbitPointingDown( star, 90 + 60, 3000, 100);		

			// KoC_Chop
			SectorEntityToken koc_chop = system.addCustomEntity("koc_chop", "Louis Armstrong", "station_sporeship_derelict", "independent");
			koc_chop.setCircularOrbitPointingDown(system.getEntityById("nomios"), 270, 430, 30);		
			koc_chop.setCustomDescriptionId("KoC_Chop_Shop");
			koc_chop.setInteractionImage("illustrations", "pirate_station");
			koc_chop.addTag("KoC_Chop");
		
		PlanetAPI arcadia2 = system.addPlanet("syrinx", star, "Syrinx", "ice_giant", 180, 300, 6000, 200);
		arcadia2.setCustomDescriptionId("planet_syrinx");
			arcadia2.getMarket().addCondition(Conditions.VOLATILES_PLENTIFUL);
			arcadia2.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
			arcadia2.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		
			// Moon of syrinx w/ ship-wrecking & industrial stuff
			PlanetAPI arcadia2a = system.addPlanet("agreus", arcadia2, "Agreus", "barren", 0, 130, 1600, 50);
			arcadia2a.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "barren02"));
			arcadia2a.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "sindria"));
			arcadia2a.getSpec().setGlowColor(new Color(235,245,255,255));
			arcadia2a.getSpec().setUseReverseLightForGlow(true);
			arcadia2a.applySpecChanges();
			arcadia2a.setInteractionImage("illustrations", "industrial_megafacility"); // TODO something better for this.
			arcadia2a.setCustomDescriptionId("planet_agreus");
		
		system.addRingBand(arcadia2, "misc", "rings_asteroids0", 256f, 0, new Color(170,210,255,255), 256f, 800, 40f, Terrain.RING, null);
		system.addAsteroidBelt(arcadia2, 20, 1000, 128, 40, 80, Terrain.ASTEROID_BELT, null);
		
			// lagrangian point of Syrinx
			SectorEntityToken relay = system.addCustomEntity("syrinx_relay", // unique id
					 "Syrinx Relay", // name - if null, defaultName from custom_entities.json will be used
					 "comm_relay_makeshift", // type of object, defined in custom_entities.json
					 "hegemony"); // faction
			relay.setCircularOrbitPointingDown( system.getEntityById("arcadia"), 180 + 60, 6000, 200);
			
			// lagrangian point of Syrinx
			JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("syrinx_passage","Syrinx Passage");
			OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 180 - 60, 6000, 200);
			jumpPoint.setOrbit(orbit);	
			jumpPoint.setRelatedPlanet(arcadia2a);
			jumpPoint.setStandardWormholeToHyperspaceVisual();
			system.addEntity(jumpPoint);
		
//		SectorEntityToken arc_station = system.addOrbitalStation("arcadia_station", arcadia2, 45, 750, 30, "Citadel Arcadia", "hegemony");
//		arc_station.setCustomDescriptionId("station_arcadia"); 
		
		SectorEntityToken arc_station = system.addCustomEntity("arcadia_station", "Citadel Arcadia", "station_side02", "hegemony");
		arc_station.setCircularOrbitPointingDown(system.getEntityById("syrinx"), 45, 730, 30);		
		arc_station.setCustomDescriptionId("station_arcadia");
		arc_station.setInteractionImage("illustrations", "hound_hangar");
		
//My first planet BEGIN

		PlanetAPI arcadia3 = system.addPlanet("randis", star, "Randis", "barren-bombarded", 180, 120, 8000, 140); //angle/size/distance/period
		arcadia3.setCustomDescriptionId("planet_randis");
//			Misc.setAbandonedStationMarket("randis_abandoned_station_market", arcadia3);
		arcadia3.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "desert01"));
			arcadia3.getMarket().addCondition(Conditions.FARMLAND_POOR);
			arcadia3.getMarket().addCondition(Conditions.ORGANICS_COMMON);
			arcadia3.getMarket().addCondition(Conditions.ORE_ABUNDANT);
			arcadia3.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
			arcadia3.getMarket().addCondition(Conditions.LOW_GRAVITY);
			arcadia3.getMarket().addCondition(Conditions.METEOR_IMPACTS);
			arcadia3.applySpecChanges();


//Acceptaqble conditions = PRISTINE/GOOD/AVERAGE/BATTERED/WRECKED
//Normal Recovery (if false requires storypoint)) = true/false
		addDerelict(system, arcadia3, "oddykoc_Standard", ShipCondition.WRECKED, 100f, false);
		addDerelict(system, arcadia3, "atlas_c_standard", ShipCondition.BATTERED, 150f, true);
		addDerelict(system, arcadia3, "shrikoc_Standard", ShipCondition.AVERAGE, 175f, true);
		addDerelict(system, arcadia3, "shrikoc_Standard", ShipCondition.WRECKED, 300f, false);
		addDerelict(system, arcadia3, "kocpest_Standard", ShipCondition.WRECKED, 200f, false);
		addDerelict(system, arcadia3, "valhalla_Standard", ShipCondition.BATTERED, 250f, true);
		addDerelict(system, arcadia3, "valhalla_Standard", ShipCondition.WRECKED, 275f, false);
		addDerelict(system, arcadia3, "strix_Standard", ShipCondition.BATTERED, 325f, true);

		addDerelict(system, arcadia3, "geist_Standard", ShipCondition.WRECKED, 125f, false);
		addDerelict(system, arcadia3, "google_afflictor_Standard", ShipCondition.WRECKED, 225f, false);
		addDerelict(system, arcadia3, "google_bastillon_Standard", ShipCondition.BATTERED, 350f, true);
		addDerelict(system, arcadia3, "google_bastillon_Standard", ShipCondition.BATTERED, 375f, false);

//My first planet END

		float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.AVERAGE,
				3, 5, // min/max entities to add
				9400, // radius to start adding at 
				3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
				true, // whether to use custom or system-name based names
				false); // whether to allow habitable worlds

		// being cheeky here.
		if (StarSystemGenerator.random.nextFloat() > 0.5)  {
			StarSystemGenerator.addSystemwideNebula(system, StarAge.AVERAGE);
		}

		system.autogenerateHyperspaceJumpPoints(true, true);
		
		//system.addScript(new IndependentTraderSpawnPoint(sector, hyper, 1, 10, hyper.createToken(-6000, 2000), station));
	}

		protected void addDerelict(StarSystemAPI system, SectorEntityToken focus, String variantId, 
								ShipCondition condition, float orbitRadius, boolean recoverable) {
		DerelictShipData params = new DerelictShipData(new PerShipData(variantId, condition), false);
		SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
		ship.setDiscoverable(true);
		
		float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
		ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);
		
		if (recoverable) {
			ShipRecoverySpecialCreator creator = new ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
			Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
		}
		
	}
	
}
