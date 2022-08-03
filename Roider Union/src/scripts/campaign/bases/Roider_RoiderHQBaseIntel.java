package scripts.campaign.bases;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;

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
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
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
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_MemFlags;

public class Roider_RoiderHQBaseIntel extends BaseIntelPlugin implements EveryFrameScript, FleetEventListener,
																EconomyAPI.EconomyUpdateListener {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_RoiderHQBaseIntel.class, "system", "s");
        x.aliasAttribute(Roider_RoiderHQBaseIntel.class, "market", "m");
        x.aliasAttribute(Roider_RoiderHQBaseIntel.class, "entity", "e");
        x.aliasAttribute(Roider_RoiderHQBaseIntel.class, "elapsedDays", "ed");
        x.aliasAttribute(Roider_RoiderHQBaseIntel.class, "duration", "d");
        x.aliasAttribute(Roider_RoiderHQBaseIntel.class, "monthlyInterval", "i");
    }

	public static Object DISCOVERED_PARAM = new Object();

	public static Logger log = Global.getLogger(Roider_RoiderHQBaseIntel.class);

	protected String system;
	protected MarketAPI market;
	protected SectorEntityToken entity;

	protected float elapsedDays = 0f;
	protected float duration = 45f;

	protected IntervalUtil monthlyInterval = new IntervalUtil(20f, 40f);

	public Roider_RoiderHQBaseIntel(StarSystemAPI system, String factionId) {
		this.system = system.getId();

		market = Global.getFactory().createMarket("roider_fringeHQBase_" + Misc.genUID(), "Roider Base", 4);
		market.setSize(4);
		market.setHidden(true);

		market.setFactionId(Factions.INDEPENDENT);

		market.setSurveyLevel(SurveyLevel.FULL);

		market.setFactionId(factionId);
		market.addCondition(Conditions.POPULATION_4);

		market.addIndustry(Industries.POPULATION);
		market.addIndustry(Industries.SPACEPORT);
		market.addIndustry(Industries.HEAVYBATTERIES);
        // Battlestation added later
		market.addIndustry(Industries.REFINING);
        market.addIndustry(Roider_Industries.UNION_HQ);
        market.addIndustry(Roider_Industries.SHIPWORKS);

		market.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market.addSubmarket(Submarkets.SUBMARKET_BLACK);
		market.addSubmarket(Submarkets.SUBMARKET_STORAGE);

		market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());

		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<>();
		weights.put(LocationType.IN_ASTEROID_BELT, 10f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 10f);
		weights.put(LocationType.IN_RING, 10f);
		weights.put(LocationType.IN_SMALL_NEBULA, 10f);
		weights.put(LocationType.GAS_GIANT_ORBIT, 10f);
		weights.put(LocationType.PLANET_ORBIT, 10f);
		weights.put(LocationType.STAR_ORBIT, 1f);
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

//		entity.setSensorProfile(1f);
		entity.setDiscoverable(true);
//		entity.getDetectedRangeMod().modifyFlat("gen", 5000f);

		market.setEconGroup(market.getId());
		market.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);

		market.addIndustry(Industries.BATTLESTATION);

		market.reapplyIndustries();

        market.getMemoryWithoutUpdate().set(Roider_MemFlags.FRINGE_HQ, true);

//        Roider_Dives dives = (Roider_Dives) market.getIndustry(Roider_Industries.UNION_HQ);
//        if (!dives.canMine()) {
//			endImmediately();
//            return;
//        }

//		PortsideBarData.getInstance().addEvent(new PirateBaseRumorBarEvent(this));
	}

    public void init() {
        if (isDone()) return;

		Global.getSector().getEconomy().addMarket(market, true);

		log.info(String.format("Added roider HQ base in [%s]", getSystem().getName()));

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
		return Global.getSector().getStarSystem(system);
	}

	public CampaignFleetAPI getAddedListenerTo() {
		return addedListenerTo;
	}

	protected CampaignFleetAPI addedListenerTo = null;
	@Override
	protected void advanceImpl(float amount) {
		if (getPlayerVisibleTimestamp() == null && entity.isInCurrentLocation() && isHidden()) {
			makeKnown();
			sendUpdateIfPlayerHasIntel(DISCOVERED_PARAM, false);

            market.setEconGroup(null);
            market.setHidden(false);
            entity.setDiscoverable(false);
		}

		CampaignFleetAPI fleet = Misc.getStationFleet(market);
		if (fleet != null && addedListenerTo != fleet) {
			if (addedListenerTo != null) {
				addedListenerTo.removeEventListener(this);
			}
			fleet.addEventListener(this);
			addedListenerTo = fleet;
		}
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
		log.info(String.format("Removing roider base at [%s]", getSystem().getName()));
		Global.getSector().getListenerManager().removeListener(this);

		Global.getSector().getEconomy().removeMarket(market);
		Global.getSector().getEconomy().removeUpdateListener(this);
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
			Roider_RoiderBaseManager.markRecentlyUsedForBase(getSystem());
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
        String base = "Roider Union";
		if (isEnding()) {
			//return "Base Abandoned - " + name;
			return base + " Base - Abandoned";
		}
		if (getListInfoParam() == DISCOVERED_PARAM) {
			return base + " Base - Discovered";
		}
		if (entity.isDiscoverable()) {
			return base + " Base - Exact Location Unknown";
		}
		return base + " Base - " + name;
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
        String local = "regional";

		info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + has +
				" established a base in the " +
				market.getContainingLocation().getNameWithLowercaseType() + ". " +
						"The base serves as a staging area for " + local + " mining operations.",
				opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());

		if (!entity.isDiscoverable()) {
            info.addPara("It has very well-developed defensive capabilities " +
                         "and is protected by a large number of fleets.", opad);
		} else {
			info.addPara("You have not yet discovered the exact location or capabilities of this base.", opad);
		}
	}

    @Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "roider_base");
		//return market.getFaction().getCrest();
	}

    @Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_EXPLORATION);
		tags.add(Roider_Factions.ROIDER_UNION);
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		//return market.getPrimaryEntity();
		if (market.getPrimaryEntity().isDiscoverable()) {
			return getSystem().getCenter();
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

		int avWithoutPenalties = (int) Math.round(com.getAvailableStat().getBaseValue());
		for (MutableStat.StatMod m : com.getAvailableStat().getFlatMods().values()) {
			if (m.value < 0) continue;
			avWithoutPenalties += (int) Math.round(m.value);
		}
		
		int a = avWithoutPenalties - curr;
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

	public SectorEntityToken getEntity() {
		return entity;
	}

}










