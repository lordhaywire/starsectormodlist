package scripts.weapons;

import com.fs.starfarer.api.combat.*;

/**
 * Author: SafariJohn
 */
public class Roider_ZapOnFireEffect implements OnFireEffectPlugin {
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        engine.addPlugin(new Roider_ZapArcKeeper((MissileAPI) projectile));
    }

}
