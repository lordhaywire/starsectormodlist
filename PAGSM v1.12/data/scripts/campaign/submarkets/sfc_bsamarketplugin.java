package data.scripts.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.submarkets.BlackMarketPlugin;

//finish this later

public class sfc_bsamarketplugin extends BlackMarketPlugin {

    //private boolean sfcbsamarketaccess = Global.getSector().getMemoryWithoutUpdate().getBoolean("$sfcunlockedmarket");

    public boolean isEnabled(CoreUIAPI ui) {
        return Global.getSector().getMemoryWithoutUpdate().getBoolean("$sfcunlockedmarket");
    }
    public boolean isHidden() {
        return !Global.getSector().getMemoryWithoutUpdate().getBoolean("$sfcunlockedmarket");
    }


    @Override
    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return "Purchases only!";
        }
        return "Can only make purchases on the " + submarket.getNameOneLine().toLowerCase() + " here";
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return "Purchases only!";
        }
        return "This market does not accept sales.";
    }

    @Override
    public float getTariff() {
        return 1f;
    }

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return true;
        }
        if (market.hasCondition(Conditions.FREE_PORT)) {
            return false;
        }
        return submarket.getFaction().isIllegal(commodityId);
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return true;
        }
        if (!stack.isCommodityStack()) {
            return false;
        }
        return isIllegalOnSubmarket((String) stack.getData(), action);
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }

    @Override
    public void updateCargoPrePlayerInteraction() {
        sinceLastCargoUpdate = 0f;

        if (okToUpdateShipsAndWeapons()) {
            sinceSWUpdate = 0f;

            pruneWeapons(0f);

            int weapons = 20 + (market.getSize() * 10);
            int fighters = 5 + (market.getSize() * 5);

            addWeapons(weapons, weapons + 2, 4, submarket.getFaction().getId());
            addWeapons(weapons, weapons + 1, 4, submarket.getFaction().getId());
            addFighters(fighters, fighters + 2, 3, submarket.getFaction().getId());

            getCargo().getMothballedShips().clear();

            FactionDoctrineAPI doctrineOverride = submarket.getFaction().getDoctrine().clone();
            addShips(submarket.getFaction().getId(),
                    300f, // combat
                    0f, // freighter
                    0f, // tanker
                    0f, // transport
                    0f, // liner
                    0f, // utilityPts
                    1.5f, // qualityOverride
                    0f, // qualityMod
                    ShipPickMode.PRIORITY_THEN_ALL,
                    doctrineOverride
            );

            addHullMods(4, 4);

        }

        getCargo().sort();
    }


    @Override
    protected Object writeReplace() {
        if (okToUpdateShipsAndWeapons()) {
            pruneWeapons(0f);
            getCargo().getMothballedShips().clear();
        }
        return this;
    }
}