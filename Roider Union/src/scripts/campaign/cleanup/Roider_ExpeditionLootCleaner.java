package scripts.campaign.cleanup;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;

/**
 * Author: SafariJohn
 */
public class Roider_ExpeditionLootCleaner implements EveryFrameScript {

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_ExpeditionLootCleaner.class, "token", "t");
        x.aliasAttribute(Roider_ExpeditionLootCleaner.class, "duration", "d");
        x.aliasAttribute(Roider_ExpeditionLootCleaner.class, "elapsed", "e");
    }

    private SectorEntityToken token;
    private final float duration;
    private float elapsed;

    public Roider_ExpeditionLootCleaner(SectorEntityToken token, float duration) {
        this.token = token;
        this.duration = duration;
        elapsed = 0;
    }

    @Override
    public boolean isDone() {
        if (token != null && token.isExpired()) cleanLoot();

        return token == null;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (token.isExpired()) {
            cleanLoot();
            return;
        }

		if (amount <= 0) {
			return; // happens during game load
		}

		float days = Global.getSector().getClock().convertToDays(amount);
		elapsed += days;

        if (duration - elapsed <= 0) {
            cleanLoot();
        }
    }

    private void cleanLoot() {
        if (token == null) return;

        if (!token.hasTag(Tags.HAS_INTERACTION_DIALOG)) {
            for (SectorEntityToken e : token.getStarSystem().getAllEntities()) {
                if (e.getOrbitFocus() == token) {
                    Misc.fadeAndExpire(e);
                }
            }
        }

        Misc.fadeAndExpire(token);
        token = null;
    }
}
