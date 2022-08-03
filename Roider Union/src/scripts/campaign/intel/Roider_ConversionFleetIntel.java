package scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Equipment;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_MemFlags;
import java.awt.Color;
import java.util.Set;

/**
 * Author: SafariJohn
 */
public class Roider_ConversionFleetIntel extends BaseIntelPlugin implements FleetEventListener {
    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_ConversionFleetIntel.class, "fleetId", "f");
        x.aliasAttribute(Roider_ConversionFleetIntel.class, "timeRemaining", "t");
        x.aliasAttribute(Roider_ConversionFleetIntel.class, "offeringConversions", "o");
        x.aliasAttribute(Roider_ConversionFleetIntel.class, "playerVisible", "v");
    }

    private final String fleetId;
    private float timeRemaining;
    private boolean offeringConversions;
    private boolean playerVisible;

    public Roider_ConversionFleetIntel(CampaignFleetAPI fleet, float timeRemaining) {
        this.fleetId = fleet.getId();
        this.timeRemaining = timeRemaining;
        offeringConversions = true;
        playerVisible = false;
    }

    @Override
    public void advance(float amount) {
        if (!playerVisible) {
            advanceImpl(amount);
            return;
        }

        super.advance(amount);
    }

    @Override
    protected void advanceImpl(float amount) {
        if (isEnding() || isEnded()) return;

        float days = Misc.getDays(amount);
        timeRemaining -= days;

        CampaignFleetAPI fleet = (CampaignFleetAPI) Global.getSector().getEntityById(fleetId);

        if (fleet == null) return;

        if (fleet.isVisibleToPlayerFleet() && !playerVisible) {
            playerVisible = true;
            Global.getSector().getIntelManager().addIntel(this);
        }

        if (timeRemaining <= 0 || !fleet.isAlive()) {
            endAfterDelay();
        }
    }

    @Override
    protected String getName() {
        CampaignFleetAPI fleet = (CampaignFleetAPI) Global.getSector().getEntityById(fleetId);

        if (fleet != null) return Misc.ucFirst(fleet.getFaction().getDisplayName()) + " Retrofit Fleet";

        return "Retrofit Fleet";
    }

    @Override
    public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "roider_expedition");
    }

    @Override
    public boolean isHidden() {
        return super.isHidden(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isPlayerVisible() {
        if (!playerVisible) return false;

        return super.isPlayerVisible();
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

        CampaignFleetAPI fleet = (CampaignFleetAPI) Global.getSector().getEntityById(fleetId);

        if (fleet == null) {
            info.addImage(Global.getSector().getFaction(Factions.PIRATES).getLogo(), width, 128, opad);

            info.addPara("This retrofit fleet is no longer offering services.", pad);

            return;
        }

        offeringConversions = fleet.getMemoryWithoutUpdate().is(Roider_MemFlags.APR_RETROFITTING, true);

        FactionAPI faction = fleet.getFaction();

		info.addImage(faction.getLogo(), width, 128, opad);

        StringBuilder text = new StringBuilder();
        text.append("You have encountered ").append(faction.getPersonNamePrefixAOrAn())
                    .append(" ").append(faction.getPersonNamePrefix())
                    .append(" retrofit fleet at the ")
                    .append(fleet.getContainingLocation().getNameWithLowercaseType())
                    .append(".");

        info.addPara(text.toString(), pad);

        if (offeringConversions && timeRemaining > 0) {
            text = new StringBuilder();
            text.append("They will continue offering services for another ")
                        .append((int) timeRemaining).append(" days.");

            info.addPara(text.toString(), pad, h, "" + (int) timeRemaining);
        } else {
            info.addPara("They are no longer offering services.", pad);
        }

    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
        CampaignFleetAPI fleet = (CampaignFleetAPI) Global.getSector().getEntityById(fleetId);

        if (fleet != null) offeringConversions = fleet.getMemoryWithoutUpdate().is(Roider_MemFlags.APR_RETROFITTING, true);

        if (timeRemaining > 0 && offeringConversions && fleet != null) {
            Color h = Misc.getHighlightColor();

            info.addPara("Faction: " + fleet.getFaction().getDisplayName(),
                        initPad, fleet.getFaction().getColor(),
                        fleet.getFaction().getDisplayName());
            info.addPara("Location: " + fleet.getContainingLocation().getNameWithTypeIfNebula(),
                        0, h, fleet.getContainingLocation().getNameWithTypeIfNebula());
            info.addPara((int) timeRemaining + " days remaining", 0, h, "" + (int) timeRemaining);
        } else {
            info.addPara("No longer offering services", initPad);
        }
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Roider_Factions.ROIDER_UNION);
        tags.add(Tags.INTEL_FLEET_LOG);

        CampaignFleetAPI fleet = (CampaignFleetAPI) Global.getSector().getEntityById(fleetId);
        if (fleet != null) tags.add(fleet.getFaction().getId());

        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        CampaignFleetAPI fleet = (CampaignFleetAPI) Global.getSector().getEntityById(fleetId);

        if (fleet == null) return null;

        if (fleet.isVisibleToPlayerFleet()) return fleet;

        if (fleet.isInHyperspace()) return null;

        return fleet.getStarSystem().getCenter();
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        endAfterDelay();
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        // if fleet has lost all its Argosi, then it can no longer offer conversions
        boolean hasArgos = false;
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            if (m.getHullId().startsWith(Roider_Equipment.ARGOS)) {
                hasArgos = true;
                break;
            }
        }

        if (!hasArgos) {
            endAfterDelay();
        }
    }

    @Override
    protected void notifyEnded() {
        notifyEnding();
    }

    @Override
    protected void notifyEnding() {
        offeringConversions = false;
        sendUpdateIfPlayerHasIntel(listInfoParam, true);
    }

}
