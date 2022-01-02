package data.scripts.bounty.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.scripts.bounty.MagicBountyCoordinator;
import data.scripts.util.MagicSettings;
import static data.scripts.util.MagicVariables.MAGICLIB_ID;

import java.util.List;
import java.util.Map;

/**
 * Returns true iff there are available bounties at the current market.
 *
 * @author Wisp
 */
public class ShouldShowMagicBountyBoard extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!MagicSettings.getBoolean(MAGICLIB_ID, "bounty_board_enabled")) return false;
        MarketAPI market = dialog.getInteractionTarget().getMarket();
        if (market == null) return false;

        if (Global.getSettings().isDevMode()) {
            return true;
        }

        return MagicBountyCoordinator.getInstance().shouldShowBountyBoardAt(market);
    }
}
