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
import data.campaign.econ.XLU_industries;
import data.scripts.ids.XLU_HullMods;
import java.util.ArrayList;
import java.util.Arrays;
/*
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import data.scripts.world.XLU.addMarketplace;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import java.util.ArrayList;
import java.util.Arrays;
*/

public class xlu_Nemvis{

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
		
		StarSystemAPI system = sector.createStarSystem("Nemvis");
                system.getLocation().set(12000, 5500);
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("pt_nemvis",
				"star_orange", // id in planets.json
				600f,
				350, // extent of corona outside star
				9f, // solar wind burn level
				1f, // flare probability
				2f); // CR loss multiplier, good values are in the range of 1-5
		
		system.setLightColor(new Color(255, 210, 200)); // light color in entire system, affects all entities
		
		
		/*
		 * addPlanet() parameters:
		 * 1. What the planet orbits (orbit is always circular)
		 * 2. Name
		 * 3. Planet type id in planets.json
		 * 4. Starting angle in orbit, i.e. 0 = to the right of the star
		 * 5. Planet radius, pixels at default zoom
		 * 6. Orbit radius, pixels at default zoom
		 * 7. Days it takes to complete an orbit. 1 day = 10 seconds.
		 */
		
		// Or: Grimnir   / The Einherjar / Heidrun / Eikthyrnir ?
		PlanetAPI nemv1 = system.addPlanet("nemv1", star, "Hixon", "lava", 210, 100, 3500, 300);
		nemv1.setCustomDescriptionId("xlu_planet_hixon");
		
        MarketAPI hixonMarket = addMarketplace("xlu", nemv1,
                null,
                "Hixon", 5, // 3 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.INIMICAL_BIOSPHERE,
                        Conditions.VERY_HOT, 
                        Conditions.TECTONIC_ACTIVITY,
                        Conditions.VOLATILES_DIFFUSE,
                        Conditions.ORE_ULTRARICH,
                        Conditions.RARE_ORE_RICH,
                        Conditions.POPULATION_5)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(XLU_industries.XLU_STATION1)),
                        new ArrayList<>(Arrays.asList(Industries.PATROLHQ)),
                        new ArrayList<>(Arrays.asList(Industries.MINING, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.REFINING)),
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)))),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f,
                false
        );

		/* The asteroid belt - some notable large ones? */ 
		system.addAsteroidBelt(star, 100, 5500, 1200, 150, 250);
		
		PlanetAPI nemv2 = system.addPlanet("nemv2", star, "Vargan", "terran", 0, 300, 7000, 860);
		nemv2.setCustomDescriptionId("xlu_planet_vargan");
		
        SectorEntityToken vargan_station = system.addCustomEntity("nemv_station1", "Vargan Yard Station", "xlu_station01", "xlu");
        vargan_station.setCircularOrbitPointingDown(system.getEntityById("nemv2"), 45, 400, 120);
        
        MarketAPI varganMarket = addMarketplace("xlu", nemv2,
                new ArrayList<>(Arrays.asList(vargan_station)),
                "Vargan", 7, // 4 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.TERRAN,
                        Conditions.HABITABLE,
                        Conditions.RUINS_EXTENSIVE, 
                        Conditions.ORGANICS_COMMON,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.ORE_MODERATE,
                        Conditions.RARE_ORE_SPARSE,
                        Conditions.POPULATION_7)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(XLU_industries.XLU_STATION3, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(XLU_industries.XLU_YARDS, Items.PRISTINE_NANOFORGE, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.HIGHCOMMAND, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.FARMING)),
                        new ArrayList<>(Arrays.asList(Industries.REFINING, Commodities.GAMMA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.POPULATION, Commodities.GAMMA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.MEGAPORT)))),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN, Submarkets.GENERIC_MILITARY)),
                0.3f,
                false
        );

        CargoAPI cargo = varganMarket.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo();
        cargo.addHullmods("xlu_crew_hardyboys", 1);
        cargo.addHullmods("xlu_crew_warshots", 1);
        cargo.addHullmods("xlu_crew_waymakers", 1);
        cargo.addSpecial(new SpecialItemData("industry_bp", "xlu_battle_yards"), 1);
        cargo.addSpecial(new SpecialItemData("industry_bp", "xlu_orbitalstation"), 1);
