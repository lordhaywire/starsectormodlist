package scripts.campaign.retrofit;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.thoughtworks.xstream.XStream;
import ids.Roider_MemFlags;
import scripts.campaign.econ.Roider_Shipworks;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper.RetrofitData;

/**
 * Author: SafariJohn
 */
public class Roider_ShipworksRetrofitManager extends Roider_BaseRetrofitManager {

    public static void aliasAttributes(XStream x) {
    }

    public Roider_ShipworksRetrofitManager(String fitter, SectorEntityToken entity, FactionAPI faction) {
        super(fitter, entity, faction);
    }

    @Override
    public RetrofitData verifyData( String id,
                String fitter, String source, String target,
                double cost, double time, RepLevel rep,
                boolean commission) {
        // Recalculate cost if there's a market
        if (entity != null && entity.getMarket() != null
                    && !entity.getMarket().isPlanetConditionMarketOnly()) {
            cost = Roider_RetrofitsKeeper
                    .calculateCost(source, target, entity.getMarket());
        }

        // Discount from having alpha core
        if (entity.getMarket().getMemoryWithoutUpdate().getBoolean(Roider_MemFlags.SHIPWORKS_ALPHA)) {
            cost *=  (100f - Roider_Shipworks.ALPHA_DISCOUNT) / 100f;
        }

        return super.verifyData(id, fitter, source, target, cost,
                    time, RepLevel.VENGEFUL, false);
    }

}
