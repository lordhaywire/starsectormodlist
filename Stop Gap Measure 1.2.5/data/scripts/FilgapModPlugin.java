package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

import data.scripts.world.FilgapGen;


public class FilgapModPlugin extends BaseModPlugin
{
    private static void initFilgap() 
        {
            new FilgapGen().generate(Global.getSector());
        }
    @Override
    public void onNewGame() {
        initFilgap();
    }
}
