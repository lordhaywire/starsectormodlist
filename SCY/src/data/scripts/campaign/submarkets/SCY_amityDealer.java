//By Tartiflette.
package data.scripts.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.util.MagicSettings;
import static data.scripts.util.SCY_txt.txt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;

public class SCY_amityDealer extends BaseSubmarketPlugin {
    
    private int MAX_WEAPONS = 30;
    private int MAX_SHIPS = 20;
    private boolean DYNAMIC_TARRIFS = true;
    private final RepLevel MIN_STANDING = RepLevel.NEUTRAL;
    private boolean verbose=false;
    
    private static final Logger log = Global.getLogger(SCY_amityDealer.class);
    
    private List<String> ALLOWED_FACTIONS = new ArrayList<>();
    private List<String> DISALLOWED_STUFF = new ArrayList<>();  
    
    private final Map<HullSize, Integer> MAX_FP= new HashMap<>();
    {
        MAX_FP.put(HullSize.DEFAULT, 50);
        MAX_FP.put(HullSize.FRIGATE, 6);
        MAX_FP.put(HullSize.DESTROYER, 10);
        MAX_FP.put(HullSize.CRUISER, 15);
        MAX_FP.put(HullSize.CAPITAL_SHIP, 24);
    }

    private void getSettings(){
        MAX_WEAPONS = MagicSettings.getInteger("SCY", "amity_maxWeaponStacks");
        MAX_SHIPS = MagicSettings.getInteger("SCY", "amity_maxShips");
        DYNAMIC_TARRIFS = MagicSettings.getBoolean("SCY", "amity_dynamicTarrif");
        ALLOWED_FACTIONS = MagicSettings.getList("SCY", "amity_factionWhitelist");
        DISALLOWED_STUFF = MagicSettings.getList("SCY", "amity_blacklist");
        if(Global.getSettings().isDevMode()){
            verbose=true;
        }
    }
    
