package data.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.Global;
import org.magiclib.util.MagicSettings;

public class apocritaGen implements SectorGeneratorPlugin {
	@Override
	
    public void generate(SectorAPI sector) {
		//new calcari().generate(sector);
		
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
        FactionAPI apocrita_association = sector.getFaction("apocrita_association");

/// REP LEVELS: VENGEFUL/HOSTILE/INHOSPITABLE/SUSPICIOUS/NEUTRAL/FAVORABLE/WELCOMING/FRIENDLY/COOPERATIVE

		apocrita_association.setRelationship(Factions.HEGEMONY, RepLevel.HOSTILE);
		apocrita_association.setRelationship(Factions.PERSEAN, RepLevel.FRIENDLY);
		apocrita_association.setRelationship(Factions.TRITACHYON, RepLevel.SUSPICIOUS);
		apocrita_association.setRelationship(Factions.LUDDIC_PATH, RepLevel.HOSTILE);
		apocrita_association.setRelationship(Factions.LUDDIC_CHURCH, RepLevel.INHOSPITABLE);
		apocrita_association.setRelationship(Factions.PIRATES, RepLevel.HOSTILE);
		apocrita_association.setRelationship(Factions.PLAYER, RepLevel.NEUTRAL);
    }

}
