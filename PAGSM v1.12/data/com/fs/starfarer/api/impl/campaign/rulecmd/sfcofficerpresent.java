/*package com.fs.starfarer.api.impl.campaign.rulecmd.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class sfcofficerpresent extends BaseCommandPlugin {
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;


    @Override
    public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        final MemoryAPI memory = getEntityMemory(memoryMap);
        final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        String cmd = null;


        cmd = params.get(0).getString(memoryMap);
        String param = null;
        if (params.size() > 1) {
            param = params.get(1).getString(memoryMap);
        }
        OfficerDataAPI Officer = Global.getSector().getPlayerFleet().getFleetData().getOfficerData(param);
        if (Officer == null) return false;

        switch (cmd) {
            case "haveOfficer":
                String param = null;
                if (params.size() > 1) {
                    param = params.get(1).getString(memoryMap);
                }
                OfficerDataAPI Officer = Global.getSector().getPlayerFleet().getFleetData().getOfficerData(param);
                if (Officer == null) return false;

                return true;
        }
        return false;
    }
}
*/