package scripts.world.systems;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;
import static ids.Roider_Ids.DESC;
import ids.Roider_Ids.*;

public class Roider_Atka {
    // Summary of system's ids
    // Stars
    public static final IdName PRIMARY = new IdName("roider_atka_solo", "Atka"); // Red Dwarf
    // Jump points
    public static final IdName JP_INNER = new IdName("roider_atka_inner_jump", "Inner System Jump-point");
    public static final IdName JP_OUTER = new IdName("roider_atka_outer_jump", "Outer System Jump-point");
    public static final IdName JP_FRINGE = new IdName("roider_atka_fringe_jump", "Fringe Jump-point");
    // Markets
    public static final IdName KOROVIN = new IdName("roider_korovin", "Korovin"); // volcanic minor world - roider
    public static final IdName ROCKPIPER_PERCH = new IdName("roider_rockpiperPerch", "Rockpiper Perch"); // roider station
    public static final IdName COLD_ROCK = new IdName("roider_coldRock", "Cold Rock"); // Frozen
    public static final IdName COLD_ROCK_BASTION = new IdName("roider_coldRock_bastion", "Cold Rock Bastion"); // pirate station
    // Other bodies
    public static final IdName AKUTAN = new IdName("roider_unalaskaI", "Akutan"); // Hot giant
    public static final IdName KALEKHTA = new IdName("roider_kalekhta", "Kalekhta"); // Ice Giant
    public static final IdName PRIEST = new IdName("roider_priest", "Priest"); // Barren
    public static final IdName MAKUSHIN = new IdName("roider_makushin", "Makushin"); // Cryovolcanic
    // Stable points
    public static final IdName SP1 = new IdName("roider_sp1", null); // Makeshift Comm Relay
    public static final IdName SP2 = new IdName("roider_sp2", null); // Makeshift Nav Buoy

    public static void generate(SectorAPI sector) {
        final StarSystemAPI system = sector.createStarSystem(PRIMARY.name);

        system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");

//        PlanetAPI star = system.initStar("roider_atka_solo",
//                    "star_yellow",
//                    750f,
//                    500, // extent of corona outside star
//                    12f, // solar wind burn level
//                    1f, // flare probability
//                    3f); // CR loss multiplier, good values are in the range of 1-5

        PlanetAPI star = system.initStar(PRIMARY.id,
                     StarTypes.RED_DWARF, // id in planets.json
                     400f,		// radius (in pixels at default zoom)
                     200); // corona radius, from star edge
		star.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "banded"));
		star.getSpec().setGlowColor(new Color(255,235,50,128));
		star.getSpec().setAtmosphereThickness(0.5f);
		star.applySpecChanges();

            //Magnetic Ring
            SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                        new MagneticFieldParams(500f, // terrain effect band width
                                    850, // terrain effect middle radius
                                    star, // entity that it's around
                                    700f, // visual band start
                                    1200f, // visual band end
                                    new Color(50, 20, 100, 40), // base color
                                    0f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                                    new Color(50, 20, 110, 130),
                                    new Color(150, 30, 120, 150),
                                    new Color(200, 50, 130, 190),
                                    new Color(250, 70, 150, 240),
                                    new Color(200, 80, 130, 255),
                                    new Color(75, 0, 160),
                                    new Color(127, 0, 255)
                                    ));
            field.setCircularOrbit(star, 0, 0, 150);


		system.setLightColor(new Color(200, 180, 160)); // light color in entire system, affects all entities

        createInnerSystem(system, star);
        createKalekhtaSystem(system, star);
        createOuterSystem(system, star);

//		StarSystemGenerator.addSystemwideNebula(system, StarAge.YOUNG);

        system.autogenerateHyperspaceJumpPoints(true, false);

        cleanup(system);
    }

    private static void createInnerSystem(StarSystemAPI system, PlanetAPI star) {

        // Hot giant
        PlanetAPI akutan = system.addPlanet(AKUTAN.id, star, AKUTAN.name, "gas_giant", 10, 240, 1550, 30);
		akutan.getSpec().setPlanetColor(new Color(245,38,8,255));
		akutan.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "banded"));
		akutan.getSpec().setGlowColor(new Color(235,38,8,145));
		akutan.getSpec().setUseReverseLightForGlow(true);
		akutan.getSpec().setAtmosphereThickness(0.5f);
		akutan.getSpec().setCloudRotation( 15f );
		akutan.getSpec().setAtmosphereColor(new Color(238,18,55,245));
		akutan.getSpec().setPitch( -5f );
		akutan.getSpec().setTilt( 40f );
		akutan.applySpecChanges();
		akutan.setCustomDescriptionId(AKUTAN.id + DESC);

