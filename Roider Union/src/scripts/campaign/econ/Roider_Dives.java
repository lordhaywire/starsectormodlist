package scripts.campaign.econ;

import java.awt.Color;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Conditions;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_Ids.Roider_Ranks;
import ids.Roider_Ids.Roider_Submarkets;
import ids.Roider_Ids.Roider_Tags;
import ids.Roider_MemFlags;
import scripts.campaign.fleets.Roider_HQPatrolManager;
import java.util.*;
import org.lwjgl.util.vector.Vector2f;
import scripts.campaign.fleets.Roider_MinerRouteManager;


public class Roider_Dives extends BaseIndustry implements MarketImmigrationModifier {

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_Dives.class, "minerManager", "man");
        x.aliasAttribute(Roider_Dives.class, "patrolManager", "p");
        x.aliasAttribute(Roider_Dives.class, "baseCommander", "com");
        x.aliasAttribute(Roider_Dives.class, "oreSupply", "o");
        x.aliasAttribute(Roider_Dives.class, "rareSupply", "r");
        x.aliasAttribute(Roider_Dives.class, "volatilesSupply", "v");
        x.aliasAttribute(Roider_Dives.class, "organicsSupply", "g");
        x.aliasAttribute(Roider_Dives.class, "addedDirectly", "aD");
    }

    public static final float DIVES_RANGE = 1f; // light years
    public static final float HQ_RANGE = 6; // light years

	public static float DEFENSE_BONUS = 0.1f;

    private Roider_MinerRouteManager minerManager;
    private Roider_HQPatrolManager patrolManager;

    private PersonAPI baseCommander;

    private int oreSupply = 0;
    private int rareSupply = 0;
    private int volatilesSupply = 0;
    private int organicsSupply = 0;

    private boolean addedDirectly = true;

    @Override
    public void init(String id, MarketAPI market) {
        super.init(id, market);

        minerManager = new Roider_MinerRouteManager(market, isUnionHQ());
        patrolManager = new Roider_HQPatrolManager(market);

        baseCommander = (PersonAPI) market.getMemoryWithoutUpdate()
                    .get("$" + Roider_Ranks.POST_BASE_COMMANDER);
        if (baseCommander == null) {
            baseCommander = Global.getSector().getFaction(Roider_Factions.ROIDER_UNION).createRandomPerson();
            market.getMemoryWithoutUpdate().set("$" + Roider_Ranks.POST_BASE_COMMANDER, baseCommander);

            ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
            ip.addPerson(baseCommander);
            ip.checkOutPerson(baseCommander, "permanent_staff");
        }
        baseCommander.setRankId(Ranks.GROUND_GENERAL);
        baseCommander.setPostId(Roider_Ranks.POST_BASE_COMMANDER);
    }

    private boolean isUnionHQ() {
        return getSpec().hasTag(Roider_Tags.UNION_HQ);
    }

	@Override
	public void advance(float amount) {
		super.advance(amount);

        if (addedDirectly && !isBuilding() && !isUpgrading()) {
            buildingFinished();
            addedDirectly = false;
        }

        if (!market.isInEconomy()) return;
		if (Global.getSector().getEconomy().isSimMode()) return;

        boolean functional = isFunctional();

        // No resources equals no roider miners
        if (canMine()) {
            Map<String, Integer> supplyLevels = new HashMap<>();
            supplyLevels.put(Commodities.ORE, oreSupply);
            supplyLevels.put(Commodities.RARE_ORE, rareSupply);
            supplyLevels.put(Commodities.VOLATILES, volatilesSupply);
            supplyLevels.put(Commodities.ORGANICS, organicsSupply);

            minerManager.advance(amount, supplyLevels, functional, isUnionHQ());
        }

        // Only Union HQs spawn patrols
        if (isUnionHQ()) {
            boolean constructing = isBuilding() || isUpgrading();
            functional &= !constructing;
            market.getMemoryWithoutUpdate().set(Roider_MemFlags.UNION_HQ_FUNCTIONAL, functional);

            patrolManager.advance(amount, functional);
        }
    }

    public PersonAPI getBaseCommander() {
        return baseCommander;
    }

    //<editor-fold defaultstate="collapsed" desc="Other Methods">
    @Override
    public boolean isFunctional() {
        boolean functional = super.isFunctional();
        if (isUnionHQ()) {
            // Union HQ does not work on markets hostile to Roider Union
            functional &= !market.getFaction().isHostileTo(Roider_Factions.ROIDER_UNION);
        } else {
            // Dives don't work if there is no mining
            functional &= canMine();
        }

        return functional;
    }

    public boolean canMine() {
        return oreSupply + rareSupply + volatilesSupply + organicsSupply > 0;
    }

    public String getResources() {
        List<String> resources = new ArrayList<>();
        if (oreSupply > 0) resources.add("ore");
        if (rareSupply > 0) resources.add("transplutonic ore");
        if (organicsSupply > 0) resources.add("organics");
        if (volatilesSupply > 0) resources.add("volatiles");

        return Misc.getAndJoined(resources);
    }

	protected boolean canImproveToIncreaseProduction() {
		return true;
	}

    @Override
    public String getCurrentImage() {
        if (isUnionHQ() && marketHasHeavyIndustry()) {
            return Global.getSettings().getSpriteName("industry", "roider_hq");
        }

        return super.getCurrentImage();
    }

    public boolean marketHasHeavyIndustry() {
        for (Industry ind : market.getIndustries()) {
            if (ind.getSpec().hasTag("heavyindustry")) return true;
        }

        if (market.hasIndustry(Industries.HEAVYINDUSTRY)) return true;
        if (market.hasIndustry(Industries.ORBITALWORKS)) return true;
        if (market.hasIndustry(Roider_Industries.SHIPWORKS)) return true;
        if (market.hasIndustry(Roider_Industries.MS_MASS_IND)) return true;
        if (market.hasIndustry(Roider_Industries.MS_MIL_PROD)) return true;
        if (market.hasIndustry(Roider_Industries.MS_MODULAR_FAC)) return true;
        if (market.hasIndustry(Roider_Industries.MS_SHIPYARD)) return true;
        if (market.hasIndustry(Roider_Industries.XLU_BATTLE_YARDS)) return true;


        return false;
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
		float opad = 10f;

        // Dives mining
        if (!isUnionHQ() && mode == IndustryTooltipMode.NORMAL) {
            if (canMine()) {
                tooltip.addPara("Roiders from across the system "
                            + "sell their hauls here, providing "
                            + "a small supply of " + getResources()
                            + ".", opad);
            } else {
                tooltip.addPara("Without any mining prospects in "
                            + "the system, few roiders live around "
                            + market.getName() + ".",
                            Misc.getNegativeHighlightColor(), opad);
                return;
            }
		}

        // End of Dives tooltip
        if (!isUnionHQ()) return;

        // If non-functional due to hostility with Roider Union
        if (market.getFaction().isHostileTo(Roider_Factions.ROIDER_UNION)) {
			tooltip.addPara("There are no Roider Union officials or "
                        + "services here due to hostilities with "
                        + market.getFaction().getDisplayNameWithArticle()
                        + ". Independent roiders, fearing discrimination, "
                        + "avoid " + market.getName() + " as well.",
                        Misc.getNegativeHighlightColor(), opad);
            return;
        }

        // Mining!
        if (canMine() && mode == IndustryTooltipMode.NORMAL) {
            tooltip.addPara("Wandering roiders come for lightyears "
                        + "to " + market.getName() + " to sell "
                        + "their hauls, buy equipment, and have "
                        + "a good time, providing a small supply "
                        + "of " + getResources() + ".", opad);
        }
        // Or not
        if (!canMine() && mode == IndustryTooltipMode.NORMAL) {
            tooltip.addPara("Without any mining prospects for lightyears, "
                        + "there are few roiders here.",
                        Misc.getNegativeHighlightColor(), opad);
        }


        boolean someMilitary = market.hasIndustry(Industries.PATROLHQ)
                    && !market.hasIndustry(Roider_Industries.THI_MERCS);
        boolean fullMilitary = market.hasIndustry(Industries.MILITARYBASE)
                    || market.hasIndustry(Industries.HIGHCOMMAND)
                    || market.hasIndustry(Roider_Industries.THI_MERCS);

        String supplement = "serve as";
        if (someMilitary || fullMilitary) supplement = "supplement";

        String heavyInd = "";
        if (!marketHasHeavyIndustry()) {
            heavyInd = " Only a limited selection of conversions "
                        + "are available without Heavy Industry.";
        }

        // Patrols, trade, and ship conversions
        if (mode == IndustryTooltipMode.NORMAL) {
            tooltip.addPara("The Roider base commander organizes "
                        + "local roiders into patrols to " + supplement
                        + " the military of " + market.getName() + ". "
                        + "Good relations with the Roider Union gives "
                        + "access to trade opportunities and the "
                        + "Roiders' famed ship conversion services."
                        + heavyInd, opad);
        }
    }

    @Override
    public void apply() {
        int extra = 0;
        if (isUnionHQ()) extra = 1;

        int size = market.getSize();

        int drugs = size;
        if (market.hasCondition(Roider_Conditions.PARASITE_SPORES)) drugs = 0;
        if (market.hasCondition(Roider_Conditions.PSYCHOACTIVE_FUNGUS)) drugs = 0;

        demand(Commodities.HEAVY_MACHINERY, size - 3);
        demand(Commodities.DRUGS, drugs);
        demand(Commodities.SUPPLIES, size - 1 + extra);
        demand(Commodities.FUEL, size - 1 + extra);
        demand(Commodities.SHIPS, size - 1 + extra);

        supplyMiningResources();
        supply(Commodities.CREW, size);

        supply(Commodities.MARINES, size - 3 + extra);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.HEAVY_MACHINERY);
        applyDeficitToProduction(0, deficit,
                    Commodities.ORE,
                    Commodities.RARE_ORE,
                    Commodities.ORGANICS,
                    Commodities.VOLATILES);

		modifyStabilityWithBaseMod();

        // Ground defenses
        if (isUnionHQ()) {
            float mult = getDeficitMult(Commodities.SUPPLIES);
            String postfix = "";
            if (mult != 1) {
                String com = getMaxDeficit(Commodities.SUPPLIES).one;
                postfix = " (" + getDeficitText(com).toLowerCase() + ")";
            }

            market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
                        .modifyMult(getModId(), 1f + DEFENSE_BONUS * mult,
                        getNameForModifier() + postfix);
        }

        super.apply(true);

        if (!isFunctional()) {
            supply.clear();
            unapply();
        }
    }

    private void supplyMiningResources() {
        oreSupply = 0;
        rareSupply = 0;
        volatilesSupply = 0;
        organicsSupply = 0;

        MarketAPI oreSource = market;
        MarketAPI rareSource = market;
        MarketAPI volatilesSource = market;
        MarketAPI organicsSource = market;

        float harvestRange = DIVES_RANGE;
        if (isUnionHQ()) harvestRange = HQ_RANGE;

        if (market.getPrimaryEntity() == null) return;
        if (market.getStarSystem() == null) return;

        for (MarketAPI target : getHarvestTargetsInRange(market, harvestRange)) {
            int orePotential = 0;
            int rarePotential = 0;
            int volatilesPotential = 0;
            int organicsPotential = 0;

            for (MarketConditionAPI c : target.getConditions()) {
                switch (c.getId()) {
                    case (Conditions.ORE_ULTRARICH): orePotential++;
                    case (Conditions.ORE_RICH): orePotential++;
                    case (Conditions.ORE_ABUNDANT): orePotential++;
                    case (Conditions.ORE_MODERATE): orePotential++;
                    case (Conditions.ORE_SPARSE): break;

                    case (Conditions.ORGANICS_PLENTIFUL): organicsPotential++;
                    case (Conditions.ORGANICS_ABUNDANT): organicsPotential++;
                    case (Conditions.ORGANICS_COMMON): organicsPotential++;
                    case (Conditions.ORGANICS_TRACE): break;

                    // Reduced supply for rare ores and volatiles
                    case (Conditions.RARE_ORE_ULTRARICH): rarePotential++;
                    case (Conditions.RARE_ORE_RICH): rarePotential++;
                    case (Conditions.RARE_ORE_ABUNDANT): break;
                    case (Conditions.RARE_ORE_MODERATE): break;
                    case (Conditions.RARE_ORE_SPARSE): break;

                    case (Conditions.VOLATILES_PLENTIFUL): volatilesPotential++;
                    case (Conditions.VOLATILES_ABUNDANT): break;
                    case (Conditions.VOLATILES_DIFFUSE): break;
                    case (Conditions.VOLATILES_TRACE): break;
                }
            }


            if (orePotential > oreSupply) {
                oreSupply = orePotential;
                oreSource = target;
            }

            if (rarePotential > rareSupply) {
                rareSupply = rarePotential;
                rareSource = target;
            }

            if (volatilesPotential > volatilesSupply) {
                volatilesSupply = volatilesPotential;
                volatilesSource = target;
            }

            if (organicsPotential > organicsSupply) {
                organicsSupply = organicsPotential;
                organicsSource = target;
            }
        }

        String oreBaseDesc = getMiningBaseDesc(oreSource, oreSupply);
        String rareBaseDesc = getMiningBaseDesc(rareSource, rareSupply);
        String volatilesBaseDesc = getMiningBaseDesc(volatilesSource, volatilesSupply);
        String organicsBaseDesc = getMiningBaseDesc(organicsSource, organicsSupply);

        String oreDesc = getMiningBonusDesc(Commodities.ORE, oreSource, oreSupply);
        String rareDesc = getMiningBonusDesc(Commodities.RARE_ORE, rareSource, rareSupply);
        String volatilesDesc = getMiningBonusDesc(Commodities.VOLATILES, volatilesSource, volatilesSupply);
        String organicsDesc = getMiningBonusDesc(Commodities.ORGANICS, organicsSource, organicsSupply);

        boolean US_parasites = market.hasCondition(Roider_Conditions.PARASITE_SPORES);
        String US_parasites_desc = "Parasitic spores";

        if (oreSupply >= 1) {
            supply(Roider_Industries.DIVES + "_" + Commodities.ORE,
                        Commodities.ORE, 1, oreBaseDesc);
            if (oreSupply > 1) supply(Roider_Industries.DIVES + "_"
                        + Commodities.ORE + "_mod", Commodities.ORE, oreSupply - 1, oreDesc);
            if (US_parasites) supply(Roider_Industries.DIVES + "_"
                        + Commodities.ORE + "_" + Roider_Conditions.PARASITE_SPORES,
                        Commodities.ORE, -1, US_parasites_desc);
        }

        if (rareSupply >= 1) {
            supply(Roider_Industries.DIVES + "_" + Commodities.RARE_ORE, Commodities.RARE_ORE, -1, rareBaseDesc);
            supply(Roider_Industries.DIVES + "_" + Commodities.RARE_ORE + "_mod", Commodities.RARE_ORE, rareSupply + 1, rareDesc);
            if (US_parasites) supply(Roider_Industries.DIVES + "_"
                        + Commodities.RARE_ORE + "_" + Roider_Conditions.PARASITE_SPORES,
                        Commodities.RARE_ORE, -1, US_parasites_desc);
        }

        if (volatilesSupply >= 1) {
            supply(Roider_Industries.DIVES + "_" + Commodities.VOLATILES, Commodities.VOLATILES, -1, volatilesBaseDesc);
            supply(Roider_Industries.DIVES + "_" + Commodities.VOLATILES + "_mod", Commodities.VOLATILES, volatilesSupply + 1, volatilesDesc);
            if (US_parasites) supply(Roider_Industries.DIVES + "_"
                        + Commodities.VOLATILES + "_" + Roider_Conditions.PARASITE_SPORES,
                        Commodities.VOLATILES, -1, US_parasites_desc);
        }

        if (organicsSupply >= 1) {
            supply(Roider_Industries.DIVES + "_" + Commodities.ORGANICS, Commodities.ORGANICS, 1, organicsBaseDesc);
            if (organicsSupply > 1) supply(Roider_Industries.DIVES + "_" + Commodities.ORGANICS + "_mod", Commodities.ORGANICS, organicsSupply - 1, organicsDesc);
            if (US_parasites) supply(Roider_Industries.DIVES + "_"
                        + Commodities.ORGANICS + "_" + Roider_Conditions.PARASITE_SPORES,
                        Commodities.ORGANICS, -1, US_parasites_desc);
        }
    }

    public static List<MarketAPI> getHarvestTargetsInRange(MarketAPI market, float ly) {
        Vector2f loc = market.getLocationInHyperspace();

        if (loc == null) return new ArrayList<>();

        // What systems are in range?
        List<PlanetAPI> pTargets = new ArrayList<>();
        pTargets.addAll(market.getStarSystem().getPlanets());
        for (StarSystemAPI s : Global.getSector().getStarSystems()) {
            if (s.getHyperspaceAnchor() == null) continue;
            if (s.getHyperspaceAnchor().getLocationInHyperspace() == null) continue;
            if (s.getId().equals(market.getStarSystem().getId())) continue;
            if (s.hasTag(Tags.THEME_UNSAFE)) continue;
            if (s.hasTag(Tags.THEME_HIDDEN)) continue;
            if (s.getType() != StarSystemType.NEBULA
                        && s.getStar() == null) continue;

            float a = loc.getX() - s.getHyperspaceAnchor().getLocationInHyperspace().getX();
            float b = loc.getY() - s.getHyperspaceAnchor().getLocationInHyperspace().getY();
            float c = (a * a) + (b * b);

            float lydist = Global.getSettings().getUnitsPerLightYear();
            if (c <= lydist * lydist * ly * ly) pTargets.addAll(s.getPlanets());
//            pTargets.addAll(s.getPlanets());
        }

        // Grab unclaimed planets from those systems.
        // And own market and planets owned by same faction
        List<MarketAPI> targets = new ArrayList<>();
        targets.add(market);
        for (PlanetAPI pTarget : pTargets) {
            if (pTarget.isStar()) continue;
            if (pTarget.getMemoryWithoutUpdate().getBoolean(Roider_MemFlags.CLAIMED)) continue;

            MarketAPI target = pTarget.getMarket();
            if (target == null) continue;
            if (target.equals(market)) continue;
            if (target.getFaction() == null) continue;
            if ((target.getFaction().isNeutralFaction() && target.isPlanetConditionMarketOnly())
                        || target.getFactionId().equals(market.getFactionId())) {
                targets.add(target);
            }
        }

        return targets;
    }

    private String getMiningBaseDesc(MarketAPI source, int supply) {
        String desc = "Base value";

//        if (supply != 1) return desc;

        if (source.getId().equals(market.getId())) {}
        else if (source.getLocationInHyperspace() == market.getLocationInHyperspace()) desc += " from a nearby planet";
        else desc += " from a nearby system";
//        else if (source.getLocationInHyperspace() == market.getLocationInHyperspace()) desc += " on " + source.getName();
//        else desc += " on " + source.getName() + " in the " + source.getStarSystem().getName();

        return desc;
    }

    private String getMiningBonusDesc(String commodity, MarketAPI source, int supply) {
        if (supply < 1) return "";

        String desc = "";

        switch (commodity) {
            case (Commodities.ORE):
                if (supply == 4) desc = "Ultrarich ore deposits";
                if (supply == 3) desc = "Rich ore deposits";
                if (supply == 2) desc = "Abundant ore deposits";
                if (supply == 1) desc = "Moderate ore deposits";
                break;
            case (Commodities.RARE_ORE):
                if (supply == 2) desc = "Ultrarich rare ore deposits";
                if (supply == 1) desc = "Rich rare ore deposits";
//                if (supply == 2) desc = "Abundant rare ore deposits";
                break;
            case (Commodities.VOLATILES):
                if (supply == 1) desc = "Plentiful volatiles";
//                if (supply == 1) desc = "Abundant volatiles";
                break;
            case (Commodities.ORGANICS):
                if (supply == 3) desc = "Plentiful organics";
                if (supply == 2) desc = "Abundant organics";
                if (supply == 1) desc = "Common organics";
                break;
        }

//        if (source.equals(market)) {}
//        else if (source.getLocationInHyperspace() == market.getLocationInHyperspace()) desc += " on a nearby planet";
//        else desc += " in a nearby system";
//        else if (source.getLocationInHyperspace() == market.getLocationInHyperspace()) desc += " on " + source.getName();
//        else desc += " on " + source.getName() + " in the " + source.getStarSystem().getName();

        return desc;
    }


    @Override
    public void unapply() {
        super.unapply();

        supply.clear();

        unmodifyStabilityWithBaseMod();

        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
                    .unmodify(getModId());
    }

    @Override
	public void downgrade() {
        super.downgrade();

        // Remove submarket
        market.removeSubmarket(Roider_Submarkets.UNION_MARKET);

        // Stop patrols
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.UNION_HQ_FUNCTIONAL, false);
        patrolManager.getTracker().forceIntervalElapsed();

        // Not a mil market anymore
        Misc.setFlagWithReason(market.getMemoryWithoutUpdate(),
                    MemFlags.MARKET_MILITARY,
                    "ind_" + Roider_Industries.UNION_HQ, false, -1);

        // Remove Base commander
        market.getCommDirectory().removePerson(baseCommander);
        market.removePerson(baseCommander);

        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        ip.getData(baseCommander).getLocation().setMarket(null);
	}

	@Override
	protected void buildingFinished() {
		super.buildingFinished();

        addedDirectly = false;

        // Add mining fleets
        minerManager.forceIntervalElapsed();
//        minerManager = new Roider_MinerManager(market);


        if (isUnionHQ()) {
            // Make military market
            Misc.setFlagWithReason(market.getMemoryWithoutUpdate(),
                        MemFlags.MARKET_MILITARY,
                        "ind_" + Roider_Industries.UNION_HQ, true, -1);

            // Add submarket
            market.addSubmarket(Roider_Submarkets.UNION_MARKET);

            // Add base commander
            market.getCommDirectory().addPerson(baseCommander);
            market.addPerson(baseCommander);

            ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
            ip.getData(baseCommander).getLocation().setMarket(market);

            // Add patrol fleets
            market.getMemoryWithoutUpdate().set(Roider_MemFlags.UNION_HQ_FUNCTIONAL, isFunctional());
            patrolManager.getTracker().forceIntervalElapsed();
        }
	}

	@Override
	protected void upgradeFinished(Industry previous) {
		super.upgradeFinished(previous);

        addedDirectly = false;

        // Make military market
        Misc.setFlagWithReason(market.getMemoryWithoutUpdate(),
                    MemFlags.MARKET_MILITARY,
                    "ind_" + Roider_Industries.UNION_HQ, true, -1);

        // Add submarket
        market.addSubmarket(Roider_Submarkets.UNION_MARKET);

        // Add base commander
        market.getCommDirectory().addPerson(baseCommander);
        market.addPerson(baseCommander);

        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        ip.getData(baseCommander).getLocation().setMarket(market);

        // Add patrol fleets
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.UNION_HQ_FUNCTIONAL, isFunctional());
		patrolManager.getTracker().forceIntervalElapsed();
	}

    @Override
    public void startUpgrading() {
        super.startUpgrading();
    }

    @Override
    public void startBuilding() {
        super.startBuilding();
    }

    @Override
    public void cancelUpgrade() {
        super.cancelUpgrade();

        // Nothing should be set yet for Union HQ
    }

	protected void notifyDisrupted() {

        // Stop fleet spawning


        // Disable submarket
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.UNION_HQ_FUNCTIONAL, false);

        // Remove base commander
        market.getCommDirectory().removePerson(baseCommander);
        market.removePerson(baseCommander);

        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        ip.getData(baseCommander).getLocation().setMarket(null);

	}

    @Override
    protected void disruptionFinished() {
        super.disruptionFinished();

        // Reallow fleets
        minerManager.forceIntervalElapsed();
        patrolManager.getTracker().forceIntervalElapsed();

        // Reenable submarket, if Union HQ
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.UNION_HQ_FUNCTIONAL, isFunctional());

        // Replace person
        market.getCommDirectory().addPerson(baseCommander);
        market.addPerson(baseCommander);

        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        ip.getData(baseCommander).getLocation().setMarket(market);

    }

    @Override
	public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
        super.notifyBeingRemoved(mode, forUpgrade);

        if (forUpgrade) return; // Do nothing


        // Unset mil market
        Misc.setFlagWithReason(market.getMemoryWithoutUpdate(),
                    MemFlags.MARKET_MILITARY,
                    "ind_" + Roider_Industries.UNION_HQ, false, -1);

        market.getMemoryWithoutUpdate().set(Roider_MemFlags.UNION_HQ_FUNCTIONAL, false);
        // Remove submarket
        market.removeSubmarket(Roider_Submarkets.UNION_MARKET);

        // Remove base commander
        market.getCommDirectory().removePerson(baseCommander);
        market.removePerson(baseCommander);

        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        ip.getData(baseCommander).getLocation().setMarket(null);

        // Force stop fleet spawning
        minerManager = null;
        patrolManager = null;
	}

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        Pair<String, Integer> deficit = getMaxDeficit(Commodities.DRUGS);
        if (deficit.two <= 0 && !isUnionHQ()) return false;
        //return mode == IndustryTooltipMode.NORMAL && isFunctional();
        return mode != IndustryTooltipMode.NORMAL || isFunctional();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        //if (mode == IndustryTooltipMode.NORMAL && isFunctional()) {
        if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
            Color h = Misc.getHighlightColor();
            float opad = 10f;
            float pad = 3f;

            Pair<String, Integer> deficit = getMaxDeficit(Commodities.DRUGS);
            if (deficit.two > 0) {
                tooltip.addPara(getDeficitText(Commodities.DRUGS) + ": %s units. Reduced colony growth.", pad, h, "" + deficit.two);
            }

            if (isUnionHQ()) addStabilityPostDemandSection(tooltip, hasDemand, mode);
        }
    }

    @Override
    protected int getBaseStabilityMod() {
        if (isUnionHQ()) return 1;

        return 0;
    }

    @Override
    public String getNameForModifier() {
        if (getSpec().getName().contains("HQ")) {
            return getSpec().getName();
        }
        return Misc.ucFirst(getSpec().getName());
    }

    @Override
    public boolean isAvailableToBuild() {
        if (isUnionHQ()) return market.getFaction().knowsIndustry(Roider_Industries.UNION_HQ);

        return market.hasSpaceport();
    }

    @Override
    public boolean showWhenUnavailable() {
        return true;
    }

    @Override
    public boolean canUpgrade() {
//        if (market.getFaction().isHostileTo(Roider_Factions.ROIDER_UNION)) return false;

        return market.getFaction().knowsIndustry(Roider_Industries.UNION_HQ);
    }

    @Override
    public String getUnavailableReason() {
        if (!isUnionHQ()) return "Requires a functional spaceport";
        if (isUnionHQ()) {
//            if (market.getFaction().isHostileTo(Roider_Factions.ROIDER_UNION)) {
//                return "You are hostile with the Roider Union";
//            }
            if (!market.getFaction().knowsIndustry(Roider_Industries.UNION_HQ)) {
                return "Purchase the blueprint for this industry at an existing Union HQ";
            }
        }
        return super.getUnavailableReason();
    }

    @Override
	public float getPatherInterest() {
		return 1f + super.getPatherInterest();
	}
    //</editor-fold>

    // MarketImmigrationModifier Method
    @Override
	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.DRUGS);
		if (deficit.two > 0) {
			incoming.getWeight().modifyFlat(getModId(), -deficit.two, "Roiders: drug shortage");
		}
	}
}





