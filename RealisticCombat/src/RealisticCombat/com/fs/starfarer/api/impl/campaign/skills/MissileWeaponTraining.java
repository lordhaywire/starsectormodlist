//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package RealisticCombat.com.fs.starfarer.api.impl.campaign.skills;

import RealisticCombat.util.SkillUtils;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.HashSet;

public final class MissileWeaponTraining extends BaseWeaponTraining {

    private static final int COMBAT_READINESS_BONUS = 10;

    private static final HashSet<WeaponType> SLOT_TYPES = new HashSet<WeaponType>() {{
        add(WeaponType.BALLISTIC);
        add(WeaponType.ENERGY);
        add(WeaponType.MISSILE);
        add(WeaponType.COMPOSITE);
        add(WeaponType.HYBRID);
        add(WeaponType.SYNERGY);
        add(WeaponType.UNIVERSAL);
    }};


    public MissileWeaponTraining() {
    }

    public static class Level1 implements ShipSkillEffect {
        public Level1() {
        }

        public void apply(final MutableShipStatsAPI stats,
                          final HullSize hullSize,
                          final String id,
                          final float level)
        {
            final FleetMemberAPI fleetMember = stats.getFleetMember();
            if (fleetMember == null) return;
            final PersonAPI captain = fleetMember.getCaptain();
            if (captain == null) return;
            final HashSet<String> skillIds = getSkillIds(captain);

            if (skillIds.contains("ballistic_mastery")
                || skillIds.contains("energy_weapon_mastery")) return;

            final HashSet<WeaponType> mountable = getMountableTypes(fleetMember.getHullSpec()),
                                      trained = new HashSet<>();

            trained.add(WeaponType.MISSILE);
            for (final WeaponType type : mountable)
                if (SLOT_TYPES.contains(type) && !trained.contains(type)) return;

            stats.getMaxCombatReadiness().modifyFlat(id, COMBAT_READINESS_BONUS,
                    "Weapon training.");
        }

        public void unapply(final MutableShipStatsAPI stats,
                            final HullSize hullSize,
                            final String id)
        {
            final FleetMemberAPI fleetMember = stats.getFleetMember();
            final PersonAPI captain = fleetMember.getCaptain();
            if (captain == null) return;
            final HashSet<String> skillIds = getSkillIds(captain);

            if (skillIds.contains("ballistic_mastery")
                || skillIds.contains("energy_weapon_mastery")) return;

            final HashSet<WeaponType> mountable = getMountableTypes(fleetMember.getHullSpec());
            final HashSet<WeaponType> trained = new HashSet<>();

            trained.add(WeaponType.MISSILE);
            for (final WeaponType type : mountable)
                if (SLOT_TYPES.contains(type) && !trained.contains(type)) return;

            stats.getMaxCombatReadiness().unmodify();
        }

        public String getEffectDescription(float level) {
            return "+" + COMBAT_READINESS_BONUS
                    + "% combat readiness if trained for every weapon type mountable on this ship";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level2 implements ShipSkillEffect {
        public Level2() {
        }

        public void apply(final MutableShipStatsAPI stats,
                          final HullSize hullSize,
                          final String id,
                          final float level)
        {}

        public void unapply(final MutableShipStatsAPI stats,
                            final HullSize hullSize,
                            final String id)
        {}

        public String getEffectDescription(float level) {
            return "";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
}
