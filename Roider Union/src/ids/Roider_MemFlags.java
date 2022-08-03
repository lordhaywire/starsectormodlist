package ids;

/**
 * Author: SafariJohn
 */
public class Roider_MemFlags {
    // Retrofit managers are kept in their market's memory.
    public static final String RETROFITTER = "$roider_retrofitManager";
    public static final String SW_RETROFITTER = "$roider_swRetrofitManager";

    // RetrofitAccess variables
    public static final String TALK_RETROFITS = "$roider_talkedAboutRetrofits";
    public static final String FEE_PAID = "$roider_retrofitFeePaid";
    public static final String ACCESS_FEE = "$roider_retrofitFee";
    public static final String STORAGE_FEE = "$roider_retrofitStorageFeeAdded";

    // Stored in the market's memory.
    public static final String UNION_HQ_FUNCTIONAL = "$roider_hq_functional";
    public static final String SHIPWORKS_FUNCTIONAL = "$roider_shipworks_functional";
    public static final String SHIPWORKS_ALPHA = "$roider_shipworks_alphaCore";

    // Roider Union sub-faction commission
    public static final String ROIDER_COMMISSION = "$roider_commission";

    // Blocks dives and Union HQs from mining
    public static final String CLAIMED = "$roider_miningBlocked";

    // Mark where fringe Union HQs are
    public static final String FRINGE_HQ = "$roider_fringeUnionHQ";

    // Tech expeditions
    public static final String EXPEDITION_LOOT = "$roider_expeditionLoot";
    public static final String EXPEDITION_LOOT_MAJOR = "$roider_expeditionLootMajor";
    public static final String EXPEDITION_FACTION = "$roider_expeditionFaction";
    public static final String EXPEDITION_MARKET = "$roider_expeditionSource";
    public static final String THIEF_KEY = "$roider_thief";

    public static final String EXPEDITION_WRECK_PLUGIN = "$roider_expeditionWreckPlugin";

    // Conversion fleets
    public static final String APR_OFFERINGS = "$roider_aprOfferings"; // List<String>
    public static final String APR_RETROFITTING = "$roider_aprIsRetrofitting";
    public static final String APR_IGNORE_REP = "$roider_aprIgnoreRep";
    public static final String APR_IGNORE_COM = "$roider_aprIgnoreCommission";
    public static final String APR_IGNORE_TRANSPONDER = "$roider_aprIgnoreTransponder";

}
