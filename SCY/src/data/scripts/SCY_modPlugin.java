package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import data.scripts.ai.SCY_antiMissileAI;
import data.scripts.ai.SCY_arcMissileAI;
import data.scripts.ai.SCY_coastingMissileAI;
import data.scripts.ai.SCY_laserTorpedoAI;
import data.scripts.ai.SCY_clusterTorpedoAI;
import data.scripts.ai.SCY_phaseTorpedoAI;
import data.scripts.ai.SCY_rocketAI;
import data.scripts.ai.SCY_swarmerAI;
import data.scripts.plugins.SCY_muzzleFlashesPlugin;
import data.scripts.plugins.SCY_projectilesEffectPlugin;
import data.scripts.util.MagicCampaign;
import static data.scripts.util.SCY_txt.txt;
import data.scripts.world.SCY_gen;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import exerelin.campaign.SectorManager;

public class SCY_modPlugin extends BaseModPlugin {

    public static final String antiMissile_ID = "SCY_antiS";
    public static final String swarmer_ID = "SCY_swarmS";
    public static final String coasting_ID = "SCY_coastingS";
    public static final String laser_ID = "SCY_laserS";
    public static final String bomberTorpedo_ID = "SCY_bomberTorpedo";
    public static final String cluster_ID = "SCY_clusterS";    
    public static final String rocket_ID = "SCY_rocketS";      
    public static final String arc_ID = "SCY_arcS";    
    public static final String phase_ID = "SCY_phasedS";
    
    ////////////////////////////////////////
    //                                    //
    //      MISSILES AI OVERRIDES         //
    //                                    //
    ////////////////////////////////////////
    
    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) { 
            case rocket_ID:
                return new PluginPick<MissileAIPlugin>(new SCY_rocketAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case antiMissile_ID:
                return new PluginPick<MissileAIPlugin>(new SCY_antiMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case swarmer_ID:
                return new PluginPick<MissileAIPlugin>(new SCY_swarmerAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case coasting_ID:
                return new PluginPick<MissileAIPlugin>(new SCY_coastingMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case laser_ID:
                return new PluginPick<MissileAIPlugin>(new SCY_laserTorpedoAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case bomberTorpedo_ID:
                return new PluginPick<MissileAIPlugin>(new SCY_laserTorpedoAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case cluster_ID:
                return new PluginPick<MissileAIPlugin>(new SCY_clusterTorpedoAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case arc_ID:
                return new PluginPick<MissileAIPlugin>(new SCY_arcMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case phase_ID:
                return new PluginPick<MissileAIPlugin>(new SCY_phaseTorpedoAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
        }
        return null;
    }
    
    ////////////////////////////////////////
    //                                    //
    //       ON APPLICATION LOAD          //
    //                                    //
    ////////////////////////////////////////
    
    @Override
    public void onApplicationLoad() throws ClassNotFoundException {
        //Check ShaderLib for lights
        try {  
            Global.getSettings().getScriptClassLoader().loadClass("org.dark.shaders.util.ShaderLib");  
            ShaderLib.init();  
            LightData.readLightDataCSV("data/config/modFiles/SCY_lights.csv");  
            TextureData.readTextureDataCSV("data/config/modFiles/SCY_maps.csv");  
        } catch (ClassNotFoundException ex) {
        }
    }    
    
    ////////////////////////////////////////
    //                                    //
    //        ON NEW GAME CREATION        //
    //                                    //
    ////////////////////////////////////////
    @Override
    public void onNewGame() {
	boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
	if (!haveNexerelin || SectorManager.getManager().getCorvusMode()){
            new SCY_gen().generate(Global.getSector());
        }
    }
    
    @Override
    public void onNewGameAfterEconomyLoad() {
	//special admins
        MarketAPI market =  Global.getSector().getEconomy().getMarket("TAR_elysee");
        if (market != null) {
            if(!market.getAdmin().getName().getLast().equals("Railey")){
                PersonAPI person = Global.getFactory().createPerson();
                person.setFaction("SCY");
                person.setGender(FullName.Gender.FEMALE);
                person.setRankId(Ranks.FACTION_LEADER);
                person.setPostId(Ranks.POST_FACTION_LEADER);
                person.getName().setFirst("Amanda");
                person.getName().setLast("Railey");
                person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "railey"));
                person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 0);
                person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 3);
                person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);

                market.setAdmin(person);
                market.getCommDirectory().addPerson(person, 0);
                market.addPerson(person);
            }
            
            //move the home system to its proper location after the proc gen added its systems
            market.getStarSystem().getLocation().set(8000f, -19500f);
            MagicCampaign.hyperspaceCleanup(market.getStarSystem());
        }
    }    
    
    ////////////////////////////////////////
    //                                    //
    //            ON GAME LOAD            //
    //                                    //
    ////////////////////////////////////////
    
    @Override
    public void onGameLoad(boolean newGame){
        // register our listener that gets notified when battles complete
        Global.getSector().addTransientListener(new ReportPlayerEngagementCampaignEventListener());
        SCY_muzzleFlashesPlugin.cleanSlate();
        SCY_projectilesEffectPlugin.cleanSlate();
    }    
    
    private static class ReportPlayerEngagementCampaignEventListener extends BaseCampaignEventListener{
        
        public ReportPlayerEngagementCampaignEventListener(){
            super(false);
        }

        @Override
        public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle){
            clearState();
        }

        @Override
        public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle){
            clearState();
        }

        @Override
        public void reportPlayerEngagement(EngagementResultAPI result) {
            clearState();
        }

        private void clearState(){
            SCY_muzzleFlashesPlugin.cleanSlate();
            SCY_projectilesEffectPlugin.cleanSlate();
        }
    }
}
