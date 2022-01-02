package scripts.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import ids.Roider_Ids.Roider_Factions;
import ids.Roider_MemFlags;
import java.util.List;
import java.util.Map;

/**
 * Author: SafariJohn
 */
public class Roider_FreeFringeWarning extends BaseCommandPlugin {
	private InteractionDialogAPI dialog;
	private SectorEntityToken entity;
	private Map<String, MemoryAPI> memoryMap;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
		this.memoryMap = memoryMap;

		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;

		entity = dialog.getInteractionTarget();

        switch (command) {
            case "isFringeHQSystem": return isFringeUnionHQSystem();
        }

        return true;
    }

    private boolean isFringeUnionHQSystem() {
        String sourceId = (String) entity.getMemoryWithoutUpdate().get(MemFlags.MEMORY_KEY_SOURCE_MARKET);
        MarketAPI source = Global.getSector().getEconomy().getMarket(sourceId);

        if (source != null) {
            MemoryAPI sourceMem = memoryMap.get(MemKeys.SOURCE_MARKET);

            return sourceMem.getBoolean(Roider_MemFlags.FRINGE_HQ)
                        || (source.getFactionId().equals(Roider_Factions.ROIDER_UNION)
                            && source.isHidden());
        }

        return false;
    }

}
