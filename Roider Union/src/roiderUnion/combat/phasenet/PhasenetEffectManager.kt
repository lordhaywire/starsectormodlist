package roiderUnion.combat.phasenet

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import roiderUnion.helpers.ExternalStrings
import roiderUnion.helpers.Helper
import roiderUnion.helpers.Settings

/**
 * Author: SafariJohn
 */
class PhasenetEffectManager : BaseEveryFrameCombatPlugin() {
    companion object {
        const val KEY = "roider_phasenetTarget"
        const val EFFECT_KEY = "roider_phasenetEffect_"
        const val ONE_STEP = 5f

        const val TOKEN_NUM = "\$numPhasenets"
        const val TOKEN_STRENGTH = "\$pullStrength"

        fun isNetted(target: ShipAPI): Boolean {
            return target.customData.keys.contains(KEY)
        }
    }

    private val trueVelocities = mutableMapOf<String, Vector2f>()
    private val prevVelocities = mutableMapOf<String, Vector2f>()
    private val prevAccelerations = mutableMapOf<String, Vector2f>()
    private val baseMaxSpeeds = mutableMapOf<String, Float>()

    @Deprecated("Deprecated in Java")
    override fun init(engine: CombatEngineAPI) {
        trueVelocities.clear()
        prevVelocities.clear()
        prevAccelerations.clear()
        baseMaxSpeeds.clear()
    }

    override fun advance(amount: Float, events: List<InputEventAPI>) {
        if (Helper.combatEngine?.isCombatOver == true) return

        for (ship in Helper.combatEngine?.ships ?: listOf()) {
            if (ship == null) continue
            val targetVectors = getTargetVectors(ship)
            val targetVectorSum = calculateTargetVectorSum(targetVectors)
            if (targetVectorSum == null) {
                clearData(ship)
                continue
            }
            val baseMaxSpeed = getBaseMaxSpeed(ship)
            val targetVelocity = calculateTargetVelocity(ship, targetVectorSum, baseMaxSpeed)
            val phasenetAcceleration = calculateAcceleration(ship, targetVelocity)
            applyAcceleration(ship, phasenetAcceleration, baseMaxSpeed, targetVelocity.length())
            displayPlayerStatus(ship, targetVectorSum.length().toInt(), targetVectors.size)
        }
    }

    private fun displayPlayerStatus(ship: ShipAPI, pullStrength: Int, numPhasenets: Int) {
        if (ship === Helper.combatEngine?.playerShip) {
            val sprite = Settings.PHASENET_ICON_NAME
            if (numPhasenets > 1) {
                val text = ExternalStrings.PHASENET_PULL_MULTI
                    .replace(TOKEN_STRENGTH, pullStrength.toString())
                    .replace(TOKEN_NUM, numPhasenets.toString())
                Helper.combatEngine?.maintainStatusForPlayerShip(
                    KEY,
                    sprite,
                    ExternalStrings.PHASENET_TITLE,
                    text,
                    true
                )
            } else {
                val text = ExternalStrings.PHASENET_PULL_SINGLE
                    .replace(TOKEN_STRENGTH, pullStrength.toString())
                Helper.combatEngine?.maintainStatusForPlayerShip(
                    KEY,
                    sprite,
                    ExternalStrings.PHASENET_TITLE,
                    text,
                    true
                )
            }
        }
    }

    private fun calculateTargetVelocity(ship: ShipAPI, targetVectorSum: Vector2f, baseMaxSpeed: Float): Vector2f {
        val trueVelocity = calculateTrueVelocity(ship, VectorUtils.getFacing(targetVectorSum), baseMaxSpeed)
        return Vector2f.add(trueVelocity, targetVectorSum, null)
    }

    private fun calculateAcceleration(ship: ShipAPI, targetVelocity: Vector2f): Vector2f {
        val result: Vector2f = Vector2f.sub(targetVelocity, ship.velocity, null)
        prevAccelerations[ship.id] = result
        return result
    }

