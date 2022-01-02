package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
//import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
//import org.lazywizard.lazylib.combat.entities.SimpleEntity;
//import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_artdeuxFire implements EveryFrameWeaponEffectPlugin {
        
    private boolean runOnce=false, hidden=false, sound=false, refire=false;
    private float delay=0;
    private SpriteAPI barrel;
    private float barrelHeight=0, recoil=0, ammo=0;
    private final float maxRecoil=-6;
//    CombatEntityAPI soundSource;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1){return;}
        if(!runOnce){
            runOnce=true;
            if(weapon.getSlot().isHidden()){
                hidden=true;
            } else {
                barrel=weapon.getBarrelSpriteAPI();
                if(weapon.getSlot().isTurret()){
                    barrelHeight=barrel.getHeight()/2;
                } else {                    
                    barrelHeight=barrel.getHeight()/4;
                }
            }
            return;
        }
        
        if(weapon.getChargeLevel()==1){
            if(!sound){
                sound=true;
                refire=true;
                delay=0;
            }
        } else if(weapon.getChargeLevel()==0){
            if(refire){
                delay+=amount;
                if(delay>0.075f)refire=false;
            } else
            if(sound){
                sound=false;
                Global.getSoundPlayer().playSound("diableavionics_artdeux_trail", 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
            }
        }
        
        /*
        if(weapon.getChargeLevel()==1){
//            soundSource = new SimpleEntity(weapon.getLocation());
//            if(!sound){
////                Global.getSoundPlayer().playSound("diableavionics_artdeux_start", 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
//            }
            if(!sound){
                Global.getSoundPlayer().playLoop("diableavionics_artdeux_loop", weapon, 1, 0.05f, weapon.getLocation(), weapon.getShip().getVelocity());
//                Global.getSoundPlayer().playSound("diableavionics_artdeux_start", 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
                sound=true;
            } else {
//            Global.getSoundPlayer().playLoop("diableavionics_artdeux_loop", soundSource, 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
                Global.getSoundPlayer().playLoop("diableavionics_artdeux_loop", weapon, 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
            }
        } else if(sound){
            sound=false;
//            soundSource=null;
            Global.getSoundPlayer().playLoop("diableavionics_artdeux_loop", weapon, 1, 0.05f, weapon.getLocation(), weapon.getShip().getVelocity());
//            Global.getSoundPlayer().playSound("diableavionics_artdeux_end", 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
        }
        */
        if(!hidden){
            if(weapon.getChargeLevel()==1 && weapon.getAmmo()<ammo){
                recoil=Math.min(1, recoil+0.05f);
            } else {
                recoil=Math.max(0, recoil-(0.5f*amount));
            }
            barrel.setCenterY(barrelHeight-(recoil*maxRecoil));
            ammo=weapon.getAmmo();            
        }
    }
}