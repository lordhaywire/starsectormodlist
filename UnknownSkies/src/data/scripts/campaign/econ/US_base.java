/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class US_base extends BaseHazardCondition{
    public static float PRODUCTION_BONUS = 0.25f;

    @Override
    public void apply(String id) {
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id, PRODUCTION_BONUS, "Ancient Military Ruins");
    }

    @Override
    public void unapply(String id) {
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodify(id);
    }
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                "%s ships produced quality.",
                10f,
                Misc.getHighlightColor(),
                "+" + (int)((PRODUCTION_BONUS)*100) + "%"
        );
    }
}
