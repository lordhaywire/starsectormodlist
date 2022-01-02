package scripts;

import scripts.campaign.LogisticsNotifications_NotificationScript;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

/**
 * Author: SafariJohn
 */
public class LogisticsNotifications_ModPlugin extends BaseModPlugin {

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().addTransientScript(new LogisticsNotifications_NotificationScript());
    }

}
