/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.econ;

//import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
//import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class US_virus extends BaseHazardCondition implements MarketImmigrationModifier {

    private float DEFENSE_MALUS = 0.5f;
    
    @Override
    public void apply(String id) {
        
        super.apply(id);
//        Object test = Global.getSettings().getSpec(ConditionGenDataSpec.class, condition.getId(), true);
//        if (test instanceof ConditionGenDataSpec) {
//            ConditionGenDataSpec spec = (ConditionGenDataSpec) test;
//            float hazard = spec.getHazard();
//            if (hazard != 0) {
//                market.getHazard().modifyFlat(id, hazard, condition.getName());
//            }
//        }
        
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, DEFENSE_MALUS, "Lingering Virus");
        market.addTransientImmigrationModifier(this);
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
//        market.getHazard().unmodify(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        market.removeTransientImmigrationModifier(this);
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.POOR, 10f);
        incoming.getWeight().modifyFlat(getModId(), getThisImmigrationBonus(), Misc.ucFirst(condition.getName().toLowerCase()));
    }
	
    private float getThisImmigrationBonus() {
        return -3*market.getSize();
    }
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        tooltip.addPara(
                "%s defense rating.",
                10f,
                Misc.getHighlightColor(),
                "" + (int)((DEFENSE_MALUS-1)*100) + "%"
        );
        tooltip.addPara(
                "%s population growth (based on market size).",
                10f, 
                Misc.getHighlightColor(),
                "" + (int) getThisImmigrationBonus()
        );
    }
}
