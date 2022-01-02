package scripts.campaign.bases;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames;
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames.MarkovNameResult;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_Ids.Roider_Submarkets;
import ids.Roider_Ids.Roider_Tags;
import scripts.campaign.econ.Roider_Dives;

public class Roider_RoiderBaseIntel extends BaseIntelPlugin implements EveryFrameScript, FleetEventListener,
																EconomyAPI.EconomyUpdateListener {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_RoiderBaseIntel.class, "system", "s");
        x.aliasAttribute(Roider_RoiderBaseIntel.class, "market", "m");
        x.aliasAttribute(Roider_RoiderBaseIntel.class, "entity", "e");
        x.aliasAttribute(Roider_RoiderBaseIntel.class, "duration", "d");
        x.aliasAttribute(Roider_RoiderBaseIntel.class, "tier", "t");
        x.aliasAttribute(Roider_RoiderBaseIntel.class, "matchedStationToTier", "mt");
        x.aliasAttribute(Roider_RoiderBaseIntel.class, "monthlyInterval", "i");
    }

	public static enum RoiderBaseTier {
		TIER_1_1MODULE,
		TIER_2_1MODULE,
		TIER_3_2MODULE,
		TIER_4_3MODULE,
		TIER_5_3MODULE,
	}

	public static Object DISCOVERED_PARAM = new Object();

	public static Logger log = Global.getLogger(Roider_RoiderBaseIntel.class);

	protected StarSystemAPI system;
	protected MarketAPI market;
	protected SectorEntityToken entity;

	protected float elapsedDays = 0f;
	protected float duration = 45f;

	protected RoiderBaseTier tier;
	protected RoiderBaseTier matchedStationToTier = null;

	protected IntervalUtil monthlyInterval = new IntervalUtil(20f, 40f);

	public Roider_RoiderBaseIntel(StarSystemAPI system, String factionId, RoiderBaseTier tier) {
		this.system = system;
		this.tier = tier;

		market = Global.getFactory().createMarket("roider_fringeIndieBase_" + Misc.genUID(), "Roider Base", 3);
		market.setSize(3);
		market.setHidden(true);

		market.setFactionId(Factions.INDEPENDENT);

		market.setSurveyLevel(SurveyLevel.FULL);

		market.setFactionId(factionId);
		market.addCondition(Conditions.POPULATION_3);

		market.addIndustry(Industries.POPULATION);
		market.addIndustry(Industries.SPACEPORT);
        market.addIndustry(Roider_Industries.DIVES);
		market.addIndustry(Industries.PATROLHQ);

//		market.addSubmarket(Roider_Submarkets.RESUPPLY_MARKET);
		market.addSubmarket(Roider_Submarkets.FRINGE_OPEN_MARKET);
		market.addSubmarket(Roider_Submarkets.FRINGE_BLACK_MARKET);
//		market.addSubmarket(Submarkets.SUBMARKET_STORAGE);

		market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());

		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<>();
		weights.put(LocationType.IN_ASTEROID_BELT, 10f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 10f);
		weights.put(LocationType.IN_RING, 10f);
		weights.put(LocationType.IN_SMALL_NEBULA, 10f);
		weights.put(LocationType.GAS_GIANT_ORBIT, 10f);
		weights.put(LocationType.PLANET_ORBIT, 10f);
		WeightedRandomPicker<EntityLocation> locs = BaseThemeGenerator.getLocations(null, system, null, 100f, weights);
		EntityLocation loc = locs.pick();

		if (loc == null) {
			endImmediately();
			return;
		}

		AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(system, loc, Entities.MAKESHIFT_STATION, factionId);

		if (added == null || added.entity == null) {
			endImmediately();
			return;
		}

		entity = added.entity;


		String name = generateName();
		if (name == null) {
			endImmediately();
			return;
		}

		market.setName(name);
		entity.setName(name);


