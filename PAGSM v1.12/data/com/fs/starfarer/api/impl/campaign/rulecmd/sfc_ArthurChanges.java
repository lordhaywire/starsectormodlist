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

public class sfc_ArthurChanges extends BaseCommandPlugin {
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;

    public static boolean ArthurChecker() {
        PersonAPI sfcarthur = Global.getSector().getImportantPeople().getPerson("sfcarthur");
        if (sfcarthur == null) return false;

        OfficerDataAPI sfcarthurOfficer = Global.getSector().getPlayerFleet().getFleetData().getOfficerData(sfcarthur);
        if (sfcarthurOfficer == null) return false;

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

        PersonAPI sfcarthur = Global.getSector().getImportantPeople().getPerson("sfcarthur");

        switch (cmd) {
            case "changeARthurPersonality":
                sfcarthur.setPersonality(param);
                return true;
            case "changeArthurChatter":
                sfcarthur.getMemoryWithoutUpdate().set("$chatterChar", param);
                return true;
            case "changeArthurPortrait":
                sfcarthur.setPortraitSprite(Global.getSettings().getSpriteName("characters", param));
            case "ArthurChecker":
                return ArthurChecker();
            default:
                return true;
        }
    }
}