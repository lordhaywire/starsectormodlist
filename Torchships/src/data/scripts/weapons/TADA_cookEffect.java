package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicRender;
import data.scripts.util.TADA_graphicLibEffects;
import static data.scripts.util.TADA_txt.txt;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class TADA_cookEffect implements EveryFrameWeaponEffectPlugin {
    
    private final String AMMO = txt("ammo");
    private boolean runOnce=false, cooking=false, disabled=false, SHADER=false;
    private final IntervalUtil timer = new IntervalUtil(0.1f,0.3f);
//    private int CELLx, CELLy;
    private ArmorGridAPI GRID;
    private int cookCount;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine.isPaused() || disabled) {
            return;
        }
        
        if(!runOnce){
            runOnce=true;
            //distortion + light effects
            SHADER = Global.getSettings().getModManager().isModEnabled("shaderLib");
            
            GRID=weapon.getShip().getArmorGrid();
        }
        
        timer.advance(amount);
        if(timer.intervalElapsed()){    
            if(!cooking){
                if(weapon.isDisabled()) {
                                        
                    Vector2f loc = weapon.getSlot().getLocation();
                    int CELLx = GRID.getLeftOf();
                    int CELLy = Math.round(loc.getX()/GRID.getCellSize())+GRID.getBelow();
                    
                    //debug
//                    weapon.getShip().getArmorGrid().setArmorValue(CELLx, CELLy, 1000);
//                    weapon.getShip().getArmorGrid().setArmorValue(CELLx-1, CELLy, 1000);
                                        
                    if(
                        GRID.getArmorFraction(CELLx, CELLy)<=0.2 
                        || 
                        GRID.getArmorFraction(CELLx-1, CELLy)<=0.2
                            ){
                        //WEAPON EXPLOSION  
                        cookCount=10+weapon.getAmmo();
                        weapon.disable(true);
                        engine.spawnExplosion(weapon.getLocation(), weapon.getShip().getVelocity(), new Color(250,150,50), 300, 4);
                        engine.addHitParticle(weapon.getLocation(), weapon.getShip().getVelocity(), 300, 1, 0.15f, Color.white);
                        engine.addFloatingText(weapon.getLocation(), AMMO, 20, Color.RED, weapon.getShip(), 2f, 4f);
                        for(int i=0; i<=25; i++){
                            engine.addHitParticle(weapon.getLocation(), MathUtils.getRandomPointInCircle(new Vector2f(), 150), 5+5*(float)Math.random(), 0.5f+(float)Math.random(), 2+(float)Math.random(), Color.WHITE);
                        }
                        cooking=true;
                    } 
                }
            } else //AMMO COOKING
            if(Math.random()>0.33){
                cookCount-=1;
                if(cookCount<=0){
                    weapon.disable(true);
                    disabled=true;
                }

                Vector2f loc = MathUtils.getRandomPointInCircle(weapon.getLocation(), 50);
                boolean onScreen = MagicRender.screenCheck(0.5f, loc);
                if(Math.random()>0.5){
                    if(onScreen){
                        engine.spawnExplosion(loc, weapon.getShip().getVelocity(), Color.DARK_GRAY, 100, 3);
                        engine.spawnExplosion(loc, weapon.getShip().getVelocity(), Color.ORANGE, 100, 1);
                        engine.addHitParticle(loc, weapon.getShip().getVelocity(), 150, 1, 0.15f, Color.YELLOW);
                        for(int i=0; i<=10; i++){
                            engine.addHitParticle(loc, MathUtils.getRandomPointInCircle(new Vector2f(), 100), 3+7*(float)Math.random(), 0.5f+(float)Math.random(), 2+(float)Math.random(), Color.WHITE);
                        }
                        if(SHADER){
                            TADA_graphicLibEffects.customLight(loc, weapon.getShip(), 200, 0.5f, new Color(255,160,50,255), 0.05f, 0.1f, 0.15f);
                        }
                    }
                    engine.applyDamage(weapon.getShip(), loc, 150, DamageType.HIGH_EXPLOSIVE, 0, true, false, weapon.getShip());
                } else {      
                    if(onScreen){
                        engine.spawnExplosion(loc, weapon.getShip().getVelocity(), Color.DARK_GRAY, 100, 3);
                        engine.spawnExplosion(loc, weapon.getShip().getVelocity(), Color.ORANGE, 100, 1);
                        engine.addHitParticle(loc, weapon.getShip().getVelocity(), 150, 1, 0.15f, Color.YELLOW);  
                        if(SHADER){
                            TADA_graphicLibEffects.customLight(loc, weapon.getShip(), 200, 0.5f, new Color(255,160,50,255), 0.05f, 0.1f, 0.15f);
                        }
                    }
                    engine.spawnProjectile(weapon.getShip(), weapon, "hammer", loc, weapon.getCurrAngle()+(-45+90*(float)Math.random()), weapon.getShip().getVelocity());
                }
            }
        }        
    }
}
