package scripts.campaign.retrofit;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import java.awt.Color;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import scripts.Roider_ModPlugin;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper.RetrofitData;
import starship_legends.RepRecord;

/**
 * Author: SafariJohn
 */
public class Roider_BaseRetrofitManager extends BaseIntelPlugin implements Roider_RetrofitVerifier {

    public static class RetrofitTracker {
        public static void aliasAttributes(XStream x) {
            x.aliasAttribute(RetrofitTracker.class, "ship", "s");
            x.aliasAttribute(RetrofitTracker.class, "data", "da");
            x.aliasAttribute(RetrofitTracker.class, "cost", "c");
            x.aliasAttribute(RetrofitTracker.class, "timeRemaining", "t");
            x.aliasAttribute(RetrofitTracker.class, "paused", "p");
            x.aliasAttribute(RetrofitTracker.class, "pauseReason", "r");
            x.aliasAttribute(RetrofitTracker.class, "done", "d");
        }

        public final FleetMemberAPI ship;
        public final RetrofitData data;
        public final double cost;
        private double timeRemaining;

        private boolean paused;
        private String pauseReason;

        private boolean done = false;

        public RetrofitTracker(FleetMemberAPI ship, RetrofitData data, double cost) {
            this.ship = ship;
            this.data = data;
            this.cost = cost;
            timeRemaining = data.time;

            paused = false;
            pauseReason = "";
        }

        public void advance(double days) {
            timeRemaining -= days;
            if (timeRemaining <= 0) done = true;
        }

        public int getDaysRemaining() {
            return (int) timeRemaining;
        }

        public boolean isPaused() {
            return paused;
        }

        public String getPauseReason() {
            return pauseReason;
        }

        public void pause(String reason) {
            paused = true;
            pauseReason = reason;
        }

        public void unpause() {
            if (!paused) return;

            paused = false;
            pauseReason = "";
        }

