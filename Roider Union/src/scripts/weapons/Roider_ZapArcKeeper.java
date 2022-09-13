package scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;

/**
 * Author: SafariJohn
 */
public class Roider_ZapArcKeeper extends BaseEveryFrameCombatPlugin {
    public static final float ARMING_TIME = 0.5f;

    public static final float ZAP_ARC = 5f; // degrees
    public static final float ZAP_RANGE = 500f;

    private final IntervalUtil interval = new IntervalUtil(0.08f, 0.12f);
    private float arming;
    private boolean armed = false;
	private MissileAPI missile;

    public Roider_ZapArcKeeper(MissileAPI missile) {
        this.missile = missile;
        arming = ARMING_TIME;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
		if (Global.getCombatEngine().isPaused()) return;

        // Countdown to arming
        if (arming > 0) {
            arming -= amount;
            return;
        }

        if (!armed) {
            armed = true;

            // First arc chance
            arcIfCan();

            // End if missile arced
            if (missile.isExpired() || missile.didDamage() || !Global.getCombatEngine().isEntityInPlay(missile)) {
                Global.getCombatEngine().removePlugin(this);
                return;
            }

            // Arm missile
            missile.setCollisionClass(CollisionClass.MISSILE_NO_FF);
        }

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            if (missile.isExpired() || missile.didDamage() || !Global.getCombatEngine().isEntityInPlay(missile)) {
                Global.getCombatEngine().removePlugin(this);
                return;
            }

            arcIfCan();
        }
    }

    private void arcIfCan() {
        // Will just zap first thing that comes up for now
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            // Don't hit allies
            if (ship.getOwner() == missile.getOwner()) continue;

            // Don't hit dead ships
            if (!ship.isAlive()) continue;

            // Must be in range
            float distance = MathUtils.getDistance(ship.getLocation(), missile.getLocation());
            if (distance > ZAP_RANGE) continue;

            // Must be in front of missile
            float misAngle = missile.getFacing();
            float shipAngle = Misc.getAngleInDegrees(missile.getLocation(), ship.getLocation());
            if (Misc.getAngleDiff(misAngle, shipAngle) > ZAP_ARC) continue;

            // Zap!
			float emp = missile.getEmpAmount();
			float dam = missile.getDamageAmount();

            if (ship.isFighter()) emp *= 5f;

            float range = missile.getWeapon().getRange() * 2f;//
            int brightness = (int) (255f * Math.max(Math.min((range - distance) / distance, 0f), 1f));

			Global.getCombatEngine().spawnEmpArc(missile.getSource(), missile.getLocation(), missile, ship,
                        DamageType.ENERGY,
                        dam,
                        emp, // emp
                        100000f, // max range
                        "roider_zap_arc",
                        20f, // thickness
                        new Color(100, 125, 200, brightness),
                        new Color(240, 250, 255, brightness)
                        );

            missile.explode();
            Global.getCombatEngine().removeEntity(missile);

            return;
        }
    }
}
