/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class US_shrooms extends BaseHazardCondition{

    private final float STABILITY_MALUS=-3;
    private final int DRUG_BONUS=1;
    
    @Override
    public void apply(String id) {
        
        //reduce drug demand to 0
        Industry industry = market.getIndustry(Industries.POPULATION);
        if(industry!=null){
            industry.getDemand(Commodities.DRUGS).getQuantity().modifyMult(id + "_0", 0);
        }
        
        industry = market.getIndustry(Industries.MINING);
        if(industry!=null){
            industry.getDemand(Commodities.DRUGS).getQuantity().modifyMult(id + "_0", 0);
        }
        
        //raise drug production by one
        industry = market.getIndustry(Industries.LIGHTINDUSTRY);
        if(industry!=null){
            if (industry.isFunctional()) {
               industry.supply(id + "_0", Commodities.DRUGS, DRUG_BONUS, "Xeno-fungus");
            } else {
               industry.getSupply(Commodities.DRUGS).getQuantity().unmodifyFlat(id + "_0");
            }
        }
        
        //stability  hit
        market.getStability().modifyFlat(id, STABILITY_MALUS, "Xeno-fungus");
    }
    
    @Override
    public void unapply(String id) {
        market.getStability().unmodify(id);
    }
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        
        tooltip.addPara(
                "Local drug demand %s.",
                10f, 
                Misc.getHighlightColor(),
                "nullified"
        );
        
        tooltip.addPara(
                "%s recreational drug production.",
                10f, 
                Misc.getHighlightColor(),
                "+" + DRUG_BONUS
        );
        
        tooltip.addPara(
                "%s stability.",
                10f, 
                Misc.getHighlightColor(),
                "" + (int)STABILITY_MALUS
        );
    }
}
