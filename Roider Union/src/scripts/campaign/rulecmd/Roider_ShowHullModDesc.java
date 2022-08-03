package scripts.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

/**
 * Roider_ShowHullModDesc <id>
 * Author: SafariJohn
 */
public class Roider_ShowHullModDesc extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (params.isEmpty()) return false;

		String id = params.get(0).getString(memoryMap);

        HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);

        if (spec == null) return false;

        TextPanelAPI text = dialog.getTextPanel();

        TooltipMakerAPI tooltip = text.beginTooltip();
        TooltipMakerAPI desc = tooltip.beginImageWithText(spec.getSpriteName(), 32);
        desc.addTitle(spec.getDisplayName());
        tooltip.addImageWithText(10f);
        switch (id) {
            case "roider_midas":
                tooltip.addPara(spec.getDescriptionFormat(), 10f, Misc.getHighlightColor(), "90%", "15%", "10%");
                break;
            default: tooltip.addPara(spec.getDescription(ShipAPI.HullSize.CRUISER), 10f);
        }
        text.addTooltip();

        return true;
    }

}
