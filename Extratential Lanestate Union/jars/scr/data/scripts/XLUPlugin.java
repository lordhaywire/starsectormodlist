package data.scripts;  
  
import com.fs.starfarer.api.BaseModPlugin;  
import com.fs.starfarer.api.Global;
import data.scripts.world.XLUGen;
import exerelin.campaign.SectorManager;
//import data.scripts.world.XLUGen;
  
public class XLUPlugin extends BaseModPlugin {  
  
    public static boolean isExerelin = false;
    
    private static void initXLU() { 
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getCorvusMode()){
            new XLUGen().generate(Global.getSector());
        }
    }  
	
    @Override
    public void onNewGame()
    {
        initXLU();
    }

    @Override
    public void onApplicationLoad()
    {
//        boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
//        if (!hasLazyLib) {
//            throw new RuntimeException("Extratential Lanestate Union requires LazyLib!");
//       }
//        isExerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
//        boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
//        if (!hasLazyLib) {
//            throw new RuntimeException("Oculian Berserks requires LazyLib!");
//        }
    }
}  