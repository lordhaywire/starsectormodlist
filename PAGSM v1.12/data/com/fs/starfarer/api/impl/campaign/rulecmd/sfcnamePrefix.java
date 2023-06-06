package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

//thanks to matt damon for this

public class sfcnamePrefix extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        String sfcnamePrefix = params.get(0).getString(memoryMap);

        if (!sfcnamePrefix.endsWith(" ")) {
            sfcnamePrefix = sfcnamePrefix + " ";
        }

        boolean retValue = false;
        if (dialog.getInteractionTarget() instanceof CampaignFleetAPI) {
            CampaignFleetAPI target = (CampaignFleetAPI) dialog.getInteractionTarget();
            if (target.getFlagship() != null
                    && target.getFlagship().getShipName().startsWith(sfcnamePrefix)) {
                retValue = true;
            }
        }
        return retValue;
    }
}