package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.TTE_systems.TTE_Generator;
import exerelin.campaign.SectorManager;


public class TTEGen implements SectorGeneratorPlugin {
   
    public void generate(SectorAPI sector) {	
     if (!Global.getSettings().getModManager().isModEnabled("nexerelin") || SectorManager.getCorvusMode()) 
	 {
        new TTE_Generator().generate(sector);		
      }
    }
}
