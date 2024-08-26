package data.weapons.proj;

//import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
//import com.fs.starfarer.api.impl.campaign.ids.Stats;
//import com.fs.starfarer.api.util.IntervalUtil;

public class AlligatorSurfing implements BeamEffectPlugin {
   
   public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
// Smoke me a kipper
      beam.getDamage().setForceHardFlux(true);
//What a guy!
        return;
    }
}
