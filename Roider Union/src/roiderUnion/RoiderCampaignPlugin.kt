package roiderUnion

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import roiderUnion.nomads.NomadJPWarningPlugin
import roiderUnion.nomads.NomadsHelper

class RoiderCampaignPlugin : BaseCampaignPlugin() {

    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken?): PluginPick<InteractionDialogPlugin>? {
        if (interactionTarget is JumpPointAPI && NomadsHelper.isNomadSystem(interactionTarget, true)) {
            return PluginPick(NomadJPWarningPlugin(), CampaignPlugin.PickPriority.MOD_SET)
        }

        return super.pickInteractionDialogPlugin(interactionTarget)
    }
}