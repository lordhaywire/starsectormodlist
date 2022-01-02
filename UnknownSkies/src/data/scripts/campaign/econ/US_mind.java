/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class US_mind extends BaseHazardCondition implements MarketImmigrationModifier {
    
    private final int PRODUCTION_MALUS=-1; 
    private final float QUALITY_MALUS=-0.25f; 
    
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
        
        //reduced production
        for(Industry i : market.getIndustries()){
            for(MutableCommodityQuantity c : i.getAllSupply()){
                i.getSupply(c.getCommodityId()).getQuantity().modifyFlat(id, PRODUCTION_MALUS, "Parasitic Spore");
            }
//            i.getSupplyBonus().modifyFlat("Parasitic Spore", -1);
        }
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id, QUALITY_MALUS, "Parasitic Spore");
        
        //stability  buff
        market.getStability().modifyFlat(id, getStabilityFloor(), "Parasitic Spore");
        
        market.addTransientImmigrationModifier(this);
    }
    
    @Override
    public void unapply(String id) {
        market.getStability().unmodify(id);
        market.removeTransientImmigrationModifier(this);
    }
    
    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.PLAYER, 10f);
        incoming.getWeight().modifyFlat(getModId(), getThisImmigrationBonus(), Misc.ucFirst(condition.getName().toLowerCase()));
    }
    
    private float getThisImmigrationBonus() {
        return 5*market.getSize();
    }
    
    private float getStabilityFloor(){
        return Math.max(0, 5-market.getStabilityValue());
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
                "%s growth boost (based on market size).",
                10f, 
                Misc.getHighlightColor(),
                "+" + getThisImmigrationBonus()
        );
        
        tooltip.addPara(
                "%s minimal stability.",
                10f, 
                Misc.getHighlightColor(),
                "+5"
        );
        
        tooltip.addPara(
                "%s production from all industries.",
                10f, 
                Misc.getHighlightColor(),
                ""+PRODUCTION_MALUS
        );
    }
}