//		system.addCorona(unalaska1, Terrain.CORONA_AKA_MAINYU,
//						300f, // radius outside planet
//						5f, // burn level of "wind"
//						0f, // flare probability
//						1f // CR loss mult while in it
//						);

            // Hot giant jumppoint - L5 (behind)
            JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(JP_INNER.id, JP_INNER.name);
            jumpPoint.setCircularOrbit(star, akutan.getCircularOrbitAngle() + 60,
                        akutan.getCircularOrbitRadius(), akutan.getCircularOrbitPeriod());
//            jumpPoint.setRelatedPlanet(akutan);

            jumpPoint.setStandardWormholeToHyperspaceVisual();
            system.addEntity(jumpPoint);

            SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                    new MagneticFieldParams(akutan.getRadius() + 200f, // terrain effect band width
                            (akutan.getRadius() + 200f) / 2f, // terrain effect middle radius
                            akutan, // entity that it's around
                            akutan.getRadius() + 50f, // visual band start
                            akutan.getRadius() + 50f + 250f, // visual band end
                            new Color(50, 20, 100, 40), // base color
                            1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                            new Color(140, 100, 235),
                            new Color(180, 110, 210),
                            new Color(150, 140, 190),
                            new Color(140, 190, 210),
                            new Color(90, 200, 170),
                            new Color(65, 230, 160),
                            new Color(20, 220, 70)
                    ));
            field.setCircularOrbit(akutan, 0, 0, 100);

        // Korovin
        PlanetAPI korovin = system.addPlanet(KOROVIN.id, star, KOROVIN.name, "lava_minor", 55, 200, 3700, 180);
        korovin.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "asharu"));
        korovin.getSpec().setGlowColor(new Color(255,255,255,255));
        korovin.getSpec().setUseReverseLightForGlow(true);
        korovin.applySpecChanges();
        korovin.setCustomDescriptionId(KOROVIN.id + DESC);
        korovin.setInteractionImage("illustrations", "industrial_megafacility");

            SectorEntityToken roiderStation = system.addCustomEntity(ROCKPIPER_PERCH.id,
                        ROCKPIPER_PERCH.name, "roider_station_rockpiper", Roider_Factions.ROIDER_UNION);

            roiderStation.setCircularOrbit(korovin, 145, 360, Float.MAX_VALUE);
//            roiderStation.setCircularOrbitPointingDown(korovin, 45 + 120, 380, 28);
            roiderStation.setCustomDescriptionId(ROCKPIPER_PERCH.id + DESC);
