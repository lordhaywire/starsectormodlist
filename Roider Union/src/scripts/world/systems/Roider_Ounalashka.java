package scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import java.awt.Color;

import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;
import static ids.Roider_Ids.DESC;
import ids.Roider_Ids.IdName;
import java.util.Random;

public class Roider_Ounalashka {
    // Stars
    public static final IdName PRIMARY = new IdName("roider_ounalashka", "Ounalashka"); // Red Dwarf
    // Jump points
    public static final IdName JP = new IdName("roider_ounalashka_jump", "Inner System Jump-point");
    // Markets
    public static final IdName MAGGIES = new IdName("roider_maggies", "Maggie's"); // pirate station
    public static final IdName AMAKNAK = new IdName("roider_amaknak", "Amaknak"); // dead barren-desert colony
    public static final IdName DUTCH_HARBOR = new IdName("roider_dutchHarbor", "Dutch Harbor"); // indie station
    // Other bodies
    public static final IdName OGLODAK = new IdName("roider_oglodak", "Oglodak"); // Gas Giant
    public static final IdName ROUND = new IdName("roider_oglodakI", "Round"); // Barren
    // Stable points
    public static final IdName SP1 = new IdName("roider_sp1", null); // Makeshift Comm Relay
    // Interesting locations
    public static final IdName GATE = new IdName("roider_unalaska_gate", "Ounalashka Gate");

    public static void generate(SectorAPI sector) {
        final StarSystemAPI system = sector.createStarSystem(PRIMARY.name);

        system.setBackgroundTextureFilename("graphics/backgrounds/background3.jpg");

        PlanetAPI star = system.initStar(PRIMARY.id,
                     StarTypes.RED_DWARF, // id in planets.json
                     200f,		// radius (in pixels at default zoom)
                     300); // corona radius, from star edge

            //Magnetic Ring
            SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                        new MagneticFieldParams(500f, // terrain effect band width
                                    850, // terrain effect middle radius
                                    star, // entity that it's around
                                    600f, // visual band start
                                    1100f, // visual band end
                                    new Color(50, 20, 100, 40), // base color
                                    1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                                    new Color(50, 20, 110, 130),
                                    new Color(150, 30, 120, 150),
                                    new Color(200, 50, 130, 190),
                                    new Color(250, 70, 150, 240),
                                    new Color(200, 80, 130, 255),
                                    new Color(75, 0, 160),
                                    new Color(127, 0, 255)
                                    ));
            field.setCircularOrbit(star, 0, 0, 150);


		StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, star.getSpec().getPlanetType(), false);
		Color min = starData.getLightColorMin();
		Color max = starData.getLightColorMax();
		Color lightColor = Misc.interpolateColor(min, max, new Random().nextFloat());
        system.setLightColor(lightColor); // light color in entire system, affects all entities


        createInnerSystem(system, star);
        createOglodalSystem(system, star);


        float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.YOUNG,
                    3, 5, // min/max entities to add
                    8260, // radius to start adding at
                    2, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                    true); // whether to use custom or system-name based names

		// Ounalashka Gate
		SectorEntityToken gate = system.addCustomEntity(GATE.id, // unique id
				 GATE.name, // name - if null, defaultName from custom_entities.json will be used
				 Entities.INACTIVE_GATE, // type of object, defined in custom_entities.json
				 null); // faction
		gate.setCircularOrbit(star, 20, radiusAfter + 500, 350);

		StarSystemGenerator.addSystemwideNebula(system, StarAge.AVERAGE);

        system.autogenerateHyperspaceJumpPoints(true, true);

        cleanup(system);
    }

    private static void createInnerSystem(StarSystemAPI system, PlanetAPI star) {
        // Maggie's Station
        SectorEntityToken pirateStation = system.addCustomEntity(MAGGIES.id,
                    MAGGIES.name, "station_side05", Factions.PIRATES);
        pirateStation.setCircularOrbitPointingDown(star, 176, 1700, 100);
        pirateStation.setInteractionImage("illustrations", "orbital_construction");
        pirateStation.setCustomDescriptionId(MAGGIES.id + DESC);
        pirateStation.addTag(Tags.STATION);

        // Rings of Ounalashka
        // <editor-fold defaultstate="collapsed">
        system.addAsteroidBelt(star, 100, 1100, 256, 100, 160, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, 1500, 256, 120, 180, Terrain.ASTEROID_BELT, null);

        system.addAsteroidBelt(star, 100, 1950, 128, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, 2250, 188, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, 2475, 256, 200, 300, Terrain.ASTEROID_BELT, null);


        system.addRingBand(star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 1000, 100f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 1200, 80f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 1400, 130f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 1600, 90f);

        // add one ring that covers all of the above
        SectorEntityToken ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(600 + 256, 1300, null, "Rings of Ounalashka"));
        ring.setCircularOrbit(star, 0, 0, 100);


        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 1800, 80f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 1900, 120f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 2000, 160f);

        // add one ring that covers all of the above
        ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(200 + 256, 1900, null, "Rings of Ounalashka"));
        ring.setCircularOrbit(star, 0, 0, 100);


        system.addRingBand(star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 2100, 140f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 2200, 180f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 2300, 220f);

        // add one ring that covers all of the above
        ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(200 + 256, 2200, null, "Rings of Ounalashka"));
        ring.setCircularOrbit(star, 0, 0, 100);


        system.addRingBand(star, "misc", "rings_ice0", 256f, 0, Color.white, 256f, 2300, 100f);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 2400, 140f);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 1, Color.white, 256f, 2500, 160f);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 2600, 180f);

        // add one ring that covers all of the above
        ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(300 + 256, 2450, null, "Rings of Ounalashka"));
        ring.setCircularOrbit(star, 0, 0, 100);
        // </editor-fold>


        PlanetAPI amaknak = system.addPlanet(AMAKNAK.id, star, AMAKNAK.name, "barren-desert", 180, 185, 3600, 90);
