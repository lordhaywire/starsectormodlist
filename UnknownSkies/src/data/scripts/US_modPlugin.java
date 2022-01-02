package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.Farming;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation.ConstellationType;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.awt.Color;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;

public class US_modPlugin extends BaseModPlugin {
	
    public static Logger log = Global.getLogger(US_modPlugin.class);
    private List<String> BG_YOUNG = new ArrayList<>();
    private List<String> BG_AVERAGE = new ArrayList<>();
    private List<String> BG_OLD = new ArrayList<>();
    
    private final String backgroundList="data/config/modFiles/US_backgroundList.csv";  
    
//    private final WeightedRandomPicker<String> METALS = new WeightedRandomPicker<>();
//    {
//        METALS.add("ore_sparse", 0.5f);
//        METALS.add("ore_moderate", 1);
//        METALS.add("ore_abundant", 2);
//        METALS.add("ore_rich", 3);
//        METALS.add("ore_ultrarich", 1);
//    }
//    
//    private final WeightedRandomPicker<String> RARE_METALS = new WeightedRandomPicker<>();
//    {
//        RARE_METALS.add("rare_ore_sparse", 1);
//        RARE_METALS.add("rare_ore_moderate", 2);
//        RARE_METALS.add("rare_ore_abundant", 3);
//        RARE_METALS.add("rare_ore_rich", 2);
//        RARE_METALS.add("rare_ore_ultrarich", 0.5f);
//    }
    
    private final WeightedRandomPicker<String> RUINS = new WeightedRandomPicker<>();
    {
        RUINS.add("ruins_scattered",1);
        RUINS.add("ruins_widespread",2);
        RUINS.add("ruins_extensive",3);
        RUINS.add("ruins_vast",1.5f);
        RUINS.add("decivilized",0.5f);
    }
    
    private Map<String,Integer> PLANET_TYPES = new HashMap<>();
    private Map<String,Integer> SPECIAL_CONDITIONS = new HashMap<>();
    