    @Override
    public void updateCargoPrePlayerInteraction() {

        if(ALLOWED_FACTIONS.isEmpty()){
            getSettings();
        }
        
        if(verbose){
            log.info("Days since update: "+sinceLastCargoUpdate);
        }
        
        //only update every 30 days
        if (sinceLastCargoUpdate<30) return;
        
        CargoAPI cargo = getCargo();
        
        //list allowed factions
        WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker<>();
        for (FactionAPI faction: Global.getSector().getAllFactions()) {
            if ( ALLOWED_FACTIONS.contains(faction.getId()) ){
                factionPicker.add(faction.getId());                
            }
        }

        // WEAPONS AND FIGHTERS
        
        //cull some of the stock
        for (CargoStackAPI s : cargo.getStacksCopy()) {
            if(Math.random()<sinceLastCargoUpdate/100){
                float qty = s.getSize();
                cargo.removeItems(s.getType(), s.getData(), qty );
                cargo.removeEmptyStacks();
            }
        }
        
        //add new stuff
        float variation=(float)Math.random()*0.5f+0.75f;
        variation *= MAX_WEAPONS;        
        List<CargoStackAPI> validStack=new ArrayList<>();
        
        for ( float i=0f; i < variation; i = (float) cargo.getStacksCopy().size()) {
            
            //add some weapons and fighters (at the same time to save cycles and ensure a good mix)
            addWeapons(5,10,2,factionPicker.pick());
            addFighters(2,5,2,factionPicker.pick());
            
            //check for invalid stuff
            List <CargoStackAPI> toRemove = new ArrayList<>();
            for (CargoStackAPI entry : cargo.getStacksCopy()) {    
                //list removable weapons
                if(entry.isWeaponStack()){
                    if(!validStack.contains(entry)){
                        if(
                                entry.getWeaponSpecIfWeapon().hasTag("no_sell") || 
                                entry.getWeaponSpecIfWeapon().getTier()>2 || 
                                DISALLOWED_STUFF.contains(entry.getWeaponSpecIfWeapon().getWeaponId())
                                ){
                            toRemove.add(entry);
                        } else {validStack.add(entry);}
                    }
                }
                //list removable wings
                if(entry.isFighterWingStack()){
                    if(!validStack.contains(entry)){
                        if( 
                                entry.getFighterWingSpecIfWing().hasTag("no_sell") || 
                                entry.getFighterWingSpecIfWing().getTier()>2 ||
                                DISALLOWED_STUFF.contains(entry.getFighterWingSpecIfWing().getId())
                                ){
                            toRemove.add(entry);                        
                        } else {validStack.add(entry);}
                    }
                }
            }
            //remove that trash
            if(!toRemove.isEmpty()){
                if (verbose){
                    for (CargoStackAPI entry : toRemove) {
                        log.info("Removing high tier/blacklisted "+ entry.getDisplayName());
                        cargo.removeStack(entry);
                    }
                } else {
                    for (CargoStackAPI entry : toRemove) {
                        cargo.removeStack(entry);
                    }
                }
            }
        }
        
        //list the added stuff if needed
        if(verbose){
            for (CargoStackAPI entry : validStack) {
                log.info("Added stack of "+ entry.getDisplayName());
            }
        }
        
        // SHIPS
        
        //cull some of the old stuff
        List<FleetMemberAPI> keep = new ArrayList<>();
        for(FleetMemberAPI m : cargo.getMothballedShips().getMembersListCopy()){
            if(Math.random()<sinceLastCargoUpdate/100){
                cargo.getMothballedShips().removeFleetMember(m);
            } else {
                //save the ships that will be kept through the next culling phases
                keep.add(m);
            }
        }
        
        if (verbose){
            //add lots of new hulls
            for(int i=0; i<20; i++){
                
                FactionAPI faction = Global.getSector().getFaction(factionPicker.pick());

                log.info("Picking ship from "+faction.getId());
                addShips(
                        faction.getId(), 
                        MathUtils.getRandomNumberInRange(50, 150),
                        0.5f,
                        0.1f,
                        0.1f,
                        0.1f,
                        0.1f,
                        0.3f,
                        0.3f,
                        FactionAPI.ShipPickMode.ALL,
//                        null
                        faction.getDoctrine()
                        );
            }


            log.info("Checking for blacklist");
            //remove blacklisted ships
            for(FleetMemberAPI m : cargo.getMothballedShips().getMembersListCopy()){
                if(DISALLOWED_STUFF.contains(m.getHullSpec().getBaseHullId())){
                    cargo.getMothballedShips().removeFleetMember(m);
                    log.info("Removing blacklisted "+ m.getHullId());
                }
            }
            log.info("Allowed ships remaining: "+ cargo.getMothballedShips().getMembersListCopy().size());


            log.info("Checking for high grade");
            //remove high grade ships
            for(FleetMemberAPI m : cargo.getMothballedShips().getMembersListCopy()){
                if(keep.contains(m))continue; //skip prevalidated hulls
                if(m.getHullSpec().getFleetPoints() > MAX_FP.get(m.getHullSpec().getHullSize())){
                    cargo.getMothballedShips().removeFleetMember(m);
                    log.info("Removing high FP "+ m.getHullId());
                }
            }
            log.info("Remaining ships after FP pruning: "+ cargo.getMothballedShips().getMembersListCopy().size());


            log.info("Checking for no or too many Dmods");
            //remove hulls with no D-mod or more than 2
            for(FleetMemberAPI m : cargo.getMothballedShips().getMembersListCopy()){
                if(keep.contains(m))continue; //skip prevalidated hulls
                if(!m.getVariant().getHullMods().isEmpty()){
                    int dmods=0;
                    for ( String h : m.getVariant().getHullMods()){
                        if(Global.getSettings().getHullModSpec(h).hasTag("dmod")){
                            dmods++;
                        }
                    }
                    if(dmods>2 || dmods==0){
                        cargo.getMothballedShips().removeFleetMember(m);
                        log.info("Removing "+dmods+" D-mod "+ m.getHullId());
                    }
                } else {
                    cargo.getMothballedShips().removeFleetMember(m);
                    log.info("Removing 0 D-mod "+ m.getHullId());
                }
            }
            log.info("Remaining ships after dmod pruning: "+ cargo.getMothballedShips().getMembersListCopy().size());


            log.info("Checking for too many ships remaining");
            //if too many are left, remove random ships
            if(cargo.getMothballedShips().getMembersListCopy().size()>MAX_SHIPS){
                float threshold = MAX_SHIPS/cargo.getMothballedShips().getMembersListCopy().size();

                for(FleetMemberAPI m : cargo.getMothballedShips().getMembersListCopy()){
                    if(keep.contains(m))continue; //skip prevalidated hulls
                    if(Math.random()>threshold){
                        cargo.getMothballedShips().removeFleetMember(m);
                        log.info("Removing randomly "+ m.getHullId());
                    }
                    if(cargo.getMothballedShips().getMembersListCopy().size()<MAX_SHIPS){
                        break;
                    }
                }
            }
            log.info(
                    "Remaining ships after random pruning: "+
                            cargo.getMothballedShips().getMembersListCopy().size()+
                            "\n"+
                            cargo.getMothballedShips().getMembersListCopy()
            ); 
        } else {
            //same thing but non verbose 
            for(int i=0; i<10; i++){
                FactionAPI faction = Global.getSector().getFaction(factionPicker.pick());
                addShips(faction.getId(),100,50,10,10,10,10,0.3f,0.3f,FactionAPI.ShipPickMode.PRIORITY_ONLY,faction.getDoctrine());
            }
            for(FleetMemberAPI m : cargo.getMothballedShips().getMembersListCopy()){
                if(DISALLOWED_STUFF.contains(m.getHullSpec().getBaseHullId()))cargo.getMothballedShips().removeFleetMember(m);
            }
            for(FleetMemberAPI m : cargo.getMothballedShips().getMembersListCopy()){
                if(keep.contains(m))continue;
                if(m.getHullSpec().getFleetPoints() > MAX_FP.get(m.getHullSpec().getHullSize()))cargo.getMothballedShips().removeFleetMember(m);
            }
            for(FleetMemberAPI m : cargo.getMothballedShips().getMembersListCopy()){
                if(keep.contains(m))continue;
                if(!m.getVariant().getHullMods().isEmpty()){
                    int dmods=0;
                    for ( String h : m.getVariant().getHullMods()){
                        if(Global.getSettings().getHullModSpec(h).hasTag("dmod"))dmods++;
                    }
                    if(dmods>2 || dmods==0)cargo.getMothballedShips().removeFleetMember(m);
                } else cargo.getMothballedShips().removeFleetMember(m);
            }
            if(cargo.getMothballedShips().getMembersListCopy().size()>MAX_SHIPS){
                float threshold = MAX_SHIPS/cargo.getMothballedShips().getMembersListCopy().size();            
                for(FleetMemberAPI m : cargo.getMothballedShips().getMembersListCopy()){
                    if(keep.contains(m))continue;
                    if(Math.random()>threshold)cargo.getMothballedShips().removeFleetMember(m);
                    if(cargo.getMothballedShips().getMembersListCopy().size()<MAX_SHIPS)break;
                }
            }
        }
        //cleanup and done!
        cargo.sort();
        sinceLastCargoUpdate = 0f;
    }
    
    @Override
    public float getTariff() {
        if(!DYNAMIC_TARRIFS){return 0.3f;}
        
        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        switch (level)
        {
            case NEUTRAL:
                return -0.15f;
            case FAVORABLE:
                return -0.21f;
            case WELCOMING:
                return -0.26f;
            case FRIENDLY:
                return -0.3f;
            case COOPERATIVE:
                return -0.33f;
            default:
                return 0f;
        }
    }
    
    @Override
    public String getTooltipAppendix(CoreUIAPI ui){
        if (!isEnabled(ui)){
            return "Requires: " + submarket.getFaction().getDisplayName() + " - " + MIN_STANDING.getDisplayName().toLowerCase();
        }
        return null;
    }
    
    @Override
    public boolean isEnabled(CoreUIAPI ui){
        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        return level.isAtWorst(MIN_STANDING);
    }

    @Override
    public boolean isBlackMarket(){
            return false;
    }
     
    //CAN'T SELL!
    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }
    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }    
    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }    
    @Override
    public String getIllegalTransferText(FleetMemberAPI member, TransferAction action)
    {
        return txt("market_amity");
    }    
    @Override
    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action)
    {
        return txt("market_amity");
    }

}