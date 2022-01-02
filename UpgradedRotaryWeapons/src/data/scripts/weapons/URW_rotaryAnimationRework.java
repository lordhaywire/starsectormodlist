//By Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class URW_rotaryAnimationRework implements EveryFrameWeaponEffectPlugin{
  
    private float charge = 0f, spinUp=0.01f, delay = 0.1f, timer = 0f;
    private int frame = 0, numFrames;
    private float minDelay;
    private boolean runOnce = false;
    private AnimationAPI theAnim;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine.isPaused() || weapon.getSlot().isHidden()) {
            return;
        }
        
        if(!runOnce){
            runOnce=true;
            
            theAnim = weapon.getAnimation();
            numFrames=theAnim.getNumFrames();
            minDelay=1/theAnim.getFrameRate();

//            if (weapon.getSize() == WeaponAPI.WeaponSize.LARGE) {
//                spinUp = 0.01f;
//            } else if (weapon.getSize() == WeaponAPI.WeaponSize.MEDIUM) {
//                spinUp = 0.02f;
//            } else {
//                spinUp = 0.04f;
//            }
//            if (weapon.getId().contentEquals("chaingun") || weapon.getId().contentEquals("ionpulser")) {
//                spinUp *= 3f;
//            }
        }

        float mult = 1f;
        if (weapon.getShip() != null) {
            switch (weapon.getSpec().getType()) {
                case BALLISTIC:
                    mult *= weapon.getShip().getMutableStats().getBallisticRoFMult().getModifiedValue();
                    break;
                case ENERGY:
                    mult *= weapon.getShip().getMutableStats().getEnergyRoFMult().getModifiedValue();
                    break;
                case MISSILE:
                    mult *= weapon.getShip().getMutableStats().getMissileRoFMult().getModifiedValue();
                    break;
                default:
                    break;
            }
        }

        minDelay/=mult;
        
        timer += amount;
        if (timer >= delay) {
            timer -= delay;
            if (weapon.getChargeLevel() >= charge && weapon.getChargeLevel()>0) {
                                
                delay = Math.max(delay - spinUp, minDelay);                
                
            } else {
                
                delay = Math.min(delay + (delay * 4f * spinUp), 0.1f);
                
            }
            if (delay != 0.1f) {
                frame++;
                if (frame == numFrames) {
                    frame = 0;
                }
            }
        }
        theAnim.setFrame(frame);

        charge = Math.max(weapon.getChargeLevel(), charge-0.05f);
    }
}
