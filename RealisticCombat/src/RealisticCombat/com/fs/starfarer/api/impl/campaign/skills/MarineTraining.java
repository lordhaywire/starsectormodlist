//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package RealisticCombat.com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.impl.campaign.skills.TacticalDrills;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public final class MarineTraining {

    private static final int ATTACK_BONUS = 50;
    private static final float CASUALTIES_MULT = 0.75f;

    public MarineTraining() {
    }

    public static class Level1 extends BaseSkillEffectDescription implements FleetStatsSkillEffect {
        public Level1() {
        }

        public void apply(final MutableFleetStatsAPI stats, final String id, final float level) {
            stats.getDynamic().getMod("ground_attack_mod").modifyPercent(id,
                    TacticalDrills.ATTACK_BONUS, "Tactical drills");
        }

        public void unapply(final MutableFleetStatsAPI stats, final String id) {
            stats.getDynamic().getMod("ground_attack_mod").unmodifyPercent(id);
        }

        public void createCustomDescription(final MutableCharacterStatsAPI stats,
                                            final SkillSpecAPI skill,
                                            final TooltipMakerAPI info,
                                            final float width) {
            this.init(stats, skill);
            float opad = 10.0F;
            Color c = Misc.getBasePlayerColor();
            info.addPara("Affects: %s", opad + 5.0F, Misc.getGrayColor(), c,
                    new String[]{"ground operations"});
            info.addSpacer(opad);
            info.addPara("+%s effectiveness of such ground operations as raids", 0.0F,
                    this.hc, this.hc, new String[]{TacticalDrills.ATTACK_BONUS + "%"});
        }

        public String getEffectDescription(final float level) {
            return "+" + TacticalDrills.ATTACK_BONUS + "% effectiveness of ground operations such as raids";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.FLEET;
        }
    }

    public static class Level2 implements FleetStatsSkillEffect {
        public Level2() {
        }

        public void apply(final MutableFleetStatsAPI stats, final String id, final float level) {
            stats.getDynamic().getStat("ground_attack_casualties_mult").modifyMult(id, TacticalDrills.CASUALTIES_MULT, "Tactical drills");
        }

        public void unapply(final MutableFleetStatsAPI stats, final String id) {
            stats.getDynamic().getStat("ground_attack_casualties_mult").unmodifyMult(id);
        }

        public String getEffectDescription(final float level) {
            return "-" + Math.round((1.0F - TacticalDrills.CASUALTIES_MULT) * 100.0F) + "% marine casualties of such ground operations as raids";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.FLEET;
        }
    }
}
