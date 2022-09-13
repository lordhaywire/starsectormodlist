package scripts;

import scripts.campaign.fleets.expeditions.*;
import scripts.campaign.cleanup.Roider_MinerTokenCleaner;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.Roider_StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.thoughtworks.xstream.XStream;
import exerelin.campaign.ExerelinSetupData;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.SectorManager;
import ids.Roider_Ids.Roider_Factions;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import scripts.campaign.Roider_ArmatureSingleBPAdderScript;
import scripts.campaign.Roider_FringeStationCleaner;
import scripts.campaign.Roider_IndieRepMatcher;
import scripts.campaign.cleanup.Roider_MadMIDASHealer;
import scripts.campaign.bases.*;
import scripts.campaign.cleanup.Roider_ExpeditionLootCleaner;
import scripts.campaign.econ.Roider_Dives;
import scripts.campaign.econ.Roider_Shipworks;
import scripts.campaign.econ.Roider_ShipworksBlueprint;
import scripts.campaign.econ.Roider_UnionHQBlueprint;
import scripts.campaign.fleets.*;
import scripts.campaign.fleets.Roider_ExpeditionTrap.Roider_ExpeditionTrapCreator;
import scripts.campaign.intel.Roider_ConversionFleetIntel;
import scripts.campaign.intel.Roider_FactionCommissionIntel;
import scripts.campaign.intel.bar.Roider_RetrofitBarEvent;
import scripts.campaign.intel.bar.Roider_RoiderBaseRumorBarEvent;
import scripts.campaign.retrofit.*;
import scripts.campaign.retrofit.blueprints.*;
import scripts.campaign.rulecmd.expeditionSpecials.Roider_PingTrapScript;
import scripts.campaign.submarkets.*;
import scripts.world.Roider_Gen;
import scripts.world.Roider_SystemMusicScript;

public class Roider_ModPlugin extends BaseModPlugin {
    public static transient boolean hasNexerelin = false;
    public static transient boolean hasStarshipLegends = false;

    @Override
    public void onApplicationLoad() {
        boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
        if (!hasLazyLib) throw new RuntimeException("Roider Union requires LazyLib!");

        boolean hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
        if (!hasMagicLib) throw new RuntimeException("Roider Union requires MagicLib!");

        boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            ShaderLib.init();
            LightData.readLightDataCSV("data/lights/roider_light_data.csv");
            TextureData.readTextureDataCSV("data/lights/roider_texture_data.csv");
        }

        hasNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");

        hasStarshipLegends = Global.getSettings().getModManager().isModEnabled("sun_starship_legends");

