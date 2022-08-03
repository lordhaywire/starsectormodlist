package scripts.campaign.rulecmd.expeditionSpecials;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.StatBonus;
import com.thoughtworks.xstream.XStream;
import org.lwjgl.util.vector.Vector2f;

/**
 * Author: SafariJohn
 */
public class Roider_PingTrapScript implements EveryFrameScript {
    public static final float UPTIME = 1f;
    public static final float DURATION = 2f;
    public static final float DOWNTIME = 1f;

    public static final float PROFILE_PENALTY = 5000f;

    public static void aliasAttributes(XStream x) {
        x.aliasAttribute(Roider_PingTrapScript.class, "effectLevel", "e");
        x.aliasAttribute(Roider_PingTrapScript.class, "duration", "r");
        x.aliasAttribute(Roider_PingTrapScript.class, "up", "u");
        x.aliasAttribute(Roider_PingTrapScript.class, "down", "d");
        x.aliasAttribute(Roider_PingTrapScript.class, "start", "s");
    }

    private float effectLevel;
    private float duration;
    private boolean up;
    private boolean down;
    private boolean start;

    public Roider_PingTrapScript() {
        effectLevel = 0;
        duration = DURATION;
        start = true;
        up = true;
        down = false;
    }

    @Override
    public void advance(float amount) {
        if (up) {
            if (start) {
                Global.getSoundPlayer().playSound("world_sensor_burst_on", 1f, 1f,
                            Global.getSector().getPlayerFleet().getLocation(), new Vector2f(0, 0));
                start = false;
            }

            effectLevel += amount / UPTIME;

            if (effectLevel >= 1) {
                effectLevel = 1;
                up = false;
            }
        }
        if (down) {
            effectLevel -= amount / DOWNTIME;
        }
        if (!up && !down) {
            duration -= amount;

            if (duration <= 0) down = true;
        }

        StatBonus profile = Global.getSector().getPlayerFleet().getStats().getSensorProfileMod();

        if (effectLevel > 0) profile.modifyFlat("roider_pingTrap", PROFILE_PENALTY * effectLevel);
        else profile.unmodifyFlat("roider_pingTrap");
    }

    @Override
    public boolean isDone() {
        return down && effectLevel <= 0;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

}
