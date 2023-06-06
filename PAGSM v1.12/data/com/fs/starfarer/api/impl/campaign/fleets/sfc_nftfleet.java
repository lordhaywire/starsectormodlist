/*package com.fs.starfarer.api.impl.campaign.fleets;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;

public class sfc_nftfleet extends PersonalFleetScript {

    public sfc_nftfleet() {
        super(People.HYDER);
        setMinRespawnDelayDays(10f);
        setMaxRespawnDelayDays(20f);
    }

    @Override
    public CampaignFleetAPI spawnFleet() {

        MarketAPI nachiketa = Global.getSector().getEconomy().getMarket("nachiketa");

        FleetCreatorMission m = new FleetCreatorMission(random);
        m.beginFleet();

        Vector2f loc = nachiketa.getLocationInHyperspace();

        m.triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.HEGEMONY, FleetTypes.PATROL_MEDIUM, loc);
        m.triggerSetFleetOfficers( OfficerNum.MORE, OfficerQuality.DEFAULT);
        m.triggerSetFleetCommander(getPerson());
        m.triggerSetFleetFaction(Factions.HEGEMONY);
        m.triggerSetPatrol();
        m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, nachiketa);
        m.triggerFleetSetNoFactionInName();
        m.triggerPatrolAllowTransponderOff();
        m.triggerFleetSetName("NFT, Inc. Merchant Militia");
        //m.triggerFleetSetPatrolActionText("patrolling");
        m.triggerOrderFleetPatrol(nachiketa.getStarSystem());

        CampaignFleetAPI fleet = m.createFleet();
        fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
        nachiketa.getContainingLocation().addEntity(fleet);
        fleet.setLocation(nachiketa.getPlanetEntity().getLocation().x, nachiketa.getPlanetEntity().getLocation().y);
        fleet.setFacing((float) random.nextFloat() * 360f);

        return fleet;
    }

    @Override
    public boolean canSpawnFleetNow() {
        MarketAPI nachiketa = Global.getSector().getEconomy().getMarket("nachiketa");
        if (nachiketa == null || nachiketa.hasCondition(Conditions.DECIVILIZED)) return false;
        if (!nachiketa.getFactionId().equals(Factions.HEGEMONY)) return false;
        return true;
    }

    @Override
    public boolean shouldScriptBeRemoved() {
        return false;
    }
}*/