//		SectorEntityToken vargan_station = system.addCustomEntity("vargan_station", "Drenor Space Hub", "xlu_station01", "xlu");
//		vargan_station.setCircularOrbitPointingDown(system.getEntityById("nemv2"), 0,  // angle
//                                                                                            750, // orbit radius
//                                                                                            120); // orbit days		
//		vargan_station.setInteractionImage("illustrations", "urban02");
                
		system.addRingBand(nemv2, "misc", "rings_asteroids0", 64f, 2, Color.blue, 64f, 600, 20f);
                
		PlanetAPI nemv2a = system.addPlanet("nemv2a", nemv2, "Kirogos", "desert", 0, 80, 1500, 30);
		
        MarketAPI kirogosMarket = addMarketplace("xlu", nemv2a,
                null,
                "Kirogos", 5, // 3 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.EXTREME_WEATHER,
                        Conditions.HOT,
                        Conditions.HABITABLE, 
                        Conditions.RUINS_WIDESPREAD,
                        Conditions.ORGANICS_TRACE,
                        Conditions.FARMLAND_POOR,
                        Conditions.ORE_MODERATE,
                        Conditions.RARE_ORE_SPARSE,
                        Conditions.POPULATION_5)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)),
                        new ArrayList<>(Arrays.asList(Industries.TECHMINING)),
                        new ArrayList<>(Arrays.asList(Industries.FARMING)),
                        new ArrayList<>(Arrays.asList(Industries.LIGHTINDUSTRY, Commodities.BETA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.MEGAPORT)))),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f,
                false
        );

		SectorEntityToken relay = system.addCustomEntity("nemvis_relay", "Nemvis Relay", "comm_relay", "xlu");
		relay.setCircularOrbitPointingDown(system.getEntityById("nemv2a"), 0,  // angle
                                                                                            300, // orbit radius
                                                                                            240); // orbit days		
                
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
		 
		
		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("vargan_gate", "Vargan Gate");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 15, 6200, 860);
		jumpPoint.setOrbit(orbit);
		jumpPoint.setRelatedPlanet(nemv2);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);
		
		PlanetAPI nemv3 = system.addPlanet("nemv3", star, "Tanfos", "ice_giant", 0, 700, 15000, 1360);
		PlanetAPI nemv3a = system.addPlanet("nemv3a", nemv3, "Tanfos I", "frozen", 0, 75, 1000, 60);
		PlanetAPI nemv3b = system.addPlanet("nemv3b", nemv3, "Tanfos II", "frozen", 0, 65, 1200, 80);
                
        MarketAPI tanfosbMarket = addMarketplace("independent", nemv3b,
                null,
                "Tanfos II", 4, // 2 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.COLD,
                        Conditions.POOR_LIGHT,
                        Conditions.NO_ATMOSPHERE, 
                        Conditions.VOLATILES_PLENTIFUL,
                        Conditions.ORE_SPARSE,
                        Conditions.RARE_ORE_RICH,
                        Conditions.POPULATION_4)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)),
                        new ArrayList<>(Arrays.asList(Industries.MINING)),
                        new ArrayList<>(Arrays.asList(Industries.FUELPROD, Items.SYNCHROTRON, Commodities.BETA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.PATROLHQ)))),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f,
                false
        );

		PlanetAPI nemv3c = system.addPlanet("nemv3c", nemv3, "Tanfos III", "frozen", 0, 50, 1400, 20);
		PlanetAPI nemv3d = system.addPlanet("nemv3d", nemv3, "Tanfos IV", "frozen", 0, 30, 1500, 100);
		PlanetAPI nemv3e = system.addPlanet("nemv3e", nemv3, "Tanfos V", "frozen", 0, 20, 1700, 120);
		
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
