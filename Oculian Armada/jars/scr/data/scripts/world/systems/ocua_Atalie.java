package data.scripts.world.systems;

import com.fs.starfarer.api.EveryFrameScript;
import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import data.scripts.campaign.econ.OCUA_industries;
import java.util.ArrayList;
import java.util.Arrays;


public class ocua_Atalie {

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
            StarSystemAPI system = sector.createStarSystem("Atalie Pax");
            system.getLocation().set(0, 19000);
            LocationAPI hyper = Global.getSector().getHyperspace();
	   
            system.setBackgroundTextureFilename("graphics/backgrounds/background3.jpg");
            
            // create the star and generate the hyperspace anchor for this system
            PlanetAPI star = system.initStar("ocua_atalie", // unique id for star
										 "star_red_dwarf", // id in planets.json
										 150f,		// radius (in pixels at default zoom)
										 250, // corona radius, from star edge
										 5f, // solar wind burn level
										 0.5f, // flare probability
										 2f); // cr loss mult

            PlanetAPI ocua_atalie1 = system.addPlanet("ocua_atalie1", star, "Atalie I", "lava",
										0, // angle
										90, // radius, size
										800, // orbit radius
                                                                                100); // orbit days);
		
            SectorEntityToken ocua_atalie_buoy = system.addCustomEntity(null, null, "nav_buoy_makeshift", "ocua");
            ocua_atalie_buoy.setCircularOrbitPointingDown(star, 60, 1500, 320);
		
