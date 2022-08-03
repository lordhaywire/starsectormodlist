package scripts.world;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import ids.Roider_Ids.Roider_Factions;
import scripts.campaign.bases.Roider_RoiderBaseIntelV2;

/**
 * Author: SafariJohn
 */
public class Roider_SystemMusicScript implements EveryFrameScript {
    public final static String MUSIC_ID = "roider_star_system";

    private int index = 0;

    @Override
    public void advance(float amount) {
        if (index >= Global.getSector().getStarSystems().size()) index = 0;

        StarSystemAPI system = Global.getSector().getStarSystems().get(index++);
        MemoryAPI mem = system.getMemoryWithoutUpdate();

        // Music already set to something else
        if (mem.contains(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY)
                    && !MUSIC_ID.equals(mem.getString(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY))) {
            return;
        }

        // Check if a Roider Union or roider fringe base is the largest market in the system
        int biggest = 0;
        int biggestRoider = 0;
        for (MarketAPI m : Global.getSector().getEconomy().getMarkets(system)) {
            if (m.isPlayerOwned()) continue; // Don't count player markets

            if (m.getSize() > biggest) biggest = m.getSize();

            if ((m.getFactionId().equals(Roider_Factions.ROIDER_UNION)
                        || m.getId().startsWith(Roider_RoiderBaseIntelV2.MARKET_PREFIX)
                        && m.getSize() > biggestRoider)) {
                biggestRoider = m.getSize();
            }
        }

        boolean isRoiderSystem = biggest > 0 && biggestRoider == biggest;

        if (isRoiderSystem) {
            mem.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, MUSIC_ID);
        }
        else mem.unset(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY);

    }



    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }
}