//		boolean down = false;
//		if (entity.getOrbitFocus() instanceof PlanetAPI) {
//			PlanetAPI planet = (PlanetAPI) entity.getOrbitFocus();
//			if (!planet.isStar()) {
//				down = true;
//			}
//		}
//		if (down) {
//			BaseThemeGenerator.convertOrbitPointingDown(entity);
//		}
		BaseThemeGenerator.convertOrbitWithSpin(entity, -5f);

		market.setPrimaryEntity(entity);
		entity.setMarket(market);

		entity.setSensorProfile(1f);
		entity.setDiscoverable(true);
		entity.getDetectedRangeMod().modifyFlat("gen", 5000f);

		market.setEconGroup(market.getId());
		market.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);

		market.reapplyIndustries();

        Roider_Dives dives = (Roider_Dives) market.getIndustry(Roider_Industries.DIVES);
        if (!dives.canMine()) {
			endImmediately();
            return;
        }

//		PortsideBarData.getInstance().addEvent(new PirateBaseRumorBarEvent(this));
	}

    public void init() {
        if (isDone()) return;

		Global.getSector().getEconomy().addMarket(market, true);

		log.info(String.format("Added roider base in [%s], tier: %s", system.getName(), tier.name()));

		Global.getSector().getIntelManager().addIntel(this, true);
		timestamp = null;

		Global.getSector().getListenerManager().addListener(this);
		Global.getSector().getEconomy().addUpdateListener(this);
    }

	@Override
	public boolean isHidden() {
		if (super.isHidden()) return true;
		//if (true) return false;
		return timestamp == null;
	}

	public StarSystemAPI getSystem() {
		return system;
	}

	protected String pickStationType() {
		WeightedRandomPicker<String> stations = new WeightedRandomPicker<>();

		if (getFactionForUIColors().getCustom().has(Factions.CUSTOM_PIRATE_BASE_STATION_TYPES)) {
			try {
				JSONObject json = getFactionForUIColors().getCustom().getJSONObject(Factions.CUSTOM_PIRATE_BASE_STATION_TYPES);
				for (String key : JSONObject.getNames(json)) {
					stations.add(key, (float) json.optDouble(key, 0f));
				}
			} catch (JSONException e) {
				stations.clear();
			}
		}

		if (stations.isEmpty()) {
			stations.add(Industries.ORBITALSTATION, 5f);
			stations.add(Industries.ORBITALSTATION_MID, 3f);
			stations.add(Industries.ORBITALSTATION_HIGH, 1f);
		}

		//stations.add(Industries.STARFORTRESS, 100000f);
		return stations.pick();
	}

	protected Industry getStationIndustry() {
		for (Industry curr : market.getIndustries()) {
			if (curr.getSpec().hasTag(Industries.TAG_STATION)) {
				return curr;
			}
		}
		return null;
	}

	protected Industry getRoiderIndustry() {
		for (Industry curr : market.getIndustries()) {
			if (curr.getSpec().hasTag(Roider_Tags.ROID_MINING)) {
				return curr;
			}
		}
		return null;
	}

	protected void updateStationIfNeeded() {
		if (matchedStationToTier == tier) return;

 		matchedStationToTier = tier;
		monthsAtCurrentTier = 0;

		Industry stationInd = getStationIndustry();

		String currStationIndId = null;
		if (stationInd != null) {
			currStationIndId = stationInd.getId();
			market.removeIndustry(stationInd.getId(), null, false);
			stationInd = null;
		}

		if (currStationIndId == null) {
			currStationIndId = pickStationType();
		}

		if (currStationIndId == null) return;

		market.addIndustry(currStationIndId);
		stationInd = getStationIndustry();
		if (stationInd == null) return;

		stationInd.finishBuildingOrUpgrading();


		CampaignFleetAPI fleet = Misc.getStationFleet(entity);
		if (fleet == null) return;

		List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		if (members.size() < 1) return;

		fleet.inflateIfNeeded();

		FleetMemberAPI station = members.get(0);

		WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<>();
		int index = 1; // index 0 is station body
		for (String slotId : station.getVariant().getModuleSlots()) {
			ShipVariantAPI mv = station.getVariant().getModuleVariant(slotId);
			if (Misc.isActiveModule(mv)) {
				picker.add(index, 1f);
			}
			index++;
		}

		float removeMult = 0f;
        boolean unionHQ = false;

		switch (tier) {
		case TIER_1_1MODULE:
		case TIER_2_1MODULE:
			removeMult = 0.67f;
			break;
		case TIER_3_2MODULE:
			removeMult = 0.33f;
			break;
		case TIER_4_3MODULE:
		case TIER_5_3MODULE:
			removeMult = 0;
//            unionHQ = true;
			break;

		}

		int remove = Math.round(picker.getItems().size() * removeMult);
		if (remove < 1 && removeMult > 0) remove = 1;
		if (remove >= picker.getItems().size()) {
			remove = picker.getItems().size() - 1;
		}

		for (int i = 0; i < remove; i++) {
			Integer pick = picker.pickAndRemove();
			if (pick != null) {
				station.getStatus().setHullFraction(pick, 0f);
				station.getStatus().setDetached(pick, true);
				station.getStatus().setPermaDetached(pick, true);
			}
		}

        // Update Roider Dives if needed.
        if (getRoiderIndustry() == null) return; // To be safe.

        if (unionHQ) {
            if (getRoiderIndustry().getId().equals(Roider_Industries.DIVES)) {
                market.removeIndustry(Roider_Industries.DIVES, null, false);
                market.addIndustry(Roider_Industries.UNION_HQ);

                Industry hqInd = getRoiderIndustry();
                hqInd.finishBuildingOrUpgrading();
            }
        } else {
            if (getRoiderIndustry().getId().equals(Roider_Industries.UNION_HQ)) {
                market.removeIndustry(Roider_Industries.UNION_HQ, null, false);
                market.addIndustry(Roider_Industries.DIVES);

                Industry hqInd = getRoiderIndustry();
                hqInd.finishBuildingOrUpgrading();
            }
        }
	}

	public CampaignFleetAPI getAddedListenerTo() {
		return addedListenerTo;
	}



	protected CampaignFleetAPI addedListenerTo = null;
	@Override
	protected void advanceImpl(float amount) {
		//makeKnown();
		float days = Global.getSector().getClock().convertToDays(amount);
		//days *= 1000f;
		//Global.getSector().getCurrentLocation().getName()
		//entity.getContainingLocation().getName()
		if (getPlayerVisibleTimestamp() == null && entity.isInCurrentLocation() && isHidden()) {
			makeKnown();
			sendUpdateIfPlayerHasIntel(DISCOVERED_PARAM, false);
		}


		//System.out.println("Name: " + market.getName());

		CampaignFleetAPI fleet = Misc.getStationFleet(market);
		if (fleet != null && addedListenerTo != fleet) {
			if (addedListenerTo != null) {
				addedListenerTo.removeEventListener(this);
			}
			fleet.addEventListener(this);
			addedListenerTo = fleet;
		}

		monthlyInterval.advance(days);
		if (monthlyInterval.intervalElapsed()) {
			checkForTierChange();
		}

		updateStationIfNeeded();
	}

	protected void checkForTierChange() {
		if (entity.isInCurrentLocation()) return;

		float minMonths = Global.getSettings().getFloat("pirateBaseMinMonthsForNextTier");
		if (monthsAtCurrentTier > minMonths) {
			float prob = (monthsAtCurrentTier - minMonths) * 0.1f;
			if ((float) Math.random() < prob) {
				RoiderBaseTier next = getNextTier(tier);
				if (next != null) {
					tier = next;
					updateStationIfNeeded();
					monthsAtCurrentTier = 0;
					return;
				}
			}
		}

		monthsAtCurrentTier++;
	}

	protected RoiderBaseTier getNextTier(RoiderBaseTier tier) {
		switch (tier) {
		case TIER_1_1MODULE: return RoiderBaseTier.TIER_2_1MODULE;
		case TIER_2_1MODULE: return RoiderBaseTier.TIER_3_2MODULE;
		case TIER_3_2MODULE: return RoiderBaseTier.TIER_4_3MODULE;
		case TIER_4_3MODULE: return RoiderBaseTier.TIER_5_3MODULE;
		case TIER_5_3MODULE: return null;
		}
		return null;
	}

	protected RoiderBaseTier getPrevTier(RoiderBaseTier tier) {
		switch (tier) {
		case TIER_1_1MODULE: return null;
		case TIER_2_1MODULE: return RoiderBaseTier.TIER_1_1MODULE;
		case TIER_3_2MODULE: return RoiderBaseTier.TIER_2_1MODULE;
		case TIER_4_3MODULE: return RoiderBaseTier.TIER_3_2MODULE;
		case TIER_5_3MODULE: return RoiderBaseTier.TIER_4_3MODULE;
		}
		return null;
	}

	public void makeKnown() {
		makeKnown(null);
	}
	public void makeKnown(TextPanelAPI text) {
		if (getPlayerVisibleTimestamp() == null) {
			Global.getSector().getIntelManager().removeIntel(this);
			Global.getSector().getIntelManager().addIntel(this, text == null, text);
		}
	}

    @Override
	public float getTimeRemainingFraction() {
		float f = 1f - elapsedDays / duration;
		return f;
	}



	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		log.info(String.format("Removing roider base at [%s]", system.getName()));
		Global.getSector().getListenerManager().removeListener(this);
		Global.getSector().getEconomy().removeUpdateListener(this);

		Global.getSector().getEconomy().removeMarket(market);
		Misc.removeRadioChatter(market);
		market.advance(0f);
	}

	@Override
	protected void notifyEnded() {
		super.notifyEnded();
	}



    @Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isEnding()) return;

		//CampaignFleetAPI station = Misc.getStationFleet(market); // null here since it's the skeleton station at this point
		if (addedListenerTo != null && fleet == addedListenerTo) {
			Misc.fadeAndExpire(entity);
			endAfterDelay();

			Roider_RoiderBaseManager.getInstance().incrDestroyed();
			Roider_RoiderBaseManager.markRecentlyUsedForBase(system);
		}
	}

    @Override
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {}

    @Override
	public boolean runWhilePaused() {
		return false;
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
	}

    @Override
	public String getSortString() {
		String base = Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix());
		return base + " Base";
		//return "Pirate Base";
	}

	public String getName() {
		String name = market.getName();
		if (isEnding()) {
			//return "Base Abandoned - " + name;
			return "Roider Base - Abandoned";
		}
		if (getListInfoParam() == DISCOVERED_PARAM) {
			return "Roider Base - Discovered";
		}
		if (entity.isDiscoverable()) {
			return "Roider Base - Exact Location Unknown";
		}
		return "Roider Base - " + name;
	}

	@Override
	public FactionAPI getFactionForUIColors() {
		return market.getFaction();
	}

    @Override
	public String getSmallDescriptionTitle() {
		return getName();
	}

    @Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {

		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		//info.addPara(getName(), c, 0f);

		//info.addSectionHeading(getName(), Alignment.MID, 0f);

		FactionAPI faction = market.getFaction();

		info.addImage(faction.getLogo(), width, 128, opad);

		String has = faction.getDisplayNameHasOrHave();
        String local = "local";

        Industry ind = getRoiderIndustry();
        if (ind != null && ind.getId().equals(Roider_Industries.UNION_HQ)) {
            local = "regional";
        }

		info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + has +
				" established a base in the " +
				market.getContainingLocation().getNameWithLowercaseType() + ". " +
						"The base serves as a staging area for " + local + " mining operations.",
				opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());

		if (!entity.isDiscoverable()) {
			switch (tier) {
			case TIER_1_1MODULE:
				info.addPara("It has very limited defensive capabilities.", opad);
				break;
			case TIER_2_1MODULE:
				info.addPara("It has limited defensive capabilities.", opad);
				break;
			case TIER_3_2MODULE:
				info.addPara("It has fairly well-developed defensive capabilities.", opad);
				break;
			case TIER_4_3MODULE:
			case TIER_5_3MODULE:
				info.addPara("It has very well-developed defensive capabilities " +
					 	 	 "and is protected by a large number of fleets.", opad);
				break;

			}
		} else {
			info.addPara("You have not yet discovered the exact location or capabilities of this base.", opad);
		}
	}

    @Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "pirate_base");
		//return market.getFaction().getCrest();
	}

    @Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_EXPLORATION);
		tags.add(market.getFactionId());
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		//return market.getPrimaryEntity();
		if (market.getPrimaryEntity().isDiscoverable()) {
			return system.getCenter();
		}
		return market.getPrimaryEntity();
	}





	private String generateName() {
		MarkovNames.loadIfNeeded();

		MarkovNameResult gen = null;
		for (int i = 0; i < 10; i++) {
			gen = MarkovNames.generate(null);
			if (gen != null) {
				String test = gen.name;
				if (test.toLowerCase().startsWith("the ")) continue;
				String p = pickPostfix();
				if (p != null && !p.isEmpty()) {
					test += " " + p;
				}
				if (test.length() > 22) continue;

				return test;
			}
		}
		return null;
	}

	protected String pickPostfix() {
		WeightedRandomPicker<String> post = new WeightedRandomPicker<>();
//		post.add("Asylum");
		post.add("Astrome");
		post.add("Barrage");
//		post.add("Briganderie");
		post.add("Camp");
		post.add("Cover");
		post.add("Citadel");
		post.add("Den");
		post.add("Donjon");
		post.add("Depot");
		post.add("Fort");
		post.add("Freehold");
		post.add("Freeport");
		post.add("Freehaven");
		post.add("Free Orbit");
		post.add("Galastat");
		post.add("Garrison");
		post.add("Harbor");
		post.add("Haven");
//		post.add("Headquarters");
//		post.add("Hideout");
//		post.add("Hideaway");
		post.add("Hold");
//		post.add("Lair");
		post.add("Locus");
		post.add("Main");
		post.add("Mine Depot");
		post.add("Nexus");
		post.add("Orbit");
		post.add("Port");
		post.add("Post");
		post.add("Presidio");
//		post.add("Prison");
		post.add("Platform");
//		post.add("Corsairie");
		post.add("Refuge");
		post.add("Retreat");
		post.add("Refinery");
		post.add("Shadow");
		post.add("Safehold");
		post.add("Starhold");
		post.add("Starport");
		post.add("Stardock");
		post.add("Sanctuary");
		post.add("Station");
		post.add("Spacedock");
		post.add("Tertiary");
		post.add("Terminus");
		post.add("Terminal");
//		post.add("Tortuga");
		post.add("Ward");
//		post.add("Warsat");
		return post.pick();
	}

    @Override
	public void commodityUpdated(String commodityId) {
		CommodityOnMarketAPI com = market.getCommodityData(commodityId);
		int curr = 0;
		String modId = market.getId();
		MutableStat.StatMod mod = com.getAvailableStat().getFlatStatMod(modId);
		if (mod != null) {
			curr = Math.round(mod.value);
		}

		int a = com.getAvailable() - curr;
		int d = com.getMaxDemand();
		if (d > a) {
			//int supply = Math.max(1, d - a - 1);
			int supply = Math.max(1, d - a);
			com.getAvailableStat().modifyFlat(modId, supply, "Brought in by roiders");
		}
	}

    @Override
	public void economyUpdated() {}

    @Override
	public boolean isEconomyListenerExpired() {
		return isEnded();
	}

	public MarketAPI getMarket() {
		return market;
	}

	protected int monthsAtCurrentTier = 0;

	public RoiderBaseTier getTier() {
		return tier;
	}

	public SectorEntityToken getEntity() {
		return entity;
	}

}










