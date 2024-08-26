package data.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.Global;

public class KoClove implements SectorGeneratorPlugin {
	@Override
    public void generate(SectorAPI sector) {
		initFactionRelationships(sector);
    }

	public static void initFactionRelationships(SectorAPI sector) {
        	FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
		FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
		FactionAPI pirates = sector.getFaction(Factions.PIRATES);
		FactionAPI independent = sector.getFaction(Factions.INDEPENDENT);
		FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
		FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
		FactionAPI player = sector.getFaction(Factions.PLAYER);
		FactionAPI diktat = sector.getFaction(Factions.DIKTAT);
        	FactionAPI league = sector.getFaction(Factions.PERSEAN);
        	FactionAPI KoC = sector.getFaction("KoC");

/// REP LEVELS: VENGEFUL/HOSTILE/INHOSPITABLE/SUSPICIOUS/NEUTRAL/FAVORABLE/WELCOMING/FRIENDLY/COOPERATIVE

		KoC.setRelationship(Factions.HEGEMONY, RepLevel.COOPERATIVE);
		KoC.setRelationship(Factions.PERSEAN, RepLevel.FRIENDLY);
		KoC.setRelationship(Factions.INDEPENDENT, RepLevel.FRIENDLY);
		KoC.setRelationship(Factions.TRITACHYON, RepLevel.HOSTILE);
		KoC.setRelationship(Factions.LUDDIC_PATH, RepLevel.HOSTILE);
		KoC.setRelationship(Factions.PIRATES, RepLevel.HOSTILE);

		KoC.setRelationship(Factions.PLAYER, RepLevel.FAVORABLE);
    }

}
