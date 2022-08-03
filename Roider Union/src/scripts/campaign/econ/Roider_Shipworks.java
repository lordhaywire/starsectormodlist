package scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids.Roider_Industries;
import ids.Roider_MemFlags;

/**
 * Author: SafariJohn
 */
public class Roider_Shipworks extends BaseIndustry {

    public static void aliasAttributes(XStream x) {
    }

    public static final float ALPHA_DISCOUNT = 10f;

    @Override
    public void apply() {

		int size = 3;

		demand(Commodities.SHIPS, size);
		demand(Commodities.HEAVY_MACHINERY, size - 2);

		supply(Commodities.SUPPLIES, size - 2);
		supply(Commodities.METALS, size);

		Pair<String, Integer> deficit = getMaxDeficit(Commodities.SHIPS, Commodities.HEAVY_MACHINERY);
		int maxDeficit = size - 2; // to allow *some* production so economy doesn't get into an unrecoverable state
		if (deficit.two > maxDeficit) deficit.two = maxDeficit;

		applyDeficitToProduction(2, deficit,
					Commodities.SUPPLIES,
					Commodities.METALS);

        super.apply(true);

        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_FUNCTIONAL, isFunctional());

		if (!isFunctional()) {
            supply.clear();
            unapply();
        }
    }

    @Override
    protected void applyAlphaCoreModifiers() {
        super.applyAlphaCoreModifiers();

        // Discount on conversions if AI core installed
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_ALPHA, true);
    }

    @Override
    protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        super.addAlphaCoreDescription(tooltip, mode);

        tooltip.addPara("Reduces retrofit costs by %s.", 0f,
                    Misc.getHighlightColor(),
                    (int) ALPHA_DISCOUNT + "%");
    }

    @Override
    public boolean canImprove() {
        return false;
    }

    @Override
    public boolean isAvailableToBuild() {
        if (!market.getFaction().knowsIndustry(Roider_Industries.SHIPWORKS)) {
            return false;
        }
        return super.isAvailableToBuild();
    }

    @Override
    public String getUnavailableReason() {
        if (!market.getFaction().knowsIndustry(Roider_Industries.SHIPWORKS)) {
            return "Purchase the blueprint for this industry at a Union HQ";
        }

        return super.getUnavailableReason();
    }

    //<editor-fold defaultstate="collapsed" desc="Cleanup code in various methods">
    @Override
    public void advance(float amount) {
        super.advance(amount);
//        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_FUNCTIONAL, isFunctional());
//        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_ALPHA, getAICoreId().equals(Commodities.ALPHA_CORE));
    }

    @Override
    public void unapply() {
        super.unapply();
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_FUNCTIONAL, false);
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_ALPHA, false);
    }

    @Override
    protected void notifyDisrupted() {
        super.notifyDisrupted();
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_FUNCTIONAL, false);
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_ALPHA, false);
    }

    @Override
    protected void disruptionFinished() {
        super.disruptionFinished();
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_FUNCTIONAL, isFunctional());

        boolean alphaCore = getAICoreId() == null ? false : getAICoreId().equals(Commodities.ALPHA_CORE);
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_ALPHA, alphaCore);
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
        super.notifyBeingRemoved(mode, forUpgrade);
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_FUNCTIONAL, false);
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_ALPHA, false);
    }

    @Override
    protected void applyGammaCoreModifiers() {
        super.applyGammaCoreModifiers();
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_ALPHA, false);
    }

    @Override
    protected void applyBetaCoreModifiers() {
        super.applyBetaCoreModifiers();
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_ALPHA, false);
    }

    @Override
    protected void applyNoAICoreModifiers() {
        super.applyNoAICoreModifiers();
        market.getMemoryWithoutUpdate().set(Roider_MemFlags.SHIPWORKS_ALPHA, false);
    }
//</editor-fold>

}
