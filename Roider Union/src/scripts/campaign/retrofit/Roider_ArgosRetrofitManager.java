package scripts.campaign.retrofit;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Fitters;
import ids.Roider_Ids.Roider_Settings;
import java.util.List;
import org.magiclib.util.MagicSettings;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper.RetrofitData;

/**
 * Author: SafariJohn
 */
public class Roider_ArgosRetrofitManager extends Roider_BaseRetrofitManager {

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_ArgosRetrofitManager.class, "offerings", "o");
    }

    private final List<String> offerings;

    public Roider_ArgosRetrofitManager(SectorEntityToken entity,
                FactionAPI faction, List<String> offerings) {
        super(Roider_Fitters.ARGOS, entity, faction, false);
        this.offerings = offerings;
    }

    @Override
    public RetrofitData verifyData(String id, String fitter,
                String source, String target, double cost,
                double time, RepLevel rep, boolean commission) {

        // Recalculate cost if there's a market
        if (entity != null && entity.getMarket() != null
                    && !entity.getMarket().isPlanetConditionMarketOnly()) {
            cost = Roider_RetrofitsKeeper
                    .calculateCost(source, target, entity.getMarket());
        }

        // Target hull may not be available at a given NPC retrofitter
        if (!faction.isPlayerFaction() && offerings != null && !offerings.contains(target)) return null;

        // Ignore rep level and commission for player
        if (faction.getId().equals(Factions.PLAYER)) {
            rep = RepLevel.VENGEFUL;
            commission = false;
        }

        // Some other factions ignore rep and/or commission
        if (MagicSettings.getList(Roider_Settings.MAGIC_ID, Roider_Settings.APR_NO_REP).contains(faction.getId())) {
            rep = RepLevel.VENGEFUL;
        }
        if (MagicSettings.getList(Roider_Settings.MAGIC_ID, Roider_Settings.APR_NO_COM).contains(faction.getId())) {
            commission = false;
        }

        // Factions that don't offer commissions don't consider commissions, naturally
        if (!faction.getCustomBoolean(Factions.CUSTOM_OFFERS_COMMISSIONS)) {
            commission = false;
        }

        return new RetrofitData(id, fitter, source, target, cost,
                    0, rep, commission);
    }

    @Override
    protected void notifyEnded() {}
}