    @Override
    public void onNewGameAfterProcGen() {
        
        //Set aquaculture planets
        Farming.AQUA_PLANETS.add("US_water");  
        Farming.AQUA_PLANETS.add("US_waterB");
        
        //seed the backgrounds
        GetBG();        
        for(StarSystemAPI s : Global.getSector().getStarSystems()){
            if (s!=null && s.isProcgen() && s.getConstellation()!=null){
                if(s.getConstellation().getType() == ConstellationType.NORMAL){
                    if(Math.random()<0.33f){
                        String path;
                        StarAge a = s.getConstellation().getAge();
                        if(a == StarAge.YOUNG){                            
                            int chooser = new Random().nextInt(BG_YOUNG.size() - 1) + 1;    
                            path=BG_YOUNG.get(chooser);
                        } else if(a == StarAge.AVERAGE){                                          
                            int chooser = new Random().nextInt(BG_AVERAGE.size() - 1) + 1;    
                            path=BG_AVERAGE.get(chooser);
                        } else {
                            int chooser = new Random().nextInt(BG_OLD.size() - 1) + 1;    
                            path=BG_OLD.get(chooser);
                        }
                        s.setBackgroundTextureFilename(path);
                    }
                } else if(Math.random()<0.66f){
                    String path;
                        StarAge a = s.getConstellation().getAge();
                        if(a == StarAge.YOUNG){                            
                            int chooser = new Random().nextInt(BG_YOUNG.size() - 1) + 1;    
                            path=BG_YOUNG.get(chooser);
                        } else if(a == StarAge.AVERAGE){                                          
                            int chooser = new Random().nextInt(BG_AVERAGE.size() - 1) + 1;    
                            path=BG_AVERAGE.get(chooser);
                        } else {
                            int chooser = new Random().nextInt(BG_OLD.size() - 1) + 1;    
                            path=BG_OLD.get(chooser);
                        }
                        s.setBackgroundTextureFilename(path);
                }
            }
        }
        
        float planets=0;
        
        //seed planetary conditions
        for(StarSystemAPI s : Global.getSector().getStarSystems()){
            
            if(s==null || s.getPlanets().isEmpty())continue;
            
            
            for(PlanetAPI p : s.getPlanets()){
                if(p.isStar())continue;

                //log planet types
                if(PLANET_TYPES.containsKey(p.getTypeId())){
                    PLANET_TYPES.put(p.getTypeId(), PLANET_TYPES.get(p.getTypeId())+1);
                } else {
                    PLANET_TYPES.put(p.getTypeId(), 1);
                }
                planets++;

                //log special conditions
                if(p.getMarket().hasCondition("US_floating")){
                    log.info("FLOATING CONTINENT found on " + p.getName() + " ("+ p.getTypeId() +") in " + s.getName());   
                    AddRandomConditionIfNeeded(p,RUINS.getItems(),RUINS);
                    log.info(" ");  
                    countConditions("Floating Continent");
                }
                if(p.getMarket().hasCondition("US_religious")){
                    log.info("RELIGIOUS SITE found on " + p.getName() + " ("+ p.getTypeId() +") in " + s.getName());     
                    log.info(" ");  
                    countConditions("Religious Landmark");
                }
                if(p.getMarket().hasCondition("US_base")){
                    log.info("MILITARY BASE found on " + p.getName() + " ("+ p.getTypeId() +") in " + s.getName());     
                    log.info(" ");  
                    countConditions("Abandoned Base");
                }
                if(p.getMarket().hasCondition("US_crash")){
                    log.info("CRASHED DRONE found on " + p.getName() + " ("+ p.getTypeId() +") in " + s.getName());     
                    log.info(" ");  
                    countConditions("Crashed Drone");
                }
                if(p.getMarket().hasCondition("US_virus")){
                    log.info("VIRUS found on " + p.getName() + " ("+ p.getTypeId() +") in " + s.getName());
                    log.info(" ");  
                    countConditions("Military Virus");
                }
                if(p.getMarket().hasCondition("US_elevator")){
                    log.info("SPACE ELEVATOR found on " + p.getName() + " ("+ p.getTypeId() +") in " + s.getName());
                    log.info(" ");  
                    countConditions("Space Elevator");
                }
                if(p.getMarket().hasCondition("US_shrooms")){
                    log.info("MAGIC SHROOMS found on " + p.getName() + " ("+ p.getTypeId() +") in " + s.getName());
                    log.info(" ");  
                    countConditions("Magic Shrooms");
                }
                if(p.getMarket().hasCondition("US_tunnels")){
                    log.info("UNDERGROUND MAZE found on " + p.getName() + " ("+ p.getTypeId() +") in " + s.getName());
                    log.info(" ");  
                    countConditions("Underground Maze");
                }
                if(p.getMarket().hasCondition("US_mind")){
                    log.info("PARASITIC SPORES found on " + p.getName() + " ("+ p.getTypeId() +") in " + s.getName());
                    log.info(" ");  
                    countConditions("Parasitic Spores");
                }
                if(p.getMarket().hasCondition("US_bedrock")){
                    log.info("ACCESSIBLE BEDROCK found on " + p.getName() + " ("+ p.getTypeId() +") in " + s.getName());
                    log.info(" ");  
                    countConditions("Accessible Bedrock");
                }
                
                //Add special conditions
                switch(p.getTypeId()){
                    case "US_artificial":
                        log.info("Adding ARTIFICIAL condition to " + p.getName() + " in " + s.getName());
                        p.getMarket().addCondition("US_artificial");     
                        log.info(" ");  
                        countConditions("Artificial");
                        break;
                    case "US_magnetic":
                        log.info("Adding MAGNETIC condition to " + p.getName() + " in " + s.getName());
                        p.getMarket().addCondition("US_magnetic");
                        SectorEntityToken magField = s.addTerrain(
                                Terrain.MAGNETIC_FIELD,
                                new MagneticFieldTerrainPlugin.MagneticFieldParams(
                                        80, // terrain effect band width 
                                        p.getRadius()+50, // terrain effect middle radius
                                        p, // entity that it's around
                                        p.getRadius(), // visual band start
                                        p.getRadius()+110, // visual band end
                                        new Color(50,175,200,100), // base color
                                        0.25f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                                        new Color(25,250,100,150)
                                )
                        );
                        magField.setCircularOrbit(p, 0, 0, 100);     
                        log.info(" ");  
                        countConditions("Magnetic Crust");
                        break;
                    case "US_storm":
                        log.info("Adding STORM condition to " + p.getName() + " in " + s.getName());
                        p.getMarket().addCondition("US_storm");     
                        log.info(" ");  
                        countConditions("Perpetual Storm");
                        break;
//                    case "US_lava":
//                        log.info("Adding ressources to " + p.getName() + " in " + s.getName());
//                        AddRandomConditionIfNeeded(p,RARE_METALS.getItems(),RARE_METALS);
//                        AddRandomConditionIfNeeded(p,METALS.getItems(),METALS);     
//                        log.info(" ");  
//                        break;
//                    case "US_volcanic":
//                        log.info("Adding ressources to " + p.getName() + " in " + s.getName());
//                        AddRandomConditionIfNeeded(p,RARE_METALS.getItems(),RARE_METALS);
//                        AddRandomConditionIfNeeded(p,METALS.getItems(),METALS);       
//                        log.info(" ");  
//                        break;
                    case "US_burnt":
                        log.info("Adding IRRADIATED condition to " + p.getName() + " in " + s.getName());
                        AddConditionIfNeeded(p,"irradiated");     
                        log.info(" ");  
                        countConditions("Irradiated Environment");
                        break;
                }
            }
        }
        
        //print out sector content
        
        log.info("_______________");
        log.info("PLANET TYPES:");
        log.info(" ");
        for(String p : PLANET_TYPES.keySet()){            
            log.info(p+" : "+PLANET_TYPES.get(p));  
            log.info(PLANET_TYPES.get(p)*100/planets+" percent");               
            log.info(" ");  
            
        }        
        
        
        log.info(" ");        
        log.info(planets+" planets in total.");        
        log.info("_______________");

        log.info("Special Conditions:");
        log.info(" ");
        for(String p : SPECIAL_CONDITIONS.keySet()){            
            log.info(p+" : "+SPECIAL_CONDITIONS.get(p));               
            log.info(" ");              
        }       
        log.info("_______________");
    }
    
