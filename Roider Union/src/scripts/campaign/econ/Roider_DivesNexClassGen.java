package scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import exerelin.world.ExerelinProcGen;
import exerelin.world.NexMarketBuilder;
import exerelin.world.industry.IndustryClassGen;
import ids.Roider_Ids.Roider_Industries;

/**
 * Author: SafariJohn
 */
public class Roider_DivesNexClassGen extends IndustryClassGen {

	public Roider_DivesNexClassGen() {
		super(Roider_Industries.DIVES, Roider_Industries.UNION_HQ);
	}

	@Override
	public boolean canApply(ExerelinProcGen.ProcGenEntity entity) {
		if (entity.market.getIndustries().size() >= 12) return false;
		return super.canApply(entity);
	}

	@Override
	public void apply(ExerelinProcGen.ProcGenEntity entity, boolean instant) {
		// If already have dives, upgrade to Union HQ
		if (entity.market.hasIndustry(Roider_Industries.DIVES)) {
			Industry ind = entity.market.getIndustry(Roider_Industries.DIVES);
			ind.startUpgrading();
			if (instant) ind.finishBuildingOrUpgrading();
			return;
		}
		// build Union HQ directly
		NexMarketBuilder.addIndustry(entity.market, Roider_Industries.UNION_HQ, this.id, instant);
		entity.numProductiveIndustries += 1;
	}

    @Override
    public boolean canAutogen() {
        return false;
    }

}
