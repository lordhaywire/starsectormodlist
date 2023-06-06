package data.scripts;



import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import data.scripts.world.systems.DE_Andor;
import data.scripts.world.systems.DE_Askonia;
import data.scripts.world.systems.DE_Hesiod;
import data.scripts.world.systems.DE_Valhalla;
import de.unkrig.commons.nullanalysis.Nullable;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Gen implements SectorGeneratorPlugin {

    private static final Logger log = Global.getLogger(Gen.class);
    public static boolean DEremovenondiktatfeatures = Global.getSettings().getBoolean("DEremovenondiktatfeatures");
    public static boolean DEenablelitemode = Global.getSettings().getBoolean("DEenablelitemode");
    public static boolean DEenablefortressmode = Global.getSettings().getBoolean("DEenablefortressmode");
    public static boolean DEdisablelobers = Global.getSettings().getBoolean("DEdisablelobers");
    //import static data.scripts.Gen.addMarketplace;
    //Shorthand function for adding a market - used in place of economy.json so I can restrict them with the boolean
    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name,
                                           int size, ArrayList<String> marketConditions, ArrayList<String> submarkets, ArrayList<String> industries, float tarrif,
                                           boolean freePort, boolean withJunkAndChatter) {
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String planetID = primaryEntity.getId();
        String marketID = planetID + "_market";

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        newMarket.getTariff().modifyFlat("generator", tarrif);

        //Adds submarkets
        if (null != submarkets) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        //Adds market conditions
        for (String condition : marketConditions) {
            newMarket.addCondition(condition);
        }

        //Add market industries
        for (String industry : industries) {
            newMarket.addIndustry(industry);
        }

        //Sets us to a free port, if we should
        newMarket.setFreePort(freePort);

        //Adds our connected entities, if any
        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        globalEconomy.addMarket(newMarket, withJunkAndChatter);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        //Finally, return the newly-generated market
        return newMarket;
    }

    @Override
    public void generate(SectorAPI sector) {
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            DEremovenondiktatfeatures = lunalib.lunaSettings.LunaSettings.getBoolean("Diktat Enhancement", "DEremovenondiktatfeatures");
            DEenablefortressmode = lunalib.lunaSettings.LunaSettings.getBoolean("Diktat Enhancement", "DEenablefortressmode");
            DEenablelitemode = lunalib.lunaSettings.LunaSettings.getBoolean("Diktat Enhancement", "DEenablelitemode");
            DEdisablelobers = lunalib.lunaSettings.LunaSettings.getBoolean("Diktat Enhancement", "DEdisablelobers");
        }

        new DE_Askonia().generate(sector);
        if (!DEenablelitemode) {
            new DE_Andor().generate(sector);
            new DE_Hesiod().generate(sector);
            if (!DEremovenondiktatfeatures) {
                new DE_Valhalla().generate(sector);
            }
        }
    }

}




