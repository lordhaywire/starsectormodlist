package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.skills.OfficerTraining;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.characters.PersonAPI;

import java.util.ArrayList;
import java.util.Arrays;

import static data.scripts.Gen.addMarketplace;


public class DE_Valhalla {

    public static String MIDGARD_ADMIN = "midgard_admin";

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.getStarSystem("Valhalla");

        /*
         * addPlanet() parameters:
         * 1. Unique id for this planet (or null to have it be autogenerated)
         * 2. What the planet orbits (orbit is always circular)
         * 3. Name
         * 4. Planet type id in planets.json
         * 5. Starting angle in orbit, i.e. 0 = to the right of the star
         * 6. Planet radius, pixels at default zoom
         * 7. Orbit radius, pixels at default zoom
         * 8. Days it takes to complete an orbit. 1 day = 10 seconds.
         */

        // Asgard L4 - Hegemony base to stare at Midgard
        SectorEntityToken asgard1 = system.addCustomEntity("asgard1", "L4 Asgard Orbital Complex", "station_lowtech3", "hegemony");
        asgard1.setCustomDescriptionId("station_asgard1");
        asgard1.setInteractionImage("illustrations", "urban01");
        asgard1.setCircularOrbitPointingDown(system.getEntityById("valhalla"), 45+60, 8000, 1000);
        MarketAPI asgard1_market = addMarketplace(
                "hegemony",
                asgard1,
                null,
                "L4 Asgard Orbital Complex",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.OUTPOST,
                                Conditions.INDUSTRIAL_POLITY,
                                Conditions.FRONTIER
                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.HEAVYBATTERIES,
                                Industries.BATTLESTATION,
                                Industries.WAYSTATION,
                                Industries.REFINING,
                                Industries.HEAVYINDUSTRY,
                                Industries.MILITARYBASE
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                true);

        // Asgard L5 - destroyed
        SectorEntityToken asgard2 = system.addCustomEntity("asgard2", "L5 Asgard Orbital Complex", "station_lowtech3", "neutral");
        asgard2.setCustomDescriptionId("station_asgard2");
        asgard2.setInteractionImage("illustrations", "abandoned_station2");
        asgard2.setCircularOrbitPointingDown(system.getEntityById("valhalla"), 45-60, 8000, 1000);

        Misc.setAbandonedStationMarket("asgard2_market", asgard2);

        // Midgard - the Third Star of Valhalla, independent fortress world
        PlanetAPI midgard = system.addPlanet("midgard", system.getEntityById("valhalla"), "Midgard", "terran", 45, 170, 8000, 1000);
        midgard.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
        midgard.applySpecChanges();
        midgard.setInteractionImage("illustrations", "urban02");
        midgard.setCustomDescriptionId("planet_midgard");
        midgard.getMarket().setImmigrationIncentivesOn(true);

        // Midgard's Station
        SectorEntityToken midgard2 = system.addCustomEntity("station_midgard", "Valhalla-Ragnar Logistical Hub", "station_lowtech2", "independent");

        MarketAPI midgard_market = addMarketplace(
                "independent",
                midgard,
                new ArrayList<>(
                        Arrays.asList(
                                midgard2
                        )
                ),
                "Midgard",
                7,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_7,
                                Conditions.HABITABLE,
                                Conditions.POOR_LIGHT,
                                Conditions.FARMLAND_RICH,
                                Conditions.ORGANICS_PLENTIFUL,
                                Conditions.ORE_MODERATE,
                                Conditions.RARE_ORE_MODERATE,
                                Conditions.INDUSTRIAL_POLITY,
                                Conditions.URBANIZED_POLITY,
                                Conditions.LARGE_REFUGEE_POPULATION,
                                Conditions.REGIONAL_CAPITAL
                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MEGAPORT,
                                Industries.HEAVYBATTERIES,
                                Industries.STARFORTRESS,
                                Industries.WAYSTATION,
                                Industries.MINING,
                                Industries.FARMING,
                                "commerce",
                                Industries.HIGHCOMMAND,
                                Industries.LIGHTINDUSTRY
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                true);
        for (SectorEntityToken linked : midgard_market.getConnectedEntities()) {
            if (!"midgard".equals(linked.getId())) {
                linked.setName("Valhalla-Ragnar Logistical Hub"); //Global.getSettings().getString("independent", "station_midgard")
                //linked.setInteractionImage("illustrations", "is_station_illustration");
                linked.setInteractionImage("illustrations", "urban03");
                linked.setCircularOrbitPointingDown(midgard, 45, 300, 1000);
                linked.setCustomDescriptionId("station_midgard");
            }
        }
        // Midgard superfleet
        FleetParamsV3 params = new FleetParamsV3(
                midgard.getMarket(), // add a source(has to be from a MarketAPI)
                null, // loc in hyper; don't need if have market
                "independent",
                2f, // quality override route.getQualityOverride()
                FleetTypes.TASK_FORCE,
                600f, // combatPts(minimal so special ships can be added)
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f// qualityMod
        );
        params.officerNumberMult = 2f;
        params.officerLevelBonus = 4;
        params.officerNumberBonus = 4;
        params.officerLevelLimit = Global.getSettings().getInt("officerMaxLevel") + (int) OfficerTraining.MAX_LEVEL_BONUS;
        params.modeOverride = FactionAPI.ShipPickMode.ALL;
        params.averageSMods = 1;
        params.flagshipVariantId = "paragon_Raider";
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) return;
        fleet.setFaction("independent", true);
        fleet.getFlagship().setId("paragon_Raider");
        fleet.setNoFactionInName(true);
        fleet.setName("Bifrost Armada");
        midgard.getContainingLocation().addEntity(fleet);
        fleet.setAI(Global.getFactory().createFleetAI(fleet));
        fleet.setMarket(midgard.getMarket());
        fleet.setLocation(midgard.getLocation().x, midgard.getLocation().y);
        fleet.setFacing((float) Math.random() * 360f);
        fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, midgard, (float) Math.random() * 90000f, null);

        // Valhalla-Ragnar Logistical Hub - use station_jangala.png?
        /*SectorEntityToken midgard2 = system.addCustomEntity("station_midgard", "Valhalla-Ragnar Logistical Hub", "station_lowtech2", "independent");
        midgard2.setCustomDescriptionId("station_midgard");
        midgard2.setInteractionImage("illustrations", "urban03");
        midgard2.setCircularOrbitPointingDown(midgard, 45, 300, 1000);*/
        //MarketAPI midgard2_market = midgard_market;

        }

}





