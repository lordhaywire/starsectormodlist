package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.ai.TADA_pilumAI;
import data.scripts.ai.TADA_spikeAI;
import data.scripts.util.MagicSettings;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

public class TADA_modPlugin extends BaseModPlugin {

    public static final String spike_ID = "TADA_spike_rod";
    public static final String pilum_ID = "TADA_pilumMissile";  

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case spike_ID:
                return new PluginPick<MissileAIPlugin>(new TADA_spikeAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case pilum_ID:
                return new PluginPick<MissileAIPlugin>(new TADA_pilumAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
        }
        return null;
    }
    
    @Override
    public void onApplicationLoad() throws ClassNotFoundException {
        
        if(Global.getSettings().getModManager().isModEnabled("shaderLib")){
            ShaderLib.init();  
            LightData.readLightDataCSV(MagicSettings.getString("TADA", "graphicLib_lights")); 
            TextureData.readTextureDataCSV(MagicSettings.getString("TADA", "graphicLib_maps")); 
        }
    }
}
