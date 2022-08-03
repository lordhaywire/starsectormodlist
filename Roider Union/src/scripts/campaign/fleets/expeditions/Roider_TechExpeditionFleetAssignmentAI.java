package scripts.campaign.fleets.expeditions;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_MemFlags;
import scripts.Roider_Misc;

public class Roider_TechExpeditionFleetAssignmentAI extends RouteFleetAssignmentAI {

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_TechExpeditionFleetAssignmentAI.class, "originalFaction", "o");
        x.aliasAttribute(Roider_TechExpeditionFleetAssignmentAI.class, "pirate", "p");
        x.aliasAttribute(Roider_TechExpeditionFleetAssignmentAI.class, "piracyCheck", "c");
    }

    protected String originalFaction;
	protected boolean pirate;
	protected IntervalUtil piracyCheck = new IntervalUtil(0.2f, 0.4f);
	public Roider_TechExpeditionFleetAssignmentAI(CampaignFleetAPI fleet, RouteData route, boolean pirate) {
		super(fleet, route);
        originalFaction = fleet.getFaction().getId();
		this.pirate = pirate;
	}

	@Override
	protected String getTravelActionText(RouteSegment segment) {
		//if (segment.systemTo == route.getMarket().getContainingLocation()) {
		if (segment.to == route.getMarket().getPrimaryEntity()) {
			return "returning to " + route.getMarket().getName();
		}
		return "travelling";
	}

	@Override
	protected String getInSystemActionText(RouteSegment segment) {
		return "exploring";
	}

    @Override
	protected String getStartingActionText(RouteSegment segment) {
		if (segment.from == route.getMarket().getPrimaryEntity()) {
			return "preparing for an expedition";
		}
		return "exploring";
	}


	@Override
	protected void addLocalAssignment(RouteSegment segment, boolean justSpawned) {
        Object custom = segment.custom;

        if (custom != null && (custom instanceof List)) {
            List<SectorEntityToken> stashes = (List) custom;

            if (stashes.isEmpty()) {
                route.expire();
                return;
            }

            // Pick closest stash
            SectorEntityToken target = null;
            float minDist = Short.MAX_VALUE * Short.MAX_VALUE;
            for (SectorEntityToken e : stashes) {
                float dist = Roider_Misc.getDistanceSquared(fleet, e);
                if (dist < minDist) {
                    minDist = dist;
                    target = e;
                }
            }

            // Fallback in case no stashes or something
            boolean collectStash = true;
            if (target == null) {
                BaseThemeGenerator.EntityLocation loc = BaseThemeGenerator.pickHiddenLocationNotNearStar(
                        route.getRandom(), fleet.getStarSystem(), 50f + route.getRandom().nextFloat() * 100f, null);

                Vector2f currLoc = loc.location;
                if (loc.orbit != null) currLoc = loc.orbit.computeCurrentLocation();
                if (currLoc == null) currLoc = new Vector2f();

                target = fleet.getStarSystem().createToken(currLoc);
                collectStash = false;
            } else {
                stashes.remove(target);
            }

            if (justSpawned) {
                Vector2f loc = Misc.getPointAtRadius(new Vector2f(target.getLocation()), 500);
                fleet.setLocation(loc.x, loc.y);
            }

            if (collectStash) fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, target, 100f, "collecting stash", new Roider_ExpeditionStashPickupScript(fleet, target));
            else fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, target, 100f, "exploring");
        } else {
            super.addLocalAssignment(segment, justSpawned);
        }
	}


	@Override
	public void advance(float amount) {
		super.advance(amount);

        float days = Global.getSector().getClock().convertToDays(amount);
        piracyCheck.advance(days);
        if (piracyCheck.intervalElapsed()) {
            if (pirate) doPiracyCheck();

            if (fleet.getFaction().getId().equals(Factions.PIRATES)) {
                Misc.makeNoRepImpact(fleet, "roider_pirate");
            } else {
                // Clear tags
                Misc.makeNotLowRepImpact(fleet, "roider_tOff");

                if (pirate || !fleet.isTransponderOn() || isThieveryDetected()) Misc.makeLowRepImpact(fleet, "roider_tOff");
            }
        }
	}

	protected void doPiracyCheck() {
		if (fleet.getBattle() != null) return;


		boolean isCurrentlyPirate = fleet.getFaction().getId().equals(Factions.PIRATES);

		if (fleet.isTransponderOn() && !isCurrentlyPirate) {
			return;
		}

        List<CampaignFleetAPI> visible = Misc.getVisibleFleets(fleet, false);

		if (isCurrentlyPirate) {
			if (visible.isEmpty()) {
				fleet.setFaction(originalFaction, true);
				Misc.clearTarget(fleet, true);
			}
			return;
		}

		if (visible.size() == 1) {
			int weakerCount = 0;
			for (CampaignFleetAPI other : visible) {
				if (fleet.getAI() != null &&
                            Global.getSector().getFaction(Factions.PIRATES).isHostileTo(other.getFaction())) {
					EncounterOption option = fleet.getAI().pickEncounterOption(null, other, true);
					if (option == EncounterOption.ENGAGE || option == EncounterOption.HOLD) {
						float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
						VisibilityLevel level = other.getVisibilityLevelTo(fleet);
						boolean seesComp = level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS ||
										   level == VisibilityLevel.COMPOSITION_DETAILS;
						if (dist < 800f && seesComp) {
							weakerCount++;
						}
					}
				}
			}

			if (weakerCount == 1) {
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
				fleet.setNoFactionInName(true);
				fleet.setFaction(Factions.PIRATES, true);
			}
		}

	}

    private boolean isThieveryDetected() {
        return Misc.flagHasReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_HOSTILE, Roider_MemFlags.THIEF_KEY);
    }




}










