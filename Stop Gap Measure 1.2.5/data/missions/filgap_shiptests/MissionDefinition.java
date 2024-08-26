package data.missions.filgap_shiptests;

import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false, 27);
		api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Your Forces");
		api.setFleetTagline(FleetSide.ENEMY, "Enemy Forces");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Experiment with and test the added ships");
		
		// Friendly ships


		api.addToFleet(FleetSide.PLAYER, "filgap_adjudicator_Standard", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "filgap_cossack_Attack", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER,
"filgap_huntsman_Standard", FleetMemberType.SHIP, true);

		api.addToFleet(FleetSide.PLAYER, 
"filgap_cathar_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, 
"filgap_hammerhead_cgr_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, 
"filgap_bogomil_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "filgap_templar_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "filgap_fulk_Standard", FleetMemberType.SHIP, false);

		api.addToFleet(FleetSide.PLAYER, 
"filgap_auspicious_balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, 
"filgap_formica_balanced", FleetMemberType.SHIP, false);

		api.addToFleet(FleetSide.PLAYER, 
"filgap_orthus_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, 
"filgap_pyrrhocorax_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, 
"filgap_rosatom_balanced", FleetMemberType.SHIP, false);


		api.addToFleet(FleetSide.PLAYER, 
"filgap_trajan_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, 
"filgap_charon_siege", FleetMemberType.SHIP, false);

		api.addToFleet(FleetSide.PLAYER, 
"filgap_wagner_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "filgap_herder_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "filgap_velite_Standard", FleetMemberType.SHIP, false);

		api.addToFleet(FleetSide.PLAYER, "filgap_starblazer_Standard", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "filgap_era_defense", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "filgap_opportunity_Support", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "filgap_foundation_Support", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "filgap_babel_Support", FleetMemberType.SHIP, true);

		api.addToFleet(FleetSide.PLAYER, 
"filgap_nitassinan_standard", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, 
"filgap_copernic_Support", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, 
"filgap_wardlaw_attack", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, 
"filgap_traverse_Standard", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, 
"filgap_dove_Standard", FleetMemberType.SHIP, true);


		// Enemy forces
		api.addToFleet(FleetSide.ENEMY, "filgap_opportunity_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "filgap_wagner_Pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, 
"filgap_ballista_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "filgap_velite_Pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_d_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "cerberus_d_pirates_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hound_d_pirates_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "filgap_pyrphoros_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "filgap_pyrphoros_Escort", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
		
		// Set up the map.
		float width = 12000f;
		float height = 12000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
		api.addPlanet(0, 0, 50f, "star_yellow", 250f, true);
		
	}

}
