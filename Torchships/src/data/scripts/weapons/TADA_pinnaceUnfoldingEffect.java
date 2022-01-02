package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.util.MagicAnim;
import org.lazywizard.lazylib.MathUtils;

public class TADA_pinnaceUnfoldingEffect implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce = false, lock=false;
    private ShipAPI SHIP;
    
    private float folding=0, fakeWeapL_center, fakeWeapR_center;
    private final float wingL_angle=-65, wingR_angle=65, fakeWeapL_angle=-15, fakeWeapR_angle=15, fakeWeapL_offset=-3, fakeWeapR_offset=-3;
    private WeaponAPI wingL, wingR, fakeWeapL, fakeWeapR, weapL, weapR;
    private SpriteAPI fakeWeapL_sprite, fakeWeapR_sprite;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        //data collection
        if (!runOnce) {            
            runOnce=true;
            
            SHIP=weapon.getShip();            
            
            for(WeaponAPI w : SHIP.getAllWeapons()){
                switch(w.getSlot().getId()){
                        case "B_WING_L":
                            wingL=w;
                            break;
                        case "B_WING_R":
                            wingR=w;
                            break;
                        case "A_WEAPON_L":
                            fakeWeapL=w;
                            fakeWeapL_sprite=w.getSprite();
                            fakeWeapL_center=w.getSprite().getHeight()/2;
                            break;
                        case "A_WEAPON_R":
                            fakeWeapR=w;
                            fakeWeapR_sprite=w.getSprite();
                            fakeWeapR_center=w.getSprite().getHeight()/2;
                            break;
                        case "WS0002":
                            weapL=w;
                            break;
                        case "WS0001":
                            weapR=w;
                            break;
                        default:
                }
            }
        }
        
        if(engine.isPaused()){return;}
        
        float rotation;        
        if(SHIP.isLanding()||SHIP.getTravelDrive().isActive()){rotation=-1;} else {rotation=1f;}
        //debug
//        if(SHIP.getSystem().isActive()){rotation=-1;} else {rotation=1f;}        
//        engine.addHitParticle(SHIP.getLocation(), SHIP.getLocation(), 50, 1, 0.1f, Color.yellow);
        
        folding=Math.min(1, Math.max(0, folding+(rotation*amount)));
        float fold= MagicAnim.smooth(folding);
        
        if(folding<1){            
            lock=false;
            float facing = SHIP.getFacing();
            
            weapR.setRemainingCooldownTo(1f);
            weapL.setRemainingCooldownTo(1f);
            
            wingL.setCurrAngle(facing+(1-fold)*wingL_angle);
            wingR.setCurrAngle(facing+(1-fold)*wingR_angle);            
            
            fakeWeapL.setCurrAngle(facing+MathUtils.getShortestRotation(facing, weapL.getCurrAngle())*fold+fakeWeapL_angle*(1-fold));
            fakeWeapR.setCurrAngle(facing+MathUtils.getShortestRotation(facing, weapR.getCurrAngle())*fold+fakeWeapR_angle*(1-fold));
            
            fakeWeapL_sprite.setCenterY(fakeWeapL_center+fakeWeapL_offset*fold);
            fakeWeapR_sprite.setCenterY(fakeWeapR_center+fakeWeapR_offset*fold);
            
        } else {
            if(!lock){
                lock=true;                
                wingL.setCurrAngle(SHIP.getFacing());
                wingR.setCurrAngle(SHIP.getFacing());
                fakeWeapL_sprite.setCenterY(fakeWeapL_center+fakeWeapL_offset);
                fakeWeapR_sprite.setCenterY(fakeWeapR_center+fakeWeapR_offset);
            }
            fakeWeapL.setCurrAngle(weapL.getCurrAngle());
            fakeWeapR.setCurrAngle(weapR.getCurrAngle());
        }       
    }
}
