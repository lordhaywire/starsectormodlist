package data.scripts.world.systems;

import com.fs.starfarer.api.EveryFrameScript;
import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import data.scripts.campaign.econ.OCUA_industries;
import java.util.ArrayList;
import java.util.Arrays;


public class ocua_Haelim {

        public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity,
                ArrayList<SectorEntityToken> connectedEntities, String name, int size,
                ArrayList<String> conditionList, ArrayList<ArrayList<String>> industryList, ArrayList<String> submarkets,
                float tarrif, boolean freePort) {
            EconomyAPI globalEconomy = Global.getSector().getEconomy();
            String planetID = primaryEntity.getId();
            String marketID = planetID/* + "_market"*/;

            MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
            newMarket.setFactionId(factionID);
            newMarket.setPrimaryEntity(primaryEntity);
            newMarket.getTariff().modifyFlat("generator", tarrif);
            newMarket.getLocationInHyperspace().set(primaryEntity.getLocationInHyperspace());

            if (null != submarkets) {
                for (String market : submarkets) {
                    newMarket.addSubmarket(market);
                }
            }

            for (String condition : conditionList) {
                newMarket.addCondition(condition);
            }

            for (ArrayList<String> industryWithParam : industryList) {
                String industry = industryWithParam.get(0);
                if (industryWithParam.size() == 1) {
                    newMarket.addIndustry(industry);
                } else {
                    newMarket.addIndustry(industry, industryWithParam.subList(1, industryWithParam.size()));
                }
            }

            if (null != connectedEntities) {
                for (SectorEntityToken entity : connectedEntities) {
                    newMarket.getConnectedEntities().add(entity);
                }
            }

            newMarket.setFreePort(freePort);
            globalEconomy.addMarket(newMarket, true);
            primaryEntity.setMarket(newMarket);
            primaryEntity.setFaction(factionID);

            if (null != connectedEntities) {
                for (SectorEntityToken entity : connectedEntities) {
                    entity.setMarket(newMarket);
                    entity.setFaction(factionID);
                }
            }

            return newMarket;
        }

    public void generate(SectorAPI sector) {
            StarSystemAPI system = sector.createStarSystem("Haelim");
            system.getLocation().set(2000, 12200);
            LocationAPI hyper = Global.getSector().getHyperspace();
	   
            system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");
            
            // create the star and generate the hyperspace anchor for this system
            PlanetAPI star = system.initStar("ocua_haelim", // unique id for star
										 "star_orange", // id in planets.json
										 120f,		// radius (in pixels at default zoom)
										 150, // corona radius, from star edge
										 3f, // solar wind burn level
										 1.5f, // flare probability
										 1f); // cr loss mult

            PlanetAPI ocua_haelim1 = system.addPlanet("ocua_haelim1", star, "Haelim I", "lava_minor",
										0, // angle
										90, // radius, size
										600, // orbit radius
                                                                                160); // orbit days);
            ocua_haelim1.setCustomDescriptionId("ocua_planet_haelim1");
		
            MarketAPI ocua_haelim1Market = addMarketplace("ocua", ocua_haelim1,
                null,
                "Haelim I", 4, // 2 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.VERY_HOT,
                        Conditions.MILD_CLIMATE,
                        Conditions.ORE_ABUNDANT,
                        Conditions.RARE_ORE_MODERATE,
                        Conditions.POPULATION_4)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)),
                        new ArrayList<>(Arrays.asList(Industries.MINING)),
                        new ArrayList<>(Arrays.asList(Industries.REFINING)),
                        new ArrayList<>(Arrays.asList(Industries.GROUNDDEFENSES)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)))),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f,
                false
            );
        
            PlanetAPI ocua_haelim2 = system.addPlanet("ocua_haelim2", star, "Darla", "arid", 0, 160, 2000, 250);
            ocua_haelim2.setCustomDescriptionId("ocua_planet_darla");

            MarketAPI ocua_darlaMarket = addMarketplace("ocua", ocua_haelim2,
                null,
                "Darla", 5, // 3 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.HOT,
                        Conditions.HABITABLE,
                        Conditions.VOLATILES_DIFFUSE,
                        Conditions.ORGANICS_ABUNDANT,
                        Conditions.RUINS_WIDESPREAD,
                        Conditions.POPULATION_5)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)),
                        new ArrayList<>(Arrays.asList(Industries.MILITARYBASE, Commodities.BETA_CORE)),
                        new ArrayList<>(Arrays.asList("commerce", Commodities.BETA_CORE)),
                        new ArrayList<>(Arrays.asList(OCUA_industries.OCUA_COOKIE_INDUSTRY, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(OCUA_industries.OCUA_STATION_LVL2, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES, Commodities.GAMMA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)))),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.GENERIC_MILITARY, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f,
                false
            );
        
            PlanetAPI ocua_haelim2a = system.addPlanet("ocua_haelim2a", ocua_haelim2, "Haelim II Minor", "barren3", 45, 30, 300, 40);
            
            MarketAPI haelim2aMarket = addMarketplace("ocua", ocua_haelim2a,
                null,
                "Haelim II Minor", 4, // 2 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.VERY_COLD,
                        Conditions.ORE_MODERATE,
                        Conditions.RARE_ORE_SPARSE,
                        Conditions.POPULATION_4)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.MINING)),
                        new ArrayList<>(Arrays.asList(Industries.REFINING)),
                        new ArrayList<>(Arrays.asList(Industries.PATROLHQ)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)))),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f,
                false
            );
            
            SectorEntityToken ocua_haelim_sensor = system.addCustomEntity(null, null, "sensor_array_makeshift", "ocua");
            ocua_haelim_sensor.setCircularOrbitPointingDown(star, 75, 2800, 310);
		
            /*
             * addAsteroidBelt() parameters:
             * 1. What the belt orbits
             * 2. Number of asteroids
             * 3. Orbit radius
             * 4. Belt width
             * 6/7. Range of days to complete one orbit. Value picked randomly for each asteroid.
             */

	     system.addAsteroidBelt(star, 100, 3500, 2000, 100, 100);
		
            /*
             * addRingBand() parameters:
             * 1. What it orbits
             * 2. Category under "graphics" in settings.json
             * 3. Key in category
             * 4. Width of band within the texture
             * 5. Index of band
             * 6. Color to apply to band
             * 7. Width of band (in the game)
             * 8. Orbit radius (of the middle of the band)
             * 9. Orbital period, in days
                 
             1.                      2.      3.             4.    5. 6.           7.    8.   9.  */
            system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 2925, 300f);
            system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 3000, -300f);
            system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 3075, 280f);
        
            PlanetAPI ocua_haelim3 = system.addPlanet("ocua_haelim3", star, "Haelim III", "rocky_metallic", 100, 150, 5000, 320);

		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("ocua_haelim_gate", "Haelim Primary Gate");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(ocua_haelim3, 45, 500, 100);
		jumpPoint.setOrbit(orbit);
		jumpPoint.setRelatedPlanet(ocua_haelim3);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);

		//system.addRingBand(haelim3, "misc", "rings_ice0", 50f, 4, Color.red, 50, 340, 20f);
		
            SectorEntityToken ocua_haelim_relay = system.addCustomEntity(null, null, "comm_relay", "ocua");
            ocua_haelim_relay.setCircularOrbitPointingDown(star, 270, 5800, 380);
                
            PlanetAPI ocua_haelim4 = system.addPlanet("ocua_haelim4", star, "Haelim IV", "rocky_ice", 0, 70, 7500, 460);
		
            PlanetAPI ocua_haelim5 = system.addPlanet("ocua_haelim5", star, "Haelim V", "rocky_metallic", 0, 50, 10000, 520);

		system.autogenerateHyperspaceJumpPoints(true, true);
    }
    
    public static class Demilitarize implements EveryFrameScript {

        private final MarketAPI market;

        Demilitarize(MarketAPI market) {
            this.market = market;
        }

        @Override
        public void advance(float amount) {
            if (market.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
                market.removeSubmarket(Submarkets.GENERIC_MILITARY);
            }
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public boolean runWhilePaused() {
            return false;
        }
    }
}