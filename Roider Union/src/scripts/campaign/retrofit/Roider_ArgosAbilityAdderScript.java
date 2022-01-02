package scripts.campaign.retrofit;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import ids.Roider_Ids;

/**
 * Author: SafariJohn
 */
public class Roider_ArgosAbilityAdderScript implements EveryFrameScript {

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_ArgosAbilityAdderScript.class, "interval", "i");
    }

    private IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);

    @Override
    public boolean isDone() {
        return Global.getSector().getCharacterData().getAbilities().contains("roider_retrofit");
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        interval.advance(Misc.getDays(amount));

        if (!interval.intervalElapsed()) return;

        for (FleetMemberAPI ship : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (ship.getVariant().hasHullMod(Roider_Ids.Roider_Hullmods.CONVERSION_DOCK)) {
                Global.getSector().getCharacterData().addAbility("roider_retrofit");
                Global.getSector().getCampaignUI().addMessage(new BaseIntelPlugin() {

                    @Override
                    public String getIcon() {
                        return Global.getSettings().getSpriteName("intel", "roider_retrofit");
                    }

                    @Override
                    public void createIntelInfo(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
                        info.addPara("Added Roider Conversions ability", getTitleColor(mode), 0f);
                    }

                });
                break;
            }
        }
    }

}
