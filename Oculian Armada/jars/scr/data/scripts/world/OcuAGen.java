package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

import data.scripts.world.systems.ocua_Atalie;
import data.scripts.world.systems.ocua_Haelim;

public class OcuAGen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
        initFactionRelationships(sector);

        new ocua_Atalie().generate(sector);
        new ocua_Haelim().generate(sector);
        
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("ocua");
    }

	public static void initFactionRelationships(SectorAPI sector) {
		FactionAPI OcuA = sector.getFaction("ocua");
		FactionAPI player = sector.getFaction(Factions.PLAYER);

		FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
		FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
		FactionAPI pirates = sector.getFaction(Factions.PIRATES);
		FactionAPI independent = sector.getFaction(Factions.INDEPENDENT);
		FactionAPI kol = sector.getFaction(Factions.KOL);
		FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
		FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
		FactionAPI diktat = sector.getFaction(Factions.DIKTAT);
		FactionAPI persean = sector.getFaction(Factions.PERSEAN);
		FactionAPI remnant = sector.getFaction(Factions.REMNANTS);
		FactionAPI derelict = sector.getFaction(Factions.DERELICT);
		FactionAPI omega = sector.getFaction(Factions.OMEGA);
		
        for (FactionAPI faction : sector.getAllFactions()) {
            if (faction != OcuA || 
                !(faction.getRelationshipLevel(OcuA) == RepLevel.HOSTILE) || 
                !(faction.getRelationshipLevel(OcuA) == RepLevel.VENGEFUL)) {
                OcuA.setRelationship(faction.getId(), RepLevel.INHOSPITABLE);
            }
        }

	OcuA.setRelationship(Factions.TRITACHYON, RepLevel.VENGEFUL);
	OcuA.setRelationship(Factions.PLAYER, RepLevel.SUSPICIOUS);
	OcuA.setRelationship(Factions.LUDDIC_PATH, RepLevel.VENGEFUL);
	OcuA.setRelationship(Factions.OMEGA, RepLevel.VENGEFUL);
	OcuA.setRelationship(Factions.LUDDIC_CHURCH, RepLevel.HOSTILE);
	OcuA.setRelationship(Factions.REMNANTS, RepLevel.HOSTILE);
	//OcuA.setRelationship(Factions.INDEPENDENT, RepLevel.INHOSPITABLE);
	OcuA.setRelationship(Factions.HEGEMONY, RepLevel.SUSPICIOUS);
	OcuA.setRelationship(Factions.DERELICT, RepLevel.NEUTRAL);
        OcuA.setRelationship(Factions.DIKTAT, RepLevel.FAVORABLE);
        OcuA.setRelationship(Factions.PIRATES, RepLevel.COOPERATIVE);
        
	player.setRelationship("ocua", RepLevel.SUSPICIOUS);
	tritachyon.setRelationship("ocua", RepLevel.VENGEFUL);
	path.setRelationship("ocua", RepLevel.VENGEFUL);
	omega.setRelationship("ocua", RepLevel.VENGEFUL);
	church.setRelationship("ocua", RepLevel.HOSTILE);
	remnant.setRelationship("ocua", RepLevel.HOSTILE);
	//independent.setRelationship("ocua", RepLevel.INHOSPITABLE);
	hegemony.setRelationship("ocua", RepLevel.SUSPICIOUS);
	derelict.setRelationship("ocua", RepLevel.NEUTRAL);
        diktat.setRelationship("ocua", RepLevel.FAVORABLE);
	pirates.setRelationship("ocua", RepLevel.COOPERATIVE);
        
	OcuA.setRelationship("mess", RepLevel.HOSTILE);
	OcuA.setRelationship("mess_remnant", RepLevel.HOSTILE);
        
        boolean haveBlue = Global.getSettings().getModManager().isModEnabled("xlu");
        if (haveBlue){
		FactionAPI xlu = sector.getFaction("xlu");
            OcuA.setRelationship("xlu", RepLevel.FAVORABLE);
            xlu.setRelationship("ocua", RepLevel.FAVORABLE);
        }
    }
}