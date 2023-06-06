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

public class sfc_iapetus {
    public static final float FLUX_CAPACITY_MULT = 1.25f;
    public static final float FLUX_DISP_MULT = 1.20f;
    public static final float FLUX_WEAPON_MULT = 0.75f;
    public static final float SHIELD_UPKEEP_MULT = 50f;
    public static final float AMMO_REGEN = 1.5f;
    public static final float PD_RANGE_MULT = 1.25f;
    public static final float SYS_COOLDOWN_MULT = 0.8f;
    //public static final float MAX_ELITE_SKILLS = 2;

    public static String sfc_iapetus1 = Global.getSettings().getString("sfc_pagsm", "sfc_iapetus1");
    public static String sfc_iapetus2 = Global.getSettings().getString("sfc_pagsm", "sfc_iapetus2");
    public static String sfc_iapetus3 = Global.getSettings().getString("sfc_pagsm", "sfc_iapetus3");
    public static String sfc_iapetus4 = Global.getSettings().getString("sfc_pagsm", "sfc_iapetus4");
    public static String sfc_iapetus5 = Global.getSettings().getString("sfc_pagsm", "sfc_iapetus5");
    public static String sfc_iapetus6 = Global.getSettings().getString("sfc_pagsm", "sfc_iapetus6");

    public static class Level1 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            if (stats.getVariant() != null) {
                if (((stats.getVariant().hasHullMod("sfchwi"))) || ("MSS_Iapetus".equals(stats.getVariant().getHullSpec().getHullId()))) {
                    stats.getFluxCapacity().modifyMult(id,FLUX_CAPACITY_MULT);
                }
            }
        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getFluxCapacity().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            return sfc_iapetus1;
            //return "Increases flux capacity by " + (int)((FLUX_CAPACITY_MULT - 1f) * 100f) + "% for Iapetus-Class ships.";
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
                    stats.getFluxDissipation().modifyMult(id, FLUX_DISP_MULT);
                };
            }
        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getFluxDissipation().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            return sfc_iapetus2;
            //return "Increases flux dissipation by " + (int)((FLUX_DISP_MULT - 1f) * 100f) + "% for Iapetus-class ships.";
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
                    stats.getBallisticWeaponFluxCostMod().modifyMult(id, FLUX_WEAPON_MULT);
                    stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_WEAPON_MULT);
                };
            }
        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getBallisticWeaponFluxCostMod().unmodifyMult(id);
            stats.getEnergyWeaponFluxCostMod().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            return sfc_iapetus3;
            //return "Decreases flux cost of Ballistic and Energy weapons by " + (int)((FLUX_WEAPON_MULT - 1f) * 100f) + "% for Iapetus-class ships.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    /*public static class Level4 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            if (stats.getVariant() != null) {
                if (("sfciapetus".equals(stats.getVariant().getHullSpec().getHullId())) || ("sfcsuperiapetus".equals(stats.getVariant().getHullSpec().getHullId()))) {
                    stats.getShieldUpkeepMult().modifyMult(id, 1f - SHIELD_UPKEEP_MULT * 0.01f);
                };
            }

        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getShieldUpkeepMult().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {

            return "Decreases shield upkeep by 50% for Iapetus-class ships.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }*/

    public static class Level4 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            if (stats.getVariant() != null) {
                if (((stats.getVariant().hasHullMod("sfchwi"))) || ("MSS_Iapetus".equals(stats.getVariant().getHullSpec().getHullId()))) {
                    stats.getBallisticAmmoRegenMult().modifyMult(id, AMMO_REGEN);
                    stats.getEnergyAmmoRegenMult().modifyMult(id, AMMO_REGEN);
                };
            }

        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getBallisticAmmoRegenMult().unmodifyMult(id);
            stats.getEnergyAmmoRegenMult().unmodify(id);
        }

        public String getEffectDescription(float level) {
            return sfc_iapetus4;
            //return "Increases ammo regen by " + (int)((AMMO_REGEN - 1f) * 100f) + "% for Iapetus class ships.";
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
                    stats.getBeamPDWeaponRangeBonus().modifyMult(id, PD_RANGE_MULT);
                    stats.getNonBeamPDWeaponRangeBonus().modifyMult(id, PD_RANGE_MULT);
                };
            }

        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getBeamPDWeaponRangeBonus().unmodifyMult(id);
            stats.getNonBeamPDWeaponRangeBonus();
        }

        public String getEffectDescription(float level) {
            return sfc_iapetus5;
            //return "Increases PD range by " + (int)((PD_RANGE_MULT - 1f) * 100f) + "% for Iapetus-class ships.";
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
                    stats.getSystemRegenBonus().modifyMult(id, SYS_COOLDOWN_MULT);
                };
            }

        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getSystemCooldownBonus().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            return sfc_iapetus6;
            //return "Decreases system cooldown by 20% for Iapetus-class ships.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
    /* public static class Level5 implements CharacterStatsSkillEffect {

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
