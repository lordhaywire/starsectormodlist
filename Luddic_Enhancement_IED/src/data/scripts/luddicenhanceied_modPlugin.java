package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.ai.LE_MightAI;
import data.scripts.util.LE_Util;

public class luddicenhanceied_modPlugin extends BaseModPlugin {

    public static final String MIGHT_ID = "lp_dram_missile";

    @Override
    public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship) {
        switch (LE_Util.getNonDHullId(ship.getHullSpec())) {
            case MIGHT_ID:
                return new PluginPick<ShipAIPlugin>(new LE_MightAI(ship), CampaignPlugin.PickPriority.HIGHEST);
            default:
                break;
        }
        return null;
    }

}

