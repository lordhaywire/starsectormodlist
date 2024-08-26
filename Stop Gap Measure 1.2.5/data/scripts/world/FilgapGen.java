package data.scripts.world;


import com.fs.starfarer.api.campaign.SectorAPI;

import data.scripts.world.systems.Byzos;
import data.scripts.world.systems.Utic;
import data.scripts.world.systems.Ivree;

public class FilgapGen {

	public void generate(SectorAPI sector) {
            (new Byzos()).generate(sector);
            (new Utic()).generate(sector);
            (new Ivree()).generate(sector);
                                               
        }

}
