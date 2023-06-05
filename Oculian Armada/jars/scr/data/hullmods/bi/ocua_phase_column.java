package data.hullmods.bi;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.combat.listeners.WeaponOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import static com.fs.starfarer.api.impl.combat.PhaseCloakStats.BASE_FLUX_LEVEL_FOR_MIN_SPEED;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.everyframe.OCUA_BlockedHullmodDisplayScript;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import org.lwjgl.util.vector.Vector2f;

public class ocua_phase_column extends BaseHullMod {

        public static final float SPEED_BONUS = 50f;
        public static final float CELERATION_BONUS = 50f;
        public static final float CLOAK_MIN_BONUS = 20f;
	public static final float RANGE_RANGE = 60f;
        public static final float SHENA_DAMAGE_BONUS = 25f;
        public static final float KEA_RANGE = 150f;
        public static final float SHIREDAIN_RANGE = 200f;
        public static final float SHENA_RANGE = 100f;
        public static final float PULSE_LASER_RANGE = 250f;
        
	public static final float PHASE_COOLDOWN_PENALTY = 100f;
	public static final float DEGRADE_INCREASE_PERCENT = 50f;
        private static final String phase_column = "ocua_phase_column";
        
        
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
        static {
            BLOCKED_HULLMODS.add("targetingunit");
            BLOCKED_HULLMODS.add("dedicated_targeting_core");
        }
        
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);
                stats.getPhaseCloakCooldownBonus().modifyMult(id, 1f + PHASE_COOLDOWN_PENALTY * 0.01f);
                
                stats.getDynamic().getMod(Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).modifyMult(id, 1f + (CLOAK_MIN_BONUS / 100));
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_RANGE);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_RANGE);
		
                stats.addListener(new WeaponOPCostModifier() {
                @Override
		public int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost) {
			if (weapon.getWeaponId().contains("ocua_shiredain")) return (currCost - (int) 3);
			return currCost;
                    }
		});   
    }
    
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new WeaponBaseRangeModifier() {
                        @Override
			public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
				return 0;
			}
                        @Override
			public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
				return 1f;
			}
                        @Override
			public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
				if (weapon.getId().equals("ocua_kea")) return KEA_RANGE;
				if (weapon.getId().equals("ocua_shiredain")) return SHIREDAIN_RANGE;
				if (weapon.getId().equals("ocua_shena")) return SHENA_RANGE;
				if (weapon.getSpec().getTags().contains("ocua_piro_wep")) return PULSE_LASER_RANGE;
				return 0f;
			}
		});
		ship.addListener(new DamageDealtModifier() {
                    @Override
                    public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
                        WeaponAPI weapon = null;
                        if (param instanceof DamagingProjectileAPI) {
				weapon = ((DamagingProjectileAPI)param).getWeapon();
			}
			if (weapon == null) return null;
			if (!weapon.getId().equals("ocua_shena")) return null;

			String id = "ocua_phase_column_shena_dam_mod";
			damage.getModifier().modifyMult(id, 1f + (SHENA_DAMAGE_BONUS / 100f));
			
			return id;
                    }
		});
                
            for (String tmp : BLOCKED_HULLMODS) {
                if (ship.getVariant().getHullMods().contains(tmp)) {
                    ship.getVariant().removeMod(tmp);
                    OCUA_BlockedHullmodDisplayScript.showBlocked(ship);
                }
            }
	}

        @Override
        public void advanceInCombat(ShipAPI ship, float amount) {
            if (!ship.isAlive()) return;
            
            MutableShipStatsAPI stats = ship.getMutableStats();
            
            if (ship.isPhased()) {
                    stats.getMaxSpeed().modifyFlat(phase_column, SPEED_BONUS);
                    stats.getAcceleration().modifyFlat(phase_column, CELERATION_BONUS);
                    stats.getDeceleration().modifyFlat(phase_column, CELERATION_BONUS);
                    
                    Global.getCombatEngine().maintainStatusForPlayerShip("ocua_phase_column_rnd_1", "graphics/icons/hullsys/phase_cloak.png",
                    "Utena in Phase: ","+" + (int) SPEED_BONUS + " max speed",false);
            }
            else {
                    stats.getMaxSpeed().unmodify(phase_column);
                    stats.getAcceleration().unmodify(phase_column);
                    stats.getDeceleration().unmodify(phase_column);
            }
        }	
        
        protected static final float LOAD_OF_BULL = 3f;
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                Color good = Misc.getPositiveHighlightColor();
                Color gray = Misc.getGrayColor();
		
                LabelAPI bullet;
                tooltip.setBulletedListMode(" • ");
                
		tooltip.addSectionHeading("Improvements", Alignment.MID, opad);
                bullet = tooltip.addPara("Top Speed while phased %s units.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) SPEED_BONUS + "" );
                bullet = tooltip.addPara("Peak Phase Stress level set to %s instead of %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + "60%", "50%" );
		bullet.setHighlight("60%", "50%");
		bullet.setHighlightColors(good, gray);
                bullet = tooltip.addPara("Range of Non-Missile weapons %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) RANGE_RANGE + "%" );
                bullet = tooltip.addPara("Shiredain cost %s OP less, base range %s units.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "" + (int) 3f + "", "+" + (int) SHIREDAIN_RANGE + "" );
		bullet.setHighlight("Shiredain", "" + (int) 3f + "", "OP", "+" + (int) SHIREDAIN_RANGE + "");
		bullet.setHighlightColors(h, good, h, good);
                bullet = tooltip.addPara("Shena %s damage, base range %s units.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) SHENA_DAMAGE_BONUS + "%", "+" + (int) SHENA_RANGE + "" );
		bullet.setHighlight("Shena", "+" + (int) SHENA_DAMAGE_BONUS + "%", "+" + (int) SHENA_RANGE + "");
		bullet.setHighlightColors(h, good, good);
                bullet = tooltip.addPara("Piroutte Small-series (Fouette+) base range %s units.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) PULSE_LASER_RANGE + "" );
		bullet.setHighlight("Piroutte", "(Fouette+)", "+" + (int) PULSE_LASER_RANGE + "");
		bullet.setHighlightColors(h, gray, good);
                bullet = tooltip.addPara("Kea base range %s units.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), good,
                    "+" + (int) KEA_RANGE + "" );
		bullet.setHighlight("Kea", "+" + (int) KEA_RANGE + "");
		bullet.setHighlightColors(h, good);
                
		tooltip.addSectionHeading("Drawbacks", Alignment.MID, opad);
                bullet = tooltip.addPara("Phase Cooldown time %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "+" + (int) (PHASE_COOLDOWN_PENALTY) + "%");
                bullet = tooltip.addPara("CR decay %s faster, once Peak Readiness is depleted.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), bad,
                    "" + (int) DEGRADE_INCREASE_PERCENT + "%" );
                
		tooltip.addSectionHeading("Compatibilities", Alignment.MID, opad);
                bullet = tooltip.addPara("Cannot install %s or %s.", LOAD_OF_BULL, Global.getSettings().getColor("standardTextColor"), h,
                    "Dedicated Targeting Core", "Integrated Targeting Unit" );
                
            tooltip.setBulletedListMode(null);
	}
	
	@Override
	public boolean affectsOPCosts() {
		return true;
	}

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		
	if(index == 0) return "" + (int) ((SPEED_BONUS));
	if(index == 1) return "1.2";
	if(index == 2) return "" + (int) PHASE_COOLDOWN_PENALTY + "%";
        else {
            return null;
        }
    }

}

	