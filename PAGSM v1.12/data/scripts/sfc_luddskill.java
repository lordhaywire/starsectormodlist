package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class sfc_luddskill {
    public static final float SPEED_BONUS = 1.2f;
    public static final float TURN_BONUS = 1.3f;
    public static final float ACCEL_BONUS = 1.25f;
    public static final float ZERO_FLUX_BONUS = 10f;

    public static String sfc_luddskill1 = Global.getSettings().getString("sfc_pagsm", "sfc_luddskill1");
    public static String sfc_luddskill2 = Global.getSettings().getString("sfc_pagsm", "sfc_luddskill2");
    public static String sfc_luddskill3 = Global.getSettings().getString("sfc_pagsm", "sfc_luddskill3");
    public static String sfc_luddskill4 = Global.getSettings().getString("sfc_pagsm", "sfc_luddskill4");

    public static class Level1 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getMaxSpeed().modifyMult(id, SPEED_BONUS);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getMaxSpeed().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            return sfc_luddskill1;
            //return "20% max speed.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level2 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getMaxTurnRate().modifyMult(id, TURN_BONUS);
            stats.getTurnAcceleration().modifyMult(id, TURN_BONUS);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getMaxTurnRate().unmodifyMult(id);
            stats.getTurnAcceleration().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            return sfc_luddskill2;
            //return "30% faster ship turn rate.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
    public static class Level3 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getAcceleration().modifyMult(id, ACCEL_BONUS);
            stats.getDeceleration().modifyMult(id, ACCEL_BONUS);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getAcceleration().unmodifyMult(id);
            stats.getDeceleration().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            return sfc_luddskill3;
            //return "25% faster ship acceleration and deceleration.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level4 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_BONUS * 2.5f);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getZeroFluxSpeedBoost().unmodifyFlat(id);
        }

        public String getEffectDescription(float level) {
            return sfc_luddskill4;
            //return "+25su to Zero Flux Speed Boost.";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
}