        public boolean isDone() {
            return done;
        }
    }

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_BaseRetrofitManager.class, "fitter", "ft");
        x.aliasAttribute(Roider_BaseRetrofitManager.class, "entity", "e");
        x.aliasAttribute(Roider_BaseRetrofitManager.class, "faction", "f");
        x.aliasAttribute(Roider_BaseRetrofitManager.class, "queued", "q");
        x.aliasAttribute(Roider_BaseRetrofitManager.class, "completedOne", "c");

        x.alias("roider_retTrk", RetrofitTracker.class);
        RetrofitTracker.aliasAttributes(x);
    }

    protected static final int COLUMNS = 7;

    protected final String fitter;
    protected final SectorEntityToken entity;
    protected final FactionAPI faction;
    protected final List<RetrofitTracker> queued;

    protected boolean completedOne = false;

    public Roider_BaseRetrofitManager(String fitter,
                SectorEntityToken entity, FactionAPI faction) {
        this(fitter, entity, faction, true);
    }

    public Roider_BaseRetrofitManager(String fitter,
                SectorEntityToken entity, FactionAPI faction,
                boolean addIntel) {
        this.fitter = fitter;
        this.entity = entity;
        this.faction = faction;
        queued = new ArrayList<>();

        if (entity == null || !addIntel) return;

		Global.getSector().getIntelManager().addIntel(this);
    }

    @Override
    public void advanceImpl(float amount) {
        if (entity == null || entity.getMarket() == null || entity.getMarket().isPlanetConditionMarketOnly()) {
            endImmediately();
            return;
        }

        if (queued.isEmpty()) return;
        if (Global.getSector().isPaused()) return;

        // Show intel
        setHidden(false);

        // Retrofitting in progress
        double days = Global.getSector().getClock().convertToDays(amount);
        RetrofitTracker active = queued.get(0);

        // If first paused, pause all.
        if (active.isPaused()) return;

        active.advance(days);
        if (active.isDone()) {
            deliverShip(active, false);

            completedOne = true;
            queued.remove(0);
        }
    }

    public void transferDMods(FleetMemberAPI source, FleetMemberAPI target) {
        Collection<String> hullmods = source.getVariant().getHullMods();
        List<HullModSpecAPI> dMods = new ArrayList<>();

        for (HullModSpecAPI mod : DModManager.getModsWithTags("dmod")) {
            if (hullmods.contains(mod.getId())) dMods.add(mod);
        }

        if (dMods.isEmpty()) return;

        ShipVariantAPI targetVariant = target.getVariant();
		targetVariant = targetVariant.clone();
		targetVariant.setOriginalVariant(null);

        DModManager.setDHull(targetVariant);
        target.setVariant(targetVariant, false, true);

        int sourceDMods = dMods.size();
        DModManager.removeUnsuitedMods(targetVariant, dMods);
        int addedDMods = dMods.size();

        if (!dMods.isEmpty()) {
            for (HullModSpecAPI mod : dMods) {
                targetVariant.addPermaMod(mod.getId());
            }

            // Crudely add random d-mods if some were unsuitable.
            if (sourceDMods > addedDMods) {
                DModManager.addDMods(target, false, sourceDMods - addedDMods, null);
            }
        } else if (sourceDMods > 0) { // All were unsuitable
            // Crudely add random d-mods
            DModManager.addDMods(target, false, sourceDMods - addedDMods, null);
        }

        // Add d-mods based on market's (maxed?) quality?
        float quality = Misc.getShipQuality(entity.getMarket(), faction.getId());
    }

    public void transferSMods(FleetMemberAPI source, FleetMemberAPI target) {
        LinkedHashSet<String> sMods = source.getVariant().getSMods();

        for (String mod : sMods) {
            target.getVariant().addPermaMod(mod, true);
        }
    }

    public void transferStarshipLegendsRep(FleetMemberAPI source, FleetMemberAPI target) {
        if (!Roider_ModPlugin.hasStarshipLegends) return;
        if (!RepRecord.existsFor(source)) return;

        RepRecord.transfer(source, target);
    }

    public void notifyPlayerCompleted(RetrofitTracker finished) {
        notifyPlayerCompleted(finished.ship, finished.data);
    }

    public void notifyPlayerCompleted(FleetMemberAPI ship, RetrofitData data) {
        String text = "The " + ship.getShipName() + " has finished retrofitting to ";

        ShipHullSpecAPI targetSpec = null;
        for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
            if (spec.getHullId().equals(data.targetHull)) {
                targetSpec = spec;
                break;
            }
        }

        if (targetSpec == null) return;

        text += targetSpec.getHullNameWithDashClass() + " at " + entity.getName() + ".";

        MessageIntel message = new MessageIntel(text);
        message.setIcon(Global.getSettings().getSpriteName("intel", "roider_retrofit"));

        Global.getSector().getCampaignUI().addMessage(message);
    }

    public String getFitter() {
        return fitter;
    }

    public SectorEntityToken getEntity() {
        return entity;
    }

    public FactionAPI getFaction() {
        return faction;
    }

    public List<RetrofitTracker> getQueued() {
        return queued;
    }

    public void addToQueue(RetrofitTracker tracker) {
        if (tracker.timeRemaining <= 0) {
            deliverShip(tracker, true);
        } else {
            queued.add(tracker);
        }
    }

    public void deliverShip(RetrofitTracker tracker, boolean toPlayer) {
            // Deliver ship and remove tracker
            SubmarketAPI storage = entity.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE);
            if (storage == null) {
                Logger.getLogger(Roider_BaseRetrofitManager.class.getName()).log(Level.SEVERE, null, "Storage does not exist! Cannot deliver retrofit!");
                return;
            }

            // Create finished ship
            FleetMemberAPI ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, tracker.data.targetHull + "_Hull");
            ship.setShipName(tracker.ship.getShipName());

            // Transfer D-mods
