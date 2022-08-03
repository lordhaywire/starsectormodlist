package scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.ArrayList;
import java.util.List;
import static scripts.campaign.bases.Roider_RoiderBaseIntelV2.MARKET_PREFIX;

/**
 * Author: SafariJohn
 */
public class Roider_FringeStationCleaner {
    public static void removeOrphanedFringeStations(SectorAPI sector) {
        List<SectorEntityToken> orphans = new ArrayList<>();

        // Find orphans
        for (LocationAPI loc : sector.getAllLocations()) {
            for (SectorEntityToken entity : loc.getAllEntities()) {
                MarketAPI market = entity.getMarket();
                if (market == null) continue;

                if (market.getId().startsWith(MARKET_PREFIX)
                            && !market.isInEconomy()) {
                    orphans.add(entity);
                }
            }
        }

        // Clean up orphans
        for (SectorEntityToken entity : orphans) {
            Global.getLogger(Roider_FringeStationCleaner.class)
                        .info("Cleaning up orphaned fringe station at "
                                    + entity.getStarSystem().getName());

            CampaignFleetAPI fleet = Misc.getStationFleet(entity);
            if (fleet != null) fleet.despawn();

            MarketAPI market = entity.getMarket();
            market.getConnectedEntities().remove(entity);

            entity.setMarket(null);

            Misc.fadeAndExpire(entity);
//            entity.getStarSystem().removeEntity(entity);

        }
    }
}
