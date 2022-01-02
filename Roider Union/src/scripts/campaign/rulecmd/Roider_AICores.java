package scripts.campaign.rulecmd;

import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.AICores;
import ids.Roider_Ids.Roider_Ranks;

/**
 * Author: SafariJohn
 */
public class Roider_AICores extends AICores {

	protected boolean personCanAcceptCores() {
		if (person == null || !buysAICores) return false;

		return Roider_Ranks.POST_BASE_COMMANDER.equals(person.getPostId());
	}

}
