package scripts.campaign.retrofit;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.CombatReadinessPlugin;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Hullmods;
import java.awt.Color;
import java.util.*;
import org.lwjgl.input.Keyboard;
import scripts.campaign.retrofit.Roider_BaseRetrofitManager.RetrofitTracker;
import static scripts.campaign.retrofit.Roider_BaseRetrofitPlugin.COLUMNS;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper.RetrofitData;

/**
 * Author: SafariJohn
 */
public class Roider_ArgosRetrofitPlugin extends Roider_BaseRetrofitPlugin {

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_ArgosRetrofitPlugin.class, "selectedDocks", "sD");
    }

    public static final String PICK_DOCKS = "roider_pickConvDocks";
    public static final String COM_CR = "CR";

    private final List<FleetMemberAPI> selectedDocks;

    public Roider_ArgosRetrofitPlugin(InteractionDialogPlugin originalPlugin,
                Roider_ArgosRetrofitManager manager, final Map<String, MemoryAPI> memoryMap) {
        super(originalPlugin, manager, memoryMap);

        selectedDocks = new ArrayList<>();
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        init(dialog, dialog.getInteractionTarget().getActivePerson());
    }

    public void init(InteractionDialogAPI dialog, PersonAPI person) {
        super.init(dialog);
    }

    @Override
    protected void updateOptions() {
        options.clearOptions();
        options.addOption("Pick retrofit hull", OptionId.PICK_TARGET);
        options.addOption("Retrofit ships", OptionId.PICK_SHIPS);
        options.addOption("Pick conversion docks", PICK_DOCKS);
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
    }

    @Override
    protected String getLeaveOptionText() {
        String leaveText = "Return";
        if (manager.getFaction().isPlayerFaction()) leaveText = "Leave";

        return leaveText;
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
        } else if (PICK_DOCKS.equals(optionData)) {
            pickDocks();
        } else {
            text.clear();
//            text.addPara("You finish your retrofitting arrangements.");
//            text.addPara("\"Anything else I can do for you?\"");

            options.clearOptions();

            visual.fadeVisualOut();
//            visual.showPersonInfo(person);

            if (originalPlugin == null) {
                dialog.dismiss();
            } else {
                dialog.setPlugin(originalPlugin);
                originalPlugin.optionSelected("Finished retrofitting", "roider_argosFinishedConverting");
            }

//            FireAll.fire(null, dialog, memoryMap, "PopulateOptions");
        }
    }

    @Override
    protected void updateVisual() {
        super.updateVisual();

        if (selectedRetrofit != null && !getSelectedConversionDockShips().isEmpty()) return;

        // Need to show available and active Argosi
        CampaignFleetAPI active = Global.getFactory().createEmptyFleet(manager.getFaction().getId(), "Active", true);
        for (FleetMemberAPI dock : getSelectedConversionDockShips()) {

            float cr = dock.getRepairTracker().getCR();
            active.getFleetData().addFleetMember(dock);

            dock.getRepairTracker().setCR(cr);
        }

        CampaignFleetAPI avail = Global.getFactory().createEmptyFleet(manager.getFaction().getId(), "Available", true);
        for (FleetMemberAPI dock : getReadyConversionDockShips()) {
            if (getSelectedConversionDockShips().contains(dock)) continue;

            float cr = dock.getRepairTracker().getCR();

            avail.getFleetData().addFleetMember(dock);

            dock.getRepairTracker().setCR(cr);
        }

        visual.showFleetInfo("Active conversion docks", active, "Available conversion docks", avail);
    }

    @Override
    protected void updateText() {
        text.clear();

        if (!manager.getFaction().isPlayerFaction()
                    || selectedRetrofit == null) {
            super.updateText();
            return;
        }

        // Need to show resource requirements when player is converting
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
                        appendIllegalsCosts(illegalCosts, getResourceCosts(selectedRetrofit, (int) data.cost));
                        illegalCosts.append("- ").append(getNotAllowedRetrofitText(data.sourceHull));
                    } else if (isIllegal(data.sourceHull)) {
                        if (firstIllegal) firstIllegal = false;
                        else illegalCosts.append("\n");

                        illegalCosts.append(spec.getNameWithDesignationWithDashClass()).append("\n");
                        appendIllegalsCosts(illegalCosts, getResourceCosts(selectedRetrofit, (int) data.cost));
                        illegalCosts.append(getIllegalRetrofitText(data.sourceHull));
                    } else {
                        if (firstLegal) firstLegal = false;
                        else costs.append("\n");

                        costs.append(spec.getNameWithDesignationWithDashClass()).append("\n");
                        appendLegalCosts(costs, highlights, getResourceCosts(selectedRetrofit, (int) data.cost));
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
                        appendIllegalsCosts(illegalCosts, getResourceCosts(selectedRetrofit, (int) data.cost));
                        illegalCosts.append("- ").append(getNotAllowedRetrofitText(data.sourceHull));
                    } else if (isIllegal(data.sourceHull)) {
                        if (firstIllegal) firstIllegal = false;
                        else illegalCosts.append("\n");

                        illegalCosts.append(spec.getNameWithDesignationWithDashClass()).append("\n");
                        appendIllegalsCosts(illegalCosts, getResourceCosts(selectedRetrofit, (int) data.cost));
                        illegalCosts.append(getIllegalRetrofitText(data.sourceHull));
                    } else {
                        if (firstLegal) firstLegal = false;
                        else costs.append("\n");

                        costs.append(spec.getNameWithDesignationWithDashClass()).append("\n");
                        appendLegalCosts(costs, highlights, getResourceCosts(selectedRetrofit, (int) data.cost));
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

    private void appendIllegalsCosts(StringBuilder costs, Map<String, Integer> resources) {
        // CR cost
        costs.append("- Costs ").append(resources.get(COM_CR)).append(" CR\n");

        int cost = resources.get(Commodities.CREDITS);

        if (cost > 0) {
            costs.append("- Costs ").append(resources.get(Commodities.HEAVY_MACHINERY)).append(" heavy machinery\n");
            costs.append("- Costs ").append(resources.get(Commodities.SUPPLIES)).append(" supplies\n");
            costs.append("- Costs ").append(resources.get(Commodities.METALS)).append(" metals\n");
        } else if (cost < 0) {
            costs.append("- Gives ").append(resources.get(Commodities.HEAVY_MACHINERY)).append(" heavy machinery\n");
            costs.append("- Gives ").append(resources.get(Commodities.SUPPLIES)).append(" supplies\n");
            costs.append("- Gives ").append(resources.get(Commodities.METALS)).append(" metals\n");
        } else {
            costs.append("- Free\n");
        }
    }

    private void appendLegalCosts(StringBuilder costs, List<String> highlights, Map<String, Integer> resources) {
        costs.append("- Costs %s CR\n");
        highlights.add(resources.get(COM_CR) + "");

        int cost = resources.get(Commodities.CREDITS);

        if (cost > 0) {
            costs.append("- Costs %s heavy machinery\n");
            highlights.add("" + resources.get(Commodities.HEAVY_MACHINERY));
            costs.append("- Costs %s supplies\n");
            highlights.add("" + resources.get(Commodities.SUPPLIES));
            costs.append("- Costs %s metals");
            highlights.add("" + resources.get(Commodities.METALS));
        } else if (cost < 0) {
            costs.append("- Gives %s heavy machinery\n");
            highlights.add("" + resources.get(Commodities.HEAVY_MACHINERY));
            costs.append("- Gives %s supplies\n");
            highlights.add("" + resources.get(Commodities.SUPPLIES));
            costs.append("- Gives %s metals");
            highlights.add("" + resources.get(Commodities.METALS));
        } else {
            highlights.add("Free");
        }
    }

    private Map<String, Integer> getResourceCosts(FleetMemberAPI targetHull, int credits) {
        Map<String, Integer> resources = new HashMap<>();
        resources.put(Commodities.CREDITS, credits); // Passing credit cost along

        // Costs are CR, supplies, metal, and heavy machinery

        // 25% of credit cost is heavy machinery
        float machinery = credits / Global.getSettings().getCommoditySpec(Commodities.HEAVY_MACHINERY).getBasePrice() / 4f;
        resources.put(Commodities.HEAVY_MACHINERY, (int) machinery);

        // 50% of credit cost is supplies
        float supplies = credits / Global.getSettings().getCommoditySpec(Commodities.SUPPLIES).getBasePrice() / 2f;

        resources.put(Commodities.SUPPLIES, (int) supplies);

        // 25% of credit cost is metals
        float metals = credits / Global.getSettings().getCommoditySpec(Commodities.METALS).getBasePrice() / 4f;
        resources.put(Commodities.METALS, (int) metals);

        // CR cost is on top of other costs
        resources.put(COM_CR, (int) (targetHull.getDeploymentCostSupplies()));

        return resources;
    }

    @Override
    protected String getNotAllowedRetrofitsTitle() {
        return "Blueprint Required";
    }

    @Override
    protected Highlights getNotAllowedRetrofitTextHighlights(String hullId) {
        Highlights h = new Highlights();
        h.setText(getNotAllowedRetrofitText(hullId));
        return h;
    }

    @Override
    protected String getNotAllowedRetrofitText(String hullId) {
        return "You do not know how to retrofit this hull";
    }

    @Override
    protected boolean isAllowed() {
        if (manager.getFaction().isPlayerFaction()) {
            return Global.getSector().getPlayerFaction().knowsShip(selectedRetrofit.getHullId());
        }

        return true;
    }

    @Override
    protected boolean isAllowed(String sourceHull) {
        if (manager.getFaction().isPlayerFaction()) {
            for (RetrofitData data : retrofits) {
                if (data.targetHull.equals(selectedRetrofit.getHullId())
                            && matchesHullId(sourceHull, data.sourceHull)) {
                    return Global.getSector().getPlayerFaction().knowsShip(data.targetHull);
                }
            }

            return false;
        }

        return true;
    }

    @Override
    protected void showRetrofitConfirmDialog(List<FleetMemberAPI> members) {
        if (members.isEmpty()) return;

        // Tally costs
        double tCost = 0; // for player only
        List<Float> crCosts = new ArrayList<>();
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
                crCosts.add(selectedRetrofit.getDeploymentCostSupplies() / 100f);
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
                crCosts.add(selectedRetrofit.getDeploymentCostSupplies() / 100f);
            }
        }

        // If another faction is doing the converting
        if (!manager.getFaction().isPlayerFaction()) {
            super.showRetrofitConfirmDialog(members);

            if (!docksHaveEnoughCR(crCosts)) {
                options.setEnabled(members, false);
                options.setTooltip(members, "Their conversion dock ships do not have enough CR!");
                options.setTooltipHighlights(members, "Their conversion dock ships do not have enough CR!");
                options.setTooltipHighlightColors(members, Misc.getNegativeHighlightColor());
            }

            return;
        }

        // Else if player is converting

        /**
         * Sort CR list so largest CR costs go last because
         * the last deduction on a dock goes through as long as
         * the dock is above malfunction CR
         **/
        Collections.sort(crCosts);

        boolean enoughCR = docksHaveEnoughCR(crCosts);

        boolean canAfford = enoughCR && canAffordResources(tCost);

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

        final Map<String, Integer> rez = getResourceCosts(selectedRetrofit, (int) Math.abs(tCost));
        int totalResources = rez.get(Commodities.HEAVY_MACHINERY)
                    + rez.get(Commodities.SUPPLIES)
                    + rez.get(Commodities.METALS);

        String message = "It is free ";
        if (tCost > 0) {
            message = "You must pay " + totalResources + " resources";
        } else if (tCost < 0) {
            message = "You will receive " + totalResources + " resources";
        }

        message += " to convert";

        if (members.size() == 1) message += " this ship. The new hull will be pristine.";
        else message += " these ships. The new hulls will be pristine.";

        text.addPara(message, Misc.getHighlightColor(), totalResources + " resources");

        Misc.showCost(text, color, manager.getFaction().getDarkUIColor(),
                    new String[] { Commodities.HEAVY_MACHINERY, Commodities.SUPPLIES, Commodities.METALS },
                    new int[] { rez.get(Commodities.HEAVY_MACHINERY), rez.get(Commodities.SUPPLIES), rez.get(Commodities.METALS) });

        // Update options
        options.clearOptions();

        String confirmText = "Confirm";
        if (tCost > 0) {
            confirmText = "Pay " + totalResources + " resources";
        } else if (tCost < 0) {
            confirmText = "Receive " + totalResources + " resources";
        }

        options.addOption(confirmText, members);

        if (selectedDocks.isEmpty()) {
            options.setEnabled(members, false);
            options.setTooltip(members, "Please pick one or more conversion dock ships!");
            options.setTooltipHighlights(members, "Please pick one or more conversion dock ships!");
            options.setTooltipHighlightColors(members, Misc.getNegativeHighlightColor());
        } else if (!enoughCR) {
            options.setEnabled(members, false);
            options.setTooltip(members, "Your conversion dock ships do not have enough CR!");
            options.setTooltipHighlights(members, "Your conversion dock ships do not have enough CR!");
            options.setTooltipHighlightColors(members, Misc.getNegativeHighlightColor());
        } else if (!canAfford) {
            options.setEnabled(members, false);
            options.setTooltip(members, "You do not have enough resources!");
            options.setTooltipHighlights(members, "You do not have enough resources!");
            options.setTooltipHighlightColors(members, Misc.getNegativeHighlightColor());
        }

        options.addOption("Cancel", OptionId.CANCEL);
        options.setShortcut(OptionId.CANCEL, Keyboard.KEY_ESCAPE, false, false, false, true);
    }

    private boolean docksHaveEnoughCR(List<Float> crCosts) {
        Map<FleetMemberAPI, Float> crAvail = new HashMap<>();

        for (FleetMemberAPI dock : getSelectedConversionDockShips()) {
            crAvail.put(dock, dock.getRepairTracker().getCR());
        }


        CombatReadinessPlugin crPlugin = Global.getSettings().getCRPlugin();

        Map<FleetMemberAPI, Float> replace = new HashMap<>();
        for (Float cr : crCosts) {
            boolean afforded = false;
            for (FleetMemberAPI dock : crAvail.keySet()) {
                Float crA = crAvail.get(dock);
                if (crA > crPlugin.getMalfunctionThreshold(dock.getStats())
                            && crA >= cr) {
                    replace.put(dock, crA - cr);
                    afforded = true;
                    break;
                }
            }

            for (FleetMemberAPI dock : replace.keySet()) {
                crAvail.put(dock, replace.get(dock));
            }

            replace.clear();

            if (!afforded) {
                return false;
            }
        }

        return true;
    }

    private boolean canAffordResources(double tCost) {
        if (tCost > 0) {
            Map<String, Integer> resources = getResourceCosts(selectedRetrofit, (int) tCost);
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();

            String com = Commodities.HEAVY_MACHINERY;
            if (cargo.getCommodityQuantity(com) < resources.get(com)) return false;

            com = Commodities.SUPPLIES;
            if (cargo.getCommodityQuantity(com) < resources.get(com)) return false;

            com = Commodities.METALS;
            if (cargo.getCommodityQuantity(com) < resources.get(com)) return false;
        }

        return true;
    }

    @Override
    protected void confirmRetrofits(List<FleetMemberAPI> members) {
        // Tally cost
        double tCost = 0;
        List<Float> crCosts = new ArrayList<>();
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
                crCosts.add(selectedRetrofit.getDeploymentCostSupplies() / 100f);
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
                crCosts.add(selectedRetrofit.getDeploymentCostSupplies() / 100f);
            }
        }

        // Consume dock CR
        List<FleetMemberAPI> docks = getSelectedConversionDockShips();
        CombatReadinessPlugin crPlugin = Global.getSettings().getCRPlugin();

        Collections.sort(docks, new Comparator<FleetMemberAPI>() {
            @Override
            public int compare(FleetMemberAPI o1, FleetMemberAPI o2) {
                return (int) (o1.getRepairTracker().getCR() * 100f - o2.getRepairTracker().getCR() * 100f);
            }
        });

        for (Float cr : crCosts) {
            for (FleetMemberAPI dock : docks) {
                if (dock.getRepairTracker().getCR() > crPlugin.getMalfunctionThreshold(dock.getStats())
                            && dock.getRepairTracker().getCR() >= cr) {
                    dock.getRepairTracker().applyCREvent(-cr, "Converted ship");
                    break;
                }
            }

            Collections.sort(docks, new Comparator<FleetMemberAPI>() {
                @Override
                public int compare(FleetMemberAPI o1, FleetMemberAPI o2) {
                    return (int) (o1.getRepairTracker().getCR() * 100f - o2.getRepairTracker().getCR() * 100f);
                }
            });
        }

        // Need to deduct resources instead of credits if player is converting
        if (manager.getFaction().isPlayerFaction()) {
            if (tCost > 0) {
                Map<String, Integer> resources = getResourceCosts(selectedRetrofit, (int) tCost);
                String com = Commodities.HEAVY_MACHINERY;
                Global.getSector().getPlayerFleet().getCargo().removeCommodity(com, resources.get(com));
                Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Lost " + resources.get(com) + " heavy machinery for retrofits");
                com = Commodities.SUPPLIES;
                Global.getSector().getPlayerFleet().getCargo().removeCommodity(com, resources.get(com));
                Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Lost " + resources.get(com) + " supplies for retrofits");
                com = Commodities.METALS;
                Global.getSector().getPlayerFleet().getCargo().removeCommodity(com, resources.get(com));
                Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Lost " + resources.get(com) + " metals for retrofits");
            }

            if (tCost < 0) {
                Map<String, Integer> resources = getResourceCosts(selectedRetrofit, (int) -tCost);
                String com = Commodities.HEAVY_MACHINERY;
                Global.getSector().getPlayerFleet().getCargo().addCommodity(com, resources.get(com));
                Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Gained " + resources.get(com) + " heavy machinery from retrofits");
                com = Commodities.SUPPLIES;
                Global.getSector().getPlayerFleet().getCargo().addCommodity(com, resources.get(com));
                Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Gained " + resources.get(com) + " supplies from retrofits");
                com = Commodities.METALS;
                Global.getSector().getPlayerFleet().getCargo().addCommodity(com, resources.get(com));
                Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Gained " + resources.get(com) + " metals from retrofits");
            }
        } else {
            // Charge player
            if (tCost > 0) {
                Global.getSector().getPlayerFleet().getCargo().getCredits().subtract((float) tCost);
                Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Paid " + tCost + " for retrofits");
            }
            // Pay player
            if (tCost < 0) {
                Global.getSector().getPlayerFleet().getCargo().getCredits().add((float) -tCost);
                Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Received " + -tCost + " for retrofits");
            }
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

    private void pickDocks() {
        List<FleetMemberAPI> avail = getReadyConversionDockShips();

        int rows = avail.size() / 7 + 1;

        dialog.showFleetMemberPickerDialog("Pick ships with conversion docks to use", "Ok", "Cancel", rows, COLUMNS, 58f, true, true, avail, new FleetMemberPickerListener() {

            @Override
            public void pickedFleetMembers(List<FleetMemberAPI> members) {
                selectedDocks.clear();
                selectedDocks.addAll(members);

                updateOptions();
                updateVisual();
            }

            @Override
            public void cancelledFleetMemberPicking() {}
        });


    }

    private List<FleetMemberAPI> getReadyConversionDockShips() {
        CampaignFleetAPI fleet = (CampaignFleetAPI) manager.getEntity();
        CombatReadinessPlugin crPlugin = Global.getSettings().getCRPlugin();

        List<FleetMemberAPI> docks = new ArrayList<>();
        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy()) {
            if (ship.getVariant().hasHullMod(Roider_Hullmods.CONVERSION_DOCK)) {
                if (ship.isMothballed()) continue;
                float mal = crPlugin.getMalfunctionThreshold(ship.getStats());
                float cr = ship.getRepairTracker().getCR();

                if (cr <= mal) continue;

                docks.add(ship);
            }
        }

        return docks;
    }

    private List<FleetMemberAPI> getSelectedConversionDockShips() {
        return selectedDocks;
    }
}
