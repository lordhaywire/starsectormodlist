package data.scripts;  
  
import exerelin.campaign.SectorManager;  
import com.fs.starfarer.api.BaseModPlugin;  
import com.fs.starfarer.api.Global;  
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import data.scripts.world.OcuAGen;
import data.scripts.everyframe.OCUA_BlockedHullmodDisplayScript;
import data.scripts.ids.OCUA_Factions;
import data.scripts.ids.OCUA_Skills;
  
public class OculusPlugin extends BaseModPlugin {  
  
    public static boolean isExerelin = false;
    
    private static void initOcuA() {  
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getCorvusMode()){
            new OcuAGen().generate(Global.getSector());
        }
        
        //if (isExerelin && !SectorManager.getCorvusMode()) {
        //    return;
        //}
        //    new OcuAGen().generate(Global.getSector());
    }  
	
    @Override
    public void onNewGame()
    {
        initOcuA();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().addTransientScript(new OCUA_BlockedHullmodDisplayScript());
        
        
    }

    @Override
    public void onApplicationLoad()
    {
    //    boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
    //    if (!hasLazyLib) {
    //        throw new RuntimeException("Oculian Berserks requires LazyLib!");
    //    }
    //    isExerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        
    }
    
    @Override
    public void onNewGameAfterEconomyLoad() {
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		MarketAPI market = null;
		
		market =  Global.getSector().getEconomy().getMarket("ocua_atalie3");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId("ocua_ei");
			person.setFaction(OCUA_Factions.OCUA);
			person.setGender(FullName.Gender.FEMALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("EI");
			person.getName().setLast("");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "ocua_ei"));
			person.getStats().setSkillLevel(OCUA_Skills.OCUA_Arc_Cognition, 1);
			//person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			person.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
                        
			market.setAdmin(person);
			market.addPerson(person);
			market.getCommDirectory().addPerson(person);
			//market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			ip.addPerson(person);
		}
    }
}  


//    private static void initOculus() {  
//        try {  
//            Global.getSettings().getScriptClassLoader().loadClass("data.scripts.world.ExerelinGen");
//        } 
//        catch (ClassNotFoundException ex) {
//            new OculusGen().generate(Global.getSector());
//        }
//    }  
    

//    public static final boolean isExerelin;
//        static
//            {
//            boolean foundExerelin;
//            try
//                {
//                    Global.getSettings().getScriptClassLoader().loadClass("data.scripts.world.ExerelinGen");
//                    foundExerelin = true;
//                }
//            catch (ClassNotFoundException ex)
//                {
//                    foundExerelin = false;
//                }
//        isExerelin = foundExerelin;
//    }