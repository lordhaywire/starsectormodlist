package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

import data.scripts.world.systems.xlu_Nemvis;
import data.scripts.world.systems.xlu_Phosmon;

public class XLUGen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
        initFactionRelationships(sector);

        new xlu_Nemvis().generate(sector);
        new xlu_Phosmon().generate(sector);
        
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("xlu");   
    }

	public static void initFactionRelationships(SectorAPI sector) {
		FactionAPI XLU = sector.getFaction("xlu");
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
		
        //for (FactionAPI faction : sector.getAllFactions()) {
        //    if (faction != XLU) {
        //        XLU.setRelationship(faction.getId(), RepLevel.SUSPICIOUS);
        //    }
        //}

	XLU.setRelationship(Factions.HEGEMONY, RepLevel.HOSTILE);
	XLU.setRelationship(Factions.PERSEAN, RepLevel.HOSTILE);
        XLU.setRelationship(Factions.PIRATES, RepLevel.HOSTILE);
	XLU.setRelationship(Factions.PLAYER, RepLevel.SUSPICIOUS);
	XLU.setRelationship(Factions.INDEPENDENT, RepLevel.COOPERATIVE);
	XLU.setRelationship(Factions.DIKTAT, RepLevel.NEUTRAL);
        
	XLU.setRelationship("ocua", RepLevel.SUSPICIOUS);
	XLU.setRelationship("anvil_industries", RepLevel.SUSPICIOUS);
        
	independent.setRelationship("xlu", RepLevel.COOPERATIVE);
	diktat.setRelationship("xlu", RepLevel.NEUTRAL);
	hegemony.setRelationship("xlu", RepLevel.HOSTILE);
	persean.setRelationship("xlu", RepLevel.HOSTILE);
	pirates.setRelationship("xlu", RepLevel.HOSTILE);
	omega.setRelationship("xlu", RepLevel.VENGEFUL);
	remnant.setRelationship("xlu", RepLevel.HOSTILE);
        
    }
}