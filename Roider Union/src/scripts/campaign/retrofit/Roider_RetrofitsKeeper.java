package scripts.campaign.retrofit;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Settings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.magiclib.util.MagicSettings;

/**
 * Author: SafariJohn
 */
public class Roider_RetrofitsKeeper {

    public static void aliasAttributes(XStream x) {
//        x.aliasAttribute(Roider_RetrofitsKeeper.class, "allData", "a");

        x.alias("roider_retData", RetrofitData.class);
        RetrofitData.aliasAttributes(x);
    }

    public static final String KEY = "$roider_retrofitsKeeper";

    public static final String RETROFIT_CSV = "data/retrofit/retrofits.csv";
    public static final String CONFIG_CSV = "data/config/modFiles/roider_retrofits.csv";

    public static final String MASTER_MOD;
    static {
        if (Global.getSettings().getModManager().isModEnabled("roider_dev")) MASTER_MOD = "roider_dev";
        else if (Global.getSettings().getModManager().isModEnabled("roider")) MASTER_MOD = "roider";
        // For testing old versions by allowing them to have roider### IDs
        else {
            String modId = "roider";
            for (ModSpecAPI mod : Global.getSettings().getModManager().getEnabledModsCopy()) {
                if (mod.getId().startsWith("roider")) {
                    modId = mod.getId();
                    break;
                }
            }

            MASTER_MOD = modId;
        }
    }

    public static class RetrofitData {
        public static void aliasAttributes(XStream x) {
            x.aliasAttribute(RetrofitData.class, "id", "i");
            x.aliasAttribute(RetrofitData.class, "fitter", "f");
            x.aliasAttribute(RetrofitData.class, "sourceHull", "s");
            x.aliasAttribute(RetrofitData.class, "targetHull", "tar");
            x.aliasAttribute(RetrofitData.class, "cost", "c");
            x.aliasAttribute(RetrofitData.class, "time", "t");
            x.aliasAttribute(RetrofitData.class, "reputation", "r");
            x.aliasAttribute(RetrofitData.class, "commission", "com");
        }

        public final String id;
        public final String fitter;
        public final String sourceHull;
        public final String targetHull;
        public final double cost; // credits
        public final double time; // days
        public final RepLevel reputation;
        public final boolean commission;

        public RetrofitData(String id, String fitter, String sourceHull, String targetHull,
                    double cost, double time, RepLevel reputation, boolean commission) {
            this.id = id;
            this.fitter = fitter;
            this.sourceHull = sourceHull;
            this.targetHull = targetHull;
            this.cost = cost;
            this.time = time;
            this.reputation = reputation;
            this.commission = commission;
        }

        public boolean equals(RetrofitData data) {
            return id.equals(data.id);
        }
    }

    public static Roider_RetrofitsKeeper getInstance() {
        Roider_RetrofitsKeeper instance = (Roider_RetrofitsKeeper) Global.getSector().getMemoryWithoutUpdate().get(KEY);
        if (instance == null) {
            instance = new Roider_RetrofitsKeeper();
            Global.getSector().getMemoryWithoutUpdate().set(KEY, instance);
        }

        return instance;
    }


    private transient List<RetrofitData> allData;

    public Roider_RetrofitsKeeper() {
        initAllData();
    }

    private void initAllData() {
        allData = new ArrayList<>();

        loadCSV(CONFIG_CSV, allData);
        // Load any retrofits in the old retrofits folder
        loadCSV(RETROFIT_CSV, allData);
    }


    public static List<RetrofitData> getRetrofits(Roider_RetrofitVerifier verifier, String ... fitters) {
        return getInstance().getRetrofits(verifier, true, fitters);
    }

    public List<RetrofitData> getRetrofits(Roider_RetrofitVerifier verifier, boolean dis, String ... fitters) {
        if (allData == null) {
            initAllData();
        }

        List<RetrofitData> retrofits = new ArrayList<>();
        for (RetrofitData data : allData) {
            if (isFittedHere(data.fitter, fitters)) {
                RetrofitData newData = verifier.verifyData(data.id, data.fitter,
                            data.sourceHull, data.targetHull, data.cost,
                            data.time, data.reputation, data.commission);
                if (newData != null) retrofits.add(newData);
            }
        }

        return retrofits;
    }