            PlanetAPI ocua_atalie2 = system.addPlanet("ocua_atalie2", star, "Atalie II", "barren-desert", 0, 120, 2500, 200);
            ocua_atalie2.setCustomDescriptionId("ocua_planet_atalie2");

		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("ocua_atalie2_gate", "Atalie II Gate");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(ocua_atalie2, 45, 500, 100);
		jumpPoint.setOrbit(orbit);
		jumpPoint.setRelatedPlanet(ocua_atalie2);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);
		
            MarketAPI ocua_atalie2Market = addMarketplace("pirates", ocua_atalie2,
                null,
                "Atalie II", 5, // 3 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.HOT,
                        Conditions.MILD_CLIMATE,
                        Conditions.ORE_ABUNDANT,
                        Conditions.RARE_ORE_MODERATE,
                        Conditions.POPULATION_5)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION, Commodities.GAMMA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)),
                        new ArrayList<>(Arrays.asList(Industries.MINING)),
                        new ArrayList<>(Arrays.asList(Industries.REFINING)),
                        new ArrayList<>(Arrays.asList(Industries.GROUNDDEFENSES)),
                        new ArrayList<>(Arrays.asList(Industries.MILITARYBASE)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)))),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.GENERIC_MILITARY, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f,
                false
            );
        
	//	SectorEntityToken OculusStationI = system.addCustomEntity("atalie_oculus_station1", "Fita Star Station", "station_side05", "oculus");
	//	OculusStationI.setCircularOrbitPointingDown(system.getEntityById("atalie2"), 0,  // angle
        //                                                                                    150, // orbit radius
        //                                                                                    60); // orbit days		
	//	OculusStationI.setCustomDescriptionId("ob_station_fita");
	//	OculusStationI.setInteractionImage("illustrations", "industrial_megafacility");
                
            PlanetAPI ocua_atalie2a = system.addPlanet("ocua_atalie2a", ocua_atalie2, "Atalie II-A", "barren", 45, 30, 200, 40);
            PlanetAPI ocua_atalie2b = system.addPlanet("ocua_atalie2b", ocua_atalie2, "Atalie II-B", "terran-eccentric", 120, 70, 350, 60);
            ocua_atalie2b.setCustomDescriptionId("ocua_planet_atalie2b");

            MarketAPI atalie2bMarket = addMarketplace("ocua", ocua_atalie2b,
                null,
                "Atalie II-B", 4, // 2 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.HABITABLE,
                        Conditions.MILD_CLIMATE,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.ORE_SPARSE,
                        Conditions.RARE_ORE_SPARSE,
                        Conditions.ORGANICS_TRACE,
                        Conditions.RUINS_WIDESPREAD,
                        Conditions.POPULATION_4)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)),
                        new ArrayList<>(Arrays.asList(Industries.LIGHTINDUSTRY)),
                        new ArrayList<>(Arrays.asList(Industries.FARMING, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(OCUA_industries.OCUA_STATION_LVL1)),
                        new ArrayList<>(Arrays.asList(Industries.PATROLHQ)),
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)))),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f,
                false
            );
            
		system.addAsteroidBelt(star, 100, 3500, 2000, 100, 100);
		
            PlanetAPI ocua_atalie3 = system.addPlanet("ocua_atalie3", star, "Adalie", "tundra", 180, 150, 5000, 240);
            ocua_atalie3.setCustomDescriptionId("ocua_planet_adalie");
		
            MarketAPI ocua_adalieMarket = addMarketplace("ocua", ocua_atalie3,
                null,
                "Adalie", 6, // 4 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.COLD,
                        Conditions.MILD_CLIMATE,
                        Conditions.INIMICAL_BIOSPHERE,
                        Conditions.ORGANICS_COMMON,
                        Conditions.VOLATILES_DIFFUSE,
                        Conditions.POPULATION_6)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)),
                        new ArrayList<>(Arrays.asList(Industries.MINING, Commodities.GAMMA_CORE)),
                        new ArrayList<>(Arrays.asList(OCUA_industries.OCUA_COOKIE_INDUSTRY)),
                        new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES)),
                        new ArrayList<>(Arrays.asList(OCUA_industries.OCUA_STATION_LVL3, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.HIGHCOMMAND, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(OCUA_industries.OCUA_ORBITAL_MATRIX)), //, Items.PRISTINE_NANOFORGE
                        new ArrayList<>(Arrays.asList(Industries.MEGAPORT)))),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.GENERIC_MILITARY, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f,
                false
            );
            
            CargoAPI cargo = ocua_adalieMarket.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo();
            cargo.addSpecial(new SpecialItemData("ocua_industry_bp", "ocua_station_lvl1"), 1);
            cargo.addSpecial(new SpecialItemData("ocua_industry_bp", "ocua_orbital_matrix"), 1);
	//	SectorEntityToken AdalieStation = system.addCustomEntity("adalie_oculus_station", "Pax Proper", "station_mining00", "oculus");
	//	AdalieStation.setCircularOrbitPointingDown(system.getEntityById("atalie3"), 0,  // angle
        //                                                                                    240, // orbit radius
        //                                                                                    60); // orbit days		
	//	AdalieStation.setCustomDescriptionId("ocua_station_pax_proper");
	//	AdalieStation.setInteractionImage("illustrations", "urban03");
		

            PlanetAPI ocua_atalie3a = system.addPlanet("ocua_atalie3a", ocua_atalie3, "Atalie III-A", "rocky_metallic", 0, 90, 700, 70);
            ocua_atalie3a.setCustomDescriptionId("ocua_planet_atalie3a");

            MarketAPI ocua_atalie3aMarket = addMarketplace("ocua", ocua_atalie3a,
                null,
                "Atalie III-A", 5, // 3 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.NO_ATMOSPHERE,
                        Conditions.RARE_ORE_ABUNDANT,
                        Conditions.ORE_RICH,
                        Conditions.VOLATILES_PLENTIFUL,
                        Conditions.POPULATION_5)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION, Commodities.GAMMA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)),
                        new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES)),
                        new ArrayList<>(Arrays.asList(Industries.MINING, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.REFINING, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.FUELPROD, Items.SYNCHROTRON, Commodities.BETA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)))),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f,
                false
            );
            // Atalie Relay
            SectorEntityToken ocua_atalie_relay = system.addCustomEntity(null, null, "comm_relay", "ocua");
            ocua_atalie_relay.setCircularOrbitPointingDown(star, 270 + 60, 6500, 340);
                
            PlanetAPI ocua_atalie4 = system.addPlanet("ocua_atalie4", star, "Atalie IV", "ice_giant", 0, 240, 8000, 560);   
            
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
		 */
		system.addRingBand(ocua_atalie4, "misc", "rings_ice0", 50f, 4, Color.red, 50, 340, 20f);
		SectorEntityToken ocua_atalie4_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldParams(ocua_atalie4.getRadius() + 200f, // terrain effect band width 
						(ocua_atalie4.getRadius() + 200f) / 2f, // terrain effect middle radius
						ocua_atalie4, // entity that it's around
						ocua_atalie4.getRadius() + 50f, // visual band start
						ocua_atalie4.getRadius() + 50f + 250f, // visual band end
						new Color(50, 20, 100, 40), // base color
						0.5f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
						new Color(140, 100, 235),
						new Color(180, 110, 210),
						new Color(150, 140, 190),
						new Color(140, 190, 210),
						new Color(90, 200, 170), 
						new Color(65, 230, 160),
						new Color(20, 220, 70)
				));
		ocua_atalie4_field.setCircularOrbit(ocua_atalie4, 0, 0, 100);
		
            PlanetAPI ocua_atalie4a = system.addPlanet("ocua_atalie4a", ocua_atalie4, "Atalie IV-a", "frozen", 0, 50, 800, 60);

            SectorEntityToken ocua_atalie_sensor = system.addCustomEntity(null, null, "sensor_array", "ocua");
            ocua_atalie_sensor.setCircularOrbitPointingDown(star, 55 + 60, 9000, 640);
		
            PlanetAPI ocua_atalie5 = system.addPlanet("ocua_atalie5", star, "Atalie V", "frozen", 180, 40, 18000, 520);       

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