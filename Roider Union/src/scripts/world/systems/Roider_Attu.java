package scripts.world.systems;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;
import static ids.Roider_Ids.DESC;
import ids.Roider_Ids.IdName;

public class Roider_Attu {
    // Stars
    public static final IdName PRIMARY = new IdName("roider_attu", "Attu"); // Orange star
    public static final IdName SECONDARY = new IdName("roider_agattu", "Agattu"); // Brown Dwarf
    // Jump points
    public static final IdName JP_INNER = new IdName("roider_attu_inner_jump", "Inner System Jump-point");
    public static final IdName JP_OUTER = new IdName("roider_attu_outer_jump", "Outer System Jump-point");
    // Markets
    public static final IdName MINING_STATION = new IdName("roider_attuStation", "Holtz Refining Complex"); // Hegemony station w/ Union HQ
    // Other bodies
    public static final IdName MOFFET = new IdName("roider_attuI", "Moffet"); // Calling it Moffet
    // Stable points
    public static final IdName SP1 = new IdName("roider_sp1", null); // Makeshift Comm Relay

    public static void generate(SectorAPI sector) {
        final StarSystemAPI system = sector.createStarSystem(PRIMARY.name);
        system.setType(StarSystemGenerator.StarSystemType.BINARY_CLOSE);

        system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");

        PlanetAPI star = system.initStar(PRIMARY.id,
                    StarTypes.ORANGE,
                    650f,
                    500, // extent of corona outside star
                    12f, // solar wind burn level
                    1f, // flare probability
                    3f); // CR loss multiplier, good values are in the range of 1-5

        system.setLightColor(new Color(255,135,40));

        createInnerSystem(system, star);
        createAsteroidFields(system, star);
        createOuterSystem(system, star);

        system.autogenerateHyperspaceJumpPoints(true, true);

        cleanup(system);
    }

    private static void createInnerSystem(StarSystemAPI system, PlanetAPI star) {
        // Planet for Holtz Union HQ to mine
        PlanetAPI attu1 = system.addPlanet(MOFFET.id, star,
                    MOFFET.name, "lava", 55, 150, 1820, 80);

            JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(JP_INNER.id, "Inner System Jump-point");
            OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, attu1.getCircularOrbitAngle() + 60,
                        attu1.getCircularOrbitRadius(), attu1.getCircularOrbitPeriod());
            jumpPoint.setOrbit(orbit);
            jumpPoint.setRelatedPlanet(attu1);
            jumpPoint.setStandardWormholeToHyperspaceVisual();
            system.addEntity(jumpPoint);


        // Asteroid Belt 1
        int belt1Radius = 2750;
        int belt1Days = 100;
        system.addAsteroidBelt(star, 90, belt1Radius, 500, 80, 140, Terrain.ASTEROID_BELT,  null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white,
                    256f, belt1Radius - 50, belt1Days + 10, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 2, Color.white,
                    256f, belt1Radius + 50, belt1Days, null, null);

        // Asteroid Belt 2
        int belt2Radius = 3400;
        int belt2Days = 125;
        system.addAsteroidBelt(star, 90, belt2Radius, 500, 180, 220f, Terrain.ASTEROID_BELT,  null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white,
                    256f, belt2Radius - 100, belt2Days + 5, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white,
                    256f, belt2Radius + 20, belt2Days - 5, null, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white,
                    256f, belt2Radius + 100, belt2Days + 5, null, null);

