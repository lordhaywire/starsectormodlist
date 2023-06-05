package data.missions.ocua_test;

import java.util.List;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "OSC", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true, 5);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Oculian Test Fleet");
		api.setFleetTagline(FleetSide.ENEMY, "Tri-Tachyon containment armada");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat the enemy forces");
		api.addBriefingItem("Refit the ships and see how they handle.");
		
		// Set up the player's fleet
		//api.addToFleet(FleetSide.PLAYER, "ocua_station_level1", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "ocua_station_level2", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "ocua_station_level3", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.PLAYER, "ocua_chimly_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_gretly_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_huely_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_prily_Escort", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.PLAYER, "ocua_chimis_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_doris_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_maximis_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_sconis_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_sophis_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_tsundere_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_sophis_c_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_sophis_b_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_storkis_Standard", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.PLAYER, "ocua_basilix_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_beatrix_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_chimex_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_dorothyx_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_ignix_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_nimbyx_Standard", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.PLAYER, "ocua_amina_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_chimiria_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_etna_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_nadia_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_utena_Standard", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.PLAYER, "ocua_pandora_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_dulcena_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "ocua_galalixia_Support", FleetMemberType.SHIP, "OSC 00001", true);
		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "paragon_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "paragon_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "astral_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "astral_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "astral_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "astral_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "odyssey_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "odyssey_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "odyssey_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "xyphos_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "xyphos_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "xyphos_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "xyphos_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "trident_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "trident_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "trident_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "trident_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "dagger_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "dagger_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "dagger_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "dagger_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "dagger_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
		
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		for (int i = 0; i < 300; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/4;
			
			if (x > -1000 && x < 1500 && y < -1000) continue;
			float radius = 200f + (float) Math.random() * 900f; 
			api.addNebula(x, y, radius);
		}
		
		api.addPlugin(new BaseEveryFrameCombatPlugin() {
			public void init(CombatEngineAPI engine) {
				engine.getContext().setStandoffRange(12000f);
			}
			public void advance(float amount, List events) {
			}
		});
			
	}

}






