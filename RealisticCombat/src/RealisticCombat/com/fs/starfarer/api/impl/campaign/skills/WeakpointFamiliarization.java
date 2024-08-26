package RealisticCombat.com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public final class WeakpointFamiliarization {

    public static final float OBLIQUE_ANGLE_FACTOR = 1.1f;

    public WeakpointFamiliarization() {}

    public static class Level1 implements ShipSkillEffect {

        public Level1() {}

        public void apply(final MutableShipStatsAPI stats,
                          final ShipAPI.HullSize hullSize,
                          final String fleetMemberId,
                          final float level) {}

        public void unapply(final MutableShipStatsAPI stats,
                            final ShipAPI.HullSize hullSize,
                            final String fleetMemberId) {}

        public String getEffectDescription(final float level) {
            return (int) ((OBLIQUE_ANGLE_FACTOR - 1) * 100) + "% greater oblique armor angle" +
                    "when hitting, up to 90 degrees.  Counters and countered by Armor Angling.";
        }

        public String getEffectPerLevelDescription() {return null;}

        public ScopeDescription getScopeDescription() {return ScopeDescription.PILOTED_SHIP;}
    }

    public static class Level2 implements ShipSkillEffect {

        public Level2() {}

        public void apply(final MutableShipStatsAPI stats,
                          final ShipAPI.HullSize hullSize,
                          final String fleetMemberId,
                          final float level) {}

        public void unapply(final MutableShipStatsAPI stats,
                            final ShipAPI.HullSize hullSize,
                            final String fleetMemberId) {}

        public String getEffectDescription(final float level) { return ""; }

        public String getEffectPerLevelDescription() {return null;}

        public ScopeDescription getScopeDescription() {return ScopeDescription.PILOTED_SHIP;}
    }
}
