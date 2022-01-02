package scripts.campaign.retrofit;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.combat.CombatReadinessPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Hullmods;
import java.util.List;

public class Roider_ArgosConversionAbility extends BaseDurationAbility {

    public static void aliasAttributes(XStream x) {
    }

	public static final float SENSOR_RANGE_BONUS = 3000f;
	public static final float DETECTABILITY_RANGE_BONUS = 5000f;
	public static final float ACCELERATION_MULT = 4f;

	@Override
	protected void activateImpl() {
        if (!isUsable()) return;

		if (entity.isPlayerFleet()) {
            SectorAPI sector = Global.getSector();

            // trigger dialog
            // dialog handles which Argosi are usable and such
            List<String> offerings = null;

            Roider_ArgosRetrofitPlugin plugin = new Roider_ArgosRetrofitPlugin(
                        null,
                        new Roider_ArgosRetrofitManager(entity, sector.getPlayerFaction(), offerings),
                        null);
//                        new HashMap<String, MemoryAPI>());

            sector.getCampaignUI().showInteractionDialog(plugin, entity);
		}
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
	}

	@Override
	public boolean isUsable() {
        if (!entity.isPlayerFleet()) return false;

        CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
        // Must not be hostile fleets nearby
        if (isHostileNearbyAndAware(fleet)) return false;

        // Must have Argosi above malfunction level
        // And must have Argosi!
        CombatReadinessPlugin crPlugin = Global.getSettings().getCRPlugin();
        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy()) {
            if (ship.getVariant().hasHullMod(Roider_Hullmods.CONVERSION_DOCK)) {
                if (ship.isMothballed()) continue;
                float mal = crPlugin.getMalfunctionThreshold(ship.getStats());
                float cr = ship.getRepairTracker().getCR();

                if (cr <= mal) continue;

                return true;
            }
        }

        return false;
	}

    public boolean isHostileNearbyAndAware(CampaignFleetAPI playerFleet) {
		for (CampaignFleetAPI fleet : playerFleet.getContainingLocation().getFleets()) {
			if (fleet.getAI() == null) continue; // dormant Remnant fleets
			if (fleet.getFaction().isPlayerFaction()) continue;
			if (fleet.isStationMode()) continue;

			if (!fleet.isHostileTo(playerFleet)) continue;
			if (fleet.getBattle() != null) continue;


			SectorEntityToken.VisibilityLevel level = playerFleet.getVisibilityLevelTo(fleet);
//			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
//			if (!mem.contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF) &&
//					!mem.contains(MemFlags.MEMORY_KEY_PURSUE_PLAYER)) {
//				if (level == VisibilityLevel.NONE) continue;
//			}
			if (level == SectorEntityToken.VisibilityLevel.NONE) continue;

			if (fleet.getFleetData().getMembersListCopy().isEmpty()) continue;

			float dist = Misc.getDistance(playerFleet.getLocation(), fleet.getLocation());
			if (dist > 1500f) continue;

			//fleet.getAI().pickEncounterOption(null, playerFleet, true);
			if (fleet.getAI() instanceof ModularFleetAIAPI) {
				ModularFleetAIAPI ai = (ModularFleetAIAPI) fleet.getAI();
				if (ai.getTacticalModule() != null &&
						(ai.getTacticalModule().isFleeing() || ai.getTacticalModule().isMaintainingContact() ||
								ai.getTacticalModule().isStandingDown())) {
					continue;
				}
			}

			return true;
		}

		return false;
    }

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		tooltip.addTitle(spec.getName());

		float pad = 10f;
		tooltip.addPara("Convert certain ships to certain Roider ships.", pad);

        if (!entity.isPlayerFleet()) return;

        boolean noDocks = true;
        boolean notReady = true;
        CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
        CombatReadinessPlugin crPlugin = Global.getSettings().getCRPlugin();
        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy()) {
            if (ship.getVariant().hasHullMod(Roider_Hullmods.CONVERSION_DOCK)) {
                noDocks = false;

                if (ship.isMothballed()) continue;
                float mal = crPlugin.getMalfunctionThreshold(ship.getStats());
                float cr = ship.getRepairTracker().getCR();

                if (cr > mal) {
                    notReady = false;
                    break;
                }
            }
        }

		if (noDocks) {
			tooltip.addPara("Your fleet has no conversion docks.", Misc.getNegativeHighlightColor(), pad);
		} else if (notReady) {
			tooltip.addPara("Your fleet has conversion docks, but they are too low on CR.", Misc.getNegativeHighlightColor(), pad);
		} else if (isHostileNearbyAndAware(fleet)) {
			tooltip.addPara("A nearby hostile fleet is tracking your movements, making conversion impossible.", Misc.getNegativeHighlightColor(), pad);
        } else {
			tooltip.addPara("Your fleet is ready to do conversions.", Misc.getPositiveHighlightColor(), pad);
		}

		addIncompatibleToTooltip(tooltip, expanded);
	}

	public boolean hasTooltip() {
		return true;
	}

    @Override
    protected void applyEffect(float amount, float level) {
    }

    @Override
    protected void deactivateImpl() {
    }

    @Override
    protected void cleanupImpl() {
    }
}





