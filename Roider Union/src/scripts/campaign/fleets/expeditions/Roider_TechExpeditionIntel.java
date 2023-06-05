package scripts.campaign.fleets.expeditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_MemFlags;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Author: SafariJohn
 */
public class Roider_TechExpeditionIntel extends BaseIntelPlugin {

    public static final String LAUNCHED = "launched";
    public static final String ENCOUNTERED = "encountered";
    public static final String ENCOUNTERED_THIEF = "encounteredThief";
    public static final String THIEF = "thief";
    public static final String LOST = "lost";
    public static final String RETURNED = "returned";

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "route", "rt");
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "source", "s");
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "dest", "d");
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "factionId", "f");
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "thiefId", "t");
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "endQueueDelay", "eqd");
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "lostDelay", "lod");
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "endDelay", "ed");
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "launched", "ld");
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "knownThief", "kt");
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "returned", "r");
        x.aliasAttribute(Roider_TechExpeditionIntel.class, "lost", "l");
    }

    private final RouteData route;

    private final String source;
    private final String dest;
    private final String factionId;
    private final String thiefId;

    private float endQueueDelay;
    private float lostDelay;
    private float endDelay;

    private boolean launched;
    private boolean knownThief;
    private boolean returned;
    private boolean lost;

    public Roider_TechExpeditionIntel(RouteData route, String sourceMarket,
                String destSystem, String factionId, String thiefId) {
        this.route = route;
        source = sourceMarket;
        dest = destSystem;
        this.factionId = factionId;

        this.thiefId = thiefId;

        endQueueDelay = getBaseDaysAfterEnd();
        lostDelay = route.getSegments().get(3).daysMax;
        endDelay = getBaseDaysAfterEnd();

        launched = false;
        knownThief = false;
        returned = false;
        lost = false;
    }

    // ----------
    // Mechanical
    // ----------

	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);

		if (route.getDelay() > 0) return;
        if (endDelay <= 0) {
            Global.getSector().removeScript(this);
            return;
        }

        if (returned) {
            endDelay -= Misc.getDays(amount);
            return;
        }

        if (!returned && route.isExpired() && route.getCurrentIndex() == route.getSegments().size() - 1) {
            sendUpdateIfPlayerHasIntel(RETURNED, true);
            setListInfoParam(RETURNED);
            returned = true;
            return;
        }

