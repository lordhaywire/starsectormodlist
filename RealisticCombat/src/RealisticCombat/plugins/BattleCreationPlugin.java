package RealisticCombat.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lwjgl.util.vector.Vector2f;
import RealisticCombat.settings.Map;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public final class BattleCreationPlugin implements com.fs.starfarer.api.campaign.BattleCreationPlugin {

    private static final float
            DEPLOYMENT_BURN_DURATION_INITIAL = 3.5f,
            DEPLOYMENT_BURN_DURATION_NORMAL = 6f,
            DEPLOYMENT_BURN_DURATION_ESCAPE = 1.5f;

    private float width, height, prevXDir = 0, prevYDir = 0, coronaIntensity = 0f;

    private List<String> objs = null;

    private BattleCreationContext context;

    private MissionDefinitionAPI loader;

    private StarCoronaTerrainPlugin corona = null;

    private PulsarBeamTerrainPlugin pulsar = null;

    private static final String COMM = "comm_relay", SENSOR = "sensor_array", NAV = "nav_buoy";

    private static final float xPad = 2000, yPad = 3000, SINGLE_PLANET_MAX_DIST = 1000f;

    public interface NebulaTextureProvider { String getNebulaTex(); String getNebulaMapTex(); }


    public void initBattle(final BattleCreationContext context, final MissionDefinitionAPI loader) {

        this.context = context;
        this.loader = loader;
        final CampaignFleetAPI playerFleet = context.getPlayerFleet(),
                               otherFleet = context.getOtherFleet();
        final FleetGoal playerGoal = context.getPlayerGoal(), enemyGoal = context.getOtherGoal();

        final Random random = Misc.getRandom(Misc.getSalvageSeed(otherFleet)
                              * (long) otherFleet.getFleetData().getNumMembers(), 23);

        final boolean playerEscaping = playerGoal == FleetGoal.ESCAPE,
                      enemyEscaping = enemyGoal == FleetGoal.ESCAPE,
                      escape = playerEscaping || enemyEscaping;

        final int maxFleetPoints = (int) Global.getSettings().getFloat("maxNoObjectiveBattleSize");
        int playerFleetPoints = 0; int enemyFleetPoints = 0;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy())
            if (playerEscaping || member.canBeDeployedForCombat())
                playerFleetPoints += member.getUnmodifiedDeploymentPointsCost();
        for (FleetMemberAPI member : otherFleet.getFleetData().getMembersListCopy())
            if (playerEscaping || member.canBeDeployedForCombat())
                enemyFleetPoints += member.getUnmodifiedDeploymentPointsCost();

        final int smaller = Math.min(playerFleetPoints, enemyFleetPoints);
        boolean withObjectives = smaller > maxFleetPoints;
        if (!context.objectivesAllowed) withObjectives = false;

        int numObjectives = 0;
        if (withObjectives)
            numObjectives = (playerFleetPoints + enemyFleetPoints > maxFleetPoints + 70)
                             ? 4 : 3 + random.nextInt(2);
        numObjectives = Math.min(numObjectives, 4); // shouldn't be possible, but..

        int baseCommandPoints = (int) Global.getSettings().getFloat("startingCommandPoints");
        loader.initFleet(FleetSide.PLAYER, "ISS", playerGoal, false,
                context.getPlayerCommandPoints() - baseCommandPoints,
                (int) playerFleet.getCommanderStats().getCommandPoints().getModifiedValue()
                        - baseCommandPoints);
        loader.initFleet(FleetSide.ENEMY, "", enemyGoal, true,
                (int) otherFleet.getCommanderStats().getCommandPoints().getModifiedValue()
                        - baseCommandPoints);

        List<FleetMemberAPI> playerShips =
                playerFleet.getFleetData().getCombatReadyMembersListCopy();
        List<FleetMemberAPI> enemyShips = otherFleet.getFleetData().getCombatReadyMembersListCopy();
        if (playerGoal == FleetGoal.ESCAPE)
            playerShips = playerFleet.getFleetData().getMembersListCopy();
        if (enemyGoal == FleetGoal.ESCAPE)
            enemyShips = otherFleet.getFleetData().getMembersListCopy();
        for (FleetMemberAPI member : playerShips) loader.addFleetMember(FleetSide.PLAYER, member);
        for (FleetMemberAPI member : enemyShips) loader.addFleetMember(FleetSide.ENEMY, member);

        width = RealisticCombat.settings.BattleCreationPlugin.getMapWidthBase();
        height = RealisticCombat.settings.BattleCreationPlugin.getMapHeightBase();
        if (withObjectives) {
            width *= 1.3f; if (numObjectives == 2) height *= 7f/9f;
        }

        width *= Map.getMapSizeFactor();
        height *= Map.getMapSizeFactor();

        createMap(random);

        context.setInitialDeploymentBurnDuration(DEPLOYMENT_BURN_DURATION_INITIAL);
        context.setNormalDeploymentBurnDuration(DEPLOYMENT_BURN_DURATION_NORMAL);
        context.setEscapeDeploymentBurnDuration(DEPLOYMENT_BURN_DURATION_ESCAPE);

        if (escape) {
            addEscapeObjectives(random);
            context.setInitialEscapeRange(
                    Global.getSettings().getFloat("escapeStartDistance")
            ); context.setFlankDeploymentDistance(
                    Global.getSettings().getFloat("escapeFlankDistance")
            ); loader.addPlugin(new EscapeRevealPlugin(context));
        } else {
            if (withObjectives) {
                addObjectives(loader, numObjectives, random);
                context.setStandoffRange(height * Map.getStandoffFactorWithObjectives());
            } else context.setStandoffRange(height * Map.getStandoffFactorWithoutObjectives());
            context.setFlankDeploymentDistance(height/2f); // matters for Force Concentration
        }
    }

    public void afterDefinitionLoad(final CombatEngineAPI engine) {
        if (coronaIntensity > 0 && (corona != null || pulsar != null)) {
            String name = "Corona";
            if (pulsar != null) name = pulsar.getTerrainName();
            else if (corona != null) name = corona.getTerrainName();

            final String name2 = name;

            final Object key1 = new Object(), key2 = new Object();
            final String icon =
                    Global.getSettings().getSpriteName("ui", "icon_tactical_cr_penalty");
            engine.addPlugin(new BaseEveryFrameCombatPlugin() {
                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    engine.maintainStatusForPlayerShip(key1, icon, name2, "reduced peak time", true);
                    engine.maintainStatusForPlayerShip(key2, icon, name2, "faster CR degradation", true);
                }
            });
        }
    }

    private void createMap(final Random random) {
        loader.initMap(-width /2f, width /2f, -height /2f, height /2f);

        CampaignFleetAPI playerFleet = context.getPlayerFleet();
        String nebulaTex = null, nebulaMapTex = null;
        boolean inNebula = false, privateFromCorona = false;
        for (CustomCampaignEntityAPI entity :
                playerFleet.getContainingLocation().getCustomEntitiesWithTag(
                        Tags.PROTECTS_FROM_CORONA_IN_BATTLE)
        )
            if (Misc.getDistance(entity.getLocation(), playerFleet.getLocation())
                 <= entity.getRadius() + playerFleet.getRadius() + 10f)
            {
                privateFromCorona = true;
                break;
            }

        float numRings = 0;

        Color coronaColor = null;
        for (CampaignTerrainAPI terrain : playerFleet.getContainingLocation().getTerrainCopy()) {
            if (terrain.getPlugin() instanceof NebulaTextureProvider) {
                if (terrain.getPlugin().containsEntity(playerFleet)) {
                    inNebula = true;
                    if (terrain.getPlugin() instanceof NebulaTextureProvider) {
                        NebulaTextureProvider provider = (NebulaTextureProvider) terrain.getPlugin();
                        nebulaTex = provider.getNebulaTex();
                        nebulaMapTex = provider.getNebulaMapTex();
                    }
                } else {
                    if (nebulaTex == null) {
                        if (terrain.getPlugin() instanceof NebulaTextureProvider) {
                            NebulaTextureProvider provider =
                                    (NebulaTextureProvider) terrain.getPlugin();
                            nebulaTex = provider.getNebulaTex();
                            nebulaMapTex = provider.getNebulaMapTex();
                        }
                    }
                }
            } else if (terrain.getPlugin() instanceof StarCoronaTerrainPlugin
                        && pulsar == null
                        && !privateFromCorona)
            {
                StarCoronaTerrainPlugin plugin = (StarCoronaTerrainPlugin) terrain.getPlugin();
                if (plugin.containsEntity(playerFleet)) {
                    float intensity = plugin.getIntensityAtPoint(playerFleet.getLocation()),
                          angle = Misc.getAngleInDegrees(terrain.getLocation(),
                                                         playerFleet.getLocation());
                    Color color = plugin.getAuroraColorForAngle(angle);
                    intensity = 0.4f + 0.6f * intensity;
                    int alpha = (int)(80f * intensity);
                    color = Misc.setAlpha(color, alpha);
                    if (coronaColor == null || coronaColor.getAlpha() < alpha) {
                        coronaColor = color;
                        coronaIntensity = intensity;
                        corona = plugin;
                    }
                }
            } else if (terrain.getPlugin() instanceof PulsarBeamTerrainPlugin
                       && !privateFromCorona)
            {
                PulsarBeamTerrainPlugin plugin = (PulsarBeamTerrainPlugin) terrain.getPlugin();
                if (plugin.containsEntity(playerFleet)) {
                    float angle = Misc.getAngleInDegreesStrict(terrain.getLocation(),
                                                               playerFleet.getLocation());
                    Color color = plugin.getPulsarColorForAngle(angle);
                    float intensity = plugin.getIntensityAtPoint(playerFleet.getLocation());
                    intensity = 0.4f + 0.6f * intensity;
                    int alpha = (int)(80f * intensity);
                    color = Misc.setAlpha(color, alpha);
                    if (coronaColor == null || coronaColor.getAlpha() < alpha) {
                        coronaColor = color;
                        coronaIntensity = intensity;
                        pulsar = plugin;
                        corona = null;
                    }
                }
            } else if (terrain.getType().equals(Terrain.RING)
                       && terrain.getPlugin().containsEntity(playerFleet)) numRings++;
        }
        if (nebulaTex != null) {
            loader.setNebulaTex(nebulaTex);
            loader.setNebulaMapTex(nebulaMapTex);
        }

        if (coronaColor != null) loader.setBackgroundGlowColor(coronaColor);

        int numNebula = 15;
        if (inNebula) numNebula = 100;
        if (!inNebula && playerFleet.isInHyperspace()) numNebula = 0;

        for (int i = 0; i < numNebula; i++) {
            float x = random.nextFloat() * width - width / 2,
                  y = random.nextFloat() * height - height / 2,
                  radius = 100f + random.nextFloat() * 400f;
            if (inNebula) radius += 100f + 500f * random.nextFloat();
            loader.addNebula(x, y, radius);
        }

        float numAsteroidsWithinRange = countNearbyAsteroids(playerFleet);
        int numAsteroids = Math.min(400, (int)((numAsteroidsWithinRange + 1f) * 20f));
        loader.addAsteroidField(0, 0, random.nextFloat() * 360f, width,
                20f, 70f, numAsteroids);
        if (numRings > 0) {
            int numRingAsteroids =
                    (int) Math.min((numRings * 300 + (numRings * 600f) * random.nextFloat()), 1500);
            loader.addRingAsteroids(0, 0, random.nextFloat() * 360f, width,
                    100f, 200f, numRingAsteroids);
        }

        loader.setBackgroundSpriteName(
                playerFleet.getContainingLocation().getBackgroundTextureFilename()
        );
        loader.setHyperspaceMode(
                playerFleet.getContainingLocation() == Global.getSector().getHyperspace()
        );

        addClosestPlanet();
    }

    private void addClosestPlanet() {
        float bgWidth = 2048f, bgHeight = 2048f;

        CampaignFleetAPI playerFleet = context.getPlayerFleet();
        PlanetAPI planet = getClosestPlanet(playerFleet);
        if (planet == null) return;

        float dist = Vector2f.sub(playerFleet.getLocation(), planet.getLocation(),
                                  new Vector2f()).length() - planet.getRadius();
        dist = Math.max(dist, 0);
        float baseRadius = planet.getRadius(),
              scaleFactor = 1.5f,
              minRadius = 100f,
              maxRadius = 500f,
              maxDist = Math.max(SINGLE_PLANET_MAX_DIST - planet.getRadius(), 1);

        boolean playerHasStation = false; boolean enemyHasStation = false;
        for (FleetMemberAPI curr : playerFleet.getFleetData().getMembersListCopy())
            if (curr.isStation()) { playerHasStation = true; break; }
        for (FleetMemberAPI curr : context.getOtherFleet().getFleetData().getMembersListCopy())
            if (curr.isStation()) { enemyHasStation = true; break; }

        float planetYOffset = 0;

        if (playerHasStation) planetYOffset = -bgHeight / 2f * 0.5f;
        if (enemyHasStation) planetYOffset = bgHeight / 2f * 0.5f;

        float f = (maxDist - dist) / maxDist * 0.65f + 0.35f,
              radius = baseRadius * f * scaleFactor;
        if (radius > maxRadius) radius = maxRadius;
        if (radius < minRadius) radius = minRadius;
        loader.setPlanetBgSize(bgWidth * f, bgHeight * f);
        loader.addPlanet(0f, planetYOffset, radius, planet, 0f, true);
    }

    private void addObjectives(final MissionDefinitionAPI loader,
                               final int num,
                               final Random random) {
        objs = new ArrayList<String>(Arrays.asList(SENSOR, SENSOR, NAV, NAV));

        if (num == 2) { // minimum is 3 now, so this shouldn't happen
            objs = new ArrayList<String>(Arrays.asList(SENSOR, SENSOR, NAV, NAV, COMM));
            addObjectiveAt(0.25f, 0.5f, 0f, 0f, random);
            addObjectiveAt(0.75f, 0.5f, 0f, 0f, random);
        } else if (num == 3) {
            float r = random.nextFloat();
            if (r < 0.33f) {
                addObjectiveAt(0.25f, 0.7f, 1f, 1f, random);
                addObjectiveAt(0.25f, 0.3f, 1f, 1f, random);
                addObjectiveAt(0.75f, 0.5f, 1f, 1f, COMM, random);
            } else if (r < 0.67f) {
                addObjectiveAt(0.75f, 0.7f, 1f, 1f, random);
                addObjectiveAt(0.75f, 0.3f, 1f, 1f, random);
                addObjectiveAt(0.25f, 0.5f, 1f, 1f, COMM, random);
            } else {
                if (random.nextFloat() < 0.5f) {
                    addObjectiveAt(0.22f, 0.7f, 1f, 1f, random);
                    addObjectiveAt(0.5f, 0.5f, 1f, 1f, COMM, random);
                    addObjectiveAt(0.78f, 0.3f, 1f, 1f, random);
                } else {
                    addObjectiveAt(0.22f, 0.3f, 1f, 1f, random);
                    addObjectiveAt(0.5f, 0.5f, 1f, 1f, COMM, random);
                    addObjectiveAt(0.78f, 0.7f, 1f, 1f, random);
                }
            }
        } else if (num == 4) {
            float r = random.nextFloat();
            if (r < 0.33f) {
                String [] maybeRelays = pickCommRelays(2, 2, false, true, true, false, random);
                addObjectiveAt(0.25f, 0.25f, 2f, 1f, maybeRelays[0], random);
                addObjectiveAt(0.25f, 0.75f, 2f, 1f, maybeRelays[1], random);
                addObjectiveAt(0.75f, 0.25f, 2f, 1f, maybeRelays[2], random);
                addObjectiveAt(0.75f, 0.75f, 2f, 1f, maybeRelays[3], random);
            } else if (r < 0.67f) {
                String [] maybeRelays = pickCommRelays(1, 2, true, false, true, false, random);
                addObjectiveAt(0.25f, 0.5f, 1f, 1f, maybeRelays[0], random);
                addObjectiveAt(0.5f, 0.75f, 1f, 1f, maybeRelays[1], random);
                addObjectiveAt(0.75f, 0.5f, 1f, 1f, maybeRelays[2], random);
                addObjectiveAt(0.5f, 0.25f, 1f, 1f, maybeRelays[3], random);
            } else {
                if (random.nextFloat() < 0.5f) {
                    String [] maybeRelays = pickCommRelays(1, 2, true, false, true, false, random);
                    addObjectiveAt(0.25f, 0.25f, 1f, 0f, maybeRelays[0], random);
                    addObjectiveAt(0.4f, 0.6f, 1f, 0f, maybeRelays[1], random);
                    addObjectiveAt(0.6f, 0.4f, 1f, 0f, maybeRelays[2], random);
                    addObjectiveAt(0.75f, 0.75f, 1f, 0f, maybeRelays[3], random);
                } else {
                    String [] maybeRelays = pickCommRelays(1, 2, false, true, false, true, random);
                    addObjectiveAt(0.25f, 0.75f, 1f, 0f, maybeRelays[0], random);
                    addObjectiveAt(0.4f, 0.4f, 1f, 0f, maybeRelays[1], random);
                    addObjectiveAt(0.6f, 0.6f, 1f, 0f, maybeRelays[2], random);
                    addObjectiveAt(0.75f, 0.25f, 1f, 0f, maybeRelays[3], random);
                }
            }
        }
    }

    private String [] pickCommRelays(final int min,
                                     final int max,
                                     final boolean comm1,
                                     final boolean comm2,
                                     final boolean comm3,
                                     final boolean comm4,
                                     final Random random)
    {
        String [] result = new String [4];

        WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<Integer>(random);
        if (comm1) picker.add(0);
        if (comm2) picker.add(1);
        if (comm3) picker.add(2);
        if (comm4) picker.add(3);

        int num = min + random.nextInt(max - min + 1);

        for (int i = 0; i < num && !picker.isEmpty(); i++) result[picker.pickAndRemove()] = COMM;
        return result;
    }

    private void addEscapeObjectives(final Random random) {
        objs = new ArrayList<>(Arrays.asList(SENSOR, SENSOR, NAV, NAV, COMM));
        float r = random.nextFloat();
        if (r < 0.33f) {
            addObjectiveAt(0.25f, 0.25f, 1f, 1f, random);
            addObjectiveAt(0.75f, 0.75f, 1f, 1f, random);
        } else if (r < 0.67f) {
            addObjectiveAt(0.75f, 0.25f, 1f, 1f, random);
            addObjectiveAt(0.25f, 0.75f, 1f, 1f, random);
        } else {
            addObjectiveAt(0.5f, 0.25f, 4f, 2f, random);
            addObjectiveAt(0.5f, 0.75f, 4f, 2f, random);
        }
    }

    private void addObjectiveAt(final float xMult,
                                final float yMult,
                                final float xOff,
                                final float yOff,
                                final Random random) {
        addObjectiveAt(xMult, yMult, xOff, yOff, null, random);
    }

    private void addObjectiveAt(final float xMult,
                                final float yMult,
                                final float xOff,
                                final float yOff,
                                String type,
                                final Random random)
    {
        if (type == null) {
            type = pickAny(random);
            if (objs != null && objs.size() > 0) {
                int index = (int) (random.nextDouble() * objs.size());
                type = objs.remove(index);
            }
        }

        float minX = -width/2 + xPad, minY = -height/2 + yPad,
              x = (width - xPad * 2f) * xMult + minX, y = (height - yPad * 2f) * yMult + minY;
        x = ((int) x / 1000) * 1000f; y = ((int) y / 1000) * 1000f;

        float offsetX = Math.round((random.nextFloat() - 0.5f) * xOff * 1f) * 1000f,
              offsetY = Math.round((random.nextFloat() - 0.5f) * yOff * 1f) * 1000f;

        float xDir = Math.signum(offsetX); float yDir = Math.signum(offsetY);
        if (xDir == prevXDir && xOff > 0) {
            xDir *= -1;
            offsetX = Math.abs(offsetX) * -prevXDir;
        }
        if (yDir == prevYDir && yOff > 0) {
            yDir *= -1;
            offsetY = Math.abs(offsetY) * -prevYDir;
        }

        prevXDir = xDir; prevYDir = yDir;

        x += offsetX; y += offsetY;

        loader.addObjective(x, y, type);

        if (random.nextFloat() > 0.6f) {
            float nebulaSize = random.nextFloat() * 1500f + 500f;
            loader.addNebula(x, y, nebulaSize);
        }
    }

    private String pickAny(final Random random) {
        float r = random.nextFloat();
        if (r < 0.33f) return "nav_buoy";
        else if (r < 0.67f) return "sensor_array";
        return "comm_relay";
    }

    private float countNearbyAsteroids(final CampaignFleetAPI playerFleet) {
        float numAsteroidsWithinRange = 0;
        LocationAPI loc = playerFleet.getContainingLocation();
        if (loc instanceof StarSystemAPI) {
            StarSystemAPI system = (StarSystemAPI) loc;
            List<SectorEntityToken> asteroids = system.getAsteroids();
            for (SectorEntityToken asteroid : asteroids) {
                float range = Vector2f.sub(playerFleet.getLocation(),
                                           asteroid.getLocation(),
                                           new Vector2f()).length();
                if (range < 300) numAsteroidsWithinRange++;
            }
        } return numAsteroidsWithinRange;
    }

    private PlanetAPI getClosestPlanet(final CampaignFleetAPI playerFleet) {
        LocationAPI loc = playerFleet.getContainingLocation();
        PlanetAPI closest = null;
        float minDist = Float.MAX_VALUE;
        if (loc instanceof StarSystemAPI) {
            StarSystemAPI system = (StarSystemAPI) loc;
            List<PlanetAPI> planets = system.getPlanets();
            Vector2f playerFleetLocation = context.getPlayerFleet().getLocation();
            for (PlanetAPI planet : planets) {
                if (planet.isStar()) continue;
                if (Planets.PLANET_LAVA.equals(planet.getTypeId())) continue;
                if (Planets.PLANET_LAVA_MINOR.equals(planet.getTypeId())) continue;
                if (planet.getSpec().isDoNotShowInCombat()) continue;
                float dist = Vector2f.sub(playerFleetLocation,
                             planet.getLocation(),
                             new Vector2f()).length();
                if (dist < minDist && dist < SINGLE_PLANET_MAX_DIST) {
                    closest = planet;
                    minDist = dist;
                }
            }
        } return closest;
    }
}