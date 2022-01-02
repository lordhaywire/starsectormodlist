package scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.*;
import com.fs.starfarer.api.impl.campaign.terrain.*;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import static ids.Roider_Ids.DESC;
import ids.Roider_Ids.IdName;
import ids.Roider_Ids.Roider_Factions;
import java.awt.Color;
import java.util.Random;

public class Roider_Kiska {
    // Stars
    public static final IdName PRIMARY = new IdName("roider_kiska", "Kiska"); // Young Nebula
    // Jump points
    public static final IdName JP_INNER = new IdName("roider_kiska_inner_jump", "Inner Jump-point");
    public static final IdName JP_OUTER = new IdName("roider_kiska_outer_jump", "Fringe Jump-point");
    // Markets
    public static final IdName DRUZHININS = new IdName("roider_druzhininsAnchorage", "Druzhinin's Anchorage");
    public static final IdName THREE_SISTERS_STATION = new IdName("roider_threeSisters", "Three Sisters Station"); // Roider station - orbits Loran
    public static final IdName ZIN_SHIPYARD = new IdName("roider_zinStation", "Zin Prototype Shipyard"); // TriTachyon station - way out near fringe jp
    // Stable points
    public static final IdName SP1 = new IdName("roider_sp1", null); // Makeshift Nav Buoy
    public static final IdName SP2 = new IdName("roider_sp2", null); // Makeshift Comm Relay
//    public static final String SP3 = "roider_sp3"; // Makeshift Sensor Buoy
    // Other bodies
    public static final IdName LORAN = new IdName("roider_loran", "Loran"); // Gas Giant
    public static final IdName GEE = new IdName("roider_gee", "Gee"); // Barren

    public static void generate(SectorAPI sector) {
        final StarSystemAPI system = sector.createStarSystem(PRIMARY.name);
        StarAge age = StarAge.YOUNG;

        PlanetAPI star = createNebula(system, age);

        createInnerSystem(system, star);
        float radiusAfter = createLoranSystem(system, star);

		radiusAfter = Roider_StarSystemGenerator.addOrbitingEntities(system, star, age,
				4, 5, // min/max entities to add
				radiusAfter + 1000, // radius to start adding at
				1, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
				true); // whether to use custom or system-name based names

        for (PlanetAPI planet : system.getPlanets()) {
            if (planet.isGasGiant()) {
                planet.setAutogenJumpPointNameInHyper(planet.getFullName() + " Gravity Well");
            }
        }

        // Fringe JP in asteroid field
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(JP_OUTER.id, JP_OUTER.name);
        jumpPoint.setCircularOrbit(star, 60, radiusAfter + 200, 580);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        SectorEntityToken asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
                    new AsteroidFieldParams(
                                200f, // min radius
                                200f, // max radius
                                10, // min asteroid count
                                20, // max asteroid count
                                4f, // min asteroid radius
                                12f, // max asteroid radius
                                null)); // null for default name

        asteroidField.setOrbit(jumpPoint.getOrbit());

        // Research Station
        SectorEntityToken tritachyonStation = system.addCustomEntity(ZIN_SHIPYARD.id,
                        ZIN_SHIPYARD.name, "station_side00", Factions.TRITACHYON);
        tritachyonStation.setCircularOrbitPointingDown(star, -120, radiusAfter + 800, 600);
        tritachyonStation.setCustomDescriptionId(ZIN_SHIPYARD.id + DESC);
        tritachyonStation.addTag(Tags.STATION);

        finalizeNebula(system, star, age, false);

        cleanup(system);
    }


    private static void createInnerSystem(StarSystemAPI system, PlanetAPI star) {
        // Inner Asteroid Fields
        // <editor-fold defaultstate="collapsed">
        SectorEntityToken asteroidField;
        // First Quarter
        asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
                    new AsteroidFieldParams(
                                4900f, // min radius
                                4900f, // max radius
                                220, // min asteroid count
                                330, // max asteroid count
                                4f, // min asteroid radius
                                16f, // max asteroid radius
                                null)); // null for default name

//        asteroidField.setCircularOrbit(star, 14, 3800, 250);

