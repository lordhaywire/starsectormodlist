package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.util.List;
import java.util.Map;

// let's see if taking this will work now for fighters!

public class sfcaddfighter extends BaseCommandPlugin {

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        String variant1 = params.get(0).getString(memoryMap);
        CargoAPI fleet = Global.getSector().getPlayerFleet().getCargo();
        fleet.addFighters(variant1, 1);
        TextPanelAPI text = dialog.getTextPanel();
        text.setFontSmallInsignia();
        text.addParagraph("Gained " + variant1, Misc.getPositiveHighlightColor());
        text.highlightInLastPara(Misc.getHighlightColor(), variant1);
        text.setFontInsignia();
        return true;
    }
}