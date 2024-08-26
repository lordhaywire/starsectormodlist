package roiderUnion.nomads.bases

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.thoughtworks.xstream.XStream
import org.magiclib.kotlin.getSourceMarket
import org.magiclib.kotlin.isPatrol
import roiderUnion.helpers.Memory
import roiderUnion.ids.Aliases
import roiderUnion.ids.MemoryKeys
import roiderUnion.nomads.NomadsHelper

class NomadWarningTaggingScript(private val base: SectorEntityToken) : EveryFrameScript {
    companion object {
        fun alias(x: XStream) {
            val jClass = NomadWarningTaggingScript::class.java
            x.alias(Aliases.NWARNTAG, jClass)
            x.aliasAttribute(jClass, "locIndex", "l")
            x.aliasAttribute(jClass, "fleetIndex", "f")
        }
    }

    private var locIndex = 0
    private var fleetIndex = 0

    override fun advance(amount: Float) {
        val baseLocs = NomadsHelper.bases.map { it.starSystem }
        if (locIndex >= (baseLocs.size)) locIndex = 0
        fleetIndex++
        if (fleetIndex >= (baseLocs[locIndex]?.fleets?.size ?: 0)) {
            fleetIndex = 0
            locIndex
        }

        checkAndMarkPatrols(baseLocs)
    }

    private fun checkAndMarkPatrols(baseLocs: List<StarSystemAPI>) {
        val fleet = baseLocs[locIndex].fleets?.get(fleetIndex) ?: return
        if (Memory.isFlag(MemoryKeys.NOMAD_WARNING, fleet)) return
        if (fleet.faction.custom.has(Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE)
            && fleet.faction.custom.getBoolean(Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE)) return

        if (fleet.isPatrol() && fleet.getSourceMarket() == base.market) {
            Memory.set(MemoryKeys.NOMAD_WARNING, true, fleet)
        }
    }

    override fun isDone(): Boolean = !base.isDiscoverable
    override fun runWhilePaused(): Boolean = false
}