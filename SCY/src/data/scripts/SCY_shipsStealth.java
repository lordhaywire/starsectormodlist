package data.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import static data.scripts.util.SCY_txt.txt;
import java.util.ArrayList;
import java.util.List;

class SCY_shipsStealth implements EveryFrameScript {

    private final String ID="SCY_engineering";
    private final float PLAYER_MOD=25,PLAYER_SUPPLIES=0.5f;
            
    @Override
    public void advance(float amount) {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        float scy=0, total=0;
        List<FleetMemberAPI>SHIPS = new ArrayList<>();
        for (FleetMemberAPI s : player.getFleetData().getMembersListCopy()){
            total++;
            if (s.getHullId().startsWith("SCY_")){
                SHIPS.add(s);
                scy++;
            }
        }
        if (player.getCurrBurnLevel()<3){
            player.getStats().getDetectedRangeMod().modifyPercent(ID, -PLAYER_MOD*(scy/total),txt("stealth_0"));
            for(FleetMemberAPI s : SHIPS){
                s.getStats().getSuppliesPerMonth().modifyMult(ID, PLAYER_SUPPLIES,ID);
            }
        } else {
            player.getStats().getDetectedRangeMod().modifyPercent(ID, PLAYER_MOD*(scy/total),txt("stealth_1"));
//            player.getStats().getDetectedRangeMod().modifyMult(ID, PLAYER_MOD*(scy/total));
            for(FleetMemberAPI s : SHIPS){
                s.getStats().getSuppliesPerMonth().unmodify(ID);
            }
        }
    }    
    
    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
