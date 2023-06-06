package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.LevelBasedEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.rpg.Person;

public class sfc_titan {
    public static final float BASE_RANGE_FLAT_BONUS = 150f;
    public static final float PROJ_SPEED_MULT = 1.25f;
    public static final float FIRE_RATE_MULT = 1.10f;
    public static final float SHIELD_DAMAGE_MULT = 0.85f;
    public static final float SHIELD_EFFECT_MULT = 1.2f;
    public static final float ARMOR_MULT = 1.10f;
    //public static final float MAX_ELITE_SKILLS = 2;

    public static String sfc_titan1 = Global.getSettings().getString("sfc_pagsm", "sfc_titan1");
    public static String sfc_titan2 = Global.getSettings().getString("sfc_pagsm", "sfc_titan2");
    public static String sfc_titan3 = Global.getSettings().getString("sfc_pagsm", "sfc_titan3");
    public static String sfc_titan4 = Global.getSettings().getString("sfc_pagsm", "sfc_titan4");
    public static String sfc_titan5 = Global.getSettings().getString("sfc_pagsm", "sfc_titan5");
    public static String sfc_titan6 = Global.getSettings().getString("sfc_pagsm", "sfc_titan6");

    public static class Level1 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            if (stats.getVariant() != null) {
                if (((stats.getVariant().hasHullMod("sfchwi"))) || ("MSS_Iapetus".equals(stats.getVariant().getHullSpec().getHullId()))) {
                    stats.getEnergyWeaponRangeBonus().modifyFlat(id,BASE_RANGE_FLAT_BONUS);
                    stats.getBallisticWeaponRangeBonus().modifyFlat(id,BASE_RANGE_FLAT_BONUS);
                };
            }
        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getEnergyWeaponRangeBonus().unmodifyFlat(id);
            stats.getBallisticWeaponRangeBonus().unmodifyFlat(id);
        }

        public String getEffectDescription(float level) {
            return sfc_titan1;
            //return "Extends range of Ballistics and Energy weapons by " + (int)(BASE_RANGE_FLAT_BONUS) + " for Iapetus-Class ships.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level2 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            if (stats.getVariant() != null) {
                if (((stats.getVariant().hasHullMod("sfchwi"))) || ("MSS_Iapetus".equals(stats.getVariant().getHullSpec().getHullId()))) {
                    stats.getBallisticProjectileSpeedMult().modifyMult(id, PROJ_SPEED_MULT);
                    stats.getEnergyProjectileSpeedMult().modifyMult(id, PROJ_SPEED_MULT);
                    stats.getMissileMaxSpeedBonus().modifyMult(id, PROJ_SPEED_MULT);
                };
            }
        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getBallisticProjectileSpeedMult().unmodifyMult(id);
            stats.getEnergyProjectileSpeedMult().unmodifyMult(id);
            stats.getMissileMaxSpeedBonus().unmodify();
        }

        public String getEffectDescription(float level) {
            return sfc_titan2;
            //return "Increases projectile and missile speed by " + (int)((PROJ_SPEED_MULT - 1f) * 100f) + "% for Iapetus-class ships.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
    public static class Level3 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            if (stats.getVariant() != null) {
                if (((stats.getVariant().hasHullMod("sfchwi"))) || ("MSS_Iapetus".equals(stats.getVariant().getHullSpec().getHullId()))) {
                    stats.getBallisticRoFMult().modifyMult(id, FIRE_RATE_MULT);
                    stats.getEnergyRoFMult().modifyMult(id, FIRE_RATE_MULT);
                };
            }
        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getBallisticRoFMult().unmodifyMult(id);
            stats.getEnergyRoFMult().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            return sfc_titan3;
            //return "Increases fire rate of Ballistic and Energy weapons by " + (int)((FIRE_RATE_MULT - 1f) * 100f) + "% for Iapetus-class ships.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level4 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            if (stats.getVariant() != null) {
                if (((stats.getVariant().hasHullMod("sfchwi"))) || ("MSS_Iapetus".equals(stats.getVariant().getHullSpec().getHullId()))) {
                    stats.getKineticShieldDamageTakenMult().modifyMult(id, SHIELD_DAMAGE_MULT);
                };
            }

        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getKineticShieldDamageTakenMult().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            return sfc_titan4;
            //return "Decreases kinetic shield damage by 15% for Iapetus-class ships.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level5 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            if (stats.getVariant() != null) {
                if (((stats.getVariant().hasHullMod("sfchwi"))) || ("MSS_Iapetus".equals(stats.getVariant().getHullSpec().getHullId()))) {
                    stats.getShieldArcBonus().modifyMult(id, SHIELD_EFFECT_MULT);
                    stats.getShieldUnfoldRateMult().modifyMult(id, SHIELD_EFFECT_MULT);
                };
            }

        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getShieldArcBonus().unmodifyMult(id);
            stats.getShieldUnfoldRateMult().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            return sfc_titan5;
            //return "Increases shield arc and unfold rate by " + (int)((SHIELD_EFFECT_MULT - 1f) * 100f) + "% for Iapetus-class ships.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level6 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            if (stats.getVariant() != null) {
                if (((stats.getVariant().hasHullMod("sfchwi"))) || ("MSS_Iapetus".equals(stats.getVariant().getHullSpec().getHullId()))) {
                    stats.getArmorBonus().modifyMult(id, ARMOR_MULT);
                };
            }

        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getArmorBonus().unmodify(id);
        }

        public String getEffectDescription(float level) {
            return sfc_titan6;
            //return "Increases ship armor by " + (int)((ARMOR_MULT - 1f) * 100f) + "% for Iapetus-class ships.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }


    /*public static class Level5 implements CharacterStatsSkillEffect {

        public void apply(MutableCharacterStatsAPI stats, String id, float level) {
            stats.getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).modifyFlat(id, MAX_ELITE_SKILLS);
        }

        public void unapply(MutableCharacterStatsAPI stats, String id) {
            stats.getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).unmodify(id);
        }

        public String getEffectDescription(float level) {
            return "+" + (int) MAX_ELITE_SKILLS + " to maximum number of elite skills";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.NONE;
        }
    }

        public static class Level5 implements ShipSkillEffect {

        public void apply(ShipAIPlugin pickShipAI(FleetMemberAPI member, ShipAPI ship)) {
            ShipAPI ship = null;
            boolean iapetus = true;
            if (ship.getVariant() != null) {
                iapetus = (("sfciapetus".equals(ship.getVariant().getHullSpec().getHullId())) || ("sfcsuperiapetus".equals(ship.getVariant().getHullSpec().getHullId())));
            }
            if (iapetus) {
                ship.getShipAI().getConfig().personalityOverride = Personalities.AGGRESSIVE;
            } else {
                ship.getShipAI().getConfig().personalityOverride = Personalities.TIMID;
            }
        }
        public void unapply(ShipAIPlugin pickShipAI(FleetMemberAPI member, ShipAPI ship)) {
            ship.getShipAI().getConfig().personalityOverride = unapply();
        }

        public String getEffectDescription(float level) {
            return "Forces aggressive personality when piloting Iapetus-class ships.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }*/
}
