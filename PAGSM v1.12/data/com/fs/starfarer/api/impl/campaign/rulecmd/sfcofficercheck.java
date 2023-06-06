package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.List;
import java.util.Map;
/*
    Taking code from other code is quite the common thing, like an ouroboros of code taking
 */
public class sfcofficercheck extends BaseCommandPlugin {

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        final MutableCharacterStatsAPI stats = dialog.getInteractionTarget().getActivePerson().getStats();
        final TextPanelAPI text = dialog.getTextPanel();
        final Color hl = Misc.getHighlightColor();
        final String personality = Misc.lcFirst(dialog.getInteractionTarget().getActivePerson().getPersonalityAPI().getDisplayName());

        text.addSkillPanel(dialog.getInteractionTarget().getActivePerson(), false);
        text.setFontSmallInsignia();
        text.addParagraph("Personality: " + personality + ", level: " + stats.getLevel());
        text.highlightInLastPara(hl, personality, "" + stats.getLevel());
        text.addParagraph(dialog.getInteractionTarget().getActivePerson().getPersonalityAPI().getDescription());
        text.setFontInsignia();

        return true;
    }

}