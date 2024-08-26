package roiderUnion.combat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
import roiderUnion.hullmods.MIDAS
import roiderUnion.ids.hullmods.RoiderHullmods
import roiderUnion.ids.ShipsAndWings
import java.util.WeakHashMap
import kotlin.math.max

class TrackerSpeedBoost : EveryFrameCombatPlugin {
    companion object {
        const val SPEED_BOOST = 20f
        const val MAX_SPEED = 220f
        private const val MAX_LOOP_FRAMES = 150
    }
    private val engine: CombatEngineAPI
        get() = Global.getCombatEngine()
    private var index = 0

    private val trackers = WeakHashMap<ShipAPI, ShipAPI>()


    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if (engine.isPaused || engine.ships.isEmpty()) return

        val indicesPerIter = max(1, engine.ships.size / MAX_LOOP_FRAMES)
        for (i in indicesPerIter downTo 0) collectAndBoost()
    }

    private fun collectAndBoost() {
        index++
        if (index >= engine.ships.size) {
            index = 0
            cleanupDeadTrackers()
        }

        val ship = engine.ships[index]

        if (ship.isFighter) {
            val speedBoost = SPEED_BOOST.coerceAtMost(MAX_SPEED - (ship.mutableStats?.maxSpeed?.baseValue ?: 0f))
            if (hasTrackerCore(ship.variant) && ship.isAlive && MIDAS.hasMIDASStatic(ship.wing?.sourceShip?.variant)) {
                trackers[ship] = ship.wing?.sourceShip ?: return
            } else if (trackers.values.contains(ship.wing?.sourceShip) && speedBoost > 0) {
                ship.mutableStats?.maxSpeed?.modifyFlat(ShipsAndWings.TRACKER_WING, speedBoost)
            } else {
                ship.mutableStats?.maxSpeed?.unmodifyFlat(ShipsAndWings.TRACKER_WING)
            }
        }
    }

    private fun cleanupDeadTrackers() {
        val temp = HashMap(trackers)
        for (ship in temp.keys) {
            if (!ship.isAlive) trackers.remove(ship)
        }
    }

    private fun hasTrackerCore(variant: ShipVariantAPI?): Boolean {
        return variant?.hasHullMod(RoiderHullmods.TRACKER_CORE) == true
    }

    override fun init(engine: CombatEngineAPI?) {}
    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {}
    override fun renderInWorldCoords(viewport: ViewportAPI?) {}
    override fun renderInUICoords(viewport: ViewportAPI?) {}
}