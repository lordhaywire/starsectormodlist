package scripts;

import scripts.campaign.LogisticsNotifications_NotificationScript;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.ui.HintPanelAPI;
import scripts.campaign.LogisticsNotifications_HintScript;

/**
 * Author: SafariJohn
 */
public class LogisticsNotifications_ModPlugin extends BaseModPlugin {

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().addTransientScript(new LogisticsNotifications_NotificationScript());
        Global.getSector().addTransientScript(new LogisticsNotifications_HintScript());
    }

    @Override
    public void beforeGameSave() {
        HintPanelAPI hints = Global.getSector().getCampaignUI().getHintPanel();

        if (hints != null) {
            if (TutorialMissionIntel.isTutorialInProgress() && hints.hasHint(3)) {
                hints.fadeOutHint(3);
            } else if (hints.hasHint(0)) {
                hints.fadeOutHint(0);
            }
        }
    }

}
