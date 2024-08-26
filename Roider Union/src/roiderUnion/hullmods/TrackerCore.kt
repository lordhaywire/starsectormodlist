package roiderUnion.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.impl.campaign.ids.Stats
import roiderUnion.helpers.Helper

/**
 * Author: SafariJohn
 */
class TrackerCore : BaseHullMod() {
    override fun applyEffectsBeforeShipCreation(hullSize: HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        if (Helper.anyNull(stats, id)) return

        stats?.autofireAimAccuracy?.modifyFlat(id, 1f)
        stats?.eccmChance?.modifyFlat(id, 1f)
        stats?.dynamic?.getMod(Stats.PD_IGNORES_FLARES)?.modifyFlat(id, 1f)
    }
}