            SectorEntityToken holtz = system.addCustomEntity(MINING_STATION.id,
                        MINING_STATION.name, "station_mining00", Factions.HEGEMONY);
            holtz.setCircularOrbitPointingDown(star, 180, belt2Radius, belt2Days);
            holtz.setCustomDescriptionId(MINING_STATION.id + DESC);
            holtz.setInteractionImage("illustrations", "space_wreckage");
            holtz.addTag(Tags.STATION);

    }

    private static void createAsteroidFields(StarSystemAPI system, PlanetAPI star) {
        // Inner ring of asteroid fields
        float radius = 4810;
        float period = 146;

        SectorEntityToken field = system.addTerrain(Terrain.ASTEROID_FIELD,
                    new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                                700f, // min radius
                                800f, // max radius
                                30, // min asteroid count
                                40, // max asteroid count
                                4f, // min asteroid radius
                                16f, // max asteroid radius
                                null)); // null for default name
        field.setCircularOrbit(star, 180 + 60, radius, 146);

            field = system.addTerrain(Terrain.ASTEROID_FIELD,
                        new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                                    700f, // min radius
                                    800f, // max radius
                                    30, // min asteroid count
                                    40, // max asteroid count
                                    4f, // min asteroid radius
                                    16f, // max asteroid radius
                                    null)); // null for default name
            field.setCircularOrbit(star, 60 - 20, radius, period);

            field = system.addTerrain(Terrain.ASTEROID_FIELD,
                        new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                                    700f, // min radius
                                    800f, // max radius
                                    30, // min asteroid count
                                    40, // max asteroid count
                                    4f, // min asteroid radius
                                    16f, // max asteroid radius
                                    null)); // null for default name
            field.setCircularOrbit(star, 60 + 20, radius, period);

        // Outer ring of asteroid fields
        radius = 7020;
        period = 202;

        field = system.addTerrain(Terrain.ASTEROID_FIELD,
                    new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                                700f, // min radius
                                800f, // max radius
                                30, // min asteroid count
                                40, // max asteroid count
                                4f, // min asteroid radius
                                16f, // max asteroid radius
                                null)); // null for default name
        field.setCircularOrbit(star, -10, radius, period);

            field = system.addTerrain(Terrain.ASTEROID_FIELD,
                        new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                                    700f, // min radius
                                    800f, // max radius
                                    30, // min asteroid count
                                    40, // max asteroid count
                                    4f, // min asteroid radius
                                    16f, // max asteroid radius
                                    null)); // null for default name
            field.setCircularOrbit(star, 80, radius, period);

            field = system.addTerrain(Terrain.ASTEROID_FIELD,
                        new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                                    700f, // min radius
                                    800f, // max radius
                                    30, // min asteroid count
                                    40, // max asteroid count
                                    4f, // min asteroid radius
                                    16f, // max asteroid radius
                                    null)); // null for default name
            field.setCircularOrbit(star, 130, radius, period);

            field = system.addTerrain(Terrain.ASTEROID_FIELD,
                        new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                                    700f, // min radius
                                    800f, // max radius
                                    30, // min asteroid count
                                    40, // max asteroid count
                                    4f, // min asteroid radius
                                    16f, // max asteroid radius
                                    null)); // null for default name
            field.setCircularOrbit(star, 200, radius, period);

            // Comm relay
            SectorEntityToken relay = system.addCustomEntity(SP1.id, // unique id
                        SP1.name, // name - if null, defaultName from custom_entities.json will be used
                        Entities.COMM_RELAY_MAKESHIFT, // type of object, defined in custom_entities.json
                        Factions.HEGEMONY); // faction
            relay.setCircularOrbitPointingDown(star, -80, radius, period);
    }

    private static void createOuterSystem(StarSystemAPI system, PlanetAPI star) {
        // Agattu - secondary star
        PlanetAPI agattu = system.addPlanet(SECONDARY.id, star, SECONDARY.name,
                    StarTypes.BROWN_DWARF, 65, 350, 10600, 600);
//        system.setSecondary(agattu);
        system.addCorona(agattu, 150, 2f, 0f, 1f);
        system.setSecondary(agattu);

            // Agattu magnetic field
            float width = 500;
            float radius = agattu.getRadius() * 3;
            SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                        new MagneticFieldParams(
                                    width, // terrain effect band width
                                    radius, // terrain effect middle radius
                                    agattu, // entity that it's around
                                    radius - width / 2, // visual band start
                                    radius + width / 2, // visual band end
                                    new Color(50, 20, 100, 40), // base color
                                    1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                                    new Color(50, 20, 110, 130),
                                    new Color(150, 30, 120, 150),
                                    new Color(200, 50, 130, 190),
                                    new Color(250, 70, 150, 240),
                                    new Color(200, 80, 130, 255),
                                    new Color(75, 0, 160),
                                    new Color(127, 0, 255) ));
            field.setCircularOrbit(agattu, 0, 0, 150);

            JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(JP_OUTER.id, JP_OUTER.name);
            OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, agattu.getCircularOrbitAngle() + 60,
                        agattu.getCircularOrbitRadius(), agattu.getCircularOrbitPeriod());
            jumpPoint.setOrbit(orbit);
            jumpPoint.setStandardWormholeToHyperspaceVisual();
            system.addEntity(jumpPoint);

        // Asteroid belt
        int belt1Radius = 1750;
        int belt1Days = 60;
        system.addAsteroidBelt(agattu, 90, belt1Radius, 500, 80, 140, Terrain.ASTEROID_BELT,  null);
        system.addRingBand(agattu, "misc", "rings_dust0", 256f, 2, Color.white,
                    256f, belt1Radius - 50, belt1Days + 10, null, null);
        system.addRingBand(agattu, "misc", "rings_asteroids0", 256f, 1, Color.white,
                    256f, belt1Radius + 50, belt1Days, null, null);
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