//			roiderStation.setInteractionImage("illustrations", "quartermaster");
//			roiderStation.setInteractionImage("illustrations", "comm_relay");
//            roiderStation.addTag(Roider_StationMaster.ROIDER_STATION_TAG);

            // FTL relay at L4 of Korovin
            SectorEntityToken relay = system.addCustomEntity(SP1.id, // unique id
                        SP1.name, // name - if null, defaultName from custom_entities.json will be used
                        Entities.COMM_RELAY_MAKESHIFT, // type of object, defined in custom_entities.json
                        Roider_Factions.ROIDER_UNION); // faction
            relay.setCircularOrbitPointingDown(star, korovin.getCircularOrbitAngle() - 60,
                        korovin.getCircularOrbitRadius(), korovin.getCircularOrbitPeriod());

            // Korovin trojans
            SectorEntityToken korovin_L5 = system.addTerrain(Terrain.ASTEROID_FIELD,
                        new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                                    400f, // min radius
                                    600f, // max radius
                                    25, // min asteroid count
                                    30, // max asteroid count
                                    4f, // min asteroid radius
                                    16f, // max asteroid radius
                                    KOROVIN.name + " L5 Asteroids")); // null for default name
            korovin_L5.setCircularOrbit(star, korovin.getCircularOrbitAngle() + 60f,
                        korovin.getCircularOrbitRadius(), korovin.getCircularOrbitPeriod());

    }

    private static void createKalekhtaSystem(StarSystemAPI system, PlanetAPI star) {
        // Asteroid belts between Korovin and Kalekhta
        float baseRingRadius = 5000;
        system.addAsteroidBelt(star, 100, baseRingRadius + 150, 128, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, baseRingRadius + 450, 188, 200, 300, Terrain.ASTEROID_BELT, null);

        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, baseRingRadius, 80f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, baseRingRadius + 100, 120f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, baseRingRadius + 200, 160f);

        // add one ring that covers all of the above
        SectorEntityToken ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(200 + 256, baseRingRadius + 100, null, "Point Cheerful"));
        ring.setCircularOrbit(star, 0, 0, 100);


        system.addRingBand(star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, baseRingRadius + 300, 140f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, baseRingRadius + 400, 180f);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 2, Color.white, 256f, baseRingRadius + 500, 220f);

        // add one ring that covers all of the above
        ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(200 + 256, baseRingRadius + 400, null, "Point Cheerful"));
        ring.setCircularOrbit(star, 0, 0, 100);


        PlanetAPI kalekhta = system.addPlanet(KALEKHTA.id, star, KALEKHTA.name, "ice_giant", 130, 290, 8000, 400);
        kalekhta.getSpec().setPlanetColor(new Color(255,210,170,255));
        kalekhta.getSpec().setPitch(20f);
        kalekhta.getSpec().setTilt(10f);
        kalekhta.applySpecChanges();
        kalekhta.setAutogenJumpPointNameInHyper(system.getBaseName() + ", " + kalekhta.getName() + " Gravity Well");
            SectorEntityToken priest = system.addPlanet(PRIEST.id, kalekhta, PRIEST.name, "barren", 30, 30, kalekhta.getRadius() + 100, 30);

            system.addRingBand(kalekhta, "misc", "rings_asteroids0", 256f, 3, new Color(170,210,255,255), 256f, 800, 40f);
            system.addAsteroidBelt(kalekhta, 50, 800, 200, 40, 80, Terrain.ASTEROID_BELT, null);

            // Kalekhta trojans
            SectorEntityToken atka3_L4 = system.addTerrain(Terrain.ASTEROID_FIELD,
                        new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                                    500f, // min radius
                                    700f, // max radius
                                    30, // min asteroid count
                                    40, // max asteroid count
                                    4f, // min asteroid radius
                                    16f, // max asteroid radius
                                    KALEKHTA.name + " L4 Asteroids")); // null for default name

            atka3_L4.setCircularOrbit(star, kalekhta.getCircularOrbitAngle() - 60f,
                        kalekhta.getCircularOrbitRadius(), kalekhta.getCircularOrbitPeriod());

            // Volcanic planet at L5
            SectorEntityToken makushin = system.addPlanet(MAKUSHIN.id, star, MAKUSHIN.name,
                        "cryovolcanic", kalekhta.getCircularOrbitAngle() + 60f, 80,
                        kalekhta.getCircularOrbitRadius(), kalekhta.getCircularOrbitPeriod());

            // Outer jump point at L4 of Kalekhta
            JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(JP_OUTER.id, JP_OUTER.name);
            jumpPoint.setCircularOrbit(star, kalekhta.getCircularOrbitAngle() - 60f,
                        kalekhta.getCircularOrbitRadius(), kalekhta.getCircularOrbitPeriod());
            jumpPoint.setStandardWormholeToHyperspaceVisual();
            system.addEntity(jumpPoint);
    }

    private static void createOuterSystem(StarSystemAPI system, PlanetAPI star) {

        // Spinning System
        // <editor-fold defaultstate="collapsed">
        SectorEntityToken orbitFocus = system.createToken(1, 1);
        system.addEntity(orbitFocus);
        orbitFocus.setCircularOrbit(star, 45f, 750f, 80f);

        // Pirate nav buoy
        SectorEntityToken relay = system.addCustomEntity(SP2.id, // unique id
                    SP2.name, // name - if null, defaultName from custom_entities.json will be used
                    Entities.NAV_BUOY_MAKESHIFT, // type of object, defined in custom_entities.json
                    Factions.PIRATES); // faction
        relay.setCircularOrbitPointingDown(orbitFocus, -120, 10000, 420);


        system.addAsteroidBelt(orbitFocus, 200, 11130 - 250, 256, 500, 560, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(orbitFocus, 200, 11820 - 250, 256, 520, 580, Terrain.ASTEROID_BELT, null);

        system.addAsteroidBelt(orbitFocus, 300, 12150, 256, 580, 640, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(orbitFocus, 300, 12450, 188, 600, 680, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(orbitFocus, 300, 12750, 128, 640, 700, Terrain.ASTEROID_BELT, null);

        // Fancy Rings
        system.addRingBand(orbitFocus, "misc", "rings_ice0", 256f, 1, Color.white, 256f, 11000 - 250, 300f);
        system.addRingBand(orbitFocus, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 11100 - 250, 530f);
        system.addRingBand(orbitFocus, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 11200 - 250, 320f);
        system.addRingBand(orbitFocus, "misc", "rings_ice0", 256f, 0, Color.white, 256f, 11260 - 250, 480f);

        // add one ring that covers all of the above
        SectorEntityToken ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(220 + 256, 11130 - 250, null, "Outer Rim"));
        ring.setCircularOrbit(orbitFocus, 0, 0, 100);


        system.addRingBand(orbitFocus, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 11300 - 250, 340f);
        system.addRingBand(orbitFocus, "misc", "rings_ice0", 256f, 3, Color.white, 256f, 11393 - 250, 420f);
        system.addRingBand(orbitFocus, "misc", "rings_ice0", 256f, 3, Color.white, 256f, 11543 - 250, 420f);

        // add one ring that covers all of the above
        ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(150 + 256, 11470 - 250, null, "Outer Rim"));
        ring.setCircularOrbit(orbitFocus, 0, 0, 100);


        system.addRingBand(orbitFocus, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 11763 - 250, 560f);
        system.addRingBand(orbitFocus, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 11863 - 250, 620f);
        system.addRingBand(orbitFocus, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 11993 - 250, 380f);
        system.addRingBand(orbitFocus, "misc", "rings_asteroids0", 256f, 3, Color.white, 256f, 12093 - 250, 780f);
        system.addRingBand(orbitFocus, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 12188 - 250, 400f);

        // add one ring that covers all of the above
        ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(200 + 256, 11900 - 250, null, "Outer Rim"));
        ring.setCircularOrbit(orbitFocus, 0, 0, 100);


        system.addRingBand(orbitFocus, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 12345 - 250, 430f);
        system.addRingBand(orbitFocus, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 12513 - 250, 600f);
        system.addRingBand(orbitFocus, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 12649 - 250, 460f);
        system.addRingBand(orbitFocus, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 12744 - 250, 300f);
        system.addRingBand(orbitFocus, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 12744 - 250, 790f);

        // add one ring that covers all of the above
        ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(400 + 256, 12500 - 250, null, "Outer Rim"));
        ring.setCircularOrbit(orbitFocus, 0, 0, 100);
        // </editor-fold>

        // Synced System
        // <editor-fold defaultstate="collapsed">
        orbitFocus = system.createToken(1, 1);
        system.addEntity(orbitFocus);
        orbitFocus.setOrbit(Global.getFactory().createCircularOrbit(star, 180f, 2000f, 600f));

        // Cold Rock at L5
        SectorEntityToken cold_rock = system.addPlanet(COLD_ROCK.id, orbitFocus, COLD_ROCK.name, "frozen3", 180, 120, 13400 - 250, 600f);
