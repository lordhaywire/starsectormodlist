package assortment_of_things.campaign.secondInCommand.abyssal

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.createDefaultShipAI
import org.magiclib.kotlin.isAutomated
import second_in_command.SCData
import second_in_command.specs.SCBaseSkillPlugin

class ForgottenPact : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "all automated ships"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("-10%% deployment points cost (maximum of 10)", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("+15%% top speed", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("+100 armor", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("Automated ships become even more fearless, no longer backing off even in the worst of situations", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI, variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {

        if (stats!!.isAutomated()) {
            val baseCost = stats.suppliesToRecover.baseValue
            val reduction = Math.min(10f, baseCost * 0.10f)

            stats.dynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, (-reduction).toFloat())

            stats.maxSpeed.modifyPercent(id, 15f)
            stats.armorBonus.modifyFlat(id, 100f)
        }

    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI, variant: ShipVariantAPI, id: String) {

        if (ship.isAutomated() && ship != Global.getCombatEngine().playerShip) {
            var config = ShipAIConfig()

            config.alwaysStrafeOffensively = true
            config.backingOffWhileNotVentingAllowed = false
            config.turnToFaceWithUndamagedArmor = false
            config.burnDriveIgnoreEnemies = true

            var carrier = variant.isCarrier && !ship.variant.isCombat
            if (carrier) {
                config.personalityOverride = Personalities.AGGRESSIVE
                config.backingOffWhileNotVentingAllowed = true
            } else {
                config.personalityOverride = Personalities.RECKLESS
            }

            ship.createDefaultShipAI(config)
        }
    }

    override fun advanceInCombat(data: SCData?, ship: ShipAPI?, amount: Float?) {

        if (ship!!.isAutomated()) {
            ship.aiFlags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF, 999f)
            ship.aiFlags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF_EVEN_WHILE_VENTING, 999f)
        }
    }



    override fun advance(data: SCData, amunt: Float?) {

    }

    override fun onActivation(data: SCData) {

    }

    override fun onDeactivation(data: SCData) {

    }

}