    private void countConditions(String condition){
        if(SPECIAL_CONDITIONS.containsKey(condition)){
            SPECIAL_CONDITIONS.put(condition, SPECIAL_CONDITIONS.get(condition)+1);
        } else {
            SPECIAL_CONDITIONS.put(condition, 1);
        }
    }
    
    private void AddConditionIfConditionMet (PlanetAPI p, List<String> toCheck, String toAdd){  
        //check for the unwanted conditions
        boolean doIt=true;
        if(!p.getMarket().getConditions().isEmpty()){
            for( MarketConditionAPI c : p.getMarket().getConditions()){
                if(toCheck.contains(c.getId())){
                    doIt=false;
                    break;
                }
            }
        }
        
        //add the condition      
        if(doIt){
            p.getMarket().addCondition(toAdd);
        }
    }
    private void AddRandomConditionIfNeeded (PlanetAPI p, List<String> toCheck, WeightedRandomPicker<String> picker){  
        //check for the unwanted conditions
        boolean doIt=true;
        if(!p.getMarket().getConditions().isEmpty()){
            for( MarketConditionAPI c : p.getMarket().getConditions()){
                if(toCheck.contains(c.getId())){
                    doIt=false;
                    break;
                }
            }
        }
        
        //add the condition      
        if(doIt){
            p.getMarket().addCondition(picker.pick());
        }
    }
    private void AddConditionIfNeeded (PlanetAPI p, String toAdd){  
        //check for the unwanted conditions
        boolean doIt=true;
        if(!p.getMarket().getConditions().isEmpty()){
            for( MarketConditionAPI c : p.getMarket().getConditions()){
                if(c.getId().equals(toAdd)){
                    doIt=false;
                    break;
                }
            }
        }
        
        //add the condition      
        if(doIt){
            p.getMarket().addCondition(toAdd);
        }
    }
    
    private void GetBG(){
        try {
            JSONArray bgList = Global.getSettings().getMergedSpreadsheetDataForMod("path", backgroundList, "US");
             for(int i = 0; i < bgList.length(); i++) {            
                JSONObject row = bgList.getJSONObject(i);
                                        
                String type = row.getString("age");
                switch (type) {
                    case "YOUNG":
                        BG_YOUNG.add(row.getString("path"));
                        break;
                    case "AVERAGE":
                        BG_AVERAGE.add(row.getString("path"));
                        break;
                    default:
                        BG_OLD.add(row.getString("path"));
                        break;
                }
            }
        } catch (IOException | JSONException ex) {
            log.error("unable to read backgroundList.csv");
        }
    }
}