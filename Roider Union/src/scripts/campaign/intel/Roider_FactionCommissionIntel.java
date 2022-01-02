package scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.FactionCommissionIntel;
import static com.fs.starfarer.api.impl.campaign.intel.FactionCommissionIntel.UPDATE_PARAM_ACCEPTED;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Commission;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_MemFlags;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Author: SafariJohn
 */
public class Roider_FactionCommissionIntel extends FactionCommissionIntel {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_FactionCommissionIntel.class, "indies", "i");
    }

    private final FactionAPI indies;

    public Roider_FactionCommissionIntel(FactionAPI faction) {
        super(faction);
        indies = Global.getSector().getFaction(Factions.INDEPENDENT);
    }

    @Override
    public void advanceMission(float amount) {
		RepLevel level = indies.getRelToPlayer().getLevel();
		if (!level.isAtWorst(RepLevel.NEUTRAL)) {
			setMissionResult(new MissionResult(-1, null));
			setMissionState(MissionState.COMPLETED);
			endMission();
			sendUpdateIfPlayerHasIntel(missionResult, false);
		} else {
			makeRepChanges(null);
		}
    }

    @Override
    public void missionAccepted() {
		log.info(String.format("Accepted commission with [%s]", faction.getDisplayName(), (int) baseBounty));

		setImportant(true);
		setMissionState(MissionState.ACCEPTED);

		Global.getSector().getIntelManager().addIntel(this, true);
		Global.getSector().getListenerManager().addListener(this);
		Global.getSector().addScript(this);

		Global.getSector().getCharacterData().getMemoryWithoutUpdate().set(Roider_MemFlags.ROIDER_COMMISSION, true);
		Global.getSector().getCharacterData().getMemoryWithoutUpdate().set(MemFlags.FCM_FACTION, indies.getId());
		Global.getSector().getCharacterData().getMemoryWithoutUpdate().set(MemFlags.FCM_EVENT, this);
    }

    @Override
	public void endMission(InteractionDialogAPI dialog) {
		log.info(String.format("Ending commission with [%s]", faction.getDisplayName()));
		Global.getSector().getListenerManager().removeListener(this);
		Global.getSector().removeScript(this);

		Global.getSector().getCharacterData().getMemoryWithoutUpdate().unset(Roider_MemFlags.ROIDER_COMMISSION);
		Global.getSector().getCharacterData().getMemoryWithoutUpdate().unset(MemFlags.FCM_FACTION);
		Global.getSector().getCharacterData().getMemoryWithoutUpdate().unset(MemFlags.FCM_EVENT);

		undoAllRepChanges(dialog);

		endAfterDelay();
	}

    @Override
    public List<FactionAPI> getHostileFactions() {
		List<FactionAPI> hostile = new ArrayList<FactionAPI>();
		for (FactionAPI other : getRelevantFactions()) {
			if (indies.isHostileTo(other)) {
				hostile.add(other);
			}
		}
		return hostile;
    }

    @Override
    public void makeRepChanges(InteractionDialogAPI dialog) {
		FactionAPI player = Global.getSector().getPlayerFaction();
		for (FactionAPI other : getRelevantFactions()) {
			RepChangeData change = repChanges.get(other.getId());

			boolean madeHostile = change != null;
			boolean factionHostile = indies.isHostileTo(other);
			boolean playerHostile = player.isHostileTo(other);

			if (factionHostile && !playerHostile && !madeHostile) {
				makeHostile(other, dialog);
			}

			if (!factionHostile && madeHostile) {
				undoRepChange(other, dialog);
			}
		}
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (isEnded() || isEnding()) return;

		if (!battle.isPlayerInvolved()) return;

		int payment = 0;
		float fpDestroyed = 0;
		for (CampaignFleetAPI otherFleet : battle.getNonPlayerSideSnapshot()) {
			if (!indies.isHostileTo(otherFleet.getFaction())) continue;

			float bounty = 0;
			for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(otherFleet)) {
				float mult = Misc.getSizeNum(loss.getHullSpec().getHullSize());
				bounty += mult * baseBounty;
				fpDestroyed += loss.getFleetPointCost();
			}

			payment += (int) (bounty * battle.getPlayerInvolvementFraction());
		}

		if (payment > 0) {
			float repFP = (int)(fpDestroyed * battle.getPlayerInvolvementFraction());
			ReputationActionResponsePlugin.ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
							new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.COMMISSION_BOUNTY_REWARD, new Float(repFP), null, null, true, false),
							indies.getId());
			latestResult = new CommissionBountyResult(payment, battle.getPlayerInvolvementFraction(), rep);
			sendUpdateIfPlayerHasIntel(latestResult, false);
		}
    }

    @Override
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {

		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;

		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;

		Color tc = getBulletColorForMode(mode);

		bullet(info);
		boolean isUpdate = getListInfoParam() != null;

		if (getListInfoParam() == UPDATE_PARAM_ACCEPTED) {
			return;
		}

		if (missionResult != null && missionResult.payment < 0) {
//			info.addPara("Annulled by " + faction.getDisplayNameWithArticle(), initPad, tc,
//					faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
		} else if (isUpdate && latestResult != null) {
			info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(latestResult.payment));
			if (Math.round(latestResult.fraction * 100f) < 100f) {
				info.addPara("%s share based on damage dealt", 0f, tc, h,
						"" + (int) Math.round(latestResult.fraction * 100f) + "%");
			}
			CoreReputationPlugin.addAdjustmentMessage(latestResult.rep1.delta, indies, null,
													  null, null, info, tc, isUpdate, 0f);
		} else if (mode == ListInfoMode.IN_DESC) {
			info.addPara("%s base reward per frigate", initPad, tc, h, Misc.getDGSCredits(baseBounty));
			info.addPara("%s monthly stipend", 0f, tc, h, Misc.getDGSCredits(computeStipend()));
		} else {
//			info.addPara("Faction: " + faction.getDisplayName(), initPad, tc,
//					faction.getBaseUIColor(), faction.getDisplayName());
//			initPad = 0f;
			info.addPara("%s base reward per frigate", initPad, tc, h, Misc.getDGSCredits(baseBounty));
			info.addPara("%s monthly stipend", 0f, tc, h, Misc.getDGSCredits(computeStipend()));
		}
		unindent(info);
	}

    @Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height,
									   boolean forMarketConditionTooltip) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		info.addImage(faction.getLogo(), width, 128, opad);


		if (isEnding()) {
			if (missionResult != null && missionResult.payment < 0) {
				info.addPara("Your commission was annulled by " + faction.getDisplayNameWithArticle() +
						" due to your standing falling too low.",
						 opad, faction.getBaseUIColor(),
						 faction.getDisplayNameWithArticleWithoutArticle());

				CoreReputationPlugin.addRequiredStanding(indies, Commission.COMMISSION_REQ, null, null, info, tc, opad, true);
				CoreReputationPlugin.addCurrentStanding(indies, null, null, info, tc, opad);
			} else {
				info.addPara("You've resigned your commission with " + faction.getDisplayNameWithArticle() +
						".",
						 opad, faction.getBaseUIColor(),
						 faction.getDisplayNameWithArticleWithoutArticle());
			}
		} else {
			info.addPara("You've accepted a %s commission.",
					opad, faction.getBaseUIColor(), Misc.ucFirst(faction.getPersonNamePrefix()));

			addBulletPoints(info, ListInfoMode.IN_DESC);

			info.addPara("The combat bounty payment depends on the number and size of ships destroyed.", opad);
		}

		if (latestResult != null) {
			//Color color = faction.getBaseUIColor();
			//Color dark = faction.getDarkUIColor();
			//info.addSectionHeading("Most Recent Reward", color, dark, Alignment.MID, opad);
			info.addPara("Most recent bounty:", opad);
			bullet(info);
			info.addPara("%s received", opad, tc, h, Misc.getDGSCredits(latestResult.payment));
			if (Math.round(latestResult.fraction * 100f) < 100f) {
				info.addPara("%s share based on damage dealt", 0f, tc, h,
						"" + (int) Math.round(latestResult.fraction * 100f) + "%");
			}
			CoreReputationPlugin.addAdjustmentMessage(latestResult.rep1.delta, faction, null,
													  null, null, info, tc, false, 0f);
			unindent(info);
		}

		if (!isEnding() && !isEnded()) {
			addAbandonButton(info, width, "Resign commission");
		}
	}

    @Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.remove(Tags.INTEL_ACCEPTED);
		tags.add(Tags.INTEL_COMMISSION);
		tags.add(indies.getId());
		return tags;
	}

    @Override
	public MissionResult createResignedCommissionResult(boolean withPenalty, boolean inPerson, InteractionDialogAPI dialog) {
//		if (withPenalty) {
//			CoreReputationPlugin.CustomRepImpact impact = new CoreReputationPlugin.CustomRepImpact();
//			impact.delta = -1f * Global.getSettings().getFloat("factionCommissionResignPenalty");
//			if (inPerson) {
//				impact.delta = -1f * Global.getSettings().getFloat("factionCommissionResignPenaltyInPerson");
//			}
//			ReputationActionResponsePlugin.ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
//					new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM,
//							impact, null, dialog != null ? dialog.getTextPanel() : null, false, true),
//							indies.getId());
//			return new MissionResult();
//		}
		return new MissionResult();
	}

    @Override
    public void reportEconomyTick(int iterIndex) {
		float numIter = Global.getSettings().getFloat("economyIterPerMonth");
		float f = 1f / numIter;

		//CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		MonthlyReport report = SharedData.getData().getCurrentReport();

		MonthlyReport.FDNode fleetNode = report.getNode(MonthlyReport.FLEET);
		fleetNode.name = "Fleet";
		fleetNode.custom = MonthlyReport.FLEET;
		fleetNode.tooltipCreator = report.getMonthlyReportTooltip();

		float stipend = computeStipend();
		MonthlyReport.FDNode stipendNode = report.getNode(fleetNode, "node_id_stipend_" + faction.getId());
		stipendNode.income += stipend * f;

		if (stipendNode.name == null) {
			stipendNode.name = faction.getDisplayName() + " Commission";
			stipendNode.icon = faction.getCrest();
			stipendNode.tooltipCreator = new TooltipMakerAPI.TooltipCreator() {
				public boolean isTooltipExpandable(Object tooltipParam) {
					return false;
				}
				public float getTooltipWidth(Object tooltipParam) {
					return 450;
				}
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					tooltip.addPara("Your monthly stipend for holding a " + faction.getDisplayName() + " commission", 0f);
				}
			};
		}
    }

}
