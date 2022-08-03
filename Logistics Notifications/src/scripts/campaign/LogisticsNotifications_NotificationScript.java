package scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetLogisticsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: SafariJohn
 */
public class LogisticsNotifications_NotificationScript implements EveryFrameScript {
    // These are specified in data/config/Logistics Notifications/config.json
    // Whether to use this display method
    private final boolean DISPLAY_NOTIFICATIONS;
    // How often supply/fuel status is displayed
    private final int INCREMENT;
    // Warning (orange text) and alarm times in days of supply
    private final int LOW_SUPPLIES;
    private final int ALARM_SUPPLIES;
    // Warning (orange text) and alarm distances in ly
    private final int LOW_FUEL;
    private final int ALARM_FUEL;
    // Whether sound alarm should play
    private final boolean SOUND_ALARM;
    // Sound ids
    private final String ALARM_ID;
    private final String FOLLOWUP_ID;
    private final String SUPPLY_ALARM_ID;
    private final String SUPPLY_FOLLOWUP_ID;
    private final String FUEL_ALARM_ID;
    private final String FUEL_FOLLOWUP_ID;

    private int lastDay;
    private int counter;
    private boolean alarmed;

    private float interval;
    private final float INTERVAL = 1;



    public LogisticsNotifications_NotificationScript() {
        lastDay = -1;
        counter = 0;
        alarmed = false;

        interval = INTERVAL;

        // Load settings from config.json
        // Defaults
        boolean notifications = false;

        int increment = 3;
        int lowSupplies = 30;
        int alarmSupplies = 10;
        int lowFuel = 20;
        int alarmFuel = 10;

        boolean soundAlarm = true;

        String alarmId = "cr_playership_critical";
        String followupId = "cr_playership_critical";

        String suAlarmId = "cr_playership_critical";
        String suFollowupId = "cr_playership_critical";

        String fuelAlarmId = "cr_playership_critical";
        String fuelFollowupId = "cr_playership_critical";
        try {
            JSONObject obj = Global.getSettings().loadJSON("data/config/Logistics Notifications/config.json");
            notifications = obj.getBoolean("useNotifications");

            increment = obj.getInt("notificationIncrement");
            lowSupplies = obj.getInt("lowSupplies");
            alarmSupplies = obj.getInt("alarmSupplies");
            lowFuel = obj.getInt("lowFuel");
            alarmFuel = obj.getInt("alarmFuel");

            soundAlarm = obj.getBoolean("soundAlarm");

            alarmId = obj.getString("alarmSoundId");
            followupId = obj.getString("followupSoundId");

            suAlarmId = obj.getString("suAlarmSoundId");
            suFollowupId = obj.getString("suFollowupSoundId");

            fuelAlarmId = obj.getString("fuelAlarmSoundId");
            fuelFollowupId = obj.getString("fuelFollowupSoundId");
        } catch (IOException | JSONException ex) {
            Logger.getLogger(LogisticsNotifications_NotificationScript.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Safety checks
        if (increment < 1) increment = 1;
        if (alarmSupplies < 0) alarmSupplies = -1;
        if (lowSupplies < alarmSupplies) lowSupplies = alarmSupplies;
        if (alarmFuel < 0) alarmFuel = -1;
        if (lowFuel < alarmFuel) lowFuel = alarmFuel;

        DISPLAY_NOTIFICATIONS = notifications;

        INCREMENT = increment;
        LOW_SUPPLIES = lowSupplies;
        ALARM_SUPPLIES = alarmSupplies;
        LOW_FUEL = lowFuel;
        ALARM_FUEL = alarmFuel;

        SOUND_ALARM = soundAlarm;

        ALARM_ID = alarmId;
        FOLLOWUP_ID = followupId;

        SUPPLY_ALARM_ID = suAlarmId;
        SUPPLY_FOLLOWUP_ID = suFollowupId;

        FUEL_ALARM_ID = fuelAlarmId;
        FUEL_FOLLOWUP_ID = fuelFollowupId;
    }

    public String displayConsole(boolean supply, boolean fuel) {
        String s = "";
        if (supply) {
            float suDisplay = (int) (getSupplyDays() * 10f) / 10f;
            s += "You have about " + suDisplay + " days of supply left.";
            if (fuel) s += "\n";
        }

        if (fuel) {
            float fuelDisplay = (int) (getFuelLY() * 10f) / 10f;
            s += "You have enough fuel to travel about " + fuelDisplay + " lightyears.";
        }

        return s;
    }

    private float delay = 1f; // Ensures text appears
    private boolean first = true; // To trigger immediate update on load
    @Override
    public void advance(float amount) {
        if (Global.getSector().getCampaignUI().isShowingDialog()
                    || Global.getSector().isInNewGameAdvance()) {
            return;
        }


        // First update
        if (DISPLAY_NOTIFICATIONS) {
            if (first && delay <= 0f) {
                first = false;
                firstUpdate();
                return;
            } else if (first) {
                delay -= amount;
                return;
            }
        }

        // if paused, return
        if (Global.getSector().isPaused()) return;

        // Check if should be alarmed every INTERVAL
        if (!alarmed && interval <= 0) {
            interval = INTERVAL;

            // If alarm needed, play sound and show relevant metric
            boolean suAlarm = isSupplyAlarm();
            boolean fuelAlarm = isFuelAlarm();
            soundAlarm(suAlarm, fuelAlarm);
            alarmed = suAlarm || fuelAlarm;

            if (suAlarm) showSupplyDays();
            if (fuelAlarm) showFuelLY();

            if (alarmed) return;
        } else {
            interval -= amount;
        }

        // Check if new day
        int day = Global.getSector().getClock().getDay();
        if (day == lastDay) return;

        lastDay = day; // New day

        // There are INCREMENT days between notifications
        if (++counter < INCREMENT) return;

        counter = 0;

        // Display messages
        if (DISPLAY_NOTIFICATIONS) {
            showSupplyDays();
            showFuelLY();
        }

        // Play alarm if danger
        alarmed = checkForDanger();
    }

    /**
     * Shows initial messages
     */
    private void firstUpdate() {
        // Display messages
        showSupplyDays();
        showFuelLY();

        alarmed = checkForDanger();
    }

    /**
     * Checks if there is danger and (if not disabled) plays an alarm.
     * @return true if there is danger
     */
    private boolean checkForDanger() {
        if (!SOUND_ALARM) return isSupplyAlarm() || isFuelAlarm();

        boolean suAlarm = isSupplyAlarm();
        boolean fuelAlarm = isFuelAlarm();
        soundAlarm(suAlarm, fuelAlarm);

        if (suAlarm) showSupplyDays();
        if (fuelAlarm) showFuelLY();

        return suAlarm || fuelAlarm;
    }

    private void soundAlarm(boolean supplyAlarm, boolean fuelAlarm) {
        if (!SOUND_ALARM) return;

        String soundId = "";
        float volume = 1f;
        if (!alarmed) { // Initial alarm
            if (supplyAlarm && fuelAlarm) soundId = ALARM_ID;
            else if (supplyAlarm) soundId = SUPPLY_ALARM_ID;
            else if (fuelAlarm) soundId = FUEL_ALARM_ID;
        } else { // Followup alarms
            volume = 0.2f;

            if (supplyAlarm && fuelAlarm) soundId = FOLLOWUP_ID;
            else if (supplyAlarm) soundId = SUPPLY_FOLLOWUP_ID;
            else if (fuelAlarm) soundId = FUEL_FOLLOWUP_ID;
        }

        try { // Play nothing if the soundId is invalid
            Global.getSoundPlayer().playUISound(soundId, 1f, volume);
        } catch (RuntimeException ex) {}
    }

    /**
     * Displays message to player showing days of supply remaining.
     */
    private void showSupplyDays() {
        float suDays = getSupplyDays();
        float supplies = Global.getSector().getPlayerFleet().getCargo().getSupplies();
        float recoveryCost = Global.getSector().getPlayerFleet()
                    .getLogistics().getTotalRepairAndRecoverySupplyCost();
        Color suColor;

        if (recoveryCost >= supplies) { // Running out
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
        float suDisplay = (int) (suDays * 10f) / 10f;

        Global.getSector().getCampaignUI().addMessage("You have about "
                    + suDisplay + " days of supply left.",
                    Misc.getTextColor(), "" + suDisplay, "",
                    suColor, Misc.getTextColor());
    }

    /**
     * Calculates how many days of supply the player has left, accounting for repairs and recovery.
     * @return days of supply
     */
    private float getSupplyDays() {
        // Calculate days of supply remaining
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        float supplies = playerFleet.getCargo().getSupplies();

        FleetLogisticsAPI logistics = playerFleet.getLogistics();
        float recoveryCost = logistics.getTotalRepairAndRecoverySupplyCost();
        float totalPerDay = logistics.getTotalSuppliesPerDay();

        float suDays;
        // Running out
        if (recoveryCost >= supplies) {
            suDays = supplies / totalPerDay;
        }
        // Account for drop back to maintenance after repairs/recovery.
        else {

            // Total up maintenance costs per day for fleet
            float maintPerDay = 0;
            for (FleetMemberAPI mem : playerFleet.getMembersWithFightersCopy()) {
                float maint = mem.getStats().getSuppliesPerMonth().getModifiedValue() / 30;
                maintPerDay += maint;
            }
            // Account for extra cost from over-capacity
            // Not going to try to do cargo because it's reductive as it consumes supplies
            maintPerDay += logistics.getExcessPersonnelCapacitySupplyCost();
            maintPerDay += logistics.getExcessFuelCapacitySupplyCost();

            // And finally: compute!
            suDays = (recoveryCost / totalPerDay) + ((supplies - (int) recoveryCost) / maintPerDay);
        }

        return suDays;
    }

    /**
     * @return true if supplies are dangerously low
     */
    private boolean isSupplyAlarm() {
        float supplies = Global.getSector().getPlayerFleet().getCargo().getSupplies();
        float recoveryCost = Global.getSector().getPlayerFleet()
                    .getLogistics().getTotalRepairAndRecoverySupplyCost();

        return recoveryCost >= supplies || getSupplyDays() <= ALARM_SUPPLIES;
    }

    /**
     * Displays message to player showing lightyears of fuel remaining.
     */
    private void showFuelLY() {
        float ly = getFuelLY();
        Color lyColor;

        if (ly > LOW_FUEL) {
            lyColor = Color.GREEN;
        } else if (ly > ALARM_FUEL) {
            lyColor = Color.ORANGE;
        } else {
            lyColor = Color.RED;
        }

        // Cut off past tenths place
        float lyDisplay = (int) (ly * 10f) / 10f;

        Global.getSector().getCampaignUI().addMessage("You have enough fuel to travel about "
                    + lyDisplay + " lightyears.",
                    Misc.getTextColor(), "" + lyDisplay, "",
                    lyColor, Misc.getTextColor());
    }

    /**
     * Calculates how far the player's fleet can travel, minus amount needed to jump to hyper if in-system.
     * @return distance in lightyears
     */
    private float getFuelLY() {
        // Calculate lightyears of fuel remaining
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        float fuel = playerFleet.getCargo().getFuel();
        float fuelPerDay = playerFleet.getLogistics().getBaseFuelCostPerLightYear();

        float ly;

        if (playerFleet.isInHyperspace()) {
            ly = fuel / fuelPerDay;
        } else {
            ly = (fuel - fuelPerDay) / fuelPerDay;
        }

        if (ly < 0) ly = 0;

        return ly;
    }

    /**
     * @return true if fuel is dangerously low
     */
    private boolean isFuelAlarm() {
        return getFuelLY() <= ALARM_FUEL;
    }

    @Override
    public boolean isDone() { return false; }
    @Override
    public boolean runWhilePaused() { return false; }

}
