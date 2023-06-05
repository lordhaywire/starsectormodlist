package scripts.campaign.fleets.expeditions;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids;
import java.util.List;
import java.util.Random;
import org.magiclib.util.MagicSettings;

/**
 * Author: SafariJohn
 */
public class Roider_ExpeditionStashPickupScript implements Script {
    public static final String STASH_ENTITY_KEY = "roider_majorStash";
    public static final String NULL_ENTITY_KEY = "roider_nullStash";

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_ExpeditionStashPickupScript.class, "token", "t");
        x.aliasAttribute(Roider_ExpeditionStashPickupScript.class, "fleet", "f");
    }

    private SectorEntityToken token;
    private CampaignFleetAPI fleet;

    public Roider_ExpeditionStashPickupScript(CampaignFleetAPI fleet, SectorEntityToken target) {
        this.token = target;
        this.fleet = fleet;

        if (target == null) {
            this.token = fleet.getContainingLocation().createToken(0, 0);
            this.token.addTag(NULL_ENTITY_KEY);
        }
    }

    @Override
    public void run() {
        if (token.hasTag(NULL_ENTITY_KEY)) {
            genMinorStashAndAdd(fleet, null);
            Misc.fadeAndExpire(token);
            token = null;
            return;
        }

        if (token.isExpired()) return;

        String customType = token.getCustomEntityType();

        if (!token.hasTag(Tags.HAS_INTERACTION_DIALOG) && token.getCustomData().containsKey(STASH_ENTITY_KEY)) {
            SectorEntityToken stash = (SectorEntityToken) token.getCustomData().get(STASH_ENTITY_KEY);

            if (!stash.isExpired()) {
                if (stash.getCustomPlugin() instanceof Roider_MajorLootStashPlugin) {
                    Random random = new Random(stash.getMemoryWithoutUpdate().getLong(MemFlags.SALVAGE_SEED));

                    WeightedRandomPicker<String> picker = new WeightedRandomPicker(random);
                    picker.add(Entities.STATION_RESEARCH, 0.1f);
                    picker.add(Entities.STATION_MINING, 1f);
                    picker.add(Entities.ORBITAL_HABITAT, 5f);
                    picker.add(Entities.WRECK, 50f);
                    String type = picker.pick();

                    if (type.equals(Entities.WRECK)) {
                        pickWreck(fleet, stash, true);
                    } else {
                        genDropAndAdd(fleet, stash);
                    }
                } else {
                    if (stash.getCustomEntityType().equals(Entities.WRECK)) {
                        addWreckToFleet(fleet, stash);
                    } else {
                        genDropAndAdd(fleet, stash);
                    }
                }

                Misc.fadeAndExpire(stash);
            }
        } else if (customType != null && customType.equals(Entities.WRECK)) {
            addWreckToFleet(fleet, token);
        } else {
            genDropAndAdd(fleet, token);
        }

        Misc.fadeAndExpire(token);
        token = null;
    }

    public static void genMinorStashAndAdd(CampaignFleetAPI fleet, Random random) {
        if (random == null) random = new Random();

        LocationAPI system = fleet.getContainingLocation();

        WeightedRandomPicker<String> picker = new WeightedRandomPicker();
        picker.add(Entities.WRECK, 50f);
        picker.add(Entities.WEAPONS_CACHE, 4f);
        picker.add(Entities.WEAPONS_CACHE_SMALL, 10f);
        picker.add(Entities.WEAPONS_CACHE_HIGH, 4f);
        picker.add(Entities.WEAPONS_CACHE_SMALL_HIGH, 10f);
        picker.add(Entities.WEAPONS_CACHE_LOW, 4f);
        picker.add(Entities.WEAPONS_CACHE_SMALL_LOW, 10f);
        picker.add(Entities.SUPPLY_CACHE, 4f);
        picker.add(Entities.SUPPLY_CACHE_SMALL, 10f);
        picker.add(Entities.EQUIPMENT_CACHE, 4f);
        picker.add(Entities.EQUIPMENT_CACHE_SMALL, 10f);
        String lootId = picker.pick();

        if (lootId.equals(Entities.WRECK)) {
            List<String> factions = MagicSettings.getList(Roider_Ids.Roider_Settings.MAGIC_ID,
                        Roider_Ids.Roider_Settings.EXPEDITION_LOOT_FACTIONS);

            picker.clear();
            picker.addAll(factions);
            int iter = 0;
            do {
                if (iter > 110) break;

                String faction = picker.pick();
                DerelictShipData params = DerelictShipEntityPlugin.createRandom(faction, null, random, DerelictShipEntityPlugin.getDefaultSModProb());
                iter++;

                if (params != null) {
                    if (params.ship.getVariant().getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP && iter < 100) {
                        continue;
                    }

                    CustomCampaignEntityAPI wreck = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(random, system,
                                                    Entities.WRECK, Factions.NEUTRAL, params);

                    addWreckToFleet(fleet, wreck);

                    break;
                }
            } while (true);
        } else {
            CustomCampaignEntityAPI e = system.addCustomEntity(null, "", lootId, Factions.NEUTRAL, null);
            genDropAndAdd(fleet, e);
        }

        fleet.getFleetData().sort();
    }

    public static void genMajorStashAndAdd(CampaignFleetAPI fleet, Random random) {
        if (random == null) random = new Random();

        LocationAPI system = fleet.getContainingLocation();

        WeightedRandomPicker<String> picker = new WeightedRandomPicker();
        picker.add(Entities.STATION_RESEARCH, 0.1f);
        picker.add(Entities.STATION_MINING, 1f);
        picker.add(Entities.ORBITAL_HABITAT, 5f);
        picker.add(Entities.WRECK, 50f);
        String lootId = picker.pick();

        if (lootId.equals(Entities.WRECK)) {
            List<String> factions = MagicSettings.getList(Roider_Ids.Roider_Settings.MAGIC_ID,
                        Roider_Ids.Roider_Settings.EXPEDITION_LOOT_FACTIONS);

            picker.clear();
            picker.addAll(factions);
            int iter = 0;
            do {
                if (iter > 110) break;

                String faction = picker.pick();
                DerelictShipData params = DerelictShipEntityPlugin.createRandom(faction, null, random, DerelictShipEntityPlugin.getDefaultSModProb());
                iter++;

                if (params != null) {
                    if (params.ship.getVariant().getHullSize() != ShipAPI.HullSize.CAPITAL_SHIP && iter < 100) {
                        continue;
                    }

                    CustomCampaignEntityAPI wreck = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(random, system,
                                                    Entities.WRECK, Factions.NEUTRAL, params);

                    addWreckToFleet(fleet, wreck);

                    break;
                }
            } while (true);
        } else {
            CustomCampaignEntityAPI e = system.addCustomEntity(null, "", lootId, Factions.NEUTRAL, null);
            genDropAndAdd(fleet, e);
        }

        fleet.getFleetData().sort();
    }

    public static void genDropAndAdd(CampaignFleetAPI fleet, SectorEntityToken e) {
        if (!e.hasTag(Tags.HAS_INTERACTION_DIALOG)) return;

        BaseThemeGenerator.genCargoFromDrop(e);
        List<SalvageEntityGenDataSpec.DropData> dropV = e.getDropValue();
        List<SalvageEntityGenDataSpec.DropData> dropR = e.getDropRandom();

        for (SalvageEntityGenDataSpec.DropData data : dropV) {
            fleet.addDropValue(data.clone());
        }

        for (SalvageEntityGenDataSpec.DropData data : dropR) {
            fleet.addDropRandom(data.clone());
        }
    }

    public static void pickWreck(CampaignFleetAPI fleet, SectorEntityToken e, boolean major) {
        List<String> factions = MagicSettings.getList(Roider_Ids.Roider_Settings.MAGIC_ID,
                    Roider_Ids.Roider_Settings.EXPEDITION_LOOT_FACTIONS);

        Random random = new Random(e.getMemoryWithoutUpdate().getLong(MemFlags.SALVAGE_SEED));

        WeightedRandomPicker<String> picker = new WeightedRandomPicker(random);
        picker.addAll(factions);
        int iter = 0;
        do {
            if (iter > 110) break;

            String faction = picker.pick();
            DerelictShipData params = DerelictShipEntityPlugin.createRandom(faction, null, random, DerelictShipEntityPlugin.getDefaultSModProb());
            iter++;

            if (params != null) {
                HullSize size = params.ship.getVariant().getHullSize();
                if (major && size != ShipAPI.HullSize.CAPITAL_SHIP && iter < 100) {
                    continue;
                }
                if (!major && size == ShipAPI.HullSize.CAPITAL_SHIP && iter < 100) {
                    continue;
                }

                CustomCampaignEntityAPI salEntity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(random, e.getContainingLocation(),
                                                Entities.WRECK, Factions.NEUTRAL, params);

                e.getContainingLocation().removeEntity(salEntity);

                addWreckToFleet(fleet, salEntity);
                break;
            }
        } while (true);
    }

    public static void addWreckToFleet(CampaignFleetAPI fleet, SectorEntityToken e) {
        CustomCampaignEntityPlugin plugin = e.getCustomPlugin();
        if (plugin != null && plugin instanceof DerelictShipEntityPlugin) {
            DerelictShipData data = ((DerelictShipEntityPlugin) plugin).getData();

            FleetMemberAPI mem = fleet.getFleetData().addFleetMember(data.ship.variantId);
            mem.setVariant(data.ship.getVariant(), false, false);
            mem.getRepairTracker().setMothballed(true);
            mem.getVariant().addTag(Tags.SHIP_RECOVERABLE);
        }

        fleet.getFleetData().sort();
    }
}
