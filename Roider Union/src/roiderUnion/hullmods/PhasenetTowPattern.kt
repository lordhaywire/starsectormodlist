package roiderUnion.hullmods

import com.fs.starfarer.api.campaign.BuffManagerAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.hullmods.DriveFieldStabilizer
import com.fs.starfarer.api.loading.HullModSpecAPI
import roiderUnion.helpers.ExternalStrings
import roiderUnion.helpers.ExternalStrings.replaceNumberToken
import roiderUnion.helpers.Memory
import roiderUnion.ids.hullmods.RoiderHullmods
import roiderUnion.helpers.Helper
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Author: SafariJohn
 */
class PhasenetTowPattern : BaseHullMod() {
    companion object {
        const val HULLMOD_ID: String = RoiderHullmods.PHASENET_TOW_PATTERN
        fun getMaxBonusForSize(size: HullSize?): Int {
            return when (size) {
                HullSize.CAPITAL_SHIP -> 1
                HullSize.CRUISER -> 1
                HullSize.DESTROYER -> 2
                else -> 4
            }
        }

        /**
         * One instance of the buff object per ship with a Phasenet.
         */
        const val ROIDER_PHASENET_TOW_KEY = "Roider_PhasenetTow_PersistentBuffs"
    }

    override fun init(spec: HullModSpecAPI?) {}
    class PhasenetTowPatternBuff(
        private val id: String
    ) : BuffManagerAPI.Buff {
        private var bonus = 1
        var frames = 0
        fun setBonus(bonus: Int) {
            this.bonus = bonus
        }

        override fun getId(): String {
            return id
        }

        override fun isExpired(): Boolean {
            return frames >= 2
        }

        override fun apply(member: FleetMemberAPI?) {
            member?.stats?.maxBurnLevel?.modifyFlat(id, bonus.toFloat())
        }

        override fun advance(days: Float) {
            frames++
        }
    }

    override fun advanceInCampaign(member: FleetMemberAPI?, amount: Float) {
        if (Helper.anyNull(member)) return
        if (member?.fleetData?.membersListCopy == null) return
        if (member.fleetData?.fleet == null) return

        if (!member.fleetData.fleet.isPlayerFleet) {
            member.stats.dynamic.getMod(Stats.FLEET_BURN_BONUS)
                .modifyFlat(HULLMOD_ID, DriveFieldStabilizer.BURN_BONUS)
            return
        }
        if (!isTowShip(member)) {
            cleanUpPhasenetTowPatternBuffBy(member)
            return
        }
        if (!member.canBeDeployedForCombat()) {
            cleanUpPhasenetTowPatternBuffBy(member)
            return
        }

        val all: MutableList<FleetMemberAPI> = member.fleetData.membersListCopy

        // Get tow ships
        val towShips = mutableListOf<FleetMemberAPI>()
        for (curr in all) {
            if (isTowShip(curr)) {
                if (!curr.canBeDeployedForCombat()) continue
                towShips.add(curr)
            }
        }

        // If there are no phasenets working, clean up
        if (towShips.isEmpty()) {
            cleanUpPhasenetTowPatternBuffBy(member)
            return
        }

        // Sort tow ships fastest to slowest
        towShips.sortWith { o1, o2 ->
            (getMaxBurnWithoutPhasenet(o2) - getMaxBurnWithoutPhasenet(o1)).roundToInt()
        }

        // Sort all ships fastest to slowest
        all.sortWith { o1, o2 ->
            (getMaxBurnWithoutPhasenet(o2) - getMaxBurnWithoutPhasenet(o1)).roundToInt()
        }

        // Assign each tow ship to another ship
        val buff = getPhasenetTowPatternBuffBy(member, true)!!
        val towed = mutableMapOf<FleetMemberAPI, FleetMemberAPI>()
        var towTarget: FleetMemberAPI? = null
        for (towShip in towShips) {

            // Apply towing buff to tow ship if it is being towed
            for (b in towShip.buffManager.buffs) {
                if (b.id.startsWith(HULLMOD_ID)) {
                    b.apply(towShip)
                    break
                }
            }
            val cutoffSpeed: Int = towShip.stats.maxBurnLevel.modifiedInt
            val index = all.indexOf(towShip)
            all.remove(towShip)
            val slowest: FleetMemberAPI? = getSlowest(all, cutoffSpeed, towed)
            if (index < all.size) all.add(index, towShip) else all.add(towShip)
            if (slowest == null) continue
            towed[towShip] = slowest
            if (towShip === member) {
                towTarget = slowest
                val bonus = min(
                    getMaxBonusForSize(slowest.hullSpec.hullSize),
                    (cutoffSpeed - getMaxBurnWithoutPhasenet(slowest)).toInt()
                )
                buff.setBonus(bonus)
                val existing = slowest.buffManager.getBuff(buff.id)
                if (existing === buff) {
                    buff.frames = 0
                } else {
                    buff.frames = 0
                    slowest.buffManager.addBuff(buff)
                }
                break
            }
        }
        for (curr in all) {
            if (curr !== towTarget) {
                if (curr.buffManager.getBuff(buff.id) != null) {
                    curr.buffManager.removeBuff(buff.id)
                    curr.variant.removePermaMod(RoiderHullmods.PHASENET_TOW)
                }
            }
        }
    }

