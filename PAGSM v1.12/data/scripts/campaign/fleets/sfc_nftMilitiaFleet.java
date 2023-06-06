/*package data.scripts.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.PersonalFleetScript;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import org.lwjgl.util.vector.Vector2f;

public class sfc_nftMilitiaFleet extends PersonalFleetScript {

    public sfc_nftMilitiaFleet() {
        setMinRespawnDelayDays(10f);
        setMaxRespawnDelayDays(20f);
    }

    @Override
    public CampaignFleetAPI spawnFleet() {

        MarketAPI nachiketa = Global.getSector().getEconomy().getMarket("sindria");

        FleetCreatorMission m = new FleetCreatorMission(random);
        m.beginFleet();

        Vector2f loc = nachiketa.getLocationInHyperspace();

        m.triggerCreateFleet(HubMissionWithTriggers.FleetSize.HUGE, HubMissionWithTriggers.FleetQuality.DEFAULT, Factions.HEGEMONY, FleetTypes.PATROL_MEDIUM, loc);
        m.triggerSetFleetOfficers( HubMissionWithTriggers.OfficerNum.DEFAULT, HubMissionWithTriggers.OfficerQuality.DEFAULT);
        m.triggerSetFleetCommander(getPerson());
        m.triggerSetFleetFaction(Factions.HEGEMONY);
        m.triggerSetPatrol();
        m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, nachiketa);
        m.triggerFleetSetNoFactionInName();
        m.triggerPatrolAllowTransponderOff();
        m.triggerFleetSetName("NFT, Inc. Merchant Militia");
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
        if ((!nachiketa.getFactionId().equals(Factions.HEGEMONY)) || (!nachiketa.getFactionId().equals("ironshell"))) return false;
        return true;
    }

    @Override
    public boolean shouldScriptBeRemoved() {
        return false;
    }

}*/