//            transferDMods(active.ship, ship);

            // Transfer S-mods
            transferSMods(tracker.ship, ship);

            // Transfer Starship Legends reputation
            transferStarshipLegendsRep(tracker.ship, ship);

            ship.getRepairTracker().setCR(tracker.ship.getRepairTracker().getCR());


            if (toPlayer) {
                // Add ship to player's fleet
                Global.getSector().getPlayerFleet().getFleetData().addFleetMember(ship);
            } else {
                // Add ship to storage
                storage.getCargo().getMothballedShips().addFleetMember(ship);
            }

            notifyPlayerCompleted(tracker);
    }

    public List<RetrofitData> getRetrofits() {
        return Roider_RetrofitsKeeper.getRetrofits(this, getFitter());
    }

    @Override
    public RetrofitData verifyData(String id, String fitter,
                String source, String target, double cost,
                double time, RepLevel rep, boolean commission) {
        // Recalculate cost if there's a market
        if (entity != null && entity.getMarket() != null
                    && !entity.getMarket().isPlanetConditionMarketOnly()) {
            cost = Roider_RetrofitsKeeper
                    .calculateCost(source, target, entity.getMarket());
        }

        return new RetrofitData(id, fitter, source, target, cost,
                    time, rep, commission);
    }

    @Override
    public boolean runWhilePaused() { return false; }

    //////////////////////////////
    //  INTEL PLUGIN METHODS    //
    //////////////////////////////

    @Override
	public String getSmallDescriptionTitle() {
		return "Retrofits at " + entity.getName();
	}

	@Override
	public FactionAPI getFactionForUIColors() {
		return getFaction();
	}


	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "roider_retrofit");
	}

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        float opad = 10f; // 10f

        info.addTitle("Retrofits at " + entity.getName());

        if (queued.isEmpty()) {
            info.addPara("No retrofits in progress.", opad);
//            info.addPara("Pick up completed retrofits from storage.", opad);

        } else {
            // Show time remaining for first hull
            StringBuilder time = new StringBuilder();
            List<String> highlights = new ArrayList<>();

            RetrofitTracker first = queued.get(0);
            ShipHullSpecAPI firstSpec = null;
            ShipHullSpecAPI firstTargetSpec = null;
            for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
                if (spec.getBaseHullId().equals(first.data.sourceHull)
                            || spec.getHullId().equals(first.data.sourceHull)) {
                    firstSpec = spec;
                }

                if (spec.getHullId().equals(first.data.targetHull)) {
                    firstTargetSpec = spec;
                }

                if (firstSpec != null && firstTargetSpec != null) break;
            }

            if (firstSpec == null || firstTargetSpec == null) {
                info.addPara("Could not find ShipHullSpecAPI for " + first.data.targetHull, opad, Misc.getNegativeHighlightColor());
                return;
            }

            if (first.isPaused()) {
                info.addPara("Retrofits paused because " + first.getPauseReason() + ".", Misc.getNegativeHighlightColor(), opad);
            } else {
                time.append(firstSpec.getNameWithDesignationWithDashClass()).append("\n");
                time.append("to ").append(firstTargetSpec.getNameWithDesignationWithDashClass()).append("\n");
                if ((int) first.getDaysRemaining() == 1) {
                    time.append("- Complete in %s day\n");
                } else {
                    time.append("- Complete in %s days\n");
                }

                highlights.add("" + (int) first.getDaysRemaining());

                // Give projected completion date of all queued hulls
                if (queued.size() > 1) {
                    int days = 0;
                    for (RetrofitTracker tracker : queued) {
                        days += tracker.getDaysRemaining();
                    }

                    if (days == 1) {
                        time.append("\nAll queued retrofits will be complete in %s day");
                    } else {
                        time.append("\nAll queued retrofits will be complete in %s days");
                    }

                    highlights.add("" + days);
                }

                String[] hl = {""};
                info.addPara(time.toString(), opad, Misc.getHighlightColor(), highlights.toArray(hl));
            }
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
//        float pad = 3f;
        float opad = 10f; // 10f

        // Description of what this intel is
        // Location of retrofitting
        // What about finished retrofits?
        // What about when everything is finished?

        info.addImage(faction.getLogo(), width, 128, opad);

        if (queued.isEmpty()) {
            info.addPara("No retrofits in progress.", opad);
            if (completedOne) info.addPara("Pick up completed retrofits from storage.", opad);

        } else {
            // Show queued retrofits
            CampaignFleetAPI playerHulls = FleetFactoryV3.createEmptyFleet(
                        Global.getSector().getPlayerFaction().getId(),
                        FleetTypes.MERC_PRIVATEER, null);
            CampaignFleetAPI retrofitHulls = FleetFactoryV3.createEmptyFleet(
                        faction.getId(),
                        FleetTypes.MERC_PRIVATEER, null);

            for (RetrofitTracker t : queued) {
                playerHulls.getFleetData().addFleetMember(t.ship);
                retrofitHulls.getFleetData().addFleetMember(t.data.targetHull + "_Hull");
            }

            List<FleetMemberAPI> playerMembers = playerHulls.getFleetData().getMembersListCopy();
            List<FleetMemberAPI> retrofitMembers = retrofitHulls.getFleetData().getMembersListCopy();

            // Match names
            for (int i = 0; i < playerMembers.size(); i++) {
                String name = playerMembers.get(i).getShipName();
                retrofitMembers.get(i).setShipName(name);
            }

            List<FleetMemberAPI> members = new ArrayList<>();
            members.addAll(retrofitMembers);

            info.addPara("Queued Retrofits", opad);
//            info.addTitle("Queued Retrofits");
            int rows = (retrofitMembers.size() / COLUMNS) + 1;
            float iconSize = width / COLUMNS;

            Color color = faction.getBaseUIColor();
            info.addShipList(COLUMNS, rows, iconSize, color, members, 0f);


            // Show time remaining for first hull
            StringBuilder time = new StringBuilder();
            List<String> highlights = new ArrayList<>();

            RetrofitTracker first = queued.get(0);
            ShipHullSpecAPI firstSpec = null;
            for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
                if (spec.getHullId().equals(first.data.targetHull)) {
                    firstSpec = spec;
                    break;
                }
            }

            if (firstSpec == null) {
                info.addPara("Could not find ShipHullSpecAPI for" + first.data.targetHull, opad, Misc.getNegativeHighlightColor());
                return;
            }

            if (first.isPaused()) {
                info.addPara("Retrofits paused because " + first.getPauseReason() + ".", Misc.getNegativeHighlightColor(), opad);
            } else {
                time.append(firstSpec.getNameWithDesignationWithDashClass()).append("\n");
                if ((int) first.getDaysRemaining() == 1) {
                    time.append("- Complete in %s day\n");
                } else {
                    time.append("- Complete in %s days\n");
                }

                highlights.add("" + (int) first.getDaysRemaining());

                // Give projected completion date of all queued hulls
                if (queued.size() > 1) {
                    int days = 0;
                    for (RetrofitTracker tracker : queued) {
                        days += tracker.getDaysRemaining();
                    }

                    if (days == 1) {
                        time.append("\nAll queued retrofits will be complete in %s day\n");
                    } else {
                        time.append("\nAll queued retrofits will be complete in %s days\n");
                    }

                    highlights.add("" + days);
                }

                String[] hl = {""};
                info.addPara(time.toString(), opad, Misc.getHighlightColor(), highlights.toArray(hl));
            }
        }
    }

    @Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_FLEET_LOG);
		tags.add(faction.getId());
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return entity;
	}

    @Override
    public boolean isHidden() {
        return hidden != null;
    }

    @Override
	protected void notifyEnded() {
        String text = "The retrofitter at " + entity.getName() + " has been destroyed.";

        MessageIntel message = new MessageIntel(text);
        message.setIcon(Global.getSettings().getSpriteName("intel", "roider_retrofit"));

        Global.getSector().getCampaignUI().addMessage(message);

        // Refund remaining retrofits???

//		Global.getSector().getIntelManager().removeIntel(this);
	}
}
