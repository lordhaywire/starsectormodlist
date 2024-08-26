package RealisticCombat.com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public final class ArmorAngling {

    public static final float OBLIQUE_ANGLE_FACTOR = 0.9f;

    public ArmorAngling() {}

    public static class Level1 implements ShipSkillEffect {

        public Level1() {}

        public void apply(final MutableShipStatsAPI stats,
                          final HullSize hullSize,
                          final String fleetMemberId,
                          final float level)
        {}

        public void unapply(final MutableShipStatsAPI stats,
                            final HullSize hullSize,
                            final String fleetMemberId)
        {}

        public String getEffectDescription(final float level) {
            return (int) ((1 - OBLIQUE_ANGLE_FACTOR ) * 100) + "% lesser oblique armor angle, " +
                    "down to 0 degrees.  Counters and countered by Weakpoint Familiarization.";
        }

        public String getEffectPerLevelDescription() { return null; }

        public ScopeDescription getScopeDescription() { return ScopeDescription.PILOTED_SHIP; }
    }

    public static class Level2 implements ShipSkillEffect {

        public Level2() {}

        public void apply(final MutableShipStatsAPI stats,
                          final HullSize hullSize,
                          final String fleetMemberId,
                          final float level)
        {}

        public void unapply(final MutableShipStatsAPI stats,
                            final HullSize hullSize,
                            final String fleetMemberId)
        {}

        public String getEffectDescription(final float level) { return ""; }

        public String getEffectPerLevelDescription() { return null; }

        public ScopeDescription getScopeDescription() { return ScopeDescription.PILOTED_SHIP; }
    }
}
