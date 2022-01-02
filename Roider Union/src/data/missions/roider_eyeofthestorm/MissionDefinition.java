package data.missions.roider_eyeofthestorm;

import java.util.List;

import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import static data.missions.Roider_DModManager.addDShipToFleet;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

// Eye of the Storm
public class MissionDefinition implements MissionDefinitionPlugin {
    @Override
    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ESCAPE, false);
        api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true);

        String lewisShip = "ISS Clarke";

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, "Captain Lewis's " + lewisShip);
        api.setFleetTagline(FleetSide.ENEMY, "Tri-Tachyon Security Patrol");

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Reach the jump point");

        // Set up the player's fleet
        addDShipToFleet(2, FleetSide.PLAYER, "roider_cyclops_early_Outdated", lewisShip, true, new Random(22345), api);


        // Set up the enemy fleet
        api.addToFleet(FleetSide.ENEMY, "roider_tempest_Pursuit", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "roider_wolf_Pursuit", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "roider_wolf_Pursuit", FleetMemberType.SHIP, false);



        // Set up the map.
        float width = 4000f;
        float height = 24000f;
        api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

//		float minX = -width/2;
//		float minY = -height/2;

        for (int i = 0; i < 15; i++) {
            float x = (float) Math.random() * width - width/2;
            float y = (float) Math.random() * height - height/2;
            float radius = 100f + (float) Math.random() * 900f;
            api.addNebula(x, y, radius);
        }

        api.addAsteroidField(10f, 0f, -88f, width, 400f, 600f, 150);
        api.addAsteroidField(-10f, 0f, -92f, width, 400f, 600f, 150);
        api.addAsteroidField(0f, 0f, 0f, height - 1000, 40f, 60f, 100);

        api.addPlugin(new SpawnShift(width, height));

        BattleCreationContext context = new BattleCreationContext(null, null, null, null);
        context.setInitialEscapeRange(10000f);
        api.addPlugin(new EscapeRevealPlugin(context));
    }

    // Credit: Cycerin, Dark Revenant
    public class SpawnShift extends BaseEveryFrameCombatPlugin {

        private final float mapX;
        private final float mapY;
        boolean hasDeployed = false;

        private SpawnShift(float mapX, float mapY) {
            this.mapX = mapX;
            this.mapY = mapY;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {

                if (!hasDeployed) {
                    CombatFleetManagerAPI enemyFleet = Global.getCombatEngine().getFleetManager(FleetSide.ENEMY);

                    for (FleetMemberAPI member : enemyFleet.getReservesCopy()) {
                        if (!enemyFleet.getDeployedCopy().contains(member)) {
                            enemyFleet.spawnFleetMember(member, getSafeSpawn(FleetSide.ENEMY, mapX, mapY), 90f, 1f);
                        }
                    }
                    hasDeployed = true;
                }
        }

        public Vector2f getSafeSpawn(FleetSide side, float mapX, float mapY) {
            Vector2f spawnLocation = new Vector2f();

            spawnLocation.x = MathUtils.getRandomNumberInRange(-mapX / 2, mapX / 2);
            spawnLocation.y = ((-mapY / 2f) + 1000);

            return spawnLocation;
        }
    }
}