//			agattuI.setCustomDescriptionId(DESC_COLD_ROCK);
//            agattuI.setInteractionImage("illustrations", "vacuum_colony");

            // Ice ring
            system.addRingBand(cold_rock, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 425, 45, Terrain.RING, null);

            // Cold Rock Bastion
            SectorEntityToken pirateStation = system.addCustomEntity(COLD_ROCK_BASTION.id,
                        COLD_ROCK_BASTION.name, "station_side06", Factions.PIRATES);
            pirateStation.setCircularOrbitPointingDown(cold_rock, 45, 200, 30);
            pirateStation.setCustomDescriptionId(COLD_ROCK_BASTION.id + DESC);
//            roiderStation.setInteractionImage("illustrations", "space_wreckage");
//            roiderStation.addTag(Roider_StationMaster.ROIDER_STATION_TAG);

//            PersonAPI roiderAdmin = Global.getSector().getFaction(Roider_Factions.ROIDER_UNION).createRandomPerson();
//            roiderAdmin.getStats().setSkillLevel("industrial_planning", 3);
//            roiderStation.getMarket().setAdmin(roiderAdmin);

            // L4 jump point
            JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint(JP_FRINGE.id, JP_FRINGE.name);
            jumpPoint.setCircularOrbit(orbitFocus, cold_rock.getCircularOrbitAngle() - 60,
                        cold_rock.getCircularOrbitRadius(), cold_rock.getCircularOrbitPeriod());

            jumpPoint.setStandardWormholeToHyperspaceVisual();
            system.addEntity(jumpPoint);


//        // Outer Rim belt
//        system.addAsteroidBelt(orbitFocus, 90, 16000 - 250, 500, 550, 720, Terrain.ASTEROID_BELT,  "Outer Rim");
//        system.addRingBand(orbitFocus, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 15950 - 250, 705f, null, null);
//        system.addRingBand(orbitFocus, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 16070 - 250, 695f, null, null);
        // </editor-fold>
    }

    // Hyperspace cleanup method by Tartiflette
    private static void cleanup(StarSystemAPI system){
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }
}