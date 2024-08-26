package roiderUnion.retrofits

import com.fs.starfarer.api.campaign.econ.MarketAPI
import retroLib.RetrofitData
import retroLib.impl.BaseRetrofitFilter
import roiderUnion.helpers.Helper
import roiderUnion.ids.Fitters

class ShipworksFilter(market: MarketAPI?, adjuster: ShipworksAdjuster) : BaseRetrofitFilter(market, adjuster) {
    override fun allow(data: RetrofitData?): Boolean {
        if (data == null) return false
        if (!super.allow(data)) return false
        return data.tags.contains(Fitters.ALL)
    }

    override fun allowHidden(data: RetrofitData): Boolean {
        if (Helper.sector?.playerFaction?.knowsShip(data.target) == true
            && Helper.sector?.playerFaction?.knowsShip(data.source) == true) return true
        return super.allowHidden(data)
    }

}