package data.scripts.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.CabalPickContributionMethod;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.UnderworldModPlugin;
import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;

public class UW_CabalFleetManager extends DisposableFleetManager {

    public static float CABAL_FLEET_FACTOR = 1f;

    private static final float MAX_LY_FROM_CABAL = 20f; // This effectively becomes 10 LY because of the rounding
    private static final String SETTINGS_FILE = "UNDERWORLD_OPTIONS.ini";

    public static void reloadSettings() throws IOException, JSONException {
        JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);

        CABAL_FLEET_FACTOR = (float) settings.getDouble("cabalFleetFactor");
    }

    @Override
    protected Object readResolve() {
        super.readResolve();
        return this;
    }

    @Override
    protected String getSpawnId() {
        return "cabal_spawn";
    }

    @Override
    protected int getMaxFleets() {
        if (!UnderworldModPlugin.Module_StarlightCabal) {
            return 0;
        }

        /* Don't allow going past this cap, to avoid build-up of superfleets */
        return getDesiredNumFleetsForSpawnLocation() + 1;
    }

    @Override
    protected int getDesiredNumFleetsForSpawnLocation() {
        if (!UnderworldModPlugin.Module_StarlightCabal) {
            return 0;
        }

        MarketAPI largestMarketNearby = getLargestMarket();
        MarketAPI closestMarket = getClosestCabal();

        float desiredNumFleets;
        if (closestMarket == null) {
            desiredNumFleets = 0f;
        } else {
            float distScale = 1f - Math.min(1f, Misc.getDistanceToPlayerLY(closestMarket.getLocationInHyperspace()) / MAX_LY_FROM_CABAL);
            desiredNumFleets = 1f * distScale;
        }

        if (largestMarketNearby != null) {
            desiredNumFleets += Math.max(0f, (largestMarketNearby.getSize() - 3f) * 0.5f);
        }

        int level = getCabalLevel();
        desiredNumFleets += level * 0.5f;

        return (int) Math.round(desiredNumFleets * CABAL_FLEET_FACTOR);
    }

    protected int getCabalLevel() {
        if (currSpawnLoc == null) {
            return 0;
        }
        int total = 0;
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(currSpawnLoc)) {
            if (market.isHidden()) {
                continue;
            }
            if (market.hasCondition(Conditions.DECIVILIZED)) {
                continue;
            }
            if (market.hasCondition("cabal_influence")) {
                total++;
            }
            switch (market.getFactionId()) {
                case Factions.TRITACHYON:
                case "cabal":
                    total++;
                    break;
                default:
                    break;
            }
        }
        return total;
    }

    protected MarketAPI getClosestCabal() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (player == null) {
            return null;
        }

        MarketAPI closest = null;
        float minDistance = 100000f;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.isHidden()) {
                continue;
            }
            if (market.hasCondition(Conditions.DECIVILIZED)) {
                continue;
            }
            if (!market.hasCondition("cabal_influence")) {
                switch (market.getFactionId()) {
                    case Factions.TRITACHYON:
                    case "cabal":
                        break;
                    default:
                        continue;
                }
            }
            float distance = Misc.getDistanceToPlayerLY(market.getLocationInHyperspace());
            if (distance < minDistance) {
                closest = market;
                minDistance = distance;
            }
        }

        return closest;
    }

    protected MarketAPI getLargestMarket() {
        if (currSpawnLoc == null) {
            return null;
        }

        MarketAPI largest = null;
        int maxSize = 0;
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(currSpawnLoc)) {
            if (market.isHidden()) {
                continue;
            }
            if (market.hasCondition(Conditions.DECIVILIZED)) {
                continue;
            }
            if (!market.hasCondition("cabal_influence")) {
                switch (market.getFactionId()) {
                    case Factions.TRITACHYON:
                    case "cabal":
                        break;
                    default:
                        continue;
                }
            }

            if (market.getSize() > maxSize) {
                maxSize = market.getSize();
                largest = market;
            }
        }

        return largest;
    }

    @Override
    protected float getExpireDaysPerFleet() {
        /* Bigger fleets, slower wind-down */
        return 20f;
    }

    @Override
    protected CampaignFleetAPI spawnFleetImpl() {
        if (!UnderworldModPlugin.Module_StarlightCabal) {
            return null;
        }

        StarSystemAPI system = currSpawnLoc;
        if (system == null) {
            return null;
        }

        float combat = MathUtils.getRandomNumberInRange(20f, MathUtils.getRandomNumberInRange(40f, MathUtils.getRandomNumberInRange(80f, 160f)));

        float timeFactor = (PirateBaseManager.getInstance().getDaysSinceStart() - 180f) / (365f * 2f);
        if (timeFactor < 0f) {
            timeFactor = 0f;
        }
        if (timeFactor > 1f) {
            timeFactor = (float) Math.sqrt(timeFactor);
        }
        if (timeFactor > 2f) {
            timeFactor = 2f;
        }
        combat *= 1f + MathUtils.getRandomNumberInRange(0f, timeFactor);
        float levelFactor = 0f;
        if (Global.getSector().getPlayerPerson() != null) {
            levelFactor = Global.getSector().getPlayerPerson().getStats().getLevel() / 50f;
        }
        combat *= 1f + MathUtils.getRandomNumberInRange(0f, levelFactor);

        float freighter = 0f;
        float tanker = 0f;
        float utility = 0f;

        if (Math.random() < (combat / 200f)) {
            freighter = Math.round(combat * MathUtils.getRandomNumberInRange(0f, 0.1f));
        }
        if (Math.random() < (combat / 400f)) {
            tanker = Math.round(combat * MathUtils.getRandomNumberInRange(0f, 0.1f));
        }
        if (Math.random() < (combat / 600f)) {
            utility = Math.round((freighter + tanker) * MathUtils.getRandomNumberInRange(0f, 0.5f));
        }

        String fleetType;
        if (combat < 50) {
            fleetType = FleetTypes.PATROL_SMALL;
        } else if (combat < 100) {
            fleetType = FleetTypes.PATROL_MEDIUM;
        } else {
            fleetType = FleetTypes.PATROL_LARGE;
        }

        FleetParamsV3 params = new FleetParamsV3(
                null, // market
                null, // location
                "cabal", // fleet's faction, if different from above, which is also used for source market picking
                2f,
                fleetType,
                combat, // combatPts
                freighter, // freighterPts
                tanker, // tankerPts
                0f, // transportPts
                0f, // linerPts
                utility, // utilityPts
                0 // qualityBonus
        );

        FactionDoctrineAPI doctrine = Global.getSector().getFaction("cabal").getDoctrine().clone();

        CabalFleetType cabalFleetType;
        if (combat < 80 && Math.random() < 0.3) {
            cabalFleetType = CabalFleetType.FRIGATES;
            doctrine.setShipSize(1);
            doctrine.setWarships(4);
            doctrine.setCarriers(1);
            doctrine.setPhaseShips(2);
        } else if (combat >= 40 && combat < 120 && Math.random() < 0.3) {
            cabalFleetType = CabalFleetType.DESTROYERS;
            doctrine.setShipSize(2);
            doctrine.setWarships(3);
            doctrine.setCarriers(1);
            doctrine.setPhaseShips(3);
        } else if (combat >= 80 && Math.random() < 0.3) {
            cabalFleetType = CabalFleetType.CRUISERS;
            doctrine.setShipSize(4);
            doctrine.setWarships(3);
            doctrine.setCarriers(1);
            doctrine.setPhaseShips(3);
        } else if (combat >= 120 && Math.random() < 0.3) {
            cabalFleetType = CabalFleetType.CAPITALS;
            doctrine.setShipSize(5);
            doctrine.setWarships(4);
            doctrine.setCarriers(1);
            doctrine.setPhaseShips(2);
        } else if (combat >= 60 && Math.random() < 0.4) {
            cabalFleetType = CabalFleetType.CARRIERS;
            doctrine.setShipSize(4);
            doctrine.setWarships(1);
            doctrine.setCarriers(5);
            doctrine.setPhaseShips(1);
        } else if (combat >= 60 && combat <= 140 && Math.random() < 0.4) {
            cabalFleetType = CabalFleetType.PHASE;
            doctrine.setShipSize(3);
            doctrine.setWarships(1);
            doctrine.setCarriers(1);
            doctrine.setPhaseShips(5);
        } else {
            cabalFleetType = CabalFleetType.BALANCED;
            doctrine.setShipSize(3);
            doctrine.setWarships(3);
            doctrine.setCarriers(2);
            doctrine.setPhaseShips(2);
        }

        params.doctrineOverride = doctrine;
        params.ignoreMarketFleetSizeMult = true;
        params.forceAllowPhaseShipsEtc = true;
        params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
        params.averageSMods = 2;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        if ((fleet == null) || fleet.isEmpty()) {
            return null;
        }

        if (combat < 50) {
            fleet.getCommander().setRankId(Ranks.SPACE_COMMANDER);
        } else if (combat < 100) {
            fleet.getCommander().setRankId(Ranks.SPACE_CAPTAIN);
        } else {
            fleet.getCommander().setRankId(Ranks.SPACE_ADMIRAL);
        }

        switch (cabalFleetType) {
            case FRIGATES:
                fleet.setName("Wolfpack");
                break;
            case DESTROYERS:
                fleet.setName("Posse");
                break;
            case CRUISERS:
                fleet.setName("Team");
                break;
            case CAPITALS:
                fleet.setName("Group");
                break;
            case PHASE:
                fleet.setName("Spooks");
                break;
            case CARRIERS:
                fleet.setName("Carrier Swarm");
                break;
            default:
            case BALANCED:
                if (combat < 50) {
                    fleet.setName("Prowlers");
                } else if (combat < 100) {
                    fleet.setName("Coterie");
                } else {
                    fleet.setName("Clan Fleet");
                }
                break;
        }

        fleet.getMemoryWithoutUpdate().set("$nex_noKeepSMods", true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, "cabalFleet");
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);

        fleet.addScript(new ScrewWithPlayer(fleet));

        int nf = getDesiredNumFleetsForSpawnLocation();

        /* Avoid hyperspace build-up */
        if (nf == 1) {
            setLocationAndOrders(fleet, 1f, 1f);
        } else {
            setLocationAndOrders(fleet, 0.5f, 0.5f);
        }

        return fleet;
    }

    public static class ScrewWithPlayer implements EveryFrameScript {

        private final CampaignFleetAPI fleet;
        private long signalExtortionPaid = 0L;
        private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.3f);

        ScrewWithPlayer(CampaignFleetAPI fleet) {
            this.fleet = fleet;
        }

        @Override
        public void advance(float amount) {
            long signalExtortionPaidGlobal = 0L;
            long timestamp = 0L;
            float timerGlobal = 0f;
            if (Global.getSector().getMemoryWithoutUpdate().contains("$uw_cabal_extortion_signal")) {
                signalExtortionPaidGlobal = Global.getSector().getMemoryWithoutUpdate().getLong("$uw_cabal_extortion_signal");
            }
            if (Global.getSector().getMemoryWithoutUpdate().contains("$uw_cabal_extortion_timer")) {
                timerGlobal = Global.getSector().getMemoryWithoutUpdate().getFloat("$uw_cabal_extortion_timer");
            }
            if (Global.getSector().getMemoryWithoutUpdate().contains("$uw_cabal_extortion_timestamp")) {
                timestamp = Global.getSector().getMemoryWithoutUpdate().getLong("$uw_cabal_extortion_timestamp");
            }

            float days = Global.getSector().getClock().convertToDays(amount);
            tracker.advance(days);
            if ((timerGlobal > 0f) && (timestamp != Global.getSector().getClock().getTimestamp())) {
                timerGlobal -= Math.max(0f, Math.min(365f, Global.getSector().getClock().getElapsedDaysSince(timestamp)));
                Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_timer", timerGlobal);
            }
            timestamp = Global.getSector().getClock().getTimestamp();
            Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_timestamp", timestamp);

            MemoryAPI mem = fleet.getMemoryWithoutUpdate();
            if (mem.getBoolean("$Cabal_extortionAskedFor")) {
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "cabalScrewWithPlayer", false, 0f);
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "cabalScrewWithPlayer", false, 0f);
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, "cabalScrewWithPlayer", false, 0f);
            }

            float chaseDuration;
            float tryAgainTimer;
            boolean allowGlobalPay;
            switch (Global.getSector().getFaction("cabal").getRelToPlayer().getLevel()) {
                default:
                case VENGEFUL:
                    tryAgainTimer = 20f;
                    chaseDuration = 8f;
                    allowGlobalPay = false;
                    break;
                case HOSTILE:
                    tryAgainTimer = 25f;
                    chaseDuration = 7f;
                    allowGlobalPay = false;
                    break;
                case INHOSPITABLE:
                    tryAgainTimer = 30f;
                    chaseDuration = 6f;
                    allowGlobalPay = true;
                    break;
                case SUSPICIOUS:
                    tryAgainTimer = 35f;
                    chaseDuration = 5f;
                    allowGlobalPay = true;
                    break;
                case NEUTRAL:
                    tryAgainTimer = 45f;
                    chaseDuration = 4f;
                    allowGlobalPay = true;
                    break;
                case FAVORABLE:
                    tryAgainTimer = 50f;
                    chaseDuration = 3f;
                    allowGlobalPay = true;
                    break;
                case WELCOMING:
                    tryAgainTimer = 55f;
                    chaseDuration = 2f;
                    allowGlobalPay = true;
                    break;
                case FRIENDLY:
                    tryAgainTimer = 60f;
                    chaseDuration = 1f;
                    allowGlobalPay = true;
                    break;
                case COOPERATIVE:
                    return;
            }

            if (signalExtortionPaid < signalExtortionPaidGlobal) {
                if (mem.getBoolean("$Cabal_extortionPaid")) {
                    signalExtortionPaidGlobal = Global.getSector().getClock().getTimestamp();
                    timerGlobal = Math.max(tryAgainTimer, 30f);
                    Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_signal", signalExtortionPaidGlobal);
                    Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_timer", timerGlobal);
                } else {
                    if (allowGlobalPay) {
                        mem.set("$Cabal_extortionPaid", true, tryAgainTimer);
                    }
                    signalExtortionPaid = signalExtortionPaidGlobal;
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "cabalScrewWithPlayer", false, 0f);
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "cabalScrewWithPlayer", false, 0f);
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, "cabalScrewWithPlayer", false, 0f);
                }
            }

            if (!fleet.getFaction().getId().contentEquals("cabal")) {
                Misc.clearFlag(mem, MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF);
            }

            if (tracker.intervalElapsed() && (timerGlobal <= 0f)) {
                doFactionChange();

                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                if (playerFleet == null) {
                    return;
                }
                if (playerFleet.getContainingLocation() != fleet.getContainingLocation()) {
                    return;
                }
                if (!CabalPickContributionMethod.playerHasAbilityToPayContribution(fleet)) {
                    return;
                }
                if ((fleet.getCurrentAssignment() != null) && (fleet.getCurrentAssignment().getAssignment() == FleetAssignment.GO_TO_LOCATION)) {
                    return;
                }
                if (!fleet.getFaction().getId().contentEquals("cabal")) {
                    return;
                }

                VisibilityLevel level = playerFleet.getVisibilityLevelTo(fleet);
                if (level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
                    float chance = CabalPickContributionMethod.playerNetWorth(fleet) / 1000000f;
                    if (Math.random() < chance) {
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "cabalScrewWithPlayer", true, chaseDuration);
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "cabalScrewWithPlayer", true, chaseDuration);
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, "cabalScrewWithPlayer", true, chaseDuration);
                        mem.set(MemFlags.FLEET_BUSY, false);
                        timerGlobal = tryAgainTimer;
                        Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_timer", timerGlobal);
                    } else {
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "cabalScrewWithPlayer", false, 0f);
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "cabalScrewWithPlayer", false, 0f);
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, "cabalScrewWithPlayer", false, 0f);
                        timerGlobal = 7f;
                        Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_timer", timerGlobal);
                    }
                }
            }
        }

        @Override
        public boolean isDone() {
            return !fleet.isAlive();
        }

        @Override
        public boolean runWhilePaused() {
            return false;
        }

        private void doFactionChange() {
            boolean canSeePlayer = false;
            MemoryAPI mem = fleet.getMemoryWithoutUpdate();

            if ((fleet.getBattle() != null) && !fleet.getBattle().isDone()) {
                return;
            }

            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (playerFleet != null) {
                if (CabalPickContributionMethod.playerHasAbilityToPayContribution(fleet)) {
                    if (playerFleet.getContainingLocation() == fleet.getContainingLocation()) {
                        VisibilityLevel level = playerFleet.getVisibilityLevelTo(fleet);
                        if ((level == VisibilityLevel.COMPOSITION_DETAILS) || (level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS)) {
                            canSeePlayer = true;
                        }
                    }
                }
            }

            float fleetStrength = fleet.getEffectiveStrength();

            if (fleetStrength < 1f) {
                return;
            }

            float cabalWeakerTotal = getWeakerTotalForFaction(Global.getSector().getFaction("cabal"));
            float cabalStrongerTotal = getStrongerTotalForFaction(Global.getSector().getFaction("cabal"));
            float cabalDecisionLevel = 0f;
            if (cabalWeakerTotal >= 1f) {
                cabalDecisionLevel += Math.min(1f, fleetStrength / cabalWeakerTotal);
            }
            cabalDecisionLevel -= (float) Math.sqrt(cabalStrongerTotal / fleetStrength);
            float worthLevel = CabalPickContributionMethod.playerNetWorth(fleet) / 1000000f;
            if (canSeePlayer) {
                cabalDecisionLevel += 1f * Math.min(1f, worthLevel);
            }
            if (mem.getBoolean(MemFlags.MEMORY_KEY_PURSUE_PLAYER)) {
                cabalDecisionLevel += 2f * Math.min(1f, worthLevel);
            }

            if (cabalDecisionLevel >= 0.25f) {
                if (!fleet.getFaction().getId().contentEquals("cabal")) {
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
                    fleet.setNoFactionInName(false);
                    fleet.setFaction("cabal", true);
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_NO_REP_IMPACT, "cabal_disguise", false, 0);
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_LOW_REP_IMPACT, "cabal_disguise", false, 0);
                    Misc.clearFlag(mem, MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF);
                }
            } else {
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "cabalScrewWithPlayer", false, 0f);
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "cabalScrewWithPlayer", false, 0f);
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, "cabalScrewWithPlayer", false, 0f);

                float tritachyonStrongerTotal = getStrongerTotalForFaction(Global.getSector().getFaction(Factions.TRITACHYON));
                float tritachyonDecisionLevel = 1f - (tritachyonStrongerTotal / fleetStrength);

                float independentStrongerTotal = getStrongerTotalForFaction(Global.getSector().getFaction(Factions.INDEPENDENT));
                float independentDecisionLevel = 0.5f - (independentStrongerTotal / fleetStrength);

                float pirateStrongerTotal = getStrongerTotalForFaction(Global.getSector().getFaction(Factions.PIRATES));
                float pirateDecisionLevel = 0f - (pirateStrongerTotal / fleetStrength);

                if ((pirateDecisionLevel > tritachyonDecisionLevel) && (pirateDecisionLevel > independentDecisionLevel)) {
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
                    fleet.setNoFactionInName(true);
                    fleet.setFaction(Factions.PIRATES, true);
                } else if (independentDecisionLevel > tritachyonDecisionLevel) {
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, false);
                    fleet.setNoFactionInName(false);
                    fleet.setFaction(Factions.INDEPENDENT, true);
                } else {
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, false);
                    fleet.setNoFactionInName(false);
                    fleet.setFaction(Factions.TRITACHYON, true);
                }
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_NO_REP_IMPACT, "cabal_disguise", true, 99999);
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_LOW_REP_IMPACT, "cabal_disguise", true, 99999);
            }
        }

        private float getWeakerTotalForFaction(FactionAPI faction) {
            List<CampaignFleetAPI> visible = Misc.getVisibleFleets(fleet, false);
            float weakerTotal = 0f;
            for (CampaignFleetAPI other : visible) {
                if ((fleet.getAI() != null) && faction.isHostileTo(other.getFaction())) {
                    EncounterOption option = fleet.getAI().pickEncounterOption(null, other, true);
                    float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
                    VisibilityLevel level = other.getVisibilityLevelTo(fleet);
                    boolean seesComp = level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS
                            || level == VisibilityLevel.COMPOSITION_DETAILS;
                    if ((dist < 800f) && seesComp) {
                        if ((option == EncounterOption.ENGAGE) || (option == EncounterOption.HOLD)) {
                            weakerTotal += other.getEffectiveStrength();
                        }
                    }
                }
            }

            return weakerTotal;
        }

        private float getStrongerTotalForFaction(FactionAPI faction) {
            List<CampaignFleetAPI> visible = Misc.getVisibleFleets(fleet, false);
            float strongerTotal = 0f;
            for (CampaignFleetAPI other : visible) {
                if ((fleet.getAI() != null) && faction.isHostileTo(other.getFaction())) {
                    EncounterOption option = fleet.getAI().pickEncounterOption(null, other, true);
                    float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
                    VisibilityLevel level = other.getVisibilityLevelTo(fleet);
                    boolean seesComp = level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS
                            || level == VisibilityLevel.COMPOSITION_DETAILS;
                    if ((dist < 800f) && seesComp) {
                        if ((option == EncounterOption.DISENGAGE) || (option == EncounterOption.HOLD_VS_STRONGER)) {
                            strongerTotal += other.getEffectiveStrength();
                        }
                    }
                }
            }

            return strongerTotal;
        }
    }

    private static enum CabalFleetType {

        FRIGATES, DESTROYERS, CRUISERS, CAPITALS, CARRIERS, PHASE, BALANCED
    }
}
