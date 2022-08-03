package scripts.campaign.retrofit;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.InteractionDialogImageVisual;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import exerelin.campaign.AllianceManager;
import exerelin.campaign.PlayerFactionStore;
import exerelin.utilities.NexUtilsFaction;
import scripts.campaign.retrofit.Roider_BaseRetrofitManager.RetrofitTracker;
import java.awt.Color;
import java.util.*;
import org.lwjgl.input.Keyboard;
import scripts.Roider_ModPlugin;
import scripts.campaign.retrofit.Roider_BaseRetrofitPlugin.OptionId;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper.RetrofitData;

/**
 * Author: SafariJohn
 */
public class Roider_BaseRetrofitPlugin implements InteractionDialogPlugin {

    protected static enum OptionId {
        PICK_TARGET,
        PICK_SHIPS,
        PRIORITIZE,
        CANCEL_SHIPS,
        LEAVE,
        CONFIRM,
        CANCEL
    }

    protected static final int COLUMNS = 7;

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_BaseRetrofitPlugin.class, "originalPlugin", "op");
        x.aliasAttribute(Roider_BaseRetrofitPlugin.class, "manager", "m");
        x.aliasAttribute(Roider_BaseRetrofitPlugin.class, "memoryMap", "mm");
        x.aliasAttribute(Roider_BaseRetrofitPlugin.class, "dialog", "d");
        x.aliasAttribute(Roider_BaseRetrofitPlugin.class, "text", "t");
        x.aliasAttribute(Roider_BaseRetrofitPlugin.class, "options", "o");
        x.aliasAttribute(Roider_BaseRetrofitPlugin.class, "visual", "v");
        x.aliasAttribute(Roider_BaseRetrofitPlugin.class, "retrofits", "r");
        x.aliasAttribute(Roider_BaseRetrofitPlugin.class, "selectedRetrofit", "s");
    }

    protected final InteractionDialogPlugin originalPlugin;
    protected final Roider_BaseRetrofitManager manager;
    protected final Map<String, MemoryAPI> memoryMap;

	protected InteractionDialogAPI dialog;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;

    protected List<RetrofitData> retrofits;
    protected FleetMemberAPI selectedRetrofit = null;

    public Roider_BaseRetrofitPlugin(InteractionDialogPlugin originalPlugin,
                Roider_BaseRetrofitManager manager, final Map<String, MemoryAPI> memoryMap) {
        this.originalPlugin = originalPlugin;
        this.manager = manager;
        this.memoryMap = memoryMap;

        retrofits = manager.getRetrofits();
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

        updateText();
        updateOptions();
        updateVisual();

    }

    protected void updateText() {
        text.clear();

        // Update queued retrofits
        if (!manager.getQueued().isEmpty()) {
            // Show time remaining for first hull
            StringBuilder costs = new StringBuilder();
            List<String> highlights = new ArrayList<>();

            RetrofitTracker first = manager.getQueued().get(0);
            ShipHullSpecAPI firstSpec = null;
            ShipHullSpecAPI firstTargetSpec = null;
            for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
                if (matchesHullId(spec, first.data.sourceHull)) {
                    firstSpec = spec;
                }

                if (spec.getHullId().equals(first.data.targetHull)) {
                    firstTargetSpec = spec;
                }

                if (firstSpec != null && firstTargetSpec != null) break;
            }

            if (firstSpec == null || firstTargetSpec == null) {
                text.addPara("Could not find ShipHullSpecAPI for " + first.data.targetHull, Misc.getNegativeHighlightColor());
                return;
            }

            costs.append(firstSpec.getNameWithDesignationWithDashClass()).append("\n");
            costs.append("to ").append(firstTargetSpec.getNameWithDesignationWithDashClass()).append("\n");
            if ((int) first.getDaysRemaining() == 1) {
                costs.append("- Complete in %s day\n");
            } else {
                costs.append("- Complete in %s days\n");
            }

            highlights.add("" + (int) first.getDaysRemaining());

            // Give projected completion date of all queued hulls
            if (manager.getQueued().size() > 1) {
                int days = 0;
                for (RetrofitTracker tracker : manager.getQueued()) {
                    days += tracker.getDaysRemaining();
                }

//                long timestamp = Global.getSector().getClock().getTimestamp();
//                timestamp += 1000 * Global.getSector().getClock().convertToSeconds(days);
//                CampaignClockAPI completeDate = Global.getSector().getClock().createClock(timestamp);

                if (days == 1) {
                    costs.append("\nAll queued retrofits will be complete in %s day\n");
                } else {
                    costs.append("\nAll queued retrofits will be complete in %s days\n");
                }

                highlights.add("" + days);
            }

            String[] hl = {""};
            text.addPara(costs.toString(), Misc.getHighlightColor(), highlights.toArray(hl));

            CampaignFleetAPI playerHulls = FleetFactoryV3.createEmptyFleet(
                        Global.getSector().getPlayerFaction().getId(),
                        FleetTypes.MERC_PRIVATEER, null);
            CampaignFleetAPI retrofitHulls = FleetFactoryV3.createEmptyFleet(
                        manager.getFaction().getId(),
                        FleetTypes.MERC_PRIVATEER, null);

            for (RetrofitTracker t : manager.getQueued()) {
                playerHulls.getFleetData().addFleetMember(t.ship);
                retrofitHulls.getFleetData().addFleetMember(t.data.targetHull + "_Hull");
            }

            List<FleetMemberAPI> playerMembers = playerHulls.getFleetData().getMembersListCopy();
            List<FleetMemberAPI> retrofitMembers = retrofitHulls.getFleetData().getMembersListCopy();

            // Match names
            for (int i = 0; i < playerMembers.size(); i++) {
                String name = playerMembers.get(i).getShipName();
                retrofitMembers.get(i).setShipName(name);
            }

            List<FleetMemberAPI> members = new ArrayList<>();
            members.addAll(retrofitMembers);

            TooltipMakerAPI targetTooltip = text.beginTooltip();
            targetTooltip.addTitle("Queued Retrofits");
            int rows = (retrofitMembers.size() / COLUMNS) + 1;
            float iconSize = dialog.getTextWidth() / COLUMNS;

            float pad = 0f; // 10f
            Color color = manager.getFaction().getBaseUIColor();
            targetTooltip.addShipList(COLUMNS, rows, iconSize, color, members, pad);
            text.addTooltip();
        }

        // List targets if nothing queued
        if (selectedRetrofit == null) {
            text.addPara("Please select a hull to retrofit to.", Misc.getButtonTextColor());

            if (manager.getQueued().isEmpty()) {
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                Set<String> playerHulls = new HashSet<>();
                for (FleetMemberAPI m : playerFleet.getMembersWithFightersCopy()) {
                    playerHulls.add(m.getHullSpec().getHullId());
                    playerHulls.add(m.getHullSpec().getBaseHullId());
                }


                List<String> targets = new ArrayList<>();
                List<ShipHullSpecAPI> specs = new ArrayList<>();
                List<ShipHullSpecAPI> specsAvailable = new ArrayList<>();
                List<ShipHullSpecAPI> specsIllegal = new ArrayList<>();
                List<ShipHullSpecAPI> specsNotAllowed = new ArrayList<>();
                for (RetrofitData data : retrofits) {
                    if (targets.contains(data.targetHull)) continue;

                    specs.add(Global.getSettings().getHullSpec(data.targetHull));
                    targets.add(data.targetHull);
                }

                // Find which targets the player has source ships for
                // If player has at least one source hull for target, include spec in allowed hulls
                targets.clear();
                for (RetrofitData data : retrofits) {
                    if (targets.contains(data.targetHull)) continue;

                    if (playerHulls.contains(data.sourceHull)) {
                        specsAvailable.add(Global.getSettings().getHullSpec(data.targetHull));
                        targets.add(data.targetHull);
                    }
                }

                // Find which targets are illegal for the player
                // or not allowed
                for (ShipHullSpecAPI spec : specs) {
                    // Temp retrofit
                    selectedRetrofit = Global.getFactory().createFleetMember(
                                FleetMemberType.SHIP, spec.getHullId() + "_Hull");

                    if (!isAllowed()) {
                        specsNotAllowed.add(spec);
                    } else if (isIllegal()) {
                        specsIllegal.add(spec);
                    }

                    // Remove temp retrofit
                    selectedRetrofit = null;
                }

                specs.removeAll(specsAvailable);
                specs.removeAll(specsIllegal);
                specs.removeAll(specsNotAllowed);
                specsAvailable.removeAll(specsIllegal);
                specsAvailable.removeAll(specsNotAllowed);

                sortShipSpecs(specsAvailable);
                sortShipSpecs(specs);
                sortShipSpecs(specsIllegal);
                sortShipSpecs(specsNotAllowed);

                StringBuilder available = new StringBuilder();

                boolean first = true;
                for (ShipHullSpecAPI spec : specsAvailable) {
                    if (first) first = false;
                    else available.append("\n");

                    available.append(spec.getNameWithDesignationWithDashClass());
                }
                if (available.length() > 0) showCatalog("Available Retrofits", specsAvailable, manager.getFaction().getBaseUIColor());
//                if (available.length() > 0) text.addPara(available.toString());

                StringBuilder unavailable = new StringBuilder();

                first = true;
                for (ShipHullSpecAPI spec : specs) {
                    if (first) first = false;
                    else unavailable.append("\n");

                    unavailable.append(spec.getNameWithDesignationWithDashClass());
                }
                if (unavailable.length() > 0) showCatalog("Unavailable Retrofits", specs, Misc.getGrayColor());
//                if (unavailable.length() > 0) text.addPara(unavailable.toString(), Misc.getGrayColor());

                StringBuilder illegal = new StringBuilder();

                first = true;
                for (ShipHullSpecAPI spec : specsIllegal) {
                    if (first) first = false;
                    else illegal.append("\n");

                    illegal.append(spec.getNameWithDesignationWithDashClass());
                }
                if (illegal.length() > 0) showCatalog("Illegal Retrofits", specsIllegal, Misc.getNegativeHighlightColor());
//                if (illegal.length() > 0) text.addPara(illegal.toString(), Misc.getNegativeHighlightColor());

                StringBuilder notAllowed = new StringBuilder();

                first = true;
                for (ShipHullSpecAPI spec : specsNotAllowed) {
                    if (first) first = false;
                    else notAllowed.append("\n");

                    notAllowed.append(spec.getNameWithDesignationWithDashClass());
                }
                if (notAllowed.length() > 0) showCatalog(getNotAllowedRetrofitsTitle(), specsNotAllowed, Misc.getNegativeHighlightColor());
//                if (illegal.length() > 0) text.addPara(illegal.toString(), Misc.getNegativeHighlightColor());

            }

        } else {
            text.addPara("Retrofitting to: " + selectedRetrofit.getVariant()
                        .getFullDesignationWithHullName(), Misc.getButtonTextColor());

            StringBuilder costs = new StringBuilder();
            List<String> highlights = new ArrayList<>();

            StringBuilder illegalCosts = new StringBuilder();

            boolean firstIllegal = true;
            boolean firstLegal = true;

            List<String> matched = new ArrayList<>();

            // Check for specific matches
            for (RetrofitData data : retrofits) {
                if (!data.targetHull.equals(selectedRetrofit.getHullId())) continue;

                for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
                    if (matched.contains(spec.getHullId())) continue;

                    if (spec.isDHull() && !matchesHullId(spec.getHullId(), data.sourceHull)) continue;
                    if (spec.getHullId().equals(selectedRetrofit.getHullId())) continue;

                    if (matchesHullId(spec.getHullId(), data.sourceHull)) {
                        if (!isAllowed(data.sourceHull)) {
                            if (firstIllegal) firstIllegal = false;
                            else illegalCosts.append("\n");

                            illegalCosts.append(spec.getNameWithDesignationWithDashClass()).append("\n");
                            appendIllegalsCosts(illegalCosts, (int) data.cost, (int) data.time);
                            illegalCosts.append(getNotAllowedRetrofitText(data.sourceHull));
                        } else if (isIllegal(data.sourceHull)) {
                            if (firstIllegal) firstIllegal = false;
                            else illegalCosts.append("\n");

                            illegalCosts.append(spec.getNameWithDesignationWithDashClass()).append("\n");
                            appendIllegalsCosts(illegalCosts, (int) data.cost, (int) data.time);
                            illegalCosts.append(getIllegalRetrofitText(data.sourceHull));
                        } else {
                            if (firstLegal) firstLegal = false;
                            else costs.append("\n");

                            costs.append(spec.getNameWithDesignationWithDashClass()).append("\n");
                            appendLegalCosts(costs, highlights, (int) data.cost, (int) data.time);
                        }

                        matched.add(spec.getHullId());
                        matched.add(spec.getHullName());
                        break;
                    }
                }
            }

            // Check for general matches
            for (RetrofitData data : retrofits) {
                if (!data.targetHull.equals(selectedRetrofit.getHullId())) continue;

                for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
                    if (matched.contains(spec.getHullId())) continue;
                    if (matched.contains(spec.getHullName())) continue;

                    if (spec.isDHull() && !matchesHullId(spec.getHullId(), data.sourceHull)) continue;
                    if (spec.getHullId().equals(selectedRetrofit.getHullId())) continue;

                    if (matchesHullId(spec, data.sourceHull)) {
                        if (!isAllowed(data.sourceHull)) {
                            if (firstIllegal) firstIllegal = false;
                            else illegalCosts.append("\n");

                            illegalCosts.append(spec.getNameWithDesignationWithDashClass()).append("\n");
                            appendIllegalsCosts(illegalCosts, (int) data.cost, (int) data.time);
                            illegalCosts.append(getNotAllowedRetrofitText(data.sourceHull));
                        } else if (isIllegal(data.sourceHull)) {
                            if (firstIllegal) firstIllegal = false;
                            else illegalCosts.append("\n");

                            illegalCosts.append(spec.getNameWithDesignationWithDashClass()).append("\n");
                            appendIllegalsCosts(illegalCosts, (int) data.cost, (int) data.time);
                            illegalCosts.append(getIllegalRetrofitText(data.sourceHull));
                        } else {
                            if (firstLegal) firstLegal = false;
                            else costs.append("\n");

                            costs.append(spec.getNameWithDesignationWithDashClass()).append("\n");
                            appendLegalCosts(costs, highlights, (int) data.cost, (int) data.time);
                        }
                    }
                }
            }

            String[] hl = {""};

            if (costs.length() > 0) text.addPara(costs.toString(),
                        Misc.getHighlightColor(), highlights.toArray(hl));
            if (illegalCosts.length() > 0) text.addPara(illegalCosts.toString(),
                        Misc.getNegativeHighlightColor(), illegalCosts.toString());
        }
    }

    protected void showCatalog(String name, List<ShipHullSpecAPI> specs, Color color) {
        CampaignFleetAPI retrofitHulls = FleetFactoryV3.createEmptyFleet(
                    manager.getFaction().getId(),
                    FleetTypes.MERC_PRIVATEER, null);

        List<ShipHullSpecAPI> included = new ArrayList<>();
        for (ShipHullSpecAPI spec : specs) {
            if (included.contains(spec)) continue;

            retrofitHulls.getFleetData().addFleetMember(spec.getHullId() + "_Hull");
            included.add(spec);
        }

        List<FleetMemberAPI> retrofitMembers = retrofitHulls.getFleetData().getMembersListCopy();

        // Match names
        for (FleetMemberAPI m : retrofitMembers) {
            m.setShipName("Retrofit to");
            m.getRepairTracker().setCR(0.7f); // Looks cleaner
        }

        List<FleetMemberAPI> members = new ArrayList<>();
        members.addAll(retrofitMembers);

        TooltipMakerAPI targetTooltip = dialog.getTextPanel().beginTooltip();
        targetTooltip.addTitle(name);
        int rows = 1 + (retrofitMembers.size() - 1) / COLUMNS;
        float iconSize = dialog.getTextWidth() / COLUMNS;

        float pad = 0f; // 10f
//        Color color = manager.getFaction().getBaseUIColor();
        targetTooltip.addShipList(COLUMNS, rows, iconSize, color, members, pad);
        text.addTooltip();
    }

    private void appendIllegalsCosts(StringBuilder builder, int cost, int time) {
        if (cost > 0 && time > 0) {
            builder.append("- Costs ").append(Misc.getDGSCredits(cost))
                    .append(" and takes ").append(time).append(" days\n");
        } else if (cost < 0 && time > 0) {
            builder.append("- Pays ").append(Misc.getDGSCredits(cost))
                    .append(" and takes ").append(time).append(" days\n");
        } else if (cost > 0) {
            builder.append("- Costs ").append(Misc.getDGSCredits(cost)).append("\n");
        } else if (cost < 0) {
            builder.append("- Pays ").append(Misc.getDGSCredits(cost)).append("\n");
        } else if (time > 0) {
            builder.append("- Takes ").append(time).append(" days\n");
        } else {
            builder.append("- Free\n");
        }
    }

    private void appendLegalCosts(StringBuilder costs, List<String> highlights, int cost, int time) {
        if (cost > 0 && time > 0) {
            costs.append("- Costs %s and takes %s days");
            highlights.add(Misc.getDGSCredits(cost));
            highlights.add("" + time);
        } else if (cost < 0 && time > 0) {
            costs.append("- Pays %s and takes %s days");
            highlights.add(Misc.getDGSCredits(cost));
            highlights.add("" + time);
        } else if (cost > 0) {
            costs.append("- Costs %s");
            highlights.add(Misc.getDGSCredits(cost));
        } else if (cost < 0) {
            costs.append("- Pays %s");
            highlights.add(Misc.getDGSCredits(cost));
        } else if (time > 0) {
            costs.append("- Takes %s days");
            highlights.add("" + time);
        } else {
            costs.append("- %s");
            highlights.add("Free");
        }
    }

    protected void sortShipSpecs(List<ShipHullSpecAPI> specs) {
        // Sort by name
        Collections.sort(specs, new Comparator<ShipHullSpecAPI>() {
            @Override
            public int compare(ShipHullSpecAPI o1, ShipHullSpecAPI o2) {
                return o1.getNameWithDesignationWithDashClass().compareTo(o2.getNameWithDesignationWithDashClass());
            }
        });


        // Sort by size
        Collections.sort(specs, new Comparator<ShipHullSpecAPI>() {
            @Override
            public int compare(ShipHullSpecAPI o1, ShipHullSpecAPI o2) {
                HullSize size1 = o1.getHullSize();
                HullSize size2 = o2.getHullSize();

                return size1.compareTo(size2);
            }
        });
    }

    protected void updateOptions() {
        options.clearOptions();
        options.addOption("Pick retrofit hull", OptionId.PICK_TARGET);

        String queue = "Retrofit ships";
        if (!manager.getQueued().isEmpty()) queue = "Queue retrofits";
        options.addOption(queue, OptionId.PICK_SHIPS);

        if (!manager.getQueued().isEmpty()) {
            options.addOption("Prioritize retrofit", OptionId.PRIORITIZE);
            options.addOption("Cancel retrofits", OptionId.CANCEL_SHIPS);
        }

        options.addOption(getLeaveOptionText(), OptionId.LEAVE);
        options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);

        if (retrofits.isEmpty()) {
            // Should never happen normally.
            options.setEnabled(OptionId.PICK_TARGET, false);
            options.setTooltip(OptionId.PICK_TARGET, "No possible retrofits!");
        }

        if (selectedRetrofit == null) {
            options.setTooltip(OptionId.PICK_SHIPS, "Please select a hull to retrofit to.");
            options.setEnabled(OptionId.PICK_SHIPS, false);
        } else {
            if (!isAllowed()) {
                options.setTooltip(OptionId.PICK_SHIPS, getNotAllowedRetrofitText(null));
                options.setTooltipHighlightColors(OptionId.PICK_SHIPS, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
                options.setTooltipHighlights(OptionId.PICK_SHIPS, getNotAllowedRetrofitTextHighlights(null).getText());
                options.setEnabled(OptionId.PICK_SHIPS, false);

            } else if (isIllegal()) { // Check if retrofit is blocked by reputation or commission
                options.setTooltip(OptionId.PICK_SHIPS, getIllegalRetrofitText(null));
                options.setTooltipHighlightColors(OptionId.PICK_SHIPS, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
                options.setTooltipHighlights(OptionId.PICK_SHIPS, getIllegalRetrofitTextHighlights(null).getText());
                options.setEnabled(OptionId.PICK_SHIPS, false);

            } else // and that there are ships available
                if (getAvailableShips().isEmpty()) {
                options.setTooltip(OptionId.PICK_SHIPS, "You have no ships that can retrofit to " + selectedRetrofit.getHullSpec().getHullNameWithDashClass() + ".");
                options.setEnabled(OptionId.PICK_SHIPS, false);
            } else {
                options.setEnabled(OptionId.PICK_SHIPS, true);
            }
        }

        boolean queueEmpty = !manager.getQueued().isEmpty();
        options.setEnabled(OptionId.PRIORITIZE, queueEmpty);
        options.setEnabled(OptionId.CANCEL_SHIPS, queueEmpty);
    }

    protected String getLeaveOptionText() {
        return "Leave";
    }

    /**
     * Checks for selectedRetrofit.
     * @return
     */
	protected boolean isIllegal() {
		RepLevel req = getRequiredLevel();
		if (req == null) return false;

		RepLevel level = manager.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));

		boolean legal = level.isAtWorst(req);
		if (requiresCommission()) {
			legal &= hasCommission();
		}

		return !legal;
	}

    /**
     * @param sourceHull
     * @return
     */
	protected boolean isIllegal(String sourceHull) {
		RepLevel req = getRequiredLevel(sourceHull);
		if (req == null) return false;

		RepLevel level = manager.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));

		boolean legal = level.isAtWorst(req);
		if (requiresCommission(sourceHull)) {
			legal &= hasCommission();
		}

		return !legal;
	}

	protected String getIllegalRetrofitText(String hullId) {
		RepLevel req;
        if (hullId == null) req = getRequiredLevel();
        else req = getRequiredLevel(hullId);

		if (req != null) {
			String str = "";
			RepLevel level = manager.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
            if (hullId == null) {
                if (!level.isAtWorst(req)) {
                    str += "Req: " + manager.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();
                }

                if (requiresCommission() && !hasCommission()) {
                    if (!str.isEmpty()) str += "\n";
                    str += "Req: " + manager.getFaction().getDisplayName() + " - " + "commission";
                }
            } else {
                if (!level.isAtWorst(req)) {
                    str += "- Req: " + manager.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();
                }

                if (requiresCommission(hullId) && !hasCommission()) {
                    if (!str.isEmpty()) str += "\n";
                    str += "- Req: " + manager.getFaction().getDisplayName() + " - " + "commission";
                }
            }

			return str;
		}

        return null;
	}

	protected Highlights getIllegalRetrofitTextHighlights(String hullId) {
		RepLevel req;
        if (hullId == null) req = getRequiredLevel();
        else req = getRequiredLevel(hullId);

		if (req != null) {
			Color c = Misc.getNegativeHighlightColor();
			Highlights h = new Highlights();
			RepLevel level = manager.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
            if (hullId == null) {
                if (!level.isAtWorst(req)) {
                    h.append("Req: " + manager.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase(), c);
                }

                if (requiresCommission() && !hasCommission()) {
                    h.append("Req: " + manager.getFaction().getDisplayName() + " - commission", c);
                }
            } else {
                if (!level.isAtWorst(req)) {
                    h.append("- Req: " + manager.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase(), c);
                }

                if (requiresCommission(hullId) && !hasCommission()) {
                    h.append("- Req: " + manager.getFaction().getDisplayName() + " - commission", c);
                }
            }

			return h;
		}

		return null;
	}

    /**
     * @return The lowest RepLevel of all the source hulls.
     */
	protected RepLevel getRequiredLevel() {
        RepLevel lowest = RepLevel.COOPERATIVE;

        for (RetrofitData data : retrofits) {
            if (!data.targetHull.equals(selectedRetrofit.getHullId())) continue;

            float rMin = data.reputation.getMin();
            if (data.reputation.isNegative()) rMin = -rMin;
            float lMin = lowest.getMin();
            if (lowest.isNegative()) rMin = -lMin;

            if (rMin < lMin) lowest = data.reputation;
        }

        return lowest;
	}

	protected RepLevel getRequiredLevel(String sourceHull) {
        for (RetrofitData data : retrofits) {
            if (data.targetHull.equals(selectedRetrofit.getHullId())
                        && matchesHullId(sourceHull, data.sourceHull)) {
                return data.reputation;
            }
        }

        return RepLevel.FAVORABLE;
	}

    /**
     * @return False if even one source hull doesn't require a commission to convert.
     */
    protected boolean requiresCommission() {
        for (RetrofitData data : retrofits) {
            if (!data.targetHull.equals(selectedRetrofit.getHullId())) continue;

            for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
                if (matchesHullId(spec, data.sourceHull) && !data.commission) {
                    return false;
                }
            }
        }

        return true;
    }

	protected boolean requiresCommission(String sourceHull) {
        for (RetrofitData data : retrofits) {
            if (data.targetHull.equals(selectedRetrofit.getHullId())
                        && matchesHullId(sourceHull, data.sourceHull)) {
                return data.commission;
            }
        }

        return true;
	}

    protected boolean hasCommission() {
        if (Roider_ModPlugin.hasNexerelin) {
            String commissionFaction = NexUtilsFaction.getCommissionFactionId();
            if (commissionFaction != null && AllianceManager.areFactionsAllied(commissionFaction, manager.getFaction().getId())) {
                return true;
            }
            if (AllianceManager.areFactionsAllied(PlayerFactionStore.getPlayerFactionId(), manager.getFaction().getId())) {
                return true;
            }
        }

		return manager.getFaction().getId().equals(Misc.getCommissionFactionId());
    }

    protected boolean isAllowed() {
        return true;
    }

    protected boolean isAllowed(String sourceHull) {
        return true;
    }

    protected String getNotAllowedRetrofitText(String hullId) {
        return "";
    }

    protected Highlights getNotAllowedRetrofitTextHighlights(String hullId) {
        return new Highlights();
    }

    protected String getNotAllowedRetrofitsTitle() {
        return "Not Allowed Retrofits";
    }

    protected void updateVisual() {
        visual.fadeVisualOut();

        if (selectedRetrofit == null) {
            visual.showImageVisual(new InteractionDialogImageVisual("illustrations", "orbital_construction", 640, 400));
        } else {
            visual.showFleetMemberInfo(selectedRetrofit);
        }
    }

    protected boolean matchesHullId(ShipHullSpecAPI hull, String source) {
        return matchesHullId(hull.getHullId(), source) || matchesHullId(hull.getBaseHullId(), source);
    }

    protected boolean matchesHullId(String hull, String source) {
        return hull.equals(source);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (OptionId.PICK_TARGET.equals(optionData)) pickTarget();
        else if (OptionId.PICK_SHIPS.equals(optionData)) pickShips();
        else if (OptionId.PRIORITIZE.equals(optionData)) prioritize();
        else if (OptionId.CANCEL_SHIPS.equals(optionData)) cancelShips();
        else if (optionData instanceof List) {
            confirmRetrofits((List) optionData);
        }
        else if (OptionId.CANCEL.equals(optionData)) {
            updateText();
            updateOptions();
        } else {
            dialog.dismiss();
        }
    }

	protected void pickTarget() {
        List<String> targetIds = new ArrayList<>();
        List<String> blockedIds = new ArrayList<>();

        RepLevel rep = manager.getFaction().getRelationshipLevel(Factions.PLAYER);
        boolean commissioned = Misc.getCommissionFaction() == manager.getFaction();

        for (RetrofitData data : retrofits) {
            boolean repPass = rep.isAtWorst(data.reputation);
            boolean comPass = !data.commission || (data.commission && commissioned);

            if (targetIds.contains(data.targetHull)) {
                if (repPass && comPass) blockedIds.remove(data.targetHull);
                continue;
            }

            boolean foundTarget = false;
            boolean foundSource = false;
            for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
                if (spec.getHullId().equals(data.targetHull)) {
                    foundTarget = true;
                }
                if (matchesHullId(spec, data.sourceHull)) {
                    foundSource = true;
                }

                if (foundTarget && foundSource) {
                    targetIds.add(data.targetHull);
                    if (!repPass || !comPass) {
                        blockedIds.add(data.targetHull);
                    }
                    break;
                }
            }
        }

        CampaignFleetAPI pool = FleetFactoryV3.createEmptyFleet(manager.getFaction().getId(), FleetTypes.MERC_PRIVATEER, null);
        for (String id : targetIds) {
            pool.getFleetData().addFleetMember(id + "_Hull");
        }

        for (FleetMemberAPI m : pool.getFleetData().getMembersListCopy()) {
            m.setShipName("Retrofit to");

            if (blockedIds.contains(m.getHullId())) {
                m.getRepairTracker().setMothballed(true);
                m.getRepairTracker().setCR(0);
            } else {
                m.getRepairTracker().setCR(m.getRepairTracker().getMaxCR()); // Looks cleaner
            }
        }

        pool.getFleetData().sort();

        List<FleetMemberAPI> targets = pool.getFleetData().getMembersListCopy();

        int rows = targets.size() / 7 + 1;

        // Calculate rows, columns, and iconSize?
        dialog.showFleetMemberPickerDialog("Pick retrofit hull", "Ok", "Cancel", rows, COLUMNS, 58f, true, false, targets, new FleetMemberPickerListener() {

            @Override
            public void pickedFleetMembers(List<FleetMemberAPI> members) {
                if (members.isEmpty()) {
                    selectedRetrofit = null;
                } else {
                    selectedRetrofit = members.get(0);
                }

                updateText();
                updateOptions();
                updateVisual();
            }

            @Override
            public void cancelledFleetMemberPicking() {}

        });
    }

    protected void pickShips() {
        List<FleetMemberAPI> avail = getAvailableShips();

        int rows = avail.size() / 7 + 1;

        dialog.showFleetMemberPickerDialog("Pick ships to retrofit", "Ok", "Cancel", rows, COLUMNS, 58f, true, true, avail, new FleetMemberPickerListener() {

            @Override
            public void pickedFleetMembers(List<FleetMemberAPI> members) {
                if (members.isEmpty()) return;

                // Tally cost
                double tCost = 0;
                for (FleetMemberAPI ship : members) {
                    RetrofitData data = null;
                    // Check for exact match
                    for (RetrofitData d : retrofits) {
                        if (d.targetHull.equals(selectedRetrofit.getHullId())
                                    && matchesHullId(ship.getHullSpec().getHullId(), d.sourceHull)) {
                            data = d;
                            break;
                        }
                    }

                    if (data != null) {
                        tCost += data.cost;
                        continue;
                    }

                    // Check for general match
                    for (RetrofitData d : retrofits) {
                        if (d.targetHull.equals(selectedRetrofit.getHullId())
                                    && matchesHullId(ship.getHullSpec(), d.sourceHull)) {
                            data = d;
                            break;
                        }
                    }

                    if (data != null) {
                        tCost += data.cost;
                    }
                }




                // Update UI
                if (tCost != 0) showRetrofitConfirmDialog(members);
                else confirmRetrofits(members);
//                updateText();
//                updateOptions();
            }

            @Override
            public void cancelledFleetMemberPicking() {}
        });
    }

    protected void showRetrofitConfirmDialog(List<FleetMemberAPI> members) {
        if (members.isEmpty()) return;

        // Tally cost
        double tCost = 0;
        for (FleetMemberAPI ship : members) {
            RetrofitData data = null;
            // Check for exact match
            for (RetrofitData d : retrofits) {
                if (d.targetHull.equals(selectedRetrofit.getHullId())
                            && matchesHullId(ship.getHullSpec().getHullId(), d.sourceHull)) {
                    data = d;
                    break;
                }
            }

            if (data != null) {
                tCost += data.cost;
                continue;
            }

            // Check for general match
            for (RetrofitData d : retrofits) {
                if (d.targetHull.equals(selectedRetrofit.getHullId())
                            && matchesHullId(ship.getHullSpec(), d.sourceHull)) {
                    data = d;
                    break;
                }
            }

            if (data != null) {
                tCost += data.cost;
            }
        }

        // Update text
        text.clear();

        TooltipMakerAPI targetTooltip = text.beginTooltip();
        targetTooltip.addTitle("Confirm Retrofits");
        int rows = (members.size() / COLUMNS) + 1;
        float iconSize = dialog.getTextWidth() / COLUMNS;

        float pad = 0f; // 10f
        Color color = manager.getFaction().getBaseUIColor();
        targetTooltip.addShipList(COLUMNS, rows, iconSize, color, members, pad);
        text.addTooltip();

        String message = "It is free ";
        if (tCost > 0) {
            message = "You must pay " + Misc.getDGSCredits((float) tCost);
        } else if (tCost < 0) {
            message = "You will receive " + Misc.getDGSCredits((float) -tCost);
        }

        message += " to convert";

        if (members.size() == 1) message += " this ship. The new hull will be pristine.";
        else message += " these ships. The new hulls will be pristine.";

        text.addPara(message, Misc.getHighlightColor(), Misc.getDGSCredits((float) tCost));

        // Update options
        options.clearOptions();

        String confirmText = "Confirm";
        if (tCost > 0) {
            confirmText = "Pay " + Misc.getDGSCredits((float) tCost) + " credits";
        } else if (tCost < 0) {
            tCost = -tCost;
            confirmText = "Receive " + Misc.getDGSCredits((float) tCost) + " credits";
        }

        options.addOption(confirmText, members);

        if (tCost > Global.getSector().getPlayerFleet().getCargo().getCredits().get()) {
            options.setEnabled(members, false);
            options.setTooltip(members, "You do not have enough credits!");
            options.setTooltipHighlights(members, "You do not have enough credits!");
            options.setTooltipHighlightColors(members, Misc.getNegativeHighlightColor());
        }

        options.addOption("Cancel", OptionId.CANCEL);
        options.setShortcut(OptionId.CANCEL, Keyboard.KEY_ESCAPE, false, false, false, true);
    }

    protected void confirmRetrofits(List<FleetMemberAPI> members) {
        // Tally cost
        double tCost = 0;
        for (FleetMemberAPI ship : members) {
            RetrofitData data = null;
            // Check for exact match
            for (RetrofitData d : retrofits) {
                if (d.targetHull.equals(selectedRetrofit.getHullId())
                            && matchesHullId(ship.getHullSpec().getHullId(), d.sourceHull)) {
                    data = d;
                    break;
                }
            }

            if (data != null) {
                tCost += data.cost;
                continue;
            }

            // Check for general match
            for (RetrofitData d : retrofits) {
                if (d.targetHull.equals(selectedRetrofit.getHullId())
                            && matchesHullId(ship.getHullSpec(), d.sourceHull)) {
                    data = d;
                    break;
                }
            }

            if (data != null) {
                tCost += data.cost;
            }
        }

        // Charge player
        if (tCost > 0) {
            Global.getSector().getPlayerFleet().getCargo().getCredits().subtract((float) tCost);
            Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Paid " + tCost + " for retrofits");
        }
        // Pay player
        if (tCost < 0) {
            tCost = -tCost;
            Global.getSector().getPlayerFleet().getCargo().getCredits().add((float) tCost);
            Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Received " + tCost + " for retrofits");
        }

        // Strip ships
        CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
        boolean stripped = false;
        for (FleetMemberAPI ship : members) {
            for (String slot : ship.getVariant().getNonBuiltInWeaponSlots()) {
                if (ship.getVariant().getWeaponId(slot) == null || ship.getVariant().getWeaponId(slot).isEmpty()) continue;

                playerCargo.addWeapons(ship.getVariant().getWeaponId(slot), 1);
                stripped = true;
                ship.getVariant().clearSlot(slot);
            }

            for (String wing : ship.getVariant().getFittedWings()) {
                playerCargo.addFighters(wing, 1);
                stripped = true;
            }

            ship.getVariant().getFittedWings().clear();
        }

        if (stripped) Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Stripped weapons and fighters to cargo");

        // Remove from player's fleet
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        for (FleetMemberAPI ship : members) {
            playerFleet.getFleetData().removeFleetMember(ship);
        }

        // Add to queue
        for (FleetMemberAPI ship : members) {
            RetrofitData data = null;
            // Check for exact match
            for (RetrofitData d : retrofits) {
                if (d.targetHull.equals(selectedRetrofit.getHullId())
                            && matchesHullId(ship.getHullSpec().getHullId(), d.sourceHull)) {
                    data = d;
                    break;
                }
            }

            if (data != null) {
                manager.addToQueue(new RetrofitTracker(ship, data, data.cost));
                continue;
            }

            // Check for general match
            for (RetrofitData d : retrofits) {
                if (d.targetHull.equals(selectedRetrofit.getHullId())
                            && matchesHullId(ship.getHullSpec(), d.sourceHull)) {
                    data = d;
                    break;
                }
            }

            if (data != null) {
                manager.addToQueue(new RetrofitTracker(ship, data, data.cost));
            }
        }

        updateText();
        updateOptions();
    }

    protected List<FleetMemberAPI> getAvailableShips() {
        List<String> sourceIds = new ArrayList<>();
        for (RetrofitData data : retrofits) {
            if (data.targetHull.equals(selectedRetrofit.getHullId())
                        && !isIllegal(data.sourceHull)
                        && isAllowed(data.sourceHull)) {
                sourceIds.add(data.sourceHull);
            }
        }

        CampaignFleetAPI pool = FleetFactoryV3.createEmptyFleet(Factions.PLAYER, FleetTypes.MERC_PRIVATEER, null);
        for (FleetMemberAPI m : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (m.getHullId().equals(selectedRetrofit.getHullId())) continue;

            for (String id : sourceIds) {
                if (matchesHullId(m.getHullSpec(), id)) {
                    pool.getFleetData().addFleetMember(m);
                    break;
                }
            }
        }

        return pool.getFleetData().getMembersListCopy();
    }

    protected void prioritize() {
        CampaignFleetAPI pool = FleetFactoryV3.createEmptyFleet(Factions.PLAYER, FleetTypes.MERC_PRIVATEER, null);
        for (RetrofitTracker tracker : manager.getQueued()) {
            FleetMemberAPI ship = tracker.ship;
            pool.getFleetData().addFleetMember(ship);
        }

        List<FleetMemberAPI> targets = pool.getFleetData().getMembersListCopy();

        int rows = targets.size() / 7 + 1;

        // Calculate rows, columns, and iconSize?
        dialog.showFleetMemberPickerDialog("Prioritize retrofit", "Ok", "Cancel", rows, COLUMNS, 58f, true, false, targets, new FleetMemberPickerListener() {

            @Override
            public void pickedFleetMembers(List<FleetMemberAPI> members) {
                // Put selected at the front of the queue
                for (RetrofitTracker tracker : manager.getQueued()) {
                    if (members.contains(tracker.ship)) {
                        manager.getQueued().remove(tracker);
                        manager.getQueued().add(0, tracker);
                        break;
                    }
                }

                // Update UI
                updateText();
                updateOptions();
            }

            @Override
            public void cancelledFleetMemberPicking() {}
        });
    }

    protected void cancelShips() {
        CampaignFleetAPI pool = FleetFactoryV3.createEmptyFleet(Factions.PLAYER, FleetTypes.MERC_PRIVATEER, null);
        for (RetrofitTracker tracker : manager.getQueued()) {
            FleetMemberAPI ship = tracker.ship;
            pool.getFleetData().addFleetMember(ship);
        }

        List<FleetMemberAPI> targets = pool.getFleetData().getMembersListCopy();

        int rows = targets.size() / 7 + 1;

        // Calculate rows, columns, and iconSize?
        dialog.showFleetMemberPickerDialog("Cancel retrofits", "Ok", "Cancel", rows, COLUMNS, 58f, true, true, targets, new FleetMemberPickerListener() {

            @Override
            public void pickedFleetMembers(List<FleetMemberAPI> members) {
                if (members.isEmpty()) return;

                // Tally refund
                double tRefund = 0;

                // Remove from queue
                List<RetrofitTracker> remove = new ArrayList<>();
                for (RetrofitTracker tracker : manager.getQueued()) {
                    if (members.contains(tracker.ship)) {
                        remove.add(tracker);
                        tRefund += tracker.cost;
                    }
                }

                manager.getQueued().removeAll(remove);

                // Refund player
                if (tRefund > 0) {
                    Global.getSector().getPlayerFleet().getCargo().getCredits().add((float) tRefund);
                    Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Refunded " + tRefund + " for cancelled retrofits");
                }

                // Add to player's fleet
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                for (FleetMemberAPI ship : members) {
                    playerFleet.getFleetData().addFleetMember(ship);
                }

                // Update UI
                updateText();
                updateOptions();
            }

            @Override
            public void cancelledFleetMemberPicking() {}
        });
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return memoryMap;
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {}

    @Override
    public void advance(float amount) {}

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {}

    @Override
    public Object getContext() { return null; }

}
