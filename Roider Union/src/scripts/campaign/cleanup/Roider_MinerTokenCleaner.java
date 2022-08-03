package scripts.campaign.cleanup;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;

/**
 * Author: SafariJohn
 */
public class Roider_MinerTokenCleaner implements EveryFrameScript {

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_MinerTokenCleaner.class, "route", "r");
        x.aliasAttribute(Roider_MinerTokenCleaner.class, "token", "t");
    }

    private RouteData route;
    private SectorEntityToken token;

    public Roider_MinerTokenCleaner(RouteData route, SectorEntityToken token) {
        this.route = route;
        this.token = token;
    }

    @Override
    public boolean isDone() {
        if (token != null && token.isExpired()) cleanToken();

        return token == null;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (!RouteManager.getInstance().getRoutesForSource(route.getSource()).contains(route)) {
            cleanToken();
        }
    }

    private void cleanToken() {
        if (token == null) return;

        route = null;

        Misc.fadeAndExpire(token);
        token = null;
    }


}