    private fun getSlowest(
        all: List<FleetMemberAPI>,
        speedCutoff: Int,
        towed: Map<FleetMemberAPI, FleetMemberAPI>
    ): FleetMemberAPI? {
        var slowest: FleetMemberAPI? = null
        var minLevel = Float.MAX_VALUE
        for (curr in all) {
            if (!isSuitable(curr)) continue
            if (towed.containsValue(curr)) continue
            val baseBurn = getMaxBurnWithoutPhasenet(curr)
            if (baseBurn >= speedCutoff) continue
            var boostedBurn = baseBurn + getMaxBonusForSize(curr.hullSpec.hullSize)
            if (boostedBurn > speedCutoff) boostedBurn = speedCutoff.toFloat()
            if (boostedBurn == minLevel) {
                if (boostedBurn == speedCutoff.toFloat()) {
                    if (isTowShip(curr) && !isTowShip(slowest)) {
                        slowest = curr
                    }
                } else {
                    if (isSmaller(curr, slowest)) {
                        slowest = curr
                    } else if (isTowShip(curr) && !isTowShip(slowest)) {
                        slowest = curr
                    }
                }
            } else if (boostedBurn < minLevel) {
                minLevel = boostedBurn
                slowest = curr
            }
        }
        return slowest
    }

    private fun isTowShip(member: FleetMemberAPI?): Boolean {
        return member?.variant?.hullMods?.contains(HULLMOD_ID) ?: false
    }

    private fun getMaxBurnWithoutPhasenet(member: FleetMemberAPI): Float {
        val burn: MutableStat = member.stats.maxBurnLevel ?: return 0f
        var sub = 0f
        for (mod in burn.flatMods.values) {
            if (mod.getSource().startsWith(HULLMOD_ID)) sub = mod.value; break
        }
        return max(0f, burn.modifiedValue - sub)
    }

    private fun isSuitable(member: FleetMemberAPI): Boolean {
        return !member.isFighterWing
    }

    private fun isSmaller(m1: FleetMemberAPI, m2: FleetMemberAPI?): Boolean {
        var m1Size = 5
        var m2Size = 5
        when (m1.hullSpec.hullSize) {
            HullSize.FRIGATE -> {
                m1Size--
                m1Size--
                m1Size--
                m1Size--
            }

            HullSize.DESTROYER -> {
                m1Size--
                m1Size--
                m1Size--
            }

            HullSize.CRUISER -> {
                m1Size--
                m1Size--
            }

            HullSize.CAPITAL_SHIP -> m1Size--
            else -> {}
        }
        when (m2?.hullSpec?.hullSize) {
            HullSize.FRIGATE -> {
                m2Size--
                m2Size--
                m2Size--
                m2Size--
            }

            HullSize.DESTROYER -> {
                m2Size--
                m2Size--
                m2Size--
            }

            HullSize.CRUISER -> {
                m2Size--
                m2Size--
            }

            HullSize.CAPITAL_SHIP -> m2Size--
            else -> {}
        }
        return m1Size < m2Size
    }

    private fun cleanUpPhasenetTowPatternBuffBy(member: FleetMemberAPI) {
        if (member.fleetData?.membersListCopy == null) return
        val buff = getPhasenetTowPatternBuffBy(member, false)
        if (buff != null) {
            for (curr in member.fleetData.membersListCopy) {
                curr.buffManager.removeBuff(buff.id)
            }
        }
    }

    private fun getPhasenetTowPatternBuffBy(
        member: FleetMemberAPI,
        createIfMissing: Boolean
    ): PhasenetTowPatternBuff? {
        val buffs = Memory.get(
            ROIDER_PHASENET_TOW_KEY,
            { it is MutableMap<*,*> },
            { mutableMapOf<FleetMemberAPI, PhasenetTowPattern>() }
        ) as MutableMap<FleetMemberAPI, PhasenetTowPatternBuff>

        var buff = buffs[member]
        if (buff == null && createIfMissing) {
            val id = HULLMOD_ID + "_" + member.id
            buff = PhasenetTowPatternBuff(id)
            buffs[member] = buff
        }
        return buff
    }

    override fun getDescriptionParam(index: Int, hullSize: HullSize): String {
        if (index == 0) return ExternalStrings.NUMBER_PLUS.replaceNumberToken(getMaxBonusForSize(HullSize.FRIGATE))
        return if (index == 1) ExternalStrings.NUMBER_PLUS.replaceNumberToken(getMaxBonusForSize(HullSize.DESTROYER))
        else ExternalStrings.NUMBER_PLUS.replaceNumberToken(getMaxBonusForSize(HullSize.CAPITAL_SHIP))
    }
}