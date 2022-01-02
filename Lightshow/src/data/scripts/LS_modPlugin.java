package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;

public class LS_modPlugin extends BaseModPlugin {
    
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
            LightData.readLightDataCSV("data/config/modFiles/LS_light_data.csv");  
        } catch (ClassNotFoundException ex) { }
    }    
}
