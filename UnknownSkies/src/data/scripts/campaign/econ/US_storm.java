/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class US_storm extends BaseHazardCondition {
	
    public static float HAZARD_PENALTY = 50;
    public static float ACCESS_PENALTY = 15;
    public static float DEFENSE_BONUS = 1.5f;

    @Override
    public void apply(String id) {
        
//        Object test = Global.getSettings().getSpec(ConditionGenDataSpec.class, condition.getId(), true);
//        if (test instanceof ConditionGenDataSpec) {
//            ConditionGenDataSpec spec = (ConditionGenDataSpec) test;
//            float hazard = spec.getHazard();
//            if (hazard != 0) {
//                market.getHazard().modifyFlat(id, hazard, condition.getName());
//            }
//        }

        super.apply(id);
        
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, DEFENSE_BONUS, "Dust Storm");
        
	market.getAccessibilityMod().modifyFlat(id, -ACCESS_PENALTY/100f, "Dust Storm");        
    }

    @Override
    public void unapply(String id) {
//        market.getHazard().unmodify(id);
        
        super.unapply(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        market.getAccessibilityMod().unmodifyFlat(id);
    }
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                "%s defense rating.",
                10f,
                Misc.getHighlightColor(),
                "+" + (int)((DEFENSE_BONUS-1)*100) + "%"
        );
        
        tooltip.addPara(
                "%s accessibility.",
                10f, 
                Misc.getHighlightColor(),
                "-" + (int) ACCESS_PENALTY + "%"
        );
    }
}
