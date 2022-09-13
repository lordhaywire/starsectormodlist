package scripts.campaign.cleanup;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;

/**
 * Author: SafariJohn
 */
public class Roider_ExpeditionMajorLootCleaner implements EveryFrameScript {
    private SectorEntityToken entity;
    private SectorEntityToken token;

    public Roider_ExpeditionMajorLootCleaner(SectorEntityToken entity) {
        this.entity = entity;
        token = entity.getOrbitFocus();
    }

    @Override
    public boolean isDone() {
        return entity == null && token == null;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (entity == null) {
            if (token != null) {
                Misc.fadeAndExpire(token);
                token = null;
            }

            return;
        }

        if (entity.isExpired() || entity.getContainingLocation() == null) {
            entity = null;
            Misc.fadeAndExpire(token);
            token = null;
        }
    }
}