//        asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
//                    new AsteroidFieldParams(
//                                1000f, // min radius
//                                1000f, // max radius
//                                20, // min asteroid count
//                                30, // max asteroid count
//                                4f, // min asteroid radius
//                                16f, // max asteroid radius
//                                null)); // null for default name
//
//        asteroidField.setCircularOrbit(star, 46, 4150, 250);
//
//        asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
//                    new AsteroidFieldParams(
//                                1000f, // min radius
//                                1000f, // max radius
//                                20, // min asteroid count
//                                30, // max asteroid count
//                                4f, // min asteroid radius
//                                16f, // max asteroid radius
//                                null)); // null for default name
//
//        asteroidField.setCircularOrbit(star, 80, 4200, 250);
//
//        // Second Quarter
//        asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
//                    new AsteroidFieldParams(
//                                1000f, // min radius
//                                1000f, // max radius
//                                20, // min asteroid count
//                                30, // max asteroid count
//                                4f, // min asteroid radius
//                                16f, // max asteroid radius
//                                null)); // null for default name
//
//        asteroidField.setCircularOrbit(star, 114, 3950, 250);
//
//        asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
//                    new AsteroidFieldParams(
//                                1000f, // min radius
//                                1000f, // max radius
//                                20, // min asteroid count
//                                30, // max asteroid count
//                                4f, // min asteroid radius
//                                16f, // max asteroid radius
//                                null)); // null for default name
//
//        asteroidField.setCircularOrbit(star, 160, 4175, 250);
//
//        // Third Quarter
//        asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
//                    new AsteroidFieldParams(
//                                1000f, // min radius
//                                1000f, // max radius
//                                20, // min asteroid count
//                                30, // max asteroid count
//                                4f, // min asteroid radius
//                                16f, // max asteroid radius
//                                null)); // null for default name
//
//        asteroidField.setCircularOrbit(star, 189, 4050, 250);
//
//        asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
//                    new AsteroidFieldParams(
//                                1000f, // min radius
//                                1000f, // max radius
//                                20, // min asteroid count
//                                30, // max asteroid count
//                                4f, // min asteroid radius
//                                16f, // max asteroid radius
//                                null)); // null for default name
//
//        asteroidField.setCircularOrbit(star, 222, 3825, 250);
//
//        asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
//                    new AsteroidFieldParams(
//                                1000f, // min radius
//                                1000f, // max radius
//                                20, // min asteroid count
//                                30, // max asteroid count
//                                4f, // min asteroid radius
//                                16f, // max asteroid radius
//                                null)); // null for default name
//
//        asteroidField.setCircularOrbit(star, 255, 4000, 250);
//
//        // Fourth Quarter
//        asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
//                    new AsteroidFieldParams(
//                                1000f, // min radius
//                                1000f, // max radius
//                                20, // min asteroid count
//                                30, // max asteroid count
//                                4f, // min asteroid radius
//                                16f, // max asteroid radius
//                                null)); // null for default name
//
//        asteroidField.setCircularOrbit(star, 283, 4100, 250);
//
//        asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
//                    new AsteroidFieldParams(
//                                1000f, // min radius
//                                1000f, // max radius
//                                20, // min asteroid count
//                                30, // max asteroid count
//                                4f, // min asteroid radius
//                                16f, // max asteroid radius
//                                null)); // null for default name
//
//        asteroidField.setCircularOrbit(star, 316, 3800, 250);
//
//        asteroidField = system.addTerrain(Terrain.ASTEROID_FIELD,
//                    new AsteroidFieldParams(
//                                1000f, // min radius
//                                1000f, // max radius
//                                20, // min asteroid count
//                                30, // max asteroid count
//                                4f, // min asteroid radius
//                                16f, // max asteroid radius
//                                null)); // null for default name
//
//        asteroidField.setCircularOrbit(star, 350, 4200, 250);
        // </editor-fold>

        // Jump Point
        // Was in Second Quarter gap
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(JP_INNER.id, JP_INNER.name);
        jumpPoint.setCircularOrbit(star, 137, 4000, 250);

        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        // Druzhinin's Belt
        system.addAsteroidBelt(star, 90, 5250, 500, 150, 320, Terrain.ASTEROID_BELT,  "Druzhinin's Belt");
        system.addRingBand(star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 5200, 305f, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 3, Color.white, 256f, 5320, 295f, null, null);

            SectorEntityToken anchorage = system.addCustomEntity(DRUZHININS.id,
                        DRUZHININS.name, "station_midline2", Roider_Factions.ROIDER_UNION);

            anchorage.setCircularOrbitPointingDown(star, 180, 5600, 300);
            anchorage.setCustomDescriptionId(DRUZHININS.id + DESC);
            anchorage.setInteractionImage("illustrations", "orbital");
            anchorage.addTag(Tags.STATION);

            // FTL relay at "L4" of Station
            SectorEntityToken relay = system.addCustomEntity(SP1.id, // unique id
                        SP1.name, // name - if null, defaultName from custom_entities.json will be used
                        Entities.NAV_BUOY_MAKESHIFT, // type of object, defined in custom_entities.json
                        Roider_Factions.ROIDER_UNION); // faction
            relay.setCircularOrbitPointingDown(star, anchorage.getCircularOrbitAngle() - 60,
                        anchorage.getCircularOrbitRadius(), anchorage.getCircularOrbitPeriod());
    }

    private static float createLoranSystem(StarSystemAPI system, PlanetAPI star) {
        // Gas Giant
        SectorEntityToken loran = system.addPlanet(LORAN.id, star, LORAN.name, "gas_giant", -50, 325, 7100, 150);
        loran.setAutogenJumpPointNameInHyper(system.getBaseName() + ", " + loran.getName() + " Gravity Well");

            SectorEntityToken gee = system.addPlanet(GEE.id, loran, GEE.name, "barren", 30, 80, 700, 35);

            system.addAsteroidBelt(loran, 30, 900, 128, 20, 40, Terrain.ASTEROID_BELT,  null);

            // Three Sisters Station in asteroid belt
            SectorEntityToken threeSisters = system.addCustomEntity(THREE_SISTERS_STATION.id,
                            THREE_SISTERS_STATION.name, "station_mining00", Factions.INDEPENDENT);
            threeSisters.setCircularOrbitPointingDown(loran, 250, 900, 40);
            threeSisters.setInteractionImage("illustrations", "hound_hangar");
            threeSisters.setCustomDescriptionId(THREE_SISTERS_STATION.id + DESC);
            threeSisters.addTag(Tags.STATION);

            // FTL relay at L5 of Loran
            SectorEntityToken sp2 = system.addCustomEntity(SP2.id, // unique id
                             SP2.name, // name - if null, defaultName from custom_entities.json will be used
                             Entities.COMM_RELAY_MAKESHIFT, // type of object, defined in custom_entities.json
                             Roider_Factions.ROIDER_UNION); // faction
            sp2.setCircularOrbitPointingDown(star, loran.getCircularOrbitAngle() + 60,
                        loran.getCircularOrbitRadius(), loran.getCircularOrbitPeriod());

            return loran.getCircularOrbitRadius() + 1100;
    }

    public static PlanetAPI createNebula(StarSystemAPI system, StarAge age) {
        if (age == StarAge.ANY) age = StarAge.AVERAGE;

        system.setType(StarSystemGenerator.StarSystemType.NEBULA);
        system.setBaseName(system.getBaseName());

        String starTypeId = "nebula_center_average";
        if (age == StarAge.OLD) starTypeId = "nebula_center_old";
        if (age == StarAge.YOUNG) starTypeId = "nebula_center_young";

        PlanetAPI star = system.initStar(PRIMARY.id, starTypeId, 0, 0);

        org.apache.log4j.Logger logger = Global.getLogger(Roider_Kiska.class);
        logger.info("creating " + system.getLocation());

        star.setSkipForJumpPointAutoGen(true);

        star.addTag(Tags.AMBIENT_LS);

        // Set background
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>();
        if (age == StarAge.OLD) {
            picker.add("graphics/backgrounds/background6.jpg", 10);
        } else if (age == StarAge.YOUNG) {
            picker.add("graphics/backgrounds/background5.jpg", 10);
        } else {
            picker.add("graphics/backgrounds/background1.jpg", 10);
            picker.add("graphics/backgrounds/background2.jpg", 10);
        }

        system.setBackgroundTextureFilename(picker.pick());

		StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, star.getSpec().getPlanetType(), false);
		Color min = starData.getLightColorMin();
		Color max = starData.getLightColorMax();
		Color lightColor = Misc.interpolateColor(min, max, new Random().nextFloat());

        system.setLightColor(lightColor); // light color in entire system, affects all entities

        return star;
    }

    public static void finalizeNebula(StarSystemAPI system, PlanetAPI center, StarAge age, boolean fringeJP) {
        Misc.generatePlanetConditions(system, age);
        org.apache.log4j.Logger logger = Global.getLogger(Roider_Kiska.class);
        logger.info("finalizing " + system.getLocation());

        // Remove star
        system.removeEntity(center);
        StarCoronaTerrainPlugin coronaPlugin = Misc.getCoronaFor(center);
        if (coronaPlugin != null) {
            system.removeEntity(coronaPlugin.getEntity());
        }
        system.setStar(null);

        SectorEntityToken systemCenter = system.initNonStarCenter();
        for (SectorEntityToken entity : system.getAllEntities()) {
            if (entity.getOrbitFocus() == center ||
                entity.getOrbitFocus() == system.getCenter()) {
                entity.setOrbit(null);
            }
        }
        system.setCenter(systemCenter);

        system.autogenerateHyperspaceJumpPoints(true, fringeJP);

        system.setStar(center);

		Roider_StarSystemGenerator.addSystemwideNebula(system, age);

//        Global.getSector().getEconomy().
    }


    // cleanup method by Tartiflette
    static void cleanup(StarSystemAPI system){
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }
}