//        kiska1.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "asharu"));
//        kiska1.getSpec().setGlowColor(new Color(255,245,235,255));
//        kiska1.getSpec().setUseReverseLightForGlow(true);
//        kiska1.applySpecChanges();
//        kiska1.setInteractionImage("illustrations", "roider_cliff_landing");
        amaknak.setCustomDescriptionId(AMAKNAK.id + DESC);

        Misc.initConditionMarket(amaknak);
//        kiska1.getMarket().addCondition(Conditions.DECIVILIZED);
        amaknak.getMarket().addCondition(Conditions.RUINS_EXTENSIVE);
        amaknak.getMarket().getFirstCondition(Conditions.RUINS_EXTENSIVE).setSurveyed(true);
//        amaknak.getMarket().addCondition(Conditions.METEOR_IMPACTS);
        amaknak.getMarket().addCondition(Conditions.ORE_MODERATE);
        amaknak.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
        amaknak.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
        amaknak.getMarket().addCondition(Conditions.POLLUTION);
        amaknak.getMarket().addCondition(Conditions.HOT);

            SectorEntityToken indieStation = system.addCustomEntity(DUTCH_HARBOR.id,
                        DUTCH_HARBOR.name, "station_side06", Factions.INDEPENDENT);
            indieStation.setCircularOrbitPointingDown(amaknak, 176, amaknak.getRadius() + 150, 30);
//                        independentStation.setInteractionImage("illustrations", "urban02");
            indieStation.setCustomDescriptionId(DUTCH_HARBOR.id + DESC);
            indieStation.addTag(Tags.STATION);

            // Nav buoy at L5 of Amaknak
            SectorEntityToken sp1 = system.addCustomEntity(SP1.id, // unique id
                         SP1.name, // name - if null, defaultName from custom_entities.json will be used
                         Entities.COMM_RELAY_MAKESHIFT, // type of object, defined in custom_entities.json
                         Factions.INDEPENDENT); // faction
            sp1.setCircularOrbitPointingDown(star, amaknak.getCircularOrbitAngle() + 60,
                        amaknak.getCircularOrbitRadius(), amaknak.getCircularOrbitPeriod());
    }

    private static void createOglodalSystem(StarSystemAPI system, PlanetAPI star) {
        PlanetAPI oglodak = system.addPlanet(OGLODAK.id, star, OGLODAK.name, "gas_giant", -20, 250, 6120, 420);

            // Ice ring
            system.addRingBand(oglodak, "misc", "rings_ice0", 256f, 3, Color.white, 256f, 400, 22, Terrain.RING, null);

            // Oglodak magnetic field
            SectorEntityToken oglodak_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                        new MagneticFieldParams(250f, // terrain effect band width
                                    (oglodak.getRadius() + 550f), // terrain effect middle radius
                                    oglodak, // entity that it's around
                                    oglodak.getRadius() + 425f, // visual band start
                                    oglodak.getRadius() + 425f + 250f, // visual band end
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
            oglodak_field.setCircularOrbit(oglodak, 0, 0, 100);

            SectorEntityToken round = system.addPlanet(ROUND.id, oglodak, ROUND.name, "barren", 30, 70, 1025, 40);

            // Jump point at L4
            JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(JP.id, JP.name);
            jumpPoint.setCircularOrbit(star, oglodak.getCircularOrbitAngle() - 60,
                        oglodak.getCircularOrbitRadius(), oglodak.getCircularOrbitPeriod());
            jumpPoint.setStandardWormholeToHyperspaceVisual();
            system.addEntity(jumpPoint);

            // Trojans at L5
            SectorEntityToken oglodakL5 = system.addTerrain(Terrain.ASTEROID_FIELD,
                        new AsteroidFieldParams(
                                    900f, // min radius
                                    900f, // max radius
                                    20, // min asteroid count
                                    30, // max asteroid count
                                    4f, // min asteroid radius
                                    16f, // max asteroid radius
                                    "Oglodak L5 Asteroids")); // null for default name

            oglodakL5.setCircularOrbit(star, oglodak.getCircularOrbitAngle() + 60,
                        oglodak.getCircularOrbitRadius(), oglodak.getCircularOrbitPeriod());
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









