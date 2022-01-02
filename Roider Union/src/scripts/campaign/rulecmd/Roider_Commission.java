package scripts.campaign.rulecmd;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.FactionCommissionIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Commission;
import static com.fs.starfarer.api.impl.campaign.rulecmd.missions.Commission.COMMISSION_REQ;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.AllianceManager;
import exerelin.campaign.alliances.Alliance;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_Ranks;
import scripts.campaign.intel.Roider_FactionCommissionIntel;
import java.awt.Color;
import java.util.List;
import scripts.Roider_ModPlugin;

/**
 * Author: SafariJohn
 */
public class Roider_Commission extends Commission {
//    @Override
//    protected void accept() {
//		if (Misc.getCommissionFactionId() == null) {
//			FactionCommissionIntel intel = new Roider_FactionCommissionIntel(faction);
//			intel.missionAccepted();
//			intel.sendUpdate(FactionCommissionIntel.UPDATE_PARAM_ACCEPTED, dialog.getTextPanel());
//			intel.makeRepChanges(dialog);
//		}
//    }

//    @Override
//    protected boolean hasFactionCommission() {
//        boolean roiderCommission = Global.getSector().getCharacterData().getMemoryWithoutUpdate()
//                    .getBoolean(Roider_MemFlags.ROIDER_COMMISSION);
//        return Factions.INDEPENDENT.equals(Misc.getCommissionFactionId()) && roiderCommission;
//    }


    @Override
	protected boolean personCanGiveCommission() {
		if (person == null) return false;
		if (!faction.getId().equals(Roider_Factions.ROIDER_UNION)) return false;

        if (Roider_ModPlugin.hasNexerelin) {
            Alliance ally = AllianceManager.getPlayerAlliance(false);
            if (ally != null) return false;
        }

		//if (Misc.getCommissionFactionId() != null) return false;

		return Roider_Ranks.POST_BASE_COMMANDER.equals(person.getPostId());
	}

    @Override
	protected void printInfo() {
        if (true) return;

		TooltipMakerAPI info = dialog.getTextPanel().beginTooltip();

		FactionCommissionIntel temp = new Roider_FactionCommissionIntel(faction);

		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;

		info.setParaSmallInsignia();

		int stipend = (int) temp.computeStipend();

		info.addPara("At your experience level, you would receive a %s monthly stipend, as well as a modest bounty for destroying enemy ships.",
				0f, h, Misc.getDGSCredits(stipend));

		List<FactionAPI> hostile = temp.getHostileFactions();
		if (hostile.isEmpty()) {
			info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " is not currently hostile to any major factions.", 0f);
		} else {
			info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " is currently hostile to:", opad);

			info.setParaFontDefault();

			info.setBulletedListMode(BaseIntelPlugin.INDENT);
			float initPad = opad;
			for (FactionAPI other : hostile) {
				info.addPara(Misc.ucFirst(other.getDisplayName()), other.getBaseUIColor(), initPad);
				initPad = 3f;
			}
			info.setBulletedListMode(null);
		}


		dialog.getTextPanel().addTooltip();
	}


//    @Override
//	protected boolean playerMeetsCriteria() {
//		return Global.getSector().getFaction(Factions.INDEPENDENT).getRelToPlayer().isAtWorst(COMMISSION_REQ);
//	}
    @Override
	protected void printRequirements() {
		CoreReputationPlugin.addRequiredStanding(faction, COMMISSION_REQ, null, dialog.getTextPanel(), null, null, 0f, true);
		CoreReputationPlugin.addCurrentStanding(faction, null, dialog.getTextPanel(), null, null, 0f);
	}
//    @Override
//	protected void printRequirements() {
//        FactionAPI indies = Global.getSector().getFaction(Factions.INDEPENDENT);
//		CoreReputationPlugin.addRequiredStanding(indies, COMMISSION_REQ, null, dialog.getTextPanel(), null, null, 0f, true);
//		CoreReputationPlugin.addCurrentStanding(indies, null, dialog.getTextPanel(), null, null, 0f);
//	}

}
