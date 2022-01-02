package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;

public class TADA_urchinEffect implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false, hidden=false, fired=false;
    private SpriteAPI barrel;
    private float barrelHeight=0, muzzleH, muzzleT, recoil=0;
    private final float maxRecoil=-15;
    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1 || hidden){return;}
        
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
                muzzleH=weapon.getSpec().getHardpointFireOffsets().get(0).getX();
                muzzleT=weapon.getSpec().getTurretFireOffsets().get(0).getX();
                weapon.ensureClonedSpec();
            }
            return;
        }
        
        if(weapon.getChargeLevel()==1){
            fired=true;
            recoil=Math.min(1, recoil+amount);
        } else if(!fired){
            return;
        } else {
            recoil=Math.max(0, recoil-(amount*0.25f));
        }
        barrel.setCenterY(barrelHeight-(recoil*maxRecoil));
        weapon.getSpec().getHardpointFireOffsets().set(0, new Vector2f(muzzleH+(recoil*maxRecoil),0));
        weapon.getSpec().getTurretFireOffsets().set(0, new Vector2f(muzzleT+(recoil*maxRecoil),0));
        if(recoil==0){
            fired=false;
        }
    }
}