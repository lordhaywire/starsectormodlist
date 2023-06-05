package data.scripts.world.exerelin;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import data.campaign.econ.XLU_industries;
import exerelin.world.ExerelinProcGen.ProcGenEntity;
import exerelin.world.NexMarketBuilder;
import exerelin.world.industry.IndustryClassGen;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class xlu_battle_yards extends IndustryClassGen {

	public static final Set<String> HEAVY_INDUSTRY = new HashSet<>(Arrays.asList(Industries.HEAVYINDUSTRY, Industries.ORBITALWORKS, "ms_modularFac", "ms_massIndustry", "xlu_battle_yards"));

	public xlu_battle_yards() {
		super("xlu_battle_yards");
	}
	
	@Override
	public float getWeight(ProcGenEntity entity) {
		MarketAPI market = entity.market;
                
		float weight = (5 + market.getSize() * 5) * 2;
				
		// bad for high hazard worlds
		weight += (150 - market.getHazardValue()) * 2;
		
		// prefer not to be on same planet as fuel production
		if (market.hasIndustry(Industries.FUELPROD))
			weight -= 400;
		// or light industry
		if (market.hasIndustry(Industries.LIGHTINDUSTRY))
			weight -= 250;
		
		return weight;
	}
        
	@Override
	public boolean canApply(ProcGenEntity entity) {
		MarketAPI market = entity.market;
		if ((market.hasIndustry(Industries.ORBITALWORKS) || market.hasIndustry("ms_massIndustry") || market.hasIndustry("xlu_battle_yards")) && 
                        !entity.market.getFactionId().equals("xlu"))
			return false;
		
		return super.canApply(entity);
	}
	
	@Override
	public void apply(ProcGenEntity entity, boolean instant) {
		MarketAPI market = entity.market;
		String id = XLU_industries.XLU_YARDS;
		if (market.getFactionId().equals("xlu"))
			id = "xlu_battle_yards";
			
		NexMarketBuilder.addIndustry(market, id, instant);
		
		entity.numProductiveIndustries += 1;
	}
	
	public static boolean hasHeavyIndustry(MarketAPI market)
	{
		for (String ind : HEAVY_INDUSTRY)
		{
			if (market.hasIndustry(ind))
				return true;
		}
		return false;
	}
}
