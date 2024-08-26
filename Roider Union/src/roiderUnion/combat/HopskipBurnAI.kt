package roiderUnion.combat

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.plugins.ShipSystemStatsScriptAdvanced
import com.fs.starfarer.api.util.IntervalUtil
import org.lwjgl.util.vector.Vector2f
import roiderUnion.helpers.Helper

class HopskipBurnAI : ShipSystemAIScript {
    private lateinit var ship: ShipAPI
    private lateinit var system: ShipSystemAPI
    private lateinit var script: ShipSystemStatsScriptAdvanced
    private var interval = IntervalUtil(0.08f, 0.12f)
    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        if (Helper.anyNull(ship, system)) return

        this.ship = ship!!
        this.system = system!!
        script = system.specAPI.statsScript as ShipSystemStatsScriptAdvanced
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (Helper.anyNull(ship, system, script)) return

        interval.advance(amount)
        if (interval.intervalElapsed()) {
            if (!ship.isAlive) return
            if (ship.allWeapons.any { it.ammo > 0 }) return
            if (system.state == ShipSystemAPI.SystemState.IDLE
                && ship.owner != target?.owner
                && script.isUsable(system, ship)
            ) {
                ship.useSystem()
            }
        }
    }
}