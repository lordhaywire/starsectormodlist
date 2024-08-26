/**
 * Tells Starsector to pick the Realistic Combat battle creation plugin.
 */

package RealisticCombat.plugins;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;


public final class CampaignPlugin extends BaseCampaignPlugin {
    @Override
    public PluginPick<com.fs.starfarer.api.campaign.BattleCreationPlugin> pickBattleCreationPlugin(
            SectorEntityToken set
    ) {
        return new PluginPick<com.fs.starfarer.api.campaign.BattleCreationPlugin>(
                new BattleCreationPlugin(), PickPriority.MOD_SPECIFIC
        );
    }
}