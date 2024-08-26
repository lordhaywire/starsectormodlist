package roiderUnion.retrofits.argos

import com.fs.starfarer.api.campaign.FactionAPI
import retroLib.RetrofitData
import retroLib.api.RetrofitFilter
import retroLib.impl.BaseRetrofitManager
import roiderUnion.helpers.Helper

class ArgosRetrofitManager(faction: FactionAPI, filter: RetrofitFilter) : BaseRetrofitManager(null, faction, filter) {
    override fun isTargetAllowed(targetId: String?): Boolean {
        val data = retrofits.firstOrNull { it.target == targetId } ?: return false
        if (retroLib.Helper.isFrameHull(data.targetSpec)) return true
        if (Helper.sector?.playerFaction?.knowsShip(targetId) == true) return true
        if (Helper.sector?.playerFaction?.knowsFighter(targetId) == true) return true
        return false
    }

    override fun isSourceAllowed(data: RetrofitData): Boolean {
        if (retroLib.Helper.isFrameHull(data.targetSpec)) return true
        if (Helper.sector?.playerFaction?.knowsShip(data.target) == true) return true
        if (Helper.sector?.playerFaction?.knowsFighter(data.target) == true) return true
        return false
    }
}