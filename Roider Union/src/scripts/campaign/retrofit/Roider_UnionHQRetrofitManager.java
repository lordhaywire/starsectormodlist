package scripts.campaign.retrofit;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Fitters;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_Ids.Roider_Submarkets;
import scripts.campaign.submarkets.Roider_UnionHQSubmarketPlugin;

/**
 * Author: SafariJohn
 */
public class Roider_UnionHQRetrofitManager extends Roider_BaseRetrofitManager {

    public static void aliasAttributes(XStream x) {
    }

    public Roider_UnionHQRetrofitManager(String fitter, SectorEntityToken entity, FactionAPI faction) {
        super(fitter, entity, faction);
    }

    public Roider_UnionHQRetrofitManager(String fitter, SectorEntityToken entity, FactionAPI faction, boolean withIntel) {
        super(fitter, entity, faction, withIntel);
    }

    @Override
    public String getFitter() {
        if (entity == null || entity.getMarket() == null) return super.getFitter();

        if (entity.getMarket().hasIndustry(Industries.HEAVYINDUSTRY)
                    || entity.getMarket().hasIndustry(Industries.ORBITALWORKS)
                    || entity.getMarket().hasIndustry(Roider_Industries.SHIPWORKS)) {
            return Roider_Fitters.FULL;
        }

        return Roider_Fitters.LIGHT;
    }

    @Override
    public void advanceImpl(float amount) {
        if (entity == null || entity.getMarket() == null || entity.getMarket().isPlanetConditionMarketOnly()) {
            endImmediately();
            return;
        }

        if (queued.isEmpty()) return;
        if (Global.getSector().isPaused()) return;

        if (faction.isAtBest(Factions.PLAYER, RepLevel.SUSPICIOUS)) {
            queued.get(0).pause("your reputation is too low");
            return;
        }

        SubmarketAPI unionHQ = entity.getMarket().getSubmarket(Roider_Submarkets.UNION_MARKET);
        if (unionHQ == null) return;

        boolean retrofitsEnabled = ((Roider_UnionHQSubmarketPlugin) unionHQ.getPlugin()).isEnabled(null);
        if (!retrofitsEnabled) {
            if (!queued.isEmpty()) {
                queued.get(0).pause("the Union HQ is disrupted");
            }

            return;
        } else {
            queued.get(0).unpause();
        }

        super.advanceImpl(amount);
    }
}
