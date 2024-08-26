package RealisticCombat.settings;

import com.fs.starfarer.api.Global;

import java.awt.*;
import java.util.HashMap;
import java.util.RandomAccess;

public final class Retreat {
    private static final HashMap<String, Float>
            CR_THRESHOLDS = new HashMap<String, Float>() {{
        put("fearless", 0f);
        put("reckless", .3f);
        put("aggressive", .40f);
        put("steady", .45f);
        put("cautious", .5f);
        put("timid", .6f);
    }},

            OVERRIDE_DURATIONS = new HashMap<String, Float>() {{
        put("fearless", Float.POSITIVE_INFINITY);
        put("reckless", 150f);
        put("aggressive", 120f);
        put("steady", 90f);
        put("cautious", 60f);
        put("timid", 30f);
    }};

    private static final String[] COMBAT_DESCRIPTIONS = { "combat", "fighting" };

    private static final String[] CAPACITY_DESCRIPTIONS = {
            "ability", "capability", "capacity", "power", "readiness", "strength"
    };

    private static final HashMap<String, String[]> STATEMENTS = new HashMap<String, String[]>() {{
        put("reckless", new String[] {
                "Even we have to leave.",
                "Too much.  Gotta leave.",
                "We have to go."
        });
        put("aggressive", new String[] {
                "Gotta fall back...",
                "Ludd, we have to let 'em go.",
                "So much for today, turning back."
        });
        put ("steady", new String[] {
                "Retreating.",
                "Heading back.",
                "Falling back."
        });
        put ("cautious", new String[] {
                "Let's retreat!",
                "That's enough, let's retreat!",
                "Retreat now!"
        });
        put ("timid", new String[] {
                "Run! RUN!",
                "Let's get outta here!",
                "Run, run, run!"
        });
    }};

    public static float getOverrideDuration(final String personalityId) {
        return OVERRIDE_DURATIONS.get(personalityId);
    }

    public static float getCRThreshold(final String personalityId) {
        return CR_THRESHOLDS.get(personalityId);
    }

    public static String[] getCombatDescriptions() { return COMBAT_DESCRIPTIONS; }

    public static String[] getCapacityDescriptions() { return CAPACITY_DESCRIPTIONS; }

    public static HashMap<String, String[]> getStatements() { return STATEMENTS; }
}
