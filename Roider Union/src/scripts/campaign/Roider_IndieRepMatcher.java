package scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import ids.Roider_Ids.Roider_Factions;

/**
 * Author: SafariJohn
 */
public class Roider_IndieRepMatcher implements EveryFrameScript {

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        FactionAPI player = Global.getSector().getPlayerFaction();
        FactionAPI indies = Global.getSector().getFaction(Factions.INDEPENDENT);
        FactionAPI roiders = Global.getSector().getFaction(Roider_Factions.ROIDER_UNION);

        if (indies.getRelToPlayer() != roiders.getRelToPlayer()) {
            roiders.setRelationship(player.getId(), indies.getRelToPlayer().getRel());
        }
    }

}
