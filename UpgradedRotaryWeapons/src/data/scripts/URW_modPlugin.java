package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

public class URW_modPlugin extends BaseModPlugin {
    
    @Override
    public void onApplicationLoad() throws ClassNotFoundException {
        
        try {  
            Global.getSettings().getScriptClassLoader().loadClass("org.dark.shaders.util.ShaderLib");  
        } catch (ClassNotFoundException ex) {  
            return;  
        }
        ShaderLib.init();
        TextureData.readTextureDataCSV("data/lights/URW_texture_data.csv");         
    }	
}