        boolean hasfastsave = Global.getSettings().getModManager().isModEnabled("fastsave");
        if (hasfastsave) throw new RuntimeException("Roider Union is not compatible with the Fast Save mod!");
    }


    @Override
    public void onNewGame() {
        SectorAPI sector = Global.getSector();

        Roider_Gen.initFactionRelationships(sector);

        SharedData.getData().getPersonBountyEventData().addParticipatingFaction(Roider_Factions.ROIDER_UNION);

        // Fixing vanilla bug
        StarSystemAPI vanillaBroken = Global.getSector().getStarSystem("Unknown Location");
        if (vanillaBroken != null) vanillaBroken.setType(StarSystemGenerator.StarSystemType.NEBULA);

		if (!hasNexerelin || SectorManager.getManager().isCorvusMode()) {
            Roider_Gen.generate(sector);
        }
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        SectorAPI sector = Global.getSector();

		if (!hasNexerelin || SectorManager.getManager().isCorvusMode()) {

            Roider_Gen.addCoreDives(sector);

            Roider_Gen.assignCustomAdmins();
            Roider_Gen.assignRandomAdmins();
        }

        if (nexerelinRoidersEnabled()) {
            Roider_Gen.placeFringeRoiderHQs(sector);

            if (!sector.hasScript(Roider_RoiderBaseManager.class)) {
                sector.addScript(new Roider_RoiderBaseManager());
            }
        }
    }

    @Override
    public void onNewGameAfterTimePass() {
        if (hasNexerelin && !SectorManager.getManager().isCorvusMode()
                    && nexerelinRoidersEnabled()) {
            Roider_Gen.addNexRandomModeDives();
            Roider_Gen.addNexRandomRockpiper();
        }

        if (nexerelinRoidersEnabled()) {
            Global.getSector().addScript(new Roider_TechExpeditionFleetRouteManager());
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        SectorAPI sector = Global.getSector();
        if (!newGame) {
            Roider_FringeStationCleaner.removeOrphanedFringeStations(sector);
        }

        if (!sector.hasScript(Roider_Savebreak.class)) {
            sector.addScript(new Roider_Savebreak());
        }

        if (!sector.getCharacterData().getAbilities().contains("roider_retrofit")) {
            sector.addTransientScript(new Roider_ArgosAbilityAdderScript());
        }

        if (!sector.hasScript(Roider_ConversionFleetRouteManager.class)) {
//            sector.addScript(new Roider_ConversionFleetRouteManager(15, 30));
//            sector.addScript(new Roider_ConversionFleetRouteManager(1, 2));
        }

        if (!sector.hasScript(Roider_PiratesLearnBPsScript.class)) {
            sector.getEconomy().addUpdateListener(new Roider_PiratesLearnBPsScript(Global.
                        getSector().getFaction(Factions.PIRATES)));
        }

//        if (!sector.hasScript(Roider_InsuranceTracker.class)) {
//            Roider_InsuranceTracker insurance = Roider_InsuranceTracker.getInsurance();
//            sector.addScript(insurance);
//            sector.getListenerManager().addListener(insurance);
//
////            Roider_InsuranceTracker.setPlayerInsured(true);
//        }

        sector.addTransientScript(new Roider_ArmatureSingleBPAdderScript());

        sector.addTransientScript(new Roider_MadMIDASHealer());
        sector.addTransientScript(new Roider_SystemMusicScript());
    }

    /**
     * Detects whether the Roider Union faction is disabled in Nexerelin random mode.
     * @return
     */
    public static boolean nexerelinRoidersEnabled() {
        if (!hasNexerelin || SectorManager.getManager().isCorvusMode()) return true;

        ExerelinSetupData data = ExerelinSetupData.getInstance();
        boolean roidersEnabled = data.factions.get(Roider_Factions.ROIDER_UNION);

		String playerFaction = PlayerFactionStore.getPlayerFactionIdNGC();

        if (playerFaction.equals(Roider_Factions.ROIDER_UNION)) return true;

        return roidersEnabled;
    }

    @Override
    public void configureXStream(XStream x) {
        // Backwards compatible
        x.alias("roider_savebreak_1_4_1", Roider_Savebreak.class);

        x.alias("roider_ssg", Roider_StarSystemGenerator.class);

        x.alias("roider_baseIntel", Roider_RoiderBaseIntel.class);
        Roider_RoiderBaseIntel.aliasAttributes(x);

        x.alias("roider_baseIntel", Roider_RoiderBaseIntelV2.class);
        Roider_RoiderBaseIntelV2.aliasAttributes(x);

        x.alias("roider_baseMan", Roider_RoiderBaseManager.class);
        Roider_RoiderBaseManager.aliasAttributes(x);

        x.alias("roider_hqBaseIntel", Roider_RoiderHQBaseIntel.class);
        Roider_RoiderHQBaseIntel.aliasAttributes(x);


        x.alias("roider_dives", Roider_Dives.class);
        Roider_Dives.aliasAttributes(x);

        x.alias("roider_sw", Roider_Shipworks.class);
        Roider_Shipworks.aliasAttributes(x);

        x.alias("roider_hqBP", Roider_UnionHQBlueprint.class);
        Roider_UnionHQBlueprint.aliasAttributes(x);

        x.alias("roider_swBP", Roider_ShipworksBlueprint.class);
        Roider_ShipworksBlueprint.aliasAttributes(x);


        x.alias("roider_minerMan", Roider_MinerRouteManager.class);
        Roider_MinerRouteManager.aliasAttributes(x);

        x.alias("roider_mineAI", Roider_MinerRouteAI.class);
        Roider_MinerRouteAI.aliasAttributes(x);

        x.alias("roider_mTClean", Roider_MinerTokenCleaner.class);
        Roider_MinerTokenCleaner.aliasAttributes(x);

        x.alias("roider_hqPMan", Roider_HQPatrolManager.class);
        Roider_HQPatrolManager.aliasAttributes(x);


        x.alias("roider_retBar", Roider_RetrofitBarEvent.class);
        Roider_RetrofitBarEvent.aliasAttributes(x);

        x.alias("roider_baseRumorBar", Roider_RoiderBaseRumorBarEvent.class);
        Roider_RoiderBaseRumorBarEvent.aliasAttributes(x);


        // Pretty sure this is unused
        x.alias("roider_comIntel", Roider_FactionCommissionIntel.class);
        Roider_FactionCommissionIntel.aliasAttributes(x);

        x.alias("roider_retKeep", Roider_RetrofitsKeeper.class);
        Roider_RetrofitsKeeper.aliasAttributes(x);


        x.alias("roider_pirLrnBP", Roider_PiratesLearnBPsScript.class);
        Roider_PiratesLearnBPsScript.aliasAttributes(x);

        x.alias("roider_retBP", Roider_RetrofitBlueprintPlugin.class);
        Roider_RetrofitBlueprintPlugin.aliasAttributes(x);


        x.alias("roider_baseRetMan", Roider_BaseRetrofitManager.class);
        Roider_BaseRetrofitManager.aliasAttributes(x);

        x.alias("roider_baseRetPlug", Roider_BaseRetrofitPlugin.class);
        Roider_BaseRetrofitPlugin.aliasAttributes(x);

        x.alias("roider_hqRetMan", Roider_UnionHQRetrofitManager.class);
        Roider_UnionHQRetrofitManager.aliasAttributes(x);

        x.alias("roider_hqRetPlug", Roider_UnionHQRetrofitPlugin.class);
        Roider_UnionHQRetrofitPlugin.aliasAttributes(x);

        x.alias("roider_swRetMan", Roider_ShipworksRetrofitManager.class);
        Roider_ShipworksRetrofitManager.aliasAttributes(x);

        x.alias("roider_swRetPlug", Roider_ShipworksRetrofitPlugin.class);
        Roider_ShipworksRetrofitPlugin.aliasAttributes(x);

        x.alias("roider_argRetMan", Roider_ArgosRetrofitManager.class);
        Roider_ArgosRetrofitManager.aliasAttributes(x);

        x.alias("roider_argRetPlug", Roider_ArgosRetrofitPlugin.class);
        Roider_ArgosRetrofitPlugin.aliasAttributes(x);


        x.alias("roider_fringeBM", Roider_FringeBlackSubmarketPlugin.class);
        Roider_FringeBlackSubmarketPlugin.aliasAttributes(x);

        x.alias("roider_fringeOM", Roider_FringeSubmarketPlugin.class);
        Roider_FringeSubmarketPlugin.aliasAttributes(x);

        x.alias("roider_resupplyM", Roider_ResupplySubmarketPlugin.class);
        Roider_ResupplySubmarketPlugin.aliasAttributes(x);

        x.alias("roider_hqM", Roider_UnionHQSubmarketPlugin.class);
        Roider_UnionHQSubmarketPlugin.aliasAttributes(x);


        x.alias("roider_fringeClnr", Roider_FringeStationCleaner.class);

        x.alias("roider_indRepMatch", Roider_IndieRepMatcher.class); // Unused

        x.alias("roider_expTrap", Roider_ExpeditionTrap.class);
        Roider_ExpeditionTrap.aliasAttributes(x);

        x.alias("roider_expTrapCr", Roider_ExpeditionTrapCreator.class);
        Roider_ExpeditionTrapCreator.aliasAttributes(x);

        x.alias("roider_expFleet", Roider_TechExpeditionFleetRouteManager.class);
        Roider_TechExpeditionFleetRouteManager.aliasAttributes(x);

        x.alias("roider_expFleetData", Roider_TechExpeditionFleetRouteManager.CustomData.class);
        Roider_TechExpeditionFleetRouteManager.CustomData.aliasAttributes(x);

        x.alias("roider_expFleetAI", Roider_TechExpeditionFleetAssignmentAI.class);
        Roider_TechExpeditionFleetAssignmentAI.aliasAttributes(x);

        x.alias("roider_expPickup", Roider_ExpeditionStashPickupScript.class);
        Roider_ExpeditionStashPickupScript.aliasAttributes(x);

        x.alias("roider_expIntel", Roider_TechExpeditionIntel.class);
        Roider_TechExpeditionIntel.aliasAttributes(x);

        x.alias("roider_expMajorLoot", Roider_MajorLootStashPlugin.class);

        x.alias("roider_expLootClnr", Roider_ExpeditionLootCleaner.class);
        Roider_ExpeditionLootCleaner.aliasAttributes(x);

        x.alias("roider_expPingTrap", Roider_PingTrapScript.class);
        Roider_PingTrapScript.aliasAttributes(x);


        x.alias("roider_sysMusic", Roider_SystemMusicScript.class);


        x.alias("roider_aAbil", Roider_ArgosConversionAbility.class);
        Roider_ArgosConversionAbility.aliasAttributes(x);

        x.alias("roider_aAbilAdd", Roider_ArgosAbilityAdderScript.class);
        Roider_ArgosAbilityAdderScript.aliasAttributes(x);

        x.alias("roider_cfrMan", Roider_ConversionFleetRouteManager.class);
        Roider_ConversionFleetRouteManager.aliasAttributes(x);

        x.alias("roider_cfrIntel", Roider_ConversionFleetIntel.class);
        Roider_ConversionFleetIntel.aliasAttributes(x);

        x.alias("roider_armBPAdder", Roider_ArmatureSingleBPAdderScript.class);
        Roider_ArmatureSingleBPAdderScript.aliasAttributes(x);

        x.alias("roider_madHealer", Roider_MadMIDASHealer.class);
        Roider_MadMIDASHealer.aliasAttributes(x);
    }
}