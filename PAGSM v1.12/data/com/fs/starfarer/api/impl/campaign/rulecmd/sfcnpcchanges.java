package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class sfcnpcchanges extends BaseCommandPlugin {
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

        final TextPanelAPI text = dialog.getTextPanel();

        PersonAPI sfcnpc = dialog.getInteractionTarget().getActivePerson();

        switch (cmd) {
            case "changenpcPersonality":
                sfcnpc.setPersonality(param);
                return true;
            case "removenpcOfficer":
                FleetMemberAPI member = playerFleet.getFleetData().getMemberWithCaptain(sfcnpc);
                if (member != null) {
                    member.setCaptain(null);}
                playerFleet.getFleetData().removeOfficer(sfcnpc);
                AddRemoveCommodity.addOfficerLossText(sfcnpc, text);
                return true;
            case "changenpcChatter":
                sfcnpc.getMemoryWithoutUpdate().set("$chatterChar", param);
                return true;
            case "changenpcImportanceVeryLow":
                sfcnpc.setImportance(PersonImportance.VERY_LOW);
                return true;
            case "changenpcImportanceLow":
                sfcnpc.setImportance(PersonImportance.LOW);
                return true;
            case "changenpcImportanceMedium":
                sfcnpc.setImportance(PersonImportance.MEDIUM);
                return true;
            case "changenpcImportanceHigh":
                sfcnpc.setImportance(PersonImportance.HIGH);
                return true;
            case "changenpcImportanceVeryHigh":
                sfcnpc.setImportance(PersonImportance.VERY_HIGH);
                return true;
            case "changerheaFirstName":
                sfcnpc.getName().setFirst(param);
                return true;
            case "changerheaLastName":
                sfcnpc.getName().setLast(param);
                return true;
            case "changenpcPortrait":
                sfcnpc.setPortraitSprite(Global.getSettings().getSpriteName("characters", param));
            default:
                return true;
        }
    }
}

