/*package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class sfcspecialship extends BaseCommandPlugin
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

            boolean sfcziggurat = false;
            if (Global.getCombatEngine().isInCampaign()){
                for (final FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
                    if (member.getHullSpec().getHullId().equals("ziggurat")) {
                        sfcziggurat = true;
                        break;
                    }
                }
            }
            boolean sfcsuperiapetus = false;
            if (Global.getCombatEngine().isInCampaign()){
                for (final FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
                    if (member.getHullSpec().getHullId().equals("sfcsuperiapetus")) {
                        sfcsuperiapetus = true;
                        break;
                    }
                }
            }

            switch (cmd) {
                case "sfczigguratHullmod":
                    for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
                        if (member.getVariant().hasHullMod("fronsec_sierrasconcord") || member.getVariant().hasHullMod("sotf_sierrasconcord"))
                    return true;
                default:
                    return true;
            }
        }
}

 */

/*    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;

    boolean sneed = false;
            if (Global.getCombatEngine().isInCampaign()){
        for (final FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (member.getHullSpec().getHullId().equals("ship_id_here")) {
                sneed = true;
                break;
            }
        }
    }
            if (sneed) {
        final TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpriteName("characters", "claire_ai"), 75);
        text.addPara("%s", pad, flavor, new String[] { "\"" + pickInsults + "\"" });
        tooltip.addImageWithText(pad);
    } else {
        final TooltipMakerAPI text1 = tooltip.beginImageWithText(Global.getSettings().getSpriteName("characters", "claire_ai"), 75);
        text1.addPara("%s", pad, flavor, new String[] { "\"" + pick + "\"" });
        tooltip.addImageWithText(pad);
    }
}

*/