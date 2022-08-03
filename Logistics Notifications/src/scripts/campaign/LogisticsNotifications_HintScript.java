package scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetLogisticsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.ui.HintPanelAPI;
import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: SafariJohn
 */
public class LogisticsNotifications_HintScript implements EveryFrameScript {
    // These are specified in data/config/Logistics Notifications/config.json
    // Whether to use this display method
    private final boolean DISPLAY_HINTS;
    // Warning (orange text) and alarm times in days of supply
    private final int LOW_SUPPLIES;
    private final int ALARM_SUPPLIES;

    private float hintRefresh;
    private final float HINT_REFRESH_INTERVAL = 0.1f;

    private float update;
    private final float UPDATE_INTERVAL = 1f;

    final int TUTORIAL_HINT_INDEX = 3;

    private Map<String, Float> suUsage;
    private final String EXCESS_FUEL = "fuel";
    private final String EXCESS_CREW = "crew";
    private int iter = 0;

    private float lastRecoveryCost;
    private float lastTotalPerDay;

    public LogisticsNotifications_HintScript() {
        hintRefresh = HINT_REFRESH_INTERVAL;
        update = 0;

        suUsage = new HashMap<>();

        boolean hintsDisplay = true;
        int lowSupplies = 30;
        int alarmSupplies = 10;

        try {
            JSONObject obj = Global.getSettings().loadJSON("data/config/Logistics Notifications/config.json");
            hintsDisplay = obj.getBoolean("useHintsDisplay");
            lowSupplies = obj.getInt("lowSupplies");
            alarmSupplies = obj.getInt("alarmSupplies");
        } catch (IOException | JSONException ex) {
            Logger.getLogger(LogisticsNotifications_NotificationScript.class.getName()).log(Level.SEVERE, null, ex);
        }

        DISPLAY_HINTS = hintsDisplay;
        LOW_SUPPLIES = lowSupplies;
        ALARM_SUPPLIES = alarmSupplies;

        lastRecoveryCost = 0;
        lastTotalPerDay = 0;
    }


    @Override
    public boolean isDone() { return false; }
    @Override
    public boolean runWhilePaused() { return true; }

    @Override
    public void advance(float amount) {
        // Use HintPanel to display days of supply
        if (DISPLAY_HINTS) {
            hintRefresh -= amount;
            update -= amount;


            if (update <= 0) {
                updateHintDataFull();
                update = UPDATE_INTERVAL;
                iter = 0;
            } else {
                updateHintDataIterative();
            }

            if (hintRefresh <= 0) {
                showHintSupplyDays();
                hintRefresh = HINT_REFRESH_INTERVAL;
            }
        }
    }

    private boolean tutorial = true; // To trigger immediate update on load
    private void showHintSupplyDays() {
        float suDays = getHintSupplyDays();
        float supplies = Global.getSector().getPlayerFleet().getCargo().getSupplies();
        Color suColor;

        if (lastRecoveryCost >= supplies) { // Running out
            suColor = Color.RED;
        } else {
            if (suDays > LOW_SUPPLIES) {
                suColor = Color.GREEN;
            } else if (suDays > ALARM_SUPPLIES) {
                suColor = Color.ORANGE;
            } else {
                suColor = Color.RED;
            }
        }

        // Cut off past tenths place
        int suDisplay = Math.round(suDays);

        HintPanelAPI hints = Global.getSector().getCampaignUI().getHintPanel();

        if (hints == null) return;

        int index = 0;
        if (TutorialMissionIntel.isTutorialInProgress()) {
            if (tutorial) {
                hints.fadeOutHint(index);
                tutorial = false;
            }
            index = TUTORIAL_HINT_INDEX;
        } else if (hints.hasHint(TUTORIAL_HINT_INDEX)) {
                hints.fadeOutHint(TUTORIAL_HINT_INDEX);
            tutorial = true;
        }

        String format = "~" + suDisplay + " days of supply";

        hints.setHint(index, format, false, suColor, "~" + suDisplay);
    }

    private float getHintSupplyDays() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        float supplies = playerFleet.getCargo().getSupplies();

        float suDays;

        // Running out
        if (lastRecoveryCost >= supplies) {
            suDays = supplies / lastTotalPerDay;
        }
        // Account for drop back to maintenance after repairs/recovery.
        else {
            // Total up maintenance costs per day for fleet
            float maintPerDay = 0;
            for (Float maint : suUsage.values()) {
                maintPerDay += maint;
            }
            // Compute!
            suDays = (lastRecoveryCost / lastTotalPerDay) + ((supplies - (int) lastRecoveryCost) / maintPerDay);
        }

        return suDays;
    }


    /**
     * Prepares data for how many days of supply the player has
     * left in full, accounting for repairs and recovery.
     */
    private void updateHintDataFull() {
        suUsage.clear();

        // Calculate days of supply remaining
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
//        float supplies = playerFleet.getCargo().getSupplies();

        FleetLogisticsAPI logistics = playerFleet.getLogistics();
        lastRecoveryCost = logistics.getTotalRepairAndRecoverySupplyCost();
        lastTotalPerDay = logistics.getTotalSuppliesPerDay();

        // Running out
//        if (recoveryCost >= supplies) {
//            suDays = supplies / totalPerDay;
//        }
        // Account for drop back to maintenance after repairs/recovery.
//        else {

            // Total up maintenance costs per day for fleet
//            float maintPerDay = 0;
            for (FleetMemberAPI mem : playerFleet.getMembersWithFightersCopy()) {
                float maint = mem.getStats().getSuppliesPerMonth().getModifiedValue() / 30;
                suUsage.put(mem.getId(), maint);
            }
            // Account for extra cost from over-capacity
            // Not going to try to do cargo because it's reductive as it consumes supplies
            suUsage.put(EXCESS_CREW, logistics.getExcessPersonnelCapacitySupplyCost());
            suUsage.put(EXCESS_FUEL, logistics.getExcessFuelCapacitySupplyCost());

            // And finally: compute!
//            suDays = (recoveryCost / totalPerDay) + ((supplies - (int) recoveryCost) / maintPerDay);
//        }
    }

    /**
     * Updates 1 ship's supply data per frame
     */
    private void updateHintDataIterative() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        if (iter >= playerFleet.getNumMembersFast()) iter = 0;

        try {
            FleetMemberAPI mem = playerFleet.getMembersWithFightersCopy().get(iter);
            float maint = mem.getStats().getSuppliesPerMonth().getModifiedValue() / 30;
            suUsage.put(mem.getId(), maint);

            iter++;
        } catch (IndexOutOfBoundsException ex) {
            iter = 0;
        }
    }

}
