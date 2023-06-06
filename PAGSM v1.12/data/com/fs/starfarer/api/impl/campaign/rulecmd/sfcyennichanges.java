package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class sfcyennichanges extends BaseCommandPlugin {
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;

    public static boolean RukaChecker() {
        PersonAPI sfcyenni = Global.getSector().getImportantPeople().getPerson("sfcyenni");
        if (sfcyenni == null) return false;

        OfficerDataAPI sfcyenniOfficer = Global.getSector().getPlayerFleet().getFleetData().getOfficerData(sfcyenni);
        if (sfcyenniOfficer == null) return false;

        return true;
    }

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

        final TextPanelAPI text = dialog.getTextPanel();

        PersonAPI sfcyenni = Global.getSector().getImportantPeople().getPerson("sfcyenni");

        switch (cmd) {
            case "changeyenniPersonality":
                sfcyenni.setPersonality(param);
                return true;
            case "changeyenniChatter":
                sfcyenni.getMemoryWithoutUpdate().set("$chatterChar", param);
                return true;
            case "changeyenniPortrait":
                sfcyenni.setPortraitSprite(Global.getSettings().getSpriteName("characters", param));
            case "RukaChecker":
                return RukaChecker();
            default:
                return true;
        }
    }
}