    private void loadCSV(String csvLoc, List<RetrofitData> retrofits) {
        JSONArray csv;
        try {
            csv = Global.getSettings().getMergedSpreadsheetDataForMod("id", csvLoc, getModId());
        } catch (RuntimeException | IOException | JSONException ex) {
            Logger.getLogger(Roider_BaseRetrofitManager.class.getName()).log(Level.SEVERE, null, ex);
            csv = null;
        }

        if (csv != null) {
            try {
                for (int i = 0; i < csv.length(); i++) {
                    JSONObject o = csv.getJSONObject(i);

                    String id = o.getString("id");
                    // Don't add retrofits that are already defined by the shared modFiles csv
                    if (alreadyLoaded(retrofits, id)) continue;

                    String fitters = o.getString("fitter");

                    String source = o.getString("source");
                    String target = o.getString("target");

                    // Assert the hulls are loaded
                    try {
                        Global.getSettings().getHullSpec(source);
                        Global.getSettings().getHullSpec(target);
                    } catch (Exception ex) { continue; }


                    // If a cost is specified, use that
                    double cost;
                    try {
                        cost = o.getInt("cost");
                    } catch (JSONException ex) {
                        cost = calculateCost(source, target, null);
                    }



                    double time = Math.max(0, o.getDouble("time"));
                    RepLevel rep = getReputation(o.getInt("reputation"));
                    boolean commission = o.getBoolean("commission");

                    retrofits.add(new RetrofitData(id, fitters, source, target, cost, time, rep, commission));
                }
            } catch (JSONException ex) {
                Logger.getLogger(Roider_BaseRetrofitManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static boolean isFittedHere(String availableFitters,
                String ... activeFitters) {
        String[] fitters = availableFitters.split(",");

        for (String avail : fitters) {
            for (String active : activeFitters) {
                if (avail.trim().equals(active.trim())) return true;
            }
        }

        return false;
    }

    public static double calculateCost(String sourceHull,
                String targetHull, MarketAPI market) {
        double cost;

        // Tariff is from the market
        float tariff = 1f;
        if (market != null) {
            tariff += market.getTariff().getModifiedValue();
        }

        // Base cost is the target hull's cost * buy price mult
        ShipHullSpecAPI targetSpec = Global.getSettings().getHullSpec(targetHull);
        float buyMult = Global.getSettings().getFloat("shipBuyPriceMult");
        cost = targetSpec.getBaseValue() * buyMult * tariff;

        // The source hull's cost * sell price mult is subtracted
        ShipHullSpecAPI sourceSpec = Global.getSettings().getHullSpec(sourceHull);
        float sellMult = Global.getSettings().getFloat("shipSellPriceMult");
        float sellPrice = sourceSpec.getBaseValue() * sellMult;
        cost -= sellPrice - sellPrice * (tariff - 1f);

        // The total is multiplied by the conversion mult
        float convMult = MagicSettings.getFloat(Roider_Settings.MAGIC_ID,
                    Roider_Settings.CONVERSION_PRICE_MULT);
        cost *= convMult;

        // Round to nearest 1k if over 100k
        if (cost >= 100000) cost = Math.round(cost / 1000) * 1000;
        // If lower than to nearest 100
        else cost = Math.round(cost / 100) * 100;

        return cost;
    }

    public static RepLevel getReputation(int repInt) {
        switch (repInt) {
            case  4: return RepLevel.COOPERATIVE;
            case  3: return RepLevel.FRIENDLY;
            case  2: return RepLevel.WELCOMING;
            case  1: return RepLevel.FAVORABLE;
            case  0: return RepLevel.NEUTRAL;
            case -1: return RepLevel.SUSPICIOUS;
            case -2: return RepLevel.INHOSPITABLE;
            case -3: return RepLevel.HOSTILE;
            case -4: return RepLevel.VENGEFUL;
            default: return RepLevel.NEUTRAL;
        }
    }

    public static boolean alreadyLoaded(List<RetrofitData> retrofits, String id) throws JSONException {
        for (RetrofitData data : retrofits) {
            if (data.id.equals(id)) return true;
        }

        return false;
    }

    private String getModId() {
        return MASTER_MOD;
    }
}
