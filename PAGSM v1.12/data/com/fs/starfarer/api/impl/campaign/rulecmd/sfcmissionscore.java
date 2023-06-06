package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

/*
 *	sfcmissionscore <mission string> <score> borrowed from that funny named mod
 */


public class sfcmissionscore extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        return Global.getSettings().getMissionScore(params.get(0).getString(memoryMap)) >= params.get(1).getFloat(memoryMap);
    }
}