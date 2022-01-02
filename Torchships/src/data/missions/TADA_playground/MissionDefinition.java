package data.missions.TADA_playground;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

        @Override
	public void defineMission(MissionDefinitionAPI api) {

            // Set up the fleets
            api.initFleet(FleetSide.PLAYER, "SGS", FleetGoal.ATTACK, false);
            api.initFleet(FleetSide.ENEMY, "???", FleetGoal.ATTACK, true);

            // Set a blurb for each fleet
            api.setFleetTagline(FleetSide.PLAYER, "Showcase Group");
            api.setFleetTagline(FleetSide.ENEMY, "Unknown contact");

            // These show up as items in the bulleted list under 
            // "Tactical Objectives" on the mission detail screen
            api.addBriefingItem("Let's do SCIENCE!");


            // Set up the player's fleet                                  
            api.addToFleet(FleetSide.PLAYER, "TADA_challenger_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_challenger_pirate_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_challenger_pather_combat", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_tinnitus_assault", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_tinnitus_p_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_tinnitus_plasma_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_tinnitus_xiv_assault", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_lasherTT_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_tempest_pirate_combat", FleetMemberType.SHIP, false);

            api.addToFleet(FleetSide.PLAYER, "TADA_bully_combat", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_bully_pirate_combat", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_bully_pather_brawler", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_scalper_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_scalper_pirate_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_cassicus_combat", FleetMemberType.SHIP, false);

            api.addToFleet(FleetSide.PLAYER, "TADA_hightide_outdated", FleetMemberType.SHIP, false);    
            api.addToFleet(FleetSide.PLAYER, "TADA_hightide_pirate_outdated", FleetMemberType.SHIP, false);                         
            api.addToFleet(FleetSide.PLAYER, "TADA_foray_standard", FleetMemberType.SHIP, false);                            
            api.addToFleet(FleetSide.PLAYER, "TADA_foray_pirate_standard", FleetMemberType.SHIP, false);                            
            api.addToFleet(FleetSide.PLAYER, "TADA_foray_pather_standard", FleetMemberType.SHIP, false);       
            api.addToFleet(FleetSide.PLAYER, "TADA_attrition_elite", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_bonnethead_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_bonnethead_pirate_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_bonnethead_pather_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_owl_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_whirlwind_standard", FleetMemberType.SHIP, false);       

            api.addToFleet(FleetSide.PLAYER, "TADA_gunwall_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_gunwall_pirate_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_gunwall_XIV_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "TADA_herald_standard", FleetMemberType.SHIP, false);

            api.addToFleet(FleetSide.PLAYER, "TADA_crane_standard", FleetMemberType.SHIP, "SAS Birdie", false);    
            api.addToFleet(FleetSide.PLAYER, "TADA_crane_pirate_standard", FleetMemberType.SHIP, false);         


            // Mark a ship as essential, if you want
            //api.defeatOnShipLoss("ISS Black Star");

            // Set up the enemy fleet
            for(int i=0; i<60; i++){
                api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, true).getCaptain().setPersonality("reckless");
            }

            // Set up the map.
            float width = 20000f;
            float height = 12000f;
            api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

            float minX = -width/2;
            float minY = -height/2;

            // All the addXXX methods take a pair of coordinates followed by data for
            // whatever object is being added.

            // Add two big nebula clouds
            api.addNebula(minX + width * 0.66f, minY + height * 0.5f, 2000);
            api.addNebula(minX + width * 0.25f, minY + height * 0.6f, 1000);
            api.addNebula(minX + width * 0.25f, minY + height * 0.4f, 1000);

            // And a few random ones to spice up the playing field.
            // A similar approach can be used to randomize everything
            // else, including fleet composition.
            for (int i = 0; i < 5; i++) {
                    float x = (float) Math.random() * width - width/2;
                    float y = (float) Math.random() * height - height/2;
                    float radius = 100f + (float) Math.random() * 400f; 
                    api.addNebula(x, y, radius);
            }

            // Add objectives. These can be captured by each side
            // and provide stat bonuses and extra command points to
            // bring in reinforcements.
            // Reinforcements only matter for large fleets - in this
            // case, assuming a 100 command point battle size,
            // both fleets will be able to deploy fully right away.
            api.addObjective(
                    minX + width * 0.25f,
                    minY + height * 0.5f, 
                    "sensor_array");
            api.addObjective(
                    minX + width * 0.75f,
                    minY + height * 0.5f, 
                    "comm_relay");
            api.addObjective(
                    minX + width * 0.33f,
                    minY + height * 0.25f, 
                    "nav_buoy");
            api.addObjective(
                    minX + width * 0.66f,
                    minY + height * 0.75f, 
                    "nav_buoy");


            api.addAsteroidField(-(minY + height), minY + height, -90, 500f,
                                                            150f, 200f, 100);
//                api.addPlugin(new Plugin());
	}
        
    private final static class Plugin extends BaseEveryFrameCombatPlugin {
        @Override
        public void init(CombatEngineAPI engine) {
            Global.getSettings().setDevMode(true);
        }
    }
}






