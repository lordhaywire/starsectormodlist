package scripts.campaign.retrofit;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Highlights;
import com.thoughtworks.xstream.XStream;
import java.util.List;
import java.util.Map;
import scripts.campaign.retrofit.Roider_RetrofitsKeeper.RetrofitData;

/**
 * Author: SafariJohn
 */
public class Roider_ShipworksRetrofitPlugin extends Roider_BaseRetrofitPlugin {

    public static void aliasAttributes(XStream x) {
    }

    public Roider_ShipworksRetrofitPlugin(InteractionDialogPlugin originalPlugin, Roider_BaseRetrofitManager manager, Map<String, MemoryAPI> memoryMap) {
        super(originalPlugin, manager, memoryMap);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (OptionId.PICK_TARGET.equals(optionData)) pickTarget();
        else if (OptionId.PICK_SHIPS.equals(optionData)) pickShips();
        else if (OptionId.PRIORITIZE.equals(optionData)) prioritize();
        else if (OptionId.CANCEL_SHIPS.equals(optionData)) cancelShips();
        else if (optionData instanceof List) {
            confirmRetrofits((List) optionData);
        }
        else if (OptionId.CANCEL.equals(optionData)) {
            updateText();
            updateOptions();
        } else {
            text.clear();

            options.clearOptions();

            visual.fadeVisualOut();

            dialog.setPlugin(originalPlugin);
            originalPlugin.optionSelected(null, "backToBar");
        }
    }

    @Override
    protected String getLeaveOptionText() {
        return "Return";
    }

    @Override
    protected String getNotAllowedRetrofitsTitle() {
        return "Blueprint Required";
    }

    @Override
    protected Highlights getNotAllowedRetrofitTextHighlights(String hullId) {
        Highlights h = new Highlights();
        h.setText(getNotAllowedRetrofitText(hullId));
        return h;
    }

    @Override
    protected String getNotAllowedRetrofitText(String hullId) {
        return "You do not know how to retrofit this hull";
    }

    @Override
    protected boolean isAllowed() {
        return Global.getSector().getPlayerFaction().knowsShip(selectedRetrofit.getHullId());
    }

    @Override
    protected boolean isAllowed(String sourceHull) {
        for (RetrofitData data : retrofits) {
            if (data.targetHull.equals(selectedRetrofit.getHullId())
                        && matchesHullId(sourceHull, data.sourceHull)) {
                return Global.getSector().getPlayerFaction().knowsShip(data.targetHull);
            }
        }

        return false;
    }

}
