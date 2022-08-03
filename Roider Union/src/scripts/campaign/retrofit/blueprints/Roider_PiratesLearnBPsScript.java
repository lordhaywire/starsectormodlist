package scripts.campaign.retrofit.blueprints;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.DelayedBlueprintLearnScript;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Fitters;
import java.util.*;
import scripts.campaign.retrofit.Roider_RetrofitVerifier;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper.RetrofitData;

/**
 * Author: SafariJohn
 */
public class Roider_PiratesLearnBPsScript implements EconomyUpdateListener, Roider_RetrofitVerifier {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_PiratesLearnBPsScript.class, "faction", "f");
        x.aliasAttribute(Roider_PiratesLearnBPsScript.class, "sources", "s");
        x.aliasAttribute(Roider_PiratesLearnBPsScript.class, "sourcesUpdated", "u");
    }

    private final FactionAPI faction;
    private transient Map<String, List<String>> sources;
    private transient boolean sourcesUpdated;

    public Roider_PiratesLearnBPsScript(FactionAPI faction) {
        this.faction = faction;
        sources = getSources();
        sourcesUpdated = false;
    }

    @Override
    public void economyUpdated() {
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            SubmarketAPI submarket = market.getSubmarket(Submarkets.SUBMARKET_BLACK);
            if (submarket == null) continue;

            CargoAPI cargo = submarket.getCargo();
            if (cargo == null) continue;

            delayedLearnBlueprintsFromTransaction(faction, cargo, 60f + 60 * (float) Math.random());
        }

        sourcesUpdated = false;
    }

	public void delayedLearnBlueprintsFromTransaction(FactionAPI faction, CargoAPI cargo, float daysDelay) {
		DelayedBlueprintLearnScript script = new DelayedBlueprintLearnScript(faction.getId(), daysDelay);

		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			SpecialItemPlugin plugin = stack.getPlugin();
			if (plugin instanceof Roider_RetrofitBlueprintPlugin) {
				Roider_RetrofitBlueprintPlugin bp = (Roider_RetrofitBlueprintPlugin) plugin;

                String id = bp.getProvidedShip();
                if (faction.knowsShip(id)) continue;
                if (!sourceKnown(id)) continue;
                script.getShips().add(id);
                cargo.removeItems(stack.getType(), stack.getData(), 1);
			}
		}

		if (!script.getFighters().isEmpty() || !script.getShips().isEmpty()) {
			Global.getSector().addScript(script);
			cargo.sort();
		}
	}

    private boolean sourceKnown(String hullId) {
        if (!sourcesUpdated) {
            sources = getSources();
            sourcesUpdated = true;
        }

        List<String> rSources = sources.get(hullId);
        if (rSources == null) return false;

        for (String source : rSources) {
            if (faction.knowsShip(source)) return true;
        }

        return false;
    }

    private Map<String, List<String>> getSources() {
        Map<String, List<String>> rSources = new HashMap<>();
        Set<String> targets = new HashSet<>();

        // Get retrofits
        List<RetrofitData> retrofits = Roider_RetrofitsKeeper.getRetrofits(this, Roider_Fitters.ALL);

        // Get targets
        for (RetrofitData data : retrofits) {
            targets.add(data.targetHull);
        }

        // Save sources for each target
        for (String target : targets) {
            List<String> sourceList = new ArrayList<>();
            for (RetrofitData data : retrofits) {
                if (data.targetHull.equals(target)) {
                    sourceList.add(data.sourceHull);
                }
            }

            rSources.put(target, sourceList);
        }

        return rSources;
    }

    @Override
    public void commodityUpdated(String commodityId) {}

    @Override
    public boolean isEconomyListenerExpired() {
        return false;
    }

    @Override
    public RetrofitData verifyData(String id, String fitter,
                String source, String target, double cost,
                double time, RepLevel rep, boolean commission) {
        return new RetrofitData(id, fitter, source, target, cost,
                    time, rep, commission);
    }
}
