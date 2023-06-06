package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

// Just borrowing a few things from smarter modders...


public class sfcreltrack extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        int sfcrellower = params.get(0).getInt(memoryMap);
        int sfcrelupper = params.get(1).getInt(memoryMap);
        SectorEntityToken entity = dialog.getInteractionTarget();
        if (entity.getActivePerson() == null) return false;
        if (sfcrelupper == 100) {
            return entity.getActivePerson().getRelToPlayer().getRepInt() >= sfcrellower && entity.getActivePerson().getRelToPlayer().getRepInt() <= sfcrelupper;
        } else {
            return entity.getActivePerson().getRelToPlayer().getRepInt() >= sfcrellower && entity.getActivePerson().getRelToPlayer().getRepInt() < sfcrelupper;
        }
    }
}