//        if (isPlayerVisible() && Global.getSector().getScripts().contains(this)
//                    && !isImportant()) {
//			Global.getSector().removeScript(this);
//        }

        // Player encounters fleet before picking up intel
        if (!isPlayerVisible() && isFleetVisible()) {
            if (Global.getSector().getIntelManager().hasIntelQueued(this)) Global.getSector().getIntelManager().unqueueIntel(this);
            launched = true;

            // Player is marked thief
            if (isPlayerThiefSpotted()) {
                setListInfoParam(ENCOUNTERED_THIEF);
                Global.getSector().getIntelManager().addIntel(this);
                thieveryDetected();
            } else {
                setListInfoParam(ENCOUNTERED);
                Global.getSector().getIntelManager().addIntel(this);
                setListInfoParam(null);
            }

//			Global.getSector().removeScript(this);
            return;
        }

        if (!launched) {
            launched = true;
            sendUpdateIfPlayerHasIntel(LAUNCHED, true);
//            setListInfoParam(LAUNCHED);
            return;
        }

        // Player is marked thief
        if (isPlayerVisible() && isPlayerThiefSpotted()) {
            sendUpdateIfPlayerHasIntel(THIEF, false);
            thieveryDetected();
        }


		float days = Misc.getDays(amount);

        if (!isPlayerVisible() && endQueueDelay > 0) {
            endQueueDelay -= days;

            if (endQueueDelay <= 0) {
                Global.getSector().getIntelManager().unqueueIntel(this);
            }
        }

		if (route.isExpired()) {
            if (isFleetVisible()) {
                lostDelay = 0;
//                sendUpdateIfPlayerHasIntel("lost", true);
//                lost = true;
            }

            if (lostDelay > 0) {
                lostDelay -= days;
            } else {
                endDelay -= days;
            }

            if (lostDelay <= 0 && !lost) {
                sendUpdateIfPlayerHasIntel(LOST, true);
                setListInfoParam(LOST);
                lost = true;
            }
        }
	}

    private boolean isPlayerMarkedThief() {
        if (knownThief) return false; // Already dealt with

        return Global.getSector().getPlayerFleet().getMemoryWithoutUpdate()
                    .contains(Roider_MemFlags.THIEF_KEY + thiefId);
    }

    private boolean isPlayerThiefSpotted() {
        if (!isPlayerMarkedThief()) return false;

        CampaignFleetAPI fleet = route.getActiveFleet();

        if (fleet == null) return false;

        return fleet.getVisibilityLevelOfPlayerFleet() == VisibilityLevel.COMPOSITION_DETAILS
                    || fleet.getVisibilityLevelOfPlayerFleet() == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS;
    }

    private void thieveryDetected() {
        setListInfoParam(THIEF);
        knownThief = true;

        Misc.setFlagWithReason(route.getActiveFleet().getMemoryWithoutUpdate(),
                    MemFlags.MEMORY_KEY_MAKE_HOSTILE, Roider_MemFlags.THIEF_KEY,
                    true, Short.MAX_VALUE);

        CustomRepImpact impact = new CustomRepImpact();
        impact.limit = RepLevel.HOSTILE;
        impact.delta = -RepRewards.SMALL;

        RepActionEnvelope action = new RepActionEnvelope(RepActions.CUSTOM, impact,
                            null, null, true);

        Global.getSector().adjustPlayerReputation(action, route.getFactionId());
        Global.getSector().adjustPlayerReputation(action, route.getActiveFleet().getCommander());
    }


    // -------
    // Display
    // -------

	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        FactionAPI faction = Global.getSector().getFaction(factionId);
        MarketAPI market = Global.getSector().getEconomy().getMarket(source);
        StarSystemAPI target = Global.getSector().getStarSystem(dest);

		float pad = 3f;
		float opad = 10f;

		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;

		Color tc = getBulletColorForMode(mode);

		bullet(info);

        String infoParam = (String) getListInfoParam();
        if (infoParam == null) infoParam = "";

		if (mode != ListInfoMode.IN_DESC && market != null) {
			info.addPara("Faction: " + faction.getDisplayName(), initPad, tc,
						 faction.getBaseUIColor(), faction.getDisplayName());
			initPad = 0f;

			LabelAPI label = info.addPara("From " + market.getName() + " to " + target.getNameWithTypeIfNebula(), tc, initPad);
			label.setHighlight(market.getName(),
                        target.getNameWithTypeIfNebula());
			label.setHighlightColors(market.getFaction().getBaseUIColor(),
                        getStarSystemColor(target));
		}

        switch (infoParam) {
            case LAUNCHED:
                info.addPara("Expedition launched", tc, initPad);
                break;
            case ENCOUNTERED:
                info.addPara("Expedition encountered", tc, initPad);
                break;
            case ENCOUNTERED_THIEF:
                info.addPara("Expedition encountered", tc, initPad);
                initPad = 0;
            case THIEF:
                info.addPara("Your thievery has been discovered", Misc.getNegativeHighlightColor(), initPad);
                break;
            case LOST:
                info.addPara("Expedition lost", tc, initPad);
                break;
            case RETURNED:
                info.addPara("Expedition over", tc, initPad);
                break;
            default:
                float delay = route.getDelay();
                if (delay > 0) {
                    addDays(info, "until departure", delay, tc, initPad);
                } else {
                    info.addPara("On expedition", tc, initPad);
                }
        }

		unindent(info);
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
        info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
	}


    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        FactionAPI faction = Global.getSector().getFaction(factionId);
        MarketAPI market = Global.getSector().getEconomy().getMarket(source);
        StarSystemAPI target = Global.getSector().getStarSystem(dest);

		Color tc = Misc.getTextColor();
		float opad = 10f;

        // Faction logo
		info.addImage(faction.getLogo(), width, 128, opad);

        // Get star system color
        Color starColor = Misc.getButtonTextColor();
        PlanetAPI star = target.getStar();
        if (star != null) starColor = star.getSpec().getIconColor();

        if (market != null) {
            LabelAPI label = info.addPara("Your contacts " + market.getOnOrAt() + " " + market.getName() +
                     " let you know that " +
                     faction.getPersonNamePrefix() + " roiders are preparing a tech expedition and will soon depart for " +
                     target.getNameWithTypeIfNebula() + ".",
                     opad, tc,
                     faction.getBaseUIColor(),
                     faction.getPersonNamePrefix());

            label.setHighlight(market.getName(),
                        faction.getPersonNamePrefix(),
                        target.getNameWithTypeIfNebula());
            label.setHighlightColors(market.getFaction().getBaseUIColor(),
                        faction.getBaseUIColor(),
                        starColor);
        } else {
            info.addPara("Your contacts let you know that " +
                     faction.getPersonNamePrefix() + " roiders are preparing a tech expedition and will soon depart for " +
                     target.getNameWithTypeIfNebula() + ".",
                     opad, tc,
                     faction.getBaseUIColor(),
                     faction.getPersonNamePrefix());
        }

		addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    private Color getStarSystemColor(StarSystemAPI system) {
        Color starColor = Misc.getButtonTextColor();
        PlanetAPI star = system.getStar();
        if (star != null) starColor = star.getSpec().getIconColor();

        return starColor;
    }

	public List<ArrowData> getArrowData(SectorMapAPI map) {
        MarketAPI market = Global.getSector().getEconomy().getMarket(source);
        StarSystemAPI target = Global.getSector().getStarSystem(dest);

		List<ArrowData> result = new ArrayList<ArrowData>();

		if (market == null || (market.getContainingLocation() == target &&
				market.getContainingLocation() != null &&
				!market.getContainingLocation().isHyperspace())) {
			return null;
		}

		SectorEntityToken entityFrom = market.getPrimaryEntity();
		if (map != null) {
			SectorEntityToken iconEntity = map.getIntelIconEntity(this);
			if (iconEntity != null) {
				entityFrom = iconEntity;
			}
		}

		ArrowData arrow = new ArrowData(entityFrom, target.getCenter());
		arrow.color = getFactionForUIColors().getBaseUIColor();
		result.add(arrow);

		return result;
	}

	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "roider_expedition");
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return route.getMarket().getPrimaryEntity();
	}

	@Override
	public FactionAPI getFactionForUIColors() {
		return Global.getSector().getFaction(factionId);
	}

	public String getName() {
        FactionAPI faction = Global.getSector().getFaction(factionId);
		return Misc.ucFirst(faction.getPersonNamePrefix()) + " Tech Expedition";
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	public String getSortString() {
		return "Roider Tech Expedition";
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add("Roider Tech Expedition");

		FactionAPI faction = Global.getSector().getFaction(factionId);
		tags.add(faction.getId());

		return tags;
	}


    // ---------------
    // Intel Mechanics
    // ---------------

	@Override
	public boolean shouldRemoveIntel() {
		if (route.getDelay() > 0) return false;
		if (isImportant()) return false;
        if (isFleetAlive()) return false;

		return route.isExpired() && endDelay <= 0;
	}

    private boolean isFleetAlive() {
        return route.getActiveFleet() != null && route.getActiveFleet().isAlive();
    }

    private boolean isFleetVisible() {
        return route.getActiveFleet() != null && route.getActiveFleet().isVisibleToPlayerFleet();
    }

	@Override
	public void setImportant(Boolean important) {
		super.setImportant(important);
//		if (isImportant()) {
//			if (!Global.getSector().getScripts().contains(this)) {
//				Global.getSector().addScript(this);
//			}
//		} else {
//			Global.getSector().removeScript(this);
//		}
	}

	@Override
	public void reportRemovedIntel() {
		super.reportRemovedIntel();
		Global.getSector().removeScript(this);
	}

}