    private fun applyAcceleration(ship: ShipAPI, acceleration: Vector2f, baseMaxSpeed: Float, targetSpeed: Float) {
        ship.velocity.x += acceleration.x
        ship.velocity.y += acceleration.y
        if (ship.velocity.length() > baseMaxSpeed
            && targetSpeed > baseMaxSpeed
        ) {
            ship.mutableStats.maxSpeed.unmodify(KEY)
            baseMaxSpeeds[ship.id] = ship.maxSpeed
            ship.mutableStats.maxSpeed.modifyMult(KEY, targetSpeed / ship.maxSpeed)
        } else {
            ship.mutableStats.maxSpeed.unmodify(KEY)
            baseMaxSpeeds[ship.id] = ship.maxSpeed
        }
    }

    private fun calculateTrueVelocity(ship: ShipAPI, targetVectorFacing: Float, baseMaxSpeed: Float): Vector2f {
        var result: Vector2f? = trueVelocities[ship.id]
        if (result == null) {
            result = Vector2f(ship.velocity)
            trueVelocities[ship.id] = result
        }

        val acceleration = getAcceleration(ship)
        var prevPhasenetAccel: Vector2f? = prevAccelerations[ship.id]
        if (prevPhasenetAccel == null) prevPhasenetAccel = Vector2f()
        result.x += acceleration.x - prevPhasenetAccel.x
        result.y += acceleration.y - prevPhasenetAccel.y

        val notAccelerating = Vector2f.sub(acceleration, prevPhasenetAccel, null).length() == 0f && result.length() < baseMaxSpeed
        if (notAccelerating) {
            val oneStep = Misc.getUnitVectorAtDegreeAngle(targetVectorFacing)
            oneStep.scale(ONE_STEP)
            result = result.plus(oneStep)
        }

        val noControl = (ship.engineController.isFlamedOut || !ship.isAlive) && result.length() < baseMaxSpeed
        if (noControl) {
            val oneStep = Misc.getUnitVectorAtDegreeAngle(targetVectorFacing)
            oneStep.scale(ONE_STEP)
            result += oneStep
        }

        if (result.length() > baseMaxSpeed) {
            result.scale(baseMaxSpeed / result.length())
        }
        return result
    }

    private fun getBaseMaxSpeed(ship: ShipAPI): Float {
        var result = baseMaxSpeeds[ship.id]
        if (result == null) {
            result = ship.maxSpeed
            baseMaxSpeeds[ship.id] = result
        }
        if (ship.engineController.isFlamedOut || !ship.isAlive) {
            result = ship.hullSpec.engineSpec.maxSpeed
        }
        return result
    }

    private fun clearData(ship: ShipAPI) {
        ship.customData?.remove(KEY)
        trueVelocities.remove(ship.id)
        prevVelocities.remove(ship.id)
        prevAccelerations.remove(ship.id)
        baseMaxSpeeds.remove(ship.id)
        ship.mutableStats?.maxSpeed?.unmodify(KEY)
    }

    private fun getAcceleration(ship: ShipAPI): Vector2f {
        var prevVelocity: Vector2f? = prevVelocities[ship.id]
        if (prevVelocity == null) {
            prevVelocity = Vector2f(ship.velocity)
            prevVelocities[ship.id] = prevVelocity
        }
        prevVelocities[ship.id] = Vector2f(ship.velocity)
        return Vector2f.sub(ship.velocity, prevVelocity, null)
    }

    private fun calculateTargetVectorSum(targetVectors: Set<Vector2f>): Vector2f? {
        val result = Vector2f()
        if (targetVectors.isEmpty()) return null
        for (v in targetVectors) {
            result.x += v.x
            result.y += v.y
        }
        return result
    }

    private fun getTargetVectors(ship: ShipAPI): Set<Vector2f> {
        val customCopy: Map<String, Any> = ship.customData.toMap()
        if (!customCopy.containsKey(KEY)) return emptySet()

        val result = customCopy
            .filter { it.key.startsWith(EFFECT_KEY) && it.value is Vector2f }
            .map { it.value as Vector2f }
            .toSet()
        customCopy.keys.filter { it.startsWith(EFFECT_KEY) }.forEach { ship.removeCustomData(it) }
        return result
    }
}