package data.scripts.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.submarkets.BlackMarketPlugin;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;

public class UW_CabalMarketPlugin extends BlackMarketPlugin {

    private static final RepLevel MIN_STANDING = RepLevel.INHOSPITABLE;

    private boolean playerPaidToUnlock = false;
    private float sinceLastUnlock = 0f;

    @Override
    public void advance(float amount) {
        super.advance(amount);
        float days = Global.getSector().getClock().convertToDays(amount);
        sinceLastUnlock += days;
        if (sinceLastUnlock > 7f) {
            playerPaidToUnlock = false;
        }
    }

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
    }

    @Override
    public DialogOption[] getDialogOptions(CoreUIAPI ui) {
        if (canPlayerAffordUnlock()) {
            return new DialogOption[]{
                new DialogOption("Pay", new Script() {
                    @Override
                    public void run() {
                        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                        playerFleet.getCargo().getCredits().subtract(getUnlockCost());
                        playerPaidToUnlock = true;
                        sinceLastUnlock = 0f;
                    }
                }),
                new DialogOption("Never mind", null)
            };
        } else {
            return new DialogOption[]{
                new DialogOption("Never mind", null)
            };
        }
    }

    @Override
    public String getDialogText(CoreUIAPI ui) {
        if (canPlayerAffordUnlock()) {
            return "\"We might consider letting you in today, so long as you pay the "
                    + Misc.getWithDGS(getUnlockCost()) + "-credit fee.\"";
        } else {
            return "\"You can't even pay the " + Misc.getWithDGS(getUnlockCost()) + "-credit fee? Get lost.\"";
        }
    }

    @Override
    public Highlights getDialogTextHighlights(CoreUIAPI ui) {
        Highlights h = new Highlights();
        h.setText("" + getUnlockCost());
        if (canPlayerAffordUnlock()) {
            h.setColors(Misc.getHighlightColor());
        } else {
            h.setColors(Misc.getNegativeHighlightColor());
        }
        return h;
    }

    @Override
    public OnClickAction getOnClickAction(CoreUIAPI ui) {
        if (playerPaidToUnlock || submarket.getFaction().getRelToPlayer().isAtWorst(RepLevel.FRIENDLY)) {
            return OnClickAction.OPEN_SUBMARKET;
        }
        return OnClickAction.SHOW_TEXT_DIALOG;
    }

    @Override
    public float getTariff() {
        float fudge;
        switch (submarket.getFaction().getRelToPlayer().getLevel()) {
            default:
            case VENGEFUL:
            case HOSTILE:
            case INHOSPITABLE:
                fudge = 2f;
                break;
            case SUSPICIOUS:
                fudge = 1.5f;
                break;
            case NEUTRAL:
                fudge = 1f;
                break;
            case FAVORABLE:
                fudge = 0.75f;
                break;
            case WELCOMING:
                fudge = 0.5f;
                break;
            case FRIENDLY:
            case COOPERATIVE:
                fudge = 0f;
                break;
        }

        MonthlyReport report = SharedData.getData().getCurrentReport();
        report.computeTotals();
        float profit = Math.max(0f, report.getRoot().totalIncome - report.getRoot().totalUpkeep);
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        return Math.round((float) Math.sqrt(Math.max(playerFleet.getCargo().getCredits().get() + profit, 150000f) / 150000f) * 10f * fudge) / 100f;
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui) {
        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        if (!level.isAtWorst(MIN_STANDING)) {
            return "Requires: " + submarket.getFaction().getDisplayName() + " - "
                    + MIN_STANDING.getDisplayName().toLowerCase();
        }
        if (Global.getSector().getPlayerFleet().isTransponderOn()) {
            return "Requires: a clandestine approach (transponder off)";
        }
        return super.getTooltipAppendix(ui);
    }

    @Override
    public boolean isEnabled(CoreUIAPI ui) {
        if (Global.getSector().getPlayerFleet().isTransponderOn()) {
            return false;
        }

        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        return level.isAtWorst(MIN_STANDING);
    }

    @Override
    public void updateCargoPrePlayerInteraction() {
        sinceLastCargoUpdate = 0f;

        if (okToUpdateShipsAndWeapons()) {
            sinceSWUpdate = 0f;

            pruneWeapons(0f);

            int weapons = 20 + (market.getSize() * 3);
            int fighters = 6 + (market.getSize() * 2);

            addWeapons(weapons, weapons + 2, 4, submarket.getFaction().getId());
            addFighters(fighters, fighters + 2, 3, submarket.getFaction().getId());

            getCargo().getMothballedShips().clear();

            FactionDoctrineAPI doctrineOverride = submarket.getFaction().getDoctrine().clone();
            doctrineOverride.setCombatFreighterProbability(1f);
            doctrineOverride.setShipSize(4);
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

            addCabalBPs();
        }

        getCargo().sort();
    }

    private void addCabalBPs() {
        CargoAPI ourCargo = getCargo();
        for (CargoStackAPI stack : ourCargo.getStacksCopy()) {
            if ((stack.getSpecialItemSpecIfSpecial() != null)
                    && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("uw_cabal_package")) {
                ourCargo.removeStack(stack);
            }
        }

        if (!Global.getSector().getPlayerFaction().knowsShip("uw_odyssey_cabal")) {
            ourCargo.addItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData("uw_cabal_package", null), 1);
        }
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        if (Global.getSector().getPlayerFleet().isTransponderOn()) {
            return true;
        }
        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        if (!level.isAtWorst(MIN_STANDING)) {
            return true;
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("uw_cabal_package")) {
            return !submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER)).isAtWorst(RepLevel.COOPERATIVE);
        }

        if (!playerPaidToUnlock && submarket.getFaction().getRelToPlayer().isAtBest(RepLevel.WELCOMING)) {
            return true;
        }

        return super.isIllegalOnSubmarket(stack, action);
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        if (Global.getSector().getPlayerFleet().isTransponderOn()) {
            return true;
        }
        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        if (!level.isAtWorst(MIN_STANDING)) {
            return true;
        }
        if (action == TransferAction.PLAYER_BUY) {
            MonthlyReport report = SharedData.getData().getCurrentReport();
            report.computeTotals();
            float profit = Math.max(0f, report.getRoot().totalIncome - report.getRoot().totalUpkeep);
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            float assets = playerFleet.getCargo().getCredits().get() + profit;

            if (member.isFrigate()) {
                return assets < 100000f;
            } else if (member.isDestroyer()) {
                return assets < 200000f;
            } else if (member.isCruiser()) {
                return assets < 400000f;
            } else if (member.isCapital()) {
                return assets < 800000f;
            }
        }

        return false;
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        if (Global.getSector().getPlayerFleet().isTransponderOn()) {
            return true;
        }
        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        if (!level.isAtWorst(MIN_STANDING)) {
            return true;
        }
        return super.isIllegalOnSubmarket(commodityId, action);
    }

    @Override
    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("uw_cabal_package")) {
            return "Req: "
                    + submarket.getFaction().getDisplayName() + " - " + RepLevel.COOPERATIVE.getDisplayName().toLowerCase();
        }

        if (!playerPaidToUnlock && submarket.getFaction().getRelToPlayer().isAtBest(RepLevel.WELCOMING)) {
            return "Requires: paid access";
        }

        return super.getIllegalTransferText(stack, action);
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
        float req = 0f;
        if (member.isFrigate()) {
            req = 100000f;
        } else if (member.isDestroyer()) {
            req = 200000f;
        } else if (member.isCruiser()) {
            req = 400000f;
        } else if (member.isCapital()) {
            req = 800000f;
        }
        return "Req: " + Misc.getDGSCredits(req) + " assets/income";
    }

    @Override
    protected Object writeReplace() {
        if (okToUpdateShipsAndWeapons()) {
            pruneWeapons(0f);
            getCargo().getMothballedShips().clear();
        }
        return this;
    }

    private boolean canPlayerAffordUnlock() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        int credits = (int) playerFleet.getCargo().getCredits().get();
        return credits >= getUnlockCost();
    }

    private int getUnlockCost() {
        float fudge;
        switch (submarket.getFaction().getRelToPlayer().getLevel()) {
            default:
            case VENGEFUL:
            case HOSTILE:
            case INHOSPITABLE:
                fudge = 2f;
                break;
            case SUSPICIOUS:
                fudge = 1.5f;
                break;
            case NEUTRAL:
                fudge = 1f;
                break;
            case FAVORABLE:
                fudge = 0.75f;
                break;
            case WELCOMING:
                fudge = 0.5f;
                break;
            case FRIENDLY:
            case COOPERATIVE:
                fudge = 0f;
                break;
        }

        MonthlyReport report = SharedData.getData().getCurrentReport();
        report.computeTotals();
        float profit = Math.max(0f, report.getRoot().totalIncome - report.getRoot().totalUpkeep);
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        return Math.round((float) Math.sqrt(Math.max(playerFleet.getCargo().getCredits().get() + profit, 100000f) / 100000f) * 30f * fudge) * 1000;
    }
}
