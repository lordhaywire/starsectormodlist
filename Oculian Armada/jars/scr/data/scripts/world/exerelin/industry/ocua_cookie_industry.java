package data.scripts.world.exerelin.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import data.scripts.campaign.econ.OCUA_industries;
import exerelin.campaign.econ.EconomyInfoHelper;
import exerelin.world.ExerelinProcGen;
import exerelin.world.ExerelinProcGen.ProcGenEntity;
import exerelin.world.industry.HeavyIndustry;
import exerelin.world.industry.IndustryClassGen;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ocua_cookie_industry extends IndustryClassGen {

	public static final Set<String> HEAVY_INDUSTRY = new HashSet<>(Arrays.asList(Industries.HEAVYINDUSTRY, Industries.ORBITALWORKS, "ms_modularFac", "ms_massIndustry", "xlu_battle_yards"));

	public ocua_cookie_industry() {
		super(OCUA_industries.OCUA_FOOD_INDUSTRY);
	}
	
	@Override
	public float getWeight(ProcGenEntity entity) {
		boolean newGame = Global.getSector().isInNewGameAdvance();
                //will only be preferred after building necessities
		if (newGame && entity.type == ExerelinProcGen.EntityType.STATION && StarSystemGenerator.random.nextBoolean())
			return -1;
                
		MarketAPI market = entity.market;
                
                // 
		float weight = 0;
				
		// bad for high hazard worlds
		weight += (200 - market.getHazardValue()) * 2;
		
		// will avoid farming as much as possible
		if (market.hasIndustry(Industries.FARMING))
			weight -= 90000;
		// or food conditions
		if (market.hasCondition(Conditions.FARMLAND_POOR))
			weight -= 250;
                else if (market.hasCondition(Conditions.FARMLAND_ADEQUATE))
			weight -= 10000;
                else if (market.hasCondition(Conditions.FARMLAND_RICH))
			weight -= 50000;
                else if (market.hasCondition(Conditions.FARMLAND_BOUNTIFUL))
			weight -= 100000;
                else weight += 10;
                
		// prefer to not be on same planet as heavy industry
		if (HeavyIndustry.hasHeavyIndustry(market))
			weight -= 200;
		
		return weight;
	}
        
	@Override
	public boolean canApply(ProcGenEntity entity) {
		MarketAPI market = entity.market;
		if (!entity.market.getFactionId().equals("ocua"))
			return false;
                
                // should not have if colony already has a farming industry
		if ((market.hasIndustry(Industries.FARMING)))
			return false;
		
		if (!Global.getSector().isInNewGameAdvance()) {
			int minSize = 4;
			if (EconomyInfoHelper.getInstance().hasHeavyIndustry(market.getFactionId())) {
				minSize = 5;
			}
			if (market.getSize() < minSize)
				return false;
		}
		
		return super.canApply(entity);
	}
}
