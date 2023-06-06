package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class sfcrunichanges extends BaseCommandPlugin {
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected MemoryAPI memory;


    public static boolean YuniferChecker() {
        PersonAPI sfcruni = Global.getSector().getImportantPeople().getPerson("sfcruni");
        if (sfcruni == null) return false;

        OfficerDataAPI sfcruniOfficer = Global.getSector().getPlayerFleet().getFleetData().getOfficerData(sfcruni);
        if (sfcruniOfficer == null) return false;

        return true;
    }

    protected static boolean removeYunifer(InteractionDialogAPI dialog, boolean unsetCaptain)
    {
        PersonAPI sfcruni = Global.getSector().getImportantPeople().getPerson("sfcruni");
        if (sfcruni == null) return unsetCaptain;

        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf.getFleetData().getOfficerData(sfcruni) != null) {
            pf.getFleetData().removeOfficer(sfcruni);
            AddRemoveCommodity.addOfficerLossText(sfcruni, dialog.getTextPanel());
            if (unsetCaptain) {
                FleetMemberAPI member = getYunifersFleetMember();
                if (member != null) member.setCaptain(null);
            }
        }
        Global.getSoundPlayer().playUISound("ui_cargo_marines_drop", 1f, 1f);
        return unsetCaptain;
    }

    public static FleetMemberAPI getYunifersFleetMember() {
        PersonAPI sfcruni = Global.getSector().getImportantPeople().getPerson("sfcruni");
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        for (FleetMemberAPI member : pf.getFleetData().getMembersListCopy()) {
            if (member.getCaptain() == sfcruni) {
                return member;
            }
        }
        return null;
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

        PersonAPI sfcruni = Global.getSector().getImportantPeople().getPerson("sfcruni");

        switch (cmd) {
            case "changeruniPersonality":
                sfcruni.setPersonality(param);
                return true;
            case "changeruniChatter":
                sfcruni.getMemoryWithoutUpdate().set("$chatterChar", param);
            case "yuniferChecker":
                return YuniferChecker();
            case "checkYuniferPersonality":
                return sfcruni.getPersonalityAPI().getId().equals(param);
            case "yuniferLeave":
                return removeYunifer(dialog, true);
            default:
                return true;
        }
    }
}