package RealisticCombat.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.input.InputEventAPI;

import java.util.List;

public final class Announcer extends BaseEveryFrameCombatPlugin {

    private static float announcementDurationRemaining = 0;

    /**
     Asks the announcer to announce a line if the announcer is not already
     announcing one.
     */
    public static void requestAnnouncement(
            final RealisticCombat.settings.Announcer.EVENT_TYPE eventType)
    {
        if (announcementDurationRemaining > 0) return;
        final int index = RealisticCombat.settings.RandomGenerator.nextInt(
                RealisticCombat.settings.Announcer.getDurations().get(eventType).size());
        Global.getSoundPlayer().playUISound(RealisticCombat.settings.Announcer.getLineIds().get(
                eventType).get(index), 1, 1);
        announcementDurationRemaining = RealisticCombat.settings.Announcer.getDurations().get(
                eventType).get(index);
    }
    @Override
    public void advance(final float amount, final List<InputEventAPI> events) {
        announcementDurationRemaining -= amount;
    }
}
