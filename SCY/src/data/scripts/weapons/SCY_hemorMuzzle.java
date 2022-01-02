//By Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.SCY_muzzleFlashesPlugin;
import data.scripts.util.MagicRender;

public class SCY_hemorMuzzle implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin{
    
//    private boolean put=false;
//    private boolean runOnce=false;
//    private boolean hidden=false;
    
//    @Override
//    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
//        
//        if(!hidden && engine.isPaused()){return;}
//        
//        if(!runOnce){
//            runOnce=true;
//            //check if the mount is hidden
//            if (weapon.getSlot().isHidden()){
//                hidden=true;
//                return;
//            }
//        }
//        
//        //assign the weapon for the muzzle flash plugin
//        if (weapon.getChargeLevel()==1){
//            if(MagicRender.screenCheck(0.2f, weapon.getLocation())){
//                //add the weapon to the MEMBERS map when it fires
//                //"put" is to make sure it's added only once
//                if (!put){
//                    put=true;
//                    int flip = Math.round(3*(float)Math.random()-0.5f)+1;
//                    if(Math.random()>0.5){
//                        flip*=-1;
//                    }
//                    SCY_muzzleFlashesPlugin.addMuzzle(weapon,0,flip,false);
//                }
//            }
//        } else if (weapon.getChargeLevel()== 0){
//            //reinitialise "put"
//            if (put){
//                put=false;
//            }
//        }
//    }
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    }
    
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if(weapon.getSlot().isHidden())return;
        if(MagicRender.screenCheck(0.25f, weapon.getLocation())){
            SCY_muzzleFlashesPlugin.addMuzzle(weapon,0,Math.random()>0.5);